package fr.crnan.videso3d.formats.xml;

import java.io.*;

//import com.thoughtworks.xstream.XStream;
//import com.thoughtworks.xstream.io.xml.DomDriver;

public abstract class XMLReader implements Reader {

	public FileInputStream LoadXML(String filePath) {
		try {	
			return new FileInputStream(filePath);
		}
		catch (FileNotFoundException e) {
        	e.printStackTrace();        	
        } 
        catch (IOException ioe) {
			ioe.printStackTrace();			
        }	
        return null;
	}	
}
