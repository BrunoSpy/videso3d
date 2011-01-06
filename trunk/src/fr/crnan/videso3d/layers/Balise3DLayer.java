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

package fr.crnan.videso3d.layers;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import fr.crnan.videso3d.graphics.Balise;
import fr.crnan.videso3d.graphics.Balise3D;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.Renderable;
/**
 * 
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class Balise3DLayer extends RenderableLayer implements BaliseLayer {

	/**
	 * Liste des balises publiées
	 */
	private HashMap<String, Balise3D> balises = new HashMap<String, Balise3D>();
	
	private LinkedList<Balise3D> balisesActives = new LinkedList<Balise3D>();
	
	private Boolean lock = false;
	
	public Balise3DLayer(String name) {
		this.setName(name);
	}

	@Override
	public void addBalise(Balise balise) {
		this.balises.put(balise.getName(), (Balise3D) balise);
		
	}

	@Override
	public void addBalises(Iterable<Balise> balises) {
		// TODO Auto-generated method stub

	}

	@Override
	public void showAll() {
		for(Balise3D b : balises.values()){
			this.showBalise(b);
		}
	}

	@Override
	public void showBalise(String name) {
		if(balises.containsKey(name))
			this.showBalise(balises.get(name));
	}

	@Override
	public void showBalise(Balise b) {
		if(!balisesActives.contains(b)){
			if(b == null) System.out.println("Pb");
			balisesActives.add((Balise3D) b);
			this.addRenderable((Balise3D) b);
			this.firePropertyChange(AVKey.LAYER, null, this);
		}
	}

	@Override
	public void showBalises(List<String> balises) {
		for(String b : balises){
			this.showBalise(b);
		}
	}

	@Override
	public void hideBalises(List<String> balises) {
		for(String b : balises){
			this.hideBalise(b);
		}
	}

	@Override
	public void hideBalise(String name) {
		Balise3D b = balises.get(name);
		if(name != null){
			this.hideBalise(b);
		}
	}

	@Override
	public void hideBalise(Balise b) {
		if(!this.isLocked() && balisesActives.contains(b)){
			balisesActives.remove(b);
			this.removeRenderable((Renderable) b);
			this.firePropertyChange(AVKey.LAYER, null, this);
		}
	}

	@Override
	public void removeAllBalises() {
		if(!this.isLocked()){
			this.removeAllRenderables();
			balisesActives.clear();
			this.firePropertyChange(AVKey.LAYER, null, this);
		}
	}

	@Override
	public void eraseAllBalises() {
		this.removeAllRenderables();
		balisesActives.clear();
		balises.clear();
	}

	public Boolean isLocked(){
		return this.lock;
	}
	
	/**
	 * Permet d'empêcher toute suppression de balise de la vue.< br />
	 * Faux par défaut;
	 * @param b
	 */
	public void setLocked(Boolean b){
		this.lock = b;
	}
	
}
