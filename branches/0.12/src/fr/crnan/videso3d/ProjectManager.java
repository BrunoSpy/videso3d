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
package fr.crnan.videso3d;

import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import fr.crnan.videso3d.databases.DatabaseManager;
import fr.crnan.videso3d.databases.DatabaseManager.Type;
import fr.crnan.videso3d.databases.edimap.Carte;
import fr.crnan.videso3d.formats.VidesoTrack;
import fr.crnan.videso3d.formats.geo.GEOTrack;
import fr.crnan.videso3d.formats.geo.GEOWriter;
import fr.crnan.videso3d.formats.images.EditableSurfaceImage;
import fr.crnan.videso3d.formats.images.ImageUtils;
import fr.crnan.videso3d.graphics.Balise;
import fr.crnan.videso3d.graphics.Balise2D;
import fr.crnan.videso3d.graphics.Balise3D;
import fr.crnan.videso3d.graphics.DatabaseVidesoObject;
import fr.crnan.videso3d.graphics.RestorableUserFacingText;
import fr.crnan.videso3d.graphics.editor.PolygonEditorsManager;
import fr.crnan.videso3d.ihm.MainWindow;
import fr.crnan.videso3d.layers.Balise2DLayer;
import fr.crnan.videso3d.layers.Balise3DLayer;
import fr.crnan.videso3d.layers.FilterableAirspaceLayer;
import fr.crnan.videso3d.layers.PriorityRenderableLayer;
import fr.crnan.videso3d.layers.TextLayer;
import fr.crnan.videso3d.layers.tracks.GEOTracksLayer;
import fr.crnan.videso3d.layers.tracks.TrajectoriesLayer;
import gov.nasa.worldwind.Restorable;
import gov.nasa.worldwind.layers.AirspaceLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.GeographicText;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.airspaces.Airspace;

/**
 * Load and save projects.<br />
 * A project is a zip file containing :
 * <ul><li>displayed objects</li>
 * <li>databases required to display these objects</li>
 * <li>trajectories</li>
 * <li>displayed images</li>
 * </ul>
 * The project file has a .vpj extension and is structured as follow :
 * <ul>
 * <li> (R) project_name : main directory
 * <ul>
 * <li> (F) version : compatibility</li>
 * <li> (R) images : geotiff images</li>
 * <li> (R) xml : 3D objects
 * <ul><li>files whose name is the class of the object</li></ul></li>
 * <li> (R) databases : required SQLite databases
 * <ul><li>one file per database</li>
 * <li>one file per controller</li></ul>
 * </li>
 * <li> (R) trajectory : all displayed trajectories</li>
 * <li> (F) globe.xml : parameters of the globe (camera, ...)</li>
 * </ul>
 * <li>
 * </ul>
 * @author Bruno Spyckerelle
 * @version 0.0.4
 */
public class ProjectManager extends ProgressSupport {

	private HashMap<Type, HashMap<Integer, List<String>>> objects;
	private List<EditableSurfaceImage> images;

	private boolean otherObjects = false;
	private boolean trajectories = false;
	
	private static final String RENDERABLE_LAYER_NAME = "XML Renderables";

	private static final String BALISES2D_LAYER_NAME = "XML Balises 2D";

	private static final String BALISES3D_LAYER_NAME = "XML Balises 3D";
	
	private static final String AIRSPACE_LAYER_NAME = "XML Airspaces";

	private static final String TEXT_LAYER_NAME = "XML Texts";
	
	private VidesoGLCanvas wwd;
	
	public ProjectManager(){
		super();
		objects = new HashMap<DatabaseManager.Type, HashMap<Integer,List<String>>>();
		images = new ArrayList<EditableSurfaceImage>();
	}

	/**
	 * 
	 * @return Set of exportables types
	 */
	public Set<Type> getTypes(){
		return objects.keySet();
	}

