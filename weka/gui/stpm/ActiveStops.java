/*
 * ActiveStops.java
 *
 * Created on 20 de Fevereiro de 2008, 22:07
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package weka.gui.stpm;

import java.util.*;
import java.sql.Timestamp;
/**
 *
 * @author Administrador
 */
public class ActiveStops {
    protected Hashtable<Integer,Map> stops = new Hashtable<Integer,Map>();
    
    private Vector<Stop> stops_actives = new Vector<Stop>();
    
    /** Creates a new instance of ActiveStops */
    public ActiveStops() {
    }
    
    // For several stops at the same time gerency
    public void addPt(GPSPoint pt, String rf, int minTime,int gid){
		int i=0;
    	while(	i<stops_actives.size()
    			&& stops_actives.elementAt(i).tableName != rf 
				&& stops_actives.elementAt(i).gid != gid)
    		i++;
    	if(i==stops_actives.size()){
    		Stop a = new Stop();
    		a.addPoint(pt, rf, minTime, gid);
    	}
    	else{
    		stops_actives.elementAt(i).addPoint(pt);
    	}	
    }
    
    public Vector<Stop> beginTime() {
        Vector<Stop> closedStops = new Vector<Stop>();
        
        //Fecha os stops que não foram continuos no ultimo instante de tempo
        Enumeration<Integer> enu = stops.keys();
        while (enu.hasMoreElements()) {
            Integer key = enu.nextElement();
            Map map = stops.get(key);
            if (map.added == false) {
                Stop stop = map.stop;
                    stop.leaveTime = stop.pts.lastElement().time;
                closedStops.addElement(stop);
                stops.remove(key);
            }else {
                map.added = false;
            }
        }
        
        return closedStops;
    }
    /***
     * minTime é o tempo minimo de intersecção pra o dado candidate stop
     **/
    public void addPoint(GPSPoint pt, String rf, int minTime) {
        Integer key = new Integer(pt.gid); //pt.gid stores the gid of the rf that intercepts the point
        if (stops.containsKey(key)) {
            Map map = stops.get(key);
            Stop stop = map.stop;
            stop.pts.addElement(pt);
            map.added = true;
        }else {
            Stop stop = new Stop();
                stop.tid = pt.tid;
                stop.enterTime = pt.time;
                stop.leaveTime = null;
                stop.gid = pt.gid;
                stop.tableName = rf;
                stop.minTime = minTime;                
                stop.pts.addElement(pt);
            Map map = new Map();                
                map.stop = stop;
                map.added = true;
            stops.put(key,map);
        }
    }
    
    public void addPoint2(GPSPoint pt, String rf, int minTime,int gid) {
        Integer key = gid; 
        if (stops.containsKey(key)) {
            Map map = stops.get(key);
            Stop stop = map.stop;
            stop.pts.addElement(pt);
            map.added = true;
        }else {
            Stop stop = new Stop();
                stop.tid = pt.tid;
                stop.enterTime = pt.time;
                stop.leaveTime = null;
                stop.gid = gid;
                stop.tableName = rf;
                stop.minTime = minTime;                
                stop.pts.addElement(pt);
            Map map = new Map();                
                map.stop = stop;
                map.added = true;
            stops.put(key,map);
        }
    }
    
    public static void main(String[] args) {
        Hashtable<Integer,String> hash = new Hashtable<Integer,String>();
        Integer key1 = new Integer(5);
        Integer key2 = new Integer(5);
        hash.put(key1,"Zecão");
        System.out.println("Contain key:"+hash.containsKey(key2));
        System.out.println("Valor: "+hash.get(key2));
    }
    
    class Map {
        Stop stop;
        boolean added = false;
    }
}
