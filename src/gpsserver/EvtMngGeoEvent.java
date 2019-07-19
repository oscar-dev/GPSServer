/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gpsserver;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author omedi
 */
public abstract class EvtMngGeoEvent extends EventManager {
    
    protected boolean rutaValida(int event_status) throws SQLException {
        
        boolean res = false;
        
        Statement stmt = this._con.createStatement();
        
        ResultSet rs = stmt.executeQuery("SELECT COUNT(1) FROM gs_user_routes WHERE route_id=" + Integer.toString(event_status));
        
        if (rs.next() ) res = rs.getInt(1) > 0;
        
        return res;
    }
    
    protected double dentroDeRuta(String puntos, double lat, double lng) throws SQLException {
        
        boolean bFin = false;
        double ret;
        String sql = "SELECT st_Distance(GeomFromText('Point(";
        sql += Double.toString(lat) + " " + Double.toString(lng);
        sql += ")'), GeomFromText('LineString(";
        
        String params[] = puntos.split(",");
        
        for(String param : params) {
            sql += param + (bFin ? "," : " ");
            
            bFin = !bFin;
        }
        
        if( sql.charAt(sql.length()-1) == ',' ) {
            sql = sql.substring(0, sql.length()-1);
        }
        
        sql += ")') ) * 111195 ";
        
        //MainServer.logger.info("SQL: [" +  sql + "]");
        
        PreparedStatement ps = this._con.prepareStatement(sql);
        
        try (ResultSet rs = ps.executeQuery()) {
            rs.next();
            
            ret = rs.getDouble(1) / 1000;
        }
        
        return ret;
    }
    
    protected int dentroDeZona(String vertices, double lat, double lng) throws SQLException {
        int ret = 0;
        boolean bFin = false;
        String polygon = "SELECT MBRCovers(GeomFromText('POLYGON((";
        String params[] = vertices.split(",");
        
        for(String param : params) {
            polygon += param + (bFin ? "," : " ");
            
            bFin = !bFin;
        }
        
        polygon = polygon.substring(0, polygon.length()-1);
        
        polygon += "))'), GeomFromText('POINT(";
        polygon += Double.toString(lat) + " " + Double.toString(lng);
        polygon += ")'))";
        
        PreparedStatement ps = this._con.prepareStatement(polygon);
        
        ResultSet rs = ps.executeQuery();
        
        rs.next();
        
        ret = rs.getInt(1);
        
        rs.close();
        
        return ret;
    }
}
