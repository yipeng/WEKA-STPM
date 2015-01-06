package weka.gui.geodata.visualizer.mouseListeners;

import java.awt.event.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import weka.gui.geodata.visualizer.ShowGeoData;

public class ItemChoiseColumnHandler implements ItemListener {
	
	ShowGeoData refMain;
	
	public ItemChoiseColumnHandler(ShowGeoData nShow) {
		refMain = nShow;
	}
	
	public void itemStateChanged(ItemEvent arg0) {
		String tempTable = refMain.choTable.getSelectedItem();
		
		String tempColumn = refMain.choColumn.getSelectedItem();
		if(tempColumn != "Choose the column"){
			populateValuesColumn(tempTable,tempColumn);
		}
	}

	// Populate the choice box from values
	public void populateValuesColumn(String table, String column) {
		Statement sChoiceTable;
		refMain.choValues.removeAll();
		refMain.choValues.add("Choose a value");
		try {
			sChoiceTable = refMain.conn.createStatement();
			String query = "SELECT DISTINCT "+column+" FROM "+table+" ORDER BY "+column;
			ResultSet vChoiceColumnName = sChoiceTable.executeQuery(query);
			while ( vChoiceColumnName.next() ) {
				if(vChoiceColumnName.getString(column)!=null){
					refMain.choValues.add(vChoiceColumnName.getString(column));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}	
	
}
