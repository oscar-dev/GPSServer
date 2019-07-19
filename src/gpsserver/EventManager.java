/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gpsserver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/*
| event_id           | int(11)       | NO   | PRI | NULL    | auto_increment |
| user_id            | int(11)       | NO   | MUL | NULL    |                |
| type               | varchar(10)   | NO   |     | NULL    |                |
| event_desc         | varchar(512)  | NO   |     | NULL    |                |
| notify_system      | varchar(40)   | NO   |     | NULL    |                |
| notify_arrow       | varchar(5)    | NO   |     | NULL    |                |
| notify_arrow_color | varchar(20)   | NO   |     | NULL    |                |
| notify_ohc         | varchar(5)    | NO   |     | NULL    |                |
| notify_ohc_color   | varchar(7)    | NO   |     | NULL    |                |
| imei               | varchar(20)   | NO   | MUL | NULL    |                |
| name               | varchar(50)   | NO   |     | NULL    |                |
| dt_server          | datetime      | NO   |     | NULL    |                |
| dt_tracker         | datetime      | NO   |     | NULL    |                |
| lat                | double        | NO   |     | NULL    |                |
| lng                | double        | NO   |     | NULL    |                |
| altitude           | double        | NO   |     | NULL    |                |
| angle              | double        | NO   |     | NULL    |                |
| speed              | double        | NO   |     | NULL    |                |
| params             | varchar(2048) | NO   |     | NULL    |                |


gs_user_events;
+----------------------------------+---------------+------+-----+---------+----------------+
| Field                            | Type          | Null | Key | Default | Extra          |
+----------------------------------+---------------+------+-----+---------+----------------+
| event_id                         | int(11)       | NO   | PRI | NULL    | auto_increment |
| user_id                          | int(11)       | NO   | MUL | NULL    |                |
| type                             | varchar(10)   | NO   |     | NULL    |                |
| name                             | varchar(50)   | NO   |     | NULL    |                |
| active                           | varchar(5)    | NO   |     | NULL    |                |
| duration_from_last_event         | varchar(5)    | NO   |     | NULL    |                |
| duration_from_last_event_minutes | int(11)       | NO   |     | NULL    |                |
| week_days                        | varchar(50)   | NO   |     | NULL    |                |
| day_time                         | varchar(512)  | NO   |     | NULL    |                |
| imei                             | text          | NO   |     | NULL    |                |
| checked_value                    | varchar(1024) | NO   |     | NULL    |                |
| route_trigger                    | varchar(5)    | NO   |     | NULL    |                |
| zone_trigger                     | varchar(5)    | NO   |     | NULL    |                |
| routes                           | varchar(4096) | NO   |     | NULL    |                |
| zones                            | varchar(4096) | NO   |     | NULL    |                |
| notify_system                    | varchar(40)   | NO   |     | NULL    |                |
| notify_email                     | varchar(5)    | NO   |     | NULL    |                |
| notify_email_address             | varchar(500)  | NO   |     | NULL    |                |
| notify_sms                       | varchar(5)    | NO   |     | NULL    |                |
| notify_sms_number                | varchar(500)  | NO   |     | NULL    |                |
| notify_arrow                     | varchar(5)    | NO   |     | NULL    |                |
| notify_arrow_color               | varchar(20)   | NO   |     | NULL    |                |
| notify_ohc                       | varchar(5)    | NO   |     | NULL    |                |
| notify_ohc_color                 | varchar(7)    | NO   |     | NULL    |                |
| email_template_id                | int(11)       | NO   |     | NULL    |                |
| sms_template_id                  | int(11)       | NO   |     | NULL    |                |
| cmd_send                         | varchar(5)    | NO   |     | NULL    |                |
| cmd_gateway                      | varchar(5)    | NO   |     | NULL    |                |
| cmd_type                         | varchar(5)    | NO   |     | NULL    |                |
| cmd_string                       | varchar(256)  | NO   |     | NULL    |                |
+----------------------------------+---------------+------+-----+---------+----------------+
*/

/**
 *
 * @author Oscar
 */
public abstract class EventManager {
    protected Connection _con;
    protected Properties _properties;
    /* gs_user_events */
    protected int _event_id;
    protected String _duration_from_last_event;
    protected int _duration_from_last_event_minutes;
    protected String _week_days;
    protected String _day_time;
    protected String _checked_value;
    protected String _route_trigger;
    protected String _zone_trigger;
    protected String _routes;
    protected String _zones;
    
    /* gs_user_events_data */
    protected int _user_id;
    protected String _type;
    protected String _event_desc;
    protected String _notify_system;
    protected String _notify_arrow;
    protected String _notify_arrow_color;
    protected String _notify_ohc;
    protected String _notify_ohc_color;
    protected String _notify_email;
    protected String _notify_email_address;
    protected int _email_template_id;
    
