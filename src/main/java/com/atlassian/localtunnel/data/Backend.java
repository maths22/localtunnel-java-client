package com.atlassian.localtunnel.data;

/**
 * @since version
 */
public class Backend
{
    private final String host;
    private final int port;

    public Backend(String host, int port)
    {
        this.host = host;
        this.port = port;
    }

    public String getHost()
    {
        return host;
    }

    public int getPort()
    {
        return port;
    }
}
