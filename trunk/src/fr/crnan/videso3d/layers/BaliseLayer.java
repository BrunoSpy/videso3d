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

import fr.crnan.videso3d.graphics.Balise2D;
import gov.nasa.worldwind.avlist.AVKey;

/**
 * Layer contenant les balises.<br />
 * Permet d'afficher une ou plusieurs balises selon leur nom.
 * @author Bruno Spyckerelle
 * @version 0.2
 */
public class BaliseLayer extends LayerSet {

	/**
	 * Liste des balises publiées
	 */
	private HashMap<String, Balise2D> balises = new HashMap<String, Balise2D>();
	
	private LinkedList<Balise2D> balisesActives = new LinkedList<Balise2D>();
	
	private TextLayer textLayer = new TextLayer("Balises");
	private BaliseMarkerLayer markerLayer = new BaliseMarkerLayer();
	
	private Boolean lock = false;
	
	public BaliseLayer(String name, Boolean amsl){
		this.setName(name);
		this.add(textLayer);
		this.add(markerLayer);
		this.textLayer.setAMSL(amsl);
	}
	/**
	 * Nouveau calque de balises.< br/>
	 * Par défaut, les balises sont AGL.
	 * @param name Nom du calque
	 */
	public BaliseLayer(String name){
		this(name, false);
	}
	
	/**
	 * Ajoute une balise.<br />
	 * Utiliser <code>showAll</code> ou <b>showBalise</b> pour rendre visible la balise.
	 * @param balise Balise à ajouter
	 */
	public void addBalise(Balise2D balise){
		this.balises.put(balise.getName(), balise);
	}
	
	/**
	 * Ajoute plusieurs balises.<br />
	 * Utiliser <code>showAll</code> ou <b>showBalise</b> pour rendre visible la balise.
	 * @param balise Balise à ajouter
	 */
	public void addBalises(Iterable<Balise2D> balises) {
		for(Balise2D b : balises){
			this.balises.put(b.getName(), b);
		}
	}
	
	/**
	 * Affiche toutes les balises
	 */
	public void showAll(){
		for(Balise2D b : balises.values()){
			this.showBalise(b);
		}
	}
	/**
	 * Affiche une balise.<br />
	 * Cett balise doit d'abord être ajoutée grâce à <code>addBalise(Balise2D balise)</code>
	 * @param name Nome de la balise
	 */
	public void showBalise(String name){
		Balise2D b = balises.get(name);
		if(b != null){
			this.showBalise(b);
		}
	}
	
	private void showBalise(Balise2D b){
		if(!balisesActives.contains(b)){
			balisesActives.add(b);
			textLayer.addGeographicText(b.getUserFacingText());
			markerLayer.addMarker(b.getMarker());
			this.firePropertyChange(AVKey.LAYER, null, this);
		}
	}
	
	/**
	 * Enlève une balise de la vue.<br />
	 * Cettebalise est toujours accessible pour être à nouveau affichée plus tard.
	 * @param name
	 */
	public void removeBalise(String name){
		Balise2D b = balises.get(name);
		if(name != null){
			this.removeBalise(b);
		}
	}
	
	private void removeBalise(Balise2D b) {
		if(balisesActives.contains(b)){
			balisesActives.remove(b);
			textLayer.removeGeographicText(b.getUserFacingText());
			markerLayer.removeMarker(b.getMarker());
			this.firePropertyChange(AVKey.LAYER, null, this);
		}
	}

	/**
	 * Cache toutes les balises, ne les supprime pas du calque.
	 */
	public void removeAllBalises(){
		if(!this.isLocked()){
			textLayer.removeAllGeographicTexts();
			markerLayer.setMarkers(null);
			balisesActives.clear();
		}
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