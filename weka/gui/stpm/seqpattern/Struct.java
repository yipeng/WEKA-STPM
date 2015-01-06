/*
 * Struct.java
 *
 * Created on 20 de Julho de 2007, 17:39
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package weka.gui.stpm.seqpattern;

import java.sql.Timestamp;
/**
 *
 * @author Administrador
 */
public class Struct {
    public int fromHours,fromMin,toHours,toMin;
    
    public Struct(int fh, int fm, int th, int tm) {
        fromHours = fh;fromMin = fm;toHours = th;toMin = tm;
    }
    public Struct() {
    }
    
    public boolean overlaps(Struct s) {
        int initTime = fromHours * 60 + fromMin;
        int endTime = toHours * 60 + toMin;
        int sInitTime = s.fromHours * 60 + s.fromMin;
        int sEndTime = s.toHours * 60 + s.toMin;
        
        if (initTime >= sInitTime && initTime <= sEndTime)
            return true;
        else if (endTime >= sInitTime && endTime <= sEndTime)
            return true;
        else if (sInitTime >= initTime && sInitTime <= endTime)
            return true;
        else if (sEndTime >= initTime && sEndTime <= endTime)
            return true;
        else
            return false;
    }
    
    public boolean timeIsIn(int h,int min) {
        if ( !((h == fromHours && min >= fromMin) || (h > fromHours)) )
            return false;
        
        if (h == toHours && min <= toMin)
            return true;
        else if (h < toHours)
            return true;
        else
            return false;
    }
    
    public String toString() {
        return "From:   "+fromHours+":"+fromMin+"   To:   "+toHours+":"+toMin;
    }
    public String toString2() {
        return fromHours+":"+fromMin+"-"+toHours+":"+toMin;
    }
}