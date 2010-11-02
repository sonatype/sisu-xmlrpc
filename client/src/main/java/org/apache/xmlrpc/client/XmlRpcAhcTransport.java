package org.apache.xmlrpc.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.common.XmlRpcStreamConfig;
import org.apache.xmlrpc.common.XmlRpcStreamRequestConfig;
import org.apache.xmlrpc.util.XmlRpcIOException;
import org.xml.sax.SAXException;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClient.BoundRequestBuilder;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.AsyncHttpClientConfig.Builder;
import com.ning.http.client.FluentCaseInsensitiveStringsMap;
import com.ning.http.client.Realm;
import com.ning.http.client.Realm.RealmBuilder;
import com.ning.http.client.Request;
import com.ning.http.client.Response;

// Timeouts x
// HTTPS
// Proxies
// GZip
// Encoding
// Redirects
public class XmlRpcAhcTransport
    extends XmlRpcHttpTransport
{
    private static final String userAgent = USER_AGENT + " (Async HTTP Client Transport)";

    private AsyncHttpClient client;

    private Builder asyncClientConfigBuilder;
    
    private RealmBuilder realmBuilder;
    
    private BoundRequestBuilder requestBuilder;

    private FluentCaseInsensitiveStringsMap headers;

    private XmlRpcHttpClientConfig config;

    public XmlRpcAhcTransport( XmlRpcAhcTransportFactory xmlRpcAhcTransportFactory )
    {
        super( xmlRpcAhcTransportFactory.getClient(), userAgent );        
        asyncClientConfigBuilder = new AsyncHttpClientConfig.Builder();
        headers = new FluentCaseInsensitiveStringsMap();
    }

    //
    // Request
    //

    @Override
    protected void initHttpHeaders( XmlRpcRequest xmlRpcRequest )
        throws XmlRpcClientException
    {
        config = (XmlRpcHttpClientConfig) xmlRpcRequest.getConfig();
        
        super.initHttpHeaders( xmlRpcRequest );
        
        if (config.getConnectionTimeout() != 0)
        {
            asyncClientConfigBuilder.setConnectionTimeoutInMs( config.getConnectionTimeout() );
        }
        
        if (config.getReplyTimeout() != 0)
        {
            asyncClientConfigBuilder.setRequestTimeoutInMs( config.getReplyTimeout() );
        }
        
        //
        // This doesn't seem like a great place, but I'm copying the commons http client
        //
        client = new AsyncHttpClient( asyncClientConfigBuilder.build() );
        
        requestBuilder = client.preparePost( config.getServerURL().toString() );
        
        if( realmBuilder != null )
        {
            requestBuilder.setRealm( realmBuilder.build() );
        }
        
        requestBuilder.setFollowRedirects( true );        
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

            realmBuilder = (new Realm.RealmBuilder()).setPrincipal( userName ).setPassword( pConfig.getBasicPassword() )
                    .setUsePreemptiveAuth( true ).setEnconding( enc );
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
        requestBuilder.setBody( new Request.EntityWriter()
        {
            public void writeEntity( OutputStream out )
                throws IOException
            {
                try
                {
                    writer.write( out );
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
        } );
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
            requestBuilder.build();
            
            // Check the status of the return
            Response response = requestBuilder.execute().get();

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
