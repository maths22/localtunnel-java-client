package it.com.atlassian.localtunnel;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.util.Map;

import com.atlassian.localtunnel.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @since version
 */
public class LocalTunnelTest
{
    @Test
    public void discoverBackendPort() throws Exception
    {
        HttpClient httpclient = new DefaultHttpClient();

        HttpGet httpGet = new HttpGet("http://v2.localtunnel.com");
        httpGet.addHeader("Accept-Encoding", "identity");
        httpGet.addHeader("Host", "_backend.v2.localtunnel.com");
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        int bport = Integer.parseInt(httpclient.execute(httpGet, responseHandler));

        assertTrue((bport > 0));
    }

    @Test
    public void discoverLocalBackendPort() throws Exception
    {
        HttpClient httpclient = new DefaultHttpClient();

        HttpGet httpGet = new HttpGet("http://jd-linux:8004");
        httpGet.addHeader("Accept-Encoding", "identity");
        httpGet.addHeader("Host", "_backend.localhost");
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        int bport = Integer.parseInt(httpclient.execute(httpGet, responseHandler));

        assertTrue((bport > 0));
    }

    @Test
    public void localRun() throws Exception
    {

        DefaultLocalTunnelFactory factory = new DefaultLocalTunnelFactory();
        LocalTunnel tunnel = factory.create(8000, "iamcool.dev");

        tunnel.start();
        Thread.sleep(1000 * 80);
        System.out.println("stopping...");
        tunnel.stop();
        System.out.println("stopped");
    }

    @Test
    public void remoteRun() throws Exception
    {

        DefaultLocalTunnelFactory factory = new DefaultLocalTunnelFactory();
        LocalTunnel tunnel = factory.create(8000);

        tunnel.start();
        Thread.sleep(1000 * 80);
        System.out.println("stopping...");
        tunnel.stop();
        System.out.println("stopped");
    }

    @Test
    public void localProtocolSendReceive() throws Exception
    {
        Socket control = null;
        try
        {
            DefaultLocalTunnelFactory factory = new DefaultLocalTunnelFactory();
            LocalTunnel tunnel = factory.create(8000, "localhost:8004");

            LocalTunnelProtocol protocol = new LocalTunnelProtocol();
            
            control = tunnel.createControlSocket(null);
            protocol.sendVersion(control);
            protocol.sendMessage(control, protocol.controlRequest(tunnel.getTunnelName(), tunnel.getClientName()));

            String response = protocol.receiveMessage(control);

            assertNotNull(response);
        }
        finally
        {
            if (null != control)
            {
                control.close();
            }
        }

    }
    
    @Test
    public void protocolSendReceive() throws Exception
    {
        Socket control = null;
        try
        {
            DefaultLocalTunnelFactory factory = new DefaultLocalTunnelFactory();
            DefaultLocalTunnel tunnel = (DefaultLocalTunnel) factory.create(8000);

            LocalTunnelProtocol protocol = new LocalTunnelProtocol();

            control = tunnel.createControlSocket(null);
            protocol.sendVersion(control);
            protocol.sendMessage(control, protocol.controlRequest(tunnel.getTunnelName(), tunnel.getClientName()));

            String response = protocol.receiveMessage(control);

            assertNotNull(response);
        }
        finally
        {
            if (null != control)
            {
                control.close();
            }
        }

    }

    @Test
    public void localTunnelCreated() throws Exception
    {
        LocalTunnelFactory factory = new DefaultLocalTunnelFactory();

        LocalTunnel tunnel = factory.create(8080);

        String remoteHost = tunnel.getRemoteHost();

        //ping the host
    }

}
