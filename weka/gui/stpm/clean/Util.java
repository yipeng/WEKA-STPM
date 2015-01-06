/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package weka.gui.stpm.clean;

import java.beans.Statement;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.Struct;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import weka.gui.stpm.GPSPoint;
import weka.gui.stpm.TrajectoryFrame;

/**
 *
 * @author hercules
 */
public class Util {

    public static File getFileSpeed(String nameTable){
        File f = new File("data");
        if (!f.exists()) {
            f.mkdir();
        }
        return new File("data/" + nameTable +".xls");
    }
    public static void imprimeVelocidades( Vector<GPSPoint> points, File file, boolean isFirst) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            DecimalFormat df = new DecimalFormat("###,###.### ");
            df.setMinimumFractionDigits(5);

            FileWriter sfw = new FileWriter(file, true);
            BufferedWriter sbw = new BufferedWriter(sfw);
            // adding comments with information
            System.out.println("%  " + TrajectoryFrame.getCurrentNameTableStop() + "  %");
            if(isFirst){
                sbw.write("%  " + TrajectoryFrame.getCurrentNameTableStop() + "  %");
                sbw.newLine();
                //System.out.println("Tid\tGid\tTime\tVelocidade");
                sbw.write("Tid\tGid\tTime\tVelocidade");
            }
            sbw.newLine();
            for (GPSPoint gpsp : points) {
                String current = gpsp.tid + "\t" + gpsp.gid + "\t" + sdf.format(gpsp.time) + "\t" + df.format(gpsp.speed);
                //System.out.println(current);
                sbw.write         (current);
                sbw.newLine();
            }
//            sbw.write         ("Tid;Gid;Time;Velocidade");
//            sbw.newLine();
            sbw.flush();
            sbw.close();
//            if(fechaArquivo){
//            }
            System.out.println("Tid\tGid\tTime\tVelocidade\n");
           
        } catch (IOException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
