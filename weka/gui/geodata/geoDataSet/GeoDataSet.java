package weka.gui.geodata.geoDataSet;

import java.awt.Color;

public class GeoDataSet {
	
	Color color		= Color.BLUE;
	String query	= "";
	String geoType	= "";
	int numData		= 0;
	boolean active 	= false;
	
	public GeoDataSet(String queryN,Color colorN,boolean activeN,int numDataN){
		color	= colorN;
		query	= queryN;
		geoType = "POINT";
		active	= activeN;
		numData	= numDataN;
	}

	public Color returnColor(){
		return color;
	}

	public String returnQuery(){
		String retorno = "";
		if(active){
			retorno = query;
		}
		return retorno;
	}

	public boolean isActive(){
		return active;
	}
	
	public void setActive(boolean activeN){
		active = activeN;
	}

	public int returnNumRows(){
		return numData;
	}

	public String returnType(){
		return geoType;
	}

	public void setNumRows(int numRowsN){
		numData=numRowsN;
	}

	public void setDataType(String geoTypeN){
		geoType=geoTypeN;
	}

}
