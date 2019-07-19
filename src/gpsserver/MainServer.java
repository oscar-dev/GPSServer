/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gpsserver;

import java.util.Properties;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.SocketException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
/**
 *
 * @author Oscar
 */
public class MainServer {
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //System.setProperty("log4j.configurationFactory", "./log4j.properties");
        
        MainServer mainServer = new MainServer();
    
        mainServer.exec();
    }
    
    protected Properties _properties = null;
    protected String _protocol = "";
    
    static public Logger logger = LogManager.getLogger(MainServer.class);
    
    public void exec()
    {
        try {
            
            MainServer.logger.info("Comenzando ejecucion...");
            
            loadConfig();
            
            ListenerServer listenerServer = getListenerServer();
        
            while(true) {
                listenerServer.listen();
                
                CommandSender cmdSender = new CommandSender(listenerServer);
                
                cmdSender.setProperties(_properties);
                cmdSender.execute();
                
                MainServer.logger.info("Mensaje recibido de " + listenerServer.getInetAddress().toString() + ">> [" + listenerServer.getBuffer() + "]");
                
                ParserThread parserThread = new ParserThread();
                
                parserThread.setProperties(_properties);
                parserThread.setBuffer(listenerServer.getBuffer());
                parserThread.setRemotePort(listenerServer.getRemotePort());
                parserThread.setProtocol(this._protocol);
                parserThread.setInetAddress(listenerServer.getInetAddress());
                
                parserThread.start();
            }
            
        } catch(SocketException se ) {
            
            MainServer.logger.error( "Error " + se.getClass().getCanonicalName() + ": " + se.getMessage());
            
        } catch(IOException ie) {
            
            MainServer.logger.error( "Error " + ie.getClass().getCanonicalName() + ": " + ie.getMessage());
           
        } catch(Exception e) {
            
            MainServer.logger.error( "Error " + e.getClass().getCanonicalName() + ": " + e.getMessage());
            
        }
    }
    
    protected ListenerServer getListenerServer()
    {
        ListenerServer listenerServer = null;
        int port = Integer.parseInt(_properties.getProperty("server.port"));

        this._protocol = _properties.getProperty("server.protocol");
        
        MainServer.logger.info("Listener en Puerto: " + port + " Protocolo: " + this._protocol);
        
        if( this._protocol.equalsIgnoreCase("udp")) {
            listenerServer = new ListenerServerUdpImpl();
            listenerServer.setPort(port);
        } else if( this._protocol.equalsIgnoreCase("tcp")) {
            listenerServer = new ListenerServerTcpImpl();
            listenerServer.setPort(port);
        }
        
        return listenerServer;
    }
    
    protected void loadConfig()
    {
        try {
            
            MainServer.logger.info("Leyendo configuracion...");
            
            this._properties = new Properties();

            FileInputStream inputStream = new FileInputStream("gpsserver.properties");

            this._properties.loadFromXML(inputStream);
        
        } catch(FileNotFoundException fne) {
            
            MainServer.logger.error( "Error " + fne.getClass().getCanonicalName() + ": " + fne.getMessage());
            
        } catch(IOException ioe) {
            
            MainServer.logger.error( "Error " + ioe.getClass().getCanonicalName() + ": " + ioe.getMessage());
            
        } catch(Exception e) {
            
            MainServer.logger.error( "Error " + e.getClass().getCanonicalName() + ": " + e.getMessage());
        }
    }
}