    protected String _imei;
    protected String _name;
    protected Date _dt_tracker;
    protected Date _dt_server;
    protected double _lat;
    protected double _lng;
    protected double _altitude;
    protected double _angle;
    protected double _speed;
    protected String _params;
    protected String _language;
    
    protected HashMap<String, String> _keysMails = new HashMap<>();

    public void setConnection(Connection con) { this._con = con; }
    
    public void setProperties(Properties properties) { this._properties = properties; }
    
    public abstract void checkEvent(Message message) throws SQLException, AddressException, MessagingException;
    
    public void loadEvent(ResultSet rs) throws SQLException {
        
        this._type = rs.getString("type");
        this._user_id = rs.getInt("user_id");
        this._event_id = rs.getInt("event_id");
        this._event_desc = rs.getString("name");
        this._notify_system = rs.getString("notify_system");
        this._notify_arrow = rs.getString("notify_arrow");
        this._notify_arrow_color = rs.getString("notify_arrow_color");
        this._notify_ohc = rs.getString("notify_ohc");
        this._notify_ohc_color = rs.getString("notify_ohc_color");
        this._duration_from_last_event = rs.getString("duration_from_last_event");
        this._duration_from_last_event_minutes = rs.getInt("duration_from_last_event_minutes");
        this._week_days = rs.getString("week_days");
        this._day_time = rs.getString("day_time");
        this._checked_value = rs.getString("checked_value");
        this._route_trigger = rs.getString("route_trigger");
        this._zone_trigger = rs.getString("zone_trigger");
        this._routes = rs.getString("routes");
        this._zones = rs.getString("zones");
        this._notify_email = rs.getString("notify_email");
        this._notify_email_address = rs.getString("notify_email_address");
        this._email_template_id = rs.getInt("email_template_id");
        this._language = rs.getString("language");
    }
    
    protected void insertEvent() throws SQLException, AddressException, MessagingException {

        String sql = "INSERT INTO gs_user_events_data( user_id, type, " +
                "event_desc, notify_system, notify_arrow, notify_arrow_color, " +
                "notify_ohc, notify_ohc_color, imei, name, dt_tracker, dt_server, " +
                "lat, lng, altitude, angle, speed, params) VALUES( " +
                "?, ?, ?, ?, ?, ?, ?, ?, ?, '', ?, ?, ?, ?, ?, ?, ?, ?)";
                
        PreparedStatement ps = this._con.prepareStatement(sql);

        ps.setInt( 1, this._user_id);
        ps.setString( 2, this._type);
        ps.setString( 3, this._event_desc);
        ps.setString( 4, this._notify_system);
        ps.setString( 5, this._notify_arrow);
        ps.setString( 6, this._notify_arrow_color);
        ps.setString( 7, this._notify_ohc);
        ps.setString( 8, this._notify_ohc_color);
        ps.setString( 9, this._imei);
        
        java.sql.Timestamp dTracker = new java.sql.Timestamp(this._dt_tracker.getTime());
        ps.setTimestamp( 10, dTracker);
        
        java.sql.Timestamp dServer = new java.sql.Timestamp(this._dt_server.getTime());
        ps.setTimestamp( 11, dServer);
        
        ps.setDouble( 12, this._lat);
        ps.setDouble( 13, this._lng);
        ps.setDouble( 14, this._altitude);
        ps.setDouble( 15, this._angle);
        ps.setDouble( 16, this._speed);
        ps.setString( 17, this._params);

        ps.executeUpdate();
        
        if( this._notify_email.equals("true") ) {
            sendEMail();
        }
    }

    public void loadMessage(Message msg) throws SQLException {
        
        this._imei = msg.getDeviceID();
        this._dt_tracker = msg.getDateEvent();
        this._dt_server = msg.getDateServer();
        this._lat = msg.getLatitude();
        this._lng = msg.getLongitude();
        this._altitude = msg.getAltitude();
        this._angle = msg.getAngle();
        this._speed = msg.getSpeed();
        this._params = msg.getMessageFields().toString();
        
        this._keysMails.put("%ALT%", Util.Double2String(msg.getAltitude()));
        this._keysMails.put("%IMEI%", msg.getDeviceID());
        this._keysMails.put("%ANGLE%", Util.Double2String( msg.getAngle()));
        this._keysMails.put("%LAT%", Util.Double2String( msg.getLatitude()));
        this._keysMails.put("%LNG%", Util.Double2String( msg.getLongitude()));
        this._keysMails.put("%SPEED%", Util.Double2String(msg.getSpeed()));
        this._keysMails.put("%DT_POS%", Util.Date2String(msg.getDateEvent()));
        this._keysMails.put("%DT_SER%", Util.Date2String(msg.getDateServer()));
        this._keysMails.put("%NAME%", this.getObjectName());
        
    }
    
