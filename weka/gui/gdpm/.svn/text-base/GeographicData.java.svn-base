/***
 *	GeographicData v1.1
 *	v 1.1
 *		- Saving and loading dependences
 *		- Status bar added
 */
package weka.gui.gdpm;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.BevelBorder;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.jar.*;
import java.sql.*;
import java.lang.Runtime;


/***
 * Main class of the GDPM (Geographic Data Preprocessing Module).
 * The GeographicData class has two constructors. The first receives directly a 
 * Connection parameter and uses it to perform the required actions. The second gets
 * three connection parameters that will be used to try a Connection with the DB.
 * <p>
 * On the top of the GeographicData window the name of the DB schema must be selected.
 * After clicking on button <i>load</i>, all tables with geographic attributes will be loaded
 * into both the target feature combo and the relevant feature list.
 * <p>
 * 
 * Target feature is the geographic entity (database table) on which discovery will be
 * performed. Relevant features are geographic entities (database tables) that may have
 * an influence over the target feature.
 * <p>
 * Data can be generated at different granularity levels. Granularity level is a 
 * specialization/generalization of geographic data to be considered in
 * the mining process.
 * Feature type granularity generates data at high level. Both instances of the
 * target feature type country, Egypt and Brazil, are crossed by River (relevant 
 * feature type).
 * <p>
 * <table border=1>
 * 	<tr>
 *		<th>Target Feature (country)
 *		<th>River
 *  </tr>
 *  <tr align="center">
 *		<td>Egypt
 *		<td>CROSSES
 *  </tr>
 *  <tr align="center">
 *		<td>Brazil
 *		<td>CROSSES
 *  </tr>
 * </table>
 * <p>
 * At the feature instance granularity level, both instances of the target feature 
 * type country, Egypt and Brazil, are crossed by the instances of River
 * (relevant feature type) River_Nilo and River_Amazonas.
 * <p>
 * <table border=1>
 * 	<tr>
 *		<th>Target Feat (contry)
 *		<th>River_Nilo
 *		<th>River_Amazonas
 *  </tr>
 *  <tr align="center">
 *		<td>Egypt
 *		<td>CROSSES
 *		<td>?
 *  </tr>
 *  <tr align="center">
 *		<td>Brazil
 *		<td>?
 *		<td>CROSSES
 *  </tr>
 * </table>
 * <p>
 * The spatial relationship panel defines which spatial relationship between the target
 * feature type will be computed.
 * <p>
 * Topology will search for different types of intersection (CONTAINS,CROSSES,
 * OVERLAPS,TOUCHES,WITHIN).
 * Intersects operation checks for high level topological relationships (INTERSECTS or
 * NON-INTERSECTS)
 * Distance operation chooses the apropriate relationship according to the distance parameters
 * informed by the user. VERY_CLOSE to distance < dist_1, CLOSE to distances between dist_1 and
 * dist_2 and "?" for the FAR relationship.
 * "?" is used when there is no spatial relationship.
 * 
 * @author Andrey Tietbohl (andrey@inf.ufrgs.br)
 * @author Vania Bogorny   (bogorny@inf.ufrgs.br)*
 */
public class GeographicData extends JFrame {
	
	//GUI Variables
	//protected JTextField schema;
	protected JComboBox comboTargetFeature,schema;
	protected DefaultListModel modelRelevantFeatures;
	private JList listRelevantFeatures;
	private JRadioButton radioFeatureType,radioFeatureInstance;
	private JRadioButton radioAutomatic,radioUserSpecified;
	private JRadioButton radioBuffer,radioTopology,radioIntersects,radioDistance;
	protected JTextField textDistance1,textDistance2;
	protected JCheckBox checkUsarDependencias;
	private JButton buttonDependencias;
	private JLabel labelVeryClose,labelClose,labelFar,labelRadius;	
	private JProgressBar progressBar;
	
	private java.util.Timer timer;
	private int maxLimitBar;	//Maximum value that the progressBar can have
	private int minLimitBar;	//Minimum value that the progressBar can have
	
	//DataBase Variables
        PropertiesUtils pu = new PropertiesUtils();        
	//private String driver = "org.postgresql.Driver";
	private Connection conn;
	
	//Dependence Variables
	private JList listDependencies;	
	
	/***
	 *	Instantiates a GeographicData that uses the Connection passed as parameter to
	 *	perform the database operations
	 */
	public GeographicData(Connection c) {
		super("Geographic Data");
		init();
                
		if (c != null) {
			conn = c;
			if (!prepareDB()) {
				JOptionPane.showMessageDialog(this,"It was not possible create the apropriate functions in the data base");
			}
			loadSchemas();
		}else {
			JOptionPane.showMessageDialog(this,"Invalid Connection parameter");
			dispose();
		}
	}
	
	/***
	 *	Instantiates a GeographicData and connects to the database given the connection parameters
	 */	
	public GeographicData(String user, String pass, String url) {
		super("Geographic Data");
		init();
		try {
		    //Class.forName(driver);
		    if (user == "")
		    	conn = DriverManager.getConnection(url);
		    else
		    	conn = DriverManager.getConnection(url,user,pass);
			if (!prepareDB()) {
				JOptionPane.showMessageDialog(this,"It was not possible create the apropriate functions in the data base");
			}
			//System.err.println("aqaqaq");
            loadSchemas();
		}catch(Exception e) {
	    	e.printStackTrace();
			JOptionPane.showMessageDialog(this,e.toString());	
                //        System.err.println("aq");
	    	dispose();
	    }	    
	}
	
