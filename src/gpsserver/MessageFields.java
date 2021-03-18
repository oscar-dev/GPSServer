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
                    if(campos.length > 1 ){
                        String[] values = campos[1].split(";");
                        
                        double dVal = Double.parseDouble(values[5]);
                        
                        this._campos.put("hdop", String.format(Locale.ROOT, "%02.2f", dVal));
                    }
                    break;
                case "LBS":     // No va
                    break;
                case "STT":
                    if(campos.length > 1 ) {
                        procesaCampoSTT(infoKey, campos[1]);
                    }
                    break;
                case "MGR":     // Km.
                    if(campos.length > 1 ) {
                        this._campos.put("mgr", campos[1]);
                    }
                    break;
                case "ADC":
                    if(campos.length > 1 ){
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
                    if(campos.length > 1 ) {
                        procesaCamposOBDOAL(infoKey, campos[1]);
                    }
                    break;
                case "OAL":
                    if(campos.length > 1 ) {
                        procesaCamposOBDOAL("dtc", campos[1]);
                    }
                    break;
                case "FUL":     // No va
                    break;
                case "HDB":
                    if(campos.length > 1 ) {
                        procesaCampoHDB(infoKey, campos[1]);
                    }
                    break;
                case "CAN":     // No va
                    if(campos.length > 1 ) {
                        procesaCamposCAN(infoKey, campos[1]);
                    }
                    break;
                case "HVD":     // No va
                    break;
                case "VIN":
                    if(campos.length > 1 ) {
                        this._campos.put("vin", campos[1]);
                    }
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
                    if(campos.length > 1 ) {
                        procesaCampoSAT(infoKey, campos[1]);
                    }
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

    public boolean canContaints() {
        boolean ret = false;

        for( HashMap.Entry<String,String> i : this._campos.entrySet()) {
            try {

                if( i.getKey().startsWith("can") ) {
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
    
    protected void procesaCamposCAN(String nombreCampo, String campo) 
    {
        int pos=0, offset=0;
        Integer iVal=0;
        Double dVal=0.0;
        String nCampo = "";
        
        nombreCampo = nombreCampo.toLowerCase();
        
        short[] data = new short[campo.length() / 2];

        for( int i=0, j=0; i < campo.length(); i+=2, j++) {
            data[j] = (Short.parseShort(campo.substring(i, i+2), 16));
        }

        this._campos.put("can00FECA-DTC", "");

        while(pos < data.length ) {
            int len = data[pos];
            
            pos++;
            
            String strKey = String.format("%02X", data[pos]) +
                                    String.format("%02X", data[pos+1]) +
                                    String.format("%02X", data[pos+2]);
            
            pos+=3;
            len-=3;
            
            offset=pos;
            
            switch( strKey ) {
                case "00F004": // Engine speed
                    nCampo = nombreCampo + strKey + "_1";
                    iVal = data[offset] & 0x0F;
                    this._campos.put(nCampo, iVal.toString());
                    
                    nCampo = nombreCampo + strKey + "_2";
                    dVal = ((data[offset] >> 4) & 0x0F)*0.125;
                    this._campos.put(nCampo, dVal.toString());
                    
                    offset++;

                    nCampo = nombreCampo + strKey + "_3";
                    iVal = (int)data[offset];
                    this._campos.put(nCampo, iVal.toString());
                    
                    offset++;
                    nCampo = nombreCampo + strKey + "_4";
                    iVal = (int)data[offset];
                    this._campos.put(nCampo, iVal.toString());
                    
                    offset++;
                    
                    nCampo = nombreCampo + strKey + "_5";
                    iVal = 0;
                    iVal |= data[offset];
                    offset++;
                    iVal |= (data[offset] << 8);
                    dVal = iVal * 0.125;
                    this._campos.put(nCampo, dVal.toString());
                    
                    offset++;
                    
                    nCampo = nombreCampo + strKey + "_6";
                    iVal = (int)data[offset];
                    this._campos.put(nCampo, iVal.toString());
                    
                    offset++;
                    
                    nCampo = nombreCampo + strKey + "_7";
                    iVal = (int)data[offset];
                    this._campos.put(nCampo, iVal.toString());
                    
                    offset++;
                    
                    nCampo = nombreCampo + strKey + "_8";
                    iVal = -125 + data[offset];
                    this._campos.put(nCampo, iVal.toString());
                    break;
                case "00FE6C": // Vehicle speed
                    nCampo = nombreCampo + strKey;
                    iVal = 0;
                    iVal |= data[offset];
                    offset++;
                    iVal |= (data[offset] << 8);
                    
                    dVal = iVal / 256.0;
                    // Km conv
                    this._campos.put(nCampo, dVal.toString());
                    break;
                case "00FEC1": // High Resolution Total Vehicle Distance
                    // Km conv
                    nCampo = nombreCampo + strKey;
                    
                    iVal = 0;
                    iVal |= data[offset];
                    offset++;
                    iVal |= (data[offset] << 8);
                    offset++;
                    iVal |= (data[offset] << 16);
                    offset++;
                    iVal |= (data[offset] << 24);
                    
                    dVal = iVal / 200.0;
                    
                    this._campos.put(nCampo, dVal.toString());
                    break;
                case "00FEE0": // Vehicle Distance
                    nCampo = nombreCampo + strKey + "_1";
                    iVal = 0;
                    iVal = data[offset] | (data[offset+1] << 8) | (data[offset+2] << 16) | (data[offset+3] << 24);
                    
                    dVal = iVal / 8.0;
                    
                    offset+=4;
                    
                    this._campos.put(nCampo, dVal.toString());
                    
                    nCampo = nombreCampo + strKey + "_2";
                    
                    iVal = 0;
                    iVal = data[offset] | (data[offset+1] << 8) | (data[offset+2] << 16) | (data[offset+3] << 24);
                    
                    dVal = iVal / 8.0;
                    
                    this._campos.put(nCampo, dVal.toString());
                    break;
                case "00FEEE": // Engine Coolant Temperature
                    nCampo = nombreCampo + strKey;
                    
                    iVal = (int)data[offset];
                    iVal -= 40;
                    
                    this._campos.put(nCampo, iVal.toString());
                    break;
                case "00FEE5": // Engine Hours, Revolutions
                    nCampo = nombreCampo + strKey + "_1";
                    iVal = 0;
                    iVal = data[offset] | (data[offset+1] << 8) | (data[offset+2] << 16) | (data[offset+3] << 24);
                    
                    dVal = iVal * 0.05;
                    
                    offset+=4;
                    
                    this._campos.put(nCampo, dVal.toString());
                    
                    nCampo = nombreCampo + strKey + "_2";
                    
                    iVal = 0;
                    iVal = data[offset] | (data[offset+1] << 8) | (data[offset+2] << 16) | (data[offset+3] << 24);
                    
                    dVal = iVal * 1000.0;
                    
                    this._campos.put(nCampo, dVal.toString());
                    break;
                case "00FEE8": // Vehicle Direction/Speed
                    nCampo = nombreCampo + strKey + "_1";
                    iVal = 0;
                    iVal = data[offset] | (data[offset+1] << 8);
                    
                    dVal = iVal / 128.00;
                    
                    offset+=2;
                    
                    this._campos.put(nCampo, dVal.toString());
                    
                    nCampo = nombreCampo + strKey + "_2";
                    iVal = 0;
                    iVal = data[offset] | (data[offset+1] << 8);
                    
                    dVal = iVal / 256.00;
                    
                    offset+=2;
                    
                    this._campos.put(nCampo, dVal.toString());
                    
                    nCampo = nombreCampo + strKey + "_3";
                    iVal = 0;
                    iVal = data[offset] | (data[offset+1] << 8) | (data[offset+2] << 16) | (data[offset+3] << 24);
                    
                    dVal = (iVal / 128.00) - 200;
                    
                    this._campos.put(nCampo, dVal.toString());
                    
                    offset+=4;
                    
                    nCampo = nombreCampo + strKey + "_4";
                    iVal = 0;
                    iVal = data[offset] | (data[offset+1] << 8) | (data[offset+2] << 16) | (data[offset+3] << 24);
                    
                    dVal = (iVal / 8.00) - 2500;
                    
                    this._campos.put(nCampo, dVal.toString());
                    break;
                case "00FEE9": // Fuel consumption
                    nCampo = nombreCampo + strKey + "_1";
                    iVal = 0;
                    iVal = data[offset] | (data[offset+1] << 8) | (data[offset+2] << 16) | (data[offset+3] << 24);
                    
                    dVal = (iVal * 0.5);
                    
                    this._campos.put(nCampo, dVal.toString());
                    
                    offset+=4;
                    
                    nCampo = nombreCampo + strKey + "_2";
                    iVal = 0;
                    iVal = data[offset] | (data[offset+1] << 8) | (data[offset+2] << 16) | (data[offset+3] << 24);
                    
                    dVal = (iVal * 0.5);
                    
                    this._campos.put(nCampo, dVal.toString());
                    break;
                case "00F003": // Accelerator Pedal Position  // VER
                    nCampo = nombreCampo + strKey;
                    
                    iVal = data[offset] & 0x0F;
                    dVal = iVal * 0.4;

                    this._campos.put(nCampo, dVal.toString());
                    break;
                case "00FEEB": // Component Identification
                    String sVal = "";
                    
                    for( int i=0; i < len; i++ ) {
                        sVal +=(char)data[pos+i];
                    }
                    
                    String[] ids = sVal.split("\\*");

                    nCampo = nombreCampo + strKey + "_1";
                    if( ids.length > 0 ) { this._campos.put(nCampo, ids[0]); }
                    
                    nCampo = nombreCampo + strKey + "_2";
                    if( ids.length > 1 ) { this._campos.put(nCampo, ids[1]); }

                    nCampo = nombreCampo + strKey + "_3";
                    if( ids.length > 2 ) { this._campos.put(nCampo, ids[2]); }
                    
                    nCampo = nombreCampo + strKey + "_4";
                    if( ids.length > 3 ) { this._campos.put(nCampo, ids[3]); }
                    break;                    
                case "00FEEF": // Engine Fluid Level/Pressure 1
                    nCampo = nombreCampo + strKey + "_1";
                    iVal = (int)data[offset];
                    dVal = iVal * 4.0;
                    this._campos.put(nCampo, dVal.toString());
                    
                    offset++;
                    nCampo = nombreCampo + strKey + "_2";
                    iVal = (int)data[offset];
                    dVal = iVal * 0.05;
                    this._campos.put(nCampo, dVal.toString());
                    
                    offset++;
                    nCampo = nombreCampo + strKey + "_3";
                    iVal = (int)data[offset];
                    dVal = iVal * 0.4;
                    this._campos.put(nCampo, dVal.toString());

                    offset++;
                    nCampo = nombreCampo + strKey + "_4";
                    iVal = (int)data[offset];
                    dVal = iVal * 4.0;
                    this._campos.put(nCampo, dVal.toString());
                    
                    offset++;
                    nCampo = nombreCampo + strKey + "_5";
                    iVal = 0;
                    iVal = data[offset] | (data[offset+1] << 8);
                    dVal = -225.0 + (iVal / 128.0);
                    this._campos.put(nCampo, dVal.toString());

                    offset+=2;
                    nCampo = nombreCampo + strKey + "_6";
                    iVal = (int)data[offset];
                    dVal = iVal * 2.0;
                    this._campos.put(nCampo, dVal.toString());

                    offset++;
                    nCampo = nombreCampo + strKey + "_7";
                    iVal = (int)data[offset];
                    dVal = iVal * 0.4;
                    this._campos.put(nCampo, dVal.toString());
                    break;
                case "00FEF1": // Cruise Control/Vehicle Speed
                    nCampo = nombreCampo + strKey + "_1";
                    iVal = (int)data[offset];
                    iVal &= 0x03;
                    this._campos.put(nCampo, iVal.toString());
                    
                    nCampo = nombreCampo + strKey + "_2";
                    iVal = (int)(data[offset] >> 2);
                    iVal &= 0x03;
                    this._campos.put(nCampo, iVal.toString());
                    
                    nCampo = nombreCampo + strKey + "_3";
                    iVal = (int)(data[offset] >> 4);
                    iVal &= 0x03;
                    this._campos.put(nCampo, iVal.toString());
                    
                    nCampo = nombreCampo + strKey + "_4";
                    iVal = (int)(data[offset] >> 6);
                    iVal &= 0x03;
                    this._campos.put(nCampo, iVal.toString());
                    
                    offset++;
                    nCampo = nombreCampo + strKey + "_5";
                    iVal = 0;
                    iVal = data[offset] | (data[offset+1] << 8);
                    
                    dVal = iVal / 256.00;
                    
                    offset+=2;
                    
                    this._campos.put(nCampo, dVal.toString());

                    nCampo = nombreCampo + strKey + "_6";
                    iVal = (int)data[offset];
                    iVal &= 0x03;
                    this._campos.put(nCampo, iVal.toString());
                    
                    nCampo = nombreCampo + strKey + "_7";
                    iVal = (int)(data[offset] >> 2);
                    iVal &= 0x03;
                    this._campos.put(nCampo, iVal.toString());
                    
                    nCampo = nombreCampo + strKey + "_8";
                    iVal = (int)(data[offset] >> 4);
                    iVal &= 0x03;
                    this._campos.put(nCampo, iVal.toString());
                    
                    nCampo = nombreCampo + strKey + "_9";
                    iVal = (int)(data[offset] >> 6);
                    iVal &= 0x03;
                    this._campos.put(nCampo, iVal.toString());

                    offset++;

                    nCampo = nombreCampo + strKey + "_10";
                    iVal = (int)data[offset];
                    iVal &= 0x03;
                    this._campos.put(nCampo, iVal.toString());
                    
                    nCampo = nombreCampo + strKey + "_11";
                    iVal = (int)(data[offset] >> 2);
                    iVal &= 0x03;
                    this._campos.put(nCampo, iVal.toString());
                    
                    nCampo = nombreCampo + strKey + "_12";
                    iVal = (int)(data[offset] >> 4);
                    iVal &= 0x03;
                    this._campos.put(nCampo, iVal.toString());
                    
                    nCampo = nombreCampo + strKey + "_13";
                    iVal = (int)(data[offset] >> 6);
                    iVal &= 0x03;
                    this._campos.put(nCampo, iVal.toString());
                    
                    nCampo = nombreCampo + strKey + "_14";
                    iVal = (int)data[offset];
                    this._campos.put(nCampo, iVal.toString());

                    nCampo = nombreCampo + strKey + "_15";
                    iVal = (int)data[offset];
                    iVal &= 0x1F;
                    this._campos.put(nCampo, iVal.toString());

                    nCampo = nombreCampo + strKey + "_16";
                    iVal = (int)(data[offset] >> 5);
                    iVal &= 0x07;
                    this._campos.put(nCampo, iVal.toString());
                    
                    offset++;
                    
                    nCampo = nombreCampo + strKey + "_17";
                    iVal = (int)data[offset];
                    iVal &= 0x03;
                    this._campos.put(nCampo, iVal.toString());
                    
                    nCampo = nombreCampo + strKey + "_18";
                    iVal = (int)(data[offset] >> 2);
                    iVal &= 0x03;
                    this._campos.put(nCampo, iVal.toString());
                    
                    nCampo = nombreCampo + strKey + "_19";
                    iVal = (int)(data[offset] >> 4);
                    iVal &= 0x03;
                    this._campos.put(nCampo, iVal.toString());
                    
                    nCampo = nombreCampo + strKey + "_20";
                    iVal = (int)(data[offset] >> 6);
                    iVal &= 0x03;
                    this._campos.put(nCampo, iVal.toString());
                    
                    break;
                case "00FEF6": // Inlet/Exhaust Conditions 1
                    nCampo = nombreCampo + strKey + "_1";
                    iVal = (int)data[offset];
                    dVal = iVal * 0.5;
                    this._campos.put(nCampo, dVal.toString());
                    
                    offset++;
                    nCampo = nombreCampo + strKey + "_2";
                    iVal = (int)data[offset];
                    dVal = iVal * 2.0;
                    this._campos.put(nCampo, dVal.toString());

                    offset++;
                    nCampo = nombreCampo + strKey + "_3";
                    iVal = (int)data[offset];
                    dVal = iVal - 40.0;
                    this._campos.put(nCampo, dVal.toString());
                    
                    offset++;
                    nCampo = nombreCampo + strKey + "_4";
                    iVal = (int)data[offset];
                    dVal = iVal * 2.0;
                    this._campos.put(nCampo, dVal.toString());

                    offset++;
                    nCampo = nombreCampo + strKey + "_5";
                    iVal = (int)data[offset];
                    dVal = iVal * 0.05;
                    this._campos.put(nCampo, dVal.toString());
                    
                    offset++;                    
                    nCampo = nombreCampo + strKey + "_6";
                    iVal = data[offset] | (data[offset+1] << 8);
                    dVal = -273.0 + 0.03125 * iVal;
                    this._campos.put(nCampo, dVal.toString());

                    offset+=2;
                    nCampo = nombreCampo + strKey + "_7";
                    iVal = (int)data[offset];
                    dVal = iVal * 0.5;
                    this._campos.put(nCampo, dVal.toString());
                    break;
                case "00FEF7": // Vehicle Electrical Power 1
                    nCampo = nombreCampo + strKey + "_1";
                    iVal = (int)data[offset];
                    dVal = -125.0 + iVal;
                    this._campos.put(nCampo, dVal.toString());
                    
                    offset++;
                    nCampo = nombreCampo + strKey + "_2";
                    iVal = (int)data[offset];
                    this._campos.put(nCampo, iVal.toString());
                    
                    offset++;
                    nCampo = nombreCampo + strKey + "_3";
                    iVal = data[offset] | (data[offset+1] << 8);
                    dVal = iVal * 0.05;
                    this._campos.put(nCampo, dVal.toString());
                    
                    offset+=2;
                    nCampo = nombreCampo + strKey + "_4";
                    iVal = data[offset] | (data[offset+1] << 8);
                    dVal = iVal * 0.05;
                    this._campos.put(nCampo, dVal.toString());
                    
                    offset+=2;
                    nCampo = nombreCampo + strKey + "_5";
                    iVal = data[offset] | (data[offset+1] << 8);
                    dVal = iVal * 0.05;
                    this._campos.put(nCampo, dVal.toString());
                    break;
                case "00FEFC": // R) Dash Display
                    nCampo = nombreCampo + strKey + "_1";
                    iVal = (int)data[offset];
                    dVal = iVal * 0.4;
                    this._campos.put(nCampo, dVal.toString());
                    
                    offset++;
                    nCampo = nombreCampo + strKey + "_2";
                    iVal = (int)data[offset];
                    dVal = iVal * 0.4;
                    this._campos.put(nCampo, dVal.toString());
                    
                    offset++;
                    nCampo = nombreCampo + strKey + "_3";
                    iVal = (int)data[offset];
                    dVal = iVal * 2.0;
                    this._campos.put(nCampo, dVal.toString());
                    
                    offset++;
                    nCampo = nombreCampo + strKey + "_4";
                    iVal = (int)data[offset];
                    dVal = iVal * 0.5;
                    this._campos.put(nCampo, dVal.toString());
                    
                    offset++;
                    nCampo = nombreCampo + strKey + "_5";
                    iVal = data[offset] | (data[offset+1] << 8);
                    dVal = -273 + 0.03125 * iVal;
                    this._campos.put(nCampo, dVal.toString());
                    
                    offset+=2;
                    nCampo = nombreCampo + strKey + "_6";
                    iVal = (int)data[offset];
                    dVal = iVal * 0.4;
                    this._campos.put(nCampo, dVal.toString());
                    break;
                case "00FECA": // (DM1)Active DTCs and lamp status information
                    //nCampo = nombreCampo + strKey;
                    sVal = DtcsDecode(data, offset, len);
                    this._campos.put("can00FECA-DTC", sVal);
                    break;
                case "00FECC": //(DM2)Previously active DTCs and lamp status information
                    //nCampo = nombreCampo + strKey;
                    sVal = DtcsDecode(data, offset, len);
                    this._campos.put("can00FECA-DTC", sVal);
                    break;
                default:
                    nCampo = nombreCampo + strKey;
                    StringBuilder sb = new StringBuilder();
                    
                    for(int idx=0; idx < len; idx++ ) {
                        sb.append(String.format("%02X", data[offset+idx]));
                    }
                    
                    sVal = sb.toString();
                    
                    this._campos.put(nCampo, sVal);
                    break;
            }
            
            pos += len;
        }
    }
    
    protected String DtcsDecode(short[] data, int offset, int len)
    {
        String result="";
        String sVal = "";
        String[] status = { "OFF", "ON","Unknown", "Unknown" };

        int iVal = data[offset];
        
        sVal = status[(iVal >> 6 ) & 0x03];
        this._campos.put("can00FECA-MIL", sVal);

        sVal = status[(iVal >> 4 ) & 0x03];
        this._campos.put("can00FECA-RSL", sVal);

        sVal = status[(iVal >> 2 ) & 0x03];
        this._campos.put("can00FECA-AWL", sVal);

        sVal = status[(iVal ) & 0x03];
        this._campos.put("can00FECA-PL", sVal);

        for( int i=0; i < (len-2)/4; i++ ) {
//            result += "DTC: " + i;
            
            long lSPN = data[offset+(2 + 4 * (i) + 2)] >> 5;
            lSPN = (lSPN << 8 ) + data[offset+(2 + 4 * i + 1)];
            lSPN = (lSPN << 8 ) + data[offset+(2 + 4 * i)];
            long lFMI = (byte)data[offset+(2 + 4 * i + 2)] & 0x1F;
            long lOC = (byte)data[offset+(2 + 4 * i + 3)] & 0x7F;
            
            result += "SPN: " + lSPN + " ";
            result += "FMI: " + lFMI + " ";
            result += "OC: " + lOC + " ";
        }

        return result;
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
