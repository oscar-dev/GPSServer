/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gpsserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 *
 * @author Oscar
 */
public class ListenerServerTcpImpl extends ListenerServer {
    ServerSocket _serverSocket = null;
    
    @Override
    public void listen() throws IOException, SocketException
    {
        this._sBuffer = null;
        this._inetAddress = null;

        if( this._serverSocket==null ) {
            this._serverSocket = new ServerSocket(this._port);
        }
        
        try (Socket socket = this._serverSocket.accept()) {
            BufferedReader buffClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this._inetAddress = socket.getInetAddress();
            this._remotePort = socket.getPort();
            this._sBuffer = buffClient.readLine();
        }
    }
    
    @Override
    public int sendCommand(String cmd) throws SocketException, IOException
    {
        return 1;
    }
}
