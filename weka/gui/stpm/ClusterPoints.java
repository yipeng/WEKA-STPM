/*
 * ClusterPoints.java
 *
 * Created on 27 de Fevereiro de 2008, 15:13
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
public class ClusterPoints {
    public Vector<GPSPoint> points = new Vector<GPSPoint>();
    public int clusterId;
    private int minPoints =2;
    
    /** Creates a new instance of ClusterPoints */
    public ClusterPoints() {
    }
    
    public boolean check(long minTimeMili){
    	if(points.size()>minPoints){//respecting the minimum cluster points	    	
	    	//System.out.println("Stop LT: "+leaveTime.getTime()+" ET: "+enterTime.getTime()+
	    	//		" MT: "+minTime*1000);
	    	if (getDuration() >= minTimeMili){            
	            //System.out.println("\t\tPassed");
	            return true;
	    	}
	        else
	            return false;
    	}
    	else return false;
    }
    
    public String sqlGids() {
        String ret = "";
        for (int i=0;i<points.size();i++) {
            if (points.elementAt(i).cluster == Cluster.STOP)
                ret += "gid = " + points.elementAt(i).gid + " OR ";
        }
        if (ret.length() > 4)
            return (" AND ("+ret.substring(0,ret.length() - 3)+")");
        else
            return (" AND (false)");
        
    }
    
    public long getDuration() {
        return points.lastElement().time.getTime() - points.firstElement().time.getTime();
    }
    
    /*
     * TRANSFORMA O PONTO COM AQUELE GID EM UM PONTO DE NOT-STOP
     */    
    public void resetStop(int gid){
    	for(int i=0;i<points.size();i++){
    		GPSPoint p = points.elementAt(i);
    		if(p.gid==gid){
    			points.elementAt(i).cluster=Cluster.NONE;
    		}
    	}
    }
    
    /*
     * TRANSFORMA O PONTO COM AQUELE GID EM UM PONTO DE STOP
     */
    public void setStop(int gid){
    	for(int i=0;i<points.size();i++){
    		GPSPoint p = points.elementAt(i);
    		if(p.gid==gid){
    			points.elementAt(i).cluster=Cluster.STOP;
    		}
    	}
    }
    
    public Vector<GPSPoint> getSortGid(){
    	Vector<GPSPoint> ret = new Vector<GPSPoint>();
    	
    	for(int i=0;i<points.size();i++){
    		GPSPoint pt=points.elementAt(i);
    		if(ret.size()>0){    			
    			int j=0;    			
    			while(j<ret.size() && pt.gid>ret.elementAt(j).gid){
    				j++;
    			}
    			ret.add(j,pt);
    		}
    		else ret.add(pt); 
    	}
    	return ret;
    }
    
}
