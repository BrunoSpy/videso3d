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

package fr.crnan.videso3d.aip;

import java.awt.Color;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;

import org.jdom.Element;


import fr.crnan.videso3d.Couple;
import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.Pallet;
import fr.crnan.videso3d.VidesoController;
import fr.crnan.videso3d.VidesoGLCanvas;
import fr.crnan.videso3d.aip.AIP.Altitude;
import fr.crnan.videso3d.graphics.Secteur3D;
import fr.crnan.videso3d.graphics.Secteur3D.Type;
import gov.nasa.worldwind.layers.AirspaceLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.airspaces.BasicAirspaceAttributes;

public class AIPController implements VidesoController {

	private VidesoGLCanvas wwd;
	private AIP aip = new AIP();
	
	private AirspaceLayer zonesLayer = new AirspaceLayer();
	{zonesLayer.setName("Zones");
	zonesLayer.setEnableAntialiasing(true);}	
	
	private HashMap<String, Secteur3D> zones;	
	
	
	
	public AIPController(VidesoGLCanvas wwd) {
		this.wwd = wwd;
		this.buildAIP();
	}

	
	private void buildAIP() {
		//this.wwd.firePropertyChange("step", "", "Données AIP");

		if(zonesLayer != null) {
			zonesLayer.removeAllAirspaces();
			this.toggleLayer(zonesLayer, true);
		} else {
			zonesLayer = new AirspaceLayer();
			zonesLayer.setName("Zones");
			zonesLayer.setEnableAntialiasing(true);
			this.toggleLayer(zonesLayer, true);
		}
		try {
			if(DatabaseManager.getCurrentAIP() != null) {
				//création des nouveaux objets
				this.toggleLayer(zonesLayer, true);
				zones = new HashMap<String, Secteur3D>();				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		this.wwd.redraw();
	}

	
	public AirspaceLayer getZonesLayer(){
		return zonesLayer;
	}
	
	
	
	
	
	@Override
	public void highlight(String name) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unHighlight(String name) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addLayer(String name, Layer layer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeLayer(String name, Layer layer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeAllLayers() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void toggleLayer(Layer layer, Boolean state) {
		this.wwd.toggleLayer(zonesLayer, state);
		
	}

	@Override
	public void showObject(int type, String name) {
		if(!zones.containsKey(name))
			this.addZone(type,name);
	}

	@Override
	public void hideObject(int type, String name) {
		this.removeZone(type,name);
	}
	
	


	/**
	 * Affiche tous les objets correspondant aux checkBox de la liste.
	 * @param type Le type des objets à afficher
	 */
	public void displayAll(int type){
		Iterator<Couple<Integer,String>> it = aip.getZones(type).iterator();
		while(it.hasNext()){
			showObject(type, it.next().getSecond());
		}
	}
	/**
	 * Enlève tous les objets correspondant aux checkBox de la liste.
	 * @param type
	 */
	public void hideAll(int type){
		Iterator<Couple<Integer,String>> it = aip.getZones(type).iterator();
		while(it.hasNext()){
			hideObject(type, it.next().getSecond());
		}
	}
	
	private void addZone(int type, String name) {
		Type secteur3DType=null;
		Color couleurZone=null;
		switch(type){
		case AIP.TSA:
			secteur3DType=Type.TSA;
			couleurZone=Color.orange;
			break;
		case AIP.SIV:
			secteur3DType=Type.SIV;
			couleurZone=Pallet.SIVColor;
			break;
		case AIP.CTR:
			secteur3DType=Type.CTR;
			couleurZone=Pallet.CTRColor;
			break;
		case AIP.TMA:
			secteur3DType=Type.TMA;
			couleurZone=Pallet.TMAColor;
			break;
		case AIP.R:
			secteur3DType=Type.R;
			couleurZone=Color.red;
			break;
		case AIP.D:
			secteur3DType=Type.D;
			couleurZone=Color.red;
			break;
		default: 
			break;
		}
			Element maZone = aip.findElementByName(type, name);
			Couple<Altitude,Altitude> niveaux = aip.getLevels(maZone);
			Secteur3D zone = new Secteur3D(name, niveaux.getFirst().getFL(), niveaux.getSecond().getFL(),secteur3DType);
			
			BasicAirspaceAttributes attrs = new BasicAirspaceAttributes();
			attrs.setDrawOutline(true);
			attrs.setMaterial(new Material(couleurZone));
			attrs.setOutlineMaterial(new Material(Pallet.makeBrighter(couleurZone)));
			attrs.setOpacity(0.2);
			attrs.setOutlineOpacity(0.9);
			attrs.setOutlineWidth(1.5);
			zone.setAttributes(attrs);
			
			zone.setAnnotation("<p><b>"+name+"</b></p>"
											+"<p>Plafond : "+niveaux.getSecond().getFullText()
											+"<br />Plancher : "+niveaux.getFirst().getFullText()+"</p>");
			ContourZone contour = new ContourZone(aip.getPartie(maZone.getChild("Partie").getAttributeValue("pk")));
			zone.setLocations(contour.getLocations());
			zones.put(name, zone);
			this.addToZonesLayer(zone);
	}

	
	


	private void removeZone(int type, String name) {
		this.removeFromZonesLayer(zones.get(name));
		zones.remove(name);
	}
	
	
	private void addToZonesLayer(Secteur3D zone){
		this.zonesLayer.addAirspace(zone);
		this.wwd.redraw();
	}
	private void removeFromZonesLayer(Secteur3D zone){
		this.zonesLayer.removeAirspace(zone);
		this.wwd.redraw();
	}

	@Override
	public void set2D(Boolean flat) {		
	}

	@Override
	public void reset() {
		this.zones.clear();
		this.zonesLayer.removeAllAirspaces();
	}


}
