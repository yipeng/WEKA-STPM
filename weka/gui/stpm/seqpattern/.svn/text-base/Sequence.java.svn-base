/*
 * Sequence.java
 *
 * Created on 25 de Setembro de 2007, 17:28
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package weka.gui.stpm.seqpattern;

import java.util.Vector;

/**
 *
 * @author Administrador
 */
public class Sequence {
    public Vector<String> item = new Vector<String>();
    public int support = 0;
    /** Creates a new instance of Sequence */
    public Sequence() {
    }
    
    public int length() {
        return item.size();
    }
    
    public boolean equals(Object obj) {
        try {
            Sequence s = (Sequence) obj;
            if (item.size() == s.item.size()) {
                for (int i=0;i<item.size();i++) {
                    if (item.elementAt(i).compareTo(s.item.elementAt(i)) != 0)
                        return false;
                }
                return true;
            }else
                return false;
        }catch(Exception e) {
            return false;
        }
    }
    
    public String insertCk(boolean insSup) {
        String desc ="";
        String value = "";
        for (int i=1;i<=item.size();i++) {
            desc += "item"+i+",";
            value+= "'"+item.elementAt(i-1)+"',";
        }
        desc = desc.substring(0,desc.length()-1);
        value = value.substring(0,value.length()-1);
        String sup = "",supValue="";
        if (insSup) {
            sup = ",support";
            supValue = ","+this.support;
        }
        return "INSERT INTO C"+item.size()+"("+desc+sup+") VALUES ("+value+supValue+")";            
    }
    
    public boolean isSubsequenceOf(Sequence seq) {
        int startIndex = 0;
        
        for (int i=0;i < item.size(); i++) {
            String s = item.elementAt(i);
            int index = seq.item.indexOf(s,startIndex);
            if (index != -1) {
                startIndex = index + 1;
            }else {
                return false;
            }
        }
        return true;
    }
    
    public Sequence generateSequence(Sequence seq) {
        if (this.item.size() == seq.item.size()) {
            Sequence ret = new Sequence();
            int i;
            for (i=0;i<this.item.size() - 1;i++) {
                if (this.item.elementAt(i).compareTo(seq.item.elementAt(i)) != 0)
                    return null;
                
                ret.item.addElement(this.item.elementAt(i));
            }
            ret.item.addElement(this.item.elementAt(i));
            ret.item.addElement(seq.item.elementAt(i));
            return ret;
        }else 
            return null;
    }
    
    public String toString() {
        String temp = "(";
        for (int i=0;i<item.size();i++)
            temp += item.elementAt(i)+",";
        temp = temp.substring(0,temp.length()-1) + ")\t Support: "+support;
        return temp;
    }
    
    public static void main(String[] args) {
        Sequence s1 = new Sequence();
        s1.item.addElement("1");
        //s1.item.addElement("2");
        //s1.item.addElement("3");
        Sequence s2 = new Sequence();
        s2.item.addElement("1");
        //s2.item.addElement("2");
        //s2.item.addElement("4");
        
        System.out.println(s2.generateSequence(s1).toString());
    }
}
