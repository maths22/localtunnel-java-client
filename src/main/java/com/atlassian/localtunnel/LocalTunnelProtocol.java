package com.atlassian.localtunnel;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @since version
 */
public class LocalTunnelProtocol
{
    public static final String VERSION = "LTP/0.2";
    
    public void sendMessage(Socket socket, String message) throws IOException
    {
        final byte[] header = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(message.length()).array();
        
        OutputStream dos = socket.getOutputStream();
        
        dos.write(header);
        dos.write(message.getBytes());
        dos.flush();
    }
    
    public String receiveMessage(Socket socket) throws IOException
    {
        String message = null;        
        BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
        DataInputStream dis = new DataInputStream(bis);

        int msgLen = dis.readInt();
        
        if(msgLen > 0)
        {
            byte[] mBytes = new byte[msgLen];
            dis.readFully(mBytes);
            message = new String(mBytes,"UTF-8");
        }
        
        return message;
    }
    
    public String controlRequest(String name, String client)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"control\":{")
                .append("\"name\":\"")
                .append(name)
                .append("\",\"client\":\"")
                .append(client)
                .append("\"}}");
        
        return sb.toString();
    }

    public String ping()
    {
        return "{\"control\": \"ping\"}";
    }

    public String pong()
    {
        return "{\"control\": \"pong\"}";
    }
    
    public void sendVersion(Socket socket) throws IOException
    {
        OutputStream os = socket.getOutputStream();
        os.write(VERSION.getBytes());
        os.flush();
    }

    public String proxyRequest(String tunnelName, String clientName)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"proxy\":{")
          .append("\"name\":\"")
          .append(tunnelName)
          .append("\",\"client\":\"")
          .append(clientName)
          .append("\"}}");

        return sb.toString();
    }
}
