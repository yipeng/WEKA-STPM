package weka.gui.geodata.visualizer.mouseListeners;

import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import weka.gui.geodata.geoDataSet.GeoDataSet;
import weka.gui.geodata.visualizer.ShowGeoData;

public class ListTableHandler implements ItemListener{
	
	Color color;
	ShowGeoData refMain;
	GeoDataSet refData;
	
	public ListTableHandler(ShowGeoData sgd, GeoDataSet  refDataN) {
		refMain = sgd;
		refData = refDataN;
	}

	public void itemStateChanged(ItemEvent arg0) {
		if(refData.isActive()){
			refData.setActive(false);
		}else{
			refData.setActive(true);
		}
		refMain.drawArea.repaint();
	}

}
