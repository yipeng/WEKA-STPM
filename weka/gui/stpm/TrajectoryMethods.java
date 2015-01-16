/*
 * TrajectoryMethods.java
 *
 * Created on 6 de Julho de 2007, 19:14
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package weka.gui.stpm;

import java.util.*;
import java.sql.*;
/**
 *
 * @author Administrador
 */
public class TrajectoryMethods {
    protected static int clusterID = -1;    
    protected static int unkID=-1;
    
    // Don't try to understand why is it here... 
    public void speedClusteringNotStatic(Trajectory t, double avgFactor, long minTimeMili, double SLFactor,Config conf) {
    	speedClustering(t,avgFactor,minTimeMili,SLFactor,conf);
    }
    
    private static Vector<GPSPoint> getPointsSortedBySpeed(Vector<GPSPoint> points) {
        Vector<GPSPoint> pointsCopy = new Vector<GPSPoint>();
        for (GPSPoint p: points) {
            pointsCopy.add(p);
        }

        Collections.sort(pointsCopy, new Comparator<GPSPoint>() {
                                            public int compare(GPSPoint p1, GPSPoint p2) {
                                                double s1 = p1.speed;
                                                double s2 = p2.speed;
                                                return s1 < s2 ? -1 : (s1 > s2 ? +1 : 0);
                                            }
        });
        return pointsCopy;
    }
    
    private static void slowestNeighborhood(Vector<GPSPoint> points, SetOfPoints seeds,
    		double speedLimit, long minTimeMilliseconds) {
        GPSPoint leftPoint;
        GPSPoint rightPoint;
        do {
            try {
                leftPoint = points.get(seeds.getFirstPointTimeIndex()-1);
            } catch (IndexOutOfBoundsException eLeft) {
                try {
                    rightPoint = points.get(seeds.getLastPointTimeIndex()+1);
                } catch (IndexOutOfBoundsException eRight) {
                    break;
                }
                if (rightPoint.clusterId == GPSPoint.NULL_CLUSTER_ID
                        && rightPoint.speed <= speedLimit) {
                    seeds.addToEnd(rightPoint);
                    continue;
                } else {
                    break;
                }
            }
            try {
                rightPoint = points.get(seeds.getLastPointTimeIndex()+1);
            } catch (IndexOutOfBoundsException eRight) {
                if (leftPoint.clusterId == GPSPoint.NULL_CLUSTER_ID
                        && leftPoint.speed <= speedLimit) {
                    seeds.addToBegin(leftPoint);
                    continue;
                } else {
                    break;
                }
            }
            if ((leftPoint.speed <= rightPoint.speed)
                    && leftPoint.clusterId == GPSPoint.NULL_CLUSTER_ID
                    && leftPoint.speed <= speedLimit) {
                seeds.addToBegin(leftPoint);
            } else if (rightPoint.clusterId == GPSPoint.NULL_CLUSTER_ID
                    && rightPoint.speed <= speedLimit) {
                seeds.addToEnd(rightPoint);
            } else {
                break;
            }
        } while (seeds.duration() < minTimeMilliseconds);
    }
    
    private static GPSPoint addSlowerNeighborToSeeds(Vector<GPSPoint> points,
    		SetOfPoints seeds) {
        GPSPoint leftPoint;
        GPSPoint rightPoint;
        try {
            leftPoint = points.get(seeds.getFirstPointTimeIndex()-1);
        } catch (IndexOutOfBoundsException eLeft) {
            leftPoint = null;
        }
        try {
            rightPoint = points.get(seeds.getLastPointTimeIndex()+1);
        } catch (IndexOutOfBoundsException eRight) {
            if (leftPoint != null) {
                seeds.addToBegin(leftPoint);
            }
            return leftPoint;
        }
        if ((leftPoint != null) && (leftPoint.speed <= rightPoint.speed)) {     // always add the slower neighbour
            seeds.addToBegin(leftPoint);
            return leftPoint;
        } else {
            seeds.addToEnd(rightPoint);
            return rightPoint;
        }
    }
    
