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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.FileParser;
import fr.crnan.videso3d.DatabaseManager.Type;

/**
 * Lecteur de BDS Stpv
 * @author Bruno Spyckerelle
 * @version 0.0.2
 */
public class Stpv extends FileParser{

	/**
	 * Nom de la BDS
	 */
	private String name;

	/**
	 * Connection à la base de données
	 */
	private Connection conn;
	
	
	public Stpv(){
		super();
	}
	


	/**
	 * Construit la bdd à partir des fichiers dans path
	 * @param path Chemin vers le répertoire contenant la BDS
	 * @param db Gestionnaire de base de données
	 */
	public Stpv(String path, DatabaseManager db) {
		super(path, db);
	}
	
	
	@Override
	public Integer doInBackground() {
		this.getName();
		try {
			//on crée la connection à la db
			this.conn = this.db.selectDB(Type.STPV, this.name);
			//si la base de données n'existe pas
			if(!this.db.databaseExists(this.name)){
				//puis la structure de la base de donnée
				this.db.createSTPV(this.name);
				//et on remplit la bdd avec les données du fichier
				this.getFromFiles();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return this.numberFiles();
	}
	
	@Override
	public void done(){
		if(this.isCancelled()){
			try {
				this.db.deleteDatabase(name, Type.STPV);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		firePropertyChange("done", false, true);
	}
	
	/**
	 * Lance les parseurs spécifiques à chaque fichier
	 */
	@Override
	protected void getFromFiles() {

		this.setFile("LIEU");
		this.setProgress(0);
		this.setLieu(path + "/LIEU");
		this.setFile("RADR");
		this.setProgress(1);
		this.setRadr(path + "/RADR");
		this.setProgress(2);
	}

	/**
	 * Parse le fichier RADR
	 * @param path {@link String} Chemin vers le fichier RADR
	 */
	private void setRadr(String path) {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
			while (in.ready()){
				String line = in.readLine();
				if(line.startsWith("RADR 30")){
					this.insertMosaique(line);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}


	private void insertMosaique(String line) throws SQLException {
		PreparedStatement insert = this.conn.prepareStatement("insert into mosaique (type, xcautra, ycautra, carre, nombre) " +
		"values (?, ?, ?, ?, ?)");
		insert.setString(1, line.substring(44, 46));
		insert.setInt(2, new Integer(line.substring(9, 13).trim()) /2);
		insert.setInt(3, new Integer(line.substring(15, 19).trim()) /2);
		insert.setInt(4, new Integer(line.substring(23, 25).trim()) /2);
		insert.setInt(5, new Integer(line.substring(29, 31).trim()));
		insert.executeUpdate();
		insert.close();
	}
	
	/**
	 * Récupère le nom de la BDS
	 */
	private void getName(){
		try {
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(path+"/RESULTAT")));
			Boolean nameFound = false;
			while (in.ready() || !nameFound){
				String line = in.readLine();
				if (line.startsWith("1     STPV - CAUTRA")){
					//on prend le premier mot de la ligne
					this.name = line.substring(29, 38).trim();
					nameFound = true;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Parse le fichier LIEU
	 * @param path Chemin vers le fichier LIEU
	 */
	private void setLieu(String path) {
	}

	@Override
	public int numberFiles() {
		return 2;
	}


}
