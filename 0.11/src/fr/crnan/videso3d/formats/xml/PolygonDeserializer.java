package fr.crnan.videso3d.formats.xml;

// import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/*
import fr.crnan.videso3d.graphics.RadioCovPolygon;
import gov.nasa.worldwind.layers.AirspaceLayer;
import gov.nasa.worldwind.render.airspaces.*;
*/

import gov.nasa.worldwind.render.airspaces.*;

public class PolygonDeserializer {

	 //public ArrayList<RadioCovPolygon> Deserialize(String file) {
	@SuppressWarnings("unchecked")
	public ArrayList<Airspace> Deserialize(String file) {
		//ArrayList<RadioCovPolygon> newPoly;
		ArrayList<Airspace> newPoly;		
		try {		
				XStream xstream = new XStream(new DomDriver());				
				FileInputStream fis = new FileInputStream(file);
				try {														
					 newPoly =(ArrayList<Airspace>)xstream.fromXML(fis);
					//newPoly =(ArrayList<RadioCovPolygon>)xstream.fromXML(fis);
					 return newPoly;
				} 
				finally {			
					// Le  deuxieme bloc Try/catch pour gere les  pbs de portee.
					fis.close();					
				}			
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