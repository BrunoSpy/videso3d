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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.sql.*;


/**
 * Gère la base de données
 * @author Bruno Spyckerelle
 * @version 0.5
 */
public final class DatabaseManager{

	private static DatabaseManager instance = new DatabaseManager();
	
	/**
	 * Types de base de données possibles
	 */
	public static enum Type {PAYS, STIP, STPV, Edimap, EXSA, Ods, Databases};
	/**
	 * Base des frontières Pays
	 */
	private Connection currentPays;
	/**
	 * Base Stip sélectionnée
	 */
	private Connection currentStip;
	/**
	 * Base Exsa sélectionnée
	 */
	private Connection currentExsa;
	/**
	 * Base Stpv sélectionnée
	 */
	private Connection currentStpv;
	/**
	 * Base Edimap sélectionnée
	 */
	private Connection currentEdimap;
	/**
	 * Base ODS sélectionnée
	 */
	private Connection currentODS;
	/**
	 * Connection par défaut
	 */
	private Connection databases;
	
	/**
	 * Crée une connection avec la base de données par défaut
	 * @throws DatabaseError 
	 */
	private DatabaseManager(){ //singleton
		//connection par défaut
		Statement st = null;
		try {
			//Chargement du driver
			Class.forName("org.sqlite.JDBC");
			//Connexion
			databases = DriverManager.getConnection("jdbc:sqlite:databases");
			//Création de la structure si besoin
			st = databases.createStatement();
			//Méthode comme une autre pour vérifier que la structure existe ... lance une exception si ce n'est pas le cas
			String query = "select * from databases where 1";
			st.executeQuery(query);
		}
		catch (SQLException e){
			try {
				String create = "create table databases (id integer primary key autoincrement, " +
				"name varchar(20), " +
				"type varchar(20), " +
				"date varchar(12), " +
				"selected boolean)";
				st.executeUpdate(create);
				create = "create table clefs (id integer primary key autoincrement," +
						"name varchar(32), " +
						"type varchar(16), " +
						"value varchar(64))";
				st.executeUpdate(create);
			} catch (SQLException e2) {
				e2.printStackTrace();
			}
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
		} 
	}

	public DatabaseManager getInstance(){
		return instance;
	}
	
	/**
	 * Sélectionne une base de données
	 * @param name Nom de la base de données à sélectionner
	 * @return {@link Connection} Connection vers la base sélectionnée
	 * @throws SQLException 
	 */
	public static Connection selectDB(Type type, String name) throws SQLException{
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		switch (type) {
		case STIP:
			if(instance.currentStip == null) {
				instance.currentStip = DriverManager.getConnection("jdbc:sqlite:"+name);
			} else {
				if(!instance.currentStip.getMetaData().getURL().equals("jdbc:sqlite:"+name)){
					//changement de base de données
					instance.currentStip.close();
					instance.currentStip = DriverManager.getConnection("jdbc:sqlite:"+name);
				}
			}
			return instance.currentStip;
		case STPV:
			if(instance.currentStpv == null) {
				instance.currentStpv = DriverManager.getConnection("jdbc:sqlite:"+name);
			} else {
				if(!instance.currentStpv.getMetaData().getURL().equals("jdbc:sqlite:"+name)){
					//changement de base de données
					instance.currentStpv.close();
					instance.currentStpv = DriverManager.getConnection("jdbc:sqlite:"+name);
				}
			}
			return instance.currentStpv;
		case EXSA:
			if(instance.currentExsa == null) {
				instance.currentExsa = DriverManager.getConnection("jdbc:sqlite:"+name);
			} else {
				if(!instance.currentExsa.getMetaData().getURL().equals("jdbc:sqlite:"+name)){
					//changement de base de données
					instance.currentExsa.close();
					instance.currentExsa = DriverManager.getConnection("jdbc:sqlite:"+name);
				}
			}
			return instance.currentExsa;
		case Ods:
			if(instance.currentODS == null) {
				instance.currentODS = DriverManager.getConnection("jdbc:sqlite:"+name);
			} else {
				if(!instance.currentODS.getMetaData().getURL().equals("jdbc:sqlite:"+name)){
					//changement de base de données
					instance.currentODS.close();
					instance.currentODS = DriverManager.getConnection("jdbc:sqlite:"+name);
				}
			}
			return instance.currentODS;
		case Edimap:
			if(instance.currentEdimap == null) {
				instance.currentEdimap = DriverManager.getConnection("jdbc:sqlite:"+name);
			} else {
				if(!instance.currentEdimap.getMetaData().getURL().equals("jdbc:sqlite:"+name)){
					//changement de base de données
					instance.currentEdimap.close();
					instance.currentEdimap = DriverManager.getConnection("jdbc:sqlite:"+name);
				}
			}
			return instance.currentEdimap;
		case PAYS:
			if(instance.currentPays == null) {
				instance.currentPays = DriverManager.getConnection("jdbc:sqlite:"+name);
			} else {
				if(!instance.currentPays.getMetaData().getURL().equals("jdbc:sqlite:"+name)){
					//changement de base de données
					instance.currentPays.close();
					instance.currentPays = DriverManager.getConnection("jdbc:sqlite:"+name);
				}
			}
			return instance.currentPays;
		case Databases:
			if(instance.databases == null) {
				instance.databases = DriverManager.getConnection("jdbc:sqlite:"+name);
			} else {
				if(!instance.databases.getMetaData().getURL().equals("jdbc:sqlite:"+name)){
					//changement de base de données
					instance.databases.close();
					instance.databases = DriverManager.getConnection("jdbc:sqlite:"+name);
				}
			}
			return instance.databases;
		default:
			return null;
		}
	}

