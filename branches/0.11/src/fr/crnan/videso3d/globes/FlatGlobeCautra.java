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

import fr.crnan.videso3d.geom.LatLonCautra;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.globes.FlatGlobe;
/**
 * Ajoute la projection Cautra à la classe {@link FlatGlobe}
 * La projection par défaut est la projection Cautra.<br />
 * @author Bruno Spyckerelle
 * @version 0.3
 */
public class FlatGlobeCautra extends FlatGlobe {

	/**
	 * Projection CAUTRA : stéréographique polaire
	 */
    public final static String PROJECTION_CAUTRA = "gov.nasa.worldwind.globes.projectionCautra";

    /**
	 * Rayon de la sphère conforme au centre de la grille, en mètres
	 */
	private static final double r0 = 6366757.037688594; 
	
	/**
	 * Globe projeté. Projection Cautra par défaut.
	 * @param equatorialRadius equatorial radius, in meters
	 * @param polarRadius polar radius, in meters
	 * @param es eccentricity squared
	 * @param em {@link ElevationModel}
	 */
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
			
			double[] latlon = LatLonCautra.toStereo(cart.x, cart.y);
			
			pos = Position.fromRadians(
					latlon[0],
					latlon[1],
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
			
		//	if(latitude.degrees < -42 && latitude.degrees > -53) latitude = Angle.fromDegrees(-42);
			
			if(longitude.degrees > 160 ) longitude = Angle.fromDegrees(160);
			if(longitude.degrees < -160) longitude = Angle.fromDegrees(-160);
			
			double phi = latitude.radians;//latitude en radians
			double psi = longitude.radians;//longitude en radians
			double lConforme = this.latitudeConforme(phi);//Latitude conforme du point
			double L0 = this.latitudeConforme(47.0/180.0*Math.PI);
			double k = 2/(1+Math.sin(L0)*Math.sin(lConforme)+Math.cos(L0)*Math.cos(lConforme)*Math.cos(psi));
			cart = new Vec4(k*r0*Math.cos(lConforme)*Math.sin(psi),
					k*r0*(Math.cos(L0)*Math.sin(lConforme)-Math.sin(L0)*Math.cos(lConforme)*Math.cos(psi)),
							metersElevation);
		} else {
			cart = super.geodeticToCartesian(latitude, longitude, metersElevation);
		}
		return cart;
	}

	
	
}
