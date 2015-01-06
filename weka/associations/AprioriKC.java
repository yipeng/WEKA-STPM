/*
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    alo0ng with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

/*
 *    OurApriori.java 2007 UHasselt
 *
 */

/**
 * look for TOBEDONE
 */
package weka.associations;

import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.FastVector;
import weka.core.Instances;
import weka.core.Instance;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.SelectedTag;
import weka.core.Tag;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.core.Capabilities.Capability;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.TreeSet;
import java.util.ArrayList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 <!-- globalinfo-start -->
 * Class implementing an Apriori-type algorithm. Iteratively reduces the minimum support until it finds the required number of rules with the given minimum confidence.<br/>
 * For more information see:<br/>
 * <br/>
 * Thesis Anke and Vania TOBEDONE
 <!-- technical-bibtex-start -->
 * BibTeX:
 * TOBEDONE
 * <p/>
 <!-- technical-bibtex-end -->
 *
 <!-- options-start -->
 * Valid options are: <p/>
 *
 * <pre> -C =confidence
 *  The metric type by which to rank rules.</pre>
 *
 * <pre> -S = Support
 *  The minimum support of a rule</pre>
 *
 *
 <!-- options-end -->
 *
 * @author Bart Moelans
 */
public class AprioriKC
extends AbstractAssociator implements OptionHandler, TechnicalInformationHandler {

	/** for serialization */
	static final long serialVersionUID = 3277498842319212687L; //TOBEDONE

	/** The minimum support. */
	protected double m_minSupport;

	/** The minimum confidense */
	protected double m_minConfidence;

	/** Boolean if we should build rules */
	protected boolean m_buildRules;

	/** Apriori type: Apriori */
	protected static final int APRIORI = 0;
	/** Apriori type: Apriori-kc */
	protected static final int APRIORIKC = 1;
	/** Apriori type: Apriori prune rules */
	protected static final int APRIORIPRUNE = 2;

	/** Metric types. */
	public static final Tag[] TAGS_SELECTION = {
		new Tag(APRIORI, "Apriori"),
		new Tag(APRIORIKC, "Apriori-KC"),
		//new Tag(APRIORIPRUNE, "Apriori-prune")
	};

	/** The selected metric type. */
	protected int m_algoType;

	/** Location of knowledge file */
	protected File m_kcFile;

	/** Vector for itemset results */
	private Vector<Hashtable> itemsets;

	/** Vector containing transaction DB */
	private Vector<TreeSet> tdb; //transaction DB

	/** Support in number of transactions*/
	private double support;

	/** Vector that contains rules */
	private Vector<Hashtable> rules;

	/** ArrayList containing the dependences */
	private ArrayList<String> dependences;
	
	private long startTime = 0;

	/**
	 * Returns a string describing this associator
	 * @return a description of the evaluator suitable for
	 * displaying in the explorer/experimenter gui
	 */
	public String globalInfo() {
		return
		"Class implementing an APriori-type algorithm. Iteratively reduces "
		+
		"the minimum support until it finds the required number of rules with "
		+ "the given minimum confidence.\n"
		+ "For more information see:\n\n"
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
		result.setValue(Field.AUTHOR, "Anke and Vania");
		//TOBEDONE

		return result;
	}

	/**
	 * Constructor that allows to sets default values for the
	 * minimum confidence and the maximum number of rules
	 * the minimum confidence.
	 */
	public AprioriKC() {
		resetOptions();
	}

	/**
	 * Resets the options to the default values.
	 */
	public void resetOptions() {
		m_minSupport = 0.5;
		m_minConfidence = 0.5;
		m_buildRules = true;
		m_algoType = APRIORI;
		m_kcFile = new File("user.dir");
		try {
			m_kcFile = new File(new File(".").getCanonicalPath());
		} catch (IOException e) {
			e.printStackTrace();
		}

		itemsets = new Vector<Hashtable> ();
		rules = new Vector<Hashtable> ();
		tdb = new Vector<TreeSet> (); //transaction DB
	}

	/**
	 * Method that launches the search to find the rules with the highest
	 * confirmation.
	 *
	 * @param instances The instances to be used for generating the rules.
	 * @throws Exception if rules can't be built successfully.
	 */
	public void buildAssociations(Instances instances) throws Exception {
		startTime = System.currentTimeMillis();
		Hashtable<String, Integer> items = new Hashtable<String, Integer> ();
		if(m_algoType != APRIORI)
			readDependences();

		int length = instances.numInstances();

		//init with 1-itemsets
		for (int i = 0; i < length; i++) {
			TreeSet<String> ta = new TreeSet<String> (); //transaction
			Instance inst = instances.instance(i);
			int inst_length = inst.numAttributes();

			for (int j = 0; j < inst_length; j++) {
				if (!inst.isMissing(j)) {
					Attribute attr = inst.attribute(j);
					String attrname = attr.name();

					Integer attrval = 1;
					if (attr.isNumeric()) {
						String attrval2 = inst.stringValue(attr).trim();
						attrname += "=" + attrval2;
					}
					else if (attr.isNominal()) {
						String attrval2 = inst.stringValue(attr).trim();
						attrname += "=" + attrval2;
					}
					else if (attr.isString()) {
						String attrval2 = inst.stringValue(attr).trim();
						if (attrval2.startsWith("[")) {
							attrval2 = attrval2.substring(1, attrval2.length() - 1);
							String[] arr = attrval2.split(";");
							for (int v = 0; v < arr.length - 1; v++) {
								String attrname2 = attrname + "=" + arr[v].trim();
								ta.add(attrname2);
								if (items.containsKey(attrname2)) {
									Integer tmp = items.get(attrname2);
									tmp += 1;
									items.put(attrname2, tmp);
								}
								else {
									items.put(attrname2, attrval);
								}
							}
							attrname += "=" + arr[arr.length - 1];
						}
						else {
							attrname += "=" + attrval2;
						}
						// System.out.println("string");
					}
					else {
						//System.out.println("anders");
					}
					//  else {
					//    attrval = Integer.getInteger(attr.value(0));
					//  }
					//System.out.println("\t attr " + attrname + "=" + attrval);
					ta.add(attrname);
					if (attrval > 0) {
						if (items.containsKey(attrname)) {
							Integer tmp = items.get(attrname);
							tmp += 1;
							items.put(attrname, tmp);
						}
						else {
							items.put(attrname, attrval);
						}
					}
				}
			}
			tdb.add(ta);
			//System.out.println();
		}
		//prune 1-itemsets
		Enumeration<String> en = items.keys();
		support = m_minSupport * ( (double) length);
		//System.out.println("Support "+support);
		while (en.hasMoreElements()) {
			String key = en.nextElement();
			Integer value = items.get(key);
			//System.out.println("Value= "+value);
			if (value < support) {
				items.remove(key);
			}
			/* else {
         System.out.println("Frequent = " + key);
       }*/
		}

		itemsets.add(items);
		//generate k-itemsets
		int k = 2;
		boolean all_found = items.isEmpty();
		// System.out.println("step further to 2:" + all_found);
		Hashtable<String, Integer> kmin = (Hashtable) items.clone();
		while (!all_found) {
			//generate k-itemsets
			items = new Hashtable<String, Integer> ();
			Hashtable<String, Double> krules = new Hashtable<String, Double> ();
			TreeSet<String> trash = new TreeSet<String> ();
			String[] keys = kmin.keySet().toArray(new String[1]);
			// System.out.println("Array size= " + keys.length);
			for (int i = 0; i < keys.length - 1; i++) {
				for (int j = i + 1; j < keys.length; j++) {
					String newset = innerJoin(keys[i], keys[j]);
					// System.out.println("a:" + k + "-itemsets= " + newset);
					if (!newset.equals("") && !trash.contains(newset)) {
						trash.add(newset); //I'm sure not looking at same itemset twice
						//check if newset has minimum support
						int count = giveSupport(newset);
						if (count >= (int) Math.ceil(support)) {
							if(m_algoType != APRIORIKC ||
									m_algoType == APRIORIKC && !isDependence(newset)) {
								items.put(newset, count);
								if (m_buildRules)
									krules = getRules(newset, k, count, krules);
							}
						}
					}
				}
			}

			//prune k-itemset
			if (!(all_found = items.isEmpty())) {
				itemsets.add(items);
				rules.add(krules);
				kmin = (Hashtable) items.clone();
				k++;
				//System.out.println("step further to " + k + ":" + all_found);
			}
		}
		
		if(m_algoType == APRIORIPRUNE) {
			prune();
		}
	}

	private void prune() {
		// remove itemsets
		for(int i = 0; i < itemsets.size(); i++) {
			Enumeration<String> keys = itemsets.get(i).keys();
			while(keys.hasMoreElements()) {
				String key = keys.nextElement();
				if(isDependence(key))
					itemsets.get(i).remove(key);
			}
		}

		// remove rules
		for(int i = 0; i < rules.size(); i++) {
			Hashtable<String,
			Double> krules = (Hashtable<String, Double>) rules.get(i);
			Enumeration<String> en2 = krules.keys();
			while (en2.hasMoreElements()) {
				String key = en2.nextElement();
				String[] parts = key.split("=>");
				String left = parts[0], right = parts[1];
				boolean remove = false;

				/* We will only work with dependences with 2 items !!
				 * with dependence A -> B, we need to remove:
				 * a) AC -> BD
				 * b) AB -> C
				 * c) C -> AB
				 * A rule like B -> A does not need to be removed.
				 */
				for(int j = 0; j < dependences.size() && !remove; j++) {
					String[] depParts = dependences.get(j).split(" ");
					if(hasDepPart(left,depParts[0])) {
						remove = hasDepPart(left, depParts[1]) ||
						hasDepPart(right, depParts[1]);
					}
					else
						remove = hasDepPart(right, depParts[0]) &&
						hasDepPart(right, depParts[1]);
				}
				if(remove)
					krules.remove(key);
			}
		}
	}
	
	private boolean hasDepPart(String s, String depPart) {
		return s.startsWith(depPart) || s.endsWith(" " + depPart) || s.contains(" " + depPart + " ");
	}

	/**
	 * a nasty implementation of the inner join method used in databases
	 * since this one WILL return 'I1 I2' as well as 'I2 I1' but only one of these
	 * combinations is stored in the getGlobalData() DB, it doesn't matter.
	 * @return the joined string.
	 */
	private String innerJoin(String one, String two) {
		String[] arr1 = one.split(" ");
		String[] arr2 = two.split(" ");
		String out = "";
		int i = 0, j = 0, ctr = 0;

		while (i < arr1.length && j < arr2.length) {
			if (arr1[i].compareTo(arr2[j]) == 0) {
				out += " " + arr1[i];
				i++;
				j++;
			}
			else if (arr1[i].compareTo(arr2[j]) < 0) {
				out += " " + arr1[i];
				i++;
			}
			else {
				out += " " + arr2[j];
				j++;
			}
			ctr++;
		}
		if (i < arr1.length)
			for (int k = i; k < arr1.length; k++) {
				out += " " + arr1[k];
				ctr++;
			}
		else if (j < arr2.length)
			for (int k = j; k < arr2.length; k++) {
				out += " " + arr2[k];
				ctr++;
			}

		return (ctr==arr1.length+1) ? out.trim() : "";
	}

	/**
	 *
	 * @param newset String => is alphabetical
	 * @param instances Instances
	 * @return Integer
	 */
	int giveSupport(String newset) {
		int count = 0;
		String[] arr = newset.split(" ");
		for (int i = 0; i < tdb.size(); i++) {
			TreeSet<String> ta = tdb.get(i);
			boolean contains = true;
			for (int j = 0; j < arr.length && contains; j++)
				contains = ta.contains(arr[j]);
			if (contains)
				count++;
		}
		return count;
	}

	Hashtable<String,
	Double> getRules(String newset, int k, Integer newset_supp,
			Hashtable<String, Double> krules) {
		String[] arr = newset.split(" ");
//		System.out.println("k: " + k + " arr: " + arr.length);
		for (int i = 1; i < k; i++) {
			//generate all combinations of length i
			boolean[] one_result = new boolean[k];
			Vector<boolean[]> results = new Vector<boolean[]> ();
			for (int j = 0; j < k; j++) {
				one_result[i] = false;
			}
			genCombis(one_result, 0, i, results);
			//System.out.println(k+"-itemset, possibilities=" + results.size()+", i="+i);
			for (int j = 0; j < results.size(); j++) {
				String left = "", right = "";
				one_result = results.get(j);
				for (int r = 0; r < k; r++) {
					if (one_result[r])
						left += arr[r] + " ";
					else
						right += arr[r] + " ";
				}

				left = left.trim();
				right = right.trim();
				int l_supp = giveSupport(left);
				double conf = (double) newset_supp / (double) l_supp;

				if (conf >= m_minConfidence)
					krules.put(left + "=>" + right, conf);
			}

		}
		return krules;
	}

	/**
	 * output is boolean array of length k with total of i (i<k) true values
	 * boolean[x]= true  means x is a left antecedent
	 * @param i int
	 * @param k int
	 * @return boolean[][]
	 */
	void genCombis(boolean[] tmp_result, int step, int totaltrues,
			Vector<boolean[]> result) {
		int count = 0;
		for (int i = 0; i < step; i++) {
			if (tmp_result[i]) {
				count++;
			}
		}
		if (count == totaltrues) {
			result.add(tmp_result.clone());
			/* System.out.println("step "+step+":");
       for (int r = 0; r < tmp_result.length; r++) {
           if (tmp_result[r]) {
             System.out.print(1);
           }
           else {
             System.out.print(0);
           }
         }
         System.out.println();
			 */
		}
		else { //still not enougth trues
			for (int i = step; i < tmp_result.length; i++) {
				tmp_result[i] = true;
				genCombis(tmp_result, i + 1, totaltrues, result);
				tmp_result[i] = false;
			}
		}
	}

	/**
	 * Returns default capabilities of the classifier.
	 *
	 * @return      the capabilities of this classifier
	 */
	public Capabilities getCapabilities() {
		Capabilities result = super.getCapabilities();

		// attributes
		result.disable(Capability.NOMINAL_ATTRIBUTES);
		result.disable(Capability.MISSING_VALUES);

		// class
		result.disable(Capability.NOMINAL_CLASS);
		result.disable(Capability.MISSING_CLASS_VALUES);

		//TOBEDONE

		return result;
	}

	/**
	 * Returns an enumeration describing the available options.
	 *
	 * @return an enumeration of all the available options.
	 */
	public Enumeration listOptions() {

		String string1 = "\tThe  the minimum support. (default = " +
		m_minSupport + ")",
		string2 = "\tThe minimum confidence. (default = " +
		m_minConfidence + ")",
		string3 = "\tIf enabled, also rules will be generated.(default = no)",
		algotype = "\tWhich apriori variant to use (default=apriori)",
		kcfile = "\tFile that contains knowledge";

		FastVector newVector = new FastVector(11);

		newVector.addElement(new Option(string1, "S", 1,
		"-S <minimum support>"));
		newVector.addElement(new Option(string2, "C", 1,
		"-C <minimum confidence>"));
		newVector.addElement(new Option(string3, "R", 0, "-R "));
		newVector.addElement(new Option(algotype, "T", 1,
		"-T <0=Apriori | 1=Apriori-kc | 2=Apriori-prune>"));
		newVector.addElement(new Option(kcfile, "F", 1,
		"-F <file location>"));

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
	 * <pre> -C
	 *  The minimum confidence</pre>
	 *
   <!-- options-end -->
	 *
	 * @param options the list of options as an array of strings
	 * @throws Exception if an option is not supported
	 */
	public void setOptions(String[] options) throws Exception {

		resetOptions();
		String minSupportString = Utils.getOption('S', options),
		minConfidenceString = Utils.getOption('C', options),
		algotypeString = Utils.getOption('T', options);
		
		if (algotypeString.length() != 0) {
			setalgoType(new SelectedTag(Integer.parseInt(algotypeString),
					TAGS_SELECTION));
		}
		if(m_algoType != APRIORI) {
			String kcfileString = Utils.getOption('F', options);
			m_kcFile = new File(kcfileString);
		}

		if (minSupportString.length() != 0) {
			m_minSupport = (new Double(minSupportString)).doubleValue();
		}
		if (minConfidenceString.length() != 0) {
			m_minConfidence = (new Double(minConfidenceString)).doubleValue();
		}

		m_buildRules = Utils.getFlag('R', options);
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
		options[current++] = "" + m_minSupport;
		options[current++] = "-C";
		options[current++] = "" + m_minConfidence;

		if (m_buildRules) {
			options[current++] = "-R";
		}

		options[current++] = "-T";
		options[current++] = "" + m_algoType;

		if(m_algoType != APRIORI) {
			options[current++] = "-F";
			options[current++] = "" + m_kcFile;
		}

		while (current < options.length) {
			options[current++] = "";
		}
		return options;
	}

	/**
	 * Outputs the size of all the generated sets of itemsets and the rules.
	 *
	 * @return a string representation of the model
	 */
	public String toString() {
		String result = "";
		//Give the support
		result += "Minimum support: " + m_minSupport + "(" + support + " transactions)\n";
		result += "Minimum confidence: " + m_minConfidence + "\n\n";

		//Frequent itemsets
		String tempResult = "";
		int totalCtr = 0;
		for (int i = 0; i < itemsets.size(); i++) {
			tempResult += (i + 1) + "-frequent itemsets\n";
			Hashtable<String,
			Integer> items = (Hashtable<String, Integer>) itemsets.get(i);
			Enumeration<String> en = items.keys();
			int ctr = 1;
			while (en.hasMoreElements()) {
				totalCtr++;
				String key = en.nextElement();
				Integer supp = items.get(key);
				tempResult += "\t(" + ctr++ + ") " + key + "  " + supp + "\n";
			}
		}
		result += "FREQUENT ITEMSETS (" + totalCtr + 
			")\n====================\n" + tempResult;

		//Frequent rules
		if (m_buildRules) {
			totalCtr = 0;
			tempResult = "";
			for (int i = 0; i < rules.size(); i++) {
				tempResult += (i + 2) + "-frequent rules\n";
				Hashtable<String,
				Double> krules = (Hashtable<String, Double>) rules.get(i);
				Enumeration<String> en = krules.keys();
				int ctr = 1;
				while (en.hasMoreElements()) {
					totalCtr++;
					String key = en.nextElement();
					Double conf = krules.get(key);
					tempResult += "\t(" + ctr++ + ") " + key + "   conf:(" + 
						Utils.doubleToString(conf,2) + ")\n";
				}
			}
			result += "\nFREQUENT RULES (" + totalCtr + 
				")\n====================\n" + tempResult;
		}

        result += "\n Total time: "
            + ((double)System.currentTimeMillis() - (double)startTime)/1000 + "s\n";

        return result;
	}

	/**
	 * Read the dependences from the file that was given
	 * (here: m_kcFile)
	 */
	private void readDependences() {
		dependences = new ArrayList<String>();
		if(m_kcFile.exists())
			try {
				BufferedReader file =
					new BufferedReader(new FileReader(m_kcFile));
				String line = "";

				while((line = file.readLine()) != null) {
					line = line.replaceAll("\\{", "");
					line = line.replaceAll("\\}", "");
					line = line.replaceAll(" ", "");
					line = line.replaceAll(",", " ");

					dependences.add(line);
					checkTransitivity();
				}
				System.out.println("read dependences: " + dependences.size());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

	/**
	 * Procedure used for checking the transitivity, when dependences
	 * are given. (APriori-KC)
	 */
	private void checkTransitivity() {
		boolean added = true;

		while(added) {
			ArrayList<String> toAdd = new ArrayList<String>();
			for(int i = 0; i < dependences.size(); i++) {
				String[] split = dependences.get(i).split(" ");
				for(int j = 0; j < dependences.size(); j++) {
					if(i != j) {
						String[] split2 = dependences.get(j).split(" ");
						String addString = split[0] + " " + split2[1];
						if(split[1].equals(split2[0])
								&& !split[0].equals(split2[1])
								&& !dependences.contains(addString))
							toAdd.add(addString);
					}
				}
			}
			added = toAdd.size() > 0;
			dependences.addAll(toAdd);
		}
	}

	/**
	 * Function to check if an itemset is a well-known dependence (APriori-KC)
	 * @param set The itemset that needs to be checked
	 * @return <code>True</code> if it is a dependence, <code>False</code>
	 * otherwise
	 */
	private boolean isDependence(String itemset) {
		boolean found = false;
		ArrayList<String> items = new ArrayList<String>();
		String[] itemsStrings = itemset.split(" ");
		for(int i = 0; i < itemsStrings.length; i++) {
			String it = itemsStrings[i];
			items.add(it);
		}

		for(int i = 0; i < dependences.size() && !found; i++) {
			String split[] = dependences.get(i).split(" ");
			boolean allFound = true;
			for(int j = 0; j < split.length && allFound; j++)
				allFound = items.contains(split[j]);
			found = allFound;
		}
		
		return found;
	}


	/**
	 * Returns the tip text for this property
	 * @return tip text for this property suitable for
	 * displaying in the explorer/experimenter gui
	 */
	public String minSupportTipText() {
		return "This is the support";
	}

	public double getminSupport() {
		return m_minSupport;
	}

	public void setminSupport(double v) {
		m_minSupport = v;
	}

	public String minConfidenceTipText() {
		return "This is the confidence";
	}

	public double getminConfidence() {
		return m_minConfidence;
	}

	public void setminConfidence(double v) {
		m_minConfidence = v;
	}

	public void setbuildRules(boolean flag) {
		System.out.println("setting buildRules to " + flag);
		m_buildRules = flag;
	}

	public boolean getbuildRules() {
		return m_buildRules;
	}

	public String buildRulesTipText() {
		return "If enabled, rules will be generated.";
	}

	public SelectedTag getalgoType() {
		return new SelectedTag(m_algoType, TAGS_SELECTION);
	}

	public String algoTypeTipText() {
		return "Set the type of algorithm by which find the rules.";
	}

	public void setalgoType(SelectedTag d) {

		if (d.getTags() == TAGS_SELECTION) {
			m_algoType = d.getSelectedTag().getID();
		}
	}

	public void setkcFile(File file) {
		m_kcFile = file;
	}

	public File getkcFile() {
		return m_kcFile;
	}

	public String kcFileTipText() {
		return "File that contains knowledge about data";
	}

	/**
	 * Main method.
	 *
	 * @param args the commandline options
	 */
	public static void main(String[] args) {
		runAssociator(new AprioriKC(), args);
	}
	
	public String getRevision() {		
		return RevisionUtils.extract("$Revision: 1.0 $");
	}
}
