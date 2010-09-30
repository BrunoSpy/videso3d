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

import fr.crnan.videso3d.Couple;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
/**
 * Ajout du support de la projection Cautra dans {@link LatLon}<br />
 * La projection Cautra est une projection stéréographique polaire.<br />
 * Les explications des formules sont dans le répertoire doc/import
 * @author Bruno Spyckerelle
 * @version 0.2
 */
public class LatLonCautra extends LatLon {

	public static final double WGS84_EQUATORIAL_RADIUS = 6378137.0; // ellipsoid equatorial getRadius, in meters
    public static final double WGS84_POLAR_RADIUS = 6356752.314; // ellipsoid polar getRadius, in meters
    public static final double WGS84_ES = 0.00669437999014; // eccentricity squared, semi-major axis
    public static final double WGS84_E = 0.081819190842622; //eccentricity	
    public static final double NM = 1852;//NM en m
	
    private static final double L0 = 0.8169557248772987; //latitude conforme au point d'origine, en radians
    private static final double R0 = 6366757.037688594; //rayon de la sphère conforme au point d'origine, en mètres
    private static final double y0 = -5040511.788585899; //ordonnée de P0 dans le plan tangent, en mètres
    
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
	public static LatLonCautra fromCautra(double x, double y){		
		double[] latlon = toStereo(x*NM, y*NM);
		
		return LatLonCautra.fromRadians(latlon[0], latlon[1]);
	}
	
	
	public static LatLonCautra fromCautraExacte(double x, double y){
		LatLonCautra result = LatLonCautra.fromDegrees(47+y/60.0, x/60.0);
		/** Par defaut, la position corrigee initiale est la position donnee;
		 *  ceci pour correspondre a un ecart initial nul.
		 */
		double[] corrige;
		Couple<Double,Double> ecart = new Couple<Double,Double>(0.,0.);
		boolean exit=false;
		int compteur=0;
		do{
			compteur+=1;
			result = LatLonCautra.fromDegrees(result.latitude.degrees-ecart.getSecond()/60.0, result.longitude.degrees-ecart.getFirst()/60.0);
			corrige = LatLonCautra.fromDegrees(result.latitude.degrees, result.longitude.degrees).getCautra();
			ecart = new Couple<Double,Double>(corrige[0]-x,corrige[1]-y);
			exit=(ecart.getFirst()<0.00001 && ecart.getSecond()<0.00001);
		}
		while(!exit);
		/**  ALGORITHME ITERATIF DE POSITION EXACTE:
		 *  INITIALISATION:
		 *  on approxime lineairement les latitude et longitude (position WGS84)
		 *  initiales a l'aide de la relation 1 NM <--> 1/60 de degre
		 *  et des coordonnees CAUTRA passees en parametres
		 *  rappel: l'origine CAUTRA est en 47N 0W
		 */
		return result;
	}
	
	/*-----------------------------------------------------*/
	/*---------- Transformation inverse quasi-exacte ------*/
	/*-----------------------------------------------------*/
	/**
	 * Projection inverse quasi-exacte
	 * @param x abscisse cautra en mètres
	 * @param y ordonnée cautra en mètres
	 * @return latitude et longitude en radians
	 */
	public static double[] toStereo(double x, double y){
		double[] latlon = {0, 0};
		
		//changement de plan stéréo
		double a = 4*Math.pow(R0, 2)-y0*y;
		double b = y0*x;
		double c = 4*Math.pow(R0, 2)*x;
		double d = 4*Math.pow(R0, 2)*(y+y0);
		double u = (a*c+b*d)/(Math.pow(a, 2)+Math.pow(b, 2));
		double v = (a*d-b*c)/(Math.pow(a, 2)+Math.pow(b, 2));
		
		//latitude géodésique
		double l = Math.PI/2-2*Math.atan(Math.sqrt(Math.pow(u, 2)+Math.pow(v, 2))/(2*R0));
		latlon[0] = 2*Math.atan(Math.pow((1+WGS84_E*Math.sin(l))/(1-WGS84_E*Math.sin(l)), WGS84_E/2)*Math.tan(Math.PI/4+l/2))-Math.PI/2;
		
		//longitude géodésique
		if(v < 0){
			latlon[1] = -Math.atan(u/v);
		} else if ( v >= 0 && u > 0) {
			latlon[1] = Math.PI/2 + Math.atan(v/u);
		} else if ( v >= 0 && u < 0) {
			latlon[1] = -Math.PI/2 + Math.atan(v/u);
		}
		
		return latlon;
	}
	
	/*-----------------------------------------------------*/
	/*----------- Projection stéréographique polaire ------*/
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
