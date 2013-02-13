package com.atlassian.localtunnel;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;

/**
 * @since version
 */
public class ControlPingPongService
{
    private final ExecutorService exec = Executors.newFixedThreadPool(1);
    private final Socket control;

    public ControlPingPongService(Socket control)
    {
        this.control = control;
    }

    public void start()
    {
        final LocalTunnelProtocol protocol = new LocalTunnelProtocol();

            exec.execute(new Runnable() {
                @Override
                public void run()
                {
                    try
                    {
                        while(!control.isClosed() && !exec.isShutdown())
                        {
                            String message = protocol.receiveMessage(control);

                            Gson gson = new Gson();
                            Map<String,String> result = gson.fromJson(message,Map.class);
                            
                            if(null != result && null != result.get("control") && result.get("control").equals("ping"))
                            {
                                protocol.sendMessage(control,protocol.pong());
                            }
                        }
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            });
    }
    
    public void stop()
    {
        exec.shutdownNow();
        try
        {
            exec.awaitTermination(30, TimeUnit.SECONDS);
        }
        catch (InterruptedException e)
        {
           
        }
    }
}
