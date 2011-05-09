package fr.crnan.videso3d.radio;

import java.util.regex.*; 

public class FilePatterns {

	public static Pattern AntennaNamePattern = Pattern.compile("^[a-zA-Z]*FL[0-9]*.txt$"); ; // Every string that looks like antennaFLxxx.txt where xxx is flight level  
	
	public static Pattern xmlFilePattern = Pattern.compile("^[a-zA-Z]*.xml$");		

	public static String radioCoveragePattern = "(\\s++)(-??)(\\d+,\\d+)(\\s++)(-??)(\\d+,\\d+)(\\s++)(-??)(\\d+,\\d+)(\\s++)(\\d)"; // The content of radio Mobile file.		

	public static String radioCoverageAltitudePattern ="mobile\\s\\d\\s[a-zA-Z\\s0-9]*[(](\\d+)[\\s??m)]";  // catch The altitude data into the file*	
	
	public static Pattern xmlInPattern =Pattern.compile("radioCoverage.xml"); // for testing purpose only
}
