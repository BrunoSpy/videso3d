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

/** SAXON 9.0 IMPLEMENTS XPATH 2.0 
 * @author mickael papail 
 * @version 0.1
 * */

package fr.crnan.videso3d.formats.xml;

import java.io.*;

import org.xml.sax.SAXException;
import java.net.MalformedURLException;
//import net.sf.saxon.functions.*;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class SaxonFactory {

	public static void SaxonJob(String XSL, String XML_in ,String XML_out)
		throws MalformedURLException, IOException, SAXException {
		
		System.setProperty("javax.xml.Transform.TransformerFactory","net.sf.saxon.TransformerFactoryImpl");
		
//		Instantiate the TransformerFactory, and use a StreamSource
		try {
			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer transformer = tFactory.newTransformer(new StreamSource(new File(XSL)));
			transformer.transform(new StreamSource(new File(XML_in)),new StreamResult(new FileOutputStream(XML_out)));
			// transformer.transform(new StreamSource(new File(XML_in)),new StreamResult(XML_out));
		}
		catch (Exception e) {
			e.printStackTrace();
		}		
	}				
	
	
	public static void SaxonJobWithParam(String XSL, String XML_in ,String XML_out,String param)
	throws MalformedURLException, IOException, SAXException {
	
	System.setProperty("javax.xml.Transform.TransformerFactory","net.sf.saxon.TransformerFactoryImpl");
	
//	Instantiate the TransformerFactory, and use a StreamSource
	try {
	
		TransformerFactory tFactory = TransformerFactory.newInstance();
		Transformer transformer = tFactory.newTransformer(new StreamSource(new File(XSL)));
		transformer.setParameter("paramName", param);
		// xslControl.setStylesheetParam("name", "value").
		transformer.transform(new StreamSource(new File(XML_in)),new StreamResult(new FileOutputStream(XML_out)));
		// transformer.transform(new StreamSource(new File(XML_in)),new StreamResult(XML_out));
	}
	catch (Exception e) {
		e.printStackTrace();
	}		
}
	
}
