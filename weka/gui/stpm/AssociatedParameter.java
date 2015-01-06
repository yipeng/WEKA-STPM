/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package weka.gui.stpm;

/**
 *
 * @author Simone
 */
public class AssociatedParameter {
    String name;
    Integer value = new Integer(120);
    String type;

    public AssociatedParameter(String p) {
        name = p;
    }
    
    public AssociatedParameter(String p,String t) {
        name = p;
        type = t;
    }

    public String toString() {
        return name;
    }
}
