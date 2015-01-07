/*
 * Stop.java
 *
 * Created on 17 de Julho de 2007, 17:11
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package weka.gui.stpm;

import java.sql.*;
import java.util.Vector;
/**
 *
 * @author Administrador
 */
public class Stop {
    public int tid;
    public int pk;
    public Timestamp enterTime,leaveTime;
    public int gid;
    public int minTime = 0; //em milisegundos
    public String tableName;
    public Vector<org.postgis.Point> points = new Vector<org.postgis.Point>(); //stop last point
    public Vector<GPSPoint> pts = new Vector<GPSPoint>();
    private int SRID;
    private boolean isbuffer;
    
    /** Creates a new instance of Stop */
    public Stop(int tid,Timestamp enter,Timestamp leave,int gid,String table) {
        this.tid = tid;
        enterTime = enter;
        leaveTime = leave;
        this.gid = gid;
        tableName = table;
        this.isbuffer=true;
        this.SRID = -1;
        
    }
    public Stop(){//initializes those variables
    	this.gid=0;
    	tableName="";
    	this.isbuffer=true;
        this.SRID = -1;
    }
    
    public Stop(boolean iB,int sr){
    	this.gid=0;
    	this.tableName="";
    	this.SRID = sr;
    	this.isbuffer= iB;
    }
    
    public String toSQL(double buffer) {
    	// respecting the minimum of 4 poins
        if (pts.size() < 2) return "null";
        
        String ret = "ST_LineFromText('LINESTRING(";
        for (int i=0;i<pts.size();i++) {
            ret += pts.elementAt(i).point.getX() + " " + pts.elementAt(i).point.getY() + ",";
        }
        ret = ret.substring(0,ret.length()-2) + ")',"+SRID+")";
        
        if(this.isbuffer){
        	ret = "ST_Multi(ST_Buffer("+ret+"::geography,"+buffer+")::geometry)";
        }
        return ret;
    }
    
    public double avgSpeed() {
        double sum = 0;
        GPSPoint p2,p1 = pts.elementAt(0);
        Timestamp ini = p1.time;
        for (int i=1;i<pts.size();i++) {
            p2 = pts.elementAt(i);
            sum += p1.distance(p2);
            p1 = p2;
        }
        Timestamp fim = pts.lastElement().time;
        long tempo = fim.getTime() - ini.getTime();
        return sum/(tempo/1000);
    }
    
    public double avg(boolean any){
    	double ret;
    	boolean a = false;
    	if(!a){
    		ret = avgSpeed();
    	}
    	else{
    		ret = 0;//another ways to calc avg ?
    	}
    	return ret;
    }
    
    public void addPoint(GPSPoint pt, String rf, int minTime,int gid){
    	this.tid = pt.tid;
        this.enterTime = pt.time;
        this.leaveTime = null;
        this.gid = gid;
        this.tableName = rf;
        this.minTime = minTime;                
        this.pts.addElement(pt);
    }
    
    public void addPoint(GPSPoint pt){
    	this.pts.addElement(pt);
    }
    
    public boolean check(){
    	if(pts.size()>=2){//respecting the minimum stop points
	    	leaveTime= pts.lastElement().time;
	    	//System.out.println("");
	    	//System.out.println(leaveTime);
	    	//System.out.println("gid = "+pts.lastElement().gid);
	    	//System.out.println("Stop LT: "+leaveTime.getTime()+" ET: "+enterTime.getTime()+
	    	//		" MT: "+minTime*1000);
	    	if (leaveTime.getTime() - enterTime.getTime() >= (minTime*1000)){            
	            //System.out.println("\t\tPassed");
	            return true;
	    	}
	        else
	            return false;
    	}
    	else return false;
    }
    
}