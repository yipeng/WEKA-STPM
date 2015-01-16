/*
 * Trajectory.java
 *
 * Created on 6 de Julho de 2007, 16:43
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package weka.gui.stpm;

import java.util.*;
import java.sql.*;
import java.io.*;
/**
 *
 * @author Administrador
 */
public class Trajectory {
    public int tid;
    public Vector<GPSPoint> points = new Vector<GPSPoint>();
    private int SRID;

    /** Creates a new instance of Trajectory */
    public Trajectory() {
    	this.SRID=-1;//default SRID for postgres
    }
    
    /** Creates a new instance of Trajectory with SRID !=-1 */
    public Trajectory(int s) {
    	this.SRID=s;
    }
    
    public int getSRID(){
    	return this.SRID;
    }
    
    public void saveClustersId(Config conf) throws SQLException {
        Statement s = conf.conn.createStatement();
        for (int i=0;i<points.size();i++) {
            if (points.elementAt(i).cluster == Cluster.STOP) {
                String sql = "UPDATE "+conf.table+ " SET cluster_id = "+points.elementAt(i).clusterId +" WHERE gid = "+points.elementAt(i).gid;
                //System.out.println(sql);
                s.execute(sql);
            }
        }
    }
    
    public Vector<ClusterPoints> generateClusterPoints(long minTimeMilis) {
        Vector<ClusterPoints> ret = new Vector<ClusterPoints>();
        
        int clusterId = -999;
        ClusterPoints cluster = null;
        Vector<GPSPoint> p = this.points;
        Collections.sort(p);
        for (int i=0;i<p.size();i++) {
            GPSPoint pt = p.elementAt(i);
            
            if (pt.clusterId != clusterId && pt.cluster == Cluster.STOP) {
                if (cluster != null)
                    ret.addElement(cluster);
                
                cluster = new ClusterPoints();
                
                cluster.clusterId = pt.clusterId;
                cluster.points.addElement(pt);
                clusterId = pt.clusterId;
            }else if (pt.clusterId == clusterId && pt.cluster == Cluster.STOP) {
                cluster.points.addElement(pt);
                clusterId = pt.clusterId;
            }
        }
        if (cluster != null)
            if (cluster.points.size() > 0)
                ret.addElement(cluster);
        
        for (int i=ret.size()-1;i>=0;i--) {
            if (ret.elementAt(i).getDuration() < minTimeMilis)
                ret.remove(i);
        }
        
        return ret;
    }
    
    //Gera a condi��o em string, para ser usada no sql. A condi��o contem os intervalos de tempo dos clusters
    public String clusterPoints() {
        String ret = "";
        // ret = OR do gid dos pontos do CLUSTER
        for (int i=0;i<points.size();i++) {
            if (points.elementAt(i).cluster == Cluster.STOP)
                ret += "gid = " + points.elementAt(i).gid + " OR ";
        }
        // teste para saber se a trajet�ria tem um m�nimo de pontos, neste caso, '4'
        if (ret.length() > 4)
            return (" AND ("+ret.substring(0,ret.length() - 3)+")");
        else
            return (" AND (false)");
    }
    
