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
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;

import fr.crnan.videso3d.DatabaseManager.Type;
import fr.crnan.videso3d.DatasManager;
import fr.crnan.videso3d.VidesoController;
import fr.crnan.videso3d.VidesoGLCanvas;
import gov.nasa.worldwind.Restorable;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.util.Logging;
/**
 * Contrôle l'affichage des éléments Edimap
 * @author Bruno Spyckerelle
 * @version 0.1.3
 */
public class EdimapController implements VidesoController {

	private VidesoGLCanvas wwd;
	
	/**
	 * Liste des layers Edimap
	 */
	private List<Layer> layers = new LinkedList<Layer>();
	
	private Cartes cartes;
	
	public EdimapController(VidesoGLCanvas wwd){
		this.wwd = wwd;
		this.wwd.firePropertyChange("step", "", "Création de cartes Edimap");
		cartes = new Cartes();
	}
	
	@Override
	public void addLayer(String name, Layer layer) {
		if(!layers.contains(layer)){
			this.layers.add(layer);
			try {
				this.wwd.addLayer(layer);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	@Override
	public void removeLayer(String name, Layer layer) {
		this.wwd.removeLayer(layer);
		this.layers.remove(layer);
	}

	@Override
	public void toggleLayer(Layer layer, Boolean state) {
		this.wwd.toggleLayer(layer, state);
	}

	@Override
	public void removeAllLayers() {
		for(Layer layer : layers){
			this.wwd.removeLayer(layer);
		}
		this.layers.clear();
	}

	@Override
	public void highlight(int type, String name) {
		this.showObject(type, name);
	}

	@Override
	public void unHighlight(int type, String name) {}
	
	@Override
	public void reset() {
		for(Layer l : layers){
			this.toggleLayer(l, false);
		}
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
		this.addLayer(name, carte);
		this.toggleLayer(carte, true);
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
		this.toggleLayer(carte, false);
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterable<Restorable> getSelectedObjects() {
		// TODO Auto-generated method stub
		return null;
	}
}
