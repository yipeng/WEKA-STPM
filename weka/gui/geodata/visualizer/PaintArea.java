package weka.gui.geodata.visualizer;

import java.awt.*;
import java.util.LinkedList;

import javax.swing.*;

import weka.gui.geodata.geoDataSet.GeoDataLine;
import weka.gui.geodata.geoDataSet.GeoDataPoint;
import weka.gui.geodata.geoDataSet.GeoDataPolygon;
import weka.gui.geodata.geoDataSet.GeoDataSet;

public class PaintArea extends Panel{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6430760477797851224L;
	ShowGeoData master;

	public PaintArea(ShowGeoData masterN){
		super();
		master = masterN;
	}

	public void paint(Graphics g){
		setBackground(Color.WHITE);
		for(int ij=0;ij<6;ij++){
			JButton buttonBuffer = master.screenAdjust[ij]; 
			buttonBuffer.setLocation(master.JButtonZoomX[ij], master.JButtonZoomY[ij]);
			buttonBuffer.setSize(50, 20);
			buttonBuffer.repaint();
		}
		if(master.syncPaint){
			LinkedList<GeoDataSet> geoSetLocal =  master.geoSet;
			for(int k=0;k<geoSetLocal.size();k++){
				GeoDataSet ref = geoSetLocal.get(k);
				if(ref.isActive()){
					g.setColor(ref.returnColor());
					if(ref.returnType()=="POINT"){
						Float[][] bufferMaior = ((GeoDataPoint) ref).returnPointsInScreen();
						for(Float[] buffer : bufferMaior){
							g.fillOval(buffer[0].intValue(), buffer[1].intValue(), 3, 3);
						}
					}else if(ref.returnType()=="LINE"){
						Float[][] bufferMaior = ((GeoDataLine) ref).returnLinesInScreen();

						for(Float[] buffer : bufferMaior){
							g.drawLine(buffer[0].intValue(), buffer[1].intValue(), buffer[2].intValue(), buffer[3].intValue());
						}
					}else if(ref.returnType()=="POLYGON"){
						Polygon[] bufferMaior = ((GeoDataPolygon) ref).returnPoligonsInScreen();
						Color defaultC = g.getColor();
						for(Polygon buffer : bufferMaior){
							g.setColor(defaultC);
							g.fillPolygon(buffer);
							//g.setColor(new Color(255-g.getColor().getRed(),255-g.getColor().getGreen(),255-g.getColor().getBlue()));
							g.setColor(Color.WHITE);
							g.drawPolygon(buffer);
						}
					} 
				}
			}
		}
	}

}
