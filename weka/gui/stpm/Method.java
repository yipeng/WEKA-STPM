/*
 * Method.java
 *
 * Created on 4 de Julho de 2007, 19:11
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package weka.gui.stpm;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Vector;
/**
 *
 * @author Administrador
 */
abstract class Method{       
	Vector<Parameter> param = new Vector<Parameter>();
	
	abstract public void run(Trajectory t,InterceptsG in,String targetFeature,InterceptsG streets) throws SQLException, IOException;
	abstract public void run2(Trajectory t,InterceptsG in,String targetFeature) throws SQLException;
}
