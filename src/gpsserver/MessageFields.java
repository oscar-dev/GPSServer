/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gpsserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

/**
 *
 * @author Oscar
 */
public class MessageFields {
    protected HashMap<String, String> _campos;
    
    public String getVal(String key) {
        return this._campos.get(key);
    }
    
    public void parse(String[] data) {
 
        this._campos = new HashMap<>();
        
        this._campos.put("dtc", "");
        
        for( int i=3; i < data.length; i++) {
            String[] campos = data[i].split(":");
            
            String infoKey = campos[0].substring(0, 3);
            
            switch( infoKey ) {
                case "GPS":     // Ya lo proceso en el mensaje principal.
                    {
                        String[] values = campos[1].split(";");
                        
                        double dVal = Double.parseDouble(values[5]);
                        
                        this._campos.put("hdop", String.format(Locale.ROOT, "%02.2f", dVal));
                    }
                    break;
                case "LBS":     // No va
                    break;
                case "STT":
                    procesaCampoSTT(infoKey, campos[1]);
                    break;
                case "MGR":     // Km.
                    this._campos.put("mgr", campos[1]);
                    break;
                case "ADC":
                    {
                        int idx=1;
                        String[] adcs = campos[1].split(";");
                        
                        for(String adcn : adcs ) {
                            this._campos.put("ai" + idx, adcn);
                            idx++;
                        }
                }
                    break;
                case "GFS":     // No va
                    break;
                case "OBD":
                    procesaCamposOBDOAL(infoKey, campos[1]);
                    break;
                case "OAL":
                    procesaCamposOBDOAL("dtc", campos[1]);
                    break;
                case "FUL":     // No va
                    break;
                case "HDB":
                    procesaCampoHDB(infoKey, campos[1]);
                    break;
                case "CAN":     // No va
                    break;
                case "HVD":     // No va
                    break;
                case "VIN":
                    this._campos.put("vin", campos[1]);
                    break;
                case "RFI":     // No va
                    break;
                case "EVT":     // No va
                    break;
                case "BCN":     // No va
                    break;
                case "EGT":     // No va
                    break;
                case "TRP":     // No va
                    break;
                case "SAT":
                    procesaCampoSAT(infoKey, campos[1]);
                    break;
            }
        }
    }
    
    public boolean obdContaints() {
        boolean ret = false;
        
        for( HashMap.Entry<String,String> i : this._campos.entrySet()) {
            try {
                
                if( i.getKey().startsWith("obd") ) {
                    return true;
                }
                
            } catch(Exception e) {    }
        }
        return ret;
    }
    
    protected void procesaCampoSTT(String nombreCampo, String campo) 
    {
        String[] valores = campo.split(";");
        
        if( valores.length != 2 ) return;

        int iStatus =  Integer.parseInt(valores[0], 16);
        //int iAlarm =  Integer.parseInt(valores[1], 16);
        
        this._campos.put("bats", (iStatus & 0x01) != 0 ? "1" : "0");
        this._campos.put("acc", (iStatus & 0x20) != 0 ? "1" : "0");
    }
    
    protected void procesaCampoHDB(String nombreCampo, String campo)
    {
        int val = Integer.parseInt(campo, 16);
        String sVal = "";
        
        if( (val & 0x01) != 0 ) { sVal = "1"; }
        
        if( (val & 0x02) != 0 ) { sVal = "2"; }
        
        if( (val & 0x04) != 0 ) { sVal = "3"; }
        
        this._campos.put(nombreCampo.toLowerCase(), sVal);        
    }
    
    protected void procesaCampoSAT(String nombreCampo, String campo)
    {
        String[] valores = campo.split(";");
        
        if( valores.length != 3 ) return;
        
        this._campos.put(nombreCampo.toLowerCase() + "1", valores[0]);
        this._campos.put(nombreCampo.toLowerCase() + "2", valores[1]);
        this._campos.put(nombreCampo.toLowerCase() + "3", valores[2]);
    }
    
