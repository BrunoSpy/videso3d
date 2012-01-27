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
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.airspaces.AirspaceAttributes;
import gov.nasa.worldwind.render.airspaces.BasicAirspaceAttributes;
import gov.nasa.worldwind.render.airspaces.Box;
import gov.nasa.worldwind.render.airspaces.TrackAirspace;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.RestorableSupport;
/**
 * Représentation 3D d'une route sous la forme d'un ruban
 * @author Bruno Spyckerelle
 * @version 0.2.6
 */
public class Route3D extends TrackAirspace implements Route {

	private List<LatLon> locations = new ArrayList<LatLon>();
	private double width = 1.0;

	private VidesoAnnotation annotation;
	
	private Space space;
		
	private List<String> balises;
	
	private boolean highlighted = false;
	private AirspaceAttributes highlightAttrs;
	private AirspaceAttributes normalAttrs;
	
	/**
	 * Nom de la route
	 */
	private String name;
	private Parity parity;
	

	public Route3D(){
		super();
		this.setDefaultMaterial();
	}

	public Route3D(Space s){
		super();
		this.setSpace(s);
		this.setDefaultMaterial();
	}
	
	public Route3D(String name, Space s){
		this(s);
		this.setName(name);
	}
	
	
	
	/**
	 * Type de la Route : UIR ou FIR
	 * @param type {@link Space}
	 */
	@Override
	public void setSpace(Space type){
		this.space = type;
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
	public void setParity(Parity p){
		Parity temp = this.parity;
		this.parity = p;
		if(this.parity != temp && this.name != null) this.setColor(this.name);
		
	}

	@Override
	public Parity getParity(){
		return this.parity;
	}
	
	/**
	 * Affecte la couleur de la route suivant le codage SIA
	 * @param name Nom de la route
	 * @param type {@link Espace} de la route
	 */
	private void setColor(String name) {
		AirspaceAttributes attrs = this.getNormalAttributes();
		switch (space) {
		case FIR:
			Character c = name.charAt(0);
			switch (c) {
			case 'A':
				attrs.setOutlineMaterial(Material.YELLOW);
				attrs.setMaterial(new Material(Pallet.makeBrighter(Color.YELLOW)));
				break;
			case 'G' :
				attrs.setOutlineMaterial(Material.GREEN);
				attrs.setMaterial(new Material(Pallet.makeBrighter(Color.GREEN)));
				break;
			case 'B' :
				attrs.setOutlineMaterial(Material.BLUE);
				attrs.setMaterial(new Material(Pallet.makeBrighter(Color.BLUE)));
				break;
			case 'R' :
				attrs.setOutlineMaterial(Material.RED);
				attrs.setMaterial(new Material(Pallet.makeBrighter(Color.RED)));
				break;
			default:
				attrs.setOutlineMaterial(Material.BLACK);
				attrs.setMaterial(new Material(Pallet.makeBrighter(Color.BLACK)));
				break;
			}
			break;
		case UIR:
			if(parity!=null) {
				switch (parity) {
				case RED :
					attrs.setOutlineMaterial(Material.RED);
					attrs.setMaterial(new Material(Pallet.makeBrighter(Color.RED)));
					break;
				case GREEN :
					attrs.setOutlineMaterial(Material.GREEN);
					attrs.setMaterial(new Material(Pallet.makeBrighter(Color.GREEN)));
					break;
				case BLUE :
					attrs.setOutlineMaterial(Material.BLUE);
					attrs.setMaterial(new Material(Pallet.makeBrighter(Color.BLUE)));
					break;
				}
			} else {
				attrs.setOutlineMaterial(Material.BLACK);
				attrs.setMaterial(new Material(Pallet.makeBrighter(Color.BLACK)));
			}
			break;
		default:
			break;
		}
	}
	
	@Override
	public void setAnnotation(String text) {
		if(annotation == null)
			this.annotation = new VidesoAnnotation("");
		this.annotation.setText(text);
	}
	
	public VidesoAnnotation getAnnotation(Position pos){
		if(annotation == null)
			this.annotation = new VidesoAnnotation("Route "+this.getName());
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
	
	/**
	 * 
	 * @return Les coordonnées des tronçons de la route
	 */
	@Override
	public Iterable<? extends LatLon> getLocations(){
		return this.locations;
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
		if(sens != LEG_FORBIDDEN){

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

	protected void setDefaultMaterial() {
		Color color = Color.CYAN;
		Color outline = Pallet.makeBrighter(color);
		
		this.normalAttrs = new BasicAirspaceAttributes();
		this.normalAttrs.setDrawOutline(true);
		this.normalAttrs.setMaterial(new Material(color));
		this.normalAttrs.setOutlineMaterial(new Material(outline));
		this.normalAttrs.setOpacity(0.8);
		this.normalAttrs.setOutlineOpacity(0.9);
		this.normalAttrs.setOutlineWidth(1.0);
		
		this.setNormalAttributes(this.normalAttrs);
		
		AirspaceAttributes attrs = new BasicAirspaceAttributes(this.normalAttrs);
		attrs.setMaterial(new Material(outline));
		this.setHighlightAttributes(attrs);
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
	public Space getSpace() {
		return this.space;
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
		
		rs.addStateValueAsString(context, "name", this.getName());
		
		if(this.annotation != null)
			rs.addStateValueAsString(context, "annotation", this.annotation.getText(), true);
		
		this.getNormalAttributes().getRestorableState(rs, rs.addStateObject(context, "hightlightAttributes"));
	}

	@Override
	protected void doRestoreState(RestorableSupport rs, RestorableSupport.StateObject context)
	{
		super.doRestoreState(rs, context);

		String s = rs.getStateValueAsString(context, "annotation");
		if(s != null)
			this.setAnnotation(s);
		
		s = rs.getStateValueAsString(context, "name");
		if(s != null)
			this.setName(s);
		
		Double d = rs.getStateValueAsDouble(context, "width");
		if (d != null)
			this.setWidth(d);

		List<LatLon> locs = rs.getStateValueAsLatLonList(context, "locations");
		if (locs != null)
			this.setLocations(locs);
		
		RestorableSupport.StateObject so = rs.getStateObject(context, "highlightAttributes");
        if (so != null)
            this.getHighlightAttributes().restoreState(rs, so);
	}

	@Override
	public boolean isHighlighted() {
		return this.highlighted;
	}

	@Override
	public void setHighlighted(boolean highlighted) {
		if(this.highlighted != highlighted){
			this.setAttributes(highlighted ? this.getHighlightAttributes() : this.getNormalAttributes());
			this.highlighted = highlighted;
		}
	}
	
    public AirspaceAttributes getNormalAttributes() {
        return this.normalAttrs == null ? this.getAttributes() : this.normalAttrs;
    }

    public void setNormalAttributes(AirspaceAttributes normalAttrs) {
        this.normalAttrs = normalAttrs;
        if(!highlighted) this.setAttributes(this.normalAttrs);
    }
    
    public AirspaceAttributes getHighlightAttributes() {
        return highlightAttrs == null ? this.normalAttrs : this.highlightAttrs;
    }

    /**
     * Specifies highlight attributes.
     *
     * @param highlightAttrs highlight attributes. May be null, in which case default attributes are used.
     */
    public void setHighlightAttributes(AirspaceAttributes highlightAttrs) {
        this.highlightAttrs = highlightAttrs;
        if(highlighted) this.setAttributes(this.highlightAttrs);
    }
	
    
    
}
