/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gpsserver;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import org.json.*;
/**
 *
 * @author Oscar Medina
 */
public class EvtMngSensor  extends EventManager {
    @Override
    public void checkEvent(Message message) throws SQLException, AddressException, MessagingException {
        
        boolean condicion = false;
        HashMap<String, Sensor> listaSensores = get_sensors(message.getDeviceID());
        
        loadMessage(message);
        
        JSONArray ajson = new JSONArray(this._checked_value);
        
        for( int idx=0; idx < ajson.length(); idx++ ){
            
            JSONObject obj = ajson.getJSONObject(idx);

            if( obj.has("src") && listaSensores.containsKey(obj.getString("src")) ) {
                
                Sensor sensor =listaSensores.get(obj.getString("src"));
                
                double val = 0.0;
                double vparam = 0.0;
                
                if( obj.has("val")) {
                    val = Double.parseDouble(obj.getString("val"));
                }
                
                if( message._messageFields == null ) {
                    MainServer.logger.error("Clase contenedora de campos en null");
                } else if( !obj.has("src") ) {
                    MainServer.logger.error("Campo src en JSON igual a null");
                } else if( message._messageFields._campos == null ) {
                    MainServer.logger.error("ArrayList de valores en null");
                } else {
                    
                    //if( message._messageFields._campos.containsKey(obj.getString("src")) ) {
                    MainServer.logger.info("Buscando [" + sensor.getParam() + "]");
                    if( message._messageFields._campos.containsKey(sensor.getParam()) ) {
                        vparam = Double.parseDouble(message._messageFields._campos.get(sensor.getParam()));
                    } else {
                        MainServer.logger.error("Valor no encontrado en los campos recibidos [" + message._messageFields.toString() + "]");
                    }
                }
                
                if( obj.has("src") && obj.getString("src").trim().equals("speed")) {
                    vparam /= 1000;  // metros a kilometros.
                }
                
                MainServer.logger.info("VPARAM: " + vparam + " VAL: " + val);
                
                switch (obj.getString("cn")) {
                    case "eq":
                        condicion = (val == vparam);
                        break;
                    case "gr":
                        condicion = (vparam > val);
                        break;
                    case "lw":
                        condicion = (vparam < val);
                        break;
                    default:
                        break;
                }
                
                MainServer.logger.info("RES "+ condicion);
                
                if( !condicion ) break;
            
            } else {
                condicion = false;
            }
            
        }
        
        int status_event = get_status();

        if( condicion ) {
            MainServer.logger.info("Condicion verdadera");

            if( status_event == -1 ) {
                MainServer.logger.info("Status a1 inserta evento");

                set_status(1);

                insertEvent();
            } else {
                MainServer.logger.info("Pasa por alto: status=" + status_event);
            }
        } else {
            MainServer.logger.info("Condicion falsa");

            if( status_event != -1 ) {
                MainServer.logger.info("Setea a status -1");

                set_status(-1);
            } else {
                MainServer.logger.info("Pasa por alto, status=" + status_event);

            }
        }
    }
    
    protected HashMap<String, Sensor> get_sensors(String imei) throws SQLException {
        HashMap<String, Sensor> lista = new HashMap<String, Sensor>();
        String sql = "SELECT sensor_id, imei, name, param FROM gs_object_sensors WHERE imei = ?";
        
        PreparedStatement ps = this._con.prepareStatement(sql);
            
        ps.setString(1, imei);
        
        ResultSet rs = ps.executeQuery();
        
        while ( rs.next() ) {
            Sensor sensor = new Sensor();
            
            sensor.setId(rs.getInt(1));
            sensor.setImei(rs.getString(2));
            sensor.setName(rs.getString(3));
            sensor.setParam(rs.getString(4));
            
            lista.put(sensor.getName(), sensor);
        }
        
        return lista;
    }
}
