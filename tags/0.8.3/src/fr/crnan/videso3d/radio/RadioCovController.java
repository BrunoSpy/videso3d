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

package fr.crnan.videso3d.radio;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.VidesoController;
import fr.crnan.videso3d.VidesoGLCanvas;
import fr.crnan.videso3d.layers.RadioCovLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.render.airspaces.Airspace;
/**
 * 
 * @author Bruno Spyckerelle
 * @version 0.2
 */
public class RadioCovController implements VidesoController {

	private VidesoGLCanvas wwd;
	
	/**
	 * Liste des layers couvertures radios
	 */
	private RadioCovLayer radioCovLayer;
	
	public static final int ANTENNE = 0; 
	
	public RadioCovController(VidesoGLCanvas wwd){
		this.wwd = wwd;
		
		//Layer des radio couv
		radioCovLayer = new RadioCovLayer("Radio Coverage",this.wwd);

		try {
			if(DatabaseManager.getCurrentRadioCov() != null) {
				ArrayList<String> radioCovPathTab = new ArrayList<String>();
				radioCovPathTab = DatabaseManager.getCurrentRadioCovPath();
				for (int i=0;i<radioCovPathTab.size();i++) {
					this.wwd.firePropertyChange("step", "", "Création des données radio");
					RadioDataManager radioDataManager = new RadioDataManager(radioCovPathTab.get(i));							
					this.insertAllRadioCovLayers(radioDataManager.loadData());		
					for (int j=0;j<radioCovPathTab.size();j++) {
					}
					
				}	
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void highlight(int type, String name) {
		// TODO Auto-generated method stub

	}

	@Override
	public void unHighlight(int type, String name) {
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
		radioCovLayer.removeAllRadioCovLayers();
	}

	@Override
	public void toggleLayer(Layer layer, Boolean state) {
		// TODO Auto-generated method stub

	}

	@Override
	public void showObject(int type, String name) {
		//un seul type ...
		radioCovLayer.addVisibleRadioCov(name);		
		this.wwd.redrawNow();
	}

	@Override
	public void hideObject(int type, String name) {
		radioCovLayer.removeVisibleRadioCov(name);
		this.wwd.redrawNow();
	}

	@Override
	public int string2type(String type) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String type2string(int type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void set2D(Boolean flat) {
		// TODO Auto-generated method stub

	}

	@Override
	public void reset() {
		this.removeAllLayers();
	}

	
	public void insertAllRadioCovLayers(ArrayList<Airspace> airspaces) {
		radioCovLayer.insertAllRadioCovLayers(airspaces);
		this.wwd.redrawNow();
	}

	public List<Layer> getLayers() {
		return wwd.getModel().getLayers();
	}
	
}