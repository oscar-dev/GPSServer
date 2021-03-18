/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gpsserver;

import java.net.InetAddress;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Oscar
 */
public class ParserThread extends DBThread {
    
    protected List<EventManager> _listEvents = null;
    protected String _buffer;
    protected InetAddress _inetAddress;
    protected int _remotePort;
    protected String _protocol;
    
    public void setBuffer(String buffer) { this._buffer = buffer; }
    public void setInetAddress(InetAddress inetAddress) { this._inetAddress = inetAddress; }
    public void setRemotePort(int remotePort) { this._remotePort =  remotePort; }
    public void setProtocol(String protocol) { this._protocol = protocol; }

    @Override
    public void run() {
        try {
            
            MainServer.logger.info("Creando thread para procesar mensaje...");
            
            this.openDB();
            
            String listaMessages[] = Message.splitMessages(this._buffer);
            
            for( String item : listaMessages ) {
                Message message = new Message();
                
                message.parser(item);
                
                message.setDateServer(this.getNOW());

                MainServer.logger.info("Mensaje recibido ID: " + message.getDeviceID() + " Fecha de mensaje: " + message.getDateEvent().toString());

                saveMessage(message);
                
                ArrayList<EventManager> listEvents = getEvents(message.getDeviceID());

                listEvents.forEach((em) -> {
                    try {
                        em.checkEvent(message);
                    } catch(Exception exp) {
                        MainServer.logger.error( "Error " + exp.getClass().getCanonicalName() + ": " + exp.getMessage() + "-" + em.getClass().toString());
                    }
                } );
            }
        
            MainServer.logger.info("Thread finalizado.");
            
            if( this._con != null) this._con.close();
            
        } catch( InterruptedException ie ) {
            
            MainServer.logger.error( "Error " + ie.getClass().getCanonicalName() + ": " + ie.getMessage());

        } catch(Exception e) {
            
            MainServer.logger.error( "Error " + e.getClass().getCanonicalName() + ": " + e.getMessage());
            
        }
    }
    
    protected ArrayList<EventManager> getEvents(String deviceID) throws SQLException {
        ArrayList<EventManager> lista = new ArrayList<>();
        
        String sql = "SELECT e.event_id, e.user_id, e.type, e.name, e.active, " +
                        "e.duration_from_last_event, " +
                        "e.duration_from_last_event_minutes, e.week_days, e.day_time, e.imei, " +
                        "e.checked_value, e.route_trigger, e.zone_trigger, e.routes, e.zones, " +
                        "e.notify_system, e.notify_email, e.notify_email_address, e.notify_sms, " +
                        "e.notify_sms_number,e.notify_arrow, e.notify_arrow_color, e.notify_ohc, " +
                        "e.notify_ohc_color, e.email_template_id, u.language FROM " +
                        "gs_user_events e INNER JOIN gs_users u " +
                        "ON e.user_id=u.id WHERE " +
                        "e.imei like ? and e.active='true'";
        
        PreparedStatement ps = this._con.prepareStatement(sql);
            
        ps.setString(1, "%" + deviceID + "%");
        
        ResultSet rs = ps.executeQuery();
        
        while ( rs.next() ) {
            EventManager item;
            
            String type = rs.getString("type");
            
            MainServer.logger.info("Evento tipo: " + type + " encontrado para id: " + deviceID);
            
            if( type.equalsIgnoreCase("overspeed")) {
                item = new EvtMngOverspeed();
            } else if( type.equalsIgnoreCase("zone_in")) {
                item = new EvtMngZoneIn();
            } else if( type.equalsIgnoreCase("zone_out")) {
                item = new EvtMngZoneOut();
            } else if( type.equalsIgnoreCase("underspeed")) {
                item = new EvtMngUnderspeed();
            } else if( type.equalsIgnoreCase("route_in")) {
                item = new EvtMngRouteIn();
            } else if( type.equalsIgnoreCase("route_out")) {
                item = new EvtMngRouteOut();
            } else if( type.equalsIgnoreCase("param")) {
                item = new EvtMngParam();
            } else if( type.equalsIgnoreCase("sensor")) {
                item = new EvtMngSensor();
            } else {
                MainServer.logger.warn("Tipo de evento no encontrado [" + type + "]");
                continue;
            }
            
            item.setProperties(this._properties);
            item.loadEvent(rs);
            
            item.setConnection(this._con);
            
            lista.add(item);
        }
        
        return lista;
    }
    
