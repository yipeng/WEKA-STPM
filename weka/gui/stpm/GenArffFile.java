/*
 * GenArffFile.java
 *
 * Created on September 15, 2008, 4:17 PM
 */

package weka.gui.stpm;

import java.sql.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import weka.gui.stpm.seqpattern.Struct;
import weka.gui.stpm.seqpattern.Sequence;
import weka.gui.stpm.seqpattern.SemanticTrajectory;
import weka.gui.stpm.seqpattern.SeqPatternResult;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.*;
import java.util.*;
import java.io.*;

/** Frame and methods do generate an arff file (Vertical or Horizontal) with a stop 
 * 	table previously created with SMoT or CB-SMoT.
 * 
 * 	Also there's a Sequential Pattern module, with the stop table or the vertical arff file
 * 	created here (with the name of 'stopsV.arff') 
 *
 * 
 */
public class GenArffFile extends JDialog {
    private Connection conn;
    private DefaultListModel listModel = new DefaultListModel();    
    private boolean type;
    
    private javax.swing.ButtonGroup buttonGroupItem;
    private javax.swing.ButtonGroup buttonGroupTime;
    private javax.swing.JButton jButtonDefine;
    private javax.swing.JButton jButtonGenArff;
    private javax.swing.JButton jButtonLoad;
    private javax.swing.JComboBox jComboBoxST;
    private javax.swing.JComboBox jComboBoxSchema;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JRadioButton jRadioButtonDayWeek;
    private javax.swing.JRadioButton jRadioButtonMonth;
    private javax.swing.JRadioButton jRadioButtonNEnd;
    private javax.swing.JRadioButton jRadioButtonNStart;
    private javax.swing.JRadioButton jRadioButtonNStartEnd;
    private javax.swing.JRadioButton jRadioButtonNameOnly;
    private javax.swing.JRadioButton jRadioButtonUserDef;
    private javax.swing.JRadioButton jRadioButtonWeek;
    private javax.swing.JRadioButton jRadioButtonYear;    
    
    private javax.swing.JRadioButton jRBHor;
    private javax.swing.JRadioButton jRBVert;
    private javax.swing.ButtonGroup buttongrouparff; 
    
    private javax.swing.JTextField jTextFieldMinSupport;
    private javax.swing.JButton jButtonExtractPattern;
    private javax.swing.JCheckBox jCheckBoxBD;
    
    /** Creates new form GenArffFile */
    public GenArffFile(Connection conn,boolean t) {
        this.setTitle("Generate Arff File");
        this.conn = conn;
        this.type = t;
        initComp();
        loadSchemas();
        this.pack();        
		this.setVisible(true);
    }
    
    /** Tests the form of GenArffFile*/
    public GenArffFile(){
    	this.setTitle("Generate Arff File");
    	initComp();
    	this.pack();
		this.setVisible(true);
    }
    
