package weka.associations;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JOptionPane;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.Tag;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;

import weka.gui.stpm.seqpattern.*;

/**<!-- globalinfo-start -->
 * Class implementing a Sequential-Pattern algorithm, based in Agrawals' work.
 * Created by Andrey Palma <br/>
 * For more information see:<br/>
 * <br/>
 * http://www.informatik.uni-trier.de/~ley/db/conf/icde/AgrawalS95.html
 *
 <!-- options-start -->
 * Valid options are: <p/>
 *
 * <pre> -S = Support
 *  The minimum support of a rule</pre>
 *
 *
 <!-- options-end -->
 *  @ REQUIRES !!! 
 *	
 *	vertical arff file, with two attributes:	
 *	-> tid NOMINAL (with a different identification to each trajectory) 
 *	-> stop_name NOMINAL (with the names of the stops, already with item and time format)
 *
 *	Also, this arff have to be stops in sequential way, like:
 *	(stop_name with name+start_time and time of weekday)
 *
 *	1,5_school_monday
 *	1,32_park_monday
 *	1,100_hospital_monday
 *	2,5_school_monday
 *	2,32_park_monday
 *	2,10_school_monday
 *
 *	Wich can be read as: the trajectory '1' went to stop school with gid '5' to stop park with gid '32' 
 *	and then to stop hospital with gid '100'. The trajecoty '2' went to to stop school with gid '5' to stop 
 *	park with gid '32' and finally to stop school with gid '10'. So, there's a pattern with Support=1.0 :
 *
 *	5_schoolmonday -> 32_park_monday
 *
 *	examples can be found at: 
 *	or using module GDPM-STDPM to create the arff file. 
 * 
 * @author Gabriel Oliveira
 */