	/**
	 * Vérifie si une base de données existe déjà
	 * @param name Nom de la base de données
	 * @return Boolean Vrai si la base de données existe
	 * @throws SQLException 
	 */
	public static Boolean databaseExists(String name) throws SQLException{
		Boolean exists = false;
		Statement st = DatabaseManager.selectDB(Type.Databases, "databases").createStatement();
		ResultSet result = st.executeQuery("SELECT * FROM databases WHERE name = '"+name+"'");
		if (result.next()) exists = true;
		st.close();
		return exists;
	}

	/**
	 * Teste si la base de données <code>id</code> est sélectionnée
	 * @param id de la base de données
	 * @return Boolean True si la base est sélectionnée
	 */
	public static Boolean isSelected(Integer id){
		try {
			Statement st = DatabaseManager.selectDB(Type.Databases, "databases").createStatement();
			ResultSet rs = st.executeQuery("select * from databases where id='"+id+"' and selected = '1'");
			if (rs.next()) {
				return true;
			} else {
				return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Ajoute une base de données dans la liste et la marque comme sélectionnée
	 * @param name Nom de la base de données
	 * @param type Type de la base de données
	 * @param date Date associée
	 */
	private static void addDatabase(String name, Type type, String date){
		try {
			Statement st = DatabaseManager.selectDB(Type.Databases, "databases").createStatement();
			st.executeUpdate("UPDATE databases SET selected = 0 WHERE selected = 1 and type = '"+type.toString()+"'");
			st.close();
			String insert = "insert into databases (name, type, date, selected) values (?, ?, ?, ?)";
			PreparedStatement insertDatabase = DatabaseManager.selectDB(Type.Databases, "databases").prepareStatement(insert);
			insertDatabase.setString(1, name);
			insertDatabase.setString(2, type.toString());
			insertDatabase.setString(3, date);
			insertDatabase.setBoolean(4, true);
			insertDatabase.executeUpdate();
			insertDatabase.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}	
	}

	/**
	 * Crée la structure des tables relatives aux données EXSA
	 * @param name Nom de la base de données recevant les tables
	 * @throws SQLException 
	 */
	public static void createEXSA(String name) throws SQLException{
		Statement st = DatabaseManager.selectDB(Type.EXSA, name).createStatement();
		st.executeUpdate("create table caragener (id integer primary key autoincrement," +
				"name varchar(20), " +
				"date varchar(30), " +
				"jeu varchar(10), " +
				"type varchar(10), " +
				"oasis float, " +
				"boa int, " +
				"videomap varchar(20), " +
				"edimap varchar(20), " +
				"satin varchar(20), " +
				"calcu varchar(10), " +
		"contexte varchar(10))");
		st.executeUpdate("create table centcentr (id integer primary key autoincrement," +
				"name varchar(10), " +
				"sl int, " +
				"type varchar(10), " +
				"str int, " +
				"plafondmsaw int, " +
				"rvsm varchar(10), " +
				"plancherrvsm int, " +
				"plafondrvsm int, " +
				"typedonnees varchar(10), " +
		"versiondonnees varchar(20))");
		st.executeUpdate("create table centmosai (id integer primary key autoincrement," +
				"latitude float, " +
				"longitude float, " +
				"xcautra float, " +
				"ycautra float, " +
				"lignes int, " +
				"colonnes int, " +
		"type varchar(8))");
		st.executeUpdate("create table ficaafniv (id integer primary key autoincrement, " +
				"abonne varchar(16), " +
				"carre int, " +
				"plancher int, " +
				"plafond int, " +
				"elimine boolean, " +
				"firstcode int, " +
		"lastcode int)");
		st.executeUpdate("create table ficaafnic (id integer primary key autoincrement, " +
				"abonne varchar(16), " +
				"carre int, " +
				"plancher int, " +
				"plafond int, " +
				"firstcode int, " +
		"lastcode int)");
		st.executeUpdate("create table centzocc (id integer primary key autoincrement, " +
				"name varchar(1), " +
				"espace varchar(1), " +
		"terrains varchar(64))");
		st.executeUpdate("create table centsczoc (id integer primary key autoincrement, " +
				"carre int, " +
				"souscarre int, " +
				"zone varchar(1), " +
		"plafond int)");
		st.executeUpdate("create table centflvvf (id integer primary key autoincrement, " +
		"name)");
		st.executeUpdate("create table centscvvf (id integer primary key autoincrement, " +
				"carre int, " +
				"souscarre int, " +
				"vvfs varchar(16), " +
				"plafonds varchar(32), " +
		"planchers varchar(32))");
		st.executeUpdate("create table radrgener (id integer primary key autoincrement, " +
				"name varchar(16), " +
				"numero int, " +
				"type varchar(16), " +
				"nommosaique varchar(1), " +
				"latitude float, " +
				"longitude float, " +
				"xcautra float, " +
				"ycautra float, " +
				"ecart float, " +
				"radarrelation varchar(16), " +
				"typerelation varchar(16), " +
				"typeplots varchar(16), " +
				"typeradar varchar(16), " +
				"codepays int, " +
				"coderadar int, " +
		"militaire boolean)");
		st.executeUpdate("create table radrtechn (id integer primary key autoincrement, " +
				"name varchar(16), " +
				"vitesse float, " +
				"hauteur float, " +
				"portee int, " +
				"deport boolean)");
		//on ajoute le nom de la base
		DatabaseManager.addDatabase(name, Type.EXSA, new SimpleDateFormat().format(new Date()));
		st.close();
	}
	/**
	 * Crée la structure des tables d'une base STPV
	 * @param name Nom de la base recevant les tables
	 * @throws SQLException 
	 */
	public static void createSTPV(String name) throws SQLException {
		Statement st = DatabaseManager.selectDB(Type.STPV, name).createStatement();
		st.executeUpdate("create table mosaique (id integer primary key autoincrement, " +
				"type varchar(2), " +
				"xcautra int, " +
				"ycautra int, " +
				"carre int, " +
		"nombre int)");
		st.executeUpdate("create table lieu20 (id integer primary key autoincrement, " +
				"oaci varchar(4))");
		st.executeUpdate("create table lieu26 (id integer primary key autoincrement, " +
				"oaci varchar(4), " +
				"balise varchar(5), " +
				"niveau int)");
		st.executeUpdate("create table lieu27 (id integer primary key autoincrement, " +
				"oaci varchar(4), " +
				"balise varchar(5), " +
				"niveau int)");
		st.executeUpdate("create table lieu6 (id integer primary key autoincrement, " +
				"oaci varchar(4), " +
				"bal1 varchar(5), " +
				"xfl1 int, " +
				"bal2 varchar(5), " +
				"xfl2 int, " +
				"bal3 varchar(5), " +
				"xfl3 int, " +
				"bal4 varchar(5), " +
				"xfl4 int, " +
				"bal5 varchar(5), " +
				"xfl5 int)");
		DatabaseManager.addDatabase(name, Type.STPV, new SimpleDateFormat().format(new Date()));
		st.close();
	}

	/**
	 * Crée la structure des tables d'une base STPV
	 * @param name Nom de la base recevant les tables
	 * @throws SQLException 
	 */
	public static void createODS(String name) throws SQLException {
		Statement st = DatabaseManager.selectDB(Type.Ods, name).createStatement();
		st.executeUpdate("create table cartesdyn (id integer primary key autoincrement," +
				"edimap varchar(16), " +
				"str varchar(16),  " +
				"secteur varchar(3)" +
		")");
		DatabaseManager.addDatabase(name, Type.Ods, new SimpleDateFormat().format(new Date()));
		st.close();
	}

	/**
	 * Crée la structue des tables d'une base Edimap
	 * @param name Nom de la base recevant les tables
	 * @throws SQLException 
	 */
	public static void createEdimap(String name, String path) throws SQLException {
		Statement st = DatabaseManager.selectDB(Type.Edimap, name).createStatement();
		st.executeUpdate("create table cartes (id integer primary key autoincrement," +
				"name varchar(32), " +
				"type varchar(16), " +
				"fichier varchar(64)" +
		")");
		PreparedStatement insertClef = DatabaseManager.selectDB(Type.Databases, "databases").prepareStatement("insert into clefs (name, type, value) values (?, ?, ?)");
		insertClef.setString(1, "path");
		insertClef.setString(2, name);
		insertClef.setString(3, path);
		insertClef.executeUpdate();
		DatabaseManager.addDatabase(name, Type.Edimap, new SimpleDateFormat().format(new Date()));
		st.close();
		insertClef.close();
	}

	/**
	 * Crée la structure des tables d'une base Satin
	 * @param name Nom de la base recevant les tables
	 * @throws SQLException 
	 */
	public static void createSTIP(String name) throws SQLException {
		Statement st = DatabaseManager.selectDB(Type.STIP, name).createStatement();
		st.executeUpdate("create table balises (id integer primary key autoincrement," +
				"name varchar(5), " +
				"publicated boolean, " +
				"latitude float, " +
				"longitude float, " +
				"centre varchar(3), " +
				"definition varchar(25)," +
				"sccag varchar(3), " +
				"sect1 varchar(3), " +
				"limit1 int, " +
				"sect2 varchar(3), " +
				"limit2 int, " +
				"sect3 varchar(3), " +
				"limit3 int, " +
				"sect4 varchar(3), " +
				"limit4 int, " +
				"sect5 varchar(3), " +
				"limit5 int, " +
				"sect6 varchar(3), " +
				"limit6 int, " +
				"sect7 varchar(3), " +
				"limit7 int, " +
				"sect8 varchar(3), " +
				"limit8 int, " +
				"sect9 varchar(3), " +
		"limit9 int)");
		st.executeUpdate("create table centres (id integer primary key autoincrement," +
				"name varchar(4), " +
				"identite varchar(20)," +
				"numero int," +
		"type varchar(4))");
		st.executeUpdate("create table secteurs (id integer primary key autoincrement, " +
				"nom varchar(3), " +
				"centre varchar(4), " +
				"espace varchar(1), " +
				"numero int, " +
				"flinf int, " +
				"flsup int, " +
		"modes boolean)");
		
		//table contenant les données du fichier POINSECT
		st.executeUpdate("create table poinsect (id integer primary key autoincrement, " +
				"ref varchar(6), " +
				"latitude float, " +
				"longitude float)");
		
		//table contenant les cartes secteurs
		st.executeUpdate("create table cartesect (id integer primary key autoincrement, " +
				"sectnum int, " +
				"flinf int, " +
				"flsup int, " +
				"lateti varchar(8), " +
		"longeti varchar(9))");
		
		//table mettant en relation les cartes avec les points qui les définissent
		st.executeUpdate("create table cartepoint (id integer primary key autoincrement, " +
				"sectnum int, " +
				"flsup int, " +
				"pointref varchar(6)," +
		"refcontour varchar(5))");
		//table contenant les routes
		st.executeUpdate("create table routes (id integer primary key autoincrement, " +
				"name varchar(7), " +
		"espace varchar(1))");
		//table mettant en relation les routes et les balises qui les définissent
		st.executeUpdate("create table routebalise (id integer primary key autoincrement, " +
				"route varchar(7), " +
				"balise varchar(10), " +
				"appartient boolean, " +
		"sens varchar(1))");
		//table contenant les itis
		st.executeUpdate("create table itis (id integer primary key autoincrement, " +
				"entree varchar(5), " +
				"sortie varchar(5), " +
				"flinf int," +
				"flsup int)");
		//table mettant en relation les balises formant les itis
		st.executeUpdate("create table balitis (id integer primary key autoincrement, " +
				"iditi int, " +
				"balise varchar(5), " +
				"appartient boolean)");
		//table des lieux
		st.executeUpdate("create table lieux (id integer primary key autoincrement, " +
				"oaci varchar(4), " +
				"type varchar(2), " +
				"centre1 varchar(1), " +
				"distance1 int, " +
				"pp1 varchar(2), " +
				"nc1 varchar(2)," +
				"centre2 varchar(1), " +
				"distance2 int, " +
				"pp2 varchar(2), " +
				"nc2 varchar(2)," +
				"centre3 varchar(1), " +
				"distance3 int, " +
				"pp3 varchar(2), " +
				"nc3 varchar(2)," +
				"centre4 varchar(1), " +
				"distance4 int, " +
				"pp4 varchar(2), " +
				"nc4 varchar(2))");
		//table des consignes
		st.executeUpdate("create table consignes (id integer primary key autoincrement, " +
				"idlieu int, " +
				"type varchar(1), " +
				"oaci varchar(4), " +
				"balise varchar(5), " +
				"niveau int, " +
				"ecart int, " +
				"eve boolean, " +
				"act boolean, " +
				"mod boolean, " +
				"base int)");
		//on référence la base de données
		DatabaseManager.addDatabase(name, Type.STIP, new SimpleDateFormat().format(new Date()));
		st.close();
	}
	/**
	 * Crée la structue des tables d'une base PAYS
	 * @param name Nome de la base
	 * @throws SQLException 
	 */
	public static void createPays(String name) throws SQLException{
		Statement st = DatabaseManager.selectDB(Type.PAYS, name).createStatement();
		//table contenant les données du fichier POINPAYS
		st.executeUpdate("create table poinpays (id integer primary key autoincrement, " +
				"ref varchar(6), " +
				"latitude float, " +
				"longitude float)");
		//table contenant les données du fichier CONTPAYS
		st.executeUpdate("create table contpays (id integer primary key autoincrement, " +
				"refcontour varchar(5), " +
		"refpoint varchar(6))");
		//table contenant les données du fichier PAYS
		st.executeUpdate("create table pays (id integer primary key autoincrement, " +
				"pays varchar(30), " +
				"contour varchar(30), " +
		"refcontour varchar(5))");
		st.close();
		//on référence la base de données
		DatabaseManager.addDatabase(name, Type.PAYS, new SimpleDateFormat().format(new Date()));
		
	}

	/**
	 * Supprimer une base de données
	 * @param name Nom de la base
	 * @throws SQLException 
	 */
	public static void deleteDatabase(String name, Type type) throws SQLException{
		//fermeture de la connection courante
		switch (type) {
		case STPV:
			if (instance.currentStpv != null && instance.currentStpv.getMetaData().getURL().equals("jdbc:sqlite:"+name)) {
				instance.currentStpv.close();
				instance.currentStpv = null;
			}
			break;
		case EXSA:
			if (instance.currentExsa != null && instance.currentExsa.getMetaData().getURL().equals("jdbc:sqlite:"+name)) {
				instance.currentExsa.close();
				instance.currentExsa = null;
			}
			break;
		case Edimap:
			if (instance.currentEdimap != null && instance.currentEdimap.getMetaData().getURL().equals("jdbc:sqlite:"+name)) {
				instance.currentEdimap.close();
				instance.currentEdimap = null;
			}
			break;
		case STIP:
			if (instance.currentStip != null && instance.currentStip.getMetaData().getURL().equals("jdbc:sqlite:"+name)) {
				instance.currentStip.close();
				instance.currentStip = null;
			}
			break;
		case Ods:
			if (instance.currentODS != null && instance.currentODS.getMetaData().getURL().equals("jdbc:sqlite:"+name)) {
				instance.currentODS.close();
				instance.currentODS = null;
			}
			break;
		case PAYS:
			if (instance.currentPays != null && instance.currentPays.getMetaData().getURL().equals("jdbc:sqlite:"+name)) {
				instance.currentPays.close();
				instance.currentPays = null;
			}
			break;
		default:
			break;
		}
		//suppression du fichier correspondant
		File file = new File(name);
		if(!file.delete()) {	
			try {
				//on vide le fichier si on arrive pas à le supprimer
				//c'est moche, mais c'est comme ça sous windows
				FileWriter out = new FileWriter(file);
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		Statement st = DatabaseManager.selectDB(Type.Databases, "databases").createStatement();
		//on supprime l'entrée dans la db
		st.executeUpdate("delete from databases where name = '" + name+"'");
		//puis on supprime les clefs correspondantes
		st.executeUpdate("delete from clefs where type='"+name+"'");
		st.close();
	}

	/**
	 * Supprimer une base de données
	 * @param id Id de la base
	 * @throws SQLException 
	 */
	public static void deleteDatabase(Integer id) throws SQLException{
		//on recherche d'abord le nom correspondant
		Statement st = DatabaseManager.selectDB(Type.Databases, "databases").createStatement();
		ResultSet rs = st.executeQuery("select name, type from databases where id = " + id.toString());
		rs.next();
		String name = rs.getString(1);
		String type = rs.getString(2);
		Type t = null;
		if(type.equalsIgnoreCase("STPV")){
			t = Type.STPV;
		}else if (type.equalsIgnoreCase("EXSA")){
			t = Type.EXSA;
		}else if (type.equalsIgnoreCase("Edimap")){
			t = Type.Edimap;
		}else if (type.equalsIgnoreCase("Stip")){
			t = Type.STIP;
		}else if (type.equalsIgnoreCase("Ods")){
			t = Type.Ods;
		}else if(type.equalsIgnoreCase("Pays")){
			t = Type.PAYS;
		}
		st.close();
		DatabaseManager.deleteDatabase(name, t);
	}

	/**
	 * Sélectionne une base de données à utiliser
	 * @param id Id de la base de données
	 * @param type Type de la base de données
	 * @throws SQLException 
	 */
	public static void selectDatabase(Integer id, Type type) throws SQLException {
		Statement st = DatabaseManager.selectDB(Type.Databases, "databases").createStatement();
		st.executeUpdate("update databases set selected = 0 where type = '"+type.toString()+"'");
		st.executeUpdate("update databases set selected = 1 where id ='"+id+"'");
		ResultSet result = st.executeQuery("select name from databases where id ='"+id+"'");
		result.next();
		DatabaseManager.selectDB(type, result.getString(1));
		result.close();
		st.close();
	}
	
	public static void selectDatabase(Integer id, String type) throws SQLException {
		if(type.equals("STIP")) {
			DatabaseManager.selectDatabase(id, Type.STIP);
		} else if(type.equals("PAYS")){
			DatabaseManager.selectDatabase(id, Type.PAYS);
		} else if(type.equals("STPV")){
			DatabaseManager.selectDatabase(id, Type.STPV);
		} else if(type.equals("EXSA")){
			DatabaseManager.selectDatabase(id, Type.EXSA);
		} else if(type.equals("Edimap")){
			DatabaseManager.selectDatabase(id, Type.Edimap);
		} else if(type.equals("Ods")){
			DatabaseManager.selectDatabase(id, Type.Ods);
		}
	}

	/**
	 * Désélectionne la base de données sélectionnée de type <code>type</code>
	 * @param type de la base de données
	 */
	public static void unselectDatabase(Type type) throws SQLException{
		Statement st = DatabaseManager.selectDB(Type.Databases, "databases").createStatement();
		ResultSet result = st.executeQuery("select * from databases where selected = 1 and type = '"+type.toString()+"'");
		if(result.next()){
			int id = result.getInt("id");
			DatabaseManager.unselectDatabase(id);
		}
	}
	
	/**
	 * Désélectionne une base de données
	 * @param id de la base de données
	 */
	public static void unselectDatabase(Integer id) throws SQLException{
		Statement st = DatabaseManager.selectDB(Type.Databases, "databases").createStatement();
		st.executeUpdate("update databases set selected = 0 where id ='"+id+"'");
		st.close();
	}
	
	/**
	 * Renvoie un {@link Statement} vers la base de données sélectionnée
	 * Renvoie null si aucune base de données n'est trouvée
	 * @param type Type de la base recherchée
	 * @return Statement
	 * @throws SQLException 
	 */
	public static Statement getCurrent(Type type) throws SQLException{
		if(type.equals(Type.Databases)){
			return DatabaseManager.selectDB(Type.Databases, "databases").createStatement();
		} else {
			Statement st = DatabaseManager.selectDB(Type.Databases, "databases").createStatement();
			ResultSet result = st.executeQuery("select name from databases where selected = 1 and type = '"+type.toString()+"'");
			if(result.next()) {
				String connectionName = result.getString(1) ;
				return DatabaseManager.selectDB(type, connectionName).createStatement();
			} else {
				return null;
			}
		}
	}

	/**
	 * Renvoie le nom de la base de données sélectionnée
	 * Renvoie null si aucune base de données n'est trouvée
	 * @param type Type de la base recherchée
	 * @return QSQLDatabase
	 * @throws SQLException 
	 */
	public static String getCurrentName(Type type) throws SQLException{
		Statement st = DatabaseManager.selectDB(Type.Databases, "databases").createStatement();
		String name = null;
		ResultSet rs = st.executeQuery("select name from databases where selected = 1 and type = '"+type+"'");
		while(rs.next()){
			name = rs.getString(1);
		}
		return name;
	}

	/**
	 * Renvoie une connexion vers la base de données STIP sélectionnée
	 * @return {@link Statement}
	 * @throws SQLException 
	 */
	public static Statement getCurrentStip() throws SQLException {
		return DatabaseManager.getCurrent(Type.STIP);
	}

	/**
	 * Renvoit une connection vers la base de données STPV sélectionnée
	 * @return {@link Statement}
	 * @throws SQLException
	 */
	public static Statement getCurrentStpv() throws SQLException {
		return DatabaseManager.getCurrent(Type.STPV);
	}

	/**
	 * Renvoit une connection vers la base de données EXSA sélectionnée
	 * @return {@link Statement}
	 * @throws SQLException 
	 */
	public static Statement getCurrentExsa() throws SQLException {
		return DatabaseManager.getCurrent(Type.EXSA);
	}

	/**
	 * Renvoit une connection vers la base de données Edimap sélectionnée
	 * @return {@link Statement}
	 * @throws SQLException 
	 */
	public static Statement getCurrentEdimap() throws SQLException {
		return DatabaseManager.getCurrent(Type.Edimap);
	}

	
	
	public static void closeAll(){
			try {
				if(instance.currentPays != null) { instance.currentPays.close(); instance.currentPays = null;}
				if(instance.currentStip != null) { instance.currentStip.close();instance.currentStip= null;}
				if(instance.currentExsa != null) { instance.currentExsa.close();instance.currentExsa= null;}
				if(instance.currentStpv != null) { instance.currentStpv.close();instance.currentStpv= null;}
				if(instance.currentEdimap != null) { instance.currentEdimap.close();instance.currentEdimap= null;}
				if(instance.currentODS != null) { instance.currentODS.close();instance.currentODS = null;}
				if(instance.databases != null) { instance.databases.close(); instance.databases = null;}
			} catch (SQLException e) {
				e.printStackTrace();
			}
	}
}
