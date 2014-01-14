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
package fr.crnan.videso3d.project;

import java.awt.Color;
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

import fr.crnan.videso3d.CompatibilityVersionException;
import fr.crnan.videso3d.DatasManager;
import fr.crnan.videso3d.DatasManager.Type;
import fr.crnan.videso3d.Couple;
import fr.crnan.videso3d.FileManager;
import fr.crnan.videso3d.ProgressSupport;
import fr.crnan.videso3d.Triplet;
import fr.crnan.videso3d.UserObjectsController;
import fr.crnan.videso3d.Videso3D;
import fr.crnan.videso3d.VidesoGLCanvas;
import fr.crnan.videso3d.databases.DatabaseManager;
import fr.crnan.videso3d.formats.VidesoTrack;
import fr.crnan.videso3d.formats.geo.GEOTrack;
import fr.crnan.videso3d.formats.geo.GEOWriter;
import fr.crnan.videso3d.formats.images.EditableSurfaceImage;
import fr.crnan.videso3d.formats.images.ImageUtils;
import fr.crnan.videso3d.graphics.DatabaseVidesoObject;
import fr.crnan.videso3d.ihm.MainWindow;
import fr.crnan.videso3d.ihm.TrajectoriesView;
import fr.crnan.videso3d.layers.tracks.GEOTracksLayer;
import fr.crnan.videso3d.layers.tracks.TrajectoriesLayer;
import gov.nasa.worldwind.Restorable;
import gov.nasa.worldwind.layers.Layer;

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
 * <ul><li> (F) one .geo file for each trajectory view</li>
 * <li> one file for each for each properties of each trajectory view</li></ul>
 * <li> (F) globe.xml : parameters of the globe (camera, ...)</li>
 * </ul>
 * <li>
 * </ul>
 * @author Bruno Spyckerelle
 * @version 0.1.2
 */
public class ProjectManager extends ProgressSupport {

	private HashMap<DatasManager.Type, HashMap<Integer, List<String>>> objects;
	
	private boolean otherObjects = false;
	private boolean trajectories = false;
	
	private VidesoGLCanvas wwd;
	
	public ProjectManager(){
		super();
		objects = new HashMap<DatasManager.Type, HashMap<Integer,List<String>>>();
	}

	/**
	 * 
	 * @return Set of exportables types
	 */
	public Set<DatasManager.Type> getTypes(){
		return objects.keySet();
	}

