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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.channels.FileChannel;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import fr.crnan.videso3d.DatabaseManager.Type;

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
					HashMap<Integer, List<String>> o = DatasManager.getController(type).getSelectedObjects();

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
	 * @throws IOException 
	 */
	public void saveProject(File file) throws IOException{
		
		int index = file.getName().lastIndexOf(".");
		
		String name = file.getName().substring(0, index);
		
		//create main directory
		File main = new File(name);
		main.mkdir();
		
		//create databases directory
		if(!objects.isEmpty()){
			File databases = new File(main.getAbsolutePath()+"/databases");
			databases.mkdir();

			try{
				for(Type type : objects.keySet()){

					//for each database with selected objects, we copy the sqlite file
					File baseCopy = new File(databases, DatabaseManager.getCurrentName(type)+"."+type);
					baseCopy.createNewFile();

					FileChannel source = new FileInputStream(new File(DatabaseManager.getCurrentName(type))).getChannel();
					FileChannel destination = new FileOutputStream(baseCopy).getChannel();

					destination.transferFrom(source, 0, source.size());

					source.close();
					destination.close();

					//then we save the selected objects
					File selectedObjects = new File(databases, type.toString());
					selectedObjects.createNewFile();
					ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(selectedObjects));
					oos.writeObject(objects.get(type));
					oos.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		//create the zip file
		FileManager.createZipFile(file, main);
		
		//delete main directory
		FileManager.deleteFile(main);
		
	}
	
	public void loadProject(File file) throws FileNotFoundException, IOException, ClassNotFoundException{
		
		List<File> files = FileManager.unzip(file);
		
		int ind = file.getName().lastIndexOf(".");
		//import databases
		String path = "temp"+file.getName()+"/"+file.getName().substring(0, ind); //don't forget to delete the extension
		final File databases = new File(path, "databases");
		for(File f : databases.listFiles()){
			int index = f.getName().lastIndexOf(".");
			final String suffix = index == -1 ? "" : f.getName().substring(index+1);
			String name = index == -1 ? f.getName() : f.getName().substring(0, index);
			if(!suffix.isEmpty() && DatabaseManager.stringToType(suffix) != null) {
				try {
					if(!DatabaseManager.databaseExists(DatabaseManager.stringToType(suffix), name)) {
						FileManager.copyFile(f);
					}
					DatabaseManager.addDatabase(name, DatabaseManager.stringToType(suffix));
				} catch (SQLException e) {
					e.printStackTrace();
				}
				//Once the database is selected and the controller created, check the objects
				DatasManager.addPropertyChangeListener("done", new PropertyChangeListener() {
					
					@Override
					public void propertyChange(PropertyChangeEvent evt) {
						File objectsFile = new File(databases, suffix);
						try {
							ObjectInputStream ois = new ObjectInputStream(new FileInputStream(objectsFile));
							HashMap<Integer, List<String>> objects = (HashMap<Integer, List<String>>) ois.readObject();
							for(Integer i : objects.keySet()){
								for(String n : objects.get(i)){
									DatasManager.getController(DatabaseManager.stringToType(suffix)).showObject(i, n);
								}
							}
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						} catch (ClassNotFoundException e) {
							e.printStackTrace();
						}
						DatasManager.removePropertyChangeListener("done", this); //ensure this is just done once
					}
				});
				
			} 
		}
		
		//remove temp files
		FileManager.removeTempFiles();
	}
	
}
