package com.atlassian.localtunnel;

import java.io.IOException;
import java.net.Socket;

import com.atlassian.localtunnel.data.Backend;

/**
 * @since version
 */
public interface LocalTunnel
{

    void start() throws IOException;

    void stop();
    
    String getRemoteHost();

    Socket createControlSocket(Backend backend) throws IOException;

    String getTunnelName();

    String getClientName();
    
    boolean isStarted();
}