    protected void procesaCamposOBDOAL(String nombreCampo, String campo)
    {
        int pos=0;
        short[] data = new short[campo.length() / 2];

        for( int i=0, j=0; i < campo.length(); i+=2, j++) {

            data[j] = (Short.parseShort(campo.substring(i, i+2), 16));
        }

        while( pos < data.length ) {
            int len = (int)(data[pos] >> 4) & 0x0F;

            if( (len+pos) > data.length ) break;

            if( len < 3 || len > 8 ) {

                pos += len;
                continue;
            }
            
            nombreCampo = nombreCampo.toLowerCase();

            int service = (int)(data[pos] & 0x0F);
            
            String key =  String.format("%s%02X", nombreCampo.toLowerCase(), service);

            switch (service)
            {
                case 1://Mode 01
                case 2://Mode 02
                    {
                        int pid = data[pos + 1];
                        short[] pidValue = new short[len - 2];
                        System.arraycopy(data, pos+2, pidValue, 0, pidValue.length);
                        ObdService0102Decode(nombreCampo, pidValue, service, pid);
                        break;
                    }
                case 3://Mode 03
                    {
                        short[] value = new short[len - 1];
                        System.arraycopy(data, pos+1, value, 0, value.length);
                        ObdService03Decode(nombreCampo, service, value);
                        break;
                    }
                case 4://Mode 04
                    break;
                case 5://Mode 05
                    break;
                case 6://Mode 06
                    break;
                case 7://Mode 07
                    break;
                case 8://Mode 08
                    break;
                case 9://Mode 09
                    break;
                case 10://Mode 0A
                    break;
                case 11://mode 21  Read Data By Identifier
                    {
                        short[] value = new short[len - 1];
                        System.arraycopy(data, pos+1, value, 0, value.length);

                        String sVal = "";

                        for( int i = 0 ; i < value.length; i++) {
                            sVal += String.format("%02d", value[i]);
                        }
                        
                        key += String.format("%02X", 0x21);
                        
                        this._campos.put(key, sVal);
                        break;
                    }
                case 12://mode 22  Read Data By Identifier
                    {
                        int pid = 0;

                        pid |= data[pos+2];
                        pid |= (data[pos+1] << 8);

                        short[] value = new short[len - 3];
                        System.arraycopy(data, pos+3, value, 0, value.length);

                        UdsService22Decode(nombreCampo, service, value, pid);
                        break;
                    }
                case 15://CANBUS sniffer data
                    {
                        short[] value = new short[len - 1];
                        System.arraycopy(data, pos+1, value, 0, value.length);
                        ObdCanSnifferDecode( nombreCampo, value);
                        break;
                    }
                default:
                    break;
            }

            pos += len;
        }
    }
    
