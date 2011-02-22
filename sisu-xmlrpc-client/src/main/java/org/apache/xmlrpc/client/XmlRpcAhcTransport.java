package org.apache.xmlrpc.client;

import com.ning.http.client.FluentCaseInsensitiveStringsMap;
import com.ning.http.client.Response;
import com.ning.http.client.SimpleAsyncHttpClient;
import com.ning.http.client.generators.ByteArrayBodyGenerator;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.common.XmlRpcStreamConfig;
import org.apache.xmlrpc.common.XmlRpcStreamRequestConfig;
import org.apache.xmlrpc.util.XmlRpcIOException;
import org.xml.sax.SAXException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;

// Encoding
public class XmlRpcAhcTransport
    extends XmlRpcHttpTransport
{
    private static final String userAgent = USER_AGENT + " (Async HTTP Client Transport)";

    private SimpleAsyncHttpClient client;

    private final SimpleAsyncHttpClient.Builder builder = new SimpleAsyncHttpClient.Builder();

    private FluentCaseInsensitiveStringsMap headers;

    private final ByteArrayOutputStream byteOs = new ByteArrayOutputStream();

    public XmlRpcAhcTransport( XmlRpcAhcTransportFactory xmlRpcAhcTransportFactory )
    {
        super( xmlRpcAhcTransportFactory.getClient(), userAgent );
        headers = new FluentCaseInsensitiveStringsMap();
    }

    //
    // Request
    //

    @Override
    protected void initHttpHeaders( XmlRpcRequest xmlRpcRequest )
        throws XmlRpcClientException
    {
        if ( !XmlRpcClientConfigImpl.class.isAssignableFrom( xmlRpcRequest.getConfig().getClass() ) )
        {
            throw new XmlRpcClientException( "Invalid XmlRpcRequest", null );
        }

        XmlRpcClientConfigImpl config = XmlRpcClientConfigImpl.class.cast( xmlRpcRequest.getConfig() );

        super.initHttpHeaders( xmlRpcRequest );

        configureProxy( config );
        configureTimeout( config );

        if ( config.getSslContext() != null )
        {
            builder.setSSLContext( config.getSslContext() );
        }

        if (config.isGzipRequesting())
        {
            builder.setRequestCompressionLevel(6);
        }

        builder.setUrl( config.getServerURL().toString() ).setFollowRedirects( config.isFollowRedirect() );
    }

    private void configureProxy( XmlRpcClientConfigImpl config )
    {
        if ( config.getProxyHost() != null )
        {
            builder.setProxyHost( config.getProxyHost() );
        }

        if ( config.getProxyPort() != 0 )
        {
            builder.setProxyPort( config.getProxyPort() );
        }

        if ( config.getProxyPrincipal() != null )
        {
            builder.setProxyPrincipal( config.getProxyPrincipal() );
        }

        if ( config.getProxyPassword() != null )
        {
            builder.setProxyPassword( config.getProxyPassword() );
        }
    }

    private void configureTimeout( XmlRpcClientConfigImpl config )
    {
        if ( config.getConnectionTimeout() != 0 )
        {
            builder.setConnectionTimeoutInMs( config.getConnectionTimeout() );
        }

        if ( config.getReplyTimeout() != 0 )
        {
            builder.setRequestTimeoutInMs( config.getReplyTimeout() );
        }

        if ( config.getConnectiomIdleTimeout() > 0 )
        {
            builder.setConnectionTimeoutInMs( config.getConnectiomIdleTimeout() );
        }

    }

    @Override
    protected void setCredentials( XmlRpcHttpClientConfig pConfig )
        throws XmlRpcClientException
    {
        String userName = pConfig.getBasicUserName();

        if ( userName != null )
        {
            String enc = pConfig.getBasicEncoding();
            if ( enc == null )
            {
                enc = XmlRpcStreamConfig.UTF8_ENCODING;
            }

            builder.setRealmPrincipal( userName ).setRealmPassword(
                pConfig.getBasicPassword() ).setRealmUsePreemptiveAuth( true ).setRealmEnconding( enc );
        }
    }

    @Override
    protected void setRequestHeader( String headerKey, String headerValue )
    {
        headers.add( headerKey, headerValue );
    }

    @Override
    protected void writeRequest( final ReqWriter writer )
        throws XmlRpcException, IOException, SAXException
    {
        try
        {
            writer.write( byteOs );
        }
        catch ( XmlRpcException e )
        {
            throw new XmlRpcIOException( e );
        }
        catch ( SAXException e )
        {
            throw new XmlRpcIOException( e );
        }
    }

    //
    // Response
    //

    @Override
    protected boolean isResponseGzipCompressed( XmlRpcStreamRequestConfig pConfig )
    {
        return false;
    }

    @Override
    protected InputStream getInputStream()
        throws XmlRpcException
    {
        try
        {
            client = builder.setHeaders( headers ).build();

            // Check the status of the return
            Response response = client.post( new ByteArrayBodyGenerator( byteOs.toByteArray() ) ).get();

            return response.getResponseBodyAsStream();
        }
        catch ( InterruptedException e )
        {
            throw new XmlRpcClientException( "Interruption during communication: " + e.getMessage(), e );
        }
        catch ( ExecutionException e )
        {
            throw new XmlRpcClientException( "Execution error during communication: " + e.getMessage(), e );
        }
        catch ( IOException e )
        {
            throw new XmlRpcClientException( "I/O error in server communication: " + e.getMessage(), e );
        }
        finally
        {
            byteOs.reset();
        }
    }

    @Override
    protected void close()
        throws XmlRpcClientException
    {
        if ( client != null )
        {
            client.close();
        }
    }
}
