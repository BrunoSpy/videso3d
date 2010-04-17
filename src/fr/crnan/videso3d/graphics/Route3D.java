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
package fr.crnan.videso3d.graphics;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import fr.crnan.videso3d.Pallet;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.GlobeAnnotation;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.airspaces.Box;
import gov.nasa.worldwind.render.airspaces.TrackAirspace;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.RestorableSupport;
/**
 * Représentation 3D d'une route sous la forme d'un ruban
 * @author Bruno Spyckerelle
 * @version 0.2.1
 */
public class Route3D extends TrackAirspace implements ObjectAnnotation, Route {

	public static final int LEG_FORBIDDEN = 0;
	public static final int LEG_AUTHORIZED = 1;

	private List<LatLon> locations = new ArrayList<LatLon>();
	private double width = 1.0;

	private GlobeAnnotation annotation;
	
	private Type type;
	
	private List<String> balises;
	
	/**
	 * Nom de la route
	 */
	private String name;

	public Route3D(){
		super();
		this.setDefaultMaterial();
	}

	public Route3D(Type type){
		this.setType(type);
		this.setDefaultMaterial();
	}

	/**
	 * Type de la Route : UIR ou FIR
	 * @param type {@link Type}
	 */
	public void setType(Type type){
		this.type = type;
		switch (type) {
		case FIR:
			this.setAltitudes(0, 5943); //FL0 à FL195, en mètres
			break;
		case UIR:
			this.setAltitudes(5943,20116 ); //FL195 à FL660, en mètres
			break;
		}
	}

	@Override
	public void setAnnotation(String text) {
		this.createAnnotation();
		this.annotation.setText(text);
	}
	
	private void createAnnotation(){
		this.annotation = new GlobeAnnotation("Route "+this.getName(), Position.ZERO);
		this.annotation.setAlwaysOnTop(true);
	}
	
	public GlobeAnnotation getAnnotation(Position pos){
		if(annotation == null) createAnnotation();
		annotation.setPosition(pos);
		return this.annotation;
	}
	
	public void setLocations(Iterable<? extends LatLon> locations)
	{
		this.locations.clear();
		this.removeAllLegs();
		this.addLocations(locations);
	}

	/**
	 * Crée les différents tronçons de la route suivant les coordonnees des arêtes et leur sens
	 * @param locations Coordonnées des tronçons
	 * @param sens Sens des tronçons : Route3D.LEG_AUTHORIZED ou Route3D.LEG_FORBIDDEN
	 */
	public void setLocations(Iterable<? extends LatLon> locations, List<Integer> sens){
		this.locations.clear();
		this.removeAllLegs();
		this.addLocations(locations, sens);
	}

	protected void addLocations(Iterable<? extends LatLon> newLocations, List<Integer> sens){
		if (newLocations != null)
		{
			LatLon last = null;
			int i = 0;
			for (LatLon cur : newLocations)
			{
				if (cur != null)
				{
					if (last != null) {
						this.addLeg(last, cur, sens.get(i));
						i++;
					}
					last = cur;
				}
			}
			this.setExtentOutOfDate();
			this.setLegsOutOfDate();
		}
	}

	protected void addLocations(Iterable<? extends LatLon> newLocations)
	{
		if (newLocations != null)
		{
			LatLon last = null;
			for (LatLon cur : newLocations)
			{
				if (cur != null)
				{
					if (last != null)
						this.addLeg(last, cur);
					last = cur;
				}
			}
			this.setExtentOutOfDate();
			this.setLegsOutOfDate();
		}
	}

	public Box addLeg(LatLon start, LatLon end){
		return this.addLeg(start, end, LEG_AUTHORIZED);
	}

