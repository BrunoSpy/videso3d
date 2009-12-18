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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

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
			this.conn.setAutoCommit(false);
			//si la base de données n'existe pas
			if(!this.db.databaseExists(this.name)){
				//puis la structure de la base de donnée
				this.db.createSTPV(this.name);
				//et on remplit la bdd avec les données du fichier
				this.getFromFiles();
				this.conn.commit();
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
	 * Parse le fichier LIEU<br />
	 * Prend en compte les LIEU 20, 26, 27, 6, 8 et 91
	 * @param path Chemin vers le fichier LIEU
	 */
	private void setLieu(String path) {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
			while(in.ready()){
				String line = in.readLine();
				if(line.startsWith("LIEU 20")){
					this.insertLieu20(line);
				} else if(line.startsWith("LIEU 26") || line.startsWith("LIEU 26B")){
					this.insertLieu26(line);
				}  else if(line.startsWith("LIEU 27") || line.startsWith("LIEU 27B")){
					this.insertLieu27(line);
				} else if(line.startsWith("LIEU 6")) {
					this.insertLieu6(line);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} 
	}

	private void insertLieu6(String line) throws SQLException {
		PreparedStatement insert = this.conn.prepareStatement("insert into lieu6 (oaci, bal1, xfl1, bal2, xfl2, bal3, xfl3, bal4, xfl4, bal5, xfl5) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
		insert.setString(1, line.substring(8, 12));
		insert.setString(2, line.substring(14, 20));
		insert.setInt(3, new Integer(line.substring(20, 25).trim()));
		if(line.trim().length() > 36) {
			insert.setString(4, line.substring(26, 31));
			insert.setInt(5, new Integer(line.substring(34, 37).trim()));
		}
		if(line.trim().length() > 48){
			insert.setString(6, line.substring(38, 43));
			insert.setInt(7, new Integer(line.substring(46, 49).trim()));
		}
		if(line.trim().length() > 60){
			insert.setString(8, line.substring(50, 56));
			insert.setInt(9, new Integer(line.substring(58, 61).trim()));
		}
		if(line.trim().length() > 72){
			insert.setString(10, line.substring(62, 68));
			insert.setInt(11, new Integer(line.substring(70, 73).trim()));
		}
		insert.executeUpdate();
	}



	private void insertLieu27(String line) throws SQLException {
		PreparedStatement insert = this.conn.prepareStatement("insert into lieu27 (oaci, balise, niveau) values (?, ?, ?)");
		insert.setString(1, line.substring(8, 12));
		insert.setString(2, line.substring(14, 19).trim());
		insert.setInt(3, new Integer(line.substring(20, 23).trim()));
		insert.executeUpdate();
	}

	private void insertLieu26(String line) throws SQLException {
		PreparedStatement insert = this.conn.prepareStatement("insert into lieu26 (oaci, balise, niveau) values (?, ?, ?)");
		insert.setString(1, line.substring(8, 12));
		insert.setString(2, line.substring(14, 19).trim());
		insert.setInt(3, new Integer(line.substring(20, 23).trim()));
		insert.executeUpdate();
	}



	private void insertLieu20(String line) throws SQLException {
		PreparedStatement insert = this.conn.prepareStatement("insert into lieu20 (oaci) values (?)");
		insert.setString(1, line.substring(8, 12));
		insert.executeUpdate();
	}



	@Override
	public int numberFiles() {
		return 2;
	}


}
