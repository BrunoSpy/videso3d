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

import java.util.LinkedList;
import java.util.List;

import fr.crnan.videso3d.graphics.Route.Type;
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
 * @version 0.1.2
 */
public class Route2D extends SurfacePolyline implements ObjectAnnotation, Route{

	private GlobeAnnotation annotation;
	
	private Type type;
	
	private String name;
	
	private List<String> balises;
	
	public Route2D(String name, Type type){
		this.setAnnotation("Route "+name);
		this.setType(type);
		this.setName(name);
	}
	
	public Route2D() {
		super();
	}

	/**
	 * Affecte la couleur de la route suivant le codage SIA
	 * @param name Nom de la route
	 * @param type {@link Type} de la route
	 */
	private void setColor(String name) {
		BasicShapeAttributes attrs = new BasicShapeAttributes();
		switch (type) {
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
			attrs.setOutlineMaterial(Material.BLACK);
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
		} else {
			annotation.setText(text);
		}
	}

	@Override
	public void setType(Type type) {
		Type temp = this.type;
		this.type = type;
		if(this.type != temp && this.name != null) this.setColor(this.name);
	}

	public void setName(String name) {
		String temp = this.name;
		this.name = name;
		if(this.name != temp && this.type != null) this.setColor(this.name);
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public Type getType() {
		return type;
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
