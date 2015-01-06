/*
 * Move.java
 *
 * Created on 17 de Julho de 2007, 17:19
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package weka.gui.stpm;

import java.util.Vector;

/**
 *
 * @author Administrador
 */
public class Move {
    public int tid;
    public Stop startStop,endStop;
    public String tableName;
    public java.sql.Timestamp startTime,endTime;
    public Vector<org.postgis.Point> points = new Vector<org.postgis.Point>();
    public Vector<GPSPoint> pts = new Vector<GPSPoint>();
    
    public Move(int tid,Stop start,Stop end) {
        this.tid = tid;
        startStop = start;
        endStop = end;
    }
    public Move() {}
    
    private void populate() {
        for (int i=0;i<pts.size();i++) {
            points.addElement(pts.elementAt(i).point);
        }
    }
    
    public String toSQL() {
        if (points.size() == 0) populate();
        
        if (points.size() == 1) return "null";
        
        StringBuffer buffer = new StringBuffer("GeometryFromText('LINESTRING(");
        for (int i=0;i<points.size();i++) {
            org.postgis.Point p = points.elementAt(i);
            buffer.append(p.getX()+" "+p.getY()+",");
        }
        buffer.deleteCharAt(buffer.length()-1);
        buffer.append(")',-1)");
        return buffer.toString();
    }
}
