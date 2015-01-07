/**
 * 
 */
package weka.gui.stpm;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.Vector;

/**Extends the methods of clusterization and CB-SMoT to:
 * -> handle data from traffic aplications;
 * -> save clusters of CB-SMoT as unknown-stops.
 *  
 * @author Gabriel Oliveira
 *
 */
public class NewTrajectoryMethods extends TrajectoryMethods {
	
    /**Join neighbors, inicially limited to one street only, with its adjacent, creating clusters. 
     * 
     * @param array		The trajectory points (already classified with 'stops' labels).
     * @return			The clusters created.
     */
    static Vector<ClusterPoints> unifyAdjacentNeighbors(Vector<GPSPoint> array,long minTimeMili) {
    	Collections.sort(array);
    	int ClusterId = nextId();
    	ClusterPoints cluster=new ClusterPoints();
    	boolean firstMove=true;
    	Vector<ClusterPoints> allClusters=new Vector<ClusterPoints>();
    	
    	for(GPSPoint p:array){
    		if(p.cluster==Cluster.STOP){
    			p.clusterId=ClusterId;
    			cluster.points.add(p);
    			cluster.clusterId=ClusterId;
    			firstMove=true;//prepair to have a move somewhere
    		}
    		else if(firstMove){//re-inicializations
    			if(ClusterId>0) ClusterId=nextId();
    			if(cluster.points.size()>4 && duration(cluster.points)>=minTimeMili){
    				allClusters.add(cluster);//cause a cluster have more than 4 points
    			}
    			firstMove = false;
    			cluster = new ClusterPoints();
    		}	
    	}
    	
    	return allClusters;
	}
    
	/**	Save the clusters created with CB-SMoT for visualization.
	 * 
	 * @param clusters 	Clusters to be saved.
	 * @param config	User configurations.
	 * @param tid		Trajectory tid to be saved.	
	 * 
	 * TODO Buffers here should probably be fixed to buffer meters rather than SRS
	 * But I'm not entirely sure of the author's intention and the code isn't called anyway. 
	 */
	public static void saveClusters(Vector<ClusterPoints> clusters, Config config, int tid){
		Statement s;
		try {
			s = config.conn.createStatement();
		    for(ClusterPoints c:clusters){
		    	String sql= "insert into clusters (tid,cluster_id,the_geom) values" +
		    			"("+tid+","+c.clusterId+",ST_Buffer(ST_LineFromText('LINESTRING(";
		    	for(int i=0;i<c.points.size();i++){
		    		org.postgis.Point p = c.points.elementAt(i).point;
		    		sql+= p.getX() + " " + p.getY() + ",";
		    	}
		    	sql=sql.substring(0,sql.length()-2) + ")',-1),10));";
		    	s.execute(sql);
		    }
			s.close();
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}
	/**Saves the clusters created as unknown-stops.
	 * 
	 * @param buffer		To use or not buffer in the stops.
	 * @param clusters		The clusters to be saved.
	 * @param config		User configurations.
	 * @param minTimeMili	The amount of time to consider the neighborhood a cluster.
	 * @param bufferValue	The buffer value, in meters.
	 * @param srid			The Spatial Reference Identifier.
	 */
	public static void saveStopsClusters(boolean buffer, Vector<ClusterPoints> clusters, Config config, 
			int minTimeMili, double bufferValue, int srid, boolean DB){
		int stopid=-1;
		Vector<Unknown> stops = new Vector<Unknown>(); // the array of stops
		Unknown unk;
		Vector<GPSPoint> array = new Vector<GPSPoint>();		
		
		for(ClusterPoints c:clusters){
			unk = new Unknown(buffer,srid);
			array=c.points;
	        //Collections.sort(array);
	        
	        for(GPSPoint p:array){
	        	unk.pontos.addElement(p);
	        }
	        
	        if(unk.check(minTimeMili)){
	        	stops.add(unk);
	        }
		}//foreach Cluster
	
	    System.out.println("\t\tSaving: "+stops.size()+" stops.");
	    stopid=saveStopsAndMoves2(stops,config.conn,false,bufferValue,++stopid);	    
	}		
}
