/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gpsserver;

/**
 *
 * @author omedi
 */
public class Sensor {
    private String _name;
    private String _imei;
    private String _param;
    private int _id;
    
    public int getId() { return this._id; }
    public void setId(int id) { this._id = id; }
    public String getName() { return this._name; }
    public void setName(String name) { this._name = name; }
    public String getImei() { return this._imei; }
    public void setImei(String imei) { this._imei = imei; }
    public String getParam() { return this._param; }
    public void setParam(String param) { this._param = param; }
}
