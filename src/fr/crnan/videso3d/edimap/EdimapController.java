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

package fr.crnan.videso3d.edimap;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.swing.JOptionPane;

import fr.crnan.videso3d.DatabaseManager.Type;
import fr.crnan.videso3d.DatasManager;
import fr.crnan.videso3d.VidesoController;
import fr.crnan.videso3d.VidesoGLCanvas;
import gov.nasa.worldwind.Restorable;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.util.Logging;
/**
 * Contrôle l'affichage des éléments Edimap
 * @author Bruno Spyckerelle
 * @version 0.2.1
 */
public class EdimapController implements VidesoController {

	private VidesoGLCanvas wwd;
	
	private Cartes cartes;
	
	public EdimapController(VidesoGLCanvas wwd){
		this.wwd = wwd;
		this.wwd.firePropertyChange("step", "", "Création de cartes Edimap");
		cartes = new Cartes();
		this.wwd.toggleLayer(cartes.getLayer(), true);
	}
	
	@Override
	public void addLayer(String name, Layer layer) {
		this.wwd.addLayer(layer);
	}
	
	@Override
	public void removeLayer(String name, Layer layer) {
		this.wwd.removeLayer(layer);
	}

	@Override
	public void toggleLayer(Layer layer, Boolean state) {
		this.wwd.toggleLayer(layer, state);
	}

	@Override
	public void removeAllLayers() {
		this.wwd.removeLayer(this.cartes.getLayer());
	}

	@Override
	public void highlight(int type, String name) {
		this.showObject(type, name);
	}

	@Override
	public void unHighlight(int type, String name) {}
	
	@Override
	public void reset() {
		this.toggleLayer(this.cartes.getLayer(), false);
	}

	@Override
	public void showObject(int type, String name) {
		Carte carte = null;
		try {
			carte = cartes.getCarte(name, type2string(type));
		} catch (FileNotFoundException e) {
			Logging.logger().severe("La carte "+e.getMessage()+" est inexistante.");
			JOptionPane.showMessageDialog(null, "<html><b>Problème :</b><br />La carte demandée n'a pas pû être trouvée.<br /><br />" +
					"<b>Solution :</b><br />Supprimez la base des cartes et réimportez là.</html>", "Erreur", JOptionPane.ERROR_MESSAGE);

		} catch (SQLException e) {
			e.printStackTrace();
		}
		carte.setVisible(true);
		this.toggleLayer(this.cartes.getLayer(), true);
		this.cartes.getLayer().firePropertyChange(AVKey.LAYER, null, this.cartes.getLayer());
		//synchroniser la vue si l'appel n'a pas été fait par la vue
		DatasManager.getView(Type.Edimap).showObject(type, name);
	}

	@Override
	public void hideObject(int type, String name) {
		Carte carte = null;
		try {
			carte = cartes.getCarte(name, type2string(type));
		} catch (FileNotFoundException e) {
			Logging.logger().severe("La carte "+e.getMessage()+" est inexistante.");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		carte.setVisible(false);
		carte.setLocationsVisible(false);
		this.cartes.getLayer().firePropertyChange(AVKey.LAYER, null, this.cartes.getLayer());
		//synchroniser la vue si l'appel n'a pas été fait par la vue
		DatasManager.getView(Type.Edimap).hideObject(type, name);
	}

	@Override
	public void set2D(Boolean flat) {}

	@Override
	public int string2type(String type) {
		return Cartes.string2type(type);
	}

	@Override
	public String type2string(int type) {
		return Cartes.type2string(type);
	}

	public String toString(){
		return "Edimap";
	}

	public static int getNumberInitSteps() {
		return 1;
	}

	@Override
	public Collection<Object> getObjects(int type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setColor(Color color, int type, String name) {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public boolean isColorEditable(int type) {
		return false;
	}

	@Override
	public HashMap<Integer, List<String>> getSelectedObjectsReference() {
		HashMap<Integer, List<String>> objects = new HashMap<Integer, List<String>>();
		for(Carte c : this.cartes.getCartes()){
			if(c.isVisible()){
				if(!objects.containsKey(c.getType())){
					objects.put(c.getType(), new ArrayList<String>());
				}
				objects.get(c.getType()).add(c.getName());
			}
		}
		return objects;
	}

	@Override
	public Iterable<Restorable> getSelectedObjects() {
		List<Restorable> restorables = new ArrayList<Restorable>();
		for(Carte carte : this.cartes.getCartes()){
			if(carte.isVisible()){
				restorables.add(carte);
			}
		}
		return restorables;
	}
	
	//TODO
	public void showLocations(int type, String name){
		Carte carte = null;
		try {
			carte = cartes.getCarte(name, type2string(type));
		} catch (FileNotFoundException e) {
			Logging.logger().severe("La carte "+e.getMessage()+" est inexistante.");
			JOptionPane.showMessageDialog(null, "<html><b>Problème :</b><br />La carte demandée n'a pas pû être trouvée.<br /><br />" +
					"<b>Solution :</b><br />Supprimez la base des cartes et réimportez là.</html>", "Erreur", JOptionPane.ERROR_MESSAGE);

		} catch (SQLException e) {
			e.printStackTrace();
		}
		carte.showLocations();
	}
}
