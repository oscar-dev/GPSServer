/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gpsserver;

import java.util.Date;

/**
 *
 * @author omedi
 */
public class Util {
    public static String Date2String( Date date ) {
        return date.toString();
    }
    
    public static String Double2String(double dValue) {
        return Double.toString(dValue);
    }
}
