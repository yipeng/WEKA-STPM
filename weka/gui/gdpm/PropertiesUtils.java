package weka.gui.gdpm;

import java.io.*;
import java.util.*;
import java.util.jar.*;

public class PropertiesUtils {

	private File f = null;
	private FileWriter fw = null;
	private BufferedReader br = null;
	private Properties props = new Properties();
        private String bdPreName;

        public void setbdPreName(String sAux){
            bdPreName  = sAux;
        }
        
        public String getbdPreName(){
            return bdPreName;
        }
        
	public void setProps(String chave, String valor) throws Exception {
		props.setProperty(chave, valor);		            
	}

        public void LoadProps(){
		try {           
                    try{
                    	//when the jar is created
						JarFile jarFile = new JarFile("weka.jar");                    
	                    JarEntry entry = jarFile.getJarEntry("weka/gui/gdpm/res/gdpm.props");                    
	                    InputStream input = jarFile.getInputStream(entry);                                        
	                    InputStreamReader isr = new InputStreamReader(input);
	                    br = new BufferedReader(isr);
                    }
                    catch(FileNotFoundException e){
                    	//when programming in workspace/Eclipse
                    	br = new BufferedReader(new FileReader("src/weka/gui/gdpm/res/gdpm.props"));
                    }
                    
                    System.out.println("JAR Carregado");
                    props.load(br);
                    br.close();                    
		} catch(Exception e) {
                    e.printStackTrace();
            	}
        }
        
	public String getProps(String KeyW) throws FileNotFoundException, IOException {				
		if (br == null) {
			LoadProps();		
		}
		return (String)props.getProperty(KeyW);
	}
        
        public String getSql(String keyW)  {
            try {
                //?????????????
		return getProps(bdPreName+"_"+keyW.trim());
            } catch (Exception vErro){
                   vErro.printStackTrace();
                   return "";
            }
	}
        
        public void SaveProps() throws FileNotFoundException, IOException {
		if (fw == null)
			fw = new FileWriter(f);				
		props.store(fw, "");
        }
	
}
