/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gpsserver;

import java.util.Date;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;

/**
 *
 * @author Oscar
 */
public class Message {
    protected String _deviceID;
    protected Date _dateEvent;
    protected Date _dateServer;
    protected double _latitude = 0.0;
    protected double _longitude = 0.0;
    protected double _speed = 0.0;
    protected double _angle = 0.0;
    protected double _altitude = 0.0;
    protected MessageFields _messageFields = new MessageFields();
    
    public String getDeviceID() { return this._deviceID; }
    public Date getDateEvent() { return this._dateEvent; }
    public Date getDateServer() { return this._dateServer; }
    public void setDateServer(Date dateServer) { this._dateServer = dateServer; }
    public double getLatitude() { return this._latitude; }
    public double getLongitude() { return this._longitude; }
    public double getSpeed() { return this._speed; }
    public double getAngle() { return this._angle; }
    public double getAltitude() { return this._altitude; }
    public MessageFields getMessageFields() { return this._messageFields; }

    public static String findIMEI(String msg)
    {
         int idx=msg.indexOf(",");
        
        if( idx == -1 ) return "";

        int fin = msg.substring(idx+1).indexOf(",");
        
        if( fin == -1 ) {
            return "";
        }
        
        return msg.substring(idx+1, fin+idx+1);
    }
    
    public static String[] splitMessages(String msg)
    {
        int pos = 0;
        ArrayList<String> lista = new ArrayList<>();
        
        while( (pos=msg.indexOf("*TS01")) != -1 ) {

            int posfinal = msg.substring(pos).indexOf("#");

            if( posfinal == -1 ) break;

            String submsg = msg.substring(pos, pos+posfinal);

            lista.add(submsg);

            msg = msg.substring(pos+posfinal);
        }
        
        String[] resultado = new String[lista.size()];
        
        for( int i = 0;  i < lista.size(); i++) {
            resultado[i] = lista.get(i);
        }
        
        return resultado;
    }
    
    public void parser(String buffer) throws Exception
    {
        String data[] = buffer.split(",");
        
        // Chequea Header
        if( !data[0].equals("*TS01")) throw new Exception("Header no identificado");
        
        // Tomo el id del device
        this._deviceID = data[1];
        
        // Fecha
        SimpleDateFormat ft = new SimpleDateFormat("HHmmssddMMyy");
        this._dateEvent = ft.parse(data[2]);
        
        parseGPSParams(data[3]);
        
        this._messageFields.parse(data);
    }
    
    protected void parseGPSParams(String buffer) {
        String params[] = buffer.split(";");
        
        this._latitude = Double.parseDouble(params[1].substring(1));
        this._latitude = params[1].charAt(0) == 'S' ? -this._latitude : this._latitude;
        
        this._longitude = Double.parseDouble(params[2].substring(1));
        this._longitude = params[2].charAt(0) == 'W' ? -this._longitude : this._longitude;
        
        this._speed = Double.parseDouble(params[3]);
        this._angle = Double.parseDouble(params[4]);
    }
    
}
