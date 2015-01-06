package weka.gui.geodata.visualizer.mouseListeners;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import weka.gui.geodata.visualizer.ShowGeoData;

public class ZoomHandler implements MouseListener{

	private int idButton;

	public ZoomHandler(int i) {
		idButton = i;
	}

	public void mouseClicked(MouseEvent me) {
		ShowGeoData sgd = (ShowGeoData) me.getComponent().getParent().getParent();
		int multiplierDirection = 1;
		Float multiplier;

		switch(idButton) {
		case 1:
			//System.out.println("Move UP");
			multiplierDirection = 1;
			multiplier = multiplierDirection * ((sgd.higherY - sgd.lowerY)/20);
			sgd.higherY	= sgd.higherY + (multiplier);
			sgd.lowerY	= sgd.lowerY + (multiplier);
			break;
		case 2:
			//System.out.println("Move Left");
			multiplierDirection = -1;
			multiplier = multiplierDirection * ((sgd.higherX - sgd.lowerX)/20);
			sgd.higherX	= sgd.higherX + (multiplier*2);
			sgd.lowerX	= sgd.lowerX + (multiplier*2);
			break;
		case 3:
			//System.out.println("Move Rigth");
			multiplierDirection = 1;
			multiplier = multiplierDirection * ((sgd.higherX - sgd.lowerX)/20);
			sgd.higherX	= sgd.higherX + (multiplier*2);
			sgd.lowerX	= sgd.lowerX + (multiplier*2);
			break;
		case 4:
			//System.out.println("Move Down");
			multiplierDirection = -1;
			multiplier = multiplierDirection * ((sgd.higherY - sgd.lowerY)/20);
			sgd.higherY	= sgd.higherY + (multiplier);
			sgd.lowerY	= sgd.lowerY + (multiplier);
			break;
		case 5:
			//System.out.println("Zoom +");
			multiplierDirection = -1;
			multiplier = multiplierDirection * ((sgd.higherX - sgd.lowerX)/20);
			sgd.higherX	= sgd.higherX + (multiplier*2);
			sgd.lowerX	= sgd.lowerX - (multiplier*2);
			sgd.higherY	= sgd.higherY + (multiplier);
			sgd.lowerY	= sgd.lowerY - (multiplier);
			break;
		case 6:
			//System.out.println("Zoom -");
			multiplierDirection = 1;
			multiplier = multiplierDirection * ((sgd.higherX - sgd.lowerX)/20);
			sgd.higherX	= sgd.higherX + (multiplier*2);
			sgd.lowerX	= sgd.lowerX - (multiplier*2);
			sgd.higherY	= sgd.higherY + (multiplier);
			sgd.lowerY	= sgd.lowerY - (multiplier);
			break;
		}
		sgd.adjustPoints();
		sgd.drawArea.repaint();
	}

	public void mouseEntered(MouseEvent arg0) {}
	public void mouseExited(MouseEvent arg0) {}
	public void mousePressed(MouseEvent arg0) {}
	public void mouseReleased(MouseEvent arg0) {}

}
