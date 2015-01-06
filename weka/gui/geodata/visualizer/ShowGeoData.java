package weka.gui.geodata.visualizer;

import weka.gui.geodata.geoDataSet.*;
import weka.gui.geodata.visualizer.mouseListeners.*;

import java.awt.*;
import java.awt.event.*;



import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;

import javax.swing.*;

public class ShowGeoData extends JPanel{	

	/**
	 * 
	 */
	private static final long serialVersionUID = 3401072518347259137L;

	// Attribute used to stop the drawing of the geographic sets while in false
	boolean syncPaint = false;

	// Attribute that store the database connection
	public Connection conn;

	// Class that should parse, validate and mount the next query
	public GeoQueryParser parser;

	// Attributes used to set the maximum and minimum size from the graphics 
	public Float lowerX	= 999999999f;
	public Float lowerY	= 999999999f;
	public Float higherX	= 0f;
	public Float higherY	= 0f;

	private Float MultX;
	private Float MultY;

	// Attribute used to see the last choosed color
	public Color lastColor	= Color.BLUE;

	// Attribute that contains a list o a set of geographic data 
	public LinkedList<GeoDataSet> geoSet;

	// Attribute that contains a list of visible tables
	public LinkedList<JButton> tableVision;
	public LinkedList<JCheckBox> tableCheckBox;

	// Attributes from the GUI
	public LinkedList<Component> fieldList;
	public Choice choTable;
	public Choice choColumn;
	public Choice choColor;
	public Choice choValues;

	public int currentButton	= 5;
	public JButton[] choJButton	= new JButton[12];
	public JButton choExecute	= new JButton("EXIT");
	public TextArea resArea		= new TextArea();

	public Label[] labelParam;
	public PaintArea drawArea; 

	// JButtons to adjust the zoom of the screen
	JButton[] screenAdjust;

	// Locations from buttons, later i have to set a equation to their positions
	final int[] JButtonZoomX = {41+5,+5,(82)+5,41+5,41+5,41+5};
	final int[] JButtonZoomY = {(25)*(1),(25)*(2),(25)*(2),(25)*(3),(25)*(4),(25)*(5)};

	public ShowGeoData(Connection connL){
		conn		= connL;
		drawArea	= new PaintArea(this);
		add(drawArea);

		parser		= new GeoQueryParser(this);

		geoSet	= new LinkedList<GeoDataSet>();
		tableVision		= new LinkedList<JButton>();
		tableCheckBox	= new LinkedList<JCheckBox>();

		screenAdjust	= new JButton[6];
		screenAdjust[0]	= new JButton("/\\");
		screenAdjust[1]	= new JButton("<");
		screenAdjust[2]	= new JButton(">");
		screenAdjust[3]	= new JButton("\\/");
		screenAdjust[4]	= new JButton("Zoom +");
		screenAdjust[5]	= new JButton("Zoom -");
		Font fontButton=new Font("Dialog",Font.PLAIN,10);
		for(int i=0;i<6;i++){
			screenAdjust[i].setMargin(new Insets(1,1,1,1));
			screenAdjust[i].setFont(fontButton);
			screenAdjust[i].addMouseListener(new ZoomHandler(i+1));
			drawArea.add(screenAdjust[i]);
		}

		labelParam		= new Label[5];
		labelParam[0]	= new Label("TABLES:");
		labelParam[1]	= new Label("COLORS:");
		labelParam[2]	= new Label("ATTRIBUTES:");
		labelParam[3]	= new Label("VALUE:");
		labelParam[4]	= new Label("QUERY:");
		Font fonteLabel=new Font("Dialog",Font.BOLD,12);
		for(int i=0;i<5;i++){
			labelParam[i].setFont(fonteLabel);
			labelParam[i].setBackground(Color.LIGHT_GRAY);
			labelParam[i].setForeground(Color.BLACK);
			add(labelParam[i]);
		}

		ItemChoiseTableHandler icth = new ItemChoiseTableHandler(this);
		choTable	= new Choice();add(choTable);
		choTable.add("Choose the table");
		choTable.addItemListener(icth);
		icth.populateChoiceTable();

		ItemChoiseColumnHandler icch = new ItemChoiseColumnHandler(this);
		choColumn	= new Choice();add(choColumn);
		choColumn.addItemListener(icch);
		ItemChoiseValuesHandler icvh = new ItemChoiseValuesHandler(this);
		choValues	= new Choice();add(choValues);
		choValues.addItemListener(icvh);

		choJButton[0]= new JButton("<");
		choJButton[1]= new JButton("<=");
		choJButton[2]= new JButton(">");	
		choJButton[3]= new JButton(">=");
		choJButton[4]= new JButton("<>");
		choJButton[5]= new JButton("=");
		choJButton[6]= new JButton("LIKE");
		choJButton[7]= new JButton("AND");
		choJButton[8]= new JButton("OR");
		choJButton[9]= new JButton("EXECUTE!");
		for(int i=0;i<choJButton.length;i++){
			if(choJButton[i]!=null){
				choJButton[i].setFont(fontButton);
				choJButton[i].setMargin(new Insets(1,1,1,1));
				choJButton[i].addMouseListener(new MouseHandler(this,i));
				add(choJButton[i]);
			}
		}

		choColor = new Choice();add(choColor);
		choColor.add("BLUE");
		choColor.add("BLACK");
		choColor.add("RED");
		choColor.add("YELLOW");
		choColor.add("CYAN");
		choColor.add("MAGENTA");
		choColor.add("GRAY");
		choColor.add("GREEN");
		choColor.addItemListener(new ItemChoiceColorHandler(this));

		add(resArea);
		resArea.addKeyListener(new KeyListener(){
			public void keyPressed(KeyEvent arg0) {
				parser.parse(resArea.getText());
			}
			public void keyReleased(KeyEvent arg0) {}
			public void keyTyped(KeyEvent arg0) {}			
		});

		Font fonteChoosers=new Font("Dialog",Font.PLAIN,11);
		choTable.setFont(fonteChoosers);
		choColor.setFont(fonteChoosers);
		choColumn.setFont(fonteChoosers);
		choValues.setFont(fonteChoosers);
	}

