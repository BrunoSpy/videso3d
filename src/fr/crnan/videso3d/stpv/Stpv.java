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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.FileManager;
import fr.crnan.videso3d.FileParser;
import fr.crnan.videso3d.DatabaseManager.Type;

/**
 * Lecteur de BDS Stpv
 * @author Bruno Spyckerelle
 * @version 0.1
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
	 */
	public Stpv(String path) {
		super(path);
	}
	
	
	@Override
	public Integer doInBackground() {
		try {
			this.getName();
			//on crée la connection à la db
			this.conn = DatabaseManager.selectDB(Type.STPV, this.name);
			this.conn.setAutoCommit(false);
			//si la base de données n'existe pas
			if(!DatabaseManager.databaseExists(this.name)){
				//puis la structure de la base de donnée
				DatabaseManager.createSTPV(this.name);
				//et on remplit la bdd avec les données du fichier
				this.getFromFiles();
				this.conn.commit();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			this.cancel(true);
		} catch (IOException e) {
			e.printStackTrace();
			this.cancel(true);
		}
		return this.numberFiles();
	}
	
	@Override
	public void done(){
		if(this.isCancelled()){
			try {
				DatabaseManager.deleteDatabase(name, Type.STPV);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			firePropertyChange("done", true, false);
		} else {
			firePropertyChange("done", false, true);
		}
	}
	
	/**
	 * Lance les parseurs spécifiques à chaque fichier
	 * @throws SQLException 
	 * @throws IOException 
	 */
	@Override
	protected void getFromFiles() throws IOException, SQLException {

		this.setFile("LIEU");
		this.setProgress(0);
		this.setLieu(FileManager.getFile(path + "/LIEU"));
		this.setFile("RADR");
		this.setProgress(1);
		this.setRadr(FileManager.getFile(path + "/RADR"));
		this.setFile("SECT");
		this.setProgress(2);
		this.setSect(FileManager.getFile(path + "/SECT"));
		this.setProgress(3);
	}

	/**
	 * Parse le fichier SECT
	 * @param path
	 * @throws SQLException 
	 * @throws IOException 
	 */
	private void setSect(String path) throws IOException, SQLException{
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
		while (in.ready()){
			String line = in.readLine();
			if(line.startsWith("SECT 5") || line.startsWith("SECT 8")){
				this.insertSect(line);
			}
		}
	}
	
	/**
	 * Insertion en base de données d'une ligne SECT 5 ou SECT 8
	 * @param line
	 * @throws SQLException 
	 */
	private void insertSect(String line) throws SQLException{
		PreparedStatement insert = this.conn.prepareStatement("insert into sect (nom, freq) " +
		"values (?, ?)");
		insert.setString(1, line.substring(8, 12).trim());
		if(line.length() > 25) {
			insert.setString(2, line.substring(20, 26).trim());
		} else {
			insert.setString(2, "0");
		}
		insert.executeUpdate();
		insert.close();
	}
	
	/**
	 * Parse le fichier RADR
	 * @param path {@link String} Chemin vers le fichier RADR
	 * @throws SQLException 
	 * @throws IOException 
	 */
	private void setRadr(String path) throws IOException, SQLException {
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
			while (in.ready()){
				String line = in.readLine();
				if(line.startsWith("RADR 30")){
					this.insertMosaique(line);
				}
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
	 * @throws IOException 
	 */
	private void getName() throws IOException{
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
	}
	
	/**
	 * Parse le fichier LIEU<br />
	 * Prend en compte les LIEU 20, 26, 27, 6, 8 et 91
	 * @param path Chemin vers le fichier LIEU
	 * @throws SQLException 
	 * @throws IOException 
	 */
	private void setLieu(String path) throws IOException, SQLException {
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
			} else if (line.startsWith("LIEU 8")) {
				this.insertLieu8(line);
			} else if(line.startsWith("LIEU 91") && !line.startsWith("LIEU 91S")) {
				this.insertLieu91(line);
			} else if(line.startsWith("LIEU 91S")){
				this.addLieu91S(line);
			}
		}
	}

	private void addLieu91S(String line) throws SQLException {
		Statement st = this.conn.createStatement();
		ResultSet rs = st.executeQuery("select max(id) from lieu91"); //le lieu 91 auquel se rapporte ce lieu 91s est forcément le dernier lieu91 enregistré
		int id = rs.getInt(1);
		String terrain1 = line.substring(14, 18).trim();
		String conf1 = line.substring(20, 21).trim();
		String terrain2 = "";
		String conf2 = "";
		if(line.length()>34){
			terrain2 = line.substring(26, 30).trim();
			conf2 = line.substring(32, 33).trim();
		}
		st.executeUpdate("update lieu91 set terrain1 ='"+terrain1+"', " +
				"conf1 = '"+conf1+"', " +
				"terrain2 = '"+terrain2+"', " +
				"conf2 = '"+conf2+"' " +
				"where id='"+id+"'");
	}



	private void insertLieu91(String line) throws SQLException {
		PreparedStatement insert = this.conn.prepareStatement("insert into lieu91 (oaci, indicateur, secteur_donnant, secteur_recevant, bal1, bal2, piste, avion, tfl) " +
				"values (?, ?, ?, ?, ?, ?, ?, ?, ?)");
		insert.setString(1, line.substring(8, 12).trim());
		insert.setString(2, line.substring(14, 17).trim());
		insert.setString(3, line.substring(20, 22).trim());
		insert.setString(4, line.substring(26, 28).trim());
		insert.setString(5, line.substring(32, 37).trim());
		insert.setString(6, line.substring(38, 43).trim());
		insert.setString(7, line.substring(44, 47).trim());
		insert.setString(8, line.substring(50, 55).trim());
		insert.setInt(9, new Integer(line.substring(58, 61).trim()));
		insert.executeUpdate();
	}



	private void insertLieu8(String line) throws SQLException {
		PreparedStatement insert = this.conn.prepareStatement("insert into lieu8 (depart, arrivee, fl) values (?, ?, ?)");
		insert.setString(1, line.substring(8, 12).trim());
		insert.setString(2, line.substring(14, 18).trim());
		insert.setInt(3, new Integer(line.substring(22, 25).trim()));
		insert.executeUpdate();
	}



	private void insertLieu6(String line) throws SQLException {
		PreparedStatement insert = this.conn.prepareStatement("insert into lieu6 (oaci, bal1, xfl1) values (?, ?, ?)");
		insert.setString(1, line.substring(8, 12).trim());
		insert.setString(2, line.substring(14, 20).trim());
		insert.setInt(3, new Integer(line.substring(20, 25).trim()));
		insert.addBatch();
		if(line.trim().length() > 36) {
			insert.setString(2, line.substring(26, 31).trim());
			insert.setInt(3, new Integer(line.substring(34, 37).trim()));
			insert.addBatch();
		}
		if(line.trim().length() > 48){
			insert.setString(2, line.substring(38, 43).trim());
			insert.setInt(3, new Integer(line.substring(46, 49).trim()));
			insert.addBatch();
		}
		if(line.trim().length() > 60){
			insert.setString(2, line.substring(50, 56).trim());
			insert.setInt(3, new Integer(line.substring(58, 61).trim()));
			insert.addBatch();
		}
		if(line.trim().length() > 72){
			insert.setString(2, line.substring(62, 68).trim());
			insert.setInt(3, new Integer(line.substring(70, 73).trim()));
			insert.addBatch();
		}
		insert.executeBatch();
	}



	private void insertLieu27(String line) throws SQLException {
		PreparedStatement insert = this.conn.prepareStatement("insert into lieu27 (oaci, balise, niveau) values (?, ?, ?)");
		insert.setString(1, line.substring(8, 12).trim());
		insert.setString(2, line.substring(14, 19).trim());
		insert.setInt(3, new Integer(line.substring(20, 23).trim()));
		insert.executeUpdate();
	}

	private void insertLieu26(String line) throws SQLException {
		PreparedStatement insert = this.conn.prepareStatement("insert into lieu26 (oaci, balise, niveau) values (?, ?, ?)");
		insert.setString(1, line.substring(8, 12).trim());
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
		return 3;
	}


}
