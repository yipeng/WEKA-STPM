package weka.gui.geodata.geoDataSet;

import java.awt.Color;
import java.util.LinkedList;

public class GeoDataLine extends GeoDataSet{

	private LinkedList<Float[]> pointsFromDB;
	private Float[][] pointsInScreen;

	public GeoDataLine(String queryN, Color colorN, boolean activeN, int numDataN) {
		super(queryN, colorN, activeN, numDataN);
		geoType = "LINE";
		color	= colorN;
	}

	public void addData(LinkedList<Float[]> buffLinesFromDB) {
		pointsFromDB	= buffLinesFromDB;
		pointsInScreen	= new Float[numData][4];
	}

	public void reprocessGraphics(Float lowerX, Float lowerY, Float multX, Float multY) {
		int i=0;
		for(Float[] linhaBD : pointsFromDB){
			pointsInScreen[i][0]=30+((linhaBD[0]-lowerX)*multX);
			pointsInScreen[i][1]=350+((linhaBD[1]-lowerY)*multY);
			pointsInScreen[i][2]=30+((linhaBD[2]-lowerX)*multX);
			pointsInScreen[i][3]=350+((linhaBD[3]-lowerY)*multY);
			i++;
		}
	}

	public Float[][] returnLinesInScreen(){
		return pointsInScreen;
	}
}
