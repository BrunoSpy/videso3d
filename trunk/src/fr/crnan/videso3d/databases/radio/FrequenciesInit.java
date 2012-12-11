package fr.crnan.videso3d.databases.radio;

//import gov.nasa.worldwind.WorldWindow;
import fr.crnan.videso3d.graphics.RadioCovPolygon;
import fr.crnan.videso3d.layers.FilterableAirspaceLayer;
//import name.papail.gui.*;

import java.io.UnsupportedEncodingException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import gov.nasa.worldwind.render.airspaces.Airspace;

import gov.nasa.worldwind.layers.AirspaceLayer;
//import gov.nasa.worldwind.render.airspaces.Airspace;

public class FrequenciesInit {

	private ArrayList<Frequency> freqList = new ArrayList<Frequency>();
	private FilterableAirspaceLayer radioCovList;
	private Collection<Airspace> airspaces;
	private Collator collator = Collator.getInstance(Locale.FRENCH);
	
	public FrequenciesInit() {
		 this.addAll();
	}
	
	public FrequenciesInit(FilterableAirspaceLayer radioCovAirspaces) {	
		setRadioCoverages(radioCovAirspaces);
		this.addAll(); 
	}

	public void addAll() {
				
		freqList.add(new Frequency(0.0,"",null,null,null,null));
		freqList.add(new Frequency(118.230,"UP",match("Amboise"),match("Deauville"),null,null));		
		freqList.add(new Frequency(118.725,"OT",null,match("Deauville"),null,null));		
		freqList.add(new Frequency(120.950, "TL",match("Amboise"),null,null,null));
		
		freqList.add(new Frequency(121.500, "DETRESSE",match("Amboise"),match("Dijon"),match("Deauville"),match("troyes")));
		freqList.add(new Frequency(122.575, "RT",null,null,null,null));
		freqList.add(new Frequency(122.615, "HP",null,null,null,null));
		freqList.add(new Frequency(124.000, "TL",null,null,null,null));
		freqList.add(new Frequency(124.625, "DG",null,null,null,null));
		freqList.add(new Frequency(124.850, "TH",null,null,null,null));
		freqList.add(new Frequency(125.075, "AO",null,null,null,null));
		freqList.add(new Frequency(125.150, "VOL FR",null,null,null,null));
		freqList.add(new Frequency(125.450, "S",null,null,null,null));
		freqList.add(new Frequency(125.700, "INFO NORD",null,null,null,null));
		freqList.add(new Frequency(126.000, "VOL AN",null,null,null,null));
		freqList.add(new Frequency(126.100, "INFO SUD",null,null,null,null));
		freqList.add(new Frequency(127.075, "DS",null,null,null,null));
		freqList.add(new Frequency(127.305, "OG",null,null,null,null));
		freqList.add(new Frequency(128.100, "TE",null,null,null,null));
		freqList.add(new Frequency(128.140, "US",null,null,null,null));
		freqList.add(new Frequency(128.275, "TB",null,null,null,null));		
	}

	public ArrayList<Frequency> getFrequencies() {
		return freqList;
	}
	
/*	
	public static void printBytes(byte[] array, String name) {
	    for (int k = 0; k < array.length; k++) {
	        System.out.println(name + "[" + k + "] = " + "0x" +
	            UnicodeFormatter.byteToHex(array[k]));
	    }
	}
*/	
	
	/*Recherche un polygone dans la liste des polygones*/
	public RadioCovPolygon match(String name) {
		for (Airspace a : airspaces) {			
			try {
					// 	suppression d'un retoutr chariot en début de caractère qui faussait les résultats de la méthode equals
					String temp = ((RadioCovPolygon) a).getName(); 
					String radioCovString = (temp.substring(1,(temp.length())));
					if  (radioCovString.equals(name) ) {
						return (RadioCovPolygon)a;
					}					
//					byte[] utf8RadioCov = ((RadioCovPolygon) a).getName().getBytes("UTF8");
//					byte[] utf8Name = name.getBytes("UTF8");															
		
			} 
			catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
			}					        			
										
		}
		return null;		
	}
	
	
	public String[] getFreqValues() {
		String[] tab= new String[freqList.size()];
		int i=0;
		for (Frequency freq : freqList) {tab[i]=freq.getInfos(); i++;}		
		return tab;
	}

	public void setRadioCoverages(FilterableAirspaceLayer radioCovAirspaces) {		
		this.radioCovList = radioCovAirspaces;
		//airspaces = radioCovList.getAllAirspaces();
		airspaces = (Collection<Airspace>) radioCovAirspaces.getAirspaces();	
	}
	
}	
