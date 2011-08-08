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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import fr.crnan.videso3d.Couple;
import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.DatabaseManager.Type;
import fr.crnan.videso3d.VidesoController;
import fr.crnan.videso3d.VidesoGLCanvas;
import fr.crnan.videso3d.geom.LatLonCautra;
import fr.crnan.videso3d.layers.MosaiqueLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.airspaces.AirspaceAttributes;

/**
 * 
 * @author Bruno Spyckerelle
 * @version 0.1.3
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
	
	private VidesoGLCanvas wwd;
	
	public StpvController(VidesoGLCanvas wwd){
		this.wwd = wwd;
		this.wwd.firePropertyChange("step", "", "Création des éléments STPV");
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
		if(mosaiquesLayer.containsKey(type+name)){
			MosaiqueLayer mos = mosaiquesLayer.get(type+name);
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
												Type.STPV, MOSAIQUE);
			mosaiquesLayer.put(type+name, mLayer);
			mLayer.setName("Mosaïque "+type+" "+name);
			mLayer.set3D(false);
			this.toggleLayer(mLayer, true);
		}
	}

	@Override
	public void hideObject(int type, String name) {
		if(mosaiquesLayer.containsKey(type+name)){
			this.toggleLayer(mosaiquesLayer.get(type+name), false);
		}
	}

	@Override
	public void set2D(Boolean flat) {}

	@Override
	public void reset() {}

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

}