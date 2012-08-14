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

package fr.crnan.videso3d.databases.stpv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.regex.Pattern;

import fr.crnan.videso3d.databases.DatabaseManager;
import fr.crnan.videso3d.databases.DatabaseManager.Type;
import fr.crnan.videso3d.FileManager;
import fr.crnan.videso3d.FileParser;
import fr.crnan.videso3d.databases.stip.Stip;

/**
 * Lecteur de BDS Stpv
 * @author Bruno Spyckerelle
 * @version 0.1.3
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
	

	public static boolean containsSTPVFiles(TreeSet<File> files) {
		Iterator<File> iterator = files.iterator();
		while (iterator.hasNext()) {
			File file = (File) iterator.next();
			if(file.getName().equalsIgnoreCase("resultat") || file.getName().equalsIgnoreCase("resultat.txt")){
				return true;
			}
		}
		return false;
	}
	
	@Override
	public Integer doInBackground() {
		try {
			this.createName();
			//si la base de données n'existe pas
			if(!DatabaseManager.databaseExists(Type.STPV, this.name)){
				//on crée la connection à la db
				this.conn = DatabaseManager.selectDB(Type.STPV, this.name);
				this.conn.setAutoCommit(false);
				//puis la structure de la base de donnée
				DatabaseManager.createSTPV(this.name, this.path);
				//et on remplit la bdd avec les données du fichier
				this.getFromFiles();
				this.conn.commit();
			} else {
				DatabaseManager.selectDatabase(this.name, Type.STPV);
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
		this.setFile("BALI");
		this.setProgress(3);
		this.setBali(FileManager.getFile(path + "/BALI"));
		this.setProgress(4);
		this.setFile("CODE");
		this.setCode(FileManager.getFile(path+ "/CODE"));
		this.setProgress(5);
		this.setFile("CONF");
		this.setConf(FileManager.getFile(path+ "/CONF"));
		this.setProgress(6);
		this.setFile("COOR");
		this.setCoor(FileManager.getFile(path+ "/COOR"));
		this.setProgress(7);
	}

	/**
	 * Parse le fichier Code 
	 * @param path
	 * @throws IOException 
	 * @throws SQLException 
	 */
	private void setCode(String path) throws IOException, SQLException{
		BufferedReader in = null;
		try{
			in = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
			while (in.ready()){
				String line = in.readLine();
				if(line.startsWith("CODE 1")){
					insertCode1(line);
				} else if(line.startsWith("CODE 30")){
					insertCode30(line);
				}
			}
		}catch(IOException e){
			throw e;
		} finally{
			if(in != null)
				in.close();
		}
	}
	
	private void insertCode30(String line) throws SQLException{
		PreparedStatement st;
		if(line.startsWith("CODE 30S")){
			st = this.conn.prepareStatement("update lps set debut_suite =?, fin_suite=?, cat_code=?, debut_suppl=?, fin_suppl=?, code2=? " +
					"where id=(select max(id) from lps)");
			String s = line.substring(21, 25).trim();
			if(!s.isEmpty()) st.setInt(1, new Integer(s));
			s = line.substring(27, 31).trim();
			if(!s.isEmpty()) st.setInt(2, new Integer(s));
			if(line.length()>34) {
				s = line.substring(32, 37).trim();
				if(!s.isEmpty()) st.setString(3, s);
			}
			if(line.length()>48){
				s = line.substring(39, 43).trim();
				if(!s.isEmpty()) st.setInt(4, new Integer(s));
				s = line.substring(45, 49).trim();
				if(!s.isEmpty()) st.setInt(5, new Integer(s));
			}
			if(line.length()>54){
				s = line.substring(53, 55).trim();
				if(!s.isEmpty()) st.setInt(6, new Integer(s));
			}
			st.executeUpdate();
		} else {
			st = this.conn.prepareStatement("insert into lps (name, cat, depart, sl1, sl2, sl3, sl4, sl5, sl6, arr, modes) " +
					"values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			st.setString(1, line.substring(14, 19).trim());
			st.setString(2, line.substring(9, 12).trim());
			st.setString(3, line.substring(21, 25).trim());
			st.setString(4, line.substring(26, 31).trim());
			st.setString(5, line.substring(32, 37).trim());
			st.setString(6, line.substring(38, 43).trim());
			st.setString(7, line.substring(44, 49).trim());
			st.setString(8, line.substring(50, 55).trim());
			st.setString(9, line.substring(56, 61).trim());
			st.setString(10, line.substring(62, 67).trim());
			if(line.length()>72) st.setBoolean(11, line.substring(68, 73).trim().equals("SOPT1"));
			st.executeUpdate();
		}
		st.close();
	}
	
	private void insertCode1(String line) throws SQLException{
		//si la ligne est trop longue, on enlève les trois derniers caractères qui sont le numéro de la ligne
		String temp = line;
		if(line.length()>=80)
			temp = line.substring(0, line.length()-3);
		String[] words = temp.split("\\s+");
		PreparedStatement st = this.conn.prepareStatement("insert into cat_code (name, code) values (?, ?)");
		st.setString(1, words[2]);
		for(int i=3;i<words.length;i+=2){
			int debut = new Integer(words[i]);
			int fin = new Integer(words[i+1]);
			for(int j=debut;j<= fin;j++){
				st.setInt(2, j);
				st.addBatch();
			}
		}
		st.executeBatch();
		st.close();
	}
	
	/**
	 * Parse le fichier BALI
	 * @param path
	 * @throws SQLException 
	 * @throws IOException 
	 */
	private void setBali(String path) throws IOException, SQLException {
		BufferedReader in = null;
		try{
			in = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
			String currentName = "";
			ArrayList<String> currentLines = new ArrayList<String>();
			while (in.ready()){
				String line = in.readLine();
				String name = (line.length()>12 ? line.substring(8, 13) : line.substring(8));
				if(!name.equals(currentName)){
					insertBali(currentLines, currentName.trim());
					currentLines.clear();
					currentName = name;
				}
				if(line.startsWith("BALI 4") || line.startsWith("BALI 5")){
					currentLines.add(line);
				}
			}
			insertBali(currentLines, currentName.trim());
		} catch(IOException e){
			throw e;
		} finally{
			if(in != null)
				in.close();
		}
	}

	/**
	 * Insertion en base de données d'une ligne BALI 4, ou BALI50 à BALI59
	 * @param line
	 * @throws SQLException 
	 */
	private void insertBali(ArrayList<String> lines, String name) throws SQLException{
		PreparedStatement insert = this.conn.prepareStatement("insert into bali (name, TMA, " +
				"sect1, sect2, sect3, sect4, sect5, sect6, sect7, sect8, sect9) " +
				"values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
		PreparedStatement insertSLCT = this.conn.prepareStatement("insert into imprSLCT (nom_balise, nom_SLCT) values (?,?)");
		
		for(int i = 1; i<=11; i++){
			insert.setInt(i, -1);
		}
		insert.setInt(2, 1);
		insert.setString(1, name);
		for(String line : lines){
			if(line.startsWith("BALI 4")){
				insertSLCT.setString(1, name);
				insertSLCT.setString(2, line.substring(20, 23));
				insertSLCT.executeUpdate();
			}else if(line.startsWith("BALI 50")){
				insert.setInt(2, 0);
			}else if(line.startsWith("BALI 5")){
				int index = Integer.parseInt(line.substring(6,7));
				if(line.contains(" NP "))
					insert.setInt(index+2, 0);
				else
					insert.setInt(index+2, 1);
			}
		}
		insert.executeUpdate();
		insert.close();
		insertSLCT.close();
	}
	

	/**
	 * Parse le fichier SECT
	 * @param path
	 * @throws SQLException 
	 * @throws IOException 
	 */
	private void setSect(String path) throws IOException, SQLException{
		BufferedReader in = null;
		try{
			in =new BufferedReader(new InputStreamReader(new FileInputStream(path)));
			while (in.ready()){
				String line = in.readLine();
				if(line.startsWith("SECT 5") || line.startsWith("SECT 8")){
					this.insertSect(line);
				}
			}
		} catch(IOException e){
			throw e;
		} finally {
			if(in != null)
				in.close();
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
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
			while (in.ready()){
				String line = in.readLine();
				if(line.startsWith("RADR 30")){
					this.insertMosaique(line);
				}
			}
		} catch(IOException e){
			throw e;
		} finally {
			if (in!= null)
				in.close();
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
	private void createName() throws IOException{
		BufferedReader in = null;
		Boolean nameFound = false;
		try {
			in = new BufferedReader(new InputStreamReader(new FileInputStream(FileManager.getFile(path+"/RESULTAT"))));
			Pattern pattern = Pattern.compile(".*STPV - CAUTRA IV - CA:.*");
			while (in.ready() && !nameFound){
				String line = in.readLine();
				if (pattern.matcher(line).matches()){
					int pos = line.indexOf(":");
					this.name = "STPV_"+line.substring(pos+1, pos+10).trim()+"."+line.substring(pos+51, pos+60).trim();
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
			throw new IOException("Format of the file RESULTAT unknown");
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
		BufferedReader in = null;
		try{
			in = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
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
				} else if(line.startsWith("LIEU 90") && !line.startsWith("LIEU 901")){
					this.insertLieu90(line);
				} else if(line.startsWith("LIEU 901")){
					this.insertLieu901(line);
				} else if(line.startsWith("LIEU 91") && !line.startsWith("LIEU 91S")) {
					this.insertLieu91(line);
				} else if(line.startsWith("LIEU 91S")){
					this.addLieu91S(line);
				}
			}
		} catch(IOException e){
			throw e;
		} finally {
			if(in != null)
				in.close();
		}
	}

	private void insertLieu901(String line) throws SQLException {
		Statement st = this.conn.createStatement();
		ResultSet rs = st.executeQuery("select max(id) from lieu90"); //le lieu 901 auquel se rapporte ce lieu 91s est forcément le dernier lieu90 enregistré
		int id = rs.getInt(1);
		String conf = line.substring(20, 25).trim();
		String name = line.substring(26).trim();
		st.executeUpdate("insert into lieu901 (lieu90, conf, name) " +
		"values ('"+id+"', '"+conf+"', '"+name+"')");
		st.close();
	}



	private void insertLieu90(String line) throws SQLException {
		PreparedStatement insert = this.conn.prepareStatement("insert into lieu90 (oaci, balini, bal1, bal2, bal3, bal4, bal5, bal6, bal7, bal8, hel, jet, fir, uir) " +
				"values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
		insert.setString(1, line.substring(8, 12).trim());
		insert.setString(2, line.substring(14, 19).trim());
		insert.setString(3, line.substring(20, 25).trim());
		insert.setString(4, line.substring(26, 31).trim());
		insert.setString(5, line.substring(32, 37).trim());
		insert.setString(6, line.substring(38, 43).trim());
		insert.setString(7, line.substring(44, 49).trim());
		insert.setString(8, line.substring(50, 55).trim());
		insert.setString(9, line.substring(56, 61).trim());
		insert.setString(10, line.substring(62, 67).trim());
		insert.setBoolean(11, line.substring(68, 69).equals("O"));
		insert.setBoolean(12, line.substring(69, 70).equals("O"));
		insert.setBoolean(13, line.substring(70, 71).equals("O"));
		insert.setBoolean(14, line.substring(71, 72).equals("O"));
		insert.executeUpdate();

	}



	private void addLieu91S(String line) throws SQLException {
		Statement st = this.conn.createStatement();
		ResultSet rs = st.executeQuery("select max(id) from lieu91"); //le lieu 91 auquel se rapporte ce lieu 91s est forcément le dernier lieu91 enregistré
		int id = rs.getInt(1);
		String terrain1 = line.substring(14, 18).trim();
		String conf1 = line.substring(20, 21).trim();
		String terrain2 = "";
		String conf2 = "";
		try{
		if(line.length()>31){
			terrain2 = line.substring(26, 30).trim();
			conf2 = line.substring(32).trim();
		}
		}catch(Exception e){
			e.printStackTrace();
		}
		st.executeUpdate("update lieu91 set terrain1 ='"+terrain1+"', " +
				"conf1 = '"+conf1+"', " +
				"terrain2 = '"+terrain2+"', " +
				"conf2 = '"+conf2+"' " +
				"where id='"+id+"'");
	}



	private void insertLieu91(String line) throws SQLException {
		PreparedStatement insert = this.conn.prepareStatement("insert into lieu91 (oaci, indicateur, secteur_donnant, secteur_recevant, bal1, bal2, " +
				"piste, avion, tfl) values (?, ?, ?, ?, ?, ?, ?, ?, ?)");
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
			Integer xfl = null;
			try{
				xfl = new Integer(line.substring(34, 37).trim());
			} catch(NumberFormatException e){
				//do nothing
			}
			if(xfl != null){//line can be long enough be with just spaces
				insert.setInt(3, xfl);
				insert.addBatch();
			}
		}
		if(line.trim().length() > 48){
			insert.setString(2, line.substring(38, 43).trim());
			Integer xfl = null;
			try{
				xfl = new Integer(line.substring(46, 49).trim());
			} catch(NumberFormatException e){
				//do nothing
			}
			if(xfl != null){//line can be long enough be with just spaces
				insert.setInt(3, xfl);
				insert.addBatch();
			}
		}
		if(line.trim().length() > 60){
			insert.setString(2, line.substring(50, 56).trim());
			Integer xfl = null;
			try{
				xfl = new Integer(line.substring(58, 61).trim());
			} catch(NumberFormatException e){
				//do nothing
			}
			if(xfl != null){//line can be long enough be with just spaces
				insert.setInt(3, new Integer(line.substring(58, 61).trim()));
				insert.addBatch();
			}
		}
		if(line.trim().length() > 72){
			insert.setString(2, line.substring(62, 68).trim());
			Integer xfl = null;
			try{
				xfl = new Integer(line.substring(70, 73).trim());
			} catch(NumberFormatException e){
				//do nothing
			}
			if(xfl != null){//line can be long enough be with just spaces
				insert.setInt(3, xfl);
				insert.addBatch();
			}
		}
		insert.executeBatch();
	}



	private void insertLieu27(String line) throws SQLException {
		PreparedStatement insert = this.conn.prepareStatement("insert into lieu27 (oaci, balise, niveau, rerfl) values (?, ?, ?, ?)");
		insert.setString(1, line.substring(8, 12).trim());
		insert.setString(2, line.substring(14, 19).trim());
		insert.setInt(3, new Integer(line.substring(20, 23).trim()));
		try{
			if(line.length()>30){
				String rerfl = line.length()==31?line.substring(26):line.substring(26, 31);
				insert.setString(4, rerfl.equals("RERFL")?"oui":"non");
			}
			else 
				insert.setString(4, "non");
		}catch(Exception e){
			e.printStackTrace();
		}
		insert.executeUpdate();
	}

	private void insertLieu26(String line) throws SQLException {
		PreparedStatement insert = this.conn.prepareStatement("insert into lieu26 (oaci, balise, niveau, actau, rerfl) values (?, ?, ?, ?, ?)");
		insert.setString(1, line.substring(8, 12).trim());
		insert.setString(2, line.substring(14, 19).trim());
		insert.setInt(3, new Integer(line.substring(20, 23).trim()));

		try{
			if(line.length()>30){
				String actau = line.length()==31?line.substring(26):line.substring(26, 31);
				insert.setString(4, actau.equals("ACTAU")?"oui":"non");
				}
			else 
				insert.setString(4, "non");
			if(line.length()>36){
				String rerfl = line.length()==37?line.substring(32):line.substring(32, 37);
				insert.setString(5, rerfl.equals("RERFL")?"oui":"non");
			}
			else
				insert.setString(5, "non");
		}catch(Exception e){
			e.printStackTrace();
		}
		insert.executeUpdate();
	}



	private void insertLieu20(String line) throws SQLException {
		PreparedStatement insert = this.conn.prepareStatement("insert into lieu20 (oaci) values (?)");
		insert.setString(1, line.substring(8, 12));
		insert.executeUpdate();
	}
	

	/**
	 * 
	 * @param path
	 * @throws IOException
	 * @throws SQLException
	 */
	private void setConf(String path) throws IOException, SQLException{
		BufferedReader in = null;
		try{
			in = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
			while (in.ready()){
				String line = in.readLine();
				if(line.startsWith("CONF 12D")){
					insertConf12D(line);
				}
			}
		} catch(IOException e){
			throw e;
		} finally{
			if (in != null)
				in.close();
		}
	}

	private void insertConf12D(String line_) {
		String line = line_;
		//si la ligne est trop longue, on enlève le numéro de ligne en trop
		if(line.length() >= 80)
			line = line.substring(0, line.length()-3).trim();
		try{
			PreparedStatement insert = this.conn.prepareStatement("insert into double (entite, destinataire, identifiant, strip, bal1, bal2, " +
					"flinf1, flsup1, flinf2, flsup2) values (?,?,?,?,?,?,?,?,?,?)");
			insert.setString(1, line.substring(14, 16));
			insert.setString(2, line.substring(26, 28));
			insert.setString(3, line.substring(20, 22));
			insert.setString(4, line.substring(9, 13).equals("INHI") ? "Non" : "Oui");
			if(line.length()>28){
				if(line.length()<45){
					insert.setString(5, line.substring(38));
					for(int i = 6; i<11; i++){
						insert.setString(i, "");
					}
				}else{
					insert.setString(5, line.substring(38, 43));
					if(line.length()<55){
						insert.setString(6, line.substring(44));
						for(int i = 7; i<11; i++){
							insert.setString(i, "");
						}
					}else{
						insert.setString(6, line.substring(44,49));
						insert.setString(7, line.substring(52, 55));
						if(line.length()>=61)
							insert.setString(8, line.substring(58, 61));
						else
							insert.setString(8, "");
						insert.setString(9, "");
						insert.setString(10, "");
						if(line.length()>=72){
							insert.setString(9, line.substring(64, 67));
							insert.setString(10, line.substring(70, 73));
						}
					}
				}
			}
			insert.executeUpdate();
		} catch(Exception e){
			System.out.println(line_+"/"+line_.length());
			System.out.println(line);
			e.printStackTrace();
		}
	}
	
	private void setCoor(String path) throws IOException, SQLException {
		BufferedReader in = null;
		try{
			in = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
			while (in.ready()){
				String line = in.readLine();
				if(line.startsWith("COOR 30") || line.startsWith("COOR 40")){
					insertCoor(line);
				}
			}
		} catch(IOException e){
			throw e;
		} finally {
			if(in != null)
				in.close();
		}
	}
	
	private void insertCoor(String line) throws SQLException {
		try{
			PreparedStatement insert = null;
			if(line.startsWith("COOR 30")){
				insert = this.conn.prepareStatement("insert into coor30 (donnant, recevant, val1, val2, val3, val4) values (?,?,?,?,?,?)");
				insert.setString(1, line.substring(20, 22));
				if(line.length()>=28)
					insert.setString(2, line.substring(26, 28));
				else
					insert.setString(2, line.substring(26, 27));
			}else if(line.startsWith("COOR 40")){
				insert = this.conn.prepareStatement("insert into coor40 (donnant, recevant, val1, val2, val3, val4, bal1, bal2, balref) values (?,?,?,?,?,?,?,?,?)");
				insert.setString(1, line.substring(8, 10));
				insert.setString(2, line.substring(14, 16));
				insert.setString(7, line.substring(20, 25));
				insert.setString(8, line.substring(26, 31));
				if(line.length()>=37)
					insert.setString(9, line.substring(32, 37));
				else{
					insert.setString(9, line.substring(32, line.length()));
				}
			}
			if(line.length()>=43)
				insert.setString(3, line.substring(39, 43));
			else
				insert.setString(3, "");
			if(line.length()>=49)
				insert.setString(4, line.substring(45, 49));
			else
				insert.setString(4, "");
			if(line.length()>=67)
				insert.setString(5, line.substring(63, 67));
			else
				insert.setString(5, "");
			if(line.length()>=73)
				insert.setString(6, line.substring(69));
			else
				insert.setString(6, "");
			insert.executeUpdate();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@Override
	public int numberFiles() {
		return 7;
	}

	/**
	 * 
	 * @param id
	 * @return
	 */
	private static String starToString(int id){
		String star = new String();
		try {
			Statement st = DatabaseManager.getCurrentStpv();
			ResultSet rs = st.executeQuery("select * from lieu90 where id ='"+id+"'");
			if(rs.next()){
				star += "LIEU 90 ";
				String oaci = rs.getString("oaci");
				star += oaci+"  ";
				String balini = rs.getString("balini");
				star += balini;
				for(int i = 0;i<6-balini.length();i++){
					star += " ";
				}
				for(int i=1;i<=8;i++){
					if(rs.getString("bal"+i) != null){
						star += rs.getString("bal"+i);
						for(int j = 0;j<6-rs.getString("bal"+i).length();j++){
							star += " ";
						}
					}
				}
				for(int i=0;i<68-star.length();i++){
					star += " ";
				}
				star += rs.getBoolean("hel") ? "O" : "N";
				star += rs.getBoolean("jet") ? "O" : "N";
				star += rs.getBoolean("fir") ? "O" : "N";
				star += rs.getBoolean("uir") ? "O" : "N";
				star += "\n";
				rs = st.executeQuery("select * from lieu901 where lieu90 = '"+id+"'");
				while(rs.next()){
					star += "LIEU 901"+oaci+"  "+balini;
					for(int i = 0;i<6-balini.length();i++){
						star += " ";
					}
					star += rs.getString("conf");
					for(int i = 0;i<6-rs.getString("conf").length();i++){
						star += " ";
					}
					star += rs.getString("name")+"\n";
				}
			}
			rs.close();
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return star;
	}
	
	/**
	 * 
	 * @param id
	 * @return
	 */
	private static String tflToString(int id){
		String tfl = new String();
		try {
			Statement st = DatabaseManager.getCurrentStpv();
			ResultSet rs = st.executeQuery("select * from lieu91 where id ='"+id+"'");
			if(rs.next()){
				tfl += "LIEU 91 ";
				String terrain = rs.getString(2);
				for(int i=0; i<4-terrain.length(); i++){
					terrain += " ";
				}
				tfl += terrain+"  "+rs.getString(3)+"   ";
				String sect1 = rs.getString(4);
				String sect2 = rs.getString(5);
				if(sect1.length()==1)
					sect1 += " ";
				if(sect2.length()==1)
					sect2 += " ";
				tfl += sect1+"    "+sect2+"    "+Stip.completerBalise(rs.getString(6))+" "+Stip.completerBalise(rs.getString(7))+" ";
				String piste = rs.getString(8);
				if(piste.length()==1)
					piste += "  ";
				if(piste.length()==0)
					piste += "   ";
				tfl += piste+"   "+rs.getString(9)+"     "+Stip.completerNiveau(rs.getString(10));
				String terrain1 = rs.getString(11);
				String terrain2 = rs.getString(13);
				if(terrain1 != null){
					tfl += "\nLIEU 91S"+terrain+"  "+terrain1+"  "+rs.getString(12);
					if(terrain2 != null){
						tfl += "     "+terrain2+"  "+rs.getString(14);
					}
				}				
			}
			rs.close();
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return tfl;
	}
	
	/**
	 * 
	 * @param id
	 * @return
	 */
	private static String citypairToString(int id){
		String citypair = new String();
		try {
			Statement st = DatabaseManager.getCurrentStpv();
			ResultSet rs = st.executeQuery("select depart, arrivee, fl from lieu8 where id ='"+id+"'");
			if(rs.next()){
				citypair +="LIEU 8  "+rs.getString(1)+"  "+rs.getString(2)+"    "+Stip.completerNiveau(rs.getString(3));
			}
			rs.close();
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return citypair;
	}
	/**
	 * 
	 * @param id
	 * @return
	 */
	private static String xflToString(int id){
		String xfl = new String();
		try {
			Statement st = DatabaseManager.getCurrentStpv();
			ResultSet rs = st.executeQuery("select oaci, bal1, xfl1 from lieu6 where oaci = (select oaci from lieu6 where id='"+id+"') order by id");
			if(rs.next()){
				xfl += "LIEU 6  "+Stip.completerBalise(rs.getString(1))+" "+Stip.completerBalise(rs.getString(2))+"   "+Stip.completerNiveau(rs.getString(3));
			}
			while(rs.next()){
				xfl +=" "+Stip.completerBalise(rs.getString(2))+"   "+Stip.completerNiveau(rs.getString(3));
			}
			rs.close();
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return xfl;
	}
	/**
	 * 
	 * @param id
	 * @return
	 */
	private static String nivTabEntreeToString(int id){
		String nivTab = new String();
		try {
			Statement st = DatabaseManager.getCurrentStpv();
			ResultSet rs = st.executeQuery("select * from lieu26 where oaci = (select oaci from lieu26 where id='"+id+"') order by id");
			//un compteur pour savoir si c'est le premier lieu26 pour ce terrain ou non, afin de déterminer si c'est un LIEU 26 ou LIEU 26B.
			int i=0;
			while(rs.next()){
				if(rs.getInt(1)==id){
					nivTab += "LIEU 26"+(i!=0?"B":" ")+rs.getString(2)+"  "+Stip.completerBalise(rs.getString(3))+" "+Stip.completerNiveau(rs.getString(4));
					String actauto = rs.getString(5);
					String rerfl = rs.getString(6);
					if(rerfl.equals("oui")){
						if(actauto.equals("oui"))
							nivTab += "   ACTAU RERFL";
						else
							nivTab += "         RERFL";
					}else{
						if(actauto.equals("oui"))
							nivTab += "   ACTAU";
					}
					break;
				}
				i++;
			}
		
			rs.close();
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return nivTab;
	}
	/**
	 * 
	 * @param id
	 * @return
	 */
	private static String nivTabSortieToString(int id){
		String nivTab = new String();
		try {
			Statement st = DatabaseManager.getCurrentStpv();
			ResultSet rs = st.executeQuery("select * from lieu27 where oaci = (select oaci from lieu27 where id='"+id+"') order by id");
			//un compteur pour savoir si c'est le premier lieu27 pour ce terrain ou non, afin de déterminer si c'est un LIEU 26 ou LIEU 26B.
			int i=0;
			while(rs.next()){
				if(rs.getInt(1)==id){
					nivTab += "LIEU 27"+(i!=0?"B":" ")+rs.getString(2)+"  "+Stip.completerBalise(rs.getString(3))+" "
							+Stip.completerNiveau(rs.getString(4))+(rs.getString(5).equals("oui")?"   RERFL":"");
					break;
				}
				i++;
			}
		
			rs.close();
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return nivTab;
	}
	

	/**
	 * 
	 * @param type from {@link StipController}
	 * @param id
	 * @return
	 */
	public static String getString(int type, int id){
		switch (type) {
		case StpvController.STAR:
			return starToString(id);
		case StpvController.TFL:
			return tflToString(id);
		case StpvController.CITYPAIR:
			return citypairToString(id);
		case StpvController.XFL:
			return xflToString(id);
		case StpvController.NIV_TAB_ENTREE:
			return nivTabEntreeToString(id);
		case StpvController.NIV_TAB_SORTIE:
			return nivTabSortieToString(id);
		default:
			return null;
		}
	}



	



	@Override
	public String getName() {
		return this.name;
	}



	@Override
	public Type getType() {
		return Type.STPV;
	}


}