    void UdsService22Decode(String nombreCampo, int service, short[] value, int pid)//For VW Amarok
    {
        String sVal = "";

        nombreCampo += String.format("%02X", service);
        nombreCampo += String.format("%02X", pid);
        
        switch (pid)
        {
            case 0x16A9://Distance
                {
                    if( value.length != 4 ) return;

                    nombreCampo += String.format("%02X", 0x22);
                    
                    long distance = value[3] | (value[2] << 8) | (value[1] << 16) | (value[0] << 24);
                    
                    sVal = Long.toString(distance);
                    break;
                }
            case 0x1047://Engine torque
                {
                    if (value.length != 2) return;
                    
                    long torque = value[1] | (value[0] << 8);
                    
                    nombreCampo += String.format("%02X", 0x22);
                    
                    torque *= 0.1;
                    
                    sVal = Long.toString(torque);
                    break;
                }
            case 0x1221://Accelerator pedal
                {
                    if (value.length != 2) return;
                    
                    long pedal = value[1] | (value[0] << 8);
                    
                    nombreCampo += String.format("%02X", 0x22);
                    
                    pedal *= 0.2;
                    
                    sVal = Long.toString(pedal);
                    
                    break;
                }
            case 0xF449://Accelerator position
                {
                    if (value.length != 1) return;
                    
                    nombreCampo += String.format("%02X", 0x22);
                    
                    double dVal = ((double)value[0]) * 100.0 / 255.0;
                    
                    sVal = String.format(Locale.ROOT, "%02.2f", dVal);
                    
                    break;
                }
            case 0x17D6://Brake actuated status
                {
                    if (value.length != 1) return;
                    
                    nombreCampo += String.format("%02X", 0x22);
                    
                    sVal = Short.toString(value[0]);
                    
                    break;
                }
            case 0xF40C://Engine speed
                {
                    if (value.length != 2) return;

                    nombreCampo += String.format("%02X", 0x22);
                    
                    long rpm = value[0] | (value[1] << 8);
                    
                    rpm *= 0.25;
                    
                    sVal = Long.toString(rpm);
                    
                    break;
                }
            case 0x111A://Fuel consumption
                {
                    if (value.length != 2) return;
                    
                    nombreCampo += String.format("%02X", 0x22);
                    
                    long consumption = value[0] | (value[1] << 8);
                    
                    consumption *= 0.01;
                    
                    sVal = Long.toString(consumption);
                    
                    break;
                }
            case 0x100C://Fuel Level
                {
                    if (value.length != 2) return;
                    
                    nombreCampo += String.format("%02X", 0x22);
                    
                    long level = value[1] | (value[0] << 8);
                    
                    level *= 0.01;
                    
                    sVal = Long.toString(level);
                    
                    break;
                }
            case 0xF423://Fuel pressure
                {
                    if (value.length != 2) return;
                    
                    nombreCampo += String.format("%02X", 0x22);
                    
                    long pressure = value[1] | (value[0] << 8);
                    
                    pressure *= 10;
                    
                    sVal = Long.toString(pressure);
                    
                    break;
                }
            case 0x121E://Fuel pressure regulator value
                {
                    if (value.length != 2) return;
                    
                    nombreCampo += String.format("%02X", 0x22);
                    
                    long val = value[1] | (value[0] << 8);
                    
                    val *= 100;
                    
                    sVal = Long.toString(val);
                    
                    break;
                }
            case 0x11BF://Fuel pressure regulator value
                {
                    if (value.length != 2) return;
                    
                    nombreCampo += String.format("%02X", 0x22);
                    
                    long val = value[1] | (value[0] << 8);
                    
                    val *= 0.01;
                    
                    sVal = Long.toString(val);
                    
                    break;
                }
            case 0x106B://Limitation torque
                {
                    if (value.length != 2) return;
                    
                    nombreCampo += String.format("%02X", 0x22);
                    
                    long val = value[1] | (value[0] << 8);
                    
                    val *= 0.01;
                    
                    sVal = Long.toString(val);
                    
                    break;
                }
            case 0x116B://Rail pressure regulation
                {
                    if (value.length != 1) return;
                    
                    nombreCampo += String.format("%02X", 0x22);
                    
                    sVal = Short.toString(value[0]);
                    
                    break;
                }
            case 0x104C://Air mass: specified value
                {
                    if (value.length != 2) return;
                    
                    nombreCampo += String.format("%02X", 0x22);
                    
                    long val = value[1] | (value[0] << 8);
                    
                    val *= 0.01;
                    
                    sVal = Long.toString(val);
                    
                    break;
                }
            case 0x1635://Sensor f charge air press betw turbochargers
                {
                    if (value.length != 2) return;
                    
                    nombreCampo += String.format("%02X", 0x22);
                    
                    long val = value[1] | (value[0] << 8);
                    
                    val *= 0.02;
                    
                    sVal = Long.toString(val);
                    
                    break;
                }
            case 0x1634://Sensor for charge air press
                {
                    if (value.length != 2) return;
                    
                    nombreCampo += String.format("%02X", 0x22);
                    
                    long val = value[1] | (value[0] << 8);
                    
                    val *= 0.02;
                    
                    sVal = Long.toString(val);
                    
                    break;
                }
            case 0x100D://Selected gear
                {
                    if (value.length != 1) return;
                    
                    nombreCampo += String.format("%02X", 0x22);
                    
                    sVal = Short.toString(value[0]);
                    
                    break;
                }
            case 0x162D://Fuel temperature
                {
                    if (value.length != 2) return;
                    
                    nombreCampo += String.format("%02X", 0x22);
                    
                    long val = value[1] | (value[0] << 8);
                    
                    val = (long)((double)val * 0.01) - 273;
                    
                    sVal = Long.toString(val);
                    
                    break;
                }
            case 0xF411://Absolute Throttle Position
                {
                    if (value.length != 1) return;
                    
                    nombreCampo += String.format("%02X", 0x22);
                    
                    double pos = ((double)value[0] * 100.0) / 255.0;
                    
                    sVal = String.format("%02.2f", pos);
                    
                    break;
                }
            case 0xF40D://Vehicle speed
                {
                    if (value.length != 1) return;
                    
                    nombreCampo += String.format("%02X", 0x22);
                    
                    sVal = Short.toString(value[0]);
                    
                    break;
                }
            case 0xF405://Engine Coolant Temperature
                {
                    if (value.length != 1) return;
                    
                    short val = value[0];
                    
                    val -= 40;
                    
                    sVal = Short.toString(val);
                    
                    break;
                }
            case 0x2222://Indicator lamps
                {
                    if (value.length != 2) return;
                    
                    nombreCampo += String.format("%02X", 0x22);
                    
                    long val = value[1] | (value[0] << 8);
                    
                    sVal = "";
                    sVal += ( (val & 0x0100) == 0 ? "0" : "1" );
                    sVal += ( (val & 0x0004) == 0 ? "0" : "1" );
                    
                    break;
                }
            case 0x2223://MIL
                {
                    if (value.length != 2) return;

                    nombreCampo += String.format("%02X", 0x22);
                    
                    long val = value[1] | (value[0] << 8);
                    
                    sVal = "";
                    sVal += ( (val & 0x0004) == 0 ? "0" : "1" );

                    break;
                }
            case 0x2260://ESI: remaining distance
                {
                    if (value.length != 2) return;
                    
                    nombreCampo += String.format("%02X", 0x22);
                    
                    long val = value[1] | (value[0] << 8);
                    
                    sVal = Long.toString(val);
                    
                    break;
                }
            case 0x2261://ESI: remaining running days
                {
                    if (value.length != 2) return;
                    
                    nombreCampo += String.format("%02X", 0x22);
                    
                    long val = value[1] | (value[0] << 8);
                    
                    sVal = Long.toString(val);
                    
                    break;
                }
            default:
                {
                    sVal = "";
                    
                    for (int i = 0; i < value.length; i++)
                    {
                        sVal += String.format("%02X", value[i]);
                    }
                    
                    break;
                }
        }
        
        this._campos.put(nombreCampo, sVal);
    }
    