	public void startDataSet(String querySQL,Color ncolor,int fullySQL){
		String queryAtual	= "";
		String queryMaxSize	= "";
		String queryBufferizeGeom = "";

		syncPaint=false;
		int numBuffersToGeom = 1;

		Statement s = null;
		try{
			s = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		ResultSet vTableName2 = null;
		ResultSet vTableName3 = null;

		querySQL = querySQL.replaceAll(" from ", " FROM ");
		querySQL = querySQL.replaceAll("select ", "SELECT ");

		if(fullySQL==2){
			queryMaxSize="SELECT max(bit_length(st_astext(the_geom))) as higher FROM "+querySQL+"";
			try{
				vTableName3 = s.executeQuery(queryMaxSize);
				if(vTableName3.next()){
					numBuffersToGeom = (int) Math.floor((vTableName3.getInt("higher")/45000)+1);
					for(int i=0; i<numBuffersToGeom;i++){
						queryBufferizeGeom += "(substring(st_astext(the_geom) from "+(i*5700)+" for 5700)) as gb"+i+",";
					}
				}else{
					queryBufferizeGeom = "st_astext(the_geom) as gb1,";
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			queryBufferizeGeom = queryBufferizeGeom.substring(0,queryBufferizeGeom.length()-1);
			queryAtual="SELECT "+queryBufferizeGeom+" FROM "+querySQL+"";

			querySQL = queryAtual;
		}else if(fullySQL==1){
			queryAtual=""+querySQL+"";
			int indexOfFrom = queryAtual.indexOf(" FROM ");
			String selected = queryAtual.substring(7, indexOfFrom);
			System.out.println(selected);
			System.out.println(selected.length());
			if(selected.equals("*")){
				selected = "the_geom";
			}

			try{
				// Execute the inner attribute operations and add in a temporary table
				String queryUtil="CREATE TABLE \"container_subpieces\" as select "+selected+" as subpiece "+queryAtual.substring(indexOfFrom);
				s.execute(queryUtil);
			} catch (SQLException e) {
				e.printStackTrace();
				 //Ignore, always got error
			}

			try{
				queryMaxSize="SELECT max(bit_length(st_astext(subpiece))) as higher FROM \"container_subpieces\"";
				vTableName3 = s.executeQuery(queryMaxSize);
				if(vTableName3.next()){
					numBuffersToGeom = (int) Math.floor((vTableName3.getInt("higher")/45000) + 1);
					for(int i=0; i<numBuffersToGeom;i++){
						queryBufferizeGeom += "(substring(st_astext(subpiece) from "+(i*5700)+" for 5700)) as gb"+i+",";
					}
					queryBufferizeGeom = queryBufferizeGeom.substring(0,queryBufferizeGeom.length()-1);
				}
				queryAtual="SELECT "+queryBufferizeGeom+" FROM container_subpieces";

			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		System.out.println(fullySQL);
		System.out.println(queryMaxSize);
		System.out.println(numBuffersToGeom);
		System.out.println(queryAtual);

		int dataSize	= 0;

		long tempo = System.currentTimeMillis();
		try{
			vTableName2 = s.executeQuery(queryAtual);
			LinkedList<Float[]> BuffPointsFromDB		= new LinkedList<Float[]>();

			LinkedList<Float[]> BuffLinesFromDB		= new LinkedList<Float[]>();

			LinkedList<Float[][]> BuffPolygonsFromDB	= new LinkedList<Float[][]>();

			Float[] FloatPieces = new Float[2];

			dataSize=0;
			String dataType = "POINT";
			while(vTableName2.next()){
				String piece = "";
				for(int i=0;i<numBuffersToGeom;i++){
					String temp = vTableName2.getString("gb"+i);
					if(temp.isEmpty())
						break;
					piece=piece.concat(temp);
				}

				// populateTable(vTableName2);
				String piece1,piece2;
				if(piece.contains("MULTIPOINT")){
					dataType = "POINT";
					piece=piece.replace("MULTIPOINT(", "");
					piece=piece.replace(")", "");

					piece=piece.replace(",", "bs");
					String[] piece_blocks;
					String[] points_coord;

					piece_blocks = piece.split("bs");
					for(int pbi=0;pbi<piece_blocks.length;pbi++){
						points_coord = piece.split(" ");
						piece1=points_coord[0];
						piece2=points_coord[1];

						FloatPieces[0]=new Float(piece1);				
						FloatPieces[1]=new Float(piece2);

						adjustLimits(FloatPieces[0],FloatPieces[1]);

						BuffPointsFromDB.add(FloatPieces.clone());

						dataSize++;
					}
				}else if(piece.contains("POINT")){
					dataType = "POINT";
					piece=piece.replace("POINT(", "");
					piece=piece.replace(")", "");

					piece1=piece.substring(0,piece.indexOf(" "));
					piece2=piece.substring(piece.indexOf(" ")+1);

					FloatPieces[0]=new Float(piece1);				
					FloatPieces[1]=new Float(piece2);

					adjustLimits(FloatPieces[0],FloatPieces[1]);

					BuffPointsFromDB.add(FloatPieces.clone());

					dataSize++;
				}else if(piece.contains("MULTILINESTRING")){
					dataType = "LINE";

					if(piece.contains("MULTILINESTRING((")){

						piece=piece.replace("MULTILINESTRING((", "");
						piece=piece.replace("))", "");
						piece=piece.replace("),(", "bs");

						String[] piece_blocks;
						if(piece.contains("bs")){
							piece_blocks = piece.split("bs");
						}else{
							piece_blocks = new String[1];
							piece_blocks[0] = piece;
						}
						for(int pbi=0;pbi<piece_blocks.length;pbi++){
							String[] line_pontos = piece_blocks[pbi].split(",");

							Float[] linha_ponto = new Float[4];
							String[] linha_coord = line_pontos[0].split(" ");
							Float[] buffer_ponto = {new Float(linha_coord[0]),new Float(linha_coord[1])};

							adjustLimits(buffer_ponto[0],buffer_ponto[1]);

							for(int i=1;i<line_pontos.length;i++){
								linha_coord = line_pontos[i].split(" ");
								linha_ponto[0] = new Float(buffer_ponto[0]);
								linha_ponto[1] = new Float(buffer_ponto[1]);
								linha_ponto[2] = new Float(linha_coord[0]);
								linha_ponto[3] = new Float(linha_coord[1]);

								buffer_ponto[0] = linha_ponto[2];
								buffer_ponto[1] = linha_ponto[3];

								adjustLimits(buffer_ponto[0],buffer_ponto[1]);
								BuffLinesFromDB.add(linha_ponto.clone());
								dataSize++;
							}
						}
					}
				}else if(piece.contains("LINESTRING")){
					dataType = "LINE";
					piece=piece.replace("LINESTRING(", "");
					piece=piece.replace(")", "");

					String[] line_pontos = piece.split(",");

					Float[] linha_ponto = new Float[4];
					String[] linha_coord = line_pontos[0].split(" ");
					Float[] buffer_ponto = {new Float(linha_coord[0]),new Float(linha_coord[1])};

					adjustLimits(buffer_ponto[0],buffer_ponto[1]);

					for(int i=1;i<line_pontos.length;i++){
						linha_coord = line_pontos[i].split(" ");
						linha_ponto[0] = new Float(buffer_ponto[0]);
						linha_ponto[1] = new Float(buffer_ponto[1]);
						linha_ponto[2] = new Float(linha_coord[0]);
						linha_ponto[3] = new Float(linha_coord[1]);

						buffer_ponto[0] = linha_ponto[2];
						buffer_ponto[1] = linha_ponto[3];

						adjustLimits(buffer_ponto[0],buffer_ponto[1]);

						BuffLinesFromDB.add(linha_ponto.clone());
						dataSize++;
					}
				}else if(piece.contains("MULTIPOLYGON")){
					dataType = "POLYGON";
					if(piece.contains("MULTIPOLYGON(((")){
						piece=piece.replace("MULTIPOLYGON(((", "");
						piece=piece.replace(")))", "");
						piece=piece.replace(")),((", "bs");

						String[] piece_blocks;
						piece_blocks = piece.split("bs");
						for(int pbi=0;pbi<piece_blocks.length;pbi++){
							String[] sub_piece_blocks;
							piece_blocks[pbi]=piece_blocks[pbi].replace("),(", "bss");
							sub_piece_blocks = piece_blocks[pbi].split("bss");
							for(int pbj=0;pbj<sub_piece_blocks.length;pbj++){
								String[] pol_pontos = sub_piece_blocks[pbj].split(",");
								Float[][] pol_ponto = new Float[pol_pontos.length][2];
								for(int i=0;i<pol_pontos.length;i++){
									String[] pol_coord = pol_pontos[i].split(" ");
									pol_ponto[i][0] = new Float(pol_coord[0]);
									pol_ponto[i][1] = new Float(pol_coord[1]);
									adjustLimits(pol_ponto[i][0],pol_ponto[i][1]);
								}
								BuffPolygonsFromDB.add(pol_ponto);
								dataSize++;
							}
						}
					}
				}else if(piece.contains("POLYGON")){
					dataType = "POLYGON";
					if(piece.contains("POLYGON((")){
						piece=piece.replace("POLYGON((", "");
						piece=piece.replace("))", "");
						piece=piece.replace("),(", "bs");

						String[] piece_blocks;
						if(piece.contains("bs")){
							piece_blocks = piece.split("bs");
						}else{
							piece_blocks = new String[1];
							piece_blocks[0] = piece;
						}
						if(!piece.contains("(")){
							for(int pbi=0;pbi<piece_blocks.length;pbi++){
								String[] pol_pontos = piece_blocks[pbi].split(",");

								Float[][] pol_ponto = new Float[pol_pontos.length][2];
								for(int i=0;i<pol_pontos.length;i++){
									String[] pol_coord = pol_pontos[i].split(" ");
									pol_ponto[i][0] = new Float(pol_coord[0]);
									pol_ponto[i][1] = new Float(pol_coord[1]);
									adjustLimits(pol_ponto[i][0],pol_ponto[i][1]);
								}
								BuffPolygonsFromDB.add(pol_ponto);
								dataSize++;
							}
						}
					}
				}else{
					System.out.println("Vazio");
				}
			}
			
			System.out.println("Tempo: "+(System.currentTimeMillis()-tempo));

			if(dataType=="POLYGON"){
				geoSet.addLast(new GeoDataPolygon(querySQL,ncolor,true,dataSize));
				((GeoDataPolygon) geoSet.getLast()).addData(BuffPolygonsFromDB);
				addInView(querySQL);
			}
			if(dataType=="LINE"){
				geoSet.addLast(new GeoDataLine(querySQL,ncolor,true,dataSize));
				((GeoDataLine) geoSet.getLast()).addData(BuffLinesFromDB);
				addInView(querySQL);
			}
			if(dataType=="POINT"){
				geoSet.addLast(new GeoDataPoint(querySQL,ncolor,true,dataSize));
				((GeoDataPoint) geoSet.getLast()).addData(BuffPointsFromDB);
				addInView(querySQL);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}	

		if(fullySQL==1){
			try{
				String queryMatadora="DROP TABLE container_subpieces";
				s.execute(queryMatadora);
			} catch (SQLException e) {
				e.printStackTrace();
				// Ignore, always got error
			}
		}

	}

	private void addInView(String queryFrom){
		int indexMedio;
		int indexFinal;
		indexMedio = queryFrom.lastIndexOf("FROM")+5;
		if(queryFrom.contains("WHERE")){
			indexFinal = queryFrom.lastIndexOf("WHERE");
		}else{
			indexFinal = queryFrom.length();
		}

		String tableName = queryFrom.substring(indexMedio, indexFinal);

		System.out.println("--"+tableName+"--");

		JButton labAtual	= new JButton(tableName);
		labAtual.setMargin(new Insets(1,1,1,1));
		add(labAtual);
		tableVision.addLast(labAtual);

		JCheckBox checkAtual	= new JCheckBox();
		checkAtual.setSelected(true);
		checkAtual.addItemListener(new ListTableHandler(this,geoSet.getLast()));
		checkAtual.setBackground(Color.LIGHT_GRAY);
		add(checkAtual);
		tableCheckBox.add(checkAtual);
	}

	private void adjustLimits(Float newx, Float newy){
		if(newx<lowerX){
			lowerX=newx;
		}
		if(newx>higherX){
			higherX=newx;
		}
		if(newy<lowerY){
			lowerY=newy;
		}
		if(newy>higherY){
			higherY=newy;
		}		
	}

	public void loadPoints(String querySQL,Color ncolor,int fullySQL){
		syncPaint=false;
		startDataSet(querySQL,ncolor,fullySQL);
		adjustPoints();
		syncPaint=true;
	}

	public void adjustPoints(){
		Float diffX=higherX-lowerX;
		Float diffY=higherY-lowerY;
		if(diffX>diffY){
			diffY=diffX;
		}else if(diffY>diffX){
			diffX=diffY;
		}
		MultX=560/diffX;
		MultY=-540/diffY;
		for(int k = 0;k<geoSet.size();k++){
			if(geoSet.get(k).returnType()=="POINT"){
				((GeoDataPoint)geoSet.get(k)).reprocessGraphics(lowerX,lowerY,MultX,MultY);		
			}
			if(geoSet.get(k).returnType()=="LINE"){
				((GeoDataLine)geoSet.get(k)).reprocessGraphics(lowerX,lowerY,MultX,MultY);
			}
			if(geoSet.get(k).returnType()=="POLYGON"){
				((GeoDataPolygon)geoSet.get(k)).reprocessGraphics(lowerX,lowerY,MultX,MultY);
			}
		}
	}

	public void paint(Graphics g){
		choExecute.setLocation(920, 125);
		choExecute.setSize(70, 20);
		choExecute.repaint();

		int baseArea = 10;

		choTable.setLocation(baseArea,25);
		choTable.setSize(180, 18);
		choColor.setLocation(baseArea,60);
		choColor.setSize(180, 18);
		choColumn.setLocation(baseArea,95);
		choColumn.setSize(180, 18);
		choValues.setLocation(baseArea,130);
		choValues.setSize(180, 18);

		int indexJButton = 0;
		for(int j=0;j<2;j++){
			for(int i=0;i<7;i++){
				JButton buttonBuffer = choJButton[indexJButton];
				if(buttonBuffer!=null){
					buttonBuffer.setLocation(baseArea+190+((i%7)*72),45+60+(j*25));
					buttonBuffer.setSize(70, 20);
					buttonBuffer.repaint();
					indexJButton++;
				}
			}
		}

		resArea.setLocation(baseArea+190,25);
		resArea.setSize(500,70);

		int i=0;
		for(JButton buttonBuffer1: tableVision){
			buttonBuffer1.setSize(140, 25);
			buttonBuffer1.setLocation(5, 174+(i*35));
			buttonBuffer1.repaint();
			i++;
		}

		i=0;
		for(JCheckBox buttonBuffer2: tableCheckBox){
			buttonBuffer2.setSize(30, 25);
			buttonBuffer2.setLocation(160, 175+(i*35));
			buttonBuffer2.repaint();
			i++;
		}
		drawArea.setLocation(190, 160);
		drawArea.setSize(610, 420);
		drawArea.repaint();

		g.setColor(Color.LIGHT_GRAY);
		g.fillRect(0, 00, 800, 160);
		g.fillRect(0, 00, 190, 600);

		g.setColor(Color.BLACK);
		g.drawLine(0, 160, 800, 160);

		for(int ij=0;ij<4;ij++){
			Label labelBuffer = labelParam[ij]; 
			labelBuffer.setLocation(baseArea-1,11+(ij*35));
			labelBuffer.setSize(80,14);
			labelBuffer.repaint();
		}

		Label labelBuffer = labelParam[4];
		labelBuffer.setLocation(baseArea+190,11); 
		labelBuffer.setSize(80,14);
		labelBuffer.repaint();
	}

}
