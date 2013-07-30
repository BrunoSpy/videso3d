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

package fr.crnan.videso3d.databases.terrainsoaci;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import fr.crnan.videso3d.databases.DatabaseManager;
import fr.crnan.videso3d.DatasManager;
import fr.crnan.videso3d.DatasManager.Type;
import fr.crnan.videso3d.FileManager;
import fr.crnan.videso3d.FileParser;
import fr.crnan.videso3d.geom.Latitude;
import fr.crnan.videso3d.geom.Longitude;

/**
 * Lecteur de Base Terrains OACI
 * @author David Granado
 * @version 0.0.1
 */
public class TerrainsOaci extends FileParser{
	
	/**
	 * Nom de la BDS
	 */
	private String name;

	/**
	 * Connection à la base de données
	 */
	private Connection conn;
	
	private final static String[] fileNames = {"TERRAG"};
	
	public TerrainsOaci(){
		super();
	}

	/**
	 * Construit la bdd à partir du fichier dans path
	 * @param path Chemin vers le répertoire contenant la BDS
	 */
	public TerrainsOaci(String path) {
		super(path);
	}
	
	/**
	 * Construit la bdd à partir du fichier dans path et lui donne le nom name
	 * @param path Chemin vers le répertoire contenant la BDS
	 * @param name Le nom à donner à cette base de données
	 */
	public TerrainsOaci(String path, String name){
		super(path);
		this.name = name;
	}

	/**
	 * Détermine si le fichier est un fichier Terrains OACI
	 * Cherche une ligne de la forme "AAAA  00 00 00 {N ou S} 000 00 00 {E ou W} A..." dans les 50 premières lignes du fichier
	 * @param file
	 * @return <code>true</code> si c'est un fichier Terrains OACI
	 */
	public static boolean isTerrainsOACIFile(File file){
		BufferedReader in = null;
		boolean found = false;
		try {
			in = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			int i = 0;
			while (in.ready() && !found && i<50){
				String line = in.readLine();
				if (line.matches("[A-Z]{4}  ([0-9]{2} ){3}[NS] [0-9]{3} ([0-9]{2} ){2}[EW].*")) {
					found = true;
				}
				i++ ;
			}
		} catch(IOException e){
			e.printStackTrace();
		} finally {
			if(in != null){
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return file.getName().toLowerCase().contains("terrag");
	}
	
	@Override
	protected void getFromFiles() throws IOException, SQLException {
		this.setFile("TerrainsOACI");
		this.setProgress(0);
		BufferedReader in = null;
		try{
			in = new BufferedReader(new InputStreamReader(new FileInputStream(this.path)));
			while (in.ready()){
				String line = in.readLine();
				if(line.matches("[A-Z]{4}  ([0-9]{2} ){3}[NS] [0-9]{3} ([0-9]{2} ){2}[EW].*")){
					insertTerrainOaci(line);
				}
			}
		} catch(IOException e){
			throw e;
		} finally{
			if(in != null)
				in.close();
		}
		this.setProgress(1);
	}
	
	private void insertTerrainOaci(String line) throws SQLException {
		PreparedStatement insert = this.conn.prepareStatement("insert into terrainsoaci (idoaci, latitude, longitude, name) " +
				"values (?, ?, ?, ?)");
		insert.setString(1, line.substring(0, 4));
		Latitude lat = new Latitude(line.substring(6, 16));
		Longitude lon = new Longitude(line.substring(17, 28));
		insert.setDouble(2, lat.toDecimal());
		insert.setDouble(3, lon.toDecimal());
		insert.setString(4, line.substring(29).trim());
		insert.executeUpdate();
		insert.close();
	}

	@Override
	public int numberFiles() {
		return 1;
	}

	@Override
	public Integer doInBackground() {
		try {
			this.createName();	
			//si la base de données n'existe pas
			if(!DatabaseManager.databaseExists(DatasManager.Type.TerrainsOACI, this.name)){
				//on crée la connection à la db
				this.conn = DatabaseManager.selectDB(DatasManager.Type.TerrainsOACI, this.name.trim());
				this.conn.setAutoCommit(false);
				//puis la structure de la base de donnée
				DatabaseManager.createTerrainsOaci(this.name);
				//et on remplit la bdd avec les données du fichier
				this.getFromFiles();
				this.conn.commit();
			} else {
				DatabaseManager.selectDatabase(this.name, DatasManager.Type.TerrainsOACI);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			this.cancel(true);
		} catch (IOException e) {
			e.printStackTrace();
			this.cancel(true);
		}
		return null;
	}

	/**
	 * Crée le nom de la base à partir de la date dans le fichier (généralement en 1ère ligne)
	 * Le fichier doit contenir le mot-clé "date" (ignoreCase) avant la date
	 * @throws IOException 
	 */
	private void createName() throws IOException{
		if(name == null){
			BufferedReader in = null;
			Boolean nameFound = false;
			try {
				in = new BufferedReader(new InputStreamReader(new FileInputStream(FileManager.getFile(path))));
				Pattern pattern = Pattern.compile(".*date.*");
				while (in.ready() && !nameFound){
					String line = in.readLine().toLowerCase();
					if (pattern.matcher(line).matches()){
						int researchstart = line.indexOf("date")+4;
						int start = 0;
						int end = 0;
						boolean bstart = false;
						boolean bend = false;
						int i = 0;
						int j = 0;
						while (!bstart) {
							Character c = (Character) line.charAt(researchstart+i);
							if(Character.toString(c).matches("[0-9]")) {
								bstart=true;
								start = researchstart+i;
							} else {
								i++;
							}
						}
						while (!bend) {
							Character c = (Character) line.charAt(start+j);
							if(Character.toString(c).matches(" ")) {
								bend=true;
								end = start+j;
							} else {
								j++;
							}
						}
						this.name = "TerrainsOACI_"+line.substring(start, end).replaceAll("[^0-9]", "");
						nameFound = true;
					}
				}
			} catch(IOException e){
				//rethrow exception to cancel data import
				throw e;
			} finally {
				if(in != null){
					in.close();
				}
			}
			if(nameFound == false){
				throw new IOException("Pas de date dans le fichier : impossible de nommer la base");
			}
		}
	}
	
	@Override
	public void done() {
		if(this.isCancelled()){
			try {
				DatabaseManager.deleteDatabase(name, DatasManager.Type.TerrainsOACI);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			firePropertyChange("done", true, false);
		} else {
			firePropertyChange("done", false, true);
		}
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public Type getType() {
		return DatasManager.Type.TerrainsOACI;
	}

	@Override
	public List<String> getRelevantFileNames() {
		return Arrays.asList(fileNames);
	}

}
