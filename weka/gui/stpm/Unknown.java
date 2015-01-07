/*
 * Unknown.java
 *
 * Created on 5 de Setembro de 2007, 16:38
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package weka.gui.stpm;

//import org.postgis.inner.Point;

import java.util.Collections;
import java.util.Vector;
import java.sql.*;

/**
 *
 * @author Administrador
 */
public class Unknown {
    public Vector<GPSPoint> pontos = new Vector<GPSPoint>();
    public Timestamp enterTime,leaveTime;
    public int pk;
    public String tableName;
    private boolean isbuffer;//if the user want or not to use buffer. Default is always use.
    private int SRID;
    
    /** Creates a new instance of Unknown */
    public Unknown() {
    	this.isbuffer=true;
    	this.SRID=-1;
    }
    
    public Unknown(boolean buffer,int s) {
    	this.isbuffer=buffer;
    	this.SRID=s;
    }
    
    public boolean check(long minTimeMilis) {
        this.organizedPoints();
    	if (pontos.size() >= 2) {//minimum four points to bem an unknown
            enterTime = pontos.firstElement().time;
            leaveTime = pontos.lastElement().time;
            //System.out.println("tempo do unknown: "+(leaveTime.getTime() - enterTime.getTime())+" com MT= "+minTimeMilis);            
            if (leaveTime.getTime() - enterTime.getTime() >= minTimeMilis)
                return true;
            else
                return false;
        } else
            return false;
    }
    
    public String saveUnknown(int clusterId,int buffer) {
        int tid = pontos.firstElement().tid;
        Timestamp init = pontos.firstElement().time;
        Timestamp end = pontos.lastElement().time;
        String sql = "INSERT INTO unknowns (tid,cluster_id,start_time,end_time,the_geom) VALUES ("+tid+","+clusterId+",'"+init.toString()+"','"+end.toString()+"',"+
                    toSQL(buffer)+")";
        System.out.println(sql);
        return sql;
    }
    
    public String toSQL(double buffer) {
    	// respecting the minimum of 4 poins
        if (pontos.size() < 4) return "null";
        
        String ret = "ST_LineFromText('LINESTRING(";
        for (int i=0;i<pontos.size();i++) {
            ret += pontos.elementAt(i).point.getX() + " " + pontos.elementAt(i).point.getY() + ",";
        }
        ret = ret.substring(0,ret.length()-2) + ")',"+SRID+")";
        
        if(this.isbuffer){
        	ret = "ST_Multi(ST_Buffer("+ret+"::geography,"+buffer+")::geometry)";
        }
        //ret += "ST_Multi("+ret+")";
        return ret;
    }
    
    public double avgSpeed() {
        double sum = 0;
        GPSPoint p2,p1 = pontos.elementAt(0);
        Timestamp ini = p1.time;
        for (int i=1;i<pontos.size();i++) {
            p2 = pontos.elementAt(i);
            sum += p1.distance(p2);
            p1 = p2;
        }
        Timestamp fim = pontos.lastElement().time;
        long tempo = fim.getTime() - ini.getTime();
        return sum/(tempo/1000);
    }   
    
    public double avg(boolean DB){
    	double ret;    	
    	ret = avgSpeed();
    	return ret;
    }
    
    public void organizedPoints(){
    	Vector<GPSPoint> array = pontos;
    	Collections.sort(array);
    	pontos = array;
    }
}
