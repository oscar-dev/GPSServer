/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gpsserver;

import java.sql.*;
import java.util.Properties;

/**
 *
 * @author Oscar
 */
public abstract class DBThread extends Thread {
    protected Properties _properties;
    protected Connection _con;
    
    public void setProperties(Properties properties) { this._properties = properties; }
    
    protected void openDB()
    {
        try {
        
            MainServer.logger.info("Conectado a la base para grabar mensaje...");
            
            String server = _properties.getProperty("db.server");
            String dbname = _properties.getProperty("db.name");
            String user = _properties.getProperty("db.user");
            String password = _properties.getProperty("db.password");

            String strconn= "jdbc:mysql://" + server + "/" + dbname + "?useSSL=false";

            Class.forName("com.mysql.jdbc.Driver");

            this._con = DriverManager.getConnection(strconn, user, password);
        
        } catch(SQLException | ClassNotFoundException se) {
            
            MainServer.logger.error( "Error " + se.getClass().getCanonicalName() + ": " + se.getMessage());
             
        }   
    }
    
}
