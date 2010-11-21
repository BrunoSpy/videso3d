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
import java.util.LinkedList;
import java.util.List;

import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.Pallet;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.Annotation;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.GlobeAnnotation;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.SurfacePolyline;
/**
 * Route en 2D.<br />
 * Couleurs respectant le codage SIA
 * @author Bruno Spyckerelle
 * @version 0.2
 */
public class Route2D extends SurfacePolyline implements Route{

	private GlobeAnnotation annotation;
	
	private Space space;
	
	private Sens sens;
	
	private int type;
	
	private String name;
	
	private List<String> balises;
	
	private DatabaseManager.Type base;
	
	public Route2D(String name, Space s, DatabaseManager.Type base, int type){
		this.setAnnotation("Route "+name);
		this.setSpace(s);
		this.setName(name);
		this.setDatabaseType(base);
		this.setType(type);
	}
	
	public Route2D(DatabaseManager.Type base, int type) {
		super();
		this.setDatabaseType(base);
		this.setType(type);
	}

	@Override
	public DatabaseManager.Type getDatabaseType() {
		return this.base;
	}

	@Override
	public void setDatabaseType(DatabaseManager.Type type) {
		this.base = type;
	}
	
	/**
	 * Affecte la couleur de la route suivant le codage SIA
	 * @param name Nom de la route
	 * @param type {@link Espace} de la route
	 */
	private void setColor(String name) {
		BasicShapeAttributes attrs = new BasicShapeAttributes();
		switch (space) {
		case FIR:
			Character c = name.charAt(0);
			switch (c) {
			case 'A':
				attrs.setOutlineMaterial(Material.YELLOW);
				break;
			case 'G' :
				attrs.setOutlineMaterial(Material.GREEN);
				break;
			case 'B' :
				attrs.setOutlineMaterial(Material.BLUE);
				break;
			case 'R' :
				attrs.setOutlineMaterial(Material.RED);
				break;
			default:
				attrs.setOutlineMaterial(Material.BLACK);
				break;
			}
			break;
		case UIR:
			if(sens!=null){
				switch (sens){
				case RED :
					attrs.setOutlineMaterial(Material.RED);
					break;
				case GREEN :
					attrs.setOutlineMaterial(Material.GREEN);
					break;
				case BLUE :
					attrs.setOutlineMaterial(Material.BLUE);
					break;
				}
			}else{
				attrs.setOutlineMaterial(Material.BLACK);
			}
			break;
		default:
			break;
		}
		attrs.setEnableAntialiasing(true);
		attrs.setDrawInterior(false);
		attrs.setOutlineWidth(1.0);
		this.setAttributes(attrs);
	}


	@Override
	public Annotation getAnnotation(Position pos) {
		if(annotation == null) this.setAnnotation("Route "+this.name);
		annotation.setPosition(pos);
		return annotation;
	}

	@Override
	public void setAnnotation(String text) {
		if(annotation == null) {
			annotation = new GlobeAnnotation(text, Position.ZERO);
			annotation.setAlwaysOnTop(true);
			annotation.getAttributes().setBackgroundColor(Pallet.ANNOTATION_BACKGROUND);
			annotation.getAttributes().setBorderColor(Color.BLACK);
			annotation.getAttributes().setAdjustWidthToText(Annotation.SIZE_FIT_TEXT);
		} else {
			annotation.setText(text);
		}
	}

	@Override
	public void setSpace(Space s) {
		Space temp = this.space;
		this.space = s;
		if(this.space != temp && this.name != null) this.setColor(this.name);
	}

	@Override
	public Space getSpace(){
		return this.space;
	}
	
	public void setName(String name) {
		String temp = this.name;
		this.name = name;
		if(this.name != temp && this.space != null) this.setColor(this.name);
	}
	
	public void setSens(Sens sens){
		Sens temp = this.sens;
		this.sens = sens;
		if(this.sens != temp && this.name != null) this.setColor(this.name);
		
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void setType(int type) {
		this.type = type;
	}

	@Override
	public int getType() {
		return this.type;
	}
	
	@Override
	public void highlight(boolean highlight) {
		if(highlight){
			BasicShapeAttributes attrs = (BasicShapeAttributes) this.getAttributes();
			attrs.setOutlineMaterial(Material.WHITE);
			attrs.setOutlineWidth(2.0);
			this.setAttributes(attrs);
			
		} else {
			this.setColor(this.getName());
		}
	}
	
	public void setBalises(List<String> balises){
		this.balises = balises;
	}
	
	public void addBalise(String balise){
		if(this.balises == null){
			this.balises = new LinkedList<String>();
		}
		this.balises.add(balise);
	}

	@Override
	public List<String> getBalises(){
		return this.balises;
	}
	
}
