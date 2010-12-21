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
import java.util.HashSet;

import fr.crnan.videso3d.graphics.MarqueurAerodrome;
import fr.crnan.videso3d.graphics.PisteAerodrome;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.UserFacingText;

/**
 * Layer contenant les aérodromes.<br />
 * @author Adrien Vidal
 */
public class AirportLayer extends LayerSet {

	
	private TextLayer textLayer = new TextLayer("Noms aéroports");
	private BaliseMarkerLayer markerLayer = new BaliseMarkerLayer();
	private RenderableLayer airportsLayer = new RenderableLayer();
	
	private HashSet<String> texts = new HashSet<String>();
	
	private HashMap<String,Object> activeAirports = new HashMap<String, Object>();
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
	
	public void addAirport(Object arpt, String nomPiste){
		UserFacingText uft=null;
		if(arpt instanceof MarqueurAerodrome){
			MarqueurAerodrome airportBalise = (MarqueurAerodrome) arpt;
			uft = airportBalise.getUserFacingText();		
			if(!activeAirports.containsKey(airportBalise.getName()+nomPiste)){
				activeAirports.put(airportBalise.getName()+nomPiste, airportBalise);		
				markerLayer.addMarker(airportBalise);
			}
		}else if(arpt instanceof PisteAerodrome){
			PisteAerodrome piste = (PisteAerodrome) arpt;
			uft = piste.getUserFacingText();
			if(!activeAirports.containsKey(piste.getName()+nomPiste)){
				activeAirports.put(piste.getName()+nomPiste, piste);
				airportsLayer.addRenderable(piste.getOuterRectangle());
				airportsLayer.addRenderable(piste.getInnerRectangle());
			}	
		}
		if(!texts.contains((String)uft.getText())){
			textLayer.addGeographicText(uft);
			texts.add((String) uft.getText());
		}
		this.firePropertyChange(AVKey.LAYER, null, this);
	}
	
	public void hideAirport(String name, String nomPiste){
		if(activeAirports.containsKey(name+nomPiste)){
			UserFacingText uft = null;
			Object arpt = activeAirports.get(name+nomPiste);
			if(arpt instanceof MarqueurAerodrome){
				uft = ((MarqueurAerodrome)arpt).getUserFacingText();
				markerLayer.removeMarker((MarqueurAerodrome)arpt);
			}else{
				uft = ((PisteAerodrome)arpt).getUserFacingText();
				airportsLayer.removeRenderable(((PisteAerodrome)arpt).getOuterRectangle());
				airportsLayer.removeRenderable(((PisteAerodrome)arpt).getInnerRectangle());
			}
			if(texts.contains((String)uft.getText())){
				textLayer.removeGeographicText(uft);
				texts.remove((String)uft.getText());
			}
			activeAirports.remove(name+nomPiste);
		}
		this.firePropertyChange(AVKey.LAYER, null, this);
	}
	
	public void removeAllAirports(){
		textLayer.removeAllGeographicTexts();
		markerLayer.setMarkers(null);
		airportsLayer.removeAllRenderables();
		activeAirports.clear();
	}
	
}
