/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package weka.gui.stpm;

import java.util.Comparator;

class SpeedComparator implements Comparator<GPSPoint> {
    public int compare(GPSPoint p1,GPSPoint p2) {
        if (p1.speed < p2.speed)
            return -1;
        else if (p1.speed > p2.speed)
            return 1;
        else
            return 0;
    }
}