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

import java.util.LinkedList;
import java.util.List;

import fr.crnan.videso3d.VidesoController;
import fr.crnan.videso3d.VidesoGLCanvas;
import gov.nasa.worldwind.layers.Layer;
/**
 * Contrôle l'affichage des éléments Edimap
 * @author Bruno Spyckerelle
 * @version 0.1.2
 */
public class EdimapController implements VidesoController {

	private VidesoGLCanvas wwd;
	
	/**
	 * Liste des layers Edimap
	 */
	private List<Layer> layers = new LinkedList<Layer>();
	
	public EdimapController(VidesoGLCanvas wwd){
		this.wwd = wwd;
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
	public void highlight(int type, String name) {}

	@Override
	public void unHighlight(int type, String name) {}
	
	@Override
	public void reset() {}

	@Override
	public void showObject(int type, String name) {}

	@Override
	public void hideObject(int type, String name) {}

	@Override
	public void set2D(Boolean flat) {}

	@Override
	public int string2type(String type) {
		return 0;
	}

	@Override
	public String type2string(int type) {
		// TODO Auto-generated method stub
		return null;
	}

}
