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

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
/**
 * Ajout du support de la projection Cautra dans {@link LatLon}
 * @author Bruno Spyckerelle
 *
 */
public class LatLonCautra extends LatLon {

	public static final double WGS84_EQUATORIAL_RADIUS = 6378137.0; // ellipsoid equatorial getRadius, in meters
    public static final double WGS84_POLAR_RADIUS = 6356752.3; // ellipsoid polar getRadius, in meters
    public static final double WGS84_ES = 0.00669437999014; // eccentricity squared, semi-major axis
    public static final double WGS84_E = 0.081819190842622; //eccentricity	
    public static final double NM = 1852;//NM en m
	
    private static final double L0 = 0.8169557248772987; //latitude conforme au point d'origine, en radians
    private static final double R0 = 6366757.037688594; //rayon de la sphère conforme au point d'origine, en mètres
  //  private static final double yPole = 2721.66; //ordonnée du pole nord, en NM
	/**
	 * Abscisse Cautra
	 */
	private Double x;
	/**
	 * Ordonnée Cautra
	 */
	private Double y;
	
	/**
	 * Centre de la grille Cautra
	 */
	public final static LatLon centerCautra = LatLon.fromDegrees(47, 0);
	
	private LatLonCautra(Angle latitude, Angle longitude) {
		super(latitude, longitude);
	}

	public LatLonCautra(double latitude, double longitude) {
		super(Angle.fromDegrees(latitude), Angle.fromDegrees(longitude));
	}

	public static LatLonCautra fromDegrees(double latitude, double longitude){
		return new LatLonCautra(latitude, longitude);
	}
	
	public static LatLonCautra fromRadians(double latitude, double longitude){
		return new LatLonCautra(Math.toDegrees(latitude), Math.toDegrees(longitude));
	}
	/**
	 * 
	 * @return coordonnées cautra
	 */
	public double[] getCautra(){
		double[] result = {0, 0};
		if(x != null && y != null){
			result[0] = x;
			result[1] = y;
		} else {
			result = toCautra(this.getLatitude().radians, this.getLongitude().radians);
			this.x = result[0]; //on stocke les résultats afin de ne pas refaire les calculs à chaque fois
			this.y = result[1];
		}
		return result;
	}
	
	/**
	 * Factor method for obtainin a new <code>LatLon</code> from Cautra coordinates
	 * @param x Abscisse cautra en NM
	 * @param y Ordonnée cautra en NM
	 * @return a new {@link LatLon}
	 */
//	public static LatLonCautra fromCautra(double x, double y){
//		double latitude = centerCautra.getLatitude().radians+Math.atan((yPole-Math.sqrt(x*x+Math.pow(y - yPole, 2)))/(R0/NM));;
//		double longitude = Math.atan(x/(yPole-y))*yPole/((R0/NM)*Math.cos(centerCautra.getLatitude().radians));
//
//		return LatLonCautra.fromDegrees(Math.toDegrees(latitude), Math.toDegrees(longitude));
//	}
	public static LatLonCautra fromCautra(double x, double y){
		
		double cosQB = (4*Math.pow(WGS84_EQUATORIAL_RADIUS/NM, 2))/(4*Math.pow(WGS84_EQUATORIAL_RADIUS/NM, 2) + Math.pow(x, 2)+ Math.pow(y, 2));
		double sinQB = 1 -cosQB;
		
		double xa = x * cosQB;
		double ya = (WGS84_EQUATORIAL_RADIUS/NM * Math.cos(centerCautra.getLatitude().radians) - y * Math.sin(centerCautra.getLatitude().radians))*cosQB - WGS84_EQUATORIAL_RADIUS/NM*sinQB*Math.cos(centerCautra.getLatitude().radians);
		
		double latitude = Math.atan(Math.sqrt( Math.pow(WGS84_EQUATORIAL_RADIUS/NM, 2)/(Math.pow(xa, 2)+Math.pow(ya, 2)) - 1));
		
		double longitude = Math.atan(xa/ya);
		
		return LatLonCautra.fromRadians(latitude, longitude);
	}
	/*-----------------------------------------------------*/
	
	/**
	 * Latitude conforme en un point
	 * @param latitude géodésique en radians
	 * @return latitude conforme en radians
	 */
	public static double latitudeConforme(double latitude){
		return 2*Math.atan(Math.pow((1-WGS84_E*Math.sin(latitude))/(1+WGS84_E*Math.sin(latitude)), WGS84_E/2)*Math.tan(Math.PI/4+latitude/2))-Math.PI/2;
	}
	
	/**
	 * Rayon de la sphère conforme en un point
	 * @param latitude géodésique en radians
	 * @return Rayon de la sphère conforme, en mètres
	 */
	public static double rayonConforme(double latitude){
		return (WGS84_EQUATORIAL_RADIUS/Math.sqrt(1-WGS84_ES*Math.pow(Math.sin(latitude), 2)))*(Math.cos(latitude)/Math.cos(latitudeConforme(latitude)));
	}
	/**
	 * Facteur d'échelle K en un point
	 * @param latitude géodésique en radians
	 * @param longitude géodésique en radians
	 * @return facteur d'échelle K en un point
	 */
	public static double facteurK(double latitude, double longitude){
		return 2/(1+Math.sin(L0)*Math.sin(latitudeConforme(latitude))+Math.cos(L0)*Math.cos(latitudeConforme(latitude))*Math.cos(longitude));
	}
	/**
	 * Coordonnées Cautra en NM
	 * @param latitude géodésique en radians
	 * @param longitude géodésique en radians
	 * @return coordonnées Cautra en radians
	 */
	public static double[] toCautra(double latitude, double longitude){
		double[] result = {0, 0};
		double k = facteurK(latitude, longitude);
		result[0] = k*R0*Math.cos(latitudeConforme(latitude))*Math.sin(longitude)/NM;
		result[1] = k*R0*(Math.cos(L0)*Math.sin(latitudeConforme(latitude))-Math.sin(L0)*Math.cos(latitudeConforme(latitude))*Math.cos(longitude))/NM;		
		return result;
	}
}
