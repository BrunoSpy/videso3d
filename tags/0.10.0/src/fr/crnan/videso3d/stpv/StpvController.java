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
package fr.crnan.videso3d.stpv;

import java.awt.Color;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import fr.crnan.videso3d.Couple;
import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.DatasManager;
import fr.crnan.videso3d.DatabaseManager.Type;
import fr.crnan.videso3d.VidesoController;
import fr.crnan.videso3d.VidesoGLCanvas;
import fr.crnan.videso3d.geom.LatLonCautra;
import fr.crnan.videso3d.graphics.DatabaseRoute2D;
import fr.crnan.videso3d.graphics.Route2D;
import fr.crnan.videso3d.layers.MosaiqueLayer;
import fr.crnan.videso3d.stip.StipController;
import gov.nasa.worldwind.Restorable;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.airspaces.AirspaceAttributes;

/**
 * 
 * @author Bruno Spyckerelle
 * @version 0.1.4
 */
public class StpvController implements VidesoController {

	public static final int MOSAIQUE = 0;

	public static final int BALISE = 1;

	public static final int SECTEUR = 2;
	
	public static final int STAR = 3;
	
	/**
	 * Liste des layers Mosaiques
	 */
	private HashMap<String, MosaiqueLayer> mosaiquesLayer = new HashMap<String, MosaiqueLayer>();
	
	/**
	 * Layer pour les stars
	 */
	private RenderableLayer starsLayer;
	
	private HashMap<String, Route2D> stars = new HashMap<String, Route2D>();
	
	private VidesoGLCanvas wwd;
	
	public StpvController(VidesoGLCanvas wwd){
		this.wwd = wwd;
		this.wwd.firePropertyChange("step", "", "Création des éléments STPV");
		if(starsLayer != null){
			starsLayer.removeAllRenderables();
			this.toggleLayer(starsLayer, true);
			stars.clear();
		} else {
			starsLayer = new RenderableLayer();
			starsLayer.setName("Stpv STARs");
			this.toggleLayer(starsLayer, true);
		}
	}
	
	@Override
	public void highlight(int type, String name) {
		this.showObject(type, name);
	}

	@Override
	public void unHighlight(int type, String name) {
	}

	@Override
	public void addLayer(String name, Layer layer) {
	}

	@Override
	public void removeLayer(String name, Layer layer) {
	}

	@Override
	public void removeAllLayers() {
		for(Layer l : mosaiquesLayer.values()){
			this.wwd.removeLayer(l);
		}
		mosaiquesLayer.clear();
	}

	@Override
	public void toggleLayer(Layer layer, Boolean state) {
		this.wwd.toggleLayer(layer, state);
	}

	@Override
	public void showObject(int type, String name) {
		switch(type){
		case MOSAIQUE :
			if(mosaiquesLayer.containsKey(name)){
				MosaiqueLayer mos = mosaiquesLayer.get(name);
				mos.set3D(false);
				this.toggleLayer(mos, true);
			} else {
				String annotationTitle = null;
				Boolean grille = true;
				LatLonCautra origine = null; 
				Integer width = 0;
				Integer height = 0;
				Integer size = 0; 
				int hSens = 0; 
				int vSens = 0;
				int numSens = 0;
				List<Couple<Integer, Integer>> squares = null;
				List<Couple<Double, Double>> altitudes = null;
				Boolean numbers = true;
				ShapeAttributes attr = null;
				AirspaceAttributes airspaceAttr = null;
				try {
					Statement st = DatabaseManager.getCurrentStpv();
					ResultSet rs = st.executeQuery("select * from mosaique where type ='"+name+"'");
					origine = LatLonCautra.fromCautra(rs.getDouble("xcautra")-512, rs.getDouble("ycautra")-512);
					width = rs.getInt("nombre");
					height = rs.getInt("nombre");
					size = rs.getInt("carre");
					hSens = MosaiqueLayer.BOTTOM_UP;
					vSens = MosaiqueLayer.LEFT_RIGHT;
					numSens = MosaiqueLayer.HORIZONTAL_FIRST;
				} catch (SQLException e) {
					e.printStackTrace();
				}

				MosaiqueLayer mLayer = new MosaiqueLayer(annotationTitle, grille, origine, 
						width, height, size, hSens, vSens, numSens, 
						squares, altitudes, numbers, attr, airspaceAttr, 
						Type.STPV, MOSAIQUE, name);
				mosaiquesLayer.put(name, mLayer);
				mLayer.setName("Mosaïque "+type+" "+name);
				mLayer.set3D(false);
				this.toggleLayer(mLayer, true);
			}
			break;
		case STAR :
			if(this.stars.containsKey(name)){
				this.stars.get(name).setVisible(true);
			} else {
				this.createStar(name);
			}
			for(String balise : this.stars.get(name).getBalises()){
				DatasManager.getController(Type.STIP).showObject(StipController.BALISES, balise);
			}
			break;
		default : 
			break;
		}
		//synchroniser la vue si l'appel n'a pas été fait par la vue
		DatasManager.getView(Type.STPV).showObject(type, name);
	}