    private static boolean limitedNeighborhood(Vector<GPSPoint> points, GPSPoint point,
    		int clusterId, double avgSpeed, long minTimeMilliseconds, double speedLimit) {
        if (point.speed > speedLimit) {
            return false;
        }
        SetOfPoints seeds = new SetOfPoints();
        seeds.addToEnd(point);
        slowestNeighborhood(points, seeds, speedLimit, minTimeMilliseconds);
        double meanSpeed = seeds.meanSpeed();
        if (meanSpeed > avgSpeed || seeds.duration() < minTimeMilliseconds) { // cont if mean speed in slowest neighborhood is smaller than global mean speed
            return false;
        } else {
            seeds.setClusterId(clusterId);
            while (true) {
                GPSPoint addedPoint = addSlowerNeighborToSeeds(points, seeds);
                if (addedPoint != null) {
                    double newMeanSpeed = seeds.meanSpeed();
                    if (newMeanSpeed <= avgSpeed && addedPoint.speed <= speedLimit) { // neighborhood speed <= 0.9*global mean, point speed <= 1.1*global mean
                        if (addedPoint.clusterId == GPSPoint.NULL_CLUSTER_ID) {
                            addedPoint.clusterId = clusterId;
                        } else {
                            break;
                        }
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            }
        }
        return true;
    }
    
    private static void unifyAdjacentClusters(Vector<GPSPoint> points) {
        if (points.size() < 2) {
            return;
        }
        
        int i = 1;
        while (i < points.size()) {
            GPSPoint p = points.get(i); // gets the second point ordered by time
            int lastClusterId = points.get(p.getTimeIndex()-1).clusterId;
            if (p.clusterId != GPSPoint.NULL_CLUSTER_ID
                    && lastClusterId != GPSPoint.NULL_CLUSTER_ID) {
                if (p.clusterId != lastClusterId) {
                    int newClusterId = Math.min(lastClusterId, p.clusterId);
                    p.clusterId = newClusterId;
                }
            }
            i++;
        }
    }
    
    private static Vector<ClusterPoints> createClusters(Vector<GPSPoint> points) {
        Vector<GPSPoint> clusterPoints = new Vector<GPSPoint>();
        for (GPSPoint p: points) {
            if (p.clusterId != GPSPoint.NULL_CLUSTER_ID) {
                clusterPoints.add(p);
            }
        }

        Vector<ClusterPoints> clustrs = new Vector<ClusterPoints>();
        ClusterPoints cluster = new ClusterPoints();

        if (clusterPoints.size() > 0) {
        	GPSPoint p = clusterPoints.get(0);
            cluster.points.add(p);
            int lastClusterId = p.clusterId;
            cluster.clusterId = lastClusterId;
            int i = 1;
            while (i < clusterPoints.size()) {
                p = clusterPoints.get(i);
                if (p.clusterId != lastClusterId) {
                    clustrs.add(cluster);
                    cluster = new ClusterPoints();
                    lastClusterId = p.clusterId;
                    cluster.clusterId = lastClusterId;
                }
                cluster.points.add(p);
                i++;
            }
            clustrs.add(cluster); // adiciona o ultimo cluster
        }
        
        return clustrs;
    }
    
    public static Vector<ClusterPoints> speedClustering(Trajectory t,
    		double avgFactor, long minTimeMilli, double speedLimitFactor) {
    	Vector<GPSPoint> points = t.points;
    	double avgSpeedOfTrajectory = t.meanSpeed();
    	double avgSpeed = avgSpeedOfTrajectory * avgFactor; //0.9 and 1.1 are pretty generic
    	double speedLimit = avgSpeedOfTrajectory * speedLimitFactor;
    	
    	int clusterId = 1;
        Vector<GPSPoint> pointsSorted = getPointsSortedBySpeed(points);
        for (GPSPoint point: pointsSorted) { // for each point, consider a limited neighborhood of points
            if (point.clusterId == GPSPoint.NULL_CLUSTER_ID) { // point is unprocessed
                if (limitedNeighborhood(points, point, clusterId, avgSpeed,
                		minTimeMilli, speedLimit)) {
                    clusterId++;
                }
            }
        }
        
        unifyAdjacentClusters(points);
        return createClusters(points);
     
    }
    
    /**Main method for clustering trajectory points.
     * 
     * @param t 			The Trajectory.
     * @param avgFactor		Avg parameter.
     * @param minTimeMili	Mintime parameter, in miliseconds.
     * @param SLFactor		Superior Limit parameter.
     * @param conf			User configurations. 
     */    
    public static Vector<ClusterPoints> speedClustering(Trajectory t, double avgFactor, long minTimeMili, double SLFactor,Config conf) {
        
    	clusterID = -1;
    	//load the trajectory and reset clusterID
        Vector<GPSPoint> array = t.points;
        reset(array);
        
        Collections.sort(array,new SpeedComparator());
        GPSPoint[] objs = new GPSPoint[array.size()];
        array.toArray(objs);
        Collections.sort(array);
        
        //seta as variaveis de controle...
        double SL = t.meanSpeed(); // SL=mean speed of trajectory;
        System.out.println("mean speed of trajectory = "+SL);
        double avg = avgFactor * SL;// avg = percentage of mean speed to be considered 
        SL = SLFactor * SL;// SL = max accepted speed (as an percentage of the mean speed of the trajectory) 
                
        int ClusterId = nextId();
        //for each point c (GPSPoint) in array 'objs',organized by time
        System.out.println("Starting clusterization...");
        for (GPSPoint c:objs) {
            //System.out.println("Speed "+c.speed+" for gid "+c.gid);
        	if (c.speed > avg)
                break;
            // if the point have no cluster yet...
            if (c.cluster == Cluster.NONE && c.speed <= SL) {
            	//try to generate a cluster with it and att the ClusterID if it works
            	if(limitedNeighborhood(array,c,ClusterId,avg,minTimeMili,SL)) {
                    ClusterId = nextId();
                    System.out.println();
                }
            }
        }
        
        //the modified points are organized by time again
        Collections.sort(array);        
        Vector<ClusterPoints> clusters = NewTrajectoryMethods.unifyAdjacentNeighbors(array,minTimeMili);
        
        return clusters;
    }    
    /** Try to create a maximal cluster with the point received.
     * 
     *@param array  		list organized by time 
     *@param point  		point received
     *@param cId    		Identificator of the cluster to be created
     *@param avg			Maximal meanspead to be reached 	  
     *@param minTimeMili	Maximal time to be reached
     *@param SL				Superior Limit of the speed of one point,based in the mean-speed
     *  
     *@return 				'true' if cluster was created, or 'false' if not  
     */
    /*
    private static boolean limitedNeighborhood(Vector<GPSPoint> array,GPSPoint point,int cId,double avg,long minTimeMili,double SL) {
        //iniciate the cluster creation...
    	Vector<GPSPoint> seeds = new Vector<GPSPoint>();
        //prevents against epty-clusters
    	seeds.add(point);
    	//expands the cluster in such a way that its' speed do not pass SL    	
    	slowestNeighborhood(seeds,array,minTimeMili,SL);
        //re-organize by time 
        Collections.sort(seeds);
        
        double newMeanSpeed,meanSpeed = meanSpeed(seeds);
        long duration = duration(seeds);
        
        // If mean speed over-pass the limit mean speed 
        // OR the duration do not exceed minimal time (eg. it passes so little time in slow speed)
        if (meanSpeed > avg || duration < minTimeMili) {
            point.cluster = Cluster.MOVE;
            return false;
        }
        // In case of mean speed <= limit mean speed 
        //AND cluster duration exceed the minimum time
        // it's called, for now on, a CORE-NEIGHBORHOOD
        else {
        	// marks the points in trajectory as STOP Points and give each of them an ID
        	for (int i=0;i<seeds.size();i++) {
                seeds.get(i).cluster = Cluster.STOP;
                seeds.get(i).clusterId = cId;
            }            
        	// try to add more slow points, neighbors in the trajectory, in such way we have an STOP with the
        	// max number of slow points  as possible.
        	GPSPoint lastPoint;
        	while (true) {
            	//gets an neighbor point
                lastPoint = slowerNeighbor(seeds,array);
                if (lastPoint != null)
                    seeds.add(lastPoint);
                else
                    break;
                //organize by time
                Collections.sort(seeds);

                //recalculate variables of control	
                newMeanSpeed = meanSpeed(seeds);
                duration = duration(seeds);
                
                // tests to check that controls' variables
                // newMeanSpeed <= avg ::::> with the point added, the speed still slower than the minimal ?
                // newMeanSpeed <= meanSpeed :::::> the new mean speed is slower than the old one ?
                // lastPoint.speed <= SL ::::> the point speed do not pass the threshold ?
                if (newMeanSpeed <= avg && (newMeanSpeed <= meanSpeed || lastPoint.speed <= SL)) {
                    if (lastPoint.cluster != Cluster.STOP) {
                        lastPoint.cluster = Cluster.STOP;
                        lastPoint.clusterId = cId;
                    }else
                        break;
                }else
                    break;
                //for the next iteration, att the mean speed
                meanSpeed = newMeanSpeed;
            }
            return true;
        }        
    }
    */
    /**Fills the cluster with such points that its' time do not exceed the limite minTime. 
     *  
     *@param  cluster 		the cluster to be filled, wich should not be empty.
     *@param  array   		the GPSPoints organized by speed, to identify the slower. 
     *@param  minTimeMili 	the amount of time to consider the neighborhood a cluster
     */        
    private static void slowestNeighborhood(Vector<GPSPoint> cluster,Vector<GPSPoint> array,long minTimeMili,double SL) {
        long duration=0;
        int flag=0;//flag to avoid infinites loops...
        // organize the cluster by Time
        Collections.sort(cluster);

        do {
            //sn = the slower point in the neighbor.
        	GPSPoint sn = slowerNeighbor(cluster,array);
        	// if it's valid (not null), add to cluster
            if (sn != null && sn.speed <= SL){
            	cluster.add(sn);
            }
            else break;
            // re-organize by time
            Collections.sort(cluster);
            //calculates trajectory duration and...
            if(duration!=duration(cluster)){//...if it changes, that's ok, save it and down flag
            duration = duration(cluster);
            flag=0;
            }
            else flag++;//... if didn't change, up flag
        
            if(flag>=10) break;// if the duration didn't change in 10 slower points, stop lhe loop
        
        //keep calculating the 'neighbohood until the trajectory reach the threshold    
        }while (duration < minTimeMili);
    }
    /** It's the slower point neighbor (adjacent, on the trajectory) to the cluster received. 
     * 
     * @param cluster cluster to be analyzed.
     * @param array   list os GPSPoints organized by speed.
     * 
     * @return GPSPoint p1 or p2 or null
     */
    private static GPSPoint slowerNeighbor(Vector<GPSPoint> cluster,Vector<GPSPoint> array) {
            ///	MinIndex = index in speed array from the FIRST cluster point 
    	    int MinIndex = array.indexOf(cluster.get(0));
    	    // MaxIndex = index in speed array from the LAST cluster point
            int MaxIndex = array.indexOf(cluster.get(cluster.size()-1));
            /**
    			p1 = index in speed array from the point before the first point of the cluster
    			p2 = index in speed array from the next point after the last point of the cluster
    			
    			Such a way that neither p1 and p2 are in the cluster.
             */
            GPSPoint p1 = MinIndex > 0 ? array.get(MinIndex - 1) : null;
            GPSPoint p2 = MaxIndex < array.size()-1 ? array.get(MaxIndex+1) : null;            
            GPSPoint candidate1 = null,candidate2 = null;
            
            //take decision such as the slower of p1 and p2 are returned, or null if there aren't any
            if (p1 != null && p2 != null) {
                if (p1.speed <= p2.speed) {
                    candidate1 = p1;candidate2 = p2;
                }else {
                    candidate1 = p2;candidate2 = p1;
                }
            }else if (p1 != null)
                candidate1 = p1;
            else if (p2 != null)
                candidate1 = p2;
            else 
                return null;
            
            if (candidate1.cluster != Cluster.STOP) {
                return candidate1;
            }else if (candidate2 != null && candidate2.cluster != Cluster.STOP) {
                return candidate2;
            }else
                return null;
    }
    /**Unify clusters within the max distance between points.
     * 
     * @param array			The trajectorys' points.
     * @param maxDist		The max distance between two consecutives points. Use 10meters por 60 km/h roads, ou 30m for 100 Km/h roads (averages) 
     * @param minTimeMili	The min time parameter of the method, to check if it's a cluster or not.
     */
    private static void cleanClusters(Vector<GPSPoint> array, double maxDist, long minTimeMili) {
        int clusterId = 0;
        int first,last,j;
        
        for (int i =0;i<array.size();) {
            GPSPoint p = array.elementAt(i);
            if (p.cluster == Cluster.STOP) {
                first = i;
                
                j = i+1;
                GPSPoint p2 = array.elementAt(j);
                while(p2.cluster == Cluster.STOP && (p.distance(p2) <= maxDist || maxDist == 0) ) {
                    j++;
                    p = p2;
                    if (j < array.size())
                        p2 = array.elementAt(j);
                    else
                        break;
                }
                i = j;
                last = j-1;
                
                GPSPoint l = array.elementAt(last);
                GPSPoint f = array.elementAt(first);
                long time = l.time.getTime() - f.time.getTime();
                if (time >= minTimeMili) {
                    for (int k=first;k<=last;k++) {
                        array.elementAt(k).clusterId = clusterId;
                    }
                    clusterId++;
                }else {
                    for (int k=first;k<=last;k++) {
                        array.elementAt(k).cluster = Cluster.MOVE;
                    }                    
                }
            }else {
                i++;
            }
        }
        
        //Numerates the Moves
        int moveId = -1;
        boolean firstMove = true;
        for (int i =0;i<array.size();i++) {
            GPSPoint p = array.elementAt(i);
            if (p.cluster == Cluster.MOVE && firstMove) {
                moveId++;
                firstMove = false;
            }
            
            if (p.cluster == Cluster.MOVE) {
                p.clusterId = moveId;
            }else {
                firstMove = true;
            }
        }
    }
    
    
    /** Calculates the diference (variation), with respect of time, of the trajectory.
     * 
     * @param array list organized by time.
     * 
     * @return duration (aka, number) of the trajectory.
     */
    protected static long duration(Vector<GPSPoint> array) {
        return array.lastElement().time.getTime() - array.firstElement().time.getTime();
    }
    
    /** "Calculates the average speed of the respective trajectory time interval."
     * 
     * @param array list organized by time.
     * 
     * @return average speed of the trajectory.
     */
    protected static double meanSpeed(Vector<GPSPoint> array) {
        double totalDist = 0.0;
        GPSPoint p2,p1 = array.elementAt(0);
        for (int i=1;i<array.size();i++) {
            p2 = array.elementAt(i);
            totalDist += p1.distance(p2);
            p1 = p2;
        }        
        //System.out.println("Distance (?): "+totalDist+"\nDuration: "+duration(array));
        return (totalDist / ((array.lastElement().time.getTime()-array.firstElement().time.getTime())/1000));        
    }
    
    /** Control and return the clusterID variable.
     * 
     * @return new cluster id
     */
    protected static int nextId() {
            return ++clusterID;
    }
    /** Control and return the unknownID variable.
     * 
     * @return new unknow ID
     */
    protected static int nextUnknown(){
    		return ++unkID;
    }
    /**Reset the unknown value between executions of the CB-SMoT.
     */
    public static void resetunknown() {
			unkID=-1;
	}

	/** Finds the stops in a trajectory.
	 * 
	 * @param bufferChecked		If the user set a buffer or not.
	 * @param bufferValue		The value of buffer.
	 * @param config			User configurations.
	 * @param traj				The trajectory being analized.
	 * @param targetFeature		The name of the trajectory table being analyzed.
	 * @param relevantFeatures	The RelevantFeatures (AssociatedParameter) array used.
	 * @param featureType		Defines to pre-process in Type (true) or Instance (false) granularity.
	 * @throws SQLException		If any table didn't exist in the BD, or a field.
	 */    
    public static void smot(boolean bufferChecked, int bufferValue, Config config,Trajectory traj, String targetFeature, AssociatedParameter[] relevantFeatures, boolean featureType) throws SQLException {
        org.postgis.PGgeometry geom;
        String campos = config.tid + "," + config.time;
        
        for (AssociatedParameter rf: relevantFeatures) {
            Runtime.getRuntime().gc();
            Statement s = config.conn.createStatement();
            
            StringBuffer sql = new StringBuffer("SELECT * FROM " +
                    "(SELECT "+config.tid+" as tid, "+config.time+" as time,the_geom, gid as serial_time FROM "+config.table+" WHERE "+config.tid+"="+traj.tid+" ORDER BY "+config.time+") T JOIN (" +
                    "SELECT '"+rf.name+"' as table_name,A.gid,the_geom as rf_the_geom,bufenv,buf FROM "+rf.name+" A JOIN "+rf.name+"_envelope B ON (A.gid = B.gid) ) R" +
                    " ON (ST_Intersects(bufenv,T.the_geom) " +
                    "AND ST_Intersects(buf,T.the_geom)) " +
                    "ORDER BY time");            

            System.out.println(sql.toString());
            java.util.Date ini = new java.util.Date();
            ResultSet rs = s.executeQuery(sql.toString());
            java.util.Date fim = new java.util.Date();
            java.util.Date tempo = new java.util.Date(fim.getTime()-ini.getTime());
            System.out.println("Main Query (RF:"+rf+"): " +tempo.getTime()+" ms");

            Vector<Stop> stops = new Vector<Stop>();
            ActiveStops activeStops = new ActiveStops();
            Timestamp time = new Timestamp(0);
            int serialTime=0;
            boolean first = true;
            
            while (rs.next()) {
                if (first) {
                    serialTime = rs.getInt("serial_time");
                    first = false;
                }                
                
                GPSPoint pt = new GPSPoint();
                    pt.tid = rs.getInt("tid");
                    pt.gid = rs.getInt("gid");
                    pt.time = rs.getTimestamp("time");
                    geom = (org.postgis.PGgeometry) rs.getObject("the_geom");
                    pt.point = (org.postgis.Point) geom.getGeometry();
                    
                if (time.getTime() != pt.time.getTime()) {
                    stops.addAll(activeStops.beginTime());
                    
                    if (rs.getInt("serial_time") - serialTime > 1)
                        stops.addAll(activeStops.beginTime());
                }

                if (rs.getObject("gid") != null) {
                    activeStops.addPoint(pt,rf.name,rf.value.intValue());
                }
                
                time = rs.getTimestamp("time");
                serialTime = rs.getInt("serial_time");
            }
            //Forces the stops pending to close.
            stops.addAll(activeStops.beginTime());
            stops.addAll(activeStops.beginTime());
            
            //Tests mean time of each stop
            for (int i=stops.size()-1;i>=0;i--) {
                Stop st = (Stop) stops.elementAt(i);
                if (st.leaveTime.getTime() - st.enterTime.getTime() < (st.minTime*1000)) {
                    stops.removeElementAt(i);
                }
            }
        
            saveStopsAndMoves(stops,config.conn,featureType,bufferValue);
            rs.close();
            s.close();
        }
    }
    
    /**	Creates a string with the time of each stop, to be used ia a query, below.
     * 
     * @param stops		The array of Stop.
     * @param config	User configurations.
     * @return			The string with times in a boolean equation.
     */
    private static String stopsIntervals(Vector<Stop> stops,Config config) {
    	StringBuffer condition = new StringBuffer("    ");
    	
    	for (int i=0;i<stops.size();i++) {
    		Stop st = stops.elementAt(i);
    		String iniTime = "'"+st.enterTime.toString()+"'";
    		String endTime = "'"+st.leaveTime.toString()+"'";
    		condition.append("("+config.time+" >= "+iniTime+" AND "+config.time+" <= "+endTime+") OR ");
    	}
    	
    	String temp = condition.toString();
    	return temp.substring(0,temp.length()-4);
    }
    
    /**	Main method to aply semantics to the clusters.
     * 
	 * @param bufferChecked		If the user set a buffer or not.
	 * @param bufferValue		The value of buffer.
	 * @param traj				The trajectory being analized.
	 * @param config			User configurations.
	 * @param targetFeature		The name of the trajectory table being analyzed.
	 * @param relevantFeatures	The RelevantFeatures (AssociatedParameter) array used.
	 * @param minTimeMilis		The mintime parameter received.
	 * @param featureType		Defines to pre-process in Type (true) or Instance (false) granularity.
	 * @throws SQLException		If any table didn't exist in the BD, or a field.
     */
    public static void stopsDiscovery(boolean bufferChecked, int bufferValue,Trajectory traj, Config config, String targetFeature, AssociatedParameter[] relevantFeatures,
    		long minTimeMilis,boolean featureType) throws SQLException {
    	Statement s = config.conn.createStatement();
    	ResultSet rs2;
    	org.postgis.PGgeometry geom;
        String campos = config.tid + "," + config.time;
        Vector stops = new Vector();
        ActiveStops activeStops = new ActiveStops();
        boolean first;
        int serialTime=0;
        Runtime.getRuntime().gc();
        //foreach item selected in relevantFeaturesList
        for (AssociatedParameter rf: relevantFeatures) {
            /***
             * The table rf_envelope has in its buf attribute the buffer value of the relevant or the the_geom value of the relevan,
             * depends on the choice of the user. See the createTables method at TrajectoryFrame.
             */
            String sql = new String("SELECT * FROM " +
                    "(SELECT "+config.tid+" as tid, "+config.time+" as time, the_geom, gid as serial_time FROM "+config.table+" WHERE "+config.tid+"="+traj.tid+traj.clusterPoints()+") T JOIN (" +
                    //"SELECT '"+rf.name+"' as table_name,A.gid,A.lid,the_geom as rf_the_geom,bufenv,buf FROM "+rf.name+" A JOIN "+rf+"_envelope B ON (A.gid = B.gid) ) R" +
                    "SELECT '"+rf.name+"' as table_name,A.gid,the_geom as rf_the_geom,bufenv,buf FROM "+rf.name+" A JOIN "+rf+"_envelope B ON (A.gid = B.gid) ) R" +
                    " ON (ST_Intersects(bufenv,T.the_geom) " +
                    //"AND T.the_geom && rf_the_geom " +
                    "AND ST_Intersects(buf,T.the_geom)) " +
                    " ORDER BY time");

            System.out.println(sql);
            java.util.Date ini = new java.util.Date();            
            ResultSet rs = s.executeQuery(sql);            
            java.util.Date fim = new java.util.Date();
            java.util.Date tempo = new java.util.Date(fim.getTime()-ini.getTime());
            System.out.println("Main Query (RF:"+rf+"): " +tempo.getTime()+" ms");

            //Vector stops = new Vector();
            //ActiveStops activeStops = new ActiveStops();
            Timestamp time = new Timestamp(0);
            first = true;
            serialTime = 0;
            
            while (rs.next()) {
                if (first) {
                    serialTime = rs.getInt("serial_time");
                    first = false;
                }
                
                GPSPoint pt = new GPSPoint();
                    pt.tid = rs.getInt("tid");
                    
                    pt.gid = rs.getInt("gid"); //Stores the gid of the rf that intercepts the point
                    
                    pt.time = rs.getTimestamp("time");
                    geom = (org.postgis.PGgeometry) rs.getObject("the_geom");
                    pt.point = (org.postgis.Point) geom.getGeometry();
                    
                if (time.getTime() != pt.time.getTime()) {
                    stops.addAll(activeStops.beginTime());
                    if (rs.getInt("serial_time") - serialTime > 1)
                        stops.addAll(activeStops.beginTime());
                }

                if (rs.getObject("gid") != null) {//lid usado no caso de RF ruas, cc usar gid                    
                    activeStops.addPoint(pt,rf.name,rf.value.intValue());                    
                }
                
                time = rs.getTimestamp("time");
                serialTime = rs.getInt("serial_time");
            }
            stops.addAll(activeStops.beginTime());
            stops.addAll(activeStops.beginTime());            
        }//foreach RF     

        	//Kills the stops with times shorter than it's minTimes
            for (int i=stops.size()-1;i>=0;i--) {
                Stop st = (Stop) stops.elementAt(i);
                if (st.leaveTime.getTime() - st.enterTime.getTime() < (st.minTime*1000)) {
                    stops.removeElementAt(i);
                }
            }
       /*** GERA UNKNOWNS ***/
       
            
            Vector<ClusterPoints> clusters = traj.generateClusterPoints(minTimeMilis);
            
            String stopsInts = stopsIntervals((Vector<Stop>)stops,config);
            if (stopsInts.length() > 3)
                stopsInts = " AND NOT ("+stopsInts+")";
            
            for (int i=0;i<clusters.size();i++) {
                ClusterPoints cluster = clusters.elementAt(i);
                
                String sql2 = new String("SELECT "+config.tid+" as tid, "+config.time+" as time, the_geom, gid as serial_time " +
                    "FROM "+config.table+" WHERE "+config.tid+"="+traj.tid+cluster.sqlGids()+stopsInts+
                    " ORDER BY "+config.time);
                
                s = config.conn.createStatement();
                rs2 = s.executeQuery(sql2);
                
                Unknown unk = new Unknown();
                first = true;
                while (rs2.next()) {
                    if (first) {
                        serialTime = rs2.getInt("serial_time");
                        first = false;
                    }                    
                    
                    GPSPoint pt = new GPSPoint();
                        pt.tid = rs2.getInt("tid");
                        pt.time = rs2.getTimestamp("time");
                        geom = (org.postgis.PGgeometry) rs2.getObject("the_geom");
                        pt.point = (org.postgis.Point) geom.getGeometry();
                    
                    if (rs2.getInt("serial_time") - serialTime > 1) {
                        if (unk.check(minTimeMilis)) {
                            stops.addElement(unk);
                        }
                        
                        unk = new Unknown();
                    }
                    unk.pontos.addElement(pt);
                    
                    serialTime = rs2.getInt("serial_time");
                }
                if (unk.check(minTimeMilis)) {
                    stops.addElement(unk);
                }
                
                rs2.close();
                s.close();                            
            }
            /*** FIM UNKNOWNS ***/
            
            saveStopsAndMoves(stops,config.conn,featureType,bufferValue);            
    }    
    
    /*** SaveStops method according documentantion/dissertation */
    private static void saveStopsAndMoves(Vector list, Connection conn, boolean featureType, int buffer) throws SQLException {
        Statement s = conn.createStatement();
        Statement s1 = conn.createStatement();      
        
        boolean flag=false;
        int stopId=0,moveId=0;
        String sql="";
        for (int i=0;i<list.size();i++) {
            Object obj = list.elementAt(i);
            if (obj.getClass() == Stop.class) {
                Stop stop = (Stop) obj;
                String stopName = featureType ? stop.tableName : (stop.gid + "_" + stop.tableName);
                sql = "INSERT INTO "+TrajectoryFrame.getCurrentNameTableStop()+" (tid,stopid,start_time,end_time,stop_gid,stop_name,the_geom,rf,avg_speed) VALUES "+
                    "("+stop.tid+","+stopId+",'"+stop.enterTime.toString()+"','"+stop.leaveTime.toString()+"','"+stop.gid+"','"+stopName+"',"+stop.toSQL(buffer)+",'"+stop.tableName+"',"+stop.avgSpeed()+")";                
                stopId++;
                flag=false;
            }else if (obj.getClass() == Unknown.class) {
                Unknown unk = (Unknown) obj;
                int tid = unk.pontos.firstElement().tid;                                
                if(unk.pontos.size()>=4){//to prevent the_geom null
                	ResultSet rs = s1.executeQuery("select stop_name from "+TrajectoryFrame.getCurrentNameTableStop()+" where rf='unknown' AND st_intersects(the_geom,"+unk.toSQL(buffer)+");");
                	if(rs.next()){
                		sql = "INSERT INTO "+TrajectoryFrame.getCurrentNameTableStop()+" (tid,stopid,start_time,end_time,stop_gid,stop_name,the_geom,rf,avg_speed) VALUES "+
                		"("+tid+","+stopId+",'"+unk.enterTime.toString()+"','"+unk.leaveTime.toString()+"',"+stopId+",'"+rs.getString("stop_name")+"',"+unk.toSQL(buffer)+",'unknown',"+unk.avgSpeed()+")";
                	}
                	else {
                		sql = "INSERT INTO "+TrajectoryFrame.getCurrentNameTableStop()+" (tid,stopid,start_time,end_time,stop_gid,stop_name,the_geom,rf,avg_speed) VALUES "+
                		"("+tid+","+stopId+",'"+unk.enterTime.toString()+"','"+unk.leaveTime.toString()+"',"+stopId+",'"+nextUnknown()+"_unknown',"+unk.toSQL(buffer)+",'unknown',"+unk.avgSpeed()+")";
                	}                    
                	stopId++;
                	flag=false;//execute query, if have the_geom...
                }
                else flag=true;//...or not
            }
            else {// it won't work properly...
                Move move = (Move) obj;
                int start_pk=-1,end_pk=-2;
                String startStop,endStop;
                if ((i-1) >= 0) {
                    startStop = "null";
                    if (list.elementAt(i-1).getClass() == Unknown.class)
                        start_pk = ((Unknown)list.elementAt(i-1)).pk;
                    else
                        start_pk = ((Stop)list.elementAt(i-1)).pk;
                }else {
                    startStop = "'begin'"; start_pk = -1;
                }
                if ((i+1) < list.size()) {
                    endStop = "null";
                    if (list.elementAt(i+1).getClass() == Unknown.class)
                        end_pk = ((Unknown)list.elementAt(i+1)).pk;
                    else
                        end_pk = ((Stop)list.elementAt(i+1)).pk;                    
                }else {
                    endStop = "'end'"; start_pk = -2;
                }
                
                sql = "INSERT INTO moves (tid,moveid,start_time,end_time,the_geom,start_stop,end_stop,start_stop_pk,end_stop_pk,rf) VALUES ("+
                    move.tid+","+moveId+",'"+move.startTime+"','"+move.endTime+"',"+move.toSQL()+","+startStop+","+endStop+","+start_pk+","+end_pk+",'"+move.tableName+"')";                
                moveId++;   
                flag=false;
            }
            if(!flag){//executes or not the query
            	s.execute(sql);            	
            }
                        
        }        
    }
    
    /**Marks GPSPoints as having no cluster.
     * 
     * @param array The GPSPoint reseted.
     */
    protected static void reset(Vector<GPSPoint> array) {
            for (int i=0;i<array.size();i++) {
                    array.elementAt(i).cluster = Cluster.NONE;
            }
    }

    /**	Main method to aply semantics to the clusters, with Intercepts Table.
     * 
     * Requirements: A trajectory with discrete and not-repeated time. One point should have
     *  a time 't' and no other point can have this same time 't'. 
     * 
	 * @param bufferChecked		If the user set a buffer or not.
	 * @param bufferValue		The value of buffer.
	 * @param clusters			The clusters being saved.
	 * @param config			User configurations.
	 * @param minTimeMilis		The mintime parameter received.
	 * @param featureType		Defines to pre-process in Type (true) or Instance (false) granularity.
	 * @param intercepts		The Table of Geometry Relations (Intercepts Table) to be used. 
     * @param SRID 				The spatial identification of the trajectory.
     * @param DB				Is it DB-SMoT or not ?
     * 
     * */
    
    public static void stopsDiscoveryFaster(boolean bufferChecked, double bufferValue,Vector<ClusterPoints> clusters, Config config,
		long minTimeMilis,boolean featureType,InterceptsG intercepts,int SRID,boolean DB){
		
		int stopid=-1;
		boolean first=true; // the flag first-point in the stop. When the stop is opened, the flag go down.	    
	    Vector<GPSPoint> array;
	    Vector stops = new Vector(); // the array of stops
	    Stop st;//the current stop
	    Unknown unk; //the current unknown    
	    System.out.println("Clusters found: "+clusters.size());

	    for(ClusterPoints c:clusters){
	        array=c.points;
	        Collections.sort(array);
	        //inicializations required...
	        first=true;
	        st = new Stop(bufferChecked,SRID);
	        unk = new Unknown(bufferChecked,SRID);
	        
	        for(int i=0;i<array.size();i++){
	        	if(!first){//the others not-the-first need to control if the gid is or not sequential  
	        		//before it, there's an Interc ?
	        		//Vector<Interc> rf=intercepts.is_in2(array.elementAt(i).gid);
	        		Interc rf = intercepts.is_in(array.elementAt(i).gid);
	        		//if(rf.size()>1)System.out.println("Size: "+rf.size());
	        		//if(rf.size()>0){//yes, it intercepts something, so...
	        		if (rf != null) {
	        			// if it's the same stop...
	        			//Interc j = rf.elementAt(0);//uses only the first Interc, cause a point do not Intercepts more than one RF at the same time
	        			Interc j = rf;
	        			if(j.gid==st.gid) st.addPoint(array.elementAt(i));
	        			//or create another stop.
	        			else{
	        				if(st.check()){//tests the actual stop
	        					if (unk.check(minTimeMilis)) {
	    	            			stops.addElement(unk);
	    	                    }
	    	                    unk = new Unknown(bufferChecked,SRID);
	    	                    
		            			stops.addElement(st);//if passes, it's added
		            		}
	        				else {//if didn't pass the test, the points became unknowns
		        				for(GPSPoint a:st.pts){
		        					unk.pontos.addElement(a);
		        				}
		        				unk.organizedPoints();
		        			}
		        			st = new Stop(bufferChecked,SRID);//creates a new stop
		        			st.addPoint(array.elementAt(i),j.rf,j.value,j.gid);
	        			}
	    			}
	        		else {//no, it didn't have an Interc, so...
	        			if(st.check()){//tests the actual stop
	        				if (unk.check(minTimeMilis)) {
		            			stops.addElement(unk);
		                    }
		                    unk = new Unknown(bufferChecked,SRID);
	        				
	            			stops.addElement(st);//if passes, it's added
	            		}
	        			else {//if didn't pass the test, the points became unknowns
	        				for(GPSPoint a:st.pts){
	        					unk.pontos.addElement(a);
	        				}
	        				unk.organizedPoints();
	        			}
	        			st = new Stop(bufferChecked,SRID);//creates a new stop
	            		first=true;
	            		unk.pontos.addElement(array.elementAt(i));
	        		}
	        	}            	
	        	else{//being the first, there's no need to tests, so...
	        		//System.out.println("gid: "+array.elementAt(i).gid);
	        		//Vector<Interc> rf=intercepts.is_in2(array.elementAt(i).gid);
	        		Interc rf = intercepts.is_in(array.elementAt(i).gid);
	        		//System.out.println("pt gid: "+rf.pt);
	            	//if(rf.size()>0){//...tests only if there's an intercs associated and add to the stop
	        		if (rf != null) {
	            		first=false;
	            		//Interc j = rf.elementAt(0);//uses only the first Interc, cause a point do not Intercepts more than one RF at the same time
	            		Interc j = rf;
	                    st.addPoint(array.elementAt(i),j.rf,j.value,j.gid); 
	            	}
	            	// in case of no Interc at all, we can continue normally putting new points in the unknown.
	            	else unk.pontos.addElement(array.elementAt(i));
	        	}	        	
	        }//end of for
	        
	        if (unk.check(minTimeMilis)) {
	            stops.addElement(unk);
	        }
	        if(st.check()){
	            stops.addElement(st);
			}
	    }//foreach Cluster

        System.out.println("\t\tSaving: "+stops.size()+" stops.");
        stopid=saveStopsAndMoves2(stops,config.conn,featureType,bufferValue,++stopid);        
    }

	protected static int saveStopsAndMoves2(Vector list, Connection conn, 
						boolean featureType, double buffer,int stopextern){
	    Statement s,s1;
	    boolean flag=false;
	    int stopId=stopextern;
	    String sql="";
	    for (int i=0;i<list.size();i++) {
	    	try{
	    		s = conn.createStatement();
		    	s1 = conn.createStatement();	
		        Object obj = list.elementAt(i);
		        if (obj.getClass() == Stop.class) {
		            Stop stop = (Stop) obj;
		            String stopName = featureType ? stop.tableName : (stop.gid + "_" + stop.tableName);
		            sql = "INSERT INTO "+TrajectoryFrame.getCurrentNameTableStop()+" (tid,stopid,start_time,end_time,stop_gid,stop_name,the_geom,rf,avg) VALUES "+
		                "("+stop.tid+","+stopId+",'"+stop.enterTime.toString()+"','"+stop.leaveTime.toString()+"','"+stop.gid+"','"+stopName+"',"+stop.toSQL(buffer)+",'"+stop.tableName+"',"+stop.avgSpeed()+")";                
		            stopId++;
		            flag=false;
		        }else if (obj.getClass() == Unknown.class) {
		            Unknown unk = (Unknown) obj;
		            int tid = unk.pontos.firstElement().tid;
		            if(unk.pontos.size()>=4){//to prevent the_geom null, nb: added by yipeng 080115
		            	String query = "select stop_name from "+TrajectoryFrame.getCurrentNameTableStop()+" where rf='unknown' AND ST_Intersects(the_geom,"+unk.toSQL(buffer)+");";
		            	ResultSet rs = s1.executeQuery(query);
		            	if(rs.next()){
		            		//joining same unknowns...
		            		//System.out.println("aqui");
		            		sql = "INSERT INTO "+TrajectoryFrame.getCurrentNameTableStop()+" (tid,stopid,start_time,end_time,stop_gid,stop_name,the_geom,rf,avg) VALUES "+
		            		"("+tid+","+stopId+",'"+unk.enterTime.toString()+"','"+unk.leaveTime.toString()+"',"+stopId+",'"+rs.getString("stop_name")+"',"+unk.toSQL(buffer)+",'unknown',"+unk.avgSpeed()+")";
		            	}
		            	else {//or creating another
		            		sql = "INSERT INTO "+TrajectoryFrame.getCurrentNameTableStop()+" (tid,stopid,start_time,end_time,stop_gid,stop_name,the_geom,rf,avg) VALUES "+
		            		"("+tid+","+stopId+",'"+unk.enterTime.toString()+"','"+unk.leaveTime.toString()+"',"+stopId+",'"+nextUnknown()+"_unknown',"+unk.toSQL(buffer)+",'unknown',"+unk.avgSpeed()+")";
		            	}
		            	stopId++;
		            	flag=false;//poe pra executar a query...
		            }
		            //else flag=true;//ou NAO executa...
		        }
		        
		        if(!flag){//teste pra saber se executa ou nao a query
		        	System.out.print("Stop "+i+" saving... ");
		        	System.out.println(sql);
		        	s.execute(sql);
		        	System.out.println("Saved");
		        }
	    	}catch(Exception e){
	    		System.out.println(e.getMessage());
	    		break;
	    	}
	    }
	    return --stopId;
	}	

	/**	Save the clusters created with CB-SMoT for visualization.
	 * 
	 * @param traj 		trajectory to be saved.
	 * @param config	User configurations.
	 * @throws SQLException		If any table didn't exist in the BD, or a field.
	 * 
	 * TODO Buffers here should probably be fixed to buffer meters rather than SRS
	 * But I'm not entirely sure of the author's intention and the code isn't called anyway. 
	 * 
	 */
	public static void saveClusters(Trajectory traj, Config config, long minTimeMilis) throws SQLException {	
		Statement s = config.conn.createStatement();
		Vector<ClusterPoints> array = traj.generateClusterPoints(minTimeMilis);	

		for(ClusterPoints c:array){
	    	String sql= "insert into clusters (tid,cluster_id,the_geom) values" +
	    			"("+traj.tid+","+c.clusterId+",ST_Buffer(ST_LineFromText('LINESTRING(";
	    	for(int i=0;i<c.points.size();i++){
	    		org.postgis.Point p = c.points.elementAt(i).point;
	    		sql+= p.getX() + " " + p.getY() + ",";
	    	}
	    	sql=sql.substring(0,sql.length()-2) + ")',-1),10));";
	    	System.out.println(sql);
	    	try{
	    	s.execute(sql);
	    	}
	    	catch(Exception e){
	    		System.out.println(e.getMessage());
	    	}
	    	finally{
	    		s.close();	    		
	    	}
	    }
	    
	}
	
	/** Finds the stops in a trajectory.
	 * 
	 * @param bufferChecked		If the user set a buffer or not.
	 * @param buffer		The value of buffer.
	 * @param config			User configurations.
	 * @param t					The trajectory being analized.
	 * @param featureType		Defines to pre-process in Type (true) or Instance (false) granularity.
	 * @param intercepts		The Table of Geometry Relations (Intercepts Table) to be used.
	 * @throws SQLException		If any table didn't exist in the BD, or a field.
	 */
	public static void smot2(boolean bufferChecked, Double buffer, Config config,Trajectory t,
		boolean featureType,InterceptsG intercepts) 
		throws SQLException {
		
		int i,j,gidaux,serial_gid=0;	
		org.postgis.PGgeometry geom;
		Statement s=config.conn.createStatement();	
		String sql = "select "+config.tid+",gid,"+config.time+",the_geom from "+config.table+" where "+config.tid+"="+t.tid+" order by time;";
		System.out.println("Aplying method smot...\n"+sql);
		ResultSet rs = s.executeQuery(sql);
		Vector stops = new Vector();
	    ActiveStops activeStops = new ActiveStops();
	    boolean first=true;
	    Stop st = new Stop();
	    int gidRelevantFeature = -1;
	    //String nameRelevantFeature = "";
	    
	    while(rs.next()){
	    	System.out.print(".");//just for visualization
	    	//creates the point in the BD 
	    	GPSPoint pt = new GPSPoint();
	    	pt.tid=rs.getInt("tid");
	    	pt.gid=rs.getInt("gid");
	    	pt.time=rs.getTimestamp(config.time);
	    	geom = (org.postgis.PGgeometry) rs.getObject("the_geom");
	        pt.point = (org.postgis.Point) geom.getGeometry();
	    	
	    	//get the actual gid/time variables to be tested
	    	gidaux=pt.gid;
	    	if(!first){//the others not-the-first need to control if the gid is or not sequential  
	    		//before it, there's an Interc ?
	    		Interc rf=intercepts.is_in(pt.gid);
	    		//Interc rf = intercepts.getRFIntercept(pt.gid, gidRelevantFeature);
	    		if((rf!=null) && (rf.gid == gidRelevantFeature) /*&& (rf.rf == nameRelevantFeature)*/) {//yes, it intercepts something, so...
	    		//if (rf != null) {
	    			//... we have to test that sequential control, then...
				//	if(timeaux!=serial_time){// the times of the points (this and the anterior), can't be the same !!
						if(gidaux - serial_gid <= 1){ // the diference between gid's can't exceed 1
							st.addPoint(pt);
						}
						//else...
				//	}
					//else...
				}
	    		else {//no, it didn't have an Interc, so...
	    			if(st.check()) {//tests the actual stop
	        			stops.addElement(st);//if passes, it's added
	        		}        			
	    			st = new Stop();//creates a new stop
	        		if (rf != null) { //it is the first of another relevant feature in the same trajectory
	        			st.addPoint(pt,rf.rf,rf.value,rf.gid); // saves the enterTime
		        		first=false;
		        		gidRelevantFeature = rf.gid;
		        		//nameRelevantFeature = rf.rf;
	        		}
	        		else {
	        			first = true;
	        		}
	    		}
	    	}
	    	else{//being the first, there's no need to tests, so...            		
	        	Interc rf=intercepts.is_in(pt.gid);
	        	if(rf!=null){//...tests only if there's an intercs associated and add to the stop
	        		st.addPoint(pt,rf.rf,rf.value,rf.gid); // saves the enterTime
	        		first=false;
	        		gidRelevantFeature = rf.gid;
	        		//nameRelevantFeature = rf.rf;
	        	}
	        	// in case of no Interc at all, we can continue normally.
	    	}
	    	//refresh the values of the serial_variables
	    	serial_gid=gidaux;
		}
	    if ((!first) && (st.check())) {
	    	stops.addElement(st);//if passes, it's added
	    }
	    System.out.println("");	
	    saveStopsAndMoves2(stops,config.conn,featureType,buffer,0);
	    rs.close();
	}
}