	/**
	 * Creates list of objects in order to allow the selection of objects to be saved
	 */
	public void prepareSaving(VidesoGLCanvas ww){
		this.wwd = ww;
		//Objets des bases de données
		for(Type type : Type.values()){
			try {
				if(DatabaseManager.getCurrent(type) != null && DatasManager.getController(type) != null){
					HashMap<Integer, List<String>> o = DatasManager.getController(type).getSelectedObjectsReference();

					if(o != null && !o.isEmpty()) {
						objects.put(type, o);
					}
				}
			}catch (SQLException e) {
				e.printStackTrace();
			}

		}

		//images
		if(!wwd.getImagesController().getImages().isEmpty()){
			this.images = wwd.getImagesController().getImages();
		}

		//trajectoires
		for(Layer l : wwd.getModel().getLayers()){
			if(l instanceof GEOTracksLayer){
				this.trajectories = true;
				break;
			}
		}
		
		//other objects
		if(PolygonEditorsManager.getLayer().getAirspaces().iterator().hasNext())
			this.otherObjects = true;
		//objects from other projets
		for(Layer l : wwd.getModel().getLayers()){
			if(l.getName().equals(RENDERABLE_LAYER_NAME) || 
					l.getName().equals(BALISES2D_LAYER_NAME) || 
					l.getName().equals(BALISES3D_LAYER_NAME) ||
					l.getName().equals(AIRSPACE_LAYER_NAME)){
				this.otherObjects = true;
				break;
			}
		}
		//user added objects
		if(wwd.hasUserObjects())
			this.otherObjects = true;
	}

