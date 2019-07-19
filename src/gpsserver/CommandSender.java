/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gpsserver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author Oscar
 */
public class CommandSender extends DBThread  {

    protected ListenerServer _listener;
    
    public void setListener(ListenerServer listener)
    {
        this._listener=listener;
    }
    
    public CommandSender(ListenerServer listener)
    {
        this._listener=listener;
    }
    
    public void execute() throws SQLException
    {
        int status=2;
        String imei=Message.findIMEI(_listener.getBuffer());
        
        MainServer.logger.error( "Chequeando comandos para imei: {}", imei);
        
        openDB();
        
        if( imei.length() <= 0) return;
        
        String sql = "SELECT c.cmd_id, c.name, c.type, c.cmd, c.status, " +
                                "o.net_protocol, o.ip, o.port " +
                                "FROM gs_object_cmd_exec c INNER JOIN gs_objects o " +
                                "ON c.imei=o.imei WHERE c.imei=? AND c.status=0;";

        PreparedStatement ps = this._con.prepareStatement(sql);
        
        ps.setString(1, imei);

        ResultSet rs = ps.executeQuery();

        while( rs.next() ) {
                    
            String net_protocol = rs.getString(6);
            String cmd = rs.getString(4);
            /*String server=rs.getString(7).replace("/", "");
            int puerto = rs.getInt(8);*/
            int id = rs.getInt(1);

            try {
                switch( net_protocol ){
                    case "udp":
                        status = sendCmdUdp(cmd);
                        break;
                    default:
                        MainServer.logger.error( "net protocol no soportado.");
                        break;
                }

                updateStatus(id, status, "");

            } catch( Exception e) {
                MainServer.logger.error( "Error procesando comando: " + e.getClass().getCanonicalName() + ": " + e.getMessage());
            }
        }
    }
    
    /*
    
    @Override
    public void run() {
        
        try {
            int status = 0;

            MainServer.logger.info("Creando thread para gestionar comandos a equipos...");

            openDB();
            
            while( true ) {
                String sql = "SELECT c.cmd_id, c.name, c.type, c.cmd, c.status, " +
                                "o.net_protocol, o.ip, o.port " +
                                "FROM gs_object_cmd_exec c INNER JOIN gs_objects o " +
                                "ON c.imei=o.imei WHERE c.status=0;";

                PreparedStatement ps = this._con.prepareStatement(sql);

                ResultSet rs = ps.executeQuery();

                while( rs.next() ) {
                    
                    String net_protocol = rs.getString(6);
                    String cmd = rs.getString(4);
                    String server=rs.getString(7).replace("/", "");
                    int puerto = rs.getInt(8);
                    int id = rs.getInt(1);
                    
                    try {
                        switch( net_protocol ){
                            case "udp":
                                status = sendCmdUdp( puerto, server, cmd);
                                break;
                            default:
                                MainServer.logger.error( "net protocol no soportado.");
                                break;
                        }
                    
                        updateStatus(id, status, "");
                    
                    } catch( Exception e) {
                        MainServer.logger.error( "Error procesando comando: " + e.getClass().getCanonicalName() + ": " + e.getMessage());
                    }
                }
                
                Thread.sleep(5000);
            }
            
        } catch( Exception e) {
            MainServer.logger.error( "Error " + e.getClass().getCanonicalName() + ": " + e.getMessage());
        } finally {
            
            MainServer.logger.info("Thread finalizado.");
            
            try {
                if( this._con != null) this._con.close();
            } catch(Exception e) {
                
            }
        }
    }
    */
    
    protected int sendCmdUdp(String cmd) throws SocketException, IOException
    {
        return this._listener.sendCommand(cmd);
    }
    
    protected int sendCmdUdp(int port, String server, String cmd) throws SocketException, IOException
    {
        int ret=1;
        byte[] buffer = new byte[1000];
        byte[] bComando = cmd.getBytes();
        byte[] bCmd = new byte[bComando.length+1];
        
        System.arraycopy(bComando, 0, bCmd, 0, bComando.length);
        
        bCmd[bComando.length] = 0x0A;   // caracter de finalizado
        
        InetAddress address = InetAddress.getByName(server);
        DatagramPacket dPacketRecv = new DatagramPacket(buffer, buffer.length);
        MainServer.logger.info("Enviando comando {} a server {} puerto {}" , cmd, server, port);
        
        DatagramPacket dPacket = new DatagramPacket( bCmd, bCmd.length, address, port );

        try (DatagramSocket dSocket = new DatagramSocket()) {

            try {
                
                dSocket.setSoTimeout(5000);

                dSocket.send(dPacket);

                dSocket.receive(dPacketRecv);

                MainServer.logger.info("Recibio respuesta : [%s]", buffer.toString());
                
            } catch(SocketTimeoutException s ) {
                MainServer.logger.info("Sale por timeout. Setea status 2");
                ret=2;
            }
        }
        
        MainServer.logger.info("Devuelve status {}", ret);
        
        return ret;
    }
    
    protected void updateStatus(int id, int status, String re_hex) throws SQLException {
        String sql = "UPDATE gs_object_cmd_exec SET status=?, re_hex=? WHERE cmd_id=" + id;
        
        MainServer.logger.info("Grabando status {} para el cmd_id={}", status, id);

        PreparedStatement ps =  this._con.prepareStatement(sql);
        
        ps.setInt(1, status);
        ps.setString(2, re_hex);
        
        ps.executeUpdate();
    }
}