    /**Load the information about stop_tables in the DB*/
    private void loadSchemas() {
        try {
            Statement smnt = conn.createStatement();
            ResultSet rs = smnt.executeQuery("SELECT DISTINCT f_table_schema FROM geometry_columns");
            while (rs.next()) {
                jComboBoxSchema.addItem(rs.getString(1));
            }
        }catch(SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,"Failed loading schemas");
        }		
    }    
    /**Formats the parameter name with informations for using in Sequential Patterns.
     * 
     * @param name			The string to be formatted.
     * @param startTime		The inicial time to be used.
     * @param endTime		The final time to be used.
     * @param dowStart		
     * @param dowEnd
     * @return				The string formatted.
     */
    private String format(String name,Timestamp startTime, Timestamp endTime, int dowStart, int dowEnd) {
        String format = name;
        boolean timeStart=false,timeEnd=false;
        if (jRadioButtonNStart.isSelected()) {
            timeStart = true;
        }else if (jRadioButtonNEnd.isSelected()) {
            timeEnd = true;    
        }else if (jRadioButtonNStartEnd.isSelected()) {
            timeStart = true;
            timeEnd = true;
        }
        GregorianCalendar calStart = new GregorianCalendar();
        calStart.setTimeInMillis(startTime.getTime());
        GregorianCalendar calEnd = new GregorianCalendar();
        calEnd.setTimeInMillis(endTime.getTime());
        
        if (!jRadioButtonNameOnly.isSelected()) {
            if (jRadioButtonYear.isSelected()) {
                if (timeStart)
                    format += "_" + calStart.get(Calendar.YEAR);
                if (timeEnd)
                    format += "_" + calEnd.get(Calendar.YEAR);
            }else if (jRadioButtonMonth.isSelected()) {
                if (timeStart)
                    format += "_" + calStart.get(Calendar.MONTH);
                if (timeEnd)
                    format += "_" + calEnd.get(Calendar.MONTH);
            }else if (jRadioButtonWeek.isSelected()) {
                if (timeStart)
                    format += "_" + ((dowStart == 0 || dowStart == 6) ? "weekend" : "weekday");
                if (timeEnd)
                    format += "_" + ((dowEnd == 0 || dowEnd == 6) ? "weekend" : "weekday");
            }else if (jRadioButtonDayWeek.isSelected()) {
                String[] week = {"sunday","monday","tuesday","wednesday","thursday","friday","saturday"};
                if (timeStart)
                    format += "_" + week[dowStart];
                if (timeEnd)
                    format += "_" + week[dowEnd];
            }else {
                if (timeStart)
                    format += "_" + selectInterval(startTime);
                if (timeEnd)
                    format += "_" + selectInterval(endTime);
            }
        }
       
        return format;
    }
    /**Converts a Timestamp time for a time in a String form.
     * 
     * @param time		The time to be converted.
     * @return			The string formatted.
     */
    private String selectInterval(Timestamp time) {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(time.getTime());
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get(Calendar.MINUTE);
        
        for (int i=0;i<listModel.size();i++) {
            Struct s = (Struct) listModel.elementAt(i);
            //System.out.print(hour + ":" +min +"\t");
            if (s.timeIsIn(hour,min)) {
                //System.out.println(s.toString2());
                return s.toString2();
            }
        }
        return "other";
    }
    
    // Old implementation of horizontal generating arff file
    private void generateArffStopFile()  throws IOException, SQLException {
        File stopsFile = new File("./stops.arff");
        FileWriter sfw = new FileWriter(stopsFile);
        BufferedWriter sbw = new BufferedWriter(sfw);
       
        Statement s = conn.createStatement();
        String sql = "SELECT pk,unknown,the_geom,tid,stop_name,start_time,end_time,Extract(dow from start_time) as dowStart,Extract(dow from end_time) as dowEnd " +
                     "FROM "+jComboBoxST.getSelectedItem().toString()+" ORDER by tid,stopid";
        //System.out.println(sql);
        ResultSet rs = s.executeQuery(sql);        
        
        Hashtable<String,Integer> hash = new Hashtable<String,Integer>();    //Key => Target Feature, Column => Index
        
        //GENERATE COLUMNS NAME (in hash)
        int index = 0;
        int unknownIndex = 0;
        String stopName;
        while (rs.next()) {
            if (rs.getObject("stop_name") != null) {
                stopName = rs.getString("stop_name");
            }else { //UNKNOWN
                Statement s2 = conn.createStatement();
                String geom = rs.getObject("the_geom") != null ? ("'" + rs.getString("the_geom") + "'") : "null";
                sql = "SELECT count(*) as qtd "+
                             "FROM "+TrajectoryFrame.getCurrentNameTableStop()+" WHERE unknown IS NOT null AND ST_Intersects(the_geom,"+geom+")";
                
                //System.out.println(sql);
                ResultSet rs2 = s2.executeQuery(sql);
                rs2.next();
                if (rs2.getInt("qtd") > 0) {
                    sql = "SELECT unknown FROM "+TrajectoryFrame.getCurrentNameTableStop()+" " +
                          "WHERE unknown IS NOT null AND ST_Intersects(the_geom,"+geom+") ORDER BY unknown LIMIT 1";
                    System.out.println(sql);
                    Statement s3 = conn.createStatement();
                    ResultSet rs3 = s3.executeQuery(sql);
                    rs3.next();
                    stopName = rs3.getString("unknown");
                    
                    sql = "UPDATE stops SET stop_name = '" + stopName + "',unknown = '" + stopName + "' WHERE pk = "+rs.getInt("pk");
                    System.out.println(sql);
                }else {
                    stopName = unknownIndex+"_unknown";
                    sql = "UPDATE stops SET unknown = '"+stopName+"' WHERE pk = "+rs.getInt("pk");
                    unknownIndex++;
                }
                //System.out.println(sql);
                s2.execute(sql);
            }
            
            stopName = format(stopName,rs.getTimestamp("start_time"),
                    rs.getTimestamp("end_time"),rs.getInt("dowStart"),rs.getInt("dowEnd"));
            if (!hash.containsKey(stopName)) {
                hash.put(stopName,new Integer(index++));
            }                            
        }//END GENERATE COLUMNS NAME
        
        //Fills the attributes of the arff file
        sbw.write("@RELATION Stops");
        sbw.newLine();sbw.newLine();
        sbw.write("@ATTRIBUTE tid NUMERIC");sbw.newLine();
        Enumeration<String> enu = hash.keys();
        while (enu.hasMoreElements()) {
            sbw.write("@ATTRIBUTE "+enu.nextElement()+" {yes}");sbw.newLine();
        }
        
        //CREATES THE MATRIX OF DATA
        s = conn.createStatement();
        sql = "SELECT DISTINCT ON (tid) tid FROM "+TrajectoryFrame.getCurrentNameTableStop()+"";
        rs = s.executeQuery(sql);
        Hashtable<Integer,Integer> hashTid = new Hashtable<Integer,Integer>();
        index = 0;
        while (rs.next()) {
            hashTid.put(new Integer(rs.getInt("tid")),new Integer(index++));
        }
        int qtdColumns = hash.size();
        String[][] matrix = new String[index][qtdColumns];
        for (int i=0;i<index;i++)
            for (int j=0;j<qtdColumns;j++)
                matrix[i][j] = "?";
        
        //Fill the matrix
        s = conn.createStatement();
        sql = "SELECT pk,unknown,the_geom,tid,stop_name,start_time,end_time,Extract(dow from start_time) as dowStart,Extract(dow from end_time) as dowEnd " +
              "FROM "+TrajectoryFrame.getCurrentNameTableStop()+" ORDER by tid,stopid";
        //System.out.println(sql);
        rs = s.executeQuery(sql);
        int tidIndex,attIndex;
        while(rs.next()) {
            if (rs.getObject("unknown") != null) {  //Eh unknown
                stopName = rs.getString("unknown");
            }else {
                stopName = rs.getString("stop_name");
            }
            stopName = format(stopName,rs.getTimestamp("start_time"),
                        rs.getTimestamp("end_time"),rs.getInt("dowStart"),rs.getInt("dowEnd"));
            tidIndex = hashTid.get(new Integer(rs.getInt("tid"))).intValue();
            attIndex = hash.get(stopName).intValue();
            matrix[tidIndex][attIndex] = "yes";
        }
        
        //Fills the @Data of the file
        String line;
        sbw.newLine();sbw.write("@DATA");sbw.newLine();
        Enumeration<Integer> enuTid = hashTid.keys();
        while (enuTid.hasMoreElements()) {
            Integer t = enuTid.nextElement();
            int indexTid = hashTid.get(t).intValue();
            int tid = t.intValue();
            line = tid +",";
            Enumeration<String> columns = hash.keys();
            while (columns.hasMoreElements()) {
                int indexCol = hash.get(columns.nextElement()).intValue();
                line += matrix[indexTid][indexCol] + ",";
            }
            line = line.substring(0,line.length()-1);
            sbw.write(line);sbw.newLine();
        }
        sbw.flush();
        sbw.close();
    }
    
    //Creates the form
    private void initComp(){
    	setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    	
    	//Mounts the layout
		Container container = getContentPane();
		container.setLayout(new BorderLayout());    	
    	
		//GENARFF Panel
		JPanel genarffPanel = new JPanel();		
		GridBagLayout gridbag = new GridBagLayout();
		genarffPanel.setLayout(gridbag);
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH; 			
		c.insets = new Insets(5,10,5,10);
		
		// UP menu
		c.gridx=0;
		c.gridy=0;
		JLabel jLabel1 = new javax.swing.JLabel("Schema");
		genarffPanel.add(jLabel1,c);
		
		c.gridx=1;
		jComboBoxSchema = new JComboBox();
		jComboBoxSchema.setPreferredSize(new Dimension(100,20));
		genarffPanel.add(jComboBoxSchema,c);
		
		c.gridx=3;
		jButtonLoad = new javax.swing.JButton("Load");
		jButtonLoad.addActionListener(new java.awt.event.ActionListener(){
		    public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonLoadActionPerformed(evt);
            }});
		genarffPanel.add(jButtonLoad,c);
		
		c.gridx=5;
		JLabel jLabel2 = new JLabel("Stop Table");
		genarffPanel.add(jLabel2,c);
		
		c.gridx=6;
		jComboBoxST = new JComboBox();
		jComboBoxST.setPreferredSize(new Dimension(150,20));
		genarffPanel.add(jComboBoxST,c);
		
		// Item Panel
		JPanel jPanel1 = new JPanel();		
		jPanel1.setLayout(new GridBagLayout());
		jPanel1.setBorder(BorderFactory.createTitledBorder("Item"));
		GridBagConstraints c2 = new GridBagConstraints();	
		c2.fill  = GridBagConstraints.BOTH;
		c2.insets = new Insets(0,10,5,10);
		
			buttonGroupItem = new ButtonGroup();
		
	        jRadioButtonNameOnly = new JRadioButton("Name Only");
	        buttonGroupItem.add(jRadioButtonNameOnly);
	        jRadioButtonNameOnly.setSelected(true);
	        c2.gridx=0;
	        c2.gridy=0;
	        c2.gridwidth=1;
	        jPanel1.add(jRadioButtonNameOnly,c2);
	        
	        jRadioButtonNStart = new JRadioButton("Name + Start Time");
	        buttonGroupItem.add(jRadioButtonNStart);
	        c2.gridy=1;
	        jPanel1.add(jRadioButtonNStart,c2);
	        
	        jRadioButtonNEnd = new JRadioButton("Name + End Time");
	        buttonGroupItem.add(jRadioButtonNEnd);
	        c2.gridy=2;
	        jPanel1.add(jRadioButtonNEnd,c2);
	        
	        jRadioButtonNStartEnd = new JRadioButton("Name + Start Time + End Time");
			buttonGroupItem.add(jRadioButtonNStartEnd);
			c2.gridy=3;
	        jPanel1.add(jRadioButtonNStartEnd,c2);
			
		c.gridx=0;
		c.gridy=1;
		c.gridwidth=4;
		genarffPanel.add(jPanel1,c);
		
		//Time Panel
		JPanel jPanel2 = new JPanel();		
		jPanel2.setLayout(new GridBagLayout());
		jPanel2.setBorder(BorderFactory.createTitledBorder("Time"));
		c2 = new GridBagConstraints();
		c2.fill  = GridBagConstraints.BOTH; 			
		c2.insets = new Insets(0,10,5,10);
		
			buttonGroupTime = new ButtonGroup();
			
			jRadioButtonYear = new JRadioButton("Year");
			buttonGroupTime.add(jRadioButtonYear);
			c2.gridx=0;
	        c2.gridy=0;
	        c2.gridwidth=1;
			jPanel2.add(jRadioButtonYear,c2);			
			
	        jRadioButtonMonth = new JRadioButton("Month");
	        buttonGroupTime.add(jRadioButtonMonth);
	        c2.gridy=1;
	        jPanel2.add(jRadioButtonMonth,c2);
	        
	        jRadioButtonWeek = new JRadioButton("Weekday | Weekend");
	        buttonGroupTime.add(jRadioButtonWeek);
	        c2.gridy=2;
	        jPanel2.add(jRadioButtonWeek,c2);
	        
	        jRadioButtonDayWeek = new JRadioButton("Day of the Week");
	        buttonGroupTime.add(jRadioButtonDayWeek);
	        jRadioButtonDayWeek.setSelected(true);
	        c2.gridy=3;
	        jPanel2.add(jRadioButtonDayWeek,c2);
	        
	        jRadioButtonUserDef = new JRadioButton("User Defined");
	        buttonGroupTime.add(jRadioButtonUserDef);
	        jRadioButtonUserDef.addChangeListener(new javax.swing.event.ChangeListener() {
	            public void stateChanged(javax.swing.event.ChangeEvent evt) {
	                jRadioButtonUserDefStateChanged(evt);
	            }
	        });
	        c2.gridy=4;
	        jPanel2.add(jRadioButtonUserDef,c2);

	        jButtonDefine = new JButton("Define...");
	        jButtonDefine.setEnabled(false);
	        jButtonDefine.addActionListener(new java.awt.event.ActionListener() {
	            public void actionPerformed(java.awt.event.ActionEvent evt) {
	                jButtonDefineActionPerformed(evt);
	            }
	        });
	        c2.gridx=1;
	        c2.gridy=4;
	        jPanel2.add(jButtonDefine,c2);
        
	    c.gridx=4;
		c.gridy=1;
		c.gridwidth=4;
		genarffPanel.add(jPanel2,c);
		
		
		//Horizontal/Vertical Arff
		JPanel arffPanel = new JPanel();
        arffPanel.setBorder(BorderFactory.createEtchedBorder());
        
	        buttongrouparff = new ButtonGroup();
	        
	        jRBHor = new JRadioButton("Horizontal Arff");
	        buttongrouparff.add(jRBHor);
	        jRBHor.setSelected(true);
	        arffPanel.add(jRBHor);
	        
	        jRBVert = new JRadioButton("Vertical Arff");
	        buttongrouparff.add(jRBVert);        
	        arffPanel.add(jRBVert);
        
        c.gridx=0;
		c.gridy=2;
		c.gridwidth=4;
		genarffPanel.add(arffPanel,c);
		
		jButtonGenArff = new JButton("Generate Arff");
		jButtonGenArff.setEnabled(false);
        jButtonGenArff.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonGenArffActionPerformed(evt);
            }
        });
        c.gridx=6;				
		genarffPanel.add(jButtonGenArff,c);
		
		container.add(genarffPanel,BorderLayout.NORTH);
		
		JPanel seqpat = new JPanel();
		seqpat.setBorder(BorderFactory.createTitledBorder("Sequential Trajectory Pattern Mining"));
		
			JLabel label3 = new JLabel("Minimum Support (0-1):");
			seqpat.add(label3);
			
			jTextFieldMinSupport = new javax.swing.JTextField();
			jTextFieldMinSupport.setPreferredSize(new Dimension(40,20));
			jTextFieldMinSupport.addFocusListener(new java.awt.event.FocusAdapter() {
	            public void focusLost(java.awt.event.FocusEvent evt) {
	                jTextFieldMinSupportFocusLost(evt);
	            }
	        });
			seqpat.add(jTextFieldMinSupport);
			
			jButtonExtractPattern = new javax.swing.JButton("Extract Patterns");
			jButtonExtractPattern.setEnabled(false);
			jButtonExtractPattern.addActionListener(new java.awt.event.ActionListener() {
	            public void actionPerformed(java.awt.event.ActionEvent evt) {
	                jButtonExtractPatternActionPerformed(evt);
	            }
	        });
			seqpat.add(jButtonExtractPattern);
			
			jCheckBoxBD = new javax.swing.JCheckBox("Use stop table from BD.");
			jCheckBoxBD.setSelected(true);
			seqpat.add(jCheckBoxBD);
			
		//container.add(seqpat,BorderLayout.SOUTH);		
		
    }
    private void jButtonLoadActionPerformed(java.awt.event.ActionEvent evt) {
        try{ 
            Statement s = conn.createStatement();
            ResultSet vTableName = s.executeQuery("SELECT f_table_name as tableName "+
                                                  "FROM geometry_columns " +
                                                  "WHERE f_table_schema=trim('"+(String) jComboBoxSchema.getSelectedItem()+"') "+
                                                  "ORDER BY tableName");
            jComboBoxST.removeAllItems();
            while ( vTableName.next() )  {/* creates a new table for each table that has objects with topological relation to vRegion */
                jComboBoxST.addItem(vTableName.getString(1));
            }	
            jButtonExtractPattern.setEnabled(true);
            jButtonGenArff.setEnabled(true);
        }catch (Exception vErro){
            vErro.printStackTrace();
        }
    }

    private void jButtonDefineActionPerformed(java.awt.event.ActionEvent evt) {
        UserDefinedFrame frame = new UserDefinedFrame();
        frame.setModel(listModel);
        frame.setVisible(true);
    }

    private void jRadioButtonUserDefStateChanged(javax.swing.event.ChangeEvent evt) {
        jButtonDefine.setEnabled(jRadioButtonUserDef.isSelected());
    }

    private void jButtonGenArffActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            if(jRBHor.isSelected())generateArffStopFile2(); //creates horizontal arff file
            else generateArffStopFileVert();// or a vertical one
            JOptionPane.showMessageDialog(this,"Arff File Created");
        }catch(Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,"Error creating Arff File");
        }
    }    
        
    private void jButtonExtractPatternActionPerformed(java.awt.event.ActionEvent evt) {
          String table = (String) jComboBoxST.getSelectedItem();
           
          try {
            Statement s = conn.createStatement();
            int qtdStops=0;
            if(jCheckBoxBD.isSelected()){
            	String sql = "SELECT count(*) as count FROM (SELECT DISTINCT tid FROM "+table+") A";
            	ResultSet rs = s.executeQuery(sql);
            	rs.next();
            	qtdStops = rs.getInt("count");
            }
            else{
            	try {
					BufferedReader bufRead = new BufferedReader(new FileReader("data/stopsV.arff"));
					String line = bufRead.readLine(); 
					while(!line.startsWith("@ATTRIBUTE tid") && !line.startsWith("@attribute tid")){
						line= bufRead.readLine();
						if(line==null) break;
            		}
					
					if(line!=null){//end of file and did not find 'ATTRIBUTE' mark
						for(int i=0;i<line.length();i++){//counts the ',', the different tids are the number of ','+1
							if(line.charAt(i)==','){
								qtdStops++;
							}
						}
						qtdStops++;
					}
					else System.out.println("No @ATTRIBUTE mark founded in stopsV.arff.");
					bufRead.close();
				} catch (FileNotFoundException e) {
					System.out.println("Required file named stopsV.arff not found.");
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}           	
            }
            
            if (qtdStops <= 0) {
                JOptionPane.showMessageDialog(this,"No trajectories founded");
                return;
            }
            double temp = Double.parseDouble(jTextFieldMinSupport.getText()) * qtdStops;
            int minSup = (int) Math.round(temp + 0.5);
            if (minSup <= 0) {
                JOptionPane.showMessageDialog(this,"No enough stops to support minimum support");
                return;
            } 
           
            //int minSup = 2;
            
            Vector<SemanticTrajectory> semTrajs = new Vector<SemanticTrajectory>();
            System.out.println("Loading semantic trajectories...");
            loadSemanticTrajectories(semTrajs,table,type);
            if(semTrajs!=null){
	            Vector<Vector<Sequence>> L = new Vector<Vector<Sequence>>();
	            
	            aprioriAllMemory(semTrajs,L,minSup);
	            
	            System.out.println("Filtering Large Sequences...\n\n");
	            filterLargeSequences(L);
	            
	            StringBuffer result = new StringBuffer();
	            for (int i=L.size()-1;i>=0;i--) {
	                result.append("Large Sequences of Length "+(i+1)+"\n");
	                Vector<Sequence> Lk = L.elementAt(i);
	                for (int j=0;j<Lk.size();j++)
	                    result.append("\t"+Lk.elementAt(j).toString()+"\n");
	            }
	            
	            System.out.println(result.toString());
	            SeqPatternResult spr = new SeqPatternResult();
	            spr.setText(result.toString());
	            spr.setVisible(true);
            }
            else System.out.println("No @DATA mark founded in stopsV.arff.");
          }catch(SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,e.toString());
          	}
    }

    private void jTextFieldMinSupportFocusLost(java.awt.event.FocusEvent evt) {
        try {
            double v = Double.parseDouble(jTextFieldMinSupport.getText());
            if (v < 0 || v > 1) {
                JOptionPane.showMessageDialog(this,"Minimum support must be a value between 0 and 1");
                jTextFieldMinSupport.grabFocus();
            }
        }catch(NumberFormatException e) {
            JOptionPane.showMessageDialog(this,"Minimum support must be a value between 0 and 1");
            jTextFieldMinSupport.grabFocus();
        }
    }

    
    /**Generates an horizontal ARFF file from a stop_table created with any STDPM method.
     *
     * @throws IOException 		In case of problems in the ARFF File.
     * @throws SQLException		In case of find problems acessing the stop table.
     */
    private void generateArffStopFile2()  throws IOException, SQLException {
        File f = new File("data");
        if (!f.exists()) f.mkdir();
        
        File stopsFile = new File("data/"+jComboBoxST.getSelectedItem().toString()+"H.arff");
        FileWriter sfw = new FileWriter(stopsFile);
        
        BufferedWriter sbw = new BufferedWriter(sfw);
        String aux;
        // adding comments with information
        sbw.write("% Generated by STDP with table "+jComboBoxST.getSelectedItem().toString()+" in "+new java.util.Date());
        sbw.newLine();
        sbw.write("%");
        sbw.newLine();
        sbw.write("%Parameters:");
        sbw.newLine();
        aux="% Item: ";
        if (jRadioButtonNStart.isSelected()) {
            aux+="Name + Start Time";
        }else if (jRadioButtonNEnd.isSelected()) {
        	aux+="Name + End Time";    
        }else if (jRadioButtonNStartEnd.isSelected()) {
        	aux+="Name + Start Time + End Time";
        }else{
        	aux+="Name Only";        
        }
        sbw.write(aux); sbw.newLine();
        
        aux="% Time: ";
        boolean flag=false;
        if (jRadioButtonYear.isSelected()) {
            aux+="Year";
        }else if (jRadioButtonMonth.isSelected()) {
        	aux+="Month";
        }else if (jRadioButtonWeek.isSelected()) {
        	aux+="Weekend | Weekday";
        }else if (jRadioButtonDayWeek.isSelected()) {
        	aux+="Day of the Week";
        }else {
        	aux+="User Defined:";
        	flag=true;
        }        
        sbw.write(aux); sbw.newLine();
        if(flag){
        	for (int i=0;i<listModel.size();i++) {
                Struct s = (Struct) listModel.elementAt(i);
                sbw.write("%                      "+s.toString2()); sbw.newLine();
            }
        }        
        sbw.write("%");sbw.newLine();
        //end of comments
        
        Statement s = conn.createStatement();
        String sql = "SELECT gid as pk,stop_name,start_time,end_time,Extract(dow from start_time) as dowStart,Extract(dow from end_time) as dowEnd " +
                     "FROM "+jComboBoxST.getSelectedItem().toString();
        //System.out.println(sql);
        ResultSet rs = s.executeQuery(sql);        
        
        Hashtable<String,Integer> hash = new Hashtable<String,Integer>();    //Key => Target Feature, Column => Index
        System.out.println("Generating columns name...");
        //using hash to know the index of each stop_name
        int index = 0; 
        int lastindex = 0;
        String stopName;
        while (rs.next()) {        	
            stopName = format(rs.getString("stop_name"),rs.getTimestamp("start_time"),
                    rs.getTimestamp("end_time"),rs.getInt("dowStart"),rs.getInt("dowEnd"));
            lastindex=rs.getInt("pk");
            if (!hash.containsKey(stopName)) {
                hash.put(stopName,new Integer(index++));
            }                            
        }        
        
        System.out.println("Filling Atributes in arff file...");        
        sbw.write("@RELATION Stops");
        sbw.newLine();
        sbw.newLine();
        //sbw.write("@ATTRIBUTE tid NUMERIC");sbw.newLine();
        Enumeration<String> enu = hash.keys();
        while (enu.hasMoreElements()) {
            sbw.write("@ATTRIBUTE "+enu.nextElement()+" {yes}");sbw.newLine();
        }
        
        System.out.println("Creating matrix of data...");        
        int qtdColumns = hash.size();                
        String[][] matrix = new String[lastindex][qtdColumns];
        for (int i=0;i<lastindex;i++)
            for (int j=0;j<qtdColumns;j++)
                matrix[i][j] = "?";
        
        System.out.println("Filling matrix of data...");       
        s = conn.createStatement();
        sql = "SELECT stop_name,start_time,end_time,Extract(dow from start_time) as dowStart,Extract(dow from end_time) as dowEnd " +
              "FROM "+jComboBoxST.getSelectedItem().toString()+"";
        //System.out.println(sql);
        rs = s.executeQuery(sql);
        int attIndex;
        index=0;
        while(rs.next()) {
            stopName = format(rs.getString("stop_name"),rs.getTimestamp("start_time"),
                        rs.getTimestamp("end_time"),rs.getInt("dowStart"),rs.getInt("dowEnd"));            
            attIndex = hash.get(stopName).intValue();
            matrix[index][attIndex] = "yes";
            index++;
        }
        
        System.out.println("Filling the Data of the file...");
        String line="";
        sbw.newLine();sbw.write("@DATA");sbw.newLine();
        index=0;
        while (index<lastindex) {            
            Enumeration<String> columns = hash.keys();
            while (columns.hasMoreElements()) {
                int indexCol = hash.get(columns.nextElement()).intValue();
                line += matrix[index][indexCol] + ",";
            }
            line = line.substring(0,line.length()-1);
            sbw.write(line);sbw.newLine();
            index++;
            line="";
        }
        sbw.flush();
        sbw.close();
        
        System.out.println("Completed !");
    }
    /**Generates a vertical ARFF file from a stop_table created with any STDPM method.
     * 
     * @throws IOException 		In case of problems in the ARFF File.
     * @throws SQLException		In case of find problems acessing the stop table.
     */
    private void generateArffStopFileVert() throws IOException, SQLException{
    	File f = new File("data");
        if (!f.exists()) f.mkdir();
        
        File stopsFile = new File("data/"+jComboBoxST.getSelectedItem().toString()+"V.arff");
        FileWriter sfw = new FileWriter(stopsFile);
        
        BufferedWriter sbw = new BufferedWriter(sfw);
        String aux;
        // adding comments with information
        sbw.write("% Generated by STDP with table "+jComboBoxST.getSelectedItem().toString()+" in "+new java.util.Date());
        sbw.newLine();
        sbw.write("%");
        sbw.newLine();
        sbw.write("%Parameters:");
        sbw.newLine();
        aux="% Item: ";
        if (jRadioButtonNStart.isSelected()) {
            aux+="Name + Start Time";
        }else if (jRadioButtonNEnd.isSelected()) {
        	aux+="Name + End Time";    
        }else if (jRadioButtonNStartEnd.isSelected()) {
        	aux+="Name + Start Time + End Time";
        }else{
        	aux+="Name Only";        
        }
        sbw.write(aux); sbw.newLine();
        
        aux="% Time: ";
        boolean flag=false;
        if (jRadioButtonYear.isSelected()) {
            aux+="Year";
        }else if (jRadioButtonMonth.isSelected()) {
        	aux+="Month";
        }else if (jRadioButtonWeek.isSelected()) {
        	aux+="Weekend | Weekday";
        }else if (jRadioButtonDayWeek.isSelected()) {
        	aux+="Day of the Week";
        }else {
        	aux+="User Defined:";
        	flag=true;
        }        
        sbw.write(aux); sbw.newLine();
        if(flag){
        	for (int i=0;i<listModel.size();i++) {
                Struct s = (Struct) listModel.elementAt(i);
                sbw.write("%                      "+s.toString2()); sbw.newLine();
            }
        }        
        sbw.write("%");sbw.newLine();
        //end of comments
        
        System.out.println("Filling Atributes in arff file...");        
        sbw.write("@RELATION Stops");
        sbw.newLine();
        sbw.newLine();
        
        //generating the tids                 
        Statement s = conn.createStatement();
        String sql = "SELECT distinct tid FROM "+jComboBoxST.getSelectedItem().toString();
        ResultSet rs = s.executeQuery(sql);
        
        aux="@ATTRIBUTE tid {";
        while(rs.next()){
        	aux+=rs.getString("tid")+",";
        }
        aux=aux.substring(0,aux.length()-1)+"}";
        
        sbw.write(aux);sbw.newLine();    	
        
        //generate the stop_names + filling the atributtes
        String linha;
        Vector<String> linhas = new Vector<String>();
        String stop_name;
        HashSet<String> stop_names = new HashSet<String>();
        
        s = conn.createStatement();
        sql = "SELECT tid,stop_name,start_time,end_time,Extract(dow from start_time) as dowStart,Extract(dow from end_time) as dowEnd " +
        "FROM "+jComboBoxST.getSelectedItem().toString();
        rs = s.executeQuery(sql);        
        
        while(rs.next()){
        	stop_name = format(rs.getString("stop_name"),rs.getTimestamp("start_time"),
                    rs.getTimestamp("end_time"),rs.getInt("dowStart"),rs.getInt("dowEnd"));
        	stop_names.add(stop_name);// controlls a set with the stop_names
        	linha = rs.getInt("tid")+","+stop_name; // the filling part
        	linhas.add(linha);
        }
        
        // saves the stop_names 
        Iterator It = stop_names.iterator();
        aux="@ATTRIBUTE stop_name {";
        while (It.hasNext()) {
            aux+=It.next()+",";
        }
        aux=aux.substring(0,aux.length()-1)+"}";
        sbw.write(aux);sbw.newLine();sbw.newLine();
                
        //saves the filling part
        System.out.println("Filling the Data of the file...");
        sbw.write("@DATA");sbw.newLine();        
        for(String a:linhas){
        	sbw.write(a);sbw.newLine();
        }
        
    	sbw.flush();
        sbw.close();
        
        System.out.println("Completed !");
    }
