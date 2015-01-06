/*
 * Config.java
 *
 * Created on 8 de Agosto de 2007, 16:38
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package weka.gui.stpm;

import java.sql.*;
/**
 *
 * @author Administrador
 */
public class Config {
    public Connection conn;
    public String table;
    public String tid;
    public String time;
	public String limit;
	public String latitude;
	public String longitude;
	
    
    /** Creates a new instance of Config */
    public Config() {
    }
}