	/***
	 *	Loads the schema combo with the schemas existing in the database
	 */
	protected void loadSchemas() {
            try {                   
                if (pu.getbdPreName().compareTo("postgresql") == 0)	{ // postgres                        
			Statement smnt = conn.createStatement();                                
                        String sAux = pu.getSql("load_schemas");		    	
                        ResultSet rs = smnt.executeQuery(sAux);                        
		    	while (rs.next()) {
		    		schema.addItem(rs.getString(1));
		    	}                        
            }else { // oracle
			String stringURL = conn.getMetaData().getURL();
			//Gets the db name
			String sAux = getDBName(stringURL);
                        schema.addItem(sAux.substring(33));            
            }
	}	catch(SQLException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(this,"Failed loading schemas");
			}
}

	
	/***
	 *	Mounts the Geographic Data window.
	 */
	private void init() {
		//Dependence List
			DefaultListModel model = new DefaultListModel();
			listDependencies = new JList(model);
			
		//Mounts the layout
			Container container = getContentPane();
			container.setLayout(new BorderLayout());
			
			//PANEL LOAD SCHEMA
				JPanel panelSchema = new JPanel();
				panelSchema.setBorder(BorderFactory.createEtchedBorder());
				JLabel schemaLabel = new JLabel("Schema:");			
				panelSchema.add(schemaLabel);
				schema = new JComboBox();
				schema.setPreferredSize(new Dimension(150,22));
				panelSchema.add(schema);
				JButton loadSchema = new JButton("Load");
				loadSchema.addActionListener(
					new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							actionPerformedButtonLoad();
						}
					}
				);			
				panelSchema.add(loadSchema);
			
			container.add(panelSchema,BorderLayout.NORTH);

			
			//PANEL FEATURE
				JPanel panelFeature = new JPanel();
				panelFeature.setBorder(BorderFactory.createEtchedBorder());
	
				GridBagLayout gridbag = new GridBagLayout();
				panelFeature.setLayout(gridbag);
				GridBagConstraints c = new GridBagConstraints();
	
				c.fill = GridBagConstraints.BOTH;			
				c.insets = new Insets(5,10,5,10);
			
			
				//Label Target Feature
				JLabel labelTargetFeature = new JLabel("Target Feature:");
								
  				c.gridx = 0;
				c.gridy = 0;
  				panelFeature.add(labelTargetFeature,c);
				
				//JComboBox Target Feature
				comboTargetFeature = new JComboBox();
				
				c.gridx = 1;
				c.gridwidth = 2;
				c.weightx = 1.0;
				panelFeature.add(comboTargetFeature,c);
				
				//Label Relevant Features
				JLabel labelRelevantFeatures = new JLabel("Relevant Features:");				

				c.gridx = 0;
				c.gridy = 1;
				c.gridwidth = 1;
				c.weightx = 0.0;				
				panelFeature.add(labelRelevantFeatures,c);
				
				//JList	Relevant Features
				String[] data = {"one", "two", "three", "four"};
				modelRelevantFeatures = new DefaultListModel();
				listRelevantFeatures = new JList(modelRelevantFeatures);
				listRelevantFeatures.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				listRelevantFeatures.setLayoutOrientation(JList.VERTICAL);
				JScrollPane listScroller = new JScrollPane(listRelevantFeatures);
				
				c.gridx = 1;
				c.gridwidth = 2;
				c.weightx = 1.0;
				c.ipady = 60;
				panelFeature.add(listScroller,c);
				
				//CheckBox Dependences
				checkUsarDependencias = new JCheckBox("Use Dependencies");
				checkUsarDependencias.addItemListener(
						new ItemListener() {
							public void itemStateChanged(ItemEvent e) {
								if (e.getStateChange() == ItemEvent.SELECTED)
									buttonDependencias.setEnabled(true);
								else
									buttonDependencias.setEnabled(false);
							}
						}
				);
				
				c.weightx = 0.0;        		
				c.ipady = 0;
				c.gridx = 0;
				c.gridy = 2;
				c.gridwidth = 1;				
				panelFeature.add(checkUsarDependencias,c);
				
				//Button Dependences
				buttonDependencias = new JButton("Dependencies...");
				buttonDependencias.addActionListener(
					new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							actionPerformedButtonDependencias();
						}
					}
				);
				buttonDependencias.setEnabled(false);
				
				c.gridx = 1;
				c.gridwidth = 2;
				panelFeature.add(buttonDependencias,c);				
				
				//Panel Granularity
				JPanel panelGranularity = new JPanel();
				panelGranularity.setBorder(BorderFactory.createTitledBorder("Granularity Level"));
				radioFeatureType = new JRadioButton("Feature Type",true);
				radioFeatureInstance = new JRadioButton("Feature Instance",false);
				ButtonGroup group = new ButtonGroup();
				group.add(radioFeatureType);
				group.add(radioFeatureInstance);
				
        		panelGranularity.add(radioFeatureType);
        		panelGranularity.add(radioFeatureInstance);
       		
				c.weightx = 0.0;
				c.ipady = 0;        		
        		c.gridx = 0;
        		c.gridy = c.gridy + 1;
        		c.gridwidth = 3;
				panelFeature.add(panelGranularity,c);
				
				//Panel Spatial Relationships
				JPanel panelUserSpecified = new JPanel();
				panelUserSpecified.setBorder(BorderFactory.createTitledBorder("Spatial Relations"));
				radioTopology = new JRadioButton("Topology",true);
				radioIntersects = new JRadioButton("Intersects",false);
				radioDistance = new JRadioButton("Distance",false);
				radioDistance.addItemListener(
					new ItemListener() {
				    	public void itemStateChanged(ItemEvent ie){
				    	   if (ie.getStateChange() == ItemEvent.SELECTED) {
				    	   		setEnabledDistancePanel(true);
				    	   }else{
								setEnabledDistancePanel(false);
				    	   }		
				    	}
			    	}					
				);
				group = new ButtonGroup();
				group.add(radioTopology);
				group.add(radioIntersects);
				group.add(radioDistance);				
        		panelUserSpecified.add(radioTopology);
				panelUserSpecified.add(radioIntersects);
				panelUserSpecified.add(radioDistance);
       		
        		c.gridy = c.gridy + 1;
				panelFeature.add(panelUserSpecified,c);
				
				//Panel Distance Value
				JPanel panelDistanceValue = new JPanel();
				panelDistanceValue.setBorder(BorderFactory.createTitledBorder("Distance Value"));
				labelVeryClose = new JLabel("Very Close <=");
				panelDistanceValue.add(labelVeryClose);
				textDistance1 = new JTextField(3);
				textDistance1.setText("00");
				panelDistanceValue.add(textDistance1);
				labelClose = new JLabel("< Close <=");
				panelDistanceValue.add(labelClose);
				textDistance2 = new JTextField(3);				
				textDistance2.setText("00");
				panelDistanceValue.add(textDistance2);
				labelFar = new JLabel(" < Far");
				panelDistanceValue.add(labelFar);

        		c.gridy = c.gridy + 1;
				panelFeature.add(panelDistanceValue,c);
				
				//Panel Status
				JPanel statusPanel = new JPanel();
				statusPanel.setBorder(BorderFactory.createTitledBorder("Status"));				

				progressBar = new JProgressBar(0,100);
				progressBar.addChangeListener(
					new ChangeListener() {
						public void stateChanged(ChangeEvent e) {
							progressBarStateChanged();
						}
					}
				);
				progressBar.setPreferredSize(new Dimension(250,20));
				progressBar.setString("Ready");
				progressBar.setStringPainted(true);
				statusPanel.add(progressBar);
				
        		c.gridy = c.gridy + 1;
				panelFeature.add(statusPanel,c);
				
			setEnabledDistancePanel(false);
			container.add(panelFeature,BorderLayout.CENTER);
			
			//PANEL BUTTONS
				JPanel panelButtons = new JPanel();
				JButton btnOk = new JButton("OK");
				btnOk.addActionListener(
					new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							actionPerformedButtonOK();
						}
					}
				);
				JButton btnCancel = new JButton("Cancel");
				btnCancel.addActionListener(
					new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							dispose();
						}
					}
				);
				panelButtons.add(btnOk);
				panelButtons.add(btnCancel);
				
			container.add(panelButtons,BorderLayout.SOUTH);
	
		this.pack();
		this.setSize(300,540);
		this.setVisible(true);
	}	
	
	class Task extends TimerTask {
		/***
		 *	Updates the progress bar periodically
		 */
		public void run() {
			if (progressBar.getValue() < maxLimitBar) {
				if (progressBar.getValue() < minLimitBar) {
					progressBar.setValue(minLimitBar);
				}else {
					progressBar.setValue(progressBar.getValue()+1);
				}
			}
        }
	}
	
	/***
	 *	Captures the Button OK click, switching the appropriate operation to perform
	 */
	private void actionPerformedButtonOK() {
		try {
			String temporaryTable = null;
			maxLimitBar = 15;
			minLimitBar = 0;
			progressBar.setValue(0);
			timer = new java.util.Timer(true);
			timer.schedule(new Task(),2000,2000);

			if (radioTopology.isSelected()) {
				progressBar.setString("Topology Spatial Join...");
				temporaryTable = topology();
			} else if (radioDistance.isSelected()) {
				progressBar.setString("Distance Spatial Join...");
				temporaryTable = distance();
			}else if (radioIntersects.isSelected()) {
				progressBar.setString("Intersects Spatial Join...");
				temporaryTable = intersects();
			}
			
			progressBar.setString("Transformation...");
			
			transformation(temporaryTable);
			
			progressBar.setString("Ready");
			progressBar.setValue(100);
			timer.cancel();
			
		}catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this,e);
		}
	}	
	
	/***
	 *	Captures the Button Load click, updating the target feature combo and 
	 *	relevant feature list according the schema name
	 */
	protected void actionPerformedButtonLoad() {
		try{ 
    	    Statement s = conn.createStatement();
            // Load sql command
            String sAux = pu.getSql("load_tables");
            // Ajusta Metacampos
            sAux = chSqlMeta(sAux,"<SELECTED_SCHEMA>",(String) schema.getSelectedItem());
            System.out.println(sAux);
    	    
            ResultSet vTableName = s.executeQuery(sAux);
                                                                                 
		  	while ( vTableName.next() )  {/* creates a new table for each table that has objects with topological relation to vRegion */
		  		modelRelevantFeatures.addElement(vTableName.getString(1));
		  		comboTargetFeature.addItem(vTableName.getString(1));
	    	}	
		}catch (Exception vErro){
                   vErro.printStackTrace();
        }

	}	
	
	/***
	 *	Creates the auxiliary functions (like stored procedures) in a Postgrees DB.
	 *  These functions are used to improve the spatial join query.
	 */
	protected boolean prepareDB() {
		try {
			String stringURL = conn.getMetaData().getURL();
			//Gets the db name
			String dbName = getDBName(stringURL);
			System.out.println("dbName: "+dbName);

                        // Initialize Load SQL command
                        InitLoadSql();
                        //System.out.println("aq oras");
                        if (pu.getbdPreName().compareTo("postgresql") == 0)	{ // postgres
                            // Load the stored procedures
                        	BufferedReader br;
                            try{
                            	//when the jar is created
                            	JarFile jarFile = new JarFile("weka.jar");
	                            JarEntry entry = jarFile.getJarEntry("weka/gui/gdpm/res/auxFunctions.txt");
	                            InputStream input = jarFile.getInputStream(entry);
	                            InputStreamReader isr = new InputStreamReader(input);
	                            br = new BufferedReader(isr);
                            }
                            catch(FileNotFoundException e){
                            	//when programming in workspace/Eclipse
                            	br = new BufferedReader(new FileReader("src/weka/gui/gdpm/res/auxFunctions.txt"));
                        	}
                            //System.out.println("Ok, carregado");
                            String st = "";
                            String script = "";
                            while ((st = br.readLine()) != null) {
                                    script += st;
                            }

                            //CUIDADO, TEM QUE FAZER ESSA INSERÃ‡ÃƒO A MÃƒO    
                            //Runtime.getRuntime().exec("createlang plpgsql "+dbName);

                            Statement smnt = conn.createStatement();			
                            smnt.execute(script);
                            //jarFile.close();
                            br.close();
                            //System.out.println("Go coffe");
                        }
			return true;
		}catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}	
	
	/***
	 *	Returns the dbname given the url connection string
	 *  @param url The url connection
	 *  @return The database name
	 */
	private String getDBName(String url) {
		int pos1 = url.indexOf("?") != -1 ? url.indexOf("?") : url.length();
		int pos2 = url.lastIndexOf("/",pos1);
		return url.substring(pos2+1,pos1);
	}	
	
	/***
	 *	Auxiliary GUI method
	 */
	private void setEnabledUserSpecified(boolean enabled) {
		radioBuffer.setEnabled(enabled);
		radioTopology.setEnabled(enabled);
		radioIntersects.setEnabled(enabled);
		radioDistance.setEnabled(enabled);
		
		if (enabled == false) {
			setEnabledDistancePanel(false);
		}else if (radioDistance.isSelected())
			setEnabledDistancePanel(true);
	}
	
	/***
	 *	Auxiliary GUI method
	 */	
	private void setEnabledDistancePanel(boolean enabled) {
		labelVeryClose.setEnabled(enabled);
		labelClose.setEnabled(enabled);
		labelFar.setEnabled(enabled);
		textDistance1.setEnabled(enabled);
		textDistance2.setEnabled(enabled);
	}
	
	/***
	 *	Captures the Button Dependences click, calling the Dependences window
	 */
	private void actionPerformedButtonDependencias() {
		 ListModel model = listRelevantFeatures.getModel();
		 Vector vector = new Vector();

		//Inserts the relevant features into the vector
		 for (int i=0; i<model.getSize();i++) {
		 	vector.add(model.getElementAt(i));
		 }
		 
		 Dependencias d = new Dependencias(this,vector);
		 d.setSize(370,270);
		 d.show();

	}
	
	/***
	 *	Performs spatial join for topological relationships. Creates a temporary table
	 *  in the database which contains the result of this operation.
	 *
	 *	@return 	the name of the temporary table 
	 **/
	protected String topology() {
		System.out.println("Topology....");
		java.util.Date ini = new java.util.Date();
		String temporaryTable = null;
		try {

			Statement smnt = conn.createStatement();
			Statement smnt1 = conn.createStatement();
			Statement smnt2 = conn.createStatement();

			String targetF = comboTargetFeature.getSelectedItem().toString();
			String schemaText = (String) schema.getSelectedItem();
			Object[] selectedRelevantFeatures = listRelevantFeatures.getSelectedValues();
						
			//Get the name of the geometric column
			String geoColumnName;
                        // Get the SQL command
                        String sAux = pu.getSql("get_geometry_column");
                        sAux = chSqlMeta(sAux, "<TARGET_FEATURE>", targetF);
                        sAux = chSqlMeta(sAux, "<SELECTED_SCHEMA>", schemaText);
                        
                        ResultSet temp = smnt1.executeQuery(sAux); 
			temp.next();
			geoColumnName = temp.getString(2);
			String targetFeatColumn;
			
			targetFeatColumn = geoColumnName;

			//Create a temporary table
			temporaryTable = targetF+"_temp";
			progressBar.setString("Creating temporary table...");
            try{
		    	smnt.execute("create table "+targetF+"_temp (gid_"+targetF+" integer,"+
					  " relevantF varchar(25)," +
					  " relationship varchar(20) )");
            } catch (SQLException vErro){
		    	smnt.execute("drop table "+targetF+"_temp");
		    	smnt.execute("create table "+targetF+"_temp (gid_"+targetF+" integer,"+
					  " relevantF varchar(25)," +
					  " relationship varchar(20) )");
		    }
			
			int progressBarIncrementRate = (int) (55/selectedRelevantFeatures.length);
			maxLimitBar = progressBar.getValue();

			DefaultListModel model = (DefaultListModel) listDependencies.getModel();
			
			progressBar.setString("Performing spatial join...");
			for (int i=0; i<selectedRelevantFeatures.length; i++) {
				
				minLimitBar = maxLimitBar;
				maxLimitBar += progressBarIncrementRate;

				String feature = ((String) selectedRelevantFeatures[i]);
				
				if (checkUsarDependencias.isSelected() && model.contains(new Dependence(targetF,feature) ) )
					continue;
                                        // Get the SQL command
                                    sAux = pu.getSql("get_tableset");                                    
                                    sAux = chSqlMeta(sAux, "<RELEVANT_FEATURE>", feature);
                                    sAux = chSqlMeta(sAux, "<SELECTED_SCHEMA>", schemaText);
                                    System.err.println(sAux);
	      	
                                    ResultSet vTableSet = smnt2.executeQuery(sAux);
    	        /***
    	        * Compute relations
    	        */
                        vTableSet.next();
				String relevantFeatColumn;
				relevantFeatColumn = vTableSet.getString(2);
	   	      	 
	   	      	if (radioFeatureInstance.isSelected()) {
                                    // Get the SQL command
                                    sAux = pu.getSql("insert_topology_feature");
                                    
                                    sAux = chSqlMeta(sAux,"<TARGET_FEATURE_TEMP>",targetF+"_temp");
                                    sAux = chSqlMeta(sAux, "<RELEVANT_FEATURE_GID>","'"+vTableSet.getString(1)+"_'||q2.gid");
                                    sAux = chSqlMeta(sAux, "<TARGET_FEATURE_COLUMN>",targetFeatColumn);
                                    sAux = chSqlMeta(sAux,"<TARGET_FEATURE>",targetF);
                                    sAux = chSqlMeta(sAux,"<RELEVANT_FEATURE_COLUMN>",relevantFeatColumn);
                                    sAux = chSqlMeta(sAux,"<RELEVANT_FEATURE>",vTableSet.getString(1));
                                    
                                    //System.err.println("ok");
                                    //System.out.println("Aqui:\n"+sAux);
	   	      		    smnt.execute(sAux);                                
                                
 				} else {
                                    // Get the SQL command
                                    sAux = pu.getSql("insert_topology_type");
                                    sAux = chSqlMeta(sAux,"<TARGET_FEATURE_TEMP>",targetF+"_temp");
                                    sAux = chSqlMeta(sAux, "<TARGET_FEATURE_COLUMN>",targetFeatColumn);
                                    sAux = chSqlMeta(sAux,"<TARGET_FEATURE>",targetF);
                                    sAux = chSqlMeta(sAux,"<RELEVANT_FEATURE_COLUMN>",relevantFeatColumn);
                                    sAux = chSqlMeta(sAux,"<RELEVANT_FEATURE>",vTableSet.getString(1));
                                    //System.out.println("Aqui:\n"+sAux);
	   	      		    smnt.execute(sAux);                                
				}	

			}
   	    }catch (SQLException vErro) {
			vErro.printStackTrace();
 		}
		java.util.Date fim = new java.util.Date();
		java.util.Date tempo = new java.util.Date(fim.getTime()-ini.getTime());
		System.out.println("Time Topology: " +tempo.getTime()+"ms"); 		

 		return temporaryTable;
	}
	
	/***
	 *	Performs spatial join for the distance relationships. Creates a temporary
	 *	table in the database which contains the result of the operation.
	 *
	 *	@return the name of the temporary table
	 **/	
            
	protected String distance() {
		String temporaryTable = null;
	    try { 
			System.out.println("Distance....");
			java.util.Date ini = new java.util.Date();
			double vdist1=0;
	      		double vdist2=0;
      	  		vdist1 = (new Double(textDistance1.getText())).doubleValue();
	      		vdist2 = (new Double(textDistance2.getText())).doubleValue();
	      		if (vdist2 < vdist1) {
	      			textDistance2.setText(textDistance1.getText());
	      			vdist2 = vdist1;
	      		}
    			String targetF = comboTargetFeature.getSelectedItem().toString().trim();
				String schemaText = (String) schema.getSelectedItem();
				Object[] selectedRelevantFeatures = listRelevantFeatures.getSelectedValues();
				

				Statement smnt = conn.createStatement();
				Statement smnt1 = conn.createStatement(); 
				Statement smnt2 = conn.createStatement(); 
 
                        String sAux = pu.getSql("get_geometry_column");
                        sAux = chSqlMeta(sAux, "<TARGET_FEATURE>", targetF);
                        sAux = chSqlMeta(sAux, "<SELECTED_SCHEMA>", schemaText);
                        System.out.println(sAux);
                        
                        ResultSet vSchema = smnt1.executeQuery(sAux);        
                       
	      		vSchema.next();
			String targetFeatColumn;
			targetFeatColumn = vSchema.getString(2);

			//Creates a temporary table
			progressBar.setString("Creating temporary table...");
			temporaryTable = targetF+"_temp";
                  try{
			    		smnt.execute(
			    			"CREATE TABLE "+targetF+"_temp (gid_"+targetF+" integer,"+
						  		" relevantF varchar(25)," +
						  		" relationship varchar(20) )");
                      } catch (SQLException vErro){
			    		smnt.execute("DROP TABLE "+targetF+"_temp");
			    		smnt.execute("CREATE TABLE "+targetF+"_temp (gid_"+targetF+" integer,"+
						  " relevantF varchar(25)," +
						  " relationship varchar(20) )");
			    
	    	        }
	    	        
			int progressBarIncrementRate = (int) (55/selectedRelevantFeatures.length);
			maxLimitBar = progressBar.getValue();
			
			DefaultListModel model = (DefaultListModel) listDependencies.getModel();	    	        

     		for (int i=0;i<selectedRelevantFeatures.length;i++) {
     			
				minLimitBar = maxLimitBar;
				maxLimitBar += progressBarIncrementRate;     			
     			
				if (checkUsarDependencias.isSelected() && model.contains(new Dependence(targetF,selectedRelevantFeatures[i].toString().trim()) ) )
					continue;     			
     			
                                // Get the SQL command
                                sAux = pu.getSql("get_tableset");
                                sAux = chSqlMeta(sAux, "<RELEVANT_FEATURE>", selectedRelevantFeatures[i].toString().trim());
                                sAux = chSqlMeta(sAux, "<SELECTED_SCHEMA>", vSchema.getString(1));
                                System.out.println(sAux);                                

                                
                                ResultSet vTableSet = smnt.executeQuery(sAux);
                               
				vTableSet.next();
				String relevantFeatColumn;
				relevantFeatColumn = vTableSet.getString(2);
				

                        if (radioFeatureInstance.isSelected()) {
                                // Get the SQL command
                                sAux = pu.getSql("insert_distance_feature");
                                sAux = chSqlMeta(sAux,"<TARGET_FEATURE_TEMP>",targetF+"_temp");
                                sAux = chSqlMeta(sAux,"<RELEVANT_FEATURE_GID>","'"+vTableSet.getString(1)+"_'||b.gid");
                                sAux = chSqlMeta(sAux,"<TARGET_FEATURE_COLUMN>",targetFeatColumn);
                                sAux = chSqlMeta(sAux,"<TARGET_FEATURE>",targetF);
                                sAux = chSqlMeta(sAux,"<RELEVANT_FEATURE_COLUMN>",relevantFeatColumn);
                                sAux = chSqlMeta(sAux,"<RELEVANT_FEATURE>",vTableSet.getString(1));
                                sAux = chSqlMeta(sAux,"<DISTANCE_VERY_CLOSE>", String.valueOf(vdist1));
                                sAux = chSqlMeta(sAux,"<DISTANCE_CLOSE>",String.valueOf(vdist2));
                                
                                System.out.println(sAux);      	    		
                                
                                smnt2.execute(sAux);
                                
			}else {
                                 // Get the SQL command
                                sAux = pu.getSql("insert_distance_type");
                                sAux = chSqlMeta(sAux,"<TARGET_FEATURE_TEMP>",targetF+"_temp");
                                sAux = chSqlMeta(sAux,"<TARGET_FEATURE_COLUMN>",targetFeatColumn);
                                sAux = chSqlMeta(sAux,"<TARGET_FEATURE>",targetF);
                                sAux = chSqlMeta(sAux,"<RELEVANT_FEATURE_COLUMN>",relevantFeatColumn);
                                sAux = chSqlMeta(sAux,"<RELEVANT_FEATURE>",vTableSet.getString(1));
                                sAux = chSqlMeta(sAux,"<DISTANCE_VERY_CLOSE>", String.valueOf(vdist1));
                                sAux = chSqlMeta(sAux,"<DISTANCE_CLOSE>",String.valueOf(vdist2));
                                
                                System.out.println(sAux);
                                
                                smnt2.execute(sAux);
                                					
                    }
						
	     	}//end for
	     		
			java.util.Date fim = new java.util.Date();
			java.util.Date tempo = new java.util.Date(fim.getTime()-ini.getTime());
			System.out.println("Time Distance: " +tempo.getTime()+"ms"); 			     		
		}catch (SQLException e) {
				e.printStackTrace();
		}
		return temporaryTable;
	}

	/***
	 *	Performs spatial join for intersects operation. Creates a temporary table
	 *	in the database which contains the result of the operation.
	 *
	 *	@return the name of the temporary table
	 **/
	protected String intersects() {
		String temporaryTable = null;
		try {
			System.out.println("Intersects....");
			java.util.Date ini = new java.util.Date();
						
			Statement smnt = conn.createStatement();
			Statement smnt2 = conn.createStatement();			
			String targetF = comboTargetFeature.getSelectedItem().toString();
			String schemaText = (String) schema.getSelectedItem();
			Object[] selectedRelevantFeatures = listRelevantFeatures.getSelectedValues();
			
			//Get the name of the geometric column
			String geoColumnName;
                        
                        String sAux = pu.getSql("get_geometry_column");
                        sAux = chSqlMeta(sAux, "<TARGET_FEATURE>", targetF);
                        sAux = chSqlMeta(sAux, "<SELECTED_SCHEMA>", schemaText);
                        System.out.println(sAux);
                        ResultSet temp = smnt.executeQuery(sAux);
                        
                        temp.next();
			geoColumnName = temp.getString(2);
			String targetFeatColumn;
			//For not using the 'envelope' function
			//if (temp.getString("type").compareTo("POINT")==0 || true)
				targetFeatColumn = geoColumnName;
			//else
			//	targetFeatColumn = "envelope("+geoColumnName+")";			
			
			//Creates a temporary table
			progressBar.setString("Creating temporary table...");
			temporaryTable = targetF+"_temp";
            try{
		    		smnt.execute(
		    			"CREATE TABLE "+targetF+"_temp (gid_"+targetF+" integer,"+
					  		" relevantF varchar(25)," +
					  		" relationship varchar(20) )");
            } catch (SQLException vErro){
		    		smnt.execute("DROP TABLE "+targetF+"_temp");
		    		smnt.execute("CREATE TABLE "+targetF+"_temp (gid_"+targetF+" integer,"+
					  " relevantF varchar(25)," +
					  " relationship varchar(20) )");
		    
  	        }
			
			int progressBarIncrementRate = (int) (55/selectedRelevantFeatures.length);
			maxLimitBar = progressBar.getValue();
			
			DefaultListModel model = (DefaultListModel) listDependencies.getModel();
			
			for (int i=0; i<selectedRelevantFeatures.length; i++) {
				
				minLimitBar = maxLimitBar;
				maxLimitBar += progressBarIncrementRate;
								
				String feature = ((String) selectedRelevantFeatures[i]).trim();
				
				if (checkUsarDependencias.isSelected() && model.contains(new Dependence(targetF,feature) ) )
					continue;
                                                                // Get the SQL command
                                sAux = pu.getSql("get_tableset");
                                sAux = chSqlMeta(sAux, "<RELEVANT_FEATURE>", feature);
                                sAux = chSqlMeta(sAux, "<SELECTED_SCHEMA>", schemaText);
                                System.out.println(sAux);                                

                                ResultSet vTableSet = smnt.executeQuery(sAux);

	          // Computes relationships
	   	      	vTableSet.next();

				String relevantFeatColumn;
				//if (vTableSet.getString("type").compareTo("POINT")==0 || true)
					relevantFeatColumn = vTableSet.getString(2);
				//else
				//	relevantFeatColumn = "envelope("+vTableSet.getString(2)+")";	   	      	
						
					if (radioFeatureInstance.isSelected()) {
                                                
                                                sAux = pu.getSql("insert_intersects_feature");
                                                sAux = chSqlMeta(sAux,"<TARGET_FEATURE_TEMP>",targetF+"_temp");
                                                sAux = chSqlMeta(sAux,"<RELEVANT_FEATURE_GID>","'"+vTableSet.getString(1)+"_'||b.gid");
                                                sAux = chSqlMeta(sAux,"<TARGET_FEATURE_COLUMN>",targetFeatColumn);
                                                sAux = chSqlMeta(sAux,"<TARGET_FEATURE>",targetF);
                                                sAux = chSqlMeta(sAux,"<RELEVANT_FEATURE_COLUMN>",relevantFeatColumn);
                                                sAux = chSqlMeta(sAux,"<RELEVANT_FEATURE>",vTableSet.getString(1));
                                                System.out.println(sAux);    
                                                smnt2.execute(sAux);
                                                
					}else {	//Feature Type
                                            
                                                sAux = pu.getSql("insert_intersects_type");
                                                sAux = chSqlMeta(sAux,"<TARGET_FEATURE_TEMP>",targetF+"_temp");
                                                sAux = chSqlMeta(sAux,"<RELEVANT_FEATURE_GID>","'"+vTableSet.getString(1)+"_'||b.gid");
                                                sAux = chSqlMeta(sAux,"<TARGET_FEATURE_COLUMN>",targetFeatColumn);
                                                sAux = chSqlMeta(sAux,"<TARGET_FEATURE>",targetF);
                                                sAux = chSqlMeta(sAux,"<RELEVANT_FEATURE_COLUMN>",relevantFeatColumn);
                                                sAux = chSqlMeta(sAux,"<RELEVANT_FEATURE>",vTableSet.getString(1));
                                                System.out.println(sAux);    
                                                smnt2.execute(sAux);
					}
			}

			java.util.Date fim = new java.util.Date();
			java.util.Date tempo = new java.util.Date(fim.getTime()-ini.getTime());
			System.out.println("Time Intersects: " +tempo.getTime()+"ms");			
		}catch (SQLException e) {
			e.printStackTrace();
		}
		
		return temporaryTable;
	}
	
	/***
	 *	Captures the State Changed event of the progress bar, refreshing the window
	 */
	public void progressBarStateChanged()	{
		Graphics g = getGraphics();
		if (g != null)
			//paintComponent(g);
			update(g);
		else
			repaint();
	}
	
	/***
	 *	Performs the transformation operation, creating the 'geographic_data.arff' file in
	 *	the /data wekaÃ¯Â¿Â½s directory.
	 *
	 *	@param table_name The name of the temporary table created by the topology|intersects|distance method
	 **/	
	protected void transformation(String table_name) throws Exception {
		Hashtable newColumns = new Hashtable();
		Hashtable gid = new Hashtable();
		boolean foundRelationships = true;
		
		java.util.Date ini = new java.util.Date();
		System.out.println("Transformation...");
		progressBar.setValue(60);
		minLimitBar = 60;
		maxLimitBar = 90;		
		
			String[] labels;
			Statement smnt = conn.createStatement();
			ResultSet rs = smnt.executeQuery("SELECT count(*) FROM "+table_name);
			rs.next();
			foundRelationships = rs.getInt(1) > 0 ? true : false;
		
			rs = smnt.executeQuery("SELECT * FROM "+table_name);
			Hashtable temp = new Hashtable();
			int indexValue=0;	//Index of the gids
			int indexValueCols=0;	//Index of the columns
			while (rs.next()) {
				String relevantF = rs.getString("relevantF");
				int gid_int = rs.getInt(1);
				
				if (radioFeatureInstance.isSelected()) {
					if (!newColumns.containsKey(relevantF) ) {
						newColumns.put(relevantF,new Integer(indexValueCols++));
					}
				}else {
					if (!temp.containsKey(relevantF)) {
						temp.put(relevantF,new Integer(indexValueCols++));
					}					
				}

				if (!gid.containsKey(new Integer(gid_int))) {
					gid.put(new Integer(gid_int),new Integer(indexValue++));
				}
			}
			
			if (radioTopology.isSelected()) {
				String[] l = {"CONTAINS","TOUCHES","WITHIN","OVERLAPS","CROSSES",
                                              "COVEREDBY","COVERS","INSIDE","ON","OVERLAPBDYDISJOINT",
                                              "OVERLAPBDYINTERSECT","TOUCH"};
				labels = l;
			}else if (radioDistance.isSelected()) {
				String[] l = {"VERY_CLOSE","CLOSE"};
				labels = l;
			}else {		//radioIntersects
				String[] l = {"INTERSECTS"};
				labels = l;
			}
			
			Enumeration e = temp.keys();
			
			if (radioFeatureType.isSelected()) {
				int index=0;
				while (e.hasMoreElements()) {
					String relevant = (String) e.nextElement();
					for (int j=0;j<labels.length;j++) {
						newColumns.put(labels[j] + "_" + relevant,new Integer(index++));
					}
				}
			}
				
			//Create the array structure
			String[][] table = new String[gid.size()][newColumns.size()];
			
			//Fills the structure
			for (int i=0;i<gid.size();i++)
				for (int j=0;j<newColumns.size();j++)
					table[i][j] = "?";			//Inicialize the structure with '?'
			
			int gid_index,cIndex;
                        
			// rs.beforeFirst();
			rs = smnt.executeQuery("SELECT * FROM "+table_name);                        
                        
			while (rs.next()) {
				if (rs.getString(3).compareTo("?")!=0) {
					gid_index = ((Integer) gid.get(new Integer(rs.getInt(1)))).intValue();
					
					if (radioFeatureInstance.isSelected()) {
						cIndex = ((Integer) newColumns.get(rs.getString(2))).intValue();
						table[gid_index][cIndex] = rs.getString(3);
					}else {
						cIndex = ((Integer) newColumns.get(rs.getString(3)+"_"+rs.getString(2))).intValue();
						table[gid_index][cIndex] = "yes";
					
						//Consists the structure in the distance case
						if (radioDistance.isSelected()) {
							if (rs.getString(3).compareTo(labels[0])==0) {//=='Very_Close'
								//Put ? in close, becouse very_close is yes
								int iClose = ((Integer) newColumns.get(labels[1]+"_"+rs.getString(2))).intValue();
								table[gid_index][iClose] = "?";
							}else {	//=='Close'
								int iVeryClose = ((Integer) newColumns.get(labels[0]+"_"+rs.getString(2))).intValue();
								//If very_close is 'yes' put ? in close
								if (table[gid_index][iVeryClose] == "yes")
									table[gid_index][cIndex] = "?";
							}
						}
					}
				}				
			}
			
			//GENERATE THE .ARFF FILE
			if (!foundRelationships) {
				JOptionPane.showMessageDialog(this,"No relationships found");
			}else {
				progressBar.setString("Generating arff file...");
				minLimitBar = 90;
				maxLimitBar = 100;				
				
                                File f = new File("data");
                                if (!f.exists()) f.mkdir();
                                
				FileWriter fw = new FileWriter("data/"+table_name+"_geographic_data.arff");
				BufferedWriter bw = new BufferedWriter(fw);
				
				bw.write("@relation geographic_data");
				bw.newLine();bw.newLine();
				
				String targetFeature = comboTargetFeature.getSelectedItem().toString().trim();
				
				//Write the normal attributes into the file
				DatabaseMetaData metaData = conn.getMetaData();
				ResultSet columnsTarget = metaData.getColumns(null,(String) schema.getSelectedItem(),targetFeature,null);
				Vector columns = new Vector();
				String discretization;
				while(columnsTarget.next()) {
					if (columnsTarget.getInt(5) != 1111) {	//Geometry type
						bw.write("@attribute "+columnsTarget.getString(4)+" ");	//Write the name of the column
						String type = IntToType(columnsTarget.getInt(5));
						if (type == "string") {
	                                                //Discretizes the string
							Statement stment = conn.createStatement();							
                                                        ResultSet rsTemp = stment.executeQuery("SELECT DISTINCT "+columnsTarget.getString(4)+" FROM "+targetFeature);
							//discretization = "{null,"; //mudado por gabriel
                                                        discretization = "{?,";
							while(rsTemp.next()) {                                                                
								if (rsTemp.getString(1) != null)
									discretization += rsTemp.getString(1).replaceAll("\\W","_") + ",";
							}
							discretization = discretization.substring(0,discretization.length()-1) + "}";
							bw.write(discretization);
						}else {
							bw.write(type);	//Write the type
						}
						bw.newLine();
						columns.addElement(columnsTarget.getString(4));
					}
				}
				//Write the new attributes into the file
				e = newColumns.keys();
				while (e.hasMoreElements()) {
					String relevant = (String) e.nextElement();
					if (radioFeatureType.isSelected())
						bw.write("@attribute "+relevant+ " {yes}");
					else if (radioTopology.isSelected())
						bw.write("@attribute "+relevant+ " {CONTAINS,TOUCHES,WITHIN,OVERLAPS,CROSSES,COVEREDBY,COVERS,INSIDE,ON,OVERLAPBDYDISJOINT,OVERLAPBDYINTERSECT,TOUCH}");
					else if (radioIntersects.isSelected())
						bw.write("@attribute "+relevant+ " {INTERSECTS}");
					else
						bw.write("@attribute "+relevant+ " {CLOSE,VERY_CLOSE}");				
					bw.newLine();
				}
				bw.newLine();bw.newLine();
				bw.write("@data");
				bw.newLine();
				
				
				//Fills the data
				Statement stmnt = conn.createStatement();
				ResultSet rset = stmnt.executeQuery("SELECT * FROM "+targetFeature);
				int ind;
				int index=0;
				while (rset.next()) {
					String line = "";
					for (int i=0;i<columns.size();i++) {
						String column = (String) columns.elementAt(i);
						String value = rset.getString(column);
						//Replaces ',' to '.' case a number
						if (value == null)
							//value = "null"; mudado por gabriel							
                                                        value = "?";
						else 
							try {
								Double.valueOf(value);
							}catch (NumberFormatException parseError) {
								value = value.replaceAll("\\W","_");
							}
							
						line += value +",";
					}
					
					index = rset.getInt("gid");
					Integer gidGradeIndex = (Integer) gid.get(new Integer(index));
					if (gidGradeIndex != null) {
						index = ((Integer) gid.get(new Integer(index))).intValue();
						e = newColumns.keys();
						while (e.hasMoreElements()) {
							String relevant = (String) e.nextElement();
							ind = ((Integer) newColumns.get(relevant)).intValue();
							line += table[index][ind] + ",";
						}
					}else {	//Fill with "?" when the target feature instance has no relationship with a relevant feature instance
						for (int i=0;i<newColumns.size();i++)
							line += "?,";
					}
					line = line.substring(0,line.length()-1);	//removes the last ','
					bw.write(line);
					bw.newLine();
				}
				bw.flush();
				bw.close();
			
				JOptionPane.showMessageDialog(this,"File 'geographic_data.arff' generated in 'data' directory");			
			}
			
			java.util.Date fim = new java.util.Date();
			java.util.Date tempo = new java.util.Date(fim.getTime()-ini.getTime());
			System.out.println("Tempo Transformation: " +tempo.getTime()+"ms");			
	}
	
	/***
	 *	Converts the integer that represents the attribute type in the DB to a String.
	 *	The String is used as type in the attributes in the .arff file.
	 *	The two possible strings returned are 'string' and 'numeric'
	 */
	private String IntToType(int intType) {
		switch (intType) {
//			case 8: return "real";
			case 12: return "string";
			default: return "numeric";
		}
	}	
        
        private void InitLoadSql(){
            try {
		//Gets the db name
                String dbName = conn.getMetaData().getURL();
		//String dbName = getDBName(stringURL);
                
                if (dbName.contains("oracle"))
                    pu.setbdPreName("oracle") ;
                else if (dbName.contains("postgresql"))
                    pu.setbdPreName("postgresql");
                else pu.setbdPreName("");
		System.out.println(pu.getbdPreName());
                // Gets the SQL instructions                
		
            }catch(Exception e) {
	    	e.printStackTrace();
			JOptionPane.showMessageDialog(this,e.toString());
            }
        }
        
        private String chSqlMeta(String sSql, String sMeta, String sVal) {
            try {
                if (sSql.contains(sMeta)){
                     sSql = sSql.substring(0, sSql.indexOf(sMeta)) +sVal+sSql.substring(sSql.indexOf(sMeta)+sMeta.length());
                     if (sSql.contains(sMeta)) 
                        {
                         sSql = chSqlMeta(sSql, sMeta, sVal);
                        }
               }  
               return sSql;
            } catch(Exception e) {
	    	e.printStackTrace();
                return sSql;
            }
        }
	
	class Dependencias extends JDialog {
		private static final String DEPENDENCE_TABLE = "knowledgeConstraints";
		
		JComboBox comboDependencia1,comboDependencia2;
		private JButton btnSave;
		private Vector modifications = new Vector();	//Used to recover in case the user press cancel
		
		/***
		 *	Creates the dependence window with 'frame' as owner. 
		 *	'vector' contains the elements that will appear in the combo boxes.
		 */
		public Dependencias(JFrame frame,Vector vector) {
			super(frame,"Dependences",true);
			
			Container container = this.getContentPane();
			container.setLayout(new GridBagLayout());
			GridBagConstraints k = new GridBagConstraints();
			k.insets = new Insets(5,5,5,5);			
			k.fill = GridBagConstraints.HORIZONTAL;
			
			//JCombos e button 'add'
				comboDependencia1 = new JComboBox(vector);
				comboDependencia2 = new JComboBox(vector);
				
				k.gridy = 0;k.gridx = 0;k.weightx = 0.5;				
			container.add(comboDependencia1,k);
				k.gridx = 1;
			container.add(comboDependencia2,k);			
			
				JButton buttonAdd = new JButton("Add");
				buttonAdd.addActionListener(
					new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							actionPerformedButtonAdd();
						}
					}
				);
				k.gridx = 2;k.weightx = 0.0;
			container.add(buttonAdd,k);
			
			//JPane
				JScrollPane scrollDependencies = new JScrollPane(listDependencies);
			
				k.gridy = 1;k.gridwidth = 2;k.gridx=0;k.weightx = 1.0;
			container.add(scrollDependencies,k);
			
			//Button 'remove'
				JButton buttonRemove = new JButton("Remove");
				buttonRemove.addActionListener(
					new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							actionPerformedButtonRemove();
						}
					}
				);
				
				k.gridx = 2;k.gridwidth = 1;k.weightx = 0.0;
				k.anchor = GridBagConstraints.PAGE_START;				
			container.add(buttonRemove,k);
			
			//Buttons OK e Cancel
				JPanel panelBtns = new JPanel();
				JButton btnLoad = new JButton("Load");
				btnLoad.addActionListener(
					new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							actionPerformedDependencesButtonLoad();
							btnSave.setEnabled(false);
						}
					}					
				);
				btnSave = new JButton("Save");
				btnSave.setEnabled(false);
				btnSave.addActionListener(
					new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							actionPerformedDependencesButtonSave();
						}
					}					
				);
				JButton btnOk = new JButton("OK");
				btnOk.addActionListener(
					new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							actionPerformedButtonOK();
						}
					}
				);
				JButton btnCancel = new JButton("Cancel");
				btnCancel.addActionListener(
					new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							actionPerformedButtonCancel();
							dispose();
						}
					}
				);
				panelBtns.add(btnLoad);
				panelBtns.add(btnSave);
				panelBtns.add(btnOk);
				panelBtns.add(btnCancel);
				
				this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
				this.addWindowListener(
					new WindowAdapter() {
						public void windowClosed(WindowEvent e) {
							actionPerformedButtonCancel();
						}
					}					
				);
				
				k.gridx=0;k.gridy=2;k.gridwidth = 3;k.anchor = GridBagConstraints.CENTER;
			container.add(panelBtns,k);
				
		}
		
		/***
		 *	Loads the dependences list from the DB if it already exists.
		 *	The database table that stores the dependences is named 'knowledgeConstraints'
		 */
		protected void actionPerformedDependencesButtonLoad() {
			boolean found = false;
			DefaultListModel model = (DefaultListModel) listDependencies.getModel();
			try {
				Statement smnt = conn.createStatement();
				try {
					Dependence dep;
					ResultSet rs = smnt.executeQuery("SELECT * FROM "+DEPENDENCE_TABLE);
					while (rs.next()) {
						found = true;
						dep = new Dependence(rs.getString(1),rs.getString(2));
						if (!model.contains(dep)) {
							model.add(0,dep);
							modifications.addElement(dep);
						}
					}
				}catch(SQLException noTable) {}
				
				if (!found) {
					JOptionPane.showMessageDialog(this,"No dependences found");
				}
			}catch(SQLException e) {
				e.printStackTrace();
			}
		}
		
		/***
		 *	Saves the current dependence list in the DB.
		 *	The database table that stores the dependences is named 'knowledgeConstraints'
		 *	Also creates a file named "dependecies_gdpm.txt" with that information
		 */
		protected void actionPerformedDependencesButtonSave() {
			File f = new File("data");
	        if (!f.exists()) f.mkdir();
	        File depend_file = new File("data/dependecies_gdpm.txt");
			try {		        
				FileWriter dfw = new FileWriter(depend_file);
				BufferedWriter dbw = new BufferedWriter(dfw);
				Statement smnt = conn.createStatement();
				try {
			    	smnt.execute("CREATE TABLE "+DEPENDENCE_TABLE+" ("+
			    					"featureTypeName1 varchar,"+
			    					"featureTypeName2 varchar" +
			    				 ")");		
				}catch (SQLException erro) {
					smnt.execute("DROP TABLE "+DEPENDENCE_TABLE);
			    	smnt.execute("CREATE TABLE "+DEPENDENCE_TABLE+" ("+
			    					"featureTypeName1 varchar,"+
			    					"featureTypeName2 varchar" +
			    				 ")");					
				}
				DefaultListModel model = (DefaultListModel) listDependencies.getModel();
				Enumeration enu = model.elements();
				StringBuffer buf = new StringBuffer();
				while(enu.hasMoreElements()) {
					Dependence dep = (Dependence) enu.nextElement();
					buf.append("INSERT INTO "+DEPENDENCE_TABLE+" VALUES ('"+dep.a+"','"+dep.b+"');");
					dbw.write("{ "+dep.a+","+dep.b+" }"); dbw.newLine();
				}
				dbw.flush();
				dbw.close();
				if (buf.length() > 0) {
					smnt.execute(buf.toString());
					JOptionPane.showMessageDialog(this,"Dependences saved");
				}else {
					JOptionPane.showMessageDialog(this,"No dependences for saving");
				}
			}catch(SQLException e) {
				e.printStackTrace();
			}
			catch (IOException e) {					
				e.printStackTrace();
			}
		}		
		
		/***
		 *	Adds the dependence which is selected in the combos
		 */
		protected void actionPerformedButtonAdd() {
			DefaultListModel model = (DefaultListModel) listDependencies.getModel();
			
			Dependence dep = new Dependence();
			dep.a = (String) comboDependencia1.getSelectedItem();
			dep.b = (String) comboDependencia2.getSelectedItem();
			if (!model.contains(dep)) {
				model.add(0,dep);
				modifications.addElement(dep);
				btnSave.setEnabled(true);
			}else
				JOptionPane.showMessageDialog(this,"Dependence already exists!");
		}
		
		/***
		 *	Removes the selected dependences from the list
		 */
		protected void actionPerformedButtonRemove() {
			DefaultListModel model = (DefaultListModel) listDependencies.getModel();
			
			while (!listDependencies.isSelectionEmpty()) {
				model.remove(listDependencies.getSelectedIndex());
				btnSave.setEnabled(true);
			}
		}		
		
		/***
		 *	Confirms any modification made since the window was openned. This method does not save the dependences list in the DB
		 */
		protected void actionPerformedButtonOK() {
			modifications.clear();
			dispose();
		}
		
		/***
		 *	Cancels any modification made since the window was openned
		 */
		protected void actionPerformedButtonCancel() {
			DefaultListModel model = (DefaultListModel) listDependencies.getModel();
			
			for (int i=0;i<modifications.size();i++) {
				Dependence dep = (Dependence) modifications.elementAt(i);
				
				if (model.contains(dep)) {
					model.removeElement(dep);
				}
			}
			
		}		
	}
}

class Dependence {
	String a,b;	//(a,b)
	
	/***
	 * Creates a dependence (a,b)
	 */
	public Dependence(String a, String b) {
		this.a = a;
		this.b = b;
	}
	
	/***
	 *	Creates a single instance of Dependence without parameters
	 */
	public Dependence() {
	}
	
	/***
	 *	Returns a string that represents a pair of geographic entities with a dependence.
	 *  It is like '(a,b)' where a,b are the geographic entity names
	 *
	 * @return The string format of this object
	 */
	public String toString() {
		return ("( " + a + ", " + b + " )");
	}
	
	/***
	 *	Two Dependence instances are considered equal if the toString() method returns the same string and if
	 *	they belong to the Dependence class.
	 */
	public boolean equals(Object obj) {
		if (this.getClass() == obj.getClass()) {
			Dependence dep = (Dependence) obj;
			return (this.a.compareTo(dep.a) == 0 && this.b.compareTo(dep.b)==0 );
		}else
			return false;
	}
}