    protected void saveMessage(Message message) throws SQLException  {
        long km=0;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
   
            if( message._messageFields._campos.containsKey("mgr")) {
                String mgr = message._messageFields._campos.get("mgr");
                km = Long.parseLong(mgr);
                km /= 1000;
            }
            
            String sql = "INSERT INTO gs_object_data_" + message.getDeviceID() +
                    "(dt_server, dt_tracker, lat, lng, altitude, angle, "
                    + "speed, params) VALUES( ?, ?, ?, ?, ?, ?, ?, ?)";

            ps = this._con.prepareStatement(sql);
            java.sql.Timestamp dServer = new java.sql.Timestamp(message.getDateServer().getTime());
            java.sql.Timestamp dTracker = new java.sql.Timestamp(message.getDateEvent().getTime());
            
            ps.setTimestamp(1, dServer);
            ps.setTimestamp(2, dTracker);
            ps.setDouble(3, message.getLatitude());
            ps.setDouble(4, message.getLongitude());
            ps.setDouble(5, message.getAltitude());
            ps.setDouble(6, message.getAngle());
            ps.setDouble(7, message.getSpeed());
            ps.setString(8, message._messageFields.toString());
            
            ps.executeUpdate();
            
            ps.close();
            
            sql = "SELECT 1 FROM gs_objects WHERE imei = ?";

            ps = this._con.prepareStatement(sql);
            
            ps.setString(1, message.getDeviceID());
            
            rs = ps.executeQuery();
            
            if( rs.next() ) {
                
                MainServer.logger.info("Actualizando objeto id=" + message.getDeviceID());
            
                sql = "UPDATE gs_objects SET net_protocol=?, ip=?, port=?, dt_server=?, "
                        + "dt_tracker=?, lat=?, lng=?, altitude=?, angle=?, "
                        + "odometer=if(odometer_type='gps', odometer + ?, odometer), "
                        + "speed=?, loc_valid='1', ";

/*                sql = "UPDATE gs_objects SET net_protocol=?, ip=?, dt_server=?, "
                        + "dt_tracker=?, lat=?, lng=?, altitude=?, angle=?, speed=?, loc_valid='1', ";*/

                if( message._messageFields.obdContaints() || message._messageFields.canContaints()) {
                    
                        sql += "params=?,";
                }
                
                if( message.getSpeed() > 0 ) {
                    sql += "dt_last_move=?, ";
                } else {
                    sql += "dt_last_stop=?, ";
                }
                
                sql += " protocol=? WHERE imei=?";

                ps =  this._con.prepareStatement(sql);
                int idx=1;
                
                ps.setString( idx++, this._protocol);
                ps.setString( idx++, this._inetAddress.toString().replace("/", ""));
                ps.setInt(idx++, this._remotePort);
                
                
                ps.setTimestamp(idx++, dServer);
                ps.setTimestamp(idx++, dTracker);
                ps.setDouble(idx++, message.getLatitude());
                ps.setDouble(idx++, message.getLongitude());
                ps.setDouble(idx++, message.getAltitude());
                ps.setDouble(idx++, message.getAngle());
                ps.setLong(idx++, km);
                ps.setDouble(idx++, message.getSpeed());
                
                if( message._messageFields.obdContaints() || message._messageFields.canContaints()) {
                    ps.setString(idx++, message._messageFields.toString());
                }
                
                ps.setTimestamp(idx++, dTracker);
                ps.setString( idx++, this._protocol);
                ps.setString( idx++, message.getDeviceID());
            
                ps.executeUpdate();
                
            } else {
                
                MainServer.logger.info("Insertando objeto id=" + message.getDeviceID());
                MainServer.logger.info("Protocolo=[" + this._protocol + "]" );
                
                sql = "INSERT INTO gs_objects(imei, net_protocol, ip, port, dt_server, dt_tracker,"
                        + "lat, lng, altitude, angle, speed, loc_valid, params, protocol, active, object_expire, "
                        + "object_expire_dt, manager_id, dt_last_stop, dt_last_idle, dt_last_move, name,"
                        + "icon, map_arrows, map_icon, tail_color, tail_points, device,sim_number,model,vin,"
                        + "plate_number,odometer_type,engine_hours_type,fcr,time_adj, accuracy, dt_chat, odometer, engine_hours) "
                        + "VALUES ( ?, ?, ?, ?,"
                        + "NOW(), ?, ?, ?, ?, ?, ?, 1, ?, ?, "
                        + "'true','false',STR_TO_DATE('2099-12-12', '%Y-%m-%d'), 0, STR_TO_DATE('2099-12-12', '%Y-%m-%d'), STR_TO_DATE('2099-12-12', '%Y-%m-%d'), "
                        + "STR_TO_DATE('2099-12-12', '%Y-%m-%d'), 'prueba 01', 'img/markers/objects/land-truck.svg'," 
                        + "'{\"arrow_no_connection\":\"arrow_red\",\"arrow_stopped\":\"arrow_red\",\"arrow_moving\":\"arrow_green\","
                        + "\"arrow_engine_idle\":\"off\"}', 'arrow', '#00FF44', 7, ''," 
                        + "'', '', '', '', 'gps', 'off', '', '- 3 hour', '', STR_TO_DATE('2099-12-12', '%Y-%m-%d'), 0.0, 0)";

                ps = this._con.prepareStatement(sql);
            
                ps.setString( 1, message.getDeviceID());
                ps.setString( 2, this._protocol);
                ps.setString( 3, this._inetAddress.toString());
                ps.setInt(4, this._remotePort);
                ps.setTimestamp(5, dTracker);
                ps.setDouble(6, message.getLatitude());
                ps.setDouble(7, message.getLongitude());
                ps.setDouble(8, message.getAltitude());
                ps.setDouble(9, message.getAngle());
                ps.setDouble(10, message.getSpeed());
                ps.setString(11, message._messageFields.toString());
                ps.setString( 12, this._protocol);
                
                ps.executeUpdate();
                
            }
            
            saveMessageDTCData(message);
            
            saveEvent(message);
            
        }
        catch(SQLException se) {
            
            MainServer.logger.error( "Error " + se.getClass().getCanonicalName() + ": " + se.getMessage());
             
        }
        finally {
            if( rs != null ) rs.close();
            
            if( ps != null) ps.close();
        }
    }
    
    protected void saveMessageDTCData(Message message) throws SQLException {
        String fieldDTC = message._messageFields._campos.get("dtc");
        
        if( fieldDTC.length() <= 0 ) return;
        
        String[] codes = fieldDTC.split("/");
        
        for( int idx=0; idx < codes.length; idx++ ) {
            MainServer.logger.info("Insertando DTCData id=" + message.getDeviceID());

            String sql = "INSERT INTO gs_dtc_data(dt_server, dt_tracker, imei, code," +
                                        "lat, lng, address) VALUES( ?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement ps = this._con.prepareStatement(sql);
            
            java.sql.Timestamp dTracker = new java.sql.Timestamp(message.getDateEvent().getTime());
            java.sql.Timestamp dServer = new java.sql.Timestamp(message.getDateServer().getTime());

            ps.setTimestamp( 1, dServer);
            ps.setTimestamp(2, dTracker);
            ps.setString( 3, message.getDeviceID());
            ps.setString( 4, codes[idx]);
            ps.setDouble( 5, message.getLatitude());
            ps.setDouble( 6, message.getLongitude());
            ps.setString( 7, this._inetAddress.toString());

            ps.executeUpdate();
        }
    }
    
    protected Date getNOW() throws SQLException
    {
        java.sql.Timestamp value = null;
        String sql = "SELECT NOW()";
        
        PreparedStatement ps = this._con.prepareStatement(sql);
        
        ResultSet rs = ps.executeQuery();
        
        if( rs.next() ) {
            value = rs.getTimestamp(1);
        }
        
        return new Date(value.getTime());
    }
    
    protected void saveEvent(Message message) throws SQLException {
        
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        MainServer.logger.info("Evento HDB=[" + message._messageFields._campos.get("hdb") + "].");
        
        if( message._messageFields._campos.get("hdb") != null ) {
            
            String event = "";
            MainServer.logger.info("Evento HDB=[" + message._messageFields._campos.get("hdb") + "].");
            
            switch( message._messageFields._campos.get("hdb") ) {
                case "1":
                    event = "haccel";
                    break;
                case "2":
                    event = "hbrake";
                    break;
                case "3":
                    event = "hcorn";
                    break;
                default:
                    return;
            }
            
            MainServer.logger.info("Evento [" + event + "] recibido.");
            
            String sql = "select user_id,type,name,notify_system, notify_arrow, " +
                    "notify_arrow_color, notify_ohc, notify_ohc_color from " +
                    "gs_user_events where imei like ? and active='true' and type=?";

            ps = this._con.prepareStatement(sql);
            
            ps.setString(1, "%" + message.getDeviceID() + "%");
            ps.setString(2, event);
            
            rs = ps.executeQuery();
            
            if( rs.next() ) {
                sql = "INSERT INTO gs_user_events_data( user_id, type, event_desc, " +
                        "notify_system, notify_arrow, notify_arrow_color, notify_ohc, " +
                        "notify_ohc_color, imei, name, dt_server, dt_tracker, lat, lng, " +
                        "altitude, angle, speed, params) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
                        "'', ?, ?, ?, ?, ?, ?, ?, ?)";
                
                ps = this._con.prepareStatement(sql);
            
                ps.setInt(1, rs.getInt("user_id"));
                ps.setString(2, rs.getString("type"));
                ps.setString(3, rs.getString("name"));
                ps.setString(4, rs.getString("notify_system"));
                ps.setString(5, rs.getString("notify_arrow"));
                ps.setString(6, rs.getString("notify_arrow_color"));
                ps.setString(7, rs.getString("notify_ohc"));
                ps.setString(8, rs.getString("notify_ohc_color"));
                ps.setString(9, message.getDeviceID());
                
                java.sql.Timestamp dServer = new java.sql.Timestamp(message.getDateServer().getTime());
                ps.setTimestamp(10, dServer);
                java.sql.Timestamp dTracker = new java.sql.Timestamp(message.getDateEvent().getTime());
                ps.setTimestamp(11, dTracker);
                ps.setDouble(12, message.getLatitude());
                ps.setDouble(13, message.getLongitude());
                ps.setDouble(14, message.getAltitude());
                ps.setDouble(15, message.getAngle());
                ps.setDouble(16, message.getSpeed());
                ps.setString(17, message._messageFields.toString());
            
                ps.executeUpdate();
            }
        }
    }
}
