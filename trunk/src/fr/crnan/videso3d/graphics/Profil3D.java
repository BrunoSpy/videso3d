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
import java.util.List;

import fr.crnan.videso3d.DatabaseManager.Type;
import fr.crnan.videso3d.Pallet;
import fr.crnan.videso3d.stip.StipController;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Polyline;
import gov.nasa.worldwind.render.SurfacePolyline;
/**
 * Profil d'un vol avec affichage des couples balise/niveau.<br />
 * Le profil est à la fois dessiné en 3D et en projeté sur le sol.<br />
 * Requiert une base de données STIP sélectionnée.
 * @author Bruno Spyckerelle
 * @version 0.2
 */
public class Profil3D {

	/**
	 * Balises
	 */
	private List<Balise2D> balises = new ArrayList<Balise2D>();

	/**
	 * Profil
	 */
	private Polyline profil = new Polyline();
	/**
	 * Profil 3D
	 */
	private VPolyline curtain = new VPolyline();
	/**
	 * Projection du profil
	 */
	private SurfacePolyline projected = new SurfacePolyline();
	/**
	 * Profil 3D plein.
	 */
	private Boolean plain = true;
	/**
	 * Balises
	 */
	private Boolean withMarkers = true;
	
	public Profil3D(){
		BasicShapeAttributes attrs = new BasicShapeAttributes();
		attrs.setOutlineStipplePattern((short) 0xAAAA);
		attrs.setOutlineStippleFactor(5);
		attrs.setOutlineWidth(3.0);
		projected.setAttributes(attrs);
		curtain.setPlain(true);
		curtain.setColor(Pallet.makeBrighter(new Color(0.0f, 0.0f, 1.0f, 0.4f)));
		profil.setColor(Color.BLUE);
		profil.setLineWidth(2.0);
	}
	/**
	 * Profil sans balises
	 * @param positions
	 */
	public Profil3D(Iterable<? extends Position> positions){
		this();
		profil.setPositions(positions);
		curtain.setPositions(positions);
		this.withMarkers = false;
	}
	
	public Profil3D(List<String> balises, Iterable<? extends Position> positions){
		this();
		this.setBalises(balises, positions);
	}
	
	public Profil3D(List<String> balises, List<String> annotations, Iterable<? extends Position> positions){
		this();
		this.setBalises(balises, annotations, positions);
	}
	
	public void addBalise(String balise, LatLon latlon, Integer fl){
		this.addBalise(balise, new Position(latlon, fl*30.48));
	}
	
	public void addBalise(String balise, Position position){
		this.balises.add(new Balise2D(balise, position, Type.STIP, StipController.BALISES));
	}
	
	public void addBalise(String balise, String annotation, Position position){
		this.balises.add(new Balise2D(balise, position, Type.STIP, StipController.BALISES));
	}
	
	public void setBalises(List<String> balises, List<String> annotations, Iterable<? extends Position> positions){
		int i = 0;
		for(Position position : positions){
			this.addBalise(balises.get(i), annotations.get(i), position);
			i++;
		}
		profil.setPositions(positions);
		curtain.setPositions(positions);
		projected.setLocations(positions);
	}
	
	public void setBalises(List<String> balises, Iterable<? extends Position> positions){
		int i = 0;
		for(Position position : positions){
			this.addBalise(balises.get(i), position);
			i++;
		}
		profil.setPositions(positions);
		curtain.setPositions(positions);
		projected.setLocations(positions);
	}
	
	public boolean withMarkers(){
		return this.withMarkers;
	}
	
	public void setPlain(Boolean plain){
		this.plain = plain;
	}
	
	public Boolean isPlain(){
		return this.plain;
	}
	
	public Polyline getCurtain(){
		return this.curtain;
	}
	
	public Polyline getProfil(){
		return this.profil;
	}
	
	public SurfacePolyline getProjection(){
		return this.projected;
	}
	
	public List<Balise2D> getBalises(){
		return this.balises;
	}
	
	public void highlight(Boolean b) {
		if(b){
			curtain.setColor(Pallet.makeBrighter(new Color(1.0f, 1.0f, 0.0f, 0.4f)));
		} else {
			curtain.setColor(Pallet.makeBrighter(new Color(0.0f, 0.0f, 1.0f, 0.4f)));
		}
	}

}
