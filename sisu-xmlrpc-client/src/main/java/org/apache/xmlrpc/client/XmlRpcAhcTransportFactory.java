package org.apache.xmlrpc.client;

public class XmlRpcAhcTransportFactory
    extends XmlRpcTransportFactoryImpl
{
    public XmlRpcAhcTransportFactory( XmlRpcClient xmlRpcClient )
    {
        super( xmlRpcClient );
    }

    public XmlRpcTransport getTransport()
    {
        return new XmlRpcAhcTransport( this );
    }

}
