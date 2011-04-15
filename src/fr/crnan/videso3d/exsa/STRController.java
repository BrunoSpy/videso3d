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

package fr.crnan.videso3d.exsa;

import java.awt.Color;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import fr.crnan.videso3d.Couple;
import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.VidesoController;
import fr.crnan.videso3d.VidesoGLCanvas;
import fr.crnan.videso3d.DatabaseManager.Type;
import fr.crnan.videso3d.geom.LatLonCautra;
import fr.crnan.videso3d.graphics.Radar;
import fr.crnan.videso3d.layers.MosaiqueLayer;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.SurfaceShape;
import gov.nasa.worldwind.render.airspaces.AirspaceAttributes;
import gov.nasa.worldwind.render.airspaces.BasicAirspaceAttributes;
/**
 * Contrôle l'affichage des éléments Exsa
 * @author Bruno Spyckerelle
 * @version 0.1.4
 */
public class STRController implements VidesoController {

	/**
	 * Liste des layers Mosaiques
	 */
	private HashMap<String, MosaiqueLayer> mosaiquesLayer = new HashMap<String, MosaiqueLayer>();
	/**
	 * Layer pour les radars
	 */
	private RenderableLayer radarsLayer = new RenderableLayer();
	{radarsLayer.setName("Radars");}
	/**
	 * Liste des radars affichés
	 */
	private HashMap<String, SurfaceShape> radars = new HashMap<String, SurfaceShape>();
	
	private VidesoGLCanvas wwd;
	
	private Boolean flat = true;
	
	public final static int MOSAIQUE = 4;
	public static final int MOSAIQUE_VVF = 0;
	public static final int MOSAIQUE_ZOCC = 1;
	public static final int MOSAIQUE_DYN = 2;
	public static final int MOSAIQUE_CAPA = 3;
	public static final int RADAR = 5;
	
	public STRController(VidesoGLCanvas wwd){
		this.wwd = wwd;
		this.wwd.firePropertyChange("step", "", "Création des éléments STR");
	}
	
	@Override
	public void highlight(int type, String name) {}

	@Override
	public void unHighlight(int type, String name) {	}

	@Override
	public void addLayer(String name, Layer layer) {}

	@Override
	public void removeLayer(String name, Layer layer) {}

	@Override
	public void removeAllLayers() {
		for(Layer l : mosaiquesLayer.values()){
			this.wwd.removeLayer(l);
		}
		mosaiquesLayer.clear();
		this.wwd.removeLayer(radarsLayer);
	}

	@Override
	public void toggleLayer(Layer layer, Boolean state) {
		this.wwd.toggleLayer(layer, state);
	}

