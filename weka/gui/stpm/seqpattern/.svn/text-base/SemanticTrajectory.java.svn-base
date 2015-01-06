/*
 * SemanticTrajectory.java
 *
 * Created on 11 de Dezembro de 2007, 17:18
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package weka.gui.stpm.seqpattern;

import java.util.Vector;
/**Each SemTrajct has a tid and a set of items. An item is an stop_name, formatted with 
 * item+time information.
 * 
 * Eg. Item: 	1_unknown_wednesday_thursday (Time: Day of the Week, Name+Start Time+End Time)
 *				10_school_1 (Time: Month, Name+Start Time) 
 *
 * @author Administrador
 */
public class SemanticTrajectory {
    public int tid;
    public Vector<String> item = new Vector<String>();
    
    /** Creates a new instance of SemanticTrajectory */
    public SemanticTrajectory() {
    }
    
    public boolean containsPattern(Vector<String> pattern) {
        int startIndex = 0;
        
        for (int i=0;i < pattern.size(); i++) {
            String s = pattern.elementAt(i);
            int index = item.indexOf(s,startIndex);
            if (index != -1) {
                startIndex = index + 1;
            }else {
                return false;
            }
        }
        return true;        
    }
}
