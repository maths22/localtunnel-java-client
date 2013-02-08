package com.atlassian.localtunnel;

import java.io.*;
import java.net.Socket;
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
        OutputStream os = socket.getOutputStream();
        DataOutputStream dos = new DataOutputStream(os);

        dos.write(header,0,4);
        dos.write(message.getBytes());
        dos.flush();
    }
    
    public String receiveMessage(Socket socket) throws IOException
    {
        System.out.println("recieving from: " + socket.getInetAddress().getHostName());
        String message = null;        
        BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
        DataInputStream dis = new DataInputStream(bis);

        final byte[] header = new byte[4];
        dis.read(header,0,4);

        int msgLen = ByteBuffer.wrap(header).order(ByteOrder.BIG_ENDIAN).getInt();
        
        msgLen = (msgLen > 0) ? msgLen : 10;
        
        if(msgLen > 0)
        {
            byte[] mBytes = new byte[msgLen];
            dis.readFully(mBytes);
            message = new String(mBytes,"UTF-8");
        }
        else
        {
            StringBuilder sb = new StringBuilder();
            int c;
            while ((c = dis.read()) > 0)
            {
                sb.append((char) c);
            }
            message = sb.toString();
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
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        dos.writeBytes(VERSION);
        dos.flush();
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
