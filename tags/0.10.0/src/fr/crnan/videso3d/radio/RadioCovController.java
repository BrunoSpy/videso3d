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

import java.awt.Color;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.swing.SwingWorker;

import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.DatasManager;
import fr.crnan.videso3d.VidesoController;
import fr.crnan.videso3d.VidesoGLCanvas;
import fr.crnan.videso3d.DatabaseManager.Type;
import fr.crnan.videso3d.ihm.RadioCovView;
import fr.crnan.videso3d.layers.RadioCovLayer;
import gov.nasa.worldwind.Restorable;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.render.airspaces.Airspace;
/**
 * 
 * @author Bruno Spyckerelle
 * @version 0.2.1
 */
public class RadioCovController implements VidesoController {

	private VidesoGLCanvas wwd;
	
	/**
	 * Liste des layers couvertures radios
	 */
	private RadioCovLayer radioCovLayer;
	
	public static final int ANTENNE = 0; 
	
	public RadioCovController(final VidesoGLCanvas wwd){
		this.wwd = wwd;
		
		//Layer des radio couv
		radioCovLayer = new RadioCovLayer("Radio Coverage",this.wwd);

		
		new SwingWorker<Void, Void>(){
			@Override
			protected Void doInBackground(){
				try {
					if(DatabaseManager.getCurrentRadioCov() != null) {
						ArrayList<String> radioCovPathTab = new ArrayList<String>();
						radioCovPathTab = DatabaseManager.getCurrentRadioCovPath();
						for (int i=0;i<radioCovPathTab.size();i++) {
							wwd.firePropertyChange("step", "", "Création des données radio");
							RadioDataManager radioDataManager = new RadioDataManager(radioCovPathTab.get(i));							
							insertAllRadioCovLayers(radioDataManager.loadData());		
							for (int j=0;j<radioCovPathTab.size();j++) {
							}
						}	
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
				return null;
			}
			
			@Override
			protected void done(){
				RadioCovView radioView  = (RadioCovView)DatasManager.getView(Type.RadioCov);
				if(radioView.initRadioCovAirspaces())
					radioView.feedPanel();
			}
			
		}.execute();

		

	}

	@Override
	public void highlight(int type, String name) {
		this.showObject(type, name);
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
		//synchroniser la vue si l'appel n'a pas été fait par la vue
		DatasManager.getView(Type.RadioCov).showObject(type, name);
	}

	@Override
	public void hideObject(int type, String name) {
		radioCovLayer.removeVisibleRadioCov(name);
		this.wwd.redrawNow();
		//synchroniser la vue si l'appel n'a pas été fait par la vue
		DatasManager.getView(Type.RadioCov).hideObject(type, name);
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
	
	public static int getNumberInitSteps(){
		return 1;
	}

	@Override
	public Collection<Object> getObjects(int type) {
		throw new UnsupportedOperationException("Not implemented");
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
