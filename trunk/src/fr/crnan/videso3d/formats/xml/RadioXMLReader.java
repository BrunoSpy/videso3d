/*
 * This file is part of ViDESO.
 * ViDESO is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ViDESO is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ViDESO.  If not, see <http://www.gnu.org/licenses/>.
 */

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