	private void createStar(String name) {
		Integer id = new Integer(name);
		DatabaseRoute2D star = new DatabaseRoute2D(name, Type.STPV, STAR);
		try {
			Statement st = DatabaseManager.getCurrentStpv();
			ResultSet rs = st.executeQuery("select name from lieu901 where lieu90='"+id+"'");
			String annotation = "<html><b>STAR ";
			if(rs.next()){
				String starName = rs.getString(1);
				if(rs.next()){
					String secondStarName = rs.getString(1);
					annotation+=starName+"/"+secondStarName.charAt(secondStarName.length()-1)+"</b><br/>";
				}else{
					annotation+= starName+"</b><br/>";
				}
			}
			rs = st.executeQuery("select * from lieu90 where id ='"+id+"'");
			ArrayList<String> balises = new ArrayList<String>();
			ArrayList<LatLon> pos = new ArrayList<LatLon>();
			if(rs.next()){
				for(int i=4; i<12; i++){
					String balise = rs.getString(i);
					if(!balise.isEmpty())
						balises.add(rs.getString(i));
					else
						break;
				}
				annotation+=((rs.getBoolean(12)?"Hélices ":"") + (rs.getBoolean(13)?"Réacteurs<br/>":"<br/>") 
						+ (rs.getBoolean(14)?"FIR ":"") + (rs.getBoolean(15)?"UIR":""));
			}
			for(String balise : balises){
				st = DatabaseManager.getCurrentStip();
				rs = st.executeQuery("select latitude, longitude from balises where name ='"+balise+"'");
				pos.add(LatLon.fromDegrees(rs.getDouble(1), rs.getDouble(2)));
			}
			star.setLocations(pos);
			star.setBalises(balises);
			star.setAnnotation(annotation);
			st.close();
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		this.stars.put(name, star);
		this.starsLayer.addRenderable(star);
		this.starsLayer.firePropertyChange(AVKey.LAYER, null, this.starsLayer);
	}
	
	
	@Override
	public void hideObject(int type, String name) {
		switch (type) {
		case MOSAIQUE:
			if(mosaiquesLayer.containsKey(name))
				this.toggleLayer(mosaiquesLayer.get(name), false);
			break;
		case STAR :
			if(this.stars.containsKey(name)){
				this.stars.get(name).setVisible(false);
				for(String balise : this.stars.get(name).getBalises()){
					DatasManager.getController(Type.STIP).showObject(StipController.BALISES, balise);
				}
			}
		default:
			break;
		}
		//synchroniser la vue si l'appel n'a pas été fait par la vue
		DatasManager.getView(Type.STPV).hideObject(type, name);
	}

	@Override
	public void set2D(Boolean flat) {}

	@Override
	public void reset() {
		for(Renderable r : this.starsLayer.getRenderables()){
			((Route2D) r).setVisible(false);
		}
	}

	@Override
	public int string2type(String type) {
		return 0;
	}

	@Override
	public String type2string(int type) {
		return null;
	}

	public static int getNumberInitSteps() {
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
		HashMap<Integer, List<String>> objects = new HashMap<Integer, List<String>>();
		
		List<String> mosaiques = new ArrayList<String>();
		for(Entry<String, MosaiqueLayer> m : mosaiquesLayer.entrySet()){
			if(m.getValue().isEnabled())
				mosaiques.add(m.getKey());
		}
		if(!mosaiques.isEmpty())
			objects.put(MOSAIQUE, mosaiques);
		return objects;
	}

	@Override
	public Iterable<Restorable> getSelectedObjects() {
		ArrayList<Restorable> restorables = new ArrayList<Restorable>();
		for(Layer l : mosaiquesLayer.values()){
			if(l.isEnabled())
				restorables.add(l);
		}
		return restorables;
	}

}
