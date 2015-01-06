package weka.gui.geodata.visualizer.mouseListeners;

import java.awt.event.*;

import weka.gui.geodata.visualizer.ShowGeoData;

public class ItemChoiseValuesHandler implements ItemListener {
	
	ShowGeoData refMain;
	
	public ItemChoiseValuesHandler(ShowGeoData nShow) {
		refMain = nShow;
	}
	
	public void itemStateChanged(ItemEvent arg0) {
		String tempColumn	= refMain.choColumn.getSelectedItem();
		String tempValue	= refMain.choValues.getSelectedItem();

		refMain.parser.addPiece(tempColumn+"='"+tempValue+"'");
	}
	
}