    void ObdService0102Decode(String nombreCampo, short[] pidValue, int service, int pid)
    {
        String sVal = "";
        switch(pid) {
            case 0x01:
                if( pidValue.length != 4) return;
                
                nombreCampo += String.format("%02X", service);
                nombreCampo += String.format("%02X", pid);
                
                sVal += (pidValue[0] & 0x80) != 0 ? "1" : "0";
                sVal += (pidValue[1] & 0x01) != 0 ? "1" : "0";
                sVal += (pidValue[1] & 0x02) != 0 ? "1" : "0";
                sVal += (pidValue[1] & 0x04) != 0 ? "1" : "0";
                sVal += (pidValue[1] & 0x08) != 0 ? "1" : "0";
                sVal += (pidValue[1] & 0x10) != 0 ? "1" : "0";
                sVal += (pidValue[1] & 0x20) != 0 ? "1" : "0";
                sVal += (pidValue[1] & 0x40) != 0 ? "1" : "0";
                sVal += (pidValue[2] & 0x01) != 0 ? "1" : "0";
                sVal += (pidValue[2] & 0x02) != 0 ? "1" : "0";
                sVal += (pidValue[2] & 0x04) != 0 ? "1" : "0";
                sVal += (pidValue[2] & 0x08) != 0 ? "1" : "0";
                sVal += (pidValue[2] & 0x10) != 0 ? "1" : "0";
                sVal += (pidValue[2] & 0x20) != 0 ? "1" : "0";
                sVal += (pidValue[2] & 0x40) != 0 ? "1" : "0";
                sVal += (pidValue[2] & 0x80) != 0 ? "1" : "0";
                sVal += (pidValue[3] & 0x01) != 0 ? "1" : "0";
                sVal += (pidValue[3] & 0x02) != 0 ? "1" : "0";
                sVal += (pidValue[3] & 0x04) != 0 ? "1" : "0";
                sVal += (pidValue[3] & 0x08) != 0 ? "1" : "0";
                sVal += (pidValue[3] & 0x10) != 0 ? "1" : "0";
                sVal += (pidValue[3] & 0x20) != 0 ? "1" : "0";
                sVal += (pidValue[3] & 0x40) != 0 ? "1" : "0";
                sVal += (pidValue[3] & 0x80) != 0 ? "1" : "0";
                
                this._campos.put(nombreCampo, sVal);
                
                break;
            case 0x04:
                {
                    if( pidValue.length != 1) return;

                    nombreCampo += String.format("%02X", service);
                    nombreCampo += String.format("%02X", pid);

                    double dVal  = ((double)pidValue[0]) * 100.0 / 255.0;

                    sVal = String.format( Locale.ROOT, "%2.2f", dVal);

                    this._campos.put(nombreCampo, sVal);
                }   
                break;
            case 0x05:
                {
                    if( pidValue.length != 1) return;

                    nombreCampo += String.format("%02X", service);
                    nombreCampo += String.format("%02X", pid);

                    int iVal = pidValue[0];
                    iVal -= 40;

                    sVal = String.format("%d", iVal);

                    this._campos.put(nombreCampo, sVal);
                }
                break;
            case 0x06:
            case 0x07:
            case 0x08:
            case 0x09:
                {
                    double dVal = 0.0;
                    double dVal2 = 0.0;
                    
                    nombreCampo += String.format("%02X", service);
                    nombreCampo += String.format("%02X", pid);

                    switch (pidValue.length) {
                        case 1:
                            dVal = ((((double)pidValue[0])-128.0) / 128.0) * 100.0;
                            sVal = String.format(Locale.ROOT, "%02.2f", dVal);
                            break;
                        case 2:
                            dVal = ((((double)pidValue[0])-128.0) / 128.0) * 100.0;
                            dVal2 = ((((double)pidValue[1])-128.0) / 128.0) * 100.0;
                            
                            sVal = String.format(Locale.ROOT, "%02.2f,%02.2f", dVal, dVal2);
                            break;
                        default:
                            return;
                    }
                    
                    this._campos.put(nombreCampo, sVal);
                }
                break;
            case 0x0A:
                {
                    nombreCampo += String.format("%02X", service);
                    nombreCampo += String.format("%02X", pid);
                    
                    if( pidValue.length != 1 ) return;
                    
                    int iVal = pidValue[0]*3;
                    
                    sVal = Integer.toString(iVal);
                    
                    this._campos.put(nombreCampo, sVal);
                }
                break;
            case 0x0B:
                {
                    nombreCampo += String.format("%02X", service);
                    nombreCampo += String.format("%02X", pid);
                    
                    if( pidValue.length != 1 ) return;
                    
                    int iVal = pidValue[0];
                    
                    sVal = Integer.toString(iVal);
                    
                    this._campos.put(nombreCampo, sVal);
                }
                break;
            case 0x0C:
                {
                    nombreCampo += String.format("%02X", service);
                    nombreCampo += String.format("%02X", pid);
                    
                    if( pidValue.length != 2 ) return;
                    
                    double dVal = ((double)(pidValue[0] * 256.0 + pidValue[1])) / 4.0;
                    
                    sVal = String.format(Locale.ROOT, "%02.2f", dVal);
                    
                    this._campos.put(nombreCampo, sVal);
                }
                break;
            case 0x0D:
                {
                    nombreCampo += String.format("%02X", service);
                    nombreCampo += String.format("%02X", pid);
                    
                    if( pidValue.length != 1 ) return;
                    
                    int iVal = pidValue[0];
                    
                    this._campos.put(nombreCampo, Integer.toString(iVal));
                }
                break;
            case 0x0E:
                {
                    nombreCampo += String.format("%02X", service);
                    nombreCampo += String.format("%02X", pid);
                    
                    if( pidValue.length != 1 ) return;
                    
                    double dVal = ((double)(pidValue[0]-128.0)) / 2.0;
                    
                    sVal = String.format(Locale.ROOT, "%02.2f", dVal);
                    
                    this._campos.put(nombreCampo, sVal);
                }
                break;
            case 0x0F:
                {
                    nombreCampo += String.format("%02X", service);
                    nombreCampo += String.format("%02X", pid);
                    
                    if( pidValue.length != 1 ) return;
                    
                    int iVal = pidValue[0];
                    
                    this._campos.put(nombreCampo, Integer.toString(iVal-40));
                }
                break;
            case 0x10:
                {
                    nombreCampo += String.format("%02X", service);
                    nombreCampo += String.format("%02X", pid);
                    
                    if( pidValue.length != 2 ) return;
                    
                    double dVal = ((double)(pidValue[0] * 256.0 + pidValue[1])) / 100.0;

                    sVal = String.format(Locale.ROOT, "%02.2f", dVal);
                    
                    this._campos.put(nombreCampo, sVal);
                }
                break;
            case 0x11:
                {
                    nombreCampo += String.format("%02X", service);
                    nombreCampo += String.format("%02X", pid);
                    
                    if( pidValue.length != 1 ) return;
                    
                    double dVal = ((double)pidValue[0]) * 100.0 / 255.0;

                    sVal = String.format(Locale.ROOT, "%02.2f", dVal);
                    
                    this._campos.put(nombreCampo, sVal);
                }
                break;
            case 0x21:
                {
                    nombreCampo += String.format("%02X", service);
                    nombreCampo += String.format("%02X", pid);
                    
                    if( pidValue.length != 2 ) return;
                    
                    int iVal = (256 * pidValue[0]) + pidValue[1];
                    
                    this._campos.put(nombreCampo, Integer.toString(iVal));
                }
                break;
            case 0x2F:
                {
                    nombreCampo += String.format("%02X", service);
                    nombreCampo += String.format("%02X", pid);
                    
                    if( pidValue.length != 1 ) return;
                    
                    double dVal = ((double)pidValue[0]) * 100.0 / 255.0;

                    sVal = String.format(Locale.ROOT, "%02.2f", dVal);
                    
                    this._campos.put(nombreCampo, sVal);
                }
                break;
            case 0x31:
                {
                    nombreCampo += String.format("%02X", service);
                    nombreCampo += String.format("%02X", pid);
                    
                    if( pidValue.length != 2 ) return;
                    
                    int iVal = (256 * pidValue[0]) + pidValue[1];
                    
                    this._campos.put(nombreCampo, Integer.toString(iVal));
                }
                break;
            default:
                {
                    nombreCampo += String.format("%02X", service);
                    nombreCampo += String.format("%02X", pid);
                    
                    for(int i=0; i < pidValue.length; i++) {
                        sVal += String.format("%02d", pidValue[i]);
                    }
                
                    this._campos.put(nombreCampo, sVal);
                }
                break;
        }
    }
    
