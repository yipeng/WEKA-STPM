package weka.gui.geodata.geoDataSet;

import java.util.LinkedList;

import weka.gui.geodata.visualizer.ShowGeoData;

public class GeoQueryParser {

	// Attributes used to parse the query
	boolean parsed;
	LinkedList<String> whereStatement;
	LinkedList<Integer> statementType;
	
	ShowGeoData refMain;
	
	public GeoQueryParser(ShowGeoData reference) {
		refMain = reference;
		
		whereStatement	= new LinkedList<String>();
		statementType	= new LinkedList<Integer>();
		parsed = true;
	}

	public void parse(String fullStatement){
		
	}
	
	public void addPiece(String statement){
		int thisType = 0;

		if((statement.equals("OR")||statement.equals("AND"))){
			thisType = 2;
			parsed = false;
		}else{
			thisType = 1;
			parsed = true;
		}

		if(whereStatement.size()==0){
			if(thisType==1){
				whereStatement.addLast(statement);
				statementType.addLast(thisType);
			}else{
				thisType=3;
			}
		}else{
			int lastStatement = statementType.getLast();
			if(thisType!=lastStatement){
				whereStatement.addLast(statement);
				statementType.addLast(thisType);
			}else{
				whereStatement.removeLast();
				whereStatement.addLast(statement);
			}
		}

		if(thisType!=3){
			String resAreaText = refMain.resArea.getText();
			resAreaText=resAreaText.substring(0,resAreaText.indexOf("WHERE"));
			String whereClause = whereStatement.toString();
			whereClause = whereClause.replace(",","");
			whereClause = whereClause.replace("[","");
			whereClause = whereClause.replace("]","");
			refMain.resArea.setText(resAreaText+"WHERE "+whereClause);
		}
	}
	
}
