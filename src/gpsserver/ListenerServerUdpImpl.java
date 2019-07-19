/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gpsserver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 *
 * @author Oscar
 */
public class ListenerServerUdpImpl extends ListenerServer {
    protected DatagramSocket _socket = null;
    
    @Override
    public void listen() throws IOException, SocketException
    {
        byte[] buffer = new byte[4096];
        
        this._sBuffer = null;
        this._inetAddress = null;

        if( this._socket==null ) {
            this._socket = new DatagramSocket(this._port);
        }

        DatagramPacket dPacket = new DatagramPacket(buffer, buffer.length);
        
        this._socket.receive(dPacket);
        
        this._sBuffer = new String(dPacket.getData(), 0, dPacket.getLength());
        
        this._inetAddress = dPacket.getAddress();
        
        this._remotePort = dPacket.getPort();
        
        this._sBuffer = new String(dPacket.getData(), 0, dPacket.getLength());
        
        this._inetAddress = dPacket.getAddress();
        
    }
    
    @Override
    public int sendCommand(String cmd) throws SocketException, IOException
    {
        MainServer.logger.info("Procesando comando {}" , cmd);
     
        byte[] buffer = cmd.getBytes();
        
        DatagramPacket dPacket = new DatagramPacket(buffer, buffer.length, this._inetAddress, this._remotePort);

        MainServer.logger.info("Enviando comando al equipo...");
        
        this._socket.send(dPacket);
        
        MainServer.logger.info("Comando enviado.");
        
        return 1;
    }
}
