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
package fr.crnan.videso3d.globes;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.globes.FlatGlobe;
/**
 * Ajoute la projection Cautra à la classe {@link FlatGlobe}
 * La projection par défaut est la projection Cautra
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class FlatGlobeCautra extends FlatGlobe {

    public final static String PROJECTION_CAUTRA = "gov.nasa.worldwind.globes.projectionCautra";

    /**
	 * Rayon de la sphère conforme au centre de la grille, en mètres
	 */
	private final double r0 = 3437.7737788646*1852; //this.a*Math.cos(this.phi0)/(Math.sqrt(1-Math.pow(this.e*Math.sin(this.phi0), 2))*Math.cos(this.latitudeConforme(this.phi0)));
	/**
	 * Latitude géodésique du centre de la grille
	 */
	private final double l0 = 47; //en degrés
	private final double phi0 = l0/180*Math.PI; //en radians
	
	/**
	 * Ordonnée du pôle nord, en metres
	 */
	private final double yPole = 2721.66*1852;
	
	public FlatGlobeCautra(double equatorialRadius, double polarRadius,
			double es, ElevationModel em) {
		super(equatorialRadius, polarRadius, es, em);
		super.setProjection(PROJECTION_CAUTRA);
	}
	
	/**
	 * Latitude conforme du point
	 * @param phi double Latitude du point en radians
	 * @return double Latitude conforme, en radians
	 */
	private double latitudeConforme(double phi){
		return 2*Math.atan(Math.pow((1-Math.sqrt(this.es)*Math.sin(phi))/(1+Math.sqrt(this.es)*Math.sin(phi)),Math.sqrt(this.es)/2)*Math.tan(Math.PI/4+phi/2))-Math.PI/2;
	}

	/* (non-Javadoc)
	 * @see gov.nasa.worldwind.globes.FlatGlobe#cartesianToGeodetic(gov.nasa.worldwind.geom.Vec4)
	 */
	@Override
	protected Position cartesianToGeodetic(Vec4 cart) {
		Position pos = null;
		if(this.getProjection().equals(PROJECTION_CAUTRA)){
			pos = Position.fromRadians(
					this.phi0+Math.atan((this.yPole-Math.sqrt(cart.x*cart.x+Math.pow(cart.y - this.yPole, 2)))/this.r0),
					Math.atan(cart.x/(this.yPole-cart.y))*this.yPole/(this.r0*Math.cos(this.phi0)),
					cart.z);
		} else {
			pos = super.cartesianToGeodetic(cart);
		}
		return pos;
	}


	/* (non-Javadoc)
	 * @see gov.nasa.worldwind.globes.FlatGlobe#geodeticToCartesian(gov.nasa.worldwind.geom.Angle, gov.nasa.worldwind.geom.Angle, double)
	 */
	@Override
	protected Vec4 geodeticToCartesian(Angle latitude, Angle longitude,
			double metersElevation) {
		Vec4 cart = null;
		if(this.getProjection().equals(PROJECTION_CAUTRA)){
			double phi = latitude.radians;//latitude en radians
			double psi = longitude.radians;//longitude en radians
			double lConforme = this.latitudeConforme(phi);//Latitude conforme du point
			double L0 = this.latitudeConforme(47.0/180.0*Math.PI);
			double k = 2/(1+Math.sin(L0)*Math.sin(lConforme)+Math.cos(L0)*Math.cos(lConforme)*Math.cos(psi));
			cart = new Vec4(k*this.r0*Math.cos(lConforme)*Math.sin(psi),
					k*this.r0*(Math.cos(L0)*Math.sin(lConforme)-Math.sin(L0)*Math.cos(lConforme)*Math.cos(psi)),
							metersElevation);
		} else {
			cart = super.geodeticToCartesian(latitude, longitude, metersElevation);
		}
		return cart;
	}

	
	
}
