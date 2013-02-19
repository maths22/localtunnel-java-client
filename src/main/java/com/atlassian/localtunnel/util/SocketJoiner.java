package com.atlassian.localtunnel.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @since version
 */
public class SocketJoiner
{
    private final ExecutorService fromExec = Executors.newSingleThreadExecutor();
    private final ExecutorService toExec = Executors.newSingleThreadExecutor();
    private Socket from;
    private Socket to;

    public SocketJoiner(Socket from, Socket to)
    {
        this.from = from;
        this.to = to;
    }
    
    public void join()
    {
        fromExec.execute(new Runnable() {
            @Override
            public void run()
            {

                    try
                    {
                        BufferedInputStream in = new BufferedInputStream(from.getInputStream());
                        int c;
                        while((c = in.read()) > -1)
                        {
                            to.getOutputStream().write(c);
                        }
                    }
                    catch (IOException e)
                    {
                        try
                        {
                            from.close();
                            to.close();
                        }
                        catch (IOException e1)
                        {
                           
                        }
                    }


                try
                {
                    to.close();
                    from.close();
                }
                catch (IOException e)
                {
                    
                }
            }
        });

        toExec.execute(new Runnable() {
            @Override
            public void run()
            {
                    try
                    {
                        BufferedInputStream in = new BufferedInputStream(to.getInputStream());
                        int c;
                        while((c = in.read()) > -1)
                        {
                            from.getOutputStream().write(c);
                        }
                    }
                    catch (IOException e)
                    {
                        try
                        {
                            to.close();
                            from.close();
                        }
                        catch (IOException e1)
                        {

                        }
                    }
                try
                {
                    from.close();
                    to.close();
                }
                catch (IOException e)
                {
                    
                }
            }
        });
    }
    
    public void stop()
    {
        fromExec.shutdownNow();
        toExec.shutdownNow();
        try
        {
            fromExec.awaitTermination(30, TimeUnit.SECONDS);
            toExec.awaitTermination(30, TimeUnit.SECONDS);
        }
        catch (InterruptedException e)
        {

        }
    }
}
