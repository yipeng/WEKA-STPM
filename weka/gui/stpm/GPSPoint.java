/*
 * GPSPoint.java
 *
 * Created on 5 de Julho de 2007, 18:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package weka.gui.stpm;

import java.sql.Timestamp;

import org.postgis.Point;
/**
 *
 * @author Administrador
 */
public class GPSPoint implements Comparable<GPSPoint> {
	public static final int NULL_CLUSTER_ID = -1;
	
    public int tid;
    public int gid; //ATTENTION it is not the gid attribute of the point!! Stores the gid of the rf that intercepts the point
    public Timestamp time;
    public Point point;
    public Cluster cluster;
    public int clusterId;
    public double speed;

    private int timeIndex;
    
    /** Creates a new instance of GPSPoint */
    public GPSPoint() {
    }
    public GPSPoint(int tid,Timestamp time,Point p) {
        this.tid = tid;
        this.time = time;
        this.point = p;
    }
    
    public GPSPoint(int tid,Timestamp time,Point p, int aTimeIndex) {
    	this.tid = tid;
        this.time = time;
        this.point = p;
        this.timeIndex = aTimeIndex;
        this.clusterId = NULL_CLUSTER_ID;
    }
    public int compareTo(GPSPoint o) {
        if (this.tid < o.tid)
            return -1;
        else if (this.tid > o.tid)
            return 1;
        else {
            return time.compareTo(o.time);
        }
    }
    public double distance(GPSPoint p) {
        double dist = Math.pow(point.getX()-p.point.getX(),2) + Math.pow(point.getY()-p.point.getY(),2);
        dist = Math.sqrt(dist);
        return dist;
    }
    
    public int getTimeIndex() {
        return this.timeIndex;
    }

    public void setTimeIndex(int aTimeIndex) {
        this.timeIndex = aTimeIndex;
    }
}
