/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gpsserver;

import java.sql.SQLException;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Oscar Medina
 */
public class EvtMngParam extends EventManager {
    @Override
    public void checkEvent(Message message) throws SQLException, AddressException, MessagingException {
        
        boolean condicion = false;
        loadMessage(message);
        
        JSONArray ajson = new JSONArray(this._checked_value);
        
        for( int idx=0; idx < ajson.length(); idx++ ){
            JSONObject obj = ajson.getJSONObject(idx);
            
            if(message._messageFields._campos.containsKey(obj.getString("src"))) {
                double val = Double.parseDouble(obj.getString("val"));
                double vparam = Double.parseDouble(message._messageFields._campos.get(obj.getString("src")));
                
                if( obj.getString("src").trim().equals("speed")) {
                    vparam /= 1000;  // metros a kilometros.
                }
                
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
            }
        }
        
        int status_event = get_status();

        if( condicion ) {
            if( status_event == -1 ) {
                set_status(1);

                insertEvent();
            }
        } else {
            if( status_event != -1 ) {
                set_status(-1);
            }
        }
        
    }
}