	public Box addLeg(LatLon start, LatLon end, int sens){
		if (start == null)
		{
			String message = "nullValue.StartIsNull";
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}
		if (end == null)
		{
			String message = "nullValue.EndIsNull";
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}


		if (this.locations.size() == 0)
		{
			this.locations.add(start);
			this.locations.add(end);
		}
		else
		{
			LatLon last = this.locations.get(this.locations.size() - 1);
			if (start.equals(last))
			{
				this.locations.add(end);
			}
			else
			{
				String message = "Shapes.Route.DisjointLegDetected";
				Logging.logger().severe(message);
				throw new IllegalArgumentException(message);
			}
		}
		Box leg = new Box();
		if(sens == LEG_AUTHORIZED){

			double[] altitudes = this.getAltitudes();
			boolean[] terrainConformant = this.isTerrainConforming();
			double legWidth = this.width / 2.0;


			leg.setAltitudes(altitudes[0], altitudes[1]);
			leg.setTerrainConforming(terrainConformant[0], terrainConformant[1]);        
			leg.setLocations(start, end);
			leg.setWidths(legWidth, legWidth);
			this.addLeg(leg);
		} else {
			//on ne dessine pas le tronçon
		}
		return leg;

	}

	private void setDefaultMaterial() {
		Color color = Color.CYAN;
		Color outline = Pallet.makeBrighter(color);

		this.getAttributes().setDrawOutline(true);
		this.getAttributes().setMaterial(new Material(color));
		this.getAttributes().setOutlineMaterial(new Material(outline));
		this.getAttributes().setOpacity(0.8);
		this.getAttributes().setOutlineOpacity(0.9);
		this.getAttributes().setOutlineWidth(1.0);
	}

	@Override
	public void highlight(boolean highlight) {
		if(highlight){
			this.getAttributes().setMaterial(Material.YELLOW);
			this.getAttributes().setOutlineMaterial(Material.YELLOW);
			this.getAttributes().setOutlineWidth(2.0);
		} else {
			this.setDefaultMaterial();
		}
	}
	
	public void setName(String name){
		this.name = name;
		this.setValue("Description", name);

	}

	public String getName(){
		return this.name;
	}

	
	
	@Override
	public void addBalise(String balise) {
		if(this.balises == null){
			this.balises = new LinkedList<String>();
		}
		this.balises.add(balise);
	}

	@Override
	public List<String> getBalises() {
		return this.balises;
	}

	@Override
	public void setBalises(List<String> balises) {
		this.balises = balises;
	}

	@Override
	public Type getType() {
		return type;
	}
	
	public double getWidth()
	{
		return this.width;
	}

	public void setWidth(double width)
	{
		if (width < 0.0)
		{
			String message = Logging.getMessage("generic.ArgumentOutOfRange", "width=" + width);
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		this.width = width;

		double legWidth = this.width / 2.0;
		for (Box l : this.getLegs())
			l.setWidths(legWidth, legWidth);

		this.setExtentOutOfDate();
		this.setLegsOutOfDate();
	}

	@Override
	public Position getReferencePosition()
	{
		return this.computeReferencePosition(this.locations, this.getAltitudes());
	}
	@Override
	protected void doMoveTo(Position oldRef, Position newRef)
	{
		if (oldRef == null)
		{
			String message = "nullValue.OldRefIsNull";
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}
		if (newRef == null)
		{
			String message = "nullValue.NewRefIsNull";
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		super.doMoveTo(oldRef, newRef);

		int count = this.locations.size();
		LatLon[] newLocations = new LatLon[count];
		for (int i = 0; i < count; i++)
		{
			LatLon ll = this.locations.get(i);
			double distance = LatLon.greatCircleDistance(oldRef, ll).radians;
			double azimuth = LatLon.greatCircleAzimuth(oldRef, ll).radians;
			newLocations[i] = LatLon.greatCircleEndPosition(newRef, azimuth, distance);
		}
		this.setLocations(Arrays.asList(newLocations));
	}

	@Override
	protected void doGetRestorableState(RestorableSupport rs, RestorableSupport.StateObject context)
	{
		super.doGetRestorableState(rs, context);

		rs.addStateValueAsDouble(context, "width", this.width);
		rs.addStateValueAsLatLonList(context, "locations", this.locations);
	}

	@Override
	protected void doRestoreState(RestorableSupport rs, RestorableSupport.StateObject context)
	{
		super.doRestoreState(rs, context);

		Double d = rs.getStateValueAsDouble(context, "width");
		if (d != null)
			this.setWidth(d);

		List<LatLon> locs = rs.getStateValueAsLatLonList(context, "locations");
		if (locs != null)
			this.setLocations(locs);
	}


}
