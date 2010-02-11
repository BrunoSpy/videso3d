package fr.crnan.videso3d.formats.xml;

/**
 * @author mickaël PAPAIL
 * @version 0.1
 * 
 * This class loads the file containing the polygon Datas in XML and transforms it into a collection of Polygon (by using Xstream api)
 * 
 */

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import java.util.ArrayList;

import fr.crnan.videso3d.graphics.*;
import gov.nasa.worldwind.render.airspaces.Airspace;

import java.io.*;

public class RadioXMLReader extends XMLReader{	
	
	private ArrayList<RadioCovPolygon> radioCovPolygons = new ArrayList<RadioCovPolygon>(); // ArrayList of  polygons
	
	public RadioXMLReader() {
		super();
	}
	
	/**
	 * 
	 * @param filePath
	 * @return arrayList of RadioCovPolygons.
	 */
	public ArrayList<RadioCovPolygon> LoadRadioData(String filePath) {		
		try {
			XStream xstream = new XStream(new DomDriver());
			radioCovPolygons = (ArrayList<RadioCovPolygon>) xstream.fromXML(LoadXML(filePath));
			return radioCovPolygons;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