	/**
	 * Before invoking this method, <code>prepareSaving</code> has to be called once.
	 * @param file .vpj file
	 * @param types Types d'objets à enregistrer
	 * @param images Nom de images à enregistrer
	 * @param trajectories Nom des trajectoires à enregistrer
	 * @param databasesIncluded If true, include databases into the project file. Only usefull if <code>onlyLinks</code> is true
	 * @param onlyLinks If true, save links to databases. If false, save standalone objects.
	 * @return True if successfull
	 * @throws IOException 
	 * @throws SQLException 
	 */
	public boolean saveProject(File file, Set<String> types,  Set<String> images,  Set<String> trajectories, 
			boolean databasesIncluded, boolean onlyLinks) throws IOException, SQLException{

		int index = file.getName().lastIndexOf(".");

		String name = file.getName().substring(0, index);

		//count for xml files
		DecimalFormat format = new DecimalFormat("####");
		format.setMinimumIntegerDigits(4);
		int count = 0;
		
		//create main directory
		File main = new File(name);
		if(main.exists()){
			throw new IOException(main.getAbsolutePath());
		}
		main.mkdir();

		//Save version
		File version = new File(main, "version");
		version.createNewFile();
		PrintWriter writer = new PrintWriter(version);
		writer.write(Videso3D.VERSION);
		writer.flush();
		writer.close();

		//create xml directory
		File xmlDir = new File(main.getAbsolutePath()+ "/xml");
		xmlDir.mkdir();

		//create databases directory
		if(types != null && !types.isEmpty()){
			File databases = new File(main.getAbsolutePath()+"/databases");
			databases.mkdir();

			for(String t : types){

				Type type = DatabaseManager.stringToType(t);
				if(type != null) {
					if(onlyLinks){
						if(databasesIncluded){
							String currentName = DatabaseManager.getCurrentName(type);
							//for each database with selected objects, we copy the sqlite file
							File baseCopy = new File(databases, currentName+"."+type);
							baseCopy.createNewFile();

							FileChannel source = new FileInputStream(new File(currentName)).getChannel();
							FileChannel destination = new FileOutputStream(baseCopy).getChannel();

							destination.transferFrom(source, 0, source.size());

							source.close();
							destination.close();

							//save the keys
							List<String[]> clefs = new ArrayList<String[]>();
							Statement st = DatabaseManager.getCurrent(Type.Databases);
							ResultSet rs = st.executeQuery("select * from clefs where type='"+currentName+"'");
							while(rs.next()){
								clefs.add(new String[]{rs.getString("name"), rs.getString("value")});
							}
							st.close();

							if(!clefs.isEmpty()){
								File clefsFile = new File(databases, currentName+"_clefs");
								clefsFile.createNewFile();
								ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(clefsFile));
								oos.writeObject(clefs);
								oos.close();
							}								

							//and we copy the associated files
							File filesDir = new File(currentName+"_files");
							if(filesDir.exists() && filesDir.isDirectory()){
								File baseFiles = new File(databases, currentName+"_files");
								baseFiles.mkdirs();
								for(File f : filesDir.listFiles()) {
									File copy = new File(baseFiles, f.getName());
									copy.createNewFile();

									source = new FileInputStream(f).getChannel();
									destination = new FileOutputStream(copy).getChannel();

									destination.transferFrom(source, 0, source.size());

									source.close();
									destination.close();

								}
							}
						}
						//then we save the selected objects
						File selectedObjects = new File(databases, type.toString());
						selectedObjects.createNewFile();
						ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(selectedObjects));
						oos.writeObject(objects.get(type));
						oos.close();
					} else {
						for(Restorable r : DatasManager.getController(type).getSelectedObjects()){
							this.saveObjectInXml(r, new File(xmlDir, r.getClass().getName()+"-"+type+"-"+format.format(count++)+".xml"));
						}
					}
				} 
			}

		}

		//save images
		File imagesDir = new File(main.getAbsolutePath()+ "/images");
		imagesDir.mkdir();
		for(EditableSurfaceImage si : this.getImages()){
			if(images.contains(si.getName())){
				int idx = si.getName().lastIndexOf(".");
				String newName = si.getName();
				if(idx != -1){
					newName = si.getName().substring(0, idx);
				}
				File img = new File(imagesDir, newName+".gtif");
				ImageUtils.writeImageToFile(si.getSector(), (BufferedImage) si.getImageSource(), img);
			}
		}

		//trajectoires
		File trajectoDir = new File(main, "trajectory");
		trajectoDir.mkdirs();
		for(Layer l : wwd.getModel().getLayers()){
			if(l instanceof GEOTracksLayer && trajectories.contains(l.getName())){
				GEOWriter geoWriter = new GEOWriter(trajectoDir.getAbsolutePath()+"/"+l.getName(), true);
				for(VidesoTrack track : ((GEOTracksLayer) l).getModel().getVisibleTracks()){
					geoWriter.writeTrack((GEOTrack) track);
				}
				geoWriter.close();
			}
		}

		//other objects
		//objects previously loaded with a project
		if(types != null && types.contains("Autres objets affichés.")){
			for(Layer l : wwd.getModel().getLayers()){
				if(l.getName().equals(AIRSPACE_LAYER_NAME)){
					for(Airspace r : ((AirspaceLayer) l).getAirspaces()){
						this.saveObjectInXml(
								(Restorable) r, 
								new File(xmlDir, r.getClass().getName()+"-"+format.format(count++)+".xml"));
					}
				} else if(l.getName().equals(RENDERABLE_LAYER_NAME)){
					for(Renderable r : ((RenderableLayer) l).getRenderables()){
						if(r instanceof Restorable){
							this.saveObjectInXml(
									(Restorable) r, 
									new File(xmlDir, r.getClass().getName()+"-"+format.format(count++)+".xml"));
						}
					}
				} else if(l.getName().equals(BALISES2D_LAYER_NAME)){
					for(Balise2D b : ((Balise2DLayer)l).getVisibleBalises()){
						this.saveObjectInXml(b, new File(xmlDir, b.getClass().getName()+"-"+format.format(count++)+".xml"));
					}
				} else if(l.getName().equals(BALISES3D_LAYER_NAME)){
					for(Balise3D b : ((Balise3DLayer)l).getVisibleBalises()){
						this.saveObjectInXml(b, new File(xmlDir, b.getClass().getName()+"-"+format.format(count++)+".xml"));
					}
				}
			}
			for(Airspace a : PolygonEditorsManager.getLayer().getAirspaces()){
				if(a.isVisible())
					this.saveObjectInXml(a, new File(xmlDir, a.getClass().getName()+"-"+format.format(count++)+".xml"));
			}
			//user added objects
			for(Restorable r : wwd.getUserObjects()){
				this.saveObjectInXml(r, new File(xmlDir, r.getClass().getName()+"-"+format.format(count++)+".xml"));
			}
		}
		
		
		
		//globe parameters
		this.saveObjectInXml(this.wwd.getView(), new File(main, "globe.xml"));
		
		//create the zip file
		FileManager.createZipFile(file, main);

		//delete main directory
		FileManager.deleteFile(main);

		return true;

	}

	private void saveObjectInXml(Restorable object, File file) throws FileNotFoundException{
		PrintWriter of = new PrintWriter(file);
		of.write(object.getRestorableState());
		of.flush();
		of.close();
	}
	
	/**
	 * 
	 * @param file
	 * @param wwd
	 * @param force Don't check version of the file
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws CompatibilityVersionException
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public void loadProject(File file, VidesoGLCanvas ww, MainWindow window, boolean force)
			throws FileNotFoundException, IOException, ClassNotFoundException, CompatibilityVersionException, InstantiationException, IllegalAccessException {

		this.wwd = ww;
		
		FileManager.unzip(file);

		int ind = file.getName().lastIndexOf(".");

		String path = "temp"+file.getName()+"/"+file.getName().substring(0, ind); //don't forget to delete the extension

		int max = FileManager.getFilesCount(new File(path));
		this.fireTaskStarts(max);

		int progress = 0;

		//check version
		if(!force){
			File version = new File(path, "version");
			BufferedReader reader = new BufferedReader(new FileReader(version));
			if(reader.ready()){
				String lin = reader.readLine();
				String[] line = lin.split("\\.");
				String[] currentVersion = Videso3D.VERSION.split("\\.");
				int major = new Integer(line[0]);
				int minor = new Integer(line[1]);
				int majorCurrent = new Integer(currentVersion[0]);
				int minorCurrent = new Integer(currentVersion[1]);
				if(major > majorCurrent) {
					throw new CompatibilityVersionException(line[0]+"."+line[1]+"."+line[2]);
				} else {
					if(minor > minorCurrent) {
						throw new CompatibilityVersionException(line[0]+"."+line[1]+"."+line[2]);
					} 
				}
			}
		}

		//import databases
		final File databases = new File(path, "databases");
		if(databases.exists()){
			for(File f : databases.listFiles()){
				if(f.isDirectory())//only files
					continue;
				this.fireTaskProgress(progress++);
				this.fireTaskInfo(f.getName());
				int index = f.getName().lastIndexOf(".");
				final String suffix = index == -1 ? "" : f.getName().substring(index+1);
				final String name = index == -1 ? f.getName() : f.getName().substring(0, index);
				final Type type = DatabaseManager.stringToType(index == -1 ? name : suffix);
				if(!suffix.isEmpty() && type != null) { //base de données existantes
					try {
						if(!DatabaseManager.databaseExists(type, name)) {
							//sqlite database
							FileManager.copyFileAs(f.getAbsolutePath(), name);
							//associated files
							File filesDir = new File(databases, name+"_files");
							if(filesDir.exists() && filesDir.isDirectory()){
								for(File filesBase : filesDir.listFiles()){
									FileManager.copyFile(filesBase, name+"_files");
								}
							}
							//clefs
							File clefsFile = new File(databases, name+"_clefs");
							if(clefsFile.exists()){
								ObjectInputStream ois = new ObjectInputStream(new FileInputStream(clefsFile));
								List<String[]> clefs = (List<String[]>) ois.readObject();
								Statement st = DatabaseManager.getCurrent(Type.Databases);
								for(String[] c : clefs){
									st.executeUpdate("insert into clefs (name, type, value) values ('"+c[0]+"', '"+name+"', '"+c[1]+"')");
								}
								st.close();
							}
						}
						DatabaseManager.addDatabase(name, type);
						DatabaseManager.fireBaseSelected(type);
					} catch (SQLException e) {
						e.printStackTrace();
					}


				} else if(suffix.isEmpty() && type != null){ //only links to objects, without databases

					if(databases.listFiles(new FilenameFilter() {//verify that there's no database of this type

						@Override
						public boolean accept(File dir, String name) {
							return name.endsWith("."+type);
						}
					}).length ==0){
						if(DatasManager.getController(type) == null) {
							//ask the DatabaseManager to select a base
							try {
								if(DatabaseManager.selectDatabase(type)){
									DatabaseManager.fireBaseSelected(type);
								} else {
									//TODO informer d'une erreur
								}
							} catch (SQLException e) {
								e.printStackTrace();
							}
						} else {
							selectObjectWithController(databases, name, type);
						}

					}
				}
				//Once the database is selected and the controller created, check the objects
				DatasManager.addPropertyChangeListener("done", new PropertyChangeListener() {

					@Override
					public void propertyChange(PropertyChangeEvent evt) {
						selectObjectWithController(databases, suffix.isEmpty() ? name : suffix, type);
						DatasManager.removePropertyChangeListener("done", this); //ensure this is just done once
					}
				});
			}
		}
		//import xml files
		File xmlDir = new File(path, "xml");
		if(xmlDir.exists() && xmlDir.isDirectory()){
			for(File f : xmlDir.listFiles()){
				this.fireTaskProgress(progress++);
				this.fireTaskInfo(f.getName());
				String[] name = f.getName().split("-");

				Class<?> c = Class.forName(name[0]);			
				Restorable o = (Restorable) c.newInstance();
				BufferedReader input = new BufferedReader(new FileReader(f));
				String s = input.readLine();
				while(input.ready()){
					s += input.readLine();
				}
				if(o instanceof DatabaseVidesoObject) {
					Class<?> c2 = Class.forName(((DatabaseVidesoObject) o).getRestorableClassName());
					o = (Restorable) c2.newInstance();
					o.restoreState(s);
				} else {
					o.restoreState(s);
				}
				if(o instanceof Carte){
					PriorityRenderableLayer renderableLayer = (PriorityRenderableLayer) this.getLayer(RENDERABLE_LAYER_NAME);
					FilterableAirspaceLayer airspaceLayer = (FilterableAirspaceLayer) this.getLayer(AIRSPACE_LAYER_NAME);
					TextLayer textLayer = (TextLayer) this.getLayer(TEXT_LAYER_NAME);
					((Carte) o).setLayers(renderableLayer, airspaceLayer, textLayer);
					((Carte) o).setVisible(true);
				} else if(o instanceof Airspace){
					((AirspaceLayer) this.getLayer(AIRSPACE_LAYER_NAME)).addAirspace((Airspace) o);
				} else if(o instanceof Renderable){
					((RenderableLayer) this.getLayer(RENDERABLE_LAYER_NAME)).addRenderable((Renderable) o);
				} else if(o instanceof Balise2D){
					((Balise2DLayer) this.getLayer(BALISES2D_LAYER_NAME)).addBalise((Balise) o);
					((Balise2DLayer) this.getLayer(BALISES2D_LAYER_NAME)).showBalise((Balise) o);
				} else if(o instanceof Balise3D){
					((Balise3DLayer) this.getLayer(BALISES3D_LAYER_NAME)).addBalise((Balise) o);
					((Balise3DLayer) this.getLayer(BALISES3D_LAYER_NAME)).showBalise((Balise) o);
				} else if(o instanceof RestorableUserFacingText){
					((TextLayer) this.getLayer(TEXT_LAYER_NAME)).addGeographicText((GeographicText) o);
				}else if(o instanceof Layer) {
					wwd.toggleLayer((Layer) o, true);
				}
			}
		}

		//load images
		File imageDir = new File(path, "images");
		if(imageDir.exists() && imageDir.isDirectory()){
			for(File i : imageDir.listFiles()){
				this.fireTaskProgress(progress++);
				wwd.getImagesController().addEditableImages(new File[]{i});
			}
		}

		//trajectories
		File trajectoDir = new File(path, "trajectory");
		if(trajectoDir.exists() && trajectoDir.isDirectory()){
			for(File t : trajectoDir.listFiles()){
				this.fireTaskProgress(progress++);
				window.addTrajectoriesViews(new File[]{t});
			}
		}
		
		//globe
		File globe = new File(path, "globe.xml");
		if(globe.exists()){
			this.fireTaskProgress(progress++);
			
			BufferedReader input = new BufferedReader(new FileReader(globe));
			String s = "";
			while(input.ready()){
				s += input.readLine();
			}
			wwd.getView().stopMovement();
			wwd.getView().restoreState(s);
		}
		//remove temp files
		FileManager.removeTempFiles();
		this.fireTaskProgress(max);
	}

	private void selectObjectWithController(File databases, String suffix, Type type){
		File objectsFile = new File(databases, suffix);
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(objectsFile));
			HashMap<Integer, List<String>> objects = (HashMap<Integer, List<String>>) ois.readObject();
			for(Integer i : objects.keySet()){
				for(String n : objects.get(i)){
					DatasManager.getController(type).showObject(i, n);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private PriorityRenderableLayer xmlRenderables = null;
	private Balise2DLayer xmlBalises = null;
	private Balise3DLayer xmlBalises3D = null;
	private FilterableAirspaceLayer xmlAirspaces = null;
	private TextLayer xmlTexts = null;

	public Layer getLayer(String name){
		
		if(name.equals(AIRSPACE_LAYER_NAME)){
			if(xmlAirspaces == null){
				xmlAirspaces = new FilterableAirspaceLayer();
				xmlAirspaces.setName(AIRSPACE_LAYER_NAME);
				wwd.toggleLayer(xmlAirspaces, true);
			}
			return xmlAirspaces;
		} else if(name.equals(RENDERABLE_LAYER_NAME)){
			if(xmlRenderables == null){
				xmlRenderables = new PriorityRenderableLayer();
				xmlRenderables.setName(RENDERABLE_LAYER_NAME);
				wwd.toggleLayer(xmlRenderables, true);
			}
			return xmlRenderables;
		} else if(name.equals(BALISES2D_LAYER_NAME)){
			if(xmlBalises == null){
				xmlBalises = new Balise2DLayer(BALISES2D_LAYER_NAME);
				wwd.toggleLayer(xmlBalises, true);
			}
			return xmlBalises;
		} else if(name.equals(BALISES3D_LAYER_NAME)){
			if(xmlBalises3D == null){
				xmlBalises3D = new Balise3DLayer(BALISES3D_LAYER_NAME);
				wwd.toggleLayer(xmlBalises3D, true);
			}
			return xmlBalises3D;
		} else if(name.equals(TEXT_LAYER_NAME)){
			if(xmlTexts == null){
				xmlTexts = new TextLayer(TEXT_LAYER_NAME);
				wwd.toggleLayer(xmlTexts, true);
			}
			return xmlTexts;
		}
		return null;
	}
	
	/**
	 * Exportable images
	 * @return List of exportable images
	 */
	public List<EditableSurfaceImage> getImages() {
		return this.images;
	}

	public boolean isOtherObjects() {
		return otherObjects;
	}

	public boolean isTrajectories(){
		return trajectories;
	}
	
	/**
	 * 
	 * @return Trajectories layers, only GEO for the moment
	 */
	public List<TrajectoriesLayer> getTrajectoriesLayers(){
		List<TrajectoriesLayer> layers = new ArrayList<TrajectoriesLayer>();
		for(Layer l : this.wwd.getModel().getLayers()){
			if(l instanceof GEOTracksLayer && !((GEOTracksLayer)l).getModel().getVisibleTracks().isEmpty()){
				layers.add((TrajectoriesLayer) l);
			}
		}
		return layers;
	}
}
