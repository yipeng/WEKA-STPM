package weka.gui.geodata.visualizer.mouseListeners;

import java.awt.event.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import weka.gui.geodata.visualizer.ShowGeoData;

public class ItemChoiseTableHandler implements ItemListener {
	
	ShowGeoData refMain;
	
	public ItemChoiseTableHandler(ShowGeoData nShow) {
		refMain = nShow;
	}
	
	public void itemStateChanged(ItemEvent arg0) {
		String tempTable = refMain.choTable.getSelectedItem();
		populateChoiceColumn(tempTable);
		String temp = "SELECT the_geom FROM "+tempTable+" WHERE true";
		
		refMain.resArea.setText(temp);
		
	}
	
	// Populate the choice box from tables  
	public void populateChoiceTable() {
		Statement sChoiceTable;
		try {
			sChoiceTable = refMain.conn.createStatement();
			ResultSet vChoiceTableName = sChoiceTable.executeQuery("SELECT f_table_name as tableName,type "+
					"FROM geometry_columns " +
					"WHERE true "+
					"ORDER BY tableName");
			while ( vChoiceTableName.next()){
				refMain.choTable.add(vChoiceTableName.getString("tableName"));
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	// Populate the choice box from attributes
	public void populateChoiceColumn(String table) {
		Statement sChoiceTable;
		refMain.choColumn.removeAll();
		refMain.choColumn.add("Choose the column");
		try {
			sChoiceTable = refMain.conn.createStatement();
			String queryPart1 = "SELECT a.attname as \"Column\", "+
			"pg_catalog.format_type(a.atttypid, a.atttypmod) as \"Datatype\" "+ 
			"FROM pg_catalog.pg_attribute a "+ 
			"WHERE a.attnum > 0 AND NOT a.attisdropped AND a.attrelid = ";
			String queryPart2 = "( SELECT c.oid FROM pg_catalog.pg_class c "+ 
			"LEFT JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace "+ 
			"WHERE c.relname ~ '^("+table+")$' "+ 
			"AND pg_catalog.pg_table_is_visible(c.oid) ) ";
			ResultSet vChoiceColumnName = sChoiceTable.executeQuery(queryPart1+queryPart2);
			while ( vChoiceColumnName.next() ) {
				refMain.choColumn.add(vChoiceColumnName.getString("Column"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}	
	
}
