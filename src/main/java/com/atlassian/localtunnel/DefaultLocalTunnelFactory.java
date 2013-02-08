package com.atlassian.localtunnel;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

/**
 * @since version
 */
public class DefaultLocalTunnelFactory implements LocalTunnelFactory
{

    @Override
    public LocalTunnel create(int port)
    {
        return create(port, "v2.localtunnel.com");
    }

    @Override
    public LocalTunnel create(int port, String host)
    {
        String tunnelName = UUID.randomUUID().toString().split("-")[0];
        //String tunnelName = "myjunk";
        String clientName = getClientName();

        return new DefaultLocalTunnel(port, tunnelName, clientName, host);
    }

    private String getClientName()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(System.getProperty("user.name"))
          .append("@")
          .append(getLocalHostname())
          .append(";")
          .append(System.getProperty("os.name"));

        return sb.toString();
    }

    private String getLocalHostname()
    {
        String localHostName = null;
        try
        {
            localHostName = InetAddress.getLocalHost().getHostName();
        }
        catch (UnknownHostException e)
        {
            localHostName = "localhost";
        }

        return localHostName;
    }
}
