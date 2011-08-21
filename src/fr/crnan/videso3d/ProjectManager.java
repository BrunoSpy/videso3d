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

import fr.crnan.videso3d.DatabaseManager.Type;
import fr.crnan.videso3d.graphics.Balise;
import fr.crnan.videso3d.graphics.Balise2D;
import fr.crnan.videso3d.graphics.Balise3D;
import fr.crnan.videso3d.graphics.DatabaseVidesoObject;
import fr.crnan.videso3d.layers.Balise2DLayer;
import fr.crnan.videso3d.layers.Balise3DLayer;
import gov.nasa.worldwind.Restorable;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.Renderable;

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
 * @version 0.0.2
 */
public class ProjectManager extends ProgressSupport {

	private HashMap<Type, HashMap<Integer, List<String>>> objects;
	
	public ProjectManager(){
		super();
		objects = new HashMap<DatabaseManager.Type, HashMap<Integer,List<String>>>();
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
	public void prepareSaving(){
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
		
		//trajectoires
	}
	
	/**
	 * Before invoking this method, <code>prepareSaving</code> has to be called once.
	 * @param file .vpj file
	 * @param databasesIncluded If true, include databases into the project file. Only usefull if <code>onlyLinks</code> is true
	 * @param onlyLinks If true, save links to databases. If false, save standalone objects.
	 * @return True if successfull
	 * @throws IOException 
	 * @throws SQLException 
	 */
	public boolean saveProject(File file, boolean databasesIncluded, boolean onlyLinks) throws IOException, SQLException{
		
		int index = file.getName().lastIndexOf(".");
		
		String name = file.getName().substring(0, index);
		
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
		if(!objects.isEmpty()){
			File databases = new File(main.getAbsolutePath()+"/databases");
			databases.mkdir();

				for(Type type : objects.keySet()){

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
						DecimalFormat f = new DecimalFormat("####");
	                    f.setMinimumIntegerDigits(4);
	                    int count = 0;
						for(Restorable r : DatasManager.getController(type).getSelectedObjects()){
							File object = new File(xmlDir, r.getClass().getName()+"-"+type+"-"+f.format(count++)+".xml");
							PrintWriter of = new PrintWriter(object);
							of.write(r.getRestorableState());
							of.flush();
							of.close();
						}
					}
				}
			
		}
		//create the zip file
		FileManager.createZipFile(file, main);
		
		//delete main directory
		FileManager.deleteFile(main);
				
		return true;
		
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
	 */
	public void loadProject(File file, VidesoGLCanvas wwd, boolean force)
			throws FileNotFoundException, IOException, ClassNotFoundException, CompatibilityVersionException {
		
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
		if(xmlDir.exists()){
			RenderableLayer xmlRenderables = null;
			Balise2DLayer xmlBalises = null;
			Balise3DLayer xmlBalises3D = null;
			for(File f : xmlDir.listFiles()){
				this.fireTaskProgress(progress++);
				this.fireTaskInfo(f.getName());
				String[] name = f.getName().split("-");
				try {
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
					
					if(o instanceof Renderable){
						if(xmlRenderables == null){
							xmlRenderables = new RenderableLayer();
							xmlRenderables.setName("XML Renderables");
							wwd.toggleLayer(xmlRenderables, true);
						}
						xmlRenderables.addRenderable((Renderable) o);
					} else if(o instanceof Balise2D){
						if(xmlBalises == null){
							xmlBalises = new Balise2DLayer("XML Balises 2D");
							wwd.toggleLayer(xmlBalises, true);
						}
						xmlBalises.addBalise((Balise) o);
						xmlBalises.showBalise((Balise) o);
					} else if(o instanceof Balise3D){
						if(xmlBalises3D == null){
							xmlBalises3D = new Balise3DLayer("XML Balises 3D");
							wwd.toggleLayer(xmlBalises3D, true);
						}
						xmlBalises3D.addBalise((Balise) o);
						xmlBalises3D.showBalise((Balise) o);
					} else if(o instanceof Layer) {
						wwd.toggleLayer((Layer) o, true);
					}

				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
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
	
}
