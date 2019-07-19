/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gpsserver;

import java.sql.SQLException;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

/**
 *
 * @author omedi
 */
public class EvtMngUnderspeed  extends EventManager{
    @Override
    public void checkEvent(Message message) throws SQLException, AddressException, MessagingException {
        
        int event_status = get_status();
        int speed = Integer.parseInt(this._checked_value);
        
        if( speed > message.getSpeed()) {
            
            if( event_status == -1 ) {
                set_status(1);
                
                loadMessage(message);
                
                insertEvent();
            }
        } else {
            if( event_status != -1 ) {
                set_status(-1);
            }
        }
    }
}
