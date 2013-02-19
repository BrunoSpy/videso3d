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
				
		freqList.add(new Frequency(129.005,"UK - 129.005",match("tanville"),match("Loc"),null,null));
		freqList.add(new Frequency(132.885,"TU - 132.885",match("Dijon"),match("Etampes"),null,null));
		freqList.add(new Frequency(372.600,"UZ-U - 372.600",match("Etampes"),match("Loc"),null,null));
		freqList.add(new Frequency(128.100,"TE - 128.100",match("trigny"),match("saint quentin"),null,null));		
		freqList.add(new Frequency(133.505,"UT - 133.505",match("Saint Saulge"),match("Etampes"),null,null));		
		freqList.add(new Frequency(120.950, "UP-U - 120.950",match("Etampes"),null,null,null));		
		freqList.add(new Frequency(127.305, "OG - 127.305",match("Amboise"),match("Etampes"),null,null));
		freqList.add(new Frequency(233.650, "TE-U - 233.650",match("Etampes"),null,null,null));
		freqList.add(new Frequency(128.275, "TB - 128.275",match("saint quentin"),match("Loc"),null,null));
		freqList.add(new Frequency(131.175, "TM - 131.175",match("trigny"),match("saint quentin"),null,null));
		freqList.add(new Frequency(375.900, "AO-U - 375.900",match("Etampes"),null,null,null));
		freqList.add(new Frequency(131.255, "UZ - 131.255",match("Etampes"),match("Loc"),null,null));
		freqList.add(new Frequency(132.380, "UJ - 132.380",match("Dijon"),match("Etampes"),null,null));
		freqList.add(new Frequency(371.450, "SU-U - 371.450",match("Etampes"),null,null,null));
		freqList.add(new Frequency(131.065, "AP - 131.065",match("Etampes"),match("Loc"),null,null));
		freqList.add(new Frequency(132.740, "PU - 132.740",match("Saint Saulgeut"),match("Etampes"),null,null));
		freqList.add(new Frequency(315.450, "OG-U - 315.450",match("Etampes"),null,null,null));
		freqList.add(new Frequency(127.075, "DS - 127.075",match("Etampes"),match("Loc"),null,null));
		freqList.add(new Frequency(369.550, "UT-U - 369.550",match("Lyon"),null,null,null));
		freqList.add(new Frequency(122.615, "HP - 122.615",match("Saint Saulge"),match("Etampes"),null,null));
		freqList.add(new Frequency(336.500, "UJ-U - 336.500",match("Lyon"),null,null,null));
		freqList.add(new Frequency(132.675, "SPE - 132.675",match("Dijon"),match("Etampes"),null,null));
		freqList.add(new Frequency(122.575, "RT - 122.575",match("tanville"),match("Etampes"),null,null));
		freqList.add(new Frequency(132.100, "AR - 132.100",match("Etampes"),match("Loc"),match("Dijon"),match("Saint Saulge")));
		freqList.add(new Frequency(125.150, "VOL FR - 125.150",match("Etampes"),null,null,null));
		freqList.add(new Frequency(135.550, "TN - 135.550",match("Dieppe"),match("Loc"),null,match("saint quentin")));
		freqList.add(new Frequency(125.450, "S - 125.450",match("Saint Saulge"),null,null,null));
		freqList.add(new Frequency(126.000, "VOL AN - 126.000",match("Etampes"),null,null,null));
		freqList.add(new Frequency(124.850, "TH - 124.850",match("Etampes"),match("Loc"),match("Deauville"),match("Dieppe")));
		freqList.add(new Frequency(135.305, "SU - 135.305",match("Etampes"),match("Loc"),null,null));
		freqList.add(new Frequency(135.750, "MAINT - 135.750",match("Etampes"),null,null,null));
		freqList.add(new Frequency(128.140, "US - 128.140",match("Saint Saulge"),match("Etampes"),null,null));
		freqList.add(new Frequency(121.500, "DETRESSE - 121.500 ",match("Etampes"),null,null,null));
		freqList.add(new Frequency(128.875, "TP - 128.875",match("Etampes"),match("Loc"),match("Dieppe"),match("Deauville")));
		freqList.add(new Frequency(124.625, "DG - 124.625",match("Etampes"),match("Loc"),null,null));
		freqList.add(new Frequency(130.230, "CER - 130.230",match("Etampes"),null,null,null));
		freqList.add(new Frequency(125.700, "INFO N - 125.700",match("Dieppe"),null,null,null));
		freqList.add(new Frequency(132.265, "OY - 132.265",match("Amboise"),match("Etampes"),null,null));
		freqList.add(new Frequency(120.950, "TL - 120.950",match("trigny"),match("troyes"),null,null));
		freqList.add(new Frequency(136.425, "CER - 136.425",match("Etampes"),null,null,null));
		freqList.add(new Frequency(126.100, "INFO S - 126.100",match("Etampes"),null,match("Saint Saulge"),null));
		freqList.add(new Frequency(118.725, "OT - 118.725",match("Amboise"),match("Etampes"),null,null));
		freqList.add(new Frequency(125.075, "AO - 125.075",match("Etampes"),match("Loc"),null,null));
		freqList.add(new Frequency(141.675, "CER - 141.675",match("Etampes"),null,null,null));
		freqList.add(new Frequency(129.625, "INFO W -129.625",match("Etampes"),null,match("Amboise"),null));
		freqList.add(new Frequency(133.925, "SPW - 133.925",match("saint quentin"),match("Loc"),null,null));
		freqList.add(new Frequency(135.400, "DO - 135.400",match("Etampes"),match("Loc"),null,null));
		freqList.add(new Frequency(376.700, "CER - 376.700",match("Etampes"),null,null,null));
		freqList.add(new Frequency(129.150, "DEB AR - 129.150",match("Etampes"),match("Loc"),null,match("Dijon")));
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