    public Vector generateStopsAndMovesList(long stopTimeMilis) {
        Vector list = new Vector();
        Move move = null;
        Stop stop = null;
        boolean first = true;
        boolean searchForStop = true;            
        for (int i=0;i<points.size();i++) {
            GPSPoint point = points.elementAt(i);

            if (first && point.cluster == Cluster.STOP) {
                stop = new Stop();
                    stop.tid = tid;
                    stop.enterTime = point.time;
                    stop.leaveTime = stop.enterTime;
                    stop.points.addElement(point.point);
                    stop.gid = point.clusterId;
                searchForStop = true;
                first = false;
            }else if (first) {  //Starts with Move
                move = new Move();
                move.tid = tid;
                move.startStop = null;
                move.points.addElement(point.point);
                searchForStop = false;
                first = false;                
            }else if (searchForStop) {
                if (point.cluster == Cluster.STOP) {
                    if (point.clusterId == stop.gid)  { //Segue o mesmo stop
                        stop.leaveTime = point.time;
                        stop.points.addElement(point.point);                
                    }else { //2 Stops consecutivos
                        list.addElement(stop);
                        move = new Move();
                            move.tid = tid;
                            move.startStop = stop;
                            move.points.addElement(stop.points.lastElement());
                        stop = new Stop();
                            stop.tid = tid;
                            stop.enterTime = point.time;
                            stop.leaveTime = stop.enterTime;
                            stop.gid = point.clusterId;
                            stop.points.addElement(point.point);                
                        move.points.addElement(stop.points.lastElement());
                        move.endStop = stop;
                        list.addElement(move);
                    }
                }else { //Move
                    list.addElement(stop);
                    move = new Move();
                    move.tid = tid;
                    move.startStop = stop;
                    move.points.addElement(stop.points.lastElement());
                    move.points.addElement(point.point);
                    searchForStop = false;
                }
            }else { //Seach for move
                if (point.cluster == Cluster.MOVE) { //Segue o move
                    move.points.addElement(point.point);                    
                }else { //Achou um stop
                    stop = new Stop();
                    stop.tid = tid;
                    stop.enterTime = point.time;
                    stop.leaveTime = stop.enterTime;
                    stop.gid = point.clusterId;
                    stop.points.addElement(point.point);                
                    move.endStop = stop;
                    list.addElement(move);
                    searchForStop = true;
                }
            }
        }
        
        if (searchForStop) {
            list.addElement(stop);
        }else {
            move.endStop = null;
            list.addElement(move);
        }
        
        for (int i=0;i<list.size();i++) {
            Object obj = list.elementAt(i);
            if (obj.getClass() == Stop.class) {
                Stop st = (Stop) obj;
                if (st.leaveTime.getTime() - st.enterTime.getTime() < stopTimeMilis) {
                    list.elementAt(i);
                    Move mprevious=null,mafter=null,mov = new Move();
                    mov.tid = st.tid;
                    if (i-1 >= 0) {
                        mprevious = (Move) list.elementAt(i-1);
                        mov.points.addAll(mprevious.points);
                        mov.startStop = mprevious.startStop;
                    }
                    if (i + 1 < list.size()) {  //Move after stop removed
                        mafter = (Move) list.elementAt(i+1);
                        mov.points.addAll(mafter.points);
                        mov.endStop = mafter.endStop;
                    }
                    if (mprevious != null)
                        list.remove(mprevious);
                    list.remove(st);
                    if (mafter != null)
                        list.remove(mafter);
                    if (i-1 >= 0) {
                        list.add(i-1,mov);
                        i = i-1;
                    }else {
                        list.add(i,mov);
                    }
                }
            }
        }
        return list;
    }
    
    public void calculatePointsSpeed(int neighborSize) {
        Neighbor neighbor = new Neighbor();
        // Inicializa o conjunto neighbor
        int aux = neighborSize * 2 + 1;
        for (int i=0;i<aux-1;i++) {
            neighbor.putOne(points.elementAt(i));
        }
        //neighbor inicializado com conjunto �mpar, de modo que tenha ponto m�dio
        GPSPoint p=null;
        for (int i=aux;i<points.size();i++) {
            //poe o neighbor 'n'
        	neighbor.putOne(points.elementAt(i));
            double speed = neighbor.calculateMeanSpeed();
            //calcula a velocidade a partir do 'ponto m�dio'
            p = neighbor.getMiddlePoint();
            p.speed = speed;
            //retira o neighbor '0'
            neighbor.removeOne();
            System.out.println("Speed of point: "+speed);
        }     
        //Calculate the speed of the initial/final points in the trajectory
        // pois eles, no c�lculo de 'pontos m�dios', ficaram de fora
        for (int i=neighborSize-1;i>=0;i--)
            points.elementAt(i).speed = points.elementAt(i+1).speed;
        
        for (int i=points.size()-neighborSize;i<points.size();i++)
            points.elementAt(i).speed = points.elementAt(i-1).speed;
    }
    
    public void calculatePointsSpeed() {
    	int i = 1;
    	while (i < points.size()) {
    		GPSPoint lastPoint = points.elementAt(i-1);
    		GPSPoint currentPoint = points.elementAt(i);
    		double distance = lastPoint.distance(currentPoint);
    		long time = currentPoint.time.getTime() - lastPoint.time.getTime();
    		points.elementAt(i).speed = distance / (time/1000.0);
    		/**
    		 * technically speed is inf when dist !=0 and time=0 
    		 * but for now we'll handle all NaNs silently.
    		 */
    		if (Double.isNaN(points.elementAt(i).speed)){ 
    			points.elementAt(i).speed = 0;
    		}
    		i++;
    	}
    }
    
    public double meanDist() {
        double sum = 0.0;
        GPSPoint p2,p1 = points.elementAt(0);
        for (int i=1;i<points.size();i++) {
            p2 = points.elementAt(i);
            sum += p1.distance(p2);
            p1 = p2;
        }
        return (sum/(points.size()));
    }
    
