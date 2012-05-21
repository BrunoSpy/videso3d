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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import fr.crnan.videso3d.graphics.Aerodrome;
import fr.crnan.videso3d.graphics.DatabasePisteAerodrome;
import fr.crnan.videso3d.graphics.MarqueurAerodrome;
import fr.crnan.videso3d.graphics.PisteAerodrome;
import gov.nasa.worldwind.Restorable;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.GeographicText;
import gov.nasa.worldwind.render.markers.Marker;

/**
 * Layer contenant les aérodromes.<br />
 * @author Adrien Vidal
 * @author Bruno Spyckerelle
 * @version 0.1.1
 */
public class AirportLayer extends LayerSet {

	
	private TextLayer textLayer = new TextLayer("Noms aéroports");
	private BaliseMarkerLayer markerLayer = new BaliseMarkerLayer();
	private RenderableLayer airportsLayer = new RenderableLayer();
	
	private HashSet<String> texts = new HashSet<String>();
	
	private HashMap<String,Aerodrome> pistes = new HashMap<String, Aerodrome>();
	/**
	 * Crée un nouveau calque d'aéroports
	 * @param name Nom du calque
	 */
	public AirportLayer(String name){
		this.setName(name);
		this.add(textLayer);
		this.add(markerLayer);
		this.add(airportsLayer);
		this.textLayer.setAMSL(false);
	}
	
	public void addAirport(Aerodrome arpt){
		if(arpt instanceof MarqueurAerodrome)
			markerLayer.addMarker((MarqueurAerodrome)arpt);
		String nomComplet = arpt.getName()+";"+arpt.getNomPiste();
		if(!pistes.containsKey(nomComplet)){
			pistes.put(nomComplet, arpt);
			if(arpt instanceof PisteAerodrome){
				airportsLayer.addRenderable((PisteAerodrome) arpt);
			}
			if(!texts.contains((String)arpt.getUserFacingText().getText())){
				textLayer.addGeographicText(arpt.getUserFacingText());
				texts.add((String)arpt.getUserFacingText().getText());
			}	
		}
		arpt.setVisible(true);
		this.firePropertyChange(AVKey.LAYER, null, this);
	}
	
	public void hideAirport(String name){
		Iterator<String> it = pistes.keySet().iterator();
		String arptCode = name.split("--")[0].trim();
		while(it.hasNext()){
			String next = it.next();
			if(next.startsWith(arptCode)){
				Aerodrome arpt = pistes.get(next);
				arpt.setVisible(false);
				if(arpt instanceof MarqueurAerodrome){
					markerLayer.removeMarker((MarqueurAerodrome)arpt);
					((MarqueurAerodrome)arpt).setLocationVisible(false);
				}else
					((DatabasePisteAerodrome)arpt).setLocationsVisible(false);
			}
		}
		this.firePropertyChange(AVKey.LAYER, null, this);
	}
	
	public void removeAllAirports(){
		markerLayer.removeAllMarkers();
		for(Aerodrome a : pistes.values()){
			a.setVisible(false);
		}
	}
	
	public boolean containsAirport(String name){
		String arptCode = name.split("--")[0].trim();
		Iterator<String> it = pistes.keySet().iterator();
		while(it.hasNext()){
			if(it.next().startsWith(arptCode))
				return true;
		}
		return false;
	}
	
	
	public ArrayList<Aerodrome> getAirport(String name){
		String arptCode = name.split("--")[0].trim();
		ArrayList<Aerodrome> aerodromes = new ArrayList<Aerodrome>();
		Iterator<String> it = pistes.keySet().iterator();
		while(it.hasNext()){
			String next = it.next();
			if(next.startsWith(arptCode))
				aerodromes.add(pistes.get(next));
		}
		return aerodromes.isEmpty()? null : aerodromes;
	}
	
	public List<Aerodrome> getVisiblePistes(){
		ArrayList<Aerodrome> pistes = new ArrayList<Aerodrome>();
		for(Aerodrome a : this.pistes.values()){
			if(a.isVisible()){
				pistes.add(a);
			}
		}
		return pistes;
	}
		
	public List<Restorable> getVisibleRestorables(){
		ArrayList<Restorable> restorables = new ArrayList<Restorable>();
		for(GeographicText t : textLayer.getActiveGeographicTexts()){
			if(t instanceof Restorable){
				restorables.add((Restorable) t);
			}
		}
		for(Marker m : this.markerLayer.getMarkers()){
			if(m instanceof Restorable){
				restorables.add((Restorable) m);
			}
		}
		for(Aerodrome a : pistes.values()){
			if(a.isVisible()){
				restorables.add(a);
			}
		}
			
		return restorables;
	}
	
	public TextLayer getTextLayer(){
		return textLayer;
	}
}
