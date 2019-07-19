/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gpsserver;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;

/**
 *
 * @author Oscar
 */
abstract public class ListenerServer {
    protected int _port;
    protected int _remotePort;
    
    protected String _sBuffer = null;
    protected InetAddress _inetAddress = null;
    
    public void setPort(int port) { this._port = port; }
    
    public int getRemotePort() { return this._remotePort; }
    public String getBuffer() { return this._sBuffer; }
    public InetAddress getInetAddress() { return this._inetAddress; }

    abstract public void listen() throws IOException, SocketException;
    abstract public int sendCommand(String cmd) throws SocketException, IOException;
    
}
