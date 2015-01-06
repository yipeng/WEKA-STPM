package weka.gui.geodata.visualizer.mouseListeners;

import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import weka.gui.geodata.visualizer.ShowGeoData;

public class ItemChoiceColorHandler implements ItemListener {

	ShowGeoData refMain;

	public ItemChoiceColorHandler(ShowGeoData nShow) {
		refMain = nShow;
	}

	public void itemStateChanged(ItemEvent ie) {
		int colorSelected = refMain.choColor.getSelectedIndex();
		switch (colorSelected) {
		case 0:  refMain.lastColor = Color.BLUE; break;
		case 1:  refMain.lastColor = Color.BLACK; break;
		case 2:  refMain.lastColor = Color.RED; break;
		case 3:  refMain.lastColor = Color.YELLOW; break;
		case 4:  refMain.lastColor = Color.CYAN; break;
		case 5:  refMain.lastColor = Color.MAGENTA; break;
		case 6:  refMain.lastColor = Color.GRAY; break;
		case 7:  refMain.lastColor = Color.GREEN; break;
		}
	}

}