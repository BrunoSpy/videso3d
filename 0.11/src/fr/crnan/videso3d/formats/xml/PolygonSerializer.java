package fr.crnan.videso3d.formats.xml;

// import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

// import fr.crnan.test.data.RadioCoverageInit;
// import fr.crnan.videso3d.graphics.RadioCovPolygon;
// import gov.nasa.worldwind.layers.AirspaceLayer;
import gov.nasa.worldwind.render.airspaces.*;

public class PolygonSerializer {
	
	public void Serialize(ArrayList<Airspace> airspaces,String file)  {
		//public void Serialize(ArrayList<RadioCovPolygon> airspaces,String file)  {

		try {
			XStream xstream = new XStream(new DomDriver());	

			FileOutputStream fos = new FileOutputStream(file);
			try  {xstream.toXML(airspaces, fos);}
			finally {
				fos.close();
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}				        		            

		System.out.println("Sérialisation terminée");
	}
}