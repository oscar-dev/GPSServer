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
public class EvtMngZoneOut  extends EvtMngGeoEvent {
    
    @Override
    public void checkEvent(Message message) throws SQLException, AddressException, MessagingException {
        int zone_id = 0;
        String sql = "SELECT zone_vertices, zone_id, zone_name FROM gs_user_zones "
                + "WHERE user_id=" + this._user_id + " AND zone_id in(" + this._zones + ")";
        
        loadMessage(message);
        
        int event_status = get_status();
        
        PreparedStatement ps = this._con.prepareStatement(sql);
        
        ResultSet rs = ps.executeQuery();
        
        while(rs.next()) {
            int en_zona = this.dentroDeZona(rs.getString(1), this._lat, this._lng);
        
            zone_id = rs.getInt(2);
            String zone_name = rs.getString(3);
            
            //MainServer.logger.info("Val: " + Integer.toString(en_zona) + " Evento: " + this._type + " en zona: " + en_zona);
            
            if( en_zona == 1 ) {
                if( event_status == -1 ) {
                    set_status(zone_id);
                }
            } else {
                if( event_status == zone_id ) {
                    set_status(-1);
                    
                    this._event_desc += " (" + zone_name + ")";
                    
                    this.insertEvent();
                }
            }
            
        }
        
        rs.close();
        ps.close();
    }
}
