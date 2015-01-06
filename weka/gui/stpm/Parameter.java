/*
 * Parameter.java
 *
 * Created on 4 de Julho de 2007, 19:13
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package weka.gui.stpm;

/**
 *
 * @author Administrador
 */
public class Parameter {
    enum Type {DOUBLE, INT};
    String name;
    Type type;
    Object value;
    /** Creates a new instance of Parameter */
    public Parameter(String name,Type t, Object value) {
        this.name = name;
        type = t;
        this.value = value;
    }
    
    public String toString() {
        return name;
    }
}