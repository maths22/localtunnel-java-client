package com.atlassian.localtunnel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.atlassian.localtunnel.data.Backend;
import com.atlassian.localtunnel.data.ControlTunnelResponse;

import com.google.gson.Gson;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * @since version
 */
public class DefaultLocalTunnel implements LocalTunnel
{
    private final int port;
    private final String tunnelName;
    private final String clientName;
    private final String host;
    private String remoteHost;
    
    private ControlPingPongService pingPongService;
    private ProxyService proxyService;

    public DefaultLocalTunnel(int port, String tunnelName, String clientName, String host)
    {
        this.port = port;
        this.tunnelName = tunnelName;
        this.clientName = clientName;
        this.host = host;
    }

    @Override
    public void start() throws IOException
    {
        LocalTunnelProtocol protocol = new LocalTunnelProtocol();
        Backend backend = getBackend(host);
        Socket control = createControlSocket(backend);
        
        protocol.sendVersion(control);
        protocol.sendMessage(control,protocol.controlRequest(getTunnelName(), getClientName()));

        String response = protocol.receiveMessage(control);
        
        pingPongService = new ControlPingPongService(control);
        pingPongService.start();

        Gson gson = new Gson();
        ControlTunnelResponse ctr = gson.fromJson(response,ControlTunnelResponse.class);
        
        proxyService = new ProxyService(backend,port,tunnelName,clientName,ctr.getConcurrency().intValue());
        proxyService.start();

        remoteHost = "http://" + ctr.getHost().split("\\.")[0] + "." + host;
        
        System.out.println("started the local tunnel");
        System.out.println("you can now access: " + remoteHost);
    }

    private Backend getBackend(String host)
    {
        String[] bParts = host.split(":");
        String bHost = bParts[0];
        int bPort = 0;

        if(bParts.length > 1)
        {
            bPort = Integer.parseInt(bParts[1]);
        }
        
        int backendPort = discoverBackendPort(bHost,bPort);
        
        return new Backend(bHost,backendPort);
    }

    @Override
    public void stop()
    {
        proxyService.stop();
        pingPongService.stop();
    }

    @Override
    public String getRemoteHost()
    {
        return remoteHost;
    }

    @Override
    public Socket createControlSocket(final Backend backend) throws IOException
    {
        Backend be = backend;
        
        if(null == backend)
        {
            be = getBackend(host);
        }
        return new Socket(be.getHost(),be.getPort());
    }

    public Socket createControlSocket(final Backend backend, Proxy proxy) throws IOException
    {
        Backend be = backend;

        if(null == backend)
        {
            be = getBackend(host);
        }
        
        Socket socket = new Socket(proxy);
        InetSocketAddress addr = new InetSocketAddress(be.getHost(),be.getPort());
        socket.connect(addr);
        return socket;
    }

    public int getPort()
    {
        return port;
    }

    public String getTunnelName()
    {
        return tunnelName;
    }

    public String getClientName()
    {
        return clientName;
    }

    public String getHost()
    {
        return host;
    }

    private int discoverBackendPort(String bHost, int bPort)
    {
        int port = 0;
        
        StringBuilder sb = new StringBuilder("http://");
        sb.append(bHost);

        if(bPort > 0)
        {
            sb.append(":").append(bPort);
        }

        HttpClient httpclient = new DefaultHttpClient();
        try
        {
            HttpGet httpGet = new HttpGet(sb.toString());
            httpGet.addHeader("Accept-Encoding","identity");
            httpGet.addHeader("Host","_backend." + bHost);
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            String response = httpclient.execute(httpGet,responseHandler);
            port = Integer.parseInt(response);
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
            port = 0;
        }
        catch (NumberFormatException nfe)
        {
            port = 0;
        }
        finally
        {
            httpclient.getConnectionManager().shutdown();
        }

        return port;
    }
}
