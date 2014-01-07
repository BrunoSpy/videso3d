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

package fr.crnan.videso3d.databases.terrainsoaci;

import java.awt.Color;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import fr.crnan.videso3d.DatasManager;
import fr.crnan.videso3d.VidesoController;
import fr.crnan.videso3d.VidesoGLCanvas;
import fr.crnan.videso3d.databases.DatabaseManager;
import fr.crnan.videso3d.graphics.Balise2D;
import fr.crnan.videso3d.graphics.DatabaseBalise2D;
import fr.crnan.videso3d.layers.Balise2DLayer;
import gov.nasa.worldwind.Restorable;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.Layer;

/**
 * Contrôle l'affichage des terrains OACI
 * @author David Granado
 * @version 0.0.1
 */
public class TerrainsOaciController implements VidesoController {

	private VidesoGLCanvas wwd;
	
	private Balise2DLayer terrLayer;
	
	/**
	 * Liste nominale des terrains
	 */
	private HashMap<String, Balise2D> terrains = new HashMap<String, Balise2D>();
	
	private Balise2D highlight;
	
	public TerrainsOaciController(VidesoGLCanvas wwd) {
		this.wwd = wwd;
		this.terrLayer = new Balise2DLayer("Terrains OACI");
		this.buildTerrainsOaci();
	}

	/**
	 * Construit ou met à jour les Terrains OACI
	 */
	private void buildTerrainsOaci() {
		this.wwd.firePropertyChange("step", "", "Suppression des Terrains OACI");
		if(terrLayer != null) {
			this.toggleLayer(terrLayer, true);
			terrLayer.removeAllBalises();
		}
		try {
			if(DatabaseManager.getCurrentTerrainsOACI() != null) {
				//création des nouveaux objets
				this.wwd.firePropertyChange("step", "", "Création des Terrains OACI");	
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		this.wwd.redraw();
	}

	@Override
	public void highlight(int type, String idoaci) {
		this.showObject(idoaci);
		Balise2D terrain = (Balise2D) terrains.get(idoaci);
		terrain.setHighlighted(true);
		this.unHighlightPrevious(terrain);
		highlight = terrain;
		this.wwd.getView().goTo(terrain.getPosition(), 4e5);
	}
	
	public void highlight(String idoaci){
		highlight(0, idoaci);
	}

	private void unHighlightPrevious(Balise2D terrain) {
		if(highlight != null){
			if(highlight == terrain) {
				return;
			} else {
				highlight.setHighlighted(false);
				highlight = null;
			}
		}
	}

	@Override
	public void unHighlight(int type, String name) {}

	@Override
	public void addLayer(String name, Layer layer) {}

	@Override
	public void removeLayer(String name, Layer layer) {}

	@Override
	public void removeAllLayers() {
		this.wwd.removeLayer(terrLayer);

	}

	@Override
	public void toggleLayer(Layer layer, Boolean state) {
		this.wwd.toggleLayer(layer, state);
	}

	@Override
	public void showObject(int type, String idoaci) {
		this.createTerrainsOACI(idoaci);
		this.terrLayer.showBalise(idoaci, type);
		DatasManager.getView(DatasManager.Type.TerrainsOACI).showObject(type, idoaci);
	}
	
	/**
	 * showObject adapté à Terrain OACI (un seul type)
	 * @param idoaci
	 */
	public void showObject(String idoaci) {
		showObject(0, idoaci);
	}

	private void createTerrainsOACI(String idoaci) {
		if(!terrains.containsKey(idoaci)) {
			try {
				Statement st = DatabaseManager.getCurrentTerrainsOACI();
				ResultSet rs = st.executeQuery("select * from terrainsoaci where idoaci = '" + idoaci + "'");
				Balise2D terrain = null;
				if(rs.next()){
					String annotation = "<p><b>Terrain "+rs.getString("idoaci") +"</b></p>";
					annotation += "Nom : "+rs.getString("name")+"<br />";
					terrain = new DatabaseBalise2D(rs.getString("idoaci"), Position.fromDegrees(rs.getDouble("latitude"), rs.getDouble("longitude"), 100.0), annotation, DatasManager.Type.TerrainsOACI, 0, terrLayer.getTextLayer());
					terrLayer.addBalise(terrain);
					//lien nominal
					this.terrains.put(rs.getString("idoaci"), terrain);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void hideObject(int type, String name) {
		this.terrLayer.hideBalise(name, type);
	}
	
	/**
	 * hideObject adapté à Terrain OACI (un seul type)
	 * @param idoaci
	 */
	public void hideObject(String idoaci) {
		hideObject(0, idoaci);
	}

	@Override
	public int string2type(String type) {
		return 0;
	}

	@Override
	public String type2string(int type) {
		return "Terrain OACI";
	}

	public String toString() {
		return "";
	}
	
	@Override
	public void set2D(Boolean flat) {}

	@Override
	public void reset() {
		this.terrLayer.setLocked(false);
		this.terrLayer.removeAllBalises();
	}

	@Override
	public Collection<Object> getObjects(int type) {
		return null;
	}

	@Override
	public void setColor(Color color, int type, String name) {}

	@Override
	public boolean isColorEditable(int type) {
		return false;
	}
	
	public boolean isColorEditable() {
		return false;
	}

	@Override
	public HashMap<Integer, List<String>> getSelectedObjectsReference() {
		HashMap<Integer, List<String>> objects = new HashMap<Integer, List<String>>();
		List<String> lTerr = new ArrayList<String>();
		lTerr.addAll(terrLayer.getVisibleBalisesNames());
		if(!lTerr.isEmpty()) objects.put(0, lTerr);
		return objects;
	}

	@Override
	public Iterable<Restorable> getSelectedObjects() {
		List<Restorable> restorables = new LinkedList<Restorable>();
		restorables.addAll(this.terrLayer.getVisibleBalises());
		return restorables;
	}

	@Override
	public boolean areLocationsVisible(int type, String name) {
		return terrains.get(name).isLocationVisible();
	}
	
	public boolean areLocationsVisible(String idoaci) {
		return areLocationsVisible(0, idoaci);
	}

	@Override
	public void setLocationsVisible(int type, String name, boolean b) {
		terrains.get(name).setLocationVisible(b);
	}
	
	public void setLocationsVisible(String idoaci, boolean b) {
		setLocationsVisible(0, idoaci, b);
	}

	public static int getNumberInitSteps() {
		return 1;
	}
	
}
