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
package fr.crnan.videso3d;

/**
 * 
 * @author Bruno Spyckerelle
 * @version 0.2
 */
public class Point{
	/**
	 * Type de coordonnées
	 */
	public static enum Type {Cautra, Stéréographique};
	/**
	 * Rayon de la sphère conforme au centre de la grille, en NM
	 */
	private final double r0 = 3437.7737788646; //this.a*Math.cos(this.phi0)/(Math.sqrt(1-Math.pow(this.e*Math.sin(this.phi0), 2))*Math.cos(this.latitudeConforme(this.phi0)));
	/**
	 * Latitude géodésique du centre de la grille
	 */
	private final double l0 = 47; //en degrés
	private final double phi0 = l0/180*Math.PI; //en radians
	/**
	 * Excentricité
	 */
	private final double e = 0.081819191;
	/**
	 * Demi grand axe équatorial, en NM
	 */
	@SuppressWarnings("unused")
	private final double a = 3443.918467;
	/**
	 * Ordonnée du pôle nord, en NM
	 */
	private final double yPole = 2721.66;
	/**
	 * Abscisse Cautra
	 */
	private double x;
	/**
	 * Ordonnée Cautra
	 */
	private double y;
	/**
	 * Abscisse stéréographique en degrés
	 */
	private double l;
	/**
	 * Ordonnée stéréographique en degrés
	 */
	private double g;
	
	/**
	 * Construit un point à partir de coordonnées Cautra ou lat/long en degrés
	 * @param x double Abscisse 
	 * @param y double Ordonnée 
	 */
	public Point(double x, double y, Type type){
		switch (type) {
		case Cautra:
			this.x = x;
			this.y = y;
			this.toStereo();
			break;
		case Stéréographique:
			this.l = x;
			this.g = y;
			this.toCautra();
			break;
		default:
			break;
		}
	}

	private void toStereo() {
		double x_ = this.x;
		double y_ = this.y;
		this.l = this.phi0+Math.atan((this.yPole-Math.sqrt(x_*x_+Math.pow(y_ - this.yPole, 2)))/this.r0);
		this.l = this.l*180/Math.PI; //conversion en degrés
		this.g = Math.atan(x_/(this.yPole-y_))*this.yPole/(this.r0*Math.cos(this.phi0));
		this.g = this.g*180/Math.PI; //conversion en degrés
	}
	/**
	 * Latitude conforme du point
	 * @param phi double Latitude du point en radians
	 * @return double Latitude conforme, en radians
	 */
	private double latitudeConforme(double phi){
		return 2*Math.atan(Math.pow((1-this.e*Math.sin(phi))/(1+this.e*Math.sin(phi)),this.e/2)*Math.tan(Math.PI/4+phi/2))-Math.PI/2;
	}
	/**
	 * Construit les coordonnées Cautra à partir des coordonnées stéréographiques
	 */
	private void toCautra(){
		double phi = l/180*Math.PI;//latitude en radians
		double psi = g/180*Math.PI;//longitude en radians
		double lConforme = this.latitudeConforme(phi);//Latitude conforme du point
		double L0 = this.latitudeConforme(this.phi0);
		double k = 2/(1+Math.sin(L0)*Math.sin(lConforme)+Math.cos(L0)*Math.cos(lConforme)*Math.cos(psi)); 
		this.x =k*this.r0*Math.cos(lConforme)*Math.sin(psi);
		this.y =k*this.r0*(Math.cos(L0)*Math.sin(lConforme)-Math.sin(L0)*Math.cos(lConforme)*Math.cos(psi));
	}

	/**
	 * Renvoit les coordonnées Cautra
	 * @return Couple<Double, Double>
	 */
	public Couple<Double, Double> coordonneesCautra(){
		return new Couple<Double, Double>(x, y);
	}
	
	/**
	 * Renvoit les coordonnées lat/long en degrés
	 * @return Couple<Double, Double>
	 */
	public Couple<Double, Double> coordonneesStereo(){
		return new Couple<Double, Double>(l,g);
	}
	
	public String toString(){
		return "Lat : "+l+" - long : "+g;
	}

}