public class TrajectorySequentialPattern 
extends AbstractAssociator implements OptionHandler, TechnicalInformationHandler {
	
	/** for serialization */
	static final long serialVersionUID = 3277498842319212687L; //TOBEDONE, WTF ?!?!?
	
	/** The support the user set. */
	protected double m_Support;
	
	/** The minimun stop, calculated in checkFile. */
	protected int minSup;
	
	/** Flag for error detecting, in format of arff file.*/
	protected boolean error_detected = false;
	
	/** Error message, in format of arff file.*/
	protected String error_msg = "";
		
	private int qtdTids;
	
	private StringBuffer result = new StringBuffer();

	/**Main constructor. */	
	public TrajectorySequentialPattern(){
		resetOptions();
	}
	
	/**
	 * Method that launches the search to find the rules with the highest
	 * confirmation.
	 *
	 * @param instances The instances to be used for generating the rules.
	 * @throws Exception if rules can't be built successfully.
	 */
	public void buildAssociations(Instances instances) throws Exception {
		//The working thing		
		
		if(checkFile(instances)){
			Vector<SemanticTrajectory> semTrajs = new Vector<SemanticTrajectory>();
            System.out.println("Loading semantic trajectories...");
            loadSemanticTrajectories(semTrajs,instances);
            if(semTrajs!=null){
	            Vector<Vector<Sequence>> L = new Vector<Vector<Sequence>>();
	            
	            aprioriAllMemory(semTrajs,L,minSup);
	            
	            System.out.println("Filtering Large Sequences...\n\n");
	            filterLargeSequences(L);
	            
	            result = new StringBuffer();
	            result.append("With "+qtdTids+" trajectories (trajs), we have: \n");
	            for (int i=L.size()-1;i>=0;i--) {
	                result.append("Large Sequences of Length "+(i+1)+"\n");
	                Vector<Sequence> Lk = L.elementAt(i);
	                for (int j=0;j<Lk.size();j++)
	                    result.append("\t"+Lk.elementAt(j).toString()+" trajs\n");
	            }
            }
		}
	}
	
	
	/**
	 * Outputs the size of all the generated sets of itemsets and the rules.
	 *
	 * @return a string representation of the model
	 */
	public String toString(){
		if(error_detected){
			return error_msg;
		}
		else{
			return result.toString();
		}
	}
	/**Check if the ARFF file has the attributes tid and stop_name,only,and if they're nominal. 
	 * 
	 * @param instances 	the data.
	 * @return		True if the file is OK. False if not.
	 */
	private boolean checkFile(Instances instances){
		//There must be only two attributes: tid and stop_name, both NOMINAL type.
		error_detected = false;		
		if(instances.numAttributes()>2){
			error_detected = true;
			error_msg = "\t\tMore than two attributes in the file.\n\t\tMust have two attributes only: tid and stop_name.";
		}		
		// The first attribute must be 'tid' and it must be nominal
		String line = instances.attribute(0).toString();
		if(!error_detected && (line.startsWith("@ATTRIBUTE tid") || line.startsWith("@attribute tid"))){
			if(instances.attribute(0).isNominal()){				
				for(int i=0;i<line.length();i++){//counts the ',', the different tids are the number of ','+1
					if(line.charAt(i)==','){
						qtdTids++;
					}
				}
				qtdTids++;
				//error_detected=false;
			}
			else{
				error_detected = true;
				error_msg = "\t\tATTRIBUTE tid must be NOMINAL type.";
			}
		}
		else if(!error_detected && (!line.startsWith("@ATTRIBUTE tid") && !line.startsWith("@attribute tid"))){
			error_detected = true;
			error_msg = "\t\tATTRIBUTE tid not found.\n\t\tMust be the first ATTRIBUTE.";			
		}
		
		if(!error_detected && qtdTids<=0){
			error_detected = true;
			error_msg = "\t\tNo trajectories found.";			
		}
		//finish to check tid
		//check stop_name, that must be nominal
		line = instances.attribute(1).toString();
		if( !error_detected && (
			!instances.attribute(1).isNominal() || //check if it's nominal
			(!line.startsWith("@ATTRIBUTE stop_name") && !line.startsWith("@attribute stop_name"))//check if it's really 'stop_name' attribute
			)){
			error_detected = true;
			error_msg = "\t\tATTRIBUTE stop_name not found.\n\t\tMust be the last (the second) ATTRIBUTE.";
		}
		
		//check minSup
		double temp = m_Support * qtdTids;//quantities of instances that have to have a sequence
        minSup = (int) Math.round(temp + 0.5);        
        if (!error_detected && minSup <= 0) {            
            error_detected = true;
			error_msg = "\t\tNo enough stops to support minimum support.";
        }
		
		return !error_detected;//if not found error, return true
	}
	
	/**
	 * Resets the options to the default values.
	 */
	private void resetOptions(){
		m_Support = 0.5;
		minSup=0;
		qtdTids=0;
		error_detected = false;
		error_msg = "";
	}
	
	/**
	 * Gets the current settings of the Apriori object.
	 *
	 * @return an array of strings suitable for passing to setOptions
	 */
	public String[] getOptions() {
		String[] options = new String[20];
		int current = 0;

		options[current++] = "-S";
		options[current++] = "" + m_Support;

		while (current < options.length) {
			options[current++] = "";
		}
		return options;
	}

	/**
	 * Returns an enumeration describing the available options.
	 *
	 * @return an enumeration of all the available options.
	 */
	public Enumeration listOptions() {
		String string1 = "\tThe minimum support. (default = " +
		m_Support + ")"; 
		
		FastVector newVector = new FastVector(11);		
		newVector.addElement(new Option(string1, "S", 1,
		"-S <minimum support>"));
		//newVector.addElement(new Option(kcfile, "F", 1,
		//"-F <file location>"));

		return newVector.elements();
	}

	/**
	 * Parses a given list of options. <p/>
	 *
   	<!-- options-start -->
	 * Valid options are: <p/>
	 *
	 * <pre> -S
	 *  The minimum support</pre>
	 *
   <!-- options-end -->
	 *
	 * @param options the list of options as an array of strings
	 * @throws Exception if an option is not supported
	 */
	public void setOptions(String[] options) throws Exception {
		resetOptions();
		String minSupportString = Utils.getOption('S', options);
		if (minSupportString.length() != 0) {
			m_Support = (new Double(minSupportString)).doubleValue();
		}
	}
	
	/**
	 * Returns a string describing this associator
	 * @return a description of the evaluator suitable for
	 * displaying in the explorer/experimenter gui
	 */
	public String globalInfo() {
		return
		"Class implementing a Sequential-Pattern algorithm. Based in Agrawals' work.Use a vertical ARFF file."
		+ getTechnicalInformation().toString();
		//TOBEDONE
	}
	
	/**
	 * Returns an instance of a TechnicalInformation object, containing
	 * detailed information about the technical background of this class,
	 * e.g., paper reference or book this class is based on.
	 *
	 * @return the technical information about this class
	 */
	public TechnicalInformation getTechnicalInformation() {
		TechnicalInformation result;
		TechnicalInformation additional;

		result = new TechnicalInformation(Type.INPROCEEDINGS);
		result.setValue(Field.AUTHOR, "First implementation by Andrey Palma");
		//TOBEDONE

		return result;		
	}

	public String minSupportTipText() {
		return "This is the support, the parcel of trajectories considered minimum.";
	}

	public double getminSupport() {
		return m_Support;
	}

	public void setminSupport(double v) {
		m_Support = v;
	}
	
	public String getRevision() {
		return RevisionUtils.extract("$Revision: 0.0 $");
	}
	
//------------START OF SEQUENTIAL PATTERN METHODS    
//  Based in the article of Agrawal, in
//  http://www.informatik.uni-trier.de/~ley/db/conf/icde/AgrawalS95.html
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
    /**Transform Instances in Semantic Trajectories.  
     * 
     * @param semTrajs			The trajectories to be returned.
     * @param instances			The Instances in the dataset.
     */
    private void loadSemanticTrajectories(Vector<SemanticTrajectory> semTrajs,Instances instances){
    	String tid ="-1",tidatual="",rest="",name="",line="";
        SemanticTrajectory semTraj = null;    	
		for(int i=0;i<instances.numInstances();i++){
			line = instances.instance(i).toString();
			//System.out.println("Line: "+line);			
			tidatual=line.substring(0,line.indexOf(","));//separates the tid,stop_name,...
			rest=line.substring(line.indexOf(",")+1);
			name=rest;
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
		if (semTraj != null) {
			//System.out.println("adding");
            semTrajs.addElement(semTraj);
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
	

}
