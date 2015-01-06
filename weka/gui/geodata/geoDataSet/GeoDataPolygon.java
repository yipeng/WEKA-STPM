package weka.gui.geodata.geoDataSet;

import java.awt.Color;
import java.awt.Polygon;
import java.util.LinkedList;

public class GeoDataPolygon extends GeoDataSet{

	private LinkedList<Float[][]> polygonsFromDB;
	private LinkedList<Polygon> polygosInScreen;
	private Polygon[] polygonsInScreen;

	public GeoDataPolygon(String queryN, Color colorN, boolean activeN, int numDataN) {
		super(queryN, colorN, activeN, numDataN);
		geoType = "POLYGON";
		color	= colorN;		
	}

	public void addData(LinkedList<Float[][]> buffFromDB) {
		polygonsFromDB	= buffFromDB;
		polygosInScreen	= new LinkedList<Polygon>();
	}

	public void reprocessGraphics(Float lowerX, Float lowerY, Float multX, Float multY) {
		int i=0;
		polygosInScreen.clear();
		for(Float[][] linhaBD : polygonsFromDB){
			polygonsInScreen = new Polygon[1];
			
			Polygon p = new Polygon();
			polygosInScreen.addLast(p);
			
			Float tempX = 0f;
			Float tempY = 0f;
			
			for(Float[] linha: linhaBD){
				tempX=30+((linha[0]-lowerX)*multX);
				tempY=350+((linha[1]-lowerY)*multY);
				p.addPoint(tempX.intValue(), tempY.intValue());
				polygonsInScreen=polygosInScreen.toArray(polygonsInScreen);
			}
			i++;
		}
	}
	
	public Polygon[] returnPoligonsInScreen(){
		return polygonsInScreen;
	}

}