//------------START OF SEQUENTIAL PATTERN METHODS    
//    Based in the article of Agrawal, in
//    http://www.informatik.uni-trier.de/~ley/db/conf/icde/AgrawalS95.html	
//
//-------------------------------------------------------------
    private void filterLargeSequences(Vector<Vector<Sequence>> L) {
        for (int i=L.size()-1;i>=0;i--) {
            Vector<Sequence> Lk = L.elementAt(i);
            for (int j=i-1;j>=0;j--) {
                Vector<Sequence> Lk1 = L.elementAt(j);
                
                for (int ik=0;ik<Lk.size();ik++) {
                    Sequence seqIk = Lk.elementAt(ik);
                    
                    for (int k=Lk1.size()-1;k>=0;k--) {
                        if (Lk1.elementAt(k).isSubsequenceOf(seqIk)) {
                            Lk1.removeElementAt(k);
                        }
                    }                    
                }
            }
        }
    }
    
    /**Formats the parameter name, for using type or instance granularity.
     * 
     * @param name 		The stop_name, in the granularity of instance.
     * @param type		The flag to indicates if use type granularity or not (in that case, using instance granularity).
     * @return			Te stop_name formatted.
     */
    private String extractGranularity(String name,boolean type) {
        if (type) {
            //if (name.indexOf("unknown") == -1) {
                int temp = name.indexOf("_");
                String sub = name.substring(0,temp);
                try {
                    int number = Integer.parseInt(sub);
                    return name.substring(temp+1);
                } catch (NumberFormatException ex) {}
            //}
        }
        return name;
    }
    
    private void loadSemanticTrajectories(Vector<SemanticTrajectory> semTrajs,String table,boolean type) throws SQLException {
        Statement s = conn.createStatement();
        //String sql = "SELECT tid,stopid,CASE WHEN stop_name IS NOT NULL THEN stop_name ELSE unknown END as name FROM "+TrajectoryFrame.getCurrentNameTableStop()+" ORDER BY tid DESC,start_time";
        if(jCheckBoxBD.isSelected()){
	        String sql = "SELECT gid as pk,stop_name as name,the_geom,tid,start_time,end_time,Extract(dow from start_time) as dowStart,Extract(dow from end_time) as dowEnd " +
	                     "FROM "+table+" ORDER BY tid DESC,start_time";
	        ResultSet rs = s.executeQuery(sql);
	        int tid = -1;
	        SemanticTrajectory semTraj = null;
	        while (rs.next()) {
	            if (rs.getInt("tid") != tid) {
	                if (semTraj != null) {
	                    semTrajs.addElement(semTraj);
	                }
	                semTraj = new SemanticTrajectory();
	                semTraj.tid = rs.getInt("tid");
	                tid = semTraj.tid;
	            }
	            String name = extractGranularity(rs.getString("name"),type);
	            semTraj.item.addElement(format(name,rs.getTimestamp("start_time"),rs.getTimestamp("end_time"),rs.getInt("dowStart"),rs.getInt("dowEnd")));
	        }
	        if (semTraj != null) {
	            semTrajs.addElement(semTraj);
	        }
        }
        else{
        	String tid ="-1",tidatual="",rest="",name="";
	        SemanticTrajectory semTraj = null;
	        try {
				BufferedReader bufRead = new BufferedReader(new FileReader("data/stopsV.arff"));
				String line = bufRead.readLine(); 
				
				while(!line.startsWith("@DATA") && !line.startsWith("@data")){
					line=bufRead.readLine();
					if(line==null) break;
        		}
				if(line!=null){
					line=bufRead.readLine(); // read line of information
					while(line!=null){
						//System.out.println("Line: "+line);
						if(!line.startsWith("%")){// that line can't be a comment...
							tidatual=line.substring(0,line.indexOf(","));//separates the tid,stop_name,...
							rest=line.substring(line.indexOf(",")+1);
							//System.out.println("Rest: "+rest);
							try{
								name=rest.substring(0, rest.indexOf(",")-1);
							}
							catch(Exception e){
								name=rest;
							}
							//System.out.println("tidatual: "+tidatual+"\nTid: "+tid);
							if(Integer.parseInt(tidatual)!=Integer.parseInt(tid)){
								if (semTraj != null) {
									//System.out.println("adding");
				                    semTrajs.addElement(semTraj);
				                }
				                semTraj = new SemanticTrajectory();
				                semTraj.tid = Integer.parseInt(tidatual);
				                tid = Integer.toString(semTraj.tid);
							}
							semTraj.item.addElement(name);
						}
						line=bufRead.readLine(); // read line of information
					}
					if (semTraj != null) {
						//System.out.println("adding");
	                    semTrajs.addElement(semTraj);
	                }			
				}
				bufRead.close();
			} catch (FileNotFoundException e) {
				System.out.println("Required file named stopsV.arff not found.");
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
    }
    
    private void aprioriAllMemory(Vector<SemanticTrajectory> semTrajs, Vector<Vector<Sequence>> L, int minSup) throws SQLException {
        System.out.println("Min Support: "+ minSup);
        // Creates sequences with 1 element
        Vector<Sequence> L1 = new Vector<Sequence>();
        for (int i=0;i<semTrajs.size();i++) {
            SemanticTrajectory st = semTrajs.elementAt(i);
            for (int j=0;j<st.item.size();j++) {
                Sequence stemp = new Sequence();
                stemp.item.addElement(st.item.elementAt(j));
                if (!L1.contains(stemp)) {
                    L1.addElement(stemp);
                }
            }
        }
        calculateSupport(semTrajs,L1,minSup);
        L.addElement(L1);
        
        for (int k=0; k < L.size() && L.elementAt(k).size() != 0; k++) {
            L1 = L.elementAt(k);
            System.out.println("Generating Candidates of size "+(k+2)+"...");
            Vector<Sequence> L2 = generateCandidates(L1);
            System.out.println("\tPruning Out...");
            pruneOut(L1,L2);
            System.out.println("\tCalculating Support...");
            calculateSupport(semTrajs,L2,minSup);
            L.addElement(L2);
        }
    }
    
    private void calculateSupport(Vector<SemanticTrajectory> semTrajs, Vector<Sequence> L2, int minSup) {
        for (int i=0;i<L2.size();i++) {
            Sequence s = L2.elementAt(i);
            
            int count = 0;
            for (int j=0;j<semTrajs.size();j++) {
                if (semTrajs.elementAt(j).containsPattern(s.item))
                    count++;
            }
            s.support = count;
            
            if (s.support < minSup) {
                L2.remove(i--);
            }
        }
    }
    
    private void pruneOut(Vector<Sequence> L1, Vector<Sequence> L2) {
        for (int i=0;i<L2.size();i++) {
            Sequence s = L2.elementAt(i);
            
            //Split subsequences
            Sequence s1 = new Sequence(),s2 = new Sequence();
            for (int j=0;j<s.item.size()-1;j++) {
                s1.item.addElement(s.item.elementAt(j));
            }
            for (int j=1;j<s.item.size();j++) {
                s2.item.addElement(s.item.elementAt(j));
            }
            
            //Look if the subsequences are in L1
            boolean subS1=false,subS2=false;
            for (int j=0;j<L1.size();j++) {
                Sequence temp = L1.elementAt(j);
                
                if (s1.isSubsequenceOf(temp))
                    subS1 = true;
                if (s2.isSubsequenceOf(temp))
                    subS2 = true;
                
                if (subS1 && subS2)
                    break;
            }
            
            if (!(subS1 && subS2)) {
                L2.remove(i--);
            }
        }
    }
    
    private Vector<Sequence> generateCandidates(Vector<Sequence> L) {
        Vector<Sequence> ret = new Vector<Sequence>();
        
        for (int i=0;i<L.size();i++) {
            Sequence s1 = L.elementAt(i);
            for (int j=0;j<L.size();j++) {
                Sequence s2 = L.elementAt(j);
                Sequence s = s1.generateSequence(s2);
                if (s != null && !ret.contains(s))
                    ret.addElement(s);
            }
        }
        
        return ret;
    }

//------------END OF SEQUENTIAL PATTERN METHODS     
    
    public static void main (String args[]){
    	new GenArffFile();
    }
}
