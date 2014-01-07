
package fr.crnan.videso3d.databases.radio;

import java.awt.Color;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.swing.SwingWorker;

import fr.crnan.videso3d.DatasManager;
import fr.crnan.videso3d.DatasManager.Type;
import fr.crnan.videso3d.VidesoController;
import fr.crnan.videso3d.VidesoGLCanvas;
import fr.crnan.videso3d.databases.DatabaseManager;
import fr.crnan.videso3d.ihm.RadioCovView;
import fr.crnan.videso3d.layers.RadioCovLayer;
import gov.nasa.worldwind.Restorable;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.render.airspaces.Airspace;


/**
 * 
 * @author Bruno Spyckerelle
 * @author Mickael Papail
 * @version 0.2.1
 */
public class RadioCovController implements VidesoController {

	private VidesoGLCanvas wwd;
	
	/**
	 * Liste des layers couvertures radios
	 */
	private RadioCovLayer radioCovLayer;
	
	public static final int ANTENNE = 0;
	private boolean visionMode;
	
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
				if(radioView.initRadioCovAirspaces() && radioView.initRadioFrequencies()) {}
					radioView.create3DPage1();
					radioView.create3DPage2();										
			}			
		}.execute();
	
	}

	
	public void setVisionMode(boolean visionMode) {
		this.visionMode = visionMode;
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

	public void hideAllRadioCovLayers() {
		radioCovLayer.hideAllRadioCovLayers();
	}
	
	public void displayAllRadioCovLayers() {
		radioCovLayer.displayAllRadioCovLayers();
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

	
	/*Utilisé par le RadioCovView pour forcer le rafraichissement d'une couverture radio */
	public void redrawNow(){
		this.wwd.redrawNow();
	}


	/**
	 * Not implemented in this controller
	 */
	public boolean areLocationsVisible(int type, String name) {
		return false;
	}
	
	/**
	 * Not implemented in this controller
	 */
	public void setLocationsVisible(int type, String name, boolean b) {
	}
}
