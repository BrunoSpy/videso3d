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
import fr.crnan.videso3d.graphics.Balise2D;
import gov.nasa.worldwind.avlist.AVKey;

/**
 * Layer contenant les balises.<br />
 * Permet d'afficher une ou plusieurs balises selon leur nom.
 * @author Bruno Spyckerelle
 * @version 0.3.1
 */
public class Balise2DLayer extends LayerSet implements BaliseLayer{

	/**
	 * Liste des balises publiées
	 */
	private HashMap<String, Balise2D> balises = new HashMap<String, Balise2D>();
	
	private LinkedList<Balise2D> balisesActives = new LinkedList<Balise2D>();
	
	private TextLayer textLayer = new TextLayer("Balises");
	private BaliseMarkerLayer markerLayer = new BaliseMarkerLayer();
	
	private Boolean lock = false;
	/**
	 * Crée un nouveau calque de balises
	 * @param name Nom du calque
	 * @param amsl Si vrai, AMSL, sinon AGL.
	 */
	public Balise2DLayer(String name, Boolean amsl){
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
	public Balise2DLayer(String name){
		this(name, false);
	}
	
	/**
	 * Ajoute une balise.<br />
	 * Utiliser <code>showAll</code> ou <b>showBalise</b> pour rendre visible la balise.
	 * @param balise Balise à ajouter
	 */
	@Override
	public void addBalise(Balise balise){
		this.balises.put(balise.getName(), (Balise2D) balise);
	}
	
	/**
	 * Ajoute plusieurs balises.<br />
	 * Utiliser <code>showAll</code> ou <b>showBalise</b> pour rendre visible la balise.
	 * @param balise Balise à ajouter
	 */
	@Override
	public void addBalises(Iterable<? extends Balise> balises) {
		for(Balise b : balises){
			this.balises.put(b.getName(), (Balise2D) b);
		}
	}
	
	/**
	 * Affiche toutes les balises
	 */
	@Override
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
	@Override
	public void showBalise(String name){
		Balise2D b = balises.get(name);
		if(b != null){
			this.showBalise(b);
		}
	}
	
	/**
	 * Affiche une balise.<br />
	 * Cett balise doit d'abord être ajoutée grâce à <code>addBalise(Balise2D balise)</code>
	 * @param b Balise à affichers
	 */
	@Override
	public void showBalise(Balise b){
		if(!balisesActives.contains(b)){
			balisesActives.add((Balise2D) b);
			textLayer.addGeographicText(((Balise2D) b).getUserFacingText());
			markerLayer.addMarker(((Balise2D) b).getMarker());
			this.firePropertyChange(AVKey.LAYER, null, this);
		}
	}
	
	/**
	 * Affiche une liste de balises
	 * @param balises
	 */
	@Override
	public void showBalises(List<String> balises) {
		for(String b : balises){
			this.showBalise(b);
		}
	}
	
	/**
	 * Enlève une liste de balises de la vue
	 * @param balises
	 */
	@Override
	public void hideBalises(List<String> balises) {
		for(String b : balises){
			this.hideBalise(b);
		}
	}
	
	/**
	 * Enlève une balise de la vue.<br />
	 * Cette balise est toujours accessible pour être à nouveau affichée plus tard.
	 * @param name
	 */
	@Override
	public void hideBalise(String name){
		Balise2D b = balises.get(name);
		if(name != null){
			this.hideBalise(b);
		}
	}
	
	/**
	 * Enlève une balise de la vue.<br />
	 * Cette balise est toujours accessible pour être à nouveau affichée plus tard.
	 * @param name
	 */
	@Override
	public void hideBalise(Balise b) {
		if(!this.isLocked() && balisesActives.contains(b)){
			balisesActives.remove(b);
			textLayer.removeGeographicText(((Balise2D) b).getUserFacingText());
			markerLayer.removeMarker(((Balise2D) b).getMarker());
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
			this.firePropertyChange(AVKey.LAYER, null, this);
		}
	}
	
	/**
	 * Supprime toutes les balises
	 */
	public void eraseAllBalises(){
		textLayer.removeAllGeographicTexts();
		markerLayer.setMarkers(null);
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
	
	
	public Balise2D getBalise(String balise){
		return balises.get(balise);
	}

}
