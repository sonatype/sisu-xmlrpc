/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.xmlrpc.client;

import org.apache.xmlrpc.common.XmlRpcHttpRequestConfigImpl;
import org.apache.xmlrpc.common.XmlRpcRequestProcessor;

import javax.net.ssl.SSLContext;
import java.io.Serializable;
import java.net.URL;


/**
 * Default implementation of a clients request configuration.
 */
public class XmlRpcClientConfigImpl
    extends XmlRpcHttpRequestConfigImpl
    implements XmlRpcHttpClientConfig, XmlRpcLocalClientConfig, Cloneable, Serializable
{
    private static final long serialVersionUID = 4121131450507800889L;

    private URL serverURL;

    private XmlRpcRequestProcessor xmlRpcServer;

    private String userAgent;

    private String proxyHost;

    private int proxyPort = 0;

    private String proxyPrincipal;

    private String proxyPassword;

    private int connectiomIdleTimeout;

    private boolean followRedirect = true;

    private SSLContext sslContext;

    /**
     * Creates a new client configuration with default settings.
     */
    public XmlRpcClientConfigImpl()
    {
    }

    /**
     * Creates a clone of this client configuration.
     *
     * @return A clone of this configuration.
     */
    public XmlRpcClientConfigImpl cloneMe()
    {
        try
        {
            return (XmlRpcClientConfigImpl) clone();
        }
        catch ( CloneNotSupportedException e )
        {
            throw new IllegalStateException( "Unable to create my clone" );
        }
    }

    /**
     * Sets the servers URL.
     *
     * @param pURL Servers URL
     */
    public void setServerURL( URL pURL )
    {
        serverURL = pURL;
    }

    public URL getServerURL()
    {
        return serverURL;
    }

    /**
     * Returns the {@link XmlRpcRequestProcessor} being invoked.
     *
     * @param pServer Server object being invoked. This will typically
     *                be a singleton instance, but could as well create a new
     *                instance with any call.
     */
    public void setXmlRpcServer( XmlRpcRequestProcessor pServer )
    {
        xmlRpcServer = pServer;
    }

    public XmlRpcRequestProcessor getXmlRpcServer()
    {
        return xmlRpcServer;
    }

    /**
     * Returns the user agent header to use
     *
     * @return the http user agent header to set when doing xmlrpc requests
     */
    public String getUserAgent()
    {
        return userAgent;
    }

    /**
     * @param pUserAgent the http user agent header to set when doing xmlrpc requests
     */
    public void setUserAgent( String pUserAgent )
    {
        userAgent = pUserAgent;
    }

    public String getProxyHost()
    {
        return proxyHost;
    }

    public void setProxyHost( String proxyHost )
    {
        this.proxyHost = proxyHost;
    }

    public int getProxyPort()
    {
        return proxyPort;
    }

    public void setProxyPort( int proxyPort )
    {
        this.proxyPort = proxyPort;
    }

    public String getProxyPrincipal()
    {
        return proxyPrincipal;
    }

    public void setProxyPrincipal( String proxyPrincipal )
    {
        this.proxyPrincipal = proxyPrincipal;
    }

    public String getProxyPassword()
    {
        return proxyPassword;
    }

    public void setProxyPassword( String proxyPassword )
    {
        this.proxyPassword = proxyPassword;
    }

    public int getConnectiomIdleTimeout()
    {
        return connectiomIdleTimeout;
    }

    public void setConnectiomIdleTimeout( int connectiomIdleTimeout )
    {
        this.connectiomIdleTimeout = connectiomIdleTimeout;
    }

    public SSLContext getSslContext()
    {
        return sslContext;
    }

    public void setSslContext( SSLContext sslContext )
    {
        this.sslContext = sslContext;
    }

    public boolean isFollowRedirect()
    {
        return followRedirect;
    }

    public void setFollowRedirect( boolean followRedirect )
    {
        this.followRedirect = followRedirect;
    }


}
