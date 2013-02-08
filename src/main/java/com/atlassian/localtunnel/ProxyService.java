package com.atlassian.localtunnel;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.atlassian.localtunnel.data.Backend;
import com.atlassian.localtunnel.util.SocketJoiner;

import com.google.gson.Gson;

/**
 * @since version
 */
public class ProxyService
{
    private final ExecutorService exec;
    private final Backend backend;
    private final int port;
    private final String tunnelName;
    private final String clientName;
    private final int concurrency;

    public ProxyService(Backend backend, int port, String tunnelName, String clientName, int concurrency)
    {
        this.exec = Executors.newFixedThreadPool(concurrency);
        this.backend = backend;
        this.port = port;
        this.tunnelName = tunnelName;
        this.clientName = clientName;
        this.concurrency = concurrency;
    }

    public void start()
    {
        final LocalTunnelProtocol protocol = new LocalTunnelProtocol();
        
        for(int i=0;i<concurrency;i++)
        {
            exec.execute(new Runnable() {
                @Override
                public void run()
                {
                    System.out.println("running proxy...");
                    SocketJoiner joiner = null;
                    try
                    {
                        while(!exec.isShutdown())
                        {
                            Socket proxy = new Socket(backend.getHost(),backend.getPort());
                            protocol.sendVersion(proxy);
                            
                            protocol.sendMessage(proxy,protocol.proxyRequest(tunnelName,clientName));
                            String message = protocol.receiveMessage(proxy);
                            
                            if(null != message && !"".equals(message))
                            {
                                Gson gson = new Gson();
                                Map result = gson.fromJson(message,Map.class);

                                if(null != result && null != result.get("proxy") && (Boolean)result.get("proxy"))
                                {
                                    Socket local = new Socket("0.0.0.0",port);
                                    joiner = new SocketJoiner(proxy,local);
                                    joiner.join();
                                }
                            }
                        }
                        if(null != joiner)
                        {
                            joiner.stop();
                        }
                    }
                    catch (IOException e)
                    {
                        if(null != joiner)
                        {
                            joiner.stop();
                        }
                    }
                }
            });
        }
    }

    public void stop()
    {
        exec.shutdownNow();
        try
        {
            exec.awaitTermination(5, TimeUnit.SECONDS);
        }
        catch (InterruptedException e)
        {

        }
    }
    
}