    public int get_status() throws SQLException
    {
        int res = -1;
        String sql = "SELECT event_status FROM gs_user_events_status WHERE event_id = ? AND imei=?";
        PreparedStatement ps = this._con.prepareStatement(sql);
            
        ps.setInt(1, this._event_id);
        ps.setString(2, this._imei);
        
        try (ResultSet rs = ps.executeQuery()) {
            if( rs.next() ) {
                res = rs.getInt(1);
            }
        }
        
        return res;
    }
    
    public void set_status(int eventStatus) throws SQLException {
        int res=0;
        MainServer.logger.info("actualizando evento.");
        String sql = "UPDATE gs_user_events_status SET dt_server=?, event_status=? WHERE event_id = ? AND imei=?";
        
        PreparedStatement ps = this._con.prepareStatement(sql);

        java.sql.Timestamp dServer = new java.sql.Timestamp(this._dt_server.getTime());
        
        ps.setTimestamp(1, dServer);
        ps.setInt(2, eventStatus);
        ps.setInt(3, this._event_id);
        ps.setString(4, this._imei);
        
        res = ps.executeUpdate();
        
        //MainServer.logger.info("Actualizando evento. RES: " + Integer.toString(res));
        
        if( res <= 0 ) {
            ps.close();
            
            MainServer.logger.info("Insertando evento.");
            
            sql = "INSERT INTO gs_user_events_status(event_id, dt_server, imei, event_status) VALUES( ?, ?, ?, ?)";
        
            ps = this._con.prepareStatement(sql);

            ps.setInt(1, this._event_id);
            ps.setTimestamp(2, dServer);            
            ps.setString(3, this._imei);
            ps.setInt(4, eventStatus);
        
            ps.executeUpdate();
        }
        ps.close();
    }
    
    
    protected String getObjectName() throws SQLException
    {
        String value="";
        String sql = "SELECT name FROM gs_objects WHERE imei='" + this._imei + "'";
        
        PreparedStatement ps = this._con.prepareStatement(sql);
        
        ResultSet rs = ps.executeQuery();
        
        if( rs.next() ) {
            value = rs.getString(1);
        }
        
        return value;
    }
    
    protected void sendEMail() throws SQLException, AddressException, MessagingException
    {
        MainServer.logger.info("Enviando mail...");
        
        String msg="";
        String subject="";
        String mMapLink = this._properties.getProperty("maps.link", "");
        
        mMapLink=mMapLink.replace("%LAT%", Double.toString(this._lat));
        mMapLink=mMapLink.replace("%LNG%", Double.toString(this._lng));
        
        this._keysMails.put("%EVENT%", this._event_desc);
        this._keysMails.put("%G_MAP%", mMapLink);
        
        //MainServer.logger.info("Obtiene template de base de datos, language {}...", this._language);
        
        String sql = "SELECT subject, message FROM gs_templates WHERE name = ? AND language=?";
        PreparedStatement ps = this._con.prepareStatement(sql);

        ps.setString(1, "event_email");
        ps.setString(2, this._language);
            
        try (ResultSet rs = ps.executeQuery()) {
            if( rs.next() ) {
                subject = rs.getString(1);
                msg = rs.getString(2);
            }
        }
        
        Iterator i=this._keysMails.entrySet().iterator();
        
        while( i.hasNext() ) {
            Map.Entry<String,String> p = (Map.Entry)i.next();
            
            String kValue = p.getKey();
            String vValue = p.getValue();

            subject=subject.replace(kValue, vValue);
            msg=msg.replace(kValue, vValue);
        }
        
        int iPort = Integer.parseInt(this._properties.getProperty("mail.port", "0"));
        String mServer = this._properties.getProperty("mail.server");
        String mUser = this._properties.getProperty("mail.user");
        String mPassword = this._properties.getProperty("mail.password");
        String mFrom = this._properties.getProperty("mail.from");
        String starttls = this._properties.getProperty("mail.starttls.enable");
        
        Properties properties = new Properties();

        properties.put("mail.smtp.host", mServer);
        properties.put("mail.smtp.starttls.enable", starttls);
        properties.put("mail.smtp.port", iPort );
        properties.put("mail.smtp.mail.sender", mFrom);
        properties.put("mail.smtp.user", mUser);
        properties.put("mail.smtp.auth", "true");

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(mUser, mPassword);
            }
        } );
        
        MainServer.logger.info("Emviando mail...");
        
        javax.mail.Message message = new MimeMessage(session);

        message.setFrom(new InternetAddress(mFrom));

        message.setRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(this._notify_email_address));

        message.setSubject(subject);

        message.setText(msg);

        Transport.send(message);
        
        //MainServer.logger.info("Se envió mail a {}.", this._notify_email_address);
    }
    
}