	@Override
	public void showObject(int type, String name) {
		switch (type) {
		case MOSAIQUE:
			this.toggleLayer(this.createMosaiqueLayer(type, name), true);
			break;
		case MOSAIQUE_CAPA:
			this.toggleLayer(this.createMosaiqueLayer(type, name), true);
			break;
		case MOSAIQUE_DYN:
			this.toggleLayer(this.createMosaiqueLayer(type, name), true);
			break;
		case MOSAIQUE_ZOCC:
			this.toggleLayer(this.createMosaiqueLayer(type, name), true);
			break;
		case MOSAIQUE_VVF:
			this.toggleLayer(this.createMosaiqueLayer(type, name), true);
			break;
		case RADAR:
			if(!radars.containsKey(name)){
				try {
					Statement st = DatabaseManager.getCurrentExsa();
					ResultSet rs = st.executeQuery("select * from radrgener, radrtechn where radrgener.name = radrtechn.name and radrgener.name ='"+name+"'");
					if(rs.next()){
						Radar radar = new Radar(name, LatLon.fromDegrees(rs.getDouble("latitude"), rs.getDouble("longitude")), rs.getInt("portee"), 
								DatabaseManager.Type.EXSA, STRController.RADAR);
						radar.setAnnotation("<html><b>Radar : "+name+"</b><br /><br />" +
								"Portée : "+rs.getInt("portee")+"NM<br />" +
								"Numéro : "+rs.getInt("numero")+"<br />" +
								"Code pays : "+rs.getInt("codepays")+"<br />" +
								"Code radar : "+rs.getInt("coderadar")+"<br />" +
								"Tour d'antenne : "+rs.getInt("vitesse")+"s<br />" +
										"</html>");
						radarsLayer.addRenderable(radar);
						radars.put(name, radar);
						this.toggleLayer(radarsLayer, true);
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			this.wwd.redraw();
			break;
		default:
			break;
		}
		
	}

	/**
	 * Crée le calque mosaîque demandé
	 * @param type Type de mosaîque
	 * @param name Nom de la mosaïque
	 * @return {@link MosaiqueLayer}
	 */
	private MosaiqueLayer createMosaiqueLayer(int type, String name){

		if(mosaiquesLayer.containsKey(type+name)){
			MosaiqueLayer mos = mosaiquesLayer.get(type+name);
			mos.set3D(!flat);
			return mos;
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
			if(type == MOSAIQUE) {
				try {
					Statement st = DatabaseManager.getCurrentExsa();
					ResultSet rs = st.executeQuery("select * from centmosai where type ='"+name+"'");
					origine = LatLonCautra.fromCautra(rs.getDouble("xcautra"), rs.getDouble("ycautra"));
					width = rs.getInt("colonnes");
					height = rs.getInt("lignes");
					rs.close();
					st.close();
					size = 32;
					hSens = MosaiqueLayer.BOTTOM_UP;
					vSens = MosaiqueLayer.LEFT_RIGHT;
					numSens = MosaiqueLayer.VERTICAL_FIRST;
				} catch (SQLException e) {
					e.printStackTrace();
				}
			} if (type == MOSAIQUE_CAPA) {
				try {
					annotationTitle = "Filtrage capacitif "+name;
					grille = false;
					squares = new LinkedList<Couple<Integer,Integer>>();
					altitudes = new LinkedList<Couple<Double,Double>>();
					Statement st = DatabaseManager.getCurrentExsa();
					String typeGrille = name.equals("VISSEC") ? "ADP" : "CCR"; //TODO comment faire pour les autres centres ??
					ResultSet rs = st.executeQuery("select * from centmosai where type ='"+typeGrille+"'");
					origine = LatLonCautra.fromCautra(rs.getDouble("xcautra"), rs.getDouble("ycautra"));
					width = rs.getInt("colonnes");
					height = rs.getInt("lignes");
					size = 32;
					hSens = MosaiqueLayer.BOTTOM_UP;
					vSens = MosaiqueLayer.LEFT_RIGHT;
					numSens = MosaiqueLayer.VERTICAL_FIRST;
					rs = st.executeQuery("select * from ficaafniv where abonne = '"+name+"'");
					rs.next();
					for(int i=1; i<= height*width; i++){
						if(rs.getInt("carre") == i){
							if(!rs.getBoolean("elimine")){
								squares.add(new Couple<Integer, Integer>(i, 0));
								altitudes.add(new Couple<Double, Double>(rs.getInt("plancher")*30.48, rs.getInt("plafond")*30.48));
							}
							rs.next();
						} else {
							squares.add(new Couple<Integer, Integer>(i, 0));
							altitudes.add(new Couple<Double, Double>(-10.0, 660*30.48));
						}
					}
					numbers = false;
					airspaceAttr = new BasicAirspaceAttributes();
					airspaceAttr.setMaterial(Material.YELLOW);
					airspaceAttr.setOpacity(0.4);
					attr = new BasicShapeAttributes();
					attr.setInteriorMaterial(Material.YELLOW);
					attr.setInteriorOpacity(0.4);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			} else if (type == MOSAIQUE_DYN){
				annotationTitle = "Filtrage dynamique "+name;
				grille = false;
				squares = new LinkedList<Couple<Integer,Integer>>();
				altitudes = new LinkedList<Couple<Double,Double>>();
				try {
					Statement st = DatabaseManager.getCurrentExsa();
					ResultSet rs = st.executeQuery("select * from centmosai where type ='CCR'");
					origine = LatLonCautra.fromCautra(rs.getDouble("xcautra"), rs.getDouble("ycautra"));
					width = rs.getInt("colonnes");
					height = rs.getInt("lignes");
					size = 32;
					hSens = MosaiqueLayer.BOTTOM_UP;
					vSens = MosaiqueLayer.LEFT_RIGHT;
					numSens = MosaiqueLayer.VERTICAL_FIRST;
					numbers = false;
					attr = new BasicShapeAttributes();
					attr.setInteriorMaterial(Material.YELLOW);
					attr.setInteriorOpacity(0.4);
					airspaceAttr = new BasicAirspaceAttributes();
					airspaceAttr.setMaterial(Material.YELLOW);
					airspaceAttr.setOpacity(0.4);
					grille = false;
					rs = st.executeQuery("select * from ficaafnic where abonne = '"+name+"'");
					while(rs.next()){
						squares.add(new Couple<Integer, Integer>(rs.getInt("carre"), 0));
						altitudes.add(new Couple<Double, Double>(rs.getInt("plancher")*30.48, rs.getInt("plafond")*30.48));
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			} else if (type == MOSAIQUE_ZOCC){
				annotationTitle = "Zone d'occultation "+name;
				grille = false;
				squares = new LinkedList<Couple<Integer,Integer>>();
				altitudes = new LinkedList<Couple<Double,Double>>();
				try {
					Statement st = DatabaseManager.getCurrentExsa();
					ResultSet rs = st.executeQuery("select * from centmosai where type ='CCR'");
					origine = LatLonCautra.fromCautra(rs.getDouble("xcautra"), rs.getDouble("ycautra"));
					width = rs.getInt("colonnes");
					height = rs.getInt("lignes");
					size = 32;
					hSens = MosaiqueLayer.BOTTOM_UP;
					vSens = MosaiqueLayer.LEFT_RIGHT;
					numSens = MosaiqueLayer.VERTICAL_FIRST;
					numbers = false;
					attr = new BasicShapeAttributes();
					attr.setInteriorMaterial(Material.YELLOW);
					attr.setInteriorOpacity(0.4);
					airspaceAttr = new BasicAirspaceAttributes();
					airspaceAttr.setMaterial(Material.YELLOW);
					airspaceAttr.setOpacity(0.4);
					grille = false;
					rs = st.executeQuery("select * from centsczoc where zone = '"+name+"'");
					while(rs.next()){
						squares.add(new Couple<Integer, Integer>(rs.getInt("carre"), rs.getInt("souscarre")));
						altitudes.add(new Couple<Double, Double>(0.0, rs.getInt("plafond")*30.48));
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			} else if (type == MOSAIQUE_VVF){
				annotationTitle = "VVF "+name;
				grille = false;
				squares = new LinkedList<Couple<Integer,Integer>>();
				altitudes = new LinkedList<Couple<Double,Double>>();
				try {
					Statement st = DatabaseManager.getCurrentExsa();
					ResultSet rs = st.executeQuery("select * from centmosai where type ='CCR'");
					origine = LatLonCautra.fromCautra(rs.getDouble("xcautra"), rs.getDouble("ycautra"));
					width = rs.getInt("colonnes");
					height = rs.getInt("lignes");
					size = 32;
					hSens = MosaiqueLayer.BOTTOM_UP;
					vSens = MosaiqueLayer.LEFT_RIGHT;
					numSens = MosaiqueLayer.VERTICAL_FIRST;
					numbers = false;
					attr = new BasicShapeAttributes();
					attr.setInteriorMaterial(Material.YELLOW);
					attr.setInteriorOpacity(0.4);
					attr.setOutlineMaterial(Material.YELLOW);
					airspaceAttr = new BasicAirspaceAttributes();
					airspaceAttr.setMaterial(Material.YELLOW);
					airspaceAttr.setOpacity(0.4);
					grille = false;
					rs = st.executeQuery("select * from centscvvf where vvfs LIKE '%"+name+"%'");
					while(rs.next()){
						squares.add(new Couple<Integer, Integer>(rs.getInt("carre"), rs.getInt("souscarre")));
						//récupérer plancher plafond correspondants
						String[] vvfs = rs.getString("vvfs").split("\\\\");
						int numVVF = 0;
						for(int i=0;i<vvfs.length;i++){
							if(vvfs[i].equals(name)) numVVF = i;
						}
						double plancher = new Double(rs.getString("planchers").split("\\\\")[numVVF])*30.48;
						double plafond = new Double(rs.getString("plafonds").split("\\\\")[numVVF])*30.48;
						altitudes.add(new Couple<Double, Double>(plancher, plafond));//TODO gérer les VVF multiples
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			} 
			MosaiqueLayer mLayer = new MosaiqueLayer(annotationTitle, grille, origine, width, 
													height, size, hSens, vSens, numSens, squares,
													altitudes, numbers, attr, airspaceAttr,
													Type.EXSA, type);
			mosaiquesLayer.put(type+name, mLayer);
			mLayer.setName("Mosaïque "+type+" "+name);
			mLayer.set3D(!flat);
			return mLayer;
		}
	}

	@Override
	public void hideObject(int type, String name) {
		if(type == RADAR){
			if(radars.containsKey(name)){
				radarsLayer.removeRenderable(radars.get(name));
				radars.remove(name);
				this.wwd.redraw();
			}
		} else {
			this.toggleLayer(this.mosaiquesLayer.get(type+name), false);
		}
	}

	@Override
	public void reset() {} //Géré par STRView

	@Override
	public void set2D(Boolean flat) {
		if(this.flat != flat){
			this.flat = flat;
			for(MosaiqueLayer l : mosaiquesLayer.values()){
				l.set3D(!flat);
			}
		}
	}

	@Override
	public int string2type(String type) {
		return 0;
	}

	/* (non-Javadoc)
	 * @see fr.crnan.videso3d.VidesoController#type2string(int)
	 */
	@Override
	public String type2string(int type) {
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