    void ObdCanSnifferDecode(String nombreCampo, short[] value)
    {
        String sVal = "";
        
        if( value.length < 3 ) return;
        
        nombreCampo += String.format("%02X", value[0]);
        nombreCampo += String.format("%02X", value[1]);
        
        for( int i= 2; i < value.length; i++) {
            sVal += String.format("%02d", value[i]);
        }
        
        this._campos.put(nombreCampo, sVal);
    }
    
    protected void ObdService03Decode(String nombreCampo, int service,  short[] value)
    {
        int offset;
        if (value.length % 2 != 0)
            offset = 1;
        else
            offset = 0;
        
        String sVal;
        String[] dtcChars = { "P", "C", "B", "U" };
        ArrayList<String> lista = new ArrayList<>();
        
        //nombreCampo += String.format("%02d", service);
        
        for (int i = 0; i < value.length / 2; i++)
        {
            short dtcA = value[2 * i + offset];
            short dtcB = value[2 * i + offset + 1];
            if (dtcA == 0 && dtcB == 0)
                continue;
 
            lista.add(String.format("%s%02X%02X",
                                dtcChars[((dtcA >> 6) & 0x03)],
                                (dtcA & 0x3F),
                                dtcB));
        }
        
        String[] values = new String[lista.size()];
        
        for(int j=0; j < lista.size(); j++) {
            values[j] = lista.get(j);
        }
        
        if( values.length > 1 ) {
            sVal = String.join("/", values);
        } else {
            sVal = values[0];
        }
        
        this._campos.put(nombreCampo, sVal);
    }

    @Override
    public String toString()
    {
        String buffer = "{";
        
        for( HashMap.Entry<String,String> e : this._campos.entrySet()) {
            buffer += "\"";
            buffer += e.getKey();
            buffer += "\":\"";
            buffer += e.getValue();
            buffer += "\",";
        }
        
        if(buffer.charAt(buffer.length()-1) == ',' ) buffer = buffer.substring(0, buffer.length()-1);
        
        buffer += "}";
        
        return buffer;
    }
}