    public double spatialStdDes() {
        GPSPoint p2,p1 = points.elementAt(0);

        double mean = meanDist();
        double dist,stdDeviation = 0;

        for (int i=1;i < points.size();i++) {
            p2 = points.elementAt(i);

            dist = p1.distance(p2);
            stdDeviation += Math.pow(dist - mean,2);

            p1 = p2;
        }

        return Math.sqrt(stdDeviation / (points.size() - 2));
    }        
    
    public long duration() {
        return (points.lastElement().time.getTime() - points.firstElement().time.getTime());
    }
    
    public double stdDesTime() {
        double mean = duration() / (points.size()-1);
        mean /= 1000;
        
        GPSPoint p2,p1 = points.elementAt(0);

        double duration = 0;
        double stdDeviation = 0;

        for (int i=1;i < points.size();i++) {
            p2 = points.elementAt(i);

            duration = (p2.time.getTime() - p1.time.getTime())/1000;
            stdDeviation += Math.pow(duration - mean,2);

            p1 = p2;
        }

        return Math.sqrt(stdDeviation / (points.size() - 2));        
    }
    
    public double meanSpeed() {
        //soma-se todas as distancias e divide-se pela diferença do tempo do primeiro para o ultimo ponto.
        double totalDist = 0.0;
        GPSPoint p2,p1 = points.elementAt(0);
        for (int i=1;i<points.size();i++) {
            p2 = points.elementAt(i);
            totalDist += p1.distance(p2);
            p1 = p2;
        }        
        
        return (totalDist / (duration()/1000));
    }
    
    public void generateFileStats(double speedPerc, long minTimeMilis) {
        int NEIGHBOR_SIZE = 6;
        
        try {
            File f = new File("trajectory "+tid+" stats.txt");
            FileWriter fw = new FileWriter(f);
            BufferedWriter bw = new BufferedWriter(fw);
            
            double meanDistance = meanDist();
            bw.write("#Mean Distance: "+meanDistance);
            bw.newLine();
            double stdDes = spatialStdDes();
            bw.write("#Distance Standard Deviation: "+stdDes);
            bw.newLine();
            bw.write("#Duration (miliseconds):"+duration());
            bw.newLine();
            bw.write("#Number of Points: "+points.size());
            bw.newLine();
            double meanSpeed = meanDistance * (points.size()-1);
            meanSpeed = meanSpeed / (duration() / 1000);
            bw.write("#Mean Speed(m/s): "+meanSpeed);
            bw.newLine();
            double speedLimit = meanSpeed * speedPerc;
            bw.write("#Speed Limit: "+speedLimit);
            bw.newLine();
            bw.write("#time,speed(m/s),cluster id");
            bw.newLine();
            
            Neighbor neighbor = new Neighbor();
            int aux = NEIGHBOR_SIZE * 2 + 1;
            for (int i=0;i<aux;i++) {
                neighbor.putOne(points.elementAt(i));
            }
            GPSPoint p = points.elementAt(0);
            int clusterid;
            for (int i=aux;i<points.size();i++) {
                neighbor.putOne(points.elementAt(i));
                double speed = neighbor.calculateMeanSpeed();
                p = neighbor.getMiddlePoint();
                if (p.cluster == Cluster.STOP)
                    clusterid = p.clusterId;
                else
                    clusterid = -1;
                bw.write(p.time.toString()+","+speed+","+clusterid);
                bw.newLine();
                neighbor.removeOne();                
            }
            
            bw.close();
            fw.close();
        }catch(IOException e) {
            e.printStackTrace();
        }
    }
    
    class Neighbor {
        ArrayList<GPSPoint> neighbor;
        
        public Neighbor() {
            neighbor = new ArrayList<GPSPoint>();
        }
        
        public void putOne(GPSPoint p) {
            neighbor.add(p);
        }
        public void removeOne() {
            neighbor.remove(0);
        }
        public GPSPoint getMiddlePoint() {
            return neighbor.get((int)((neighbor.size()-1)/2));
        }
        public double calculateMeanSpeed() {
            double dist=0;
            GPSPoint p2=null,p1 = neighbor.get(0);
            Timestamp fim=null,ini = p1.time;
            for (int i=1;i<neighbor.size();i++) {
                p2 = neighbor.get(i);
                dist += p1.distance(p2);
                p1 = p2;
            }
            fim = p2.time;
            long tempo = fim.getTime()-ini.getTime();
            
            return (dist / (neighbor.size()-1))/(tempo/1000);
        }
    }
}
