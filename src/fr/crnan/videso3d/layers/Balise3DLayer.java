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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import fr.crnan.videso3d.graphics.Balise;
import fr.crnan.videso3d.graphics.Balise3D;
import fr.crnan.videso3d.graphics.DatabaseVidesoObject;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.Renderable;
/**
 * 
 * @author Bruno Spyckerelle
 * @version 0.2.4
 */
public class Balise3DLayer extends RenderableLayer implements BaliseLayer {

	/**
	 * Liste des balises publiées
	 */
	private HashMap<String, Balise3D> balises = new HashMap<String, Balise3D>();
	
	private List<Balise3D> balisesActives = new ArrayList<Balise3D>();
	
	private Boolean lock = false;
		
	public Balise3DLayer(String name) {
		this.setName(name);
	}

	@Override
	public void addBalise(Balise balise) {
		if(balise instanceof DatabaseVidesoObject){
			this.balises.put(balise.getName()+((DatabaseVidesoObject) balise).getType(), (Balise3D) balise);
		} else {
			this.balises.put(balise.getName(), (Balise3D) balise);
		}
		
	}

	@Override
	public void addBalises(Iterable<? extends Balise> balises) {
		for(Balise b : balises){
			this.addBalise(b);
		}
	}
	
	
	@Override
	public void showAll() {
		for(Balise3D b : balises.values()){
			this.showBalise(b);
		}
	}

	@Override
	public void showBalise(String name, int type) {
		if(balises.containsKey(name+type))
			this.showBalise(balises.get(name+type));
	}
	
	@Override
	public void showBalise(Balise b) {
		if(!balisesActives.contains(b)){
			balisesActives.add((Balise3D) b);
			this.addRenderable((Balise3D) b);
			this.firePropertyChange(AVKey.LAYER, null, this);
		}
	}

	@Override
	public void showBalises(List<String> balises, int type) {
		for(String b : balises){
			this.showBalise(b, type);
		}
	}

	@Override
	public void hideBalises(List<String> balises, int type) {
		for(String b : balises){
			this.hideBalise(b, type);
		}
	}

	@Override
	public void hideBalise(String name, int type) {
		Balise3D b = balises.get(name+type);
		if(b != null){
			this.hideBalise(b);
			b.setLocationVisible(false);
		}
	}
	
	@Override
	public void hideBalise(Balise b) {
		if(!this.isLocked() && balisesActives.contains(b)){
			balisesActives.remove(b);
			this.removeRenderable((Renderable) b);
			b.setLocationVisible(false);
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

	/**
	 * Test si une balise a déjà été ajoutée
	 * @param balise nom de la balise
	 * @return Vrai si la balise a déjà été ajoutée
	 */
	public Boolean contains(String balise){
		return balises.containsKey(balise);
	}
	
	/**
	 * Test si une balise a déjà été ajoutée
	 * @param balise nom de la balise
	 * @param type
	 * @return Vrai si la balise a déjà été ajoutée
	 */
	public Boolean contains(String balise, int type){
		return balises.containsKey(balise+type);
	}
	
	public Balise3D getBalise(String balise){
		return balises.get(balise);
	}
	
	public Balise3D getBalise(String balise, int type){
		Balise3D b = balises.get(balise+type);
		return (b!=null?b:balises.get(balise));
	}

	@Override
	public List<String> getVisibleBalisesNames() {
		List<String> balisesList = new ArrayList<String>();
		for(Balise b : balisesActives){
			balisesList.add(b.getName());
		}
		return balisesList;
	}
	
	@Override
	public List<Balise3D> getVisibleBalises() {
		return this.balisesActives;
	}
	
	@Override
	public void removeBalise(Balise balise) {
		this.hideBalise(balise);
		if(balise instanceof DatabaseVidesoObject){
			this.balises.remove(balise.getName()+((DatabaseVidesoObject) balise).getType());
		} else {
			this.balises.remove(balise.getName());
		}
	}

}