	/**
	 * Creates list of objects in order to allow the selection of objects to be saved
	 */
	public void prepareSaving(VidesoGLCanvas ww){
		this.wwd = ww;
		//Objets des bases de données
		for(DatasManager.Type type : DatasManager.Type.values()){
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

		//trajectoires
		for(Layer l : wwd.getModel().getLayers()){
			if(l instanceof GEOTracksLayer){
				this.trajectories = true;
				break;
			}
		}
		
		//objects from other projets
		//TODO use UserObjectController ?
//		for(Layer l : wwd.getModel().getLayers()){
//			if(l.getName().equals(RENDERABLE_LAYER_NAME) || 
//					l.getName().equals(BALISES2D_LAYER_NAME) || 
//					l.getName().equals(BALISES3D_LAYER_NAME) ||
//					l.getName().equals(AIRSPACE_LAYER_NAME)){
//				this.otherObjects = true;
//				break;
//			}
//		}
		//user added objects
		if(DatasManager.getController(Type.UserObject) != null && ((UserObjectsController) DatasManager.getController(Type.UserObject)).hasUserObjects()){
			this.otherObjects = true;
		}
	}

	/**
	 * Before invoking this method, <code>prepareSaving</code> has to be called once.
	 * @param file .vpj file
	 * @param types Types d'objets à enregistrer
	 * @param images Nom des images à enregistrer
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

		if(index == -1){
			return false;
		}
		
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

				DatasManager.Type type = DatabaseManager.stringToType(t);
				if(type != null) {
					if(onlyLinks){
						if(databasesIncluded){
							String currentName = DatabaseManager.getCurrentName(type);
							//for each database with selected objects, we copy the sqlite file
							File baseCopy = new File(databases, currentName+"."+type);
							baseCopy.createNewFile();

							FileInputStream sourceStream = new FileInputStream(new File(currentName));
							FileChannel source = sourceStream.getChannel();
							FileOutputStream destStream = new FileOutputStream(baseCopy);
							FileChannel destination = destStream.getChannel();

							destination.transferFrom(source, 0, source.size());

							source.close();
							sourceStream.close();
							destination.close();
							destStream.close();

							//save the keys
							List<String[]> clefs = new ArrayList<String[]>();
							Statement st = DatabaseManager.getCurrent(DatasManager.Type.Databases);
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

									sourceStream = new FileInputStream(f);
									source = sourceStream.getChannel();
									destStream = new FileOutputStream(copy);
									destination = destStream.getChannel();

									destination.transferFrom(source, 0, source.size());

									source.close();
									sourceStream.close();
									destination.close();
									destStream.close();
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
		for(EditableSurfaceImage si : DatasManager.getUserObjectsController(wwd).getImages()){
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
			if(l instanceof GEOTracksLayer && trajectories != null && trajectories.contains(l.getName())){
				GEOWriter geoWriter = new GEOWriter(trajectoDir.getAbsolutePath()+"/"+l.getName(), true);
				//hidden trajectories
				List<Integer> hiddenTraj = new ArrayList<Integer>();
				for(VidesoTrack track : ((GEOTracksLayer) l).getModel().getAllTracks()){
					geoWriter.writeTrack((GEOTrack) track);
					if(!((GEOTracksLayer) l).getModel().isVisible(track))
						hiddenTraj.add(track.getNumTraj());
				}
				geoWriter.close();
				//save properties
				TrajectoriesView view = DatasManager.getTrajectoryView(((GEOTracksLayer) l).getModel());
				//filtres couleurs
				ObjectOutputStream oos = null;
				try{
					oos = new ObjectOutputStream(new FileOutputStream(new File(trajectoDir.getAbsolutePath(), l.getName()+"_colors")));
					oos.writeObject(view.getColorFilters());
				} catch(IOException e){
					e.printStackTrace();
				} finally{
					if(oos != null)
						oos.close();
				}
				//multicolors
				try {
					oos = new ObjectOutputStream(new FileOutputStream(new File(trajectoDir.getAbsolutePath(), l.getName()+"_multicolors")));
					Couple<Integer, Couple<Double[], Color[]>> params = new Couple<Integer, Couple<Double[],Color[]>>();
					params.setFirst(((TrajectoriesLayer)l).getParamColor());
					params.setSecond(((TrajectoriesLayer)l).getMultiColors());
					oos.writeObject(params);
				} catch(IOException e){
					e.printStackTrace();
				} finally {
					if(oos != null)
						oos.close();
				}
				//filtres
				try{
					oos = new ObjectOutputStream(new FileOutputStream(new File(trajectoDir.getAbsolutePath(), l.getName()+"_filters")));
					oos.writeObject(view.getFilters());
				} catch(IOException e){
					e.printStackTrace();
				} finally{
					if(oos != null)
						oos.close();
				}
				//hidden trajectories
				try{
					oos = new ObjectOutputStream(new FileOutputStream(new File(trajectoDir.getAbsolutePath(), l.getName()+"_hidden")));
					oos.writeObject(hiddenTraj);
				} catch(IOException e){
					e.printStackTrace();
				} finally{
					if(oos != null)
						oos.close();
				}
			}
		}

		//other objects
		//objects previously loaded with a project
		//and user generated objects
		//TODO !!
		if(types != null && types.contains("Autres objets affichés.")){
//			//user added objects
			for(Restorable r : DatasManager.getUserObjectsController(wwd).getUserObjects()){
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
	public Project loadProject(File file, VidesoGLCanvas ww, MainWindow window, boolean force)
			throws FileNotFoundException, IOException, ClassNotFoundException, CompatibilityVersionException, InstantiationException, IllegalAccessException {

		this.wwd = ww;
		
		FileManager.unzip(file);

		int ind = file.getName().lastIndexOf(".");

		String path = "temp"+file.getName()+"/"+file.getName().substring(0, ind); //don't forget to delete the extension

		Project project = new Project(file.getName().substring(0, ind));
		
		int max = FileManager.getFilesCount(new File(path));
		this.fireTaskStarts(max);

		int progress = 0;

		//check version
		if(!force){
			File version = new File(path, "version");
			BufferedReader reader = null;
			try{
				reader = new BufferedReader(new FileReader(version));

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
			} catch(IOException e){
				throw e;
			} finally {
				if(reader != null)
					reader.close();
			}
		}

		//import databases
		final File databases = new File(path, "databases");
		if(databases.exists()){
			for(final DatasManager.Type t : DatasManager.Type.values()){
				boolean found = false;
				File[] db = databases.listFiles(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return name.endsWith("."+t);
					}
				});
				File[] links = databases.listFiles(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return name.equals(t);
					}
				});
				
				//first check if a full db is present
				if(db.length == 1){
					File f = db[0];
					int index = f.getName().lastIndexOf(".");
					final String suffix = index == -1 ? "" : f.getName().substring(index+1);
					final String name = index == -1 ? f.getName() : f.getName().substring(0, index);
					final DatasManager.Type type = DatabaseManager.stringToType(index == -1 ? name : suffix);
					//in that case, import db
					try {
						if(!DatabaseManager.databaseExists(t, name)) {
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
								ObjectInputStream ois = null;
								try{
									ois = new ObjectInputStream(new FileInputStream(clefsFile));

									List<String[]> clefs = (List<String[]>) ois.readObject();
									Statement st = DatabaseManager.getCurrent(DatasManager.Type.Databases);
									for(String[] c : clefs){
										st.executeUpdate("insert into clefs (name, type, value) values ('"+c[0]+"', '"+name+"', '"+c[1]+"')");
									}
									st.close();
								} catch(IOException e){
									e.printStackTrace();
								} finally{
									if(ois != null)
										ois.close();
								}
							}
						}
						DatabaseManager.addDatabase(name, type);
						DatabaseManager.fireBaseSelected(type);
						found = true;
					} catch (SQLException e) {
						e.printStackTrace();
					}
				} else if(links.length == 1){
					//if there's no database, check if there's a link file
					//in that case, try to select the database
					if(DatasManager.getController(t) == null) {
						//ask the DatabaseManager to select a base
						try {
							if(DatabaseManager.selectDatabase(t)){
								DatabaseManager.fireBaseSelected(t);
								found = true;
							} else {
								//TODO informer d'une erreur
							}
						} catch (SQLException e) {
							e.printStackTrace();
						}
					} else {
						//if a database is already selected, no event is fired
						//force selection of objects
						selectObjectWithController(databases, t);
					}
					
				}
				if(found){
					//Once the database is selected and the controller created, check the objects
					DatasManager.addPropertyChangeListener("done", new PropertyChangeListener() {
						@Override
						public void propertyChange(PropertyChangeEvent evt) {
							selectObjectWithController(databases, t);
							DatasManager.removePropertyChangeListener("done", this); //ensure this is just done once
						}
					});
				}
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
				BufferedReader input = null;
				try{
					input = new BufferedReader(new FileReader(f));

					String s = input.readLine();
					while(input.ready()){
						s += input.readLine();
					}

					if(o instanceof DatabaseVidesoObject) {
						//object is not linked to a database anymore : downgrade it to the relevant class
						Class<?> c2 = Class.forName(((DatabaseVidesoObject) o).getRestorableClassName());
						o = (Restorable) c2.newInstance();
						o.restoreState(s);
					} else {
						o.restoreState(s);
					}
					
				} catch (IOException e){
					throw e;
				} finally{
					if(input != null)
						input.close();
				}
				project.addObject(o);
			}
		}

		//load images
		File imageDir = new File(path, "images");
		if(imageDir.exists() && imageDir.isDirectory()){
			for(File i : imageDir.listFiles()){
				this.fireTaskProgress(progress++);
				project.addObject(ImageUtils.createEditableImage(i, wwd));
			}
		}

		//trajectories
		final File trajectoDir = new File(path, "trajectory");
		if(trajectoDir.exists() && trajectoDir.isDirectory()){
			for(final File t : trajectoDir.listFiles()){
				this.fireTaskProgress(progress++);
				if(t.getName().toLowerCase().endsWith(".geo")){
					DatasManager.addPropertyChangeListener("new trajectory view", new PropertyChangeListener() {

						@Override
						public void propertyChange(PropertyChangeEvent evt) {
							TrajectoriesView view = (TrajectoriesView) evt.getNewValue();
							ObjectInputStream ois = null;
							//colors
							File propFile = new File(trajectoDir, t.getName()+"_colors");
							try{
								ois = new ObjectInputStream(new FileInputStream(propFile));
								List<Triplet<String, String, Color>> colorsProperties = (List<Triplet<String, String, Color>>) ois.readObject();
								if(colorsProperties != null){
									view.setColorFilters(colorsProperties);
								}
							
							} catch(IOException | ClassNotFoundException e){
								e.printStackTrace();
							} finally{
								if(ois != null)
									try {
										ois.close();
									} catch (IOException e) {
										e.printStackTrace();
									}
							}
							//multicolors
							propFile = new File(trajectoDir, t.getName()+"_multicolors");
							try {
								ois = new ObjectInputStream(new FileInputStream(propFile));
								Couple<Integer, Couple<Double[], Color[]>> multiColors = (Couple<Integer, Couple<Double[], Color[]>>) ois.readObject();
								if(multiColors != null){
									view.getLayer().setMultiColors(multiColors.getFirst(), multiColors.getSecond().getFirst(), multiColors.getSecond().getSecond());
								}
							} catch(IOException | ClassNotFoundException e){
								e.printStackTrace();
							} finally{
								if(ois != null)
									try {
										ois.close();
									} catch (IOException e) {
										e.printStackTrace();
									}
							}
							//regex filters
							propFile = new File(trajectoDir, t.getName()+"_filters");
							try{
								ois = new ObjectInputStream(new FileInputStream(propFile));
								HashMap<Integer, String> filterProperties = (HashMap<Integer, String>) ois.readObject();
								if(filterProperties != null){
									view.setRegexFilters(filterProperties);
								}
							
							} catch(IOException | ClassNotFoundException e){
								e.printStackTrace();
							} finally{
								if(ois != null)
									try {
										ois.close();
									} catch (IOException e) {
										e.printStackTrace();
									}
							}
							//hidden trajectories
							propFile = new File(trajectoDir, t.getName()+"_hidden");
							try{
								ois = new ObjectInputStream(new FileInputStream(propFile));
								List<Integer> hidden = (List<Integer>) ois.readObject();
								if(hidden != null){
									view.hideTrajectories(hidden);
								}
							
							} catch(IOException | ClassNotFoundException e){
								e.printStackTrace();
							} finally{
								if(ois != null)
									try {
										ois.close();
									} catch (IOException e) {
										e.printStackTrace();
									}
							}
							//unregister myself once job is done
							DatasManager.removePropertyChangeListener(this);
						}
					});
					window.addTrajectoriesViews(new File[]{t});
				}
			}
		}
		
		//globe
		File globe = new File(path, "globe.xml");
		if(globe.exists()){
			this.fireTaskProgress(progress++);
			
			BufferedReader input = null;
			try {
				input = new BufferedReader(new FileReader(globe));

				String s = "";
				while(input.ready()){
					s += input.readLine();
				}
				wwd.getView().stopMovement();
				wwd.getView().restoreState(s);
			} catch (IOException e){
				e.printStackTrace();
			} finally{
				if(input != null)
					input.close();
			}
			
		}
		//remove temp files
	//	FileManager.removeTempFiles(); //can't remove temp files : trajectory views are done in a swingworker. this will be done when app is closed
		this.fireTaskProgress(max);
		
		return project;
	}

	private void selectObjectWithController(File databases, DatasManager.Type type){
		File objectsFile = new File(databases, type.toString());
		ObjectInputStream ois = null;
		try {
			if(objectsFile.exists()){
				ois = new ObjectInputStream(new FileInputStream(objectsFile));
				HashMap<Integer, List<String>> objects = (HashMap<Integer, List<String>>) ois.readObject();
				for(Integer i : objects.keySet()){
					for(String n : objects.get(i)){
						DatasManager.getController(type).showObject(i, n);
					}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally{
			if(ois != null)
				try {
					ois.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	public boolean isOtherObjects() {
		return otherObjects;
	}

	public boolean isTrajectories(){
		return trajectories;
	}
	
	public boolean hasImages(){
		return DatasManager.getUserObjectsController(wwd).hasImages();
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
