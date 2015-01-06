package weka.gui.geodata.visualizer.mouseListeners;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;

import weka.gui.geodata.geoDataSet.GeoQueryParser;
import weka.gui.geodata.visualizer.ShowGeoData;

public class MouseHandler implements MouseListener{

	private int idButton;
	ShowGeoData refMain;

	public MouseHandler(ShowGeoData nShow, int i) {
		refMain = nShow;
		idButton = i;
	}

	public void mouseClicked(MouseEvent me) {
		refMain.repaint();
		//System.out.println("button "+((Button) me.getSource()).getLabel()+" - number "+(idButton+1)+" clicked!");

		switch(idButton) {
		case 7:
			refMain.parser.addPiece((((JButton) me.getSource()).getText()));
			break;
		case 8:
			refMain.parser.addPiece((((JButton) me.getSource()).getText()));
			break;
		case 9:
			refMain.loadPoints(refMain.resArea.getText(),refMain.lastColor,1);
			refMain.adjustPoints();
			refMain.repaint(190, 160, 610, 420);
			refMain.parser = new GeoQueryParser(refMain);
			break;
		default:
			String column = refMain.choColumn.getSelectedItem();
			String values = refMain.choValues.getSelectedItem();
			if(column!=null && column!="Choose the column" && values != "Choose a value"){
				refMain.parser.addPiece(column+" "+(((JButton) me.getSource()).getText())+" '"+values+"'");
			}
			break;
		}

	}

	public void mouseEntered(MouseEvent arg0) {}
	public void mouseExited(MouseEvent arg0) {}
	public void mousePressed(MouseEvent arg0) {}
	public void mouseReleased(MouseEvent arg0) {}

}
