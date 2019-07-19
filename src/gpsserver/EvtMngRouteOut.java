/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gpsserver;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

/**
 *
 * @author omedi
 */
public class EvtMngRouteOut extends EvtMngGeoEvent{
    
    @Override
    public void checkEvent(Message message) throws SQLException, AddressException, MessagingException {
        int route_id = 0;
        double deviation = 0.0;
        
        loadMessage(message);
        
        String sql = "SELECT route_points, route_id, route_name, route_deviation FROM gs_user_routes "
                + "WHERE user_id=" + this._user_id + " AND route_id in(" + this._routes + ")";
        
        int event_status = get_status();
        
        if( ! this.rutaValida(event_status) ) event_status = -1;
        
        PreparedStatement ps = this._con.prepareStatement(sql);
        
        ResultSet rs = ps.executeQuery();
        
        while(rs.next()) {
            double en_ruta = this.dentroDeRuta(rs.getString(1), this._lat, this._lng);
        
            route_id = rs.getInt(2);
            String route_name = rs.getString(3);
            
            MainServer.logger.info("Val: " + Double.toString(en_ruta) + " Evento: " + this._type + " en ruta: " + en_ruta + " desv: " + deviation);
            
            if( ! rs.getString(4).trim().equals("") )
                deviation = Double.parseDouble(rs.getString(4));
            
            if( en_ruta < deviation ) {
                if( event_status == -1 ) {
                    set_status(route_id);
                }
            } else {
                if( event_status == route_id ) {
                    set_status(-1);
                    
                    this._event_desc += " (" + route_name + ")";
                    
                    insertEvent();
                }
            }
        }
        
        rs.close();
        ps.close();
    }
}
