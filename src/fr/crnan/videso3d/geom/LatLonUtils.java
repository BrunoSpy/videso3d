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

package fr.crnan.videso3d.geom;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.util.Logging;

/**
 * Classe d'outils de conversion 
 * @author Bruno Spyckerelle
 * @version 0.2
 */
public class LatLonUtils {
	/**
     * Tries to extract a latitude and a longitude from the given text string.
     * @author Patrick Murris
     * @param coordString the input string.
     * @param globe the current <code>Globe</code>.
     * @return the corresponding <code>LatLon</code> or <code>null</code>.
     */
    public static LatLon computeLatLonFromString(String coordString)
    {
        if (coordString == null)
        {
            String msg = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        Angle lat = null;
        Angle lon = null;
        coordString = coordString.trim();
        String regex;
        String separators = "(\\s*|,|,\\s*)";
        Pattern pattern;
        Matcher matcher;

        // Try to extract a pair of signed decimal values separated by a space, ',' or ', '
        // Allow E, W, S, N sufixes
        if (lat == null || lon == null)
        {
            regex = "([-|\\+]?\\d+?(\\.\\d+?)??\\s*[N|n|S|s]??)";
            regex += separators;
            regex += "([-|\\+]?\\d+?(\\.\\d+?)??\\s*[E|e|W|w]??)";
            pattern =  Pattern.compile(regex);
            matcher = pattern.matcher(coordString);
            if (matcher.matches())
            {
                String sLat = matcher.group(1).trim();  // Latitude
                int signLat = 1;
                char suffix = sLat.toUpperCase().charAt(sLat.length() - 1);
                if (!Character.isDigit(suffix))
                {
                    signLat = suffix == 'N' ? 1 : -1;
                    sLat = sLat.substring(0, sLat.length() - 1);
                    sLat = sLat.trim();
                }

                String sLon = matcher.group(4).trim();  // Longitude
                int signLon = 1;
                suffix = sLon.toUpperCase().charAt(sLon.length() - 1);
                if (!Character.isDigit(suffix))
                {
                    signLon = suffix == 'E' ? 1 : -1;
                    sLon = sLon.substring(0, sLon.length() - 1);
                    sLon = sLon.trim();
                }

                lat = Angle.fromDegrees(Double.parseDouble(sLat) * signLat);
                lon = Angle.fromDegrees(Double.parseDouble(sLon) * signLon);
            }
        }

        // Try to extract two degrees minute seconds blocks separated by a space, ',' or ', '
        // Allow S, N, W, E suffixes and signs.
        // eg: -123° 34' 42" +45°12' 30"
        // eg: 123° 34' 42"S 45° 12' 30"W
        if (lat == null || lon == null)
        {
            regex = "([-|\\+]?\\d{1,3}[d|D|\u00B0|\\s](\\s*\\d{1,2}['|\u2019|\\s])?(\\s*\\d{1,2}[\"|\u201d])?\\s*[N|n|S|s]?)";
            regex += separators;
            regex += "([-|\\+]?\\d{1,3}[d|D|\u00B0|\\s](\\s*\\d{1,2}['|\u2019|\\s])?(\\s*\\d{1,2}[\"|\u201d])?\\s*[E|e|W|w]?)";
            pattern =  Pattern.compile(regex);
            matcher = pattern.matcher(coordString);
            if (matcher.matches())
            {
                lat = parseDMSString(matcher.group(1));
                lon = parseDMSString(matcher.group(5));
            }
        }

        if (lat == null || lon == null)
            return null;

        if(lat.degrees >= -90 && lat.degrees <= 90 && lon.degrees >= -180 && lon.degrees <= 180)
            return new LatLon(lat, lon);

        return null;
    }
    
    /**
     * Extract a {@link LatLon} from a SkyView string
     * @author Bruno Spyckerelle
     * @param lat the latitude input string
     * @param lon the longitude input string
     * @return the corresponding {@link LatLon}
     */
    public static LatLon computeLatLonFromSkyviewString(String lat, String lon){
    	
    	double deg = 0;
    	
    	deg = Double.parseDouble(lat.substring(2, 4));
    	deg += Double.parseDouble(lat.substring(5, 7))/60;
    	deg += Double.parseDouble(lat.substring(8, 10))/3600;
    	if(lat.substring(0, 1).equals("S")){
    		deg *= -1.0;
    	}
    	
    	Angle latitude = Angle.fromDegrees(deg);
    	
    	deg = Double.parseDouble(lon.substring(2, 5));
    	deg += Double.parseDouble(lon.substring(6, 8))/60;
    	deg += Double.parseDouble(lon.substring(9, 11))/3600;
    	if(lon.substring(0, 1).equals("W")){
    		deg *= -1.0;
    	}
    	
    	Angle longitude = Angle.fromDegrees(deg);
    	
    	return new LatLon(latitude, longitude);
    }
    
    
    
    
    /**
     * Parse a Degrees, Minute, Second coordinate string.
     * @author Patrick Murris
     * @param dmsString the string to parse.
     * @return the corresponding <code>Angle</code> or null.
     */
    private static Angle parseDMSString(String dmsString)
    {
        // Replace degree, min and sec signs with space
        dmsString = dmsString.replaceAll("[D|d|\u00B0|'|\u2019|\"|\u201d]", " ");
        // Replace multiple spaces with single ones
        dmsString = dmsString.replaceAll("\\s+", " ");
        dmsString = dmsString.trim();

        // Check for sign prefix and suffix
        int sign = 1;
        char suffix = dmsString.toUpperCase().charAt(dmsString.length() - 1);
        if (!Character.isDigit(suffix))
        {
            sign = (suffix == 'N' || suffix == 'E') ? 1 : -1;
            dmsString = dmsString.substring(0, dmsString.length() - 1);
            dmsString = dmsString.trim();
        }
        char prefix = dmsString.charAt(0);
        if (!Character.isDigit(prefix))
        {
            sign *= (prefix == '-') ? -1 : 1;
            dmsString = dmsString.substring(1, dmsString.length());
        }

        // Process degrees, minutes and seconds
        String[] DMS = dmsString.split(" ");
        double d = Integer.parseInt(DMS[0]);
        double m = DMS.length > 1 ? Integer.parseInt(DMS[1]) : 0;
        double s = DMS.length > 2 ? Integer.parseInt(DMS[2]) : 0;

        if (m >= 0 && m <= 60 && s >= 0 && s <= 60)
            return Angle.fromDegrees(d * sign + m / 60 * sign + s / 3600 * sign);
        
        return null;
    }
    /**
     * Si les deux points sont à la même altitude, calcule la distance ellipsoïdale.<br />
     * Sinon, calcule la distance dans le repère Cautra. Cette méthode donne de bons résultats en France Métropolitaine, mais est inutilisable ailleurs.
     * @param pos1
     * @param pos2
     * @return
     */
    public static double computeDistance(Position pos1, Position pos2, Globe globe){
    	if(pos1.elevation == pos2.elevation){
    		return Position.ellipsoidalDistance(pos1, pos2, globe.getEquatorialRadius()+pos1.elevation, globe.getPolarRadius()+pos1.elevation);
    	} else {
    		LatLonCautra point1 = LatLonCautra.fromDegrees(pos1.latitude.degrees, pos1.longitude.degrees);
    		LatLonCautra point2 = LatLonCautra.fromDegrees(pos2.latitude.degrees, pos2.longitude.degrees);		
    		return Math.sqrt(Math.pow(point1.getCautra()[0]*LatLonCautra.NM-point2.getCautra()[0]*LatLonCautra.NM, 2)
    						+Math.pow(point1.getCautra()[1]*LatLonCautra.NM-point2.getCautra()[1]*LatLonCautra.NM, 2)
    						+Math.pow(pos1.elevation-pos2.elevation, 2));
    	}
    }
    
    public static String toLatLonToString(LatLon l){
    	String latString = l.getLatitude().toDMSString();
    	String degresLat = latString.split("°")[0];
    	if(degresLat.charAt(0)=='-')
    		degresLat = degresLat.substring(1);
    	degresLat = (degresLat.length()==1 ? "0"+degresLat : degresLat);
    	String minutesLat = latString.split("°")[1].split("’")[0];
    	minutesLat = (minutesLat.length()==2 ? "0"+minutesLat.trim() : minutesLat);
    	String secondesLat = latString.split("’")[1].split("”")[0];
    	secondesLat = (secondesLat.length()==2 ? "0"+secondesLat.trim() : secondesLat);
    	latString = degresLat + "° " + minutesLat + "’ " + secondesLat + "”";
		latString = (l.getLatitude().degrees>0? latString+" N" : latString.substring(1)+" S");
		
		String lonString = l.getLongitude().toDMSString();
		String degresLon = lonString.split("°")[0];
    	if(degresLon.charAt(0)=='-')
    		degresLon = degresLon.substring(1);
    	degresLon = (degresLon.length()==1 ? "0"+degresLon : degresLon);
    	String minutesLon = lonString.split("°")[1].split("’")[0];
    	minutesLon = (minutesLon.length()==2 ? "0"+minutesLon.trim() : minutesLon);
    	String secondesLon = lonString.split("’")[1].split("”")[0];
    	secondesLon = (secondesLon.length()==2 ? "0"+secondesLon.trim() : secondesLon);
    	lonString = degresLon + "° " + minutesLon + "’ " + secondesLon + "”";
		lonString += (l.getLongitude().degrees>0 ?  "E" : " W");
		
		String latLonString = latString+"   "+lonString;
    	return latLonString;
    }
}
