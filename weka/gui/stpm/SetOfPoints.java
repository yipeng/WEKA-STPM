package weka.gui.stpm;

import java.util.ArrayList;

/**
 *
 * @author Cassiano
 */
public class SetOfPoints {
    private static final int FIRST_INDEX = 0;
    
    ArrayList<GPSPoint> points;

    public SetOfPoints() {
        points = new ArrayList<GPSPoint>();
    }

    /**
     * Get the duration of time of the set of points.
     * @return duration of time in miliseconds
     */
    public long duration() {
        long firstTime = points.get(FIRST_INDEX).time.getTime();
        long lastTime = points.get(points.size()-1).time.getTime();
        return lastTime - firstTime; // in miliseconds
    }

    public void addToBegin(GPSPoint point) {
        points.add(FIRST_INDEX, point);
    }

    public void addToEnd(GPSPoint point) {
        points.add(point);
    }

    public int getFirstPointTimeIndex() {
        return points.get(FIRST_INDEX).getTimeIndex();
    }

    public int getLastPointTimeIndex() {
        return points.get(points.size()-1).getTimeIndex();
    }

    public double meanSpeed() {
        int i = 1;
        long timeFirst = points.get(0).time.getTime();
        double distance = 0.0;
    	while (i < points.size()) {
            GPSPoint lastPoint = points.get(i-1);
            GPSPoint currentPoint = points.get(i);
            distance = distance + lastPoint.distance(currentPoint);
            i++;
    	}
        long timeLast = points.get(points.size()-1).time.getTime();
        long time = timeLast - timeFirst;

        return (distance / ((double) time/1000));
    }

    public void setClusterId(int clusterId) {
        for (GPSPoint p: points) {
            p.clusterId = clusterId;
        }
    }
}
