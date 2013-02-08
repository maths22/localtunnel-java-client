package com.atlassian.localtunnel;

/**
 * @since version
 */
public interface LocalTunnelFactory
{
    LocalTunnel create(int port);
    LocalTunnel create(int port, String host);
}
