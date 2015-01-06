package weka.gui.geodata.geoDataSet;

import java.awt.Color;
import java.util.LinkedList;

public class GeoDataPoint extends GeoDataSet{

	private LinkedList<Float[]> pointsFromDB;
	private Float[][] pointsInScreen;
	
	public GeoDataPoint(String queryN, Color colorN, boolean activeN, int numDataN) {
		super(queryN, colorN, activeN, numDataN);
		geoType = "POINT";
		color	= colorN;
	}

	public void addData(LinkedList<Float[]> buffFromDB) {
		pointsFromDB	= buffFromDB;
		pointsInScreen	= new Float[buffFromDB.size()][2];
	}

	public void reprocessGraphics(Float lowerX, Float lowerY, Float multX, Float multY) {
		int i=0;
		for(Float[] linhaBD : pointsFromDB){
			pointsInScreen[i][0]=linhaBD[0]-lowerX;
			pointsInScreen[i][1]=linhaBD[1]-lowerY;
			pointsInScreen[i][0]=30+(pointsInScreen[i][0]*multX);
			pointsInScreen[i][1]=350+(pointsInScreen[i][1]*multY);
			i++;
		}		
	}
	
	public Float[][] returnPointsInScreen(){
		return pointsInScreen;
	}
}
