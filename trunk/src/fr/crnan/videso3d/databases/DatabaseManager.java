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

package fr.crnan.videso3d.databases;

import fr.crnan.videso3d.Couple;
import fr.crnan.videso3d.DatasManager;
import fr.crnan.videso3d.VidesoController;
import fr.crnan.videso3d.databases.aip.AIP;
import fr.crnan.videso3d.databases.edimap.Cartes;
import fr.crnan.videso3d.databases.skyview.SkyViewController;
import fr.crnan.videso3d.databases.stip.StipController;
import fr.crnan.videso3d.ihm.components.Omnibox;
import fr.crnan.videso3d.ihm.components.Omnibox.ItemCouple;
import gov.nasa.worldwind.util.Logging;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javax.swing.event.SwingPropertyChangeSupport;


/**
 * Gère la base de données
 * @author Bruno Spyckerelle
 * @version 0.8.3
 */
public final class DatabaseManager {
	
	private static DatabaseManager instance = new DatabaseManager();
	
	private SwingPropertyChangeSupport support;
	
	/**
	 * Property fired when a base is changed
	 */
	public static String BASE_UNSELECTED = "fr.crnan.videso3d.baseunselected";
	public static String BASE_SELECTED = "fr.crnan.videso3d.baseselected";
	
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
	 * Base couvertures radios sélectionnée
	 */
	private Connection currentRadioCov;	
	/**
	 * Base SkyView
	 */
	private Connection currentSkyView;
	/**
	 * Base AIP
	 */
	private Connection currentAIP;
	/**
	 * Base KML
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
			st.close();
		}
		catch (SQLException e){
			try {
				String create = "create table databases (id integer primary key autoincrement, " +
				"name varchar(20), " +
				"type varchar(20), " +
				"date varchar(12), " +
				"commentaire varchar(64), "+
				"selected boolean)";
				st.executeUpdate(create);
				create = "create table clefs (id integer primary key autoincrement," +
						"name varchar(32), " +
						"type varchar(16), " +
						"value varchar(64))";
				st.executeUpdate(create);
				st.close();
			} catch (SQLException e2) {
				e2.printStackTrace();
			}
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
		} 
		support = new SwingPropertyChangeSupport(this);
	}
	
	/**
	 * Sélectionne une base de données
	 * @param name Nom de la base de données à sélectionner
	 * @return {@link Connection} Connection vers la base sélectionnée
	 * @throws SQLException 
	 */
	public static Connection selectDB(DatasManager.Type type, String name) throws SQLException{
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
		case RadioCov:
			if(instance.currentRadioCov == null) {
				instance.currentRadioCov = DriverManager.getConnection("jdbc:sqlite:"+name);
			} else {
				if(!instance.currentRadioCov.getMetaData().getURL().equals("jdbc:sqlite:"+name)){
					//changement de base de données
					instance.currentRadioCov.close();
					instance.currentRadioCov = DriverManager.getConnection("jdbc:sqlite:"+name);
				}
			}
			return instance.currentRadioCov;		
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
		case SkyView:
			//il faut d'abord récupérer le chemin de la bdd
			String path = getSkyViewPath(name);
			if(path != null) {
				try {
					Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				String database = "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};DBQ=";
				database += path + ";DriverID=22;READONLY=true}";
				if(instance.currentSkyView == null) {
					instance.currentSkyView = DriverManager.getConnection(database, "", "");
				} else {
					if(!instance.currentSkyView.getMetaData().getURL().equals(database)){
						instance.currentSkyView.close();
						instance.currentSkyView = DriverManager.getConnection(database, "", "");
					}
				}
				return instance.currentSkyView;
			} else {
				//aucune base trouvée
				return null;
			}
			
		case AIP:
			if(instance.currentAIP == null) {
				instance.currentAIP = DriverManager.getConnection("jdbc:sqlite:"+name);
			} else {
				if(!instance.currentAIP.getMetaData().getURL().equals("jdbc:sqlite:"+name)){
					//changement de base de données
					instance.currentAIP.close();
					instance.currentAIP = DriverManager.getConnection("jdbc:sqlite:"+name);
				}
			}
			return instance.currentAIP;
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
	public static Boolean databaseExists(DatasManager.Type type, String name) throws SQLException{
		Boolean exists = false;
		Statement st = DatabaseManager.selectDB(DatasManager.Type.Databases, "databases").createStatement();
		ResultSet result = st.executeQuery("SELECT * FROM databases WHERE name = '"+name+"' and type ='"+type.toString()+"'");
		if (result.next()) exists = true;
		result.close();
		st.close();
		return exists;
	}

	/**
	 * Teste si la base de données <code>id</code> est sélectionnée
	 * @param id de la base de données
	 * @return Boolean True si la base est sélectionnée
	 */
	public static Boolean isSelected(Integer id){
		Boolean result = false;
		try {
			Statement st = DatabaseManager.selectDB(DatasManager.Type.Databases, "databases").createStatement();
			ResultSet rs = st.executeQuery("select * from databases where id='"+id+"' and selected = '1'");
			if (rs.next()) {
				result = true;
			} 
			rs.close();
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Ajoute une base de données dans la liste et la marque comme sélectionnée
	 * @param name Nom de la base de données
	 * @param type Type de la base de données
	 * @param date Date associée
	 */
	private static void addDatabase(String name, DatasManager.Type type, String date){
		try {
			Statement st = DatabaseManager.selectDB(DatasManager.Type.Databases, "databases").createStatement();
			st.executeUpdate("UPDATE databases SET selected = 0 WHERE selected = 1 and type = '"+type.toString()+"'");
			st.close();
			String insert = "insert into databases (name, type, date, selected) values (?, ?, ?, ?)";
			PreparedStatement insertDatabase = DatabaseManager.selectDB(DatasManager.Type.Databases, "databases").prepareStatement(insert);
			insertDatabase.setString(1, name);
			insertDatabase.setString(2, type.toString());
			insertDatabase.setString(3, date);
			insertDatabase.setBoolean(4, true);
			insertDatabase.executeUpdate();
			insertDatabase.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}	
		//à l'ajout d'une base de données, l'envoi de propertychange est demandé par le Filemanager à la fin de l'import
	}

	public static void addDatabase(String name, DatasManager.Type type) throws SQLException{
		if(!databaseExists(type, name)){
			addDatabase(name, type, new SimpleDateFormat().format(new Date()));
		}
		selectDatabase(getId(name), type);
	}
	
	/**
	 * Ajoute une référence à une base SkyView
	 * @param name
	 * @param path
	 */
	public static void createSkyView(String name, String path){
		PreparedStatement insertClef;
		try {
			insertClef = DatabaseManager.selectDB(DatasManager.Type.Databases, "databases").prepareStatement("insert into clefs (name, type, value) values (?, ?, ?)");
			insertClef.setString(1, "path");
			insertClef.setString(2, name);
			insertClef.setString(3, path);
			insertClef.executeUpdate();
			insertClef.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		DatabaseManager.addDatabase(name, DatasManager.Type.SkyView, new SimpleDateFormat().format(new Date()));
	}
	
	/**
	 * Crée la structure des tables relatives aux données EXSA
	 * @param name Nom de la base de données recevant les tables
	 * @throws SQLException 
	 */
	public static void createEXSA(String name) throws SQLException{
		Statement st = DatabaseManager.selectDB(DatasManager.Type.EXSA, name).createStatement();
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
		st.executeUpdate("create table centscodf (id integer primary key autoincrement, " +
				"vvf varchar(1), " +
				"debut varchar(4), " +
				"fin varchar(4), " +
				"espaces varchar(20))");
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
		st.executeUpdate("create table association (id integer primary key autoincrement, " +
				"name varchar(6)," +
				"associations varchar(70))");
		st.executeUpdate("create table signalisation (id integer primary key autoincrement, " +
				"name varchar(6)," +
				"signalisations varchar(70))");
		st.executeUpdate("create table centstack (id integer primary key autoincrement, " +
				"name varchar(6)," +
				"latitude float," +
				"longitude float," +
				"xcautra float," +
				"ycautra float," +
				"rayonint int," +
				"rayonext int," +
				"flinf int," +
				"flsup int," +
				"type varchar(8))");
		st.executeUpdate("create table centtmaf (id integer primary key autoincrement, " +
				"name varchar(6)," +
				"latitude float," +
				"longitude float," +
				"xcautra float," +
				"ycautra float," +
				"rayon int," +
				"fl int," +
				"nomsecteur varchar(8))");
		st.executeUpdate("create table centsctma (id integer primary key autoincrement, " +
				"carre int, " +
				"souscarre int, " +
				"v1 int, " +
				"v2 int, " +
				"v3 int, " +
				"name varchar(2))");
		st.executeUpdate("create table centcrvsm (id integer primary key autoincrement, " +
				"carre int, " +
				"souscarre int, " +
				"rvsm int)"); //0: non rvsm; 1: RVSM sauf non corrélés; 2: RVSM même pour les non corrélés
		st.close();
		//on ajoute le nom de la base
		DatabaseManager.addDatabase(name, DatasManager.Type.EXSA, new SimpleDateFormat().format(new Date()));
		
	}
	/**
	 * Crée la structure des tables d'une base STPV
	 * @param name Nom de la base recevant les tables
	 * @throws SQLException 
	 */
	public static void createSTPV(String name, String path) throws SQLException {
		Statement st = DatabaseManager.selectDB(DatasManager.Type.STPV, name).createStatement();
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
				"niveau int, "+
				"actau int, "+
				"rerfl int)");
		st.executeUpdate("create table lieu27 (id integer primary key autoincrement, " +
				"oaci varchar(4), " +
				"balise varchar(5), " +
				"niveau int, "+
				"rerfl int)");
		st.executeUpdate("create table lieu6 (id integer primary key autoincrement, " +
				"oaci varchar(4), " +
				"bal1 varchar(5), " +
				"xfl1 int)");
		st.executeUpdate("create table lieu8 (id integer primary key autoincrement, " +
				"depart varchar(4), " +
				"arrivee varchar(4), " +
				"fl int)");
		st.executeUpdate("create table lieu91 (id integer primary key autoincrement, " +
				"oaci varchar(4), " +
				"indicateur varchar(3), " +
				"secteur_donnant varchar(2), " +
				"secteur_recevant varchar(2), " +
				"bal1 varchar(5), " +
				"bal2 varchar(5), " +
				"piste varchar(3), " +
				"avion varchar(3), " +
				"tfl int, " +
				"terrain1 varchar(4), " +
				"conf1 varchar(3), " +
				"terrain2 varchar(4), " +
				"conf2 varchar(3))");
		st.executeUpdate("create table sect (id integer primary key autoincrement, " +
				"nom varchar(2), " +
				"freq varchar(7))");
		st.executeUpdate("create table lieu90 (id integer primary key autoincrement, " +
				"oaci varchar(4), " +
				"balini varchar(5), " +
				"bal1 varchar(5), " +
				"bal2 varchar(5), " +
				"bal3 varchar(5), " +
				"bal4 varchar(5), " +
				"bal5 varchar(5), " +
				"bal6 varchar(5), " +
				"bal7 varchar(5), " +
				"bal8 varchar(5), " +
				"hel boolean, " +
				"jet boolean, " +
				"fir boolean, " +
				"uir boolean)");
		st.executeUpdate("create table lieu901 (id integer primary key autoincrement, " +
				"lieu90 int, " +
				"conf varchar(5), " +
				"name varchar(7))");
		st.executeUpdate("create table bali (id integer primary key autoincrement, " +
				"name varchar(5), " +
				"TMA int, " +
				"sect1 int, " +
				"sect2 int, " +
				"sect3 int, " +
				"sect4 int, " +
				"sect5 int, " +
				"sect6 int, " +
				"sect7 int, " +
				"sect8 int, " +
				"sect9 int)");
		st.executeUpdate("create table imprSLCT (id integer primary key autoincrement, " +
				"nom_balise varchar(5), " +
				"nom_SLCT varchar(4))");
		//catégories de code
		st.executeUpdate("create table cat_code (id integer primary key autoincrement, name varchar(2), code int)");
		//liaisons privilégiées
		st.executeUpdate("create table lps (id integer primary key autoincrement, " +
				"name varchar(5), " +
				"cat varchar(3), " +
				"depart varchar(4), " +
				"sl1 varchar(5), " +
				"sl2 varchar(5), " +
				"sl3 varchar(5), " +
				"sl4 varchar(5), " +
				"sl5 varchar(5), " +
				"sl6 varchar(5), " +
				"arr varchar(5), " +
				"modes boolean, " +
				"debut_suite int, " +
				"fin_suite int, " +
				"cat_code varchar(3), " +
				"debut_suppl int, " +
				"fin_suppl int, " +
				"code2 int)");
		st.executeUpdate("create table double (id integer primary key autoincrement, " +
				"entite varchar(2), " +
				"destinataire varchar(2), " +
				"identifiant varchar(2), " +
				"strip varchar(3), " +
				"bal1 varchar(5), " +
				"bal2 varchar(5), " +
				"flinf1 varchar(3), " +
				"flsup1 varchar(3), " +
				"flinf2 varchar(3), " +
				"flsup2 varchar(3))");
		st.executeUpdate("create table coor30 (id integer primary key autoincrement, " +
				"donnant varchar(2), " +
				"recevant varchar(2), " +
				"val1 varchar(3), " +
				"val2 varchar(3), " +
				"val3 varchar(3), " +
				"val4 varchar(3))");
		st.executeUpdate("create table coor40 (id integer primary key autoincrement, " +
				"donnant varchar(2), " +
				"recevant varchar(2), " +
				"bal1 varchar(5), " +
				"bal2 varchar(5), " +
				"balref varchar(5), " +
				"val1 varchar(3), " +
				"val2 varchar(3), " +
				"val3 varchar(3), " +
				"val4 varchar(3))");
		st.close();
		DatabaseManager.addDatabase(name, DatasManager.Type.STPV, new SimpleDateFormat().format(new Date()));
	}

	/**
	 * Crée la structure des tables d'une base ODS
	 * @param name Nom de la base recevant les tables
	 * @throws SQLException 
	 */
	public static void createODS(String name) throws SQLException {
		Statement st = DatabaseManager.selectDB(DatasManager.Type.Ods, name).createStatement();
		st.executeUpdate("create table cartesdyn (id integer primary key autoincrement," +
				"edimap varchar(16), " +
				"str varchar(16),  " +
				"secteur varchar(3)" +
		")");
		st.close();
		DatabaseManager.addDatabase(name, DatasManager.Type.Ods, new SimpleDateFormat().format(new Date()));
	}

	/**
	 * Crée la structue des tables d'une base Edimap
	 * @param name Nom de la base recevant les tables
	 * @throws SQLException 
	 */
	public static void createEdimap(String name, String path) throws SQLException {
		Statement st = DatabaseManager.selectDB(DatasManager.Type.Edimap, name).createStatement();
		st.executeUpdate("create table cartes (id integer primary key autoincrement," +
				"name varchar(32), " +
				"type varchar(16), " +
				"fichier varchar(64)" +
		")");
		st.close();
		PreparedStatement insertClef = DatabaseManager.selectDB(DatasManager.Type.Databases, "databases").prepareStatement("insert into clefs (name, type, value) values (?, ?, ?)");
		insertClef.setString(1, "path");
		insertClef.setString(2, name);
		insertClef.setString(3, path);
		insertClef.executeUpdate();
		insertClef.close();
		DatabaseManager.addDatabase(name, DatasManager.Type.Edimap, new SimpleDateFormat().format(new Date()));
	}

	/**
	 * Crée la structure des tables d'une base Satin
	 * @param name Nom de la base recevant les tables
	 * @throws SQLException 
	 */
	public static void createSTIP(String name) throws SQLException {
		Statement st = DatabaseManager.selectDB(DatasManager.Type.STIP, name).createStatement();
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
				"routeid int, " +
				"balise varchar(10), " +
				"appartient boolean, " +
		"sens varchar(1))");
		//table contenant les entrées des routes
		st.executeUpdate("create table routeentrees (id integer primary key autoincrement, " +
				"route varchar(7), " +
				"routeid int, " +
				"entree varchar(1))");
		//table contenant les sorties des routes
				st.executeUpdate("create table routesorties (id integer primary key autoincrement, " +
						"route varchar(7), " +
						"routeid int, " +
						"sortie varchar(1))");
		//table contenant les itis
		st.executeUpdate("create table itis (id integer primary key autoincrement, " +
				"entree varchar(5), " +
				"sortie varchar(5), " +
				"flinf int," +
				"flsup int)");
		//table mettant en relation les balises formant les itis
		st.executeUpdate("create table balitis (id integer primary key autoincrement, " +
				"iditi int, " +
				"balid int, " +
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
		//table des trajets
		st.executeUpdate("create table trajets (id integer primary key autoincrement, " +
				"eclatement varchar(5), " +
				"eclatement_id int, " +
				"raccordement varchar(5), " +
				"raccordement_id int, " +
				"type varchar(2), " +
				"fl int, " +
				"cond1 varchar(7), " +
				"etat1 varchar(1), " +
				"cond2 varchar(7), " +
				"etat2 varchar(1), " +
				"cond3 varchar(7), " +
				"etat3 varchar(1), " +
				"cond4 varchar(7), " +
				"etat4 varchar(1), " +
				"FOREIGN KEY (eclatement_id) REFERENCES balises(id))");
		//table des balises des trajets
		st.executeUpdate("create table baltrajets (id integer primary key autoincrement, " +
				"idtrajet int," +
				"balise varchar(5), " +
				"balid int, " +
				"appartient boolean, " +
				"FOREIGN KEY(balid) REFERENCES balises(id))");
		st.close();
		//table des couples de balises interdits
		st.executeUpdate("create table balint (id integer primary key autoincrement, " +
				"uir boolean, " +
				"fir boolean, " +
				"bal1 varchar(5), " +
				"bal2 varchar(5), " +
				"balise varchar(5), " +
				"appartient boolean)");
		//table des couples de balises des itis
		//nécessaire pour rendre plus rapide la recherche de connexions et de trajets
		st.executeUpdate("create table couplebalitis (id integer primary key autoincrement, " +
				"iditi int, " +
				"idbal1 int, " +
				"idbal2 int," +
				"bal1 varchar(5), " +
				"bal2 varchar(6))");
		//table des connexions
		st.executeUpdate("create table connexions (id integer primary key autoincrement, " +
				"terrain varchar(4), " +
				"connexion varchar(5), " +
				"type varchar(1), " +
				"perfo varchar(1), " +
				"flinf int, " +
				"flsup int, " +
				"vitessesigne varchar(1), " +
				"vitesse int)");
		//table des balises des connexions
		st.executeUpdate("create table balconnexions (id integer primary key autoincrement, " +
				"idconn int, " +
				"balise varchar(5), " +
				"balid int, " +
				"appartient boolean)");
		//on référence la base de données
		DatabaseManager.addDatabase(name, DatasManager.Type.STIP, new SimpleDateFormat().format(new Date()));
	}
	/**
	 * Crée la structue des tables d'une base PAYS
	 * @param name Nome de la base
	 * @throws SQLException 
	 */
	public static void createPays(String name) throws SQLException{
		Statement st = DatabaseManager.selectDB(DatasManager.Type.PAYS, name).createStatement();
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
		DatabaseManager.addDatabase(name, DatasManager.Type.PAYS, new SimpleDateFormat().format(new Date()));
		
	}

	/**
	 * Cree la structure des tables d'une base Couvertures Radios
	 * @param name Nom de la base recevant les tables
	 * @throws SQLException 
	 */
	public static void createRadioCov(String name, String path) throws SQLException {
		Statement st = DatabaseManager.selectDB(DatasManager.Type.RadioCov, name).createStatement();
		/*
		st.executeUpdate("create table radio (id integer primary key autoincrement," +
				"name varchar(32), " +
				"type varchar(16), " +
				"path varchar(64)" +
		")");
		*/
		st.executeUpdate("create table radio (id integer primary key autoincrement," +
				"databaseId integer, " +				
				"path varchar(64)" +
		")");
		st.close();

		/*
		PreparedStatement insertClef = DatabaseManager.selectDB(Type.Databases, "databases").prepareStatement("insert into clefs (name, type, value) values (?, ?, ?)");
		insertClef.setString(1, "path");
		insertClef.setString(2, name);
		insertClef.setString(3, path);
		insertClef.executeUpdate();
		insertClef.close();
		DatabaseManager.addDatabase(name, Type.RadioCov, new SimpleDateFormat().format(new Date()));
		 */
	}
	
	public static void insertRadioCov(String name, String path) throws SQLException {
			
		String date = new SimpleDateFormat().format(new Date());
		int databaseId = 0;
		
		PreparedStatement insertClef = DatabaseManager.selectDB(DatasManager.Type.Databases, "databases").prepareStatement("insert into clefs (name, type, value) values (?, ?, ?)");
		insertClef.setString(1, "path");
		insertClef.setString(2, name);
		insertClef.setString(3, path);
		insertClef.executeUpdate();
		insertClef.close();
		DatabaseManager.addDatabase(name, DatasManager.Type.RadioCov, date);
		
		Statement st = DatabaseManager.selectDB(DatasManager.Type.Databases, "databases").createStatement();
		ResultSet rs = st.executeQuery("select id from databases where name = '"+name+"'");				
		 rs.next();
		 databaseId = rs.getInt(1);			
		
		PreparedStatement insertRadio = DatabaseManager.selectDB(DatasManager.Type.RadioCov, "radio").prepareStatement("insert into radio(databaseId,path) values (?,?)");
		insertRadio.setInt(1, databaseId);
		
		insertRadio.setString(2,path);
		insertRadio.executeUpdate();
		insertRadio.close();		
	}
	
	/**
	 * Cree la structure des tables pour les données AIP
	 * @param name Nom de la base recevant les tables
	 * @throws SQLException 
	 */
	public static void createAIP(String name, String path) throws SQLException{
		Statement st = DatabaseManager.selectDB(DatasManager.Type.AIP, name).createStatement();
		
		st.executeUpdate("create table volumes (id integer primary key autoincrement," +
				"pk integer,"+
				"type varchar(8),"+
				"nom varchar(64)"+
		")");
		st.executeUpdate("create table routes (pk integer primary key," +
				"type varchar(3),"+
				"nom varchar(10),"+
				"navFixExtremite integer"+
		")");
		st.executeUpdate("create table segments (pk integer primary key," +
				"pkRoute integer,"+
				"sequence integer,"+
				"navFixExtremite integer"+
		")");
		st.executeUpdate("create table ACCTraverses (routes_pk integer,"+
				"nomACC varchar(10),"+
				"PRIMARY KEY (routes_pk, nomACC),"+
				"FOREIGN KEY (routes_pk) references routes(pk)"+
		")");
		st.executeUpdate("create table NavFix (pk integer primary key," +
				"type varchar(7),"+
				"nom varchar(10),"+
				"lat double,"+
				"lon double,"+
				"frequence double"+
		")");
		st.executeUpdate("create table aerodromes (pk integer primary key,"+
				"code varchar(6),"+
				"nom varchar(30),"+
				"type int,"+			// type=0 : ras; type=1 : altisurface; type=2 : privé. 
				"latRef double,"+
				"lonRef double"+
				")");
		st.executeUpdate("create table runways (pk integer primary key,"+
				"pk_ad integer,"+
				"nom varchar(7),"+
				"orientation integer,"+
				"longueur double,"+
				"largeur double,"+
				"lat1 double,"+
				"lon1 double,"+
				"lat2 double,"+
				"lon2 double,"+
				"FOREIGN KEY (pk_ad) references aerodromes(pk)"+
				")");
		st.close();
		
		PreparedStatement insertClef = DatabaseManager.selectDB(DatasManager.Type.Databases, "databases").prepareStatement("insert into clefs (name, type, value) values (?, ?, ?)");
		insertClef.setString(1, "path");
		insertClef.setString(2, name);
		insertClef.setString(3, new File(path).getName());
		insertClef.executeUpdate();
		insertClef.close();
		
		//on référence la base de données
		DatabaseManager.addDatabase(name, DatasManager.Type.AIP, new SimpleDateFormat().format(new Date()));
	}
	
	/**
	 * Supprimer une base de donnees
	 * @param name Nom de la base
	 * @throws SQLException 
	 */
	public static void deleteDatabase(String name, DatasManager.Type type) throws SQLException{
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
		case RadioCov:
			if (instance.currentRadioCov != null && instance.currentRadioCov.getMetaData().getURL().equals("jdbc:sqlite:"+name)) {
				instance.currentRadioCov.close();
				instance.currentRadioCov = null;
			}
			break;	
		case SkyView:
			String path = getSkyViewPath(name);
			if(path != null){
				String driver = "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};DBQ=";
				driver += path + ";DriverID=22;READONLY=true}";
				if(instance.currentSkyView != null && instance.currentSkyView.getMetaData().getURL().equals(driver)){
					instance.currentSkyView.close();
					instance.currentSkyView = null;
				}
			}
			break;
		case AIP:
			if (instance.currentAIP != null && instance.currentAIP.getMetaData().getURL().equals("jdbc:sqlite:"+name)) {
				instance.currentAIP.close();
				instance.currentAIP = null;
			}
			break;
		default:
			break;
		}

		//suppression du fichier correspondant seulement si ce n'est pas une base SkyView
		if(type.compareTo(DatasManager.Type.SkyView) != 0 && name != null) {
			File file = new File(name);
			if(file.exists() && !file.delete()) {	
				//on vide le fichier si on arrive pas à le supprimer
				//c'est moche, mais c'est comme ça sous windows
				String[] subFiles = file.list();
				for(String subFile : subFiles){
					File deleteFile = new File(subFile);
					deleteFile.delete();
				}
			}
		}
		//on vérifie si la bdd était sélectionnée
		Statement st = DatabaseManager.selectDB(DatasManager.Type.Databases, "databases").createStatement();
		ResultSet rs = st.executeQuery("select selected from databases where name = '"+name+"'");
		boolean changed = false;
		if(rs.next() && rs.getBoolean(1)){
			//la base de données sélectionnée va changer
			changed = true;
		}
		//on supprime l'entrée dans la db
		st.executeUpdate("delete from databases where name = '" + name+"'");
		//puis on supprime les clefs correspondantes
		st.executeUpdate("delete from clefs where type='"+name+"'");
		st.close();
		//si la base de données sélectionnée a changé
		if(changed) instance.support.firePropertyChange(BASE_UNSELECTED, null, type);
	}

	/**
	 * Supprimer une base de données
	 * @param id Id de la base
	 * @throws SQLException 
	 */
	public static void deleteDatabase(Integer id) throws SQLException{
		//on recherche d'abord le nom correspondant
		Statement st = DatabaseManager.selectDB(DatasManager.Type.Databases, "databases").createStatement();
		ResultSet rs = st.executeQuery("select name, type from databases where id = " + id.toString());
		rs.next();
		String name = rs.getString(1);
		String type = rs.getString(2);
		st.close();
		DatabaseManager.deleteDatabase(name, stringToType(type));
	}

	/**
	 * Sélectionne une base de données à utiliser
	 * Ne pas oublier d'appeler DatabaseManager.fireBaseSelected(type)
	 * pour informer les listeners du changement de base
	 * @param id Id de la base de données
	 * @param type Type de la base de données
	 * @throws SQLException 
	 */
	public static void selectDatabase(Integer id, DatasManager.Type type) throws SQLException {
		Statement st = DatabaseManager.selectDB(DatasManager.Type.Databases, "databases").createStatement();
		st.executeUpdate("update databases set selected = 0 where type = '"+type.toString()+"'");
		st.executeUpdate("update databases set selected = 1 where id ='"+id+"'");
		ResultSet result = st.executeQuery("select name from databases where id ='"+id+"'");
		result.next();
		String name = result.getString(1);
		result.close();
		st.close();
		DatabaseManager.selectDB(type, name);
	}
	
	/**
	 * Selectionne la base la plus récente
	 * @param type Type de la base de données à sélectionner
	 * @return true si une base a été sélectionnée
	 * @throws SQLException 
	 */
	public static boolean selectDatabase(DatasManager.Type type) throws SQLException{
		Statement st = DatabaseManager.selectDB(DatasManager.Type.Databases, "databases").createStatement();
		ResultSet rs = st.executeQuery("select max(id) from databases where type ='"+type.toString()+"'");
		if(rs.next()){
			selectDatabase(rs.getInt(1), type);
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Ne pas oublier d'appeler DatabaseManager.fireBaseSelected(type)
	 * pour informer les listeners du changement de base
	 * @param id
	 * @param type
	 * @throws SQLException
	 */
	public static void selectDatabase(Integer id, String type) throws SQLException {
		DatabaseManager.selectDatabase(id, stringToType(type));	
	}

	/**
	 * Ne pas oublier d'appeler DatabaseManager.fireBaseSelected(type)
	 * pour informer les listeners du changement de base
	 * @param name
	 * @param type
	 * @throws SQLException
	 */
	public static void selectDatabase(String name, DatasManager.Type type) throws SQLException {
		Statement st = DatabaseManager.selectDB(DatasManager.Type.Databases, "databases").createStatement();
		ResultSet rs = st.executeQuery("select id from databases where name ='"+name+"'");
		if(rs.next()){
			selectDatabase(rs.getInt(1), type);
		}
	}
	
	/**
	 * Désélectionne la base de données sélectionnée de type <code>type</code>
	 * @param type de la base de données
	 */
	public static void unselectDatabase(DatasManager.Type type) throws SQLException{
		Statement st = DatabaseManager.selectDB(DatasManager.Type.Databases, "databases").createStatement();
		ResultSet result = st.executeQuery("select * from databases where selected = 1 and type = '"+type.toString()+"'");
		Integer id = null;
		if(result.next()){
			id = result.getInt("id");
		}
		result.close();
		st.close();
		if(id != null) {
			DatabaseManager.unselectDatabase(id);
			instance.support.firePropertyChange(BASE_UNSELECTED, null, type);
		}
	}
	
	/**
	 * Désélectionne une base de données
	 * @param id de la base de données
	 */
	private static void unselectDatabase(Integer id) throws SQLException{
		Statement st = DatabaseManager.selectDB(DatasManager.Type.Databases, "databases").createStatement();
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
	public static Statement getCurrent(DatasManager.Type type) throws SQLException{
		if(type.equals(DatasManager.Type.Databases)){
			return DatabaseManager.selectDB(DatasManager.Type.Databases, "databases").createStatement();
		} else {
			Statement st = DatabaseManager.selectDB(DatasManager.Type.Databases, "databases").createStatement();
			ResultSet result = st.executeQuery("select name from databases where selected = 1 and type = '"+type.toString()+"'");
			String connectionName = null;
			if(result.next()) {
				connectionName = result.getString(1) ;
			} 
			result.close();
			st.close();
			return connectionName == null ? null : DatabaseManager.selectDB(type, connectionName).createStatement();
		}
	}
	
	
	/**
	 * Crée un PreparedStatement sur la base de données courante du type <code>type</code>, avec la requête sql passée en paramètre.
	 * @param type
	 * @param sqlRequest
	 * @return {@link PreparedStatement}
	 * @throws SQLException
	 */
	public static PreparedStatement prepareStatement(DatasManager.Type type, String sqlRequest) throws SQLException{
		if(type.equals(DatasManager.Type.Databases)){
			return DatabaseManager.selectDB(DatasManager.Type.Databases, "databases").prepareStatement(sqlRequest);
		} else {
			String connectionName = getCurrentName(type);
			return connectionName == null ? null : DatabaseManager.selectDB(type, connectionName).prepareStatement(sqlRequest);
		}
	}

	/**
	 * Renvoie le nom de la base de données sélectionnée
	 * Renvoie null si aucune base de données n'est trouvée
	 * @param type Type de la base recherchée
	 * @return QSQLDatabase
	 * @throws SQLException 
	 */
	public static String getCurrentName(DatasManager.Type type) throws SQLException{
		Statement st = DatabaseManager.selectDB(DatasManager.Type.Databases, "databases").createStatement();
		String name = null;
		ResultSet rs = st.executeQuery("select name from databases where selected = 1 and type = '"+type+"'");
		while(rs.next()){
			name = rs.getString(1);
		}
		rs.close();
		st.close();
		return name;
	}

	/**
	 * 
	 * @param name Name of the database
	 * @return
	 * @throws SQLException 
	 */
	private static int getId(String name) throws SQLException{
		Statement st = DatabaseManager.selectDB(DatasManager.Type.Databases, "databases").createStatement();
		ResultSet rs = st.executeQuery("select id from databases where name = '"+name+"'");
		Integer id = null;
		while(rs.next()){
			id = rs.getInt(1);
		}
		rs.close();
		st.close();
		return id;
	}
	
	/**
	 * Retourne le chemin correspondant à une base SkyView.
	 * Renvoit null si aucune base n'est trouvée
	 * @param name
	 * @return Le chemin correspondant à la base SkyView <code>name</code>
	 * @throws SQLException 
	 */
	private static String getSkyViewPath(String name) throws SQLException{
		Statement st = DatabaseManager.selectDB(DatasManager.Type.Databases, "databases").createStatement();
		ResultSet rs = st.executeQuery("select value from clefs where type ='"+name+"' and name = 'path'");
		if(rs.next()){
			return rs.getString(1);
		} else {
			return null;
		}
	}
	
	/**
	 * Renvoie une connexion vers la base de données STIP sélectionnée
	 * @return {@link Statement}
	 * @throws SQLException 
	 */
	public static Statement getCurrentStip() throws SQLException {
		return DatabaseManager.getCurrent(DatasManager.Type.STIP);
	}

	/**
	 * Renvoie une connection vers la base de données STPV sélectionnée
	 * @return {@link Statement}
	 * @throws SQLException
	 */
	public static Statement getCurrentStpv() throws SQLException {
		return DatabaseManager.getCurrent(DatasManager.Type.STPV);
	}
	
	/**
	 * Renvoit une connection vers la base de données EXSA sélectionnée
	 * @return {@link Statement}
	 * @throws SQLException 
	 */
	public static Statement getCurrentExsa() throws SQLException {
		return DatabaseManager.getCurrent(DatasManager.Type.EXSA);
	}

	/**
	 * Renvoit une connection vers la base de données Edimap sélectionnée
	 * @return {@link Statement}
	 * @throws SQLException 
	 */
	public static Statement getCurrentEdimap() throws SQLException {
		return DatabaseManager.getCurrent(DatasManager.Type.Edimap);
	}

	/**
	 * Renvoit une connection vers la base de donnees des couvertures radios selectionnee
	 * @return {@link Statement}
	 * @throws SQLException 
	 */
	public static Statement getCurrentRadioCov() throws SQLException {
		return DatabaseManager.getCurrent(DatasManager.Type.RadioCov);
	}
	
	/**
	 * Renvoit la liste des path des donnees de couvertures radio selectionnes
	 * @return liste des chemin vers les couvertures radios
	 * @throws SQLException
	 */
	public static ArrayList<String> getCurrentRadioCovPath() throws SQLException {
		ArrayList <String> pathTab = new ArrayList<String>();
		Statement st = DatabaseManager.getCurrentRadioCov();
		ResultSet rs = st.executeQuery("select radio.path from radio");
		while (rs.next()) {
		    pathTab.add(rs.getString(1));
		}
		return pathTab;
	}
	
	/**
	 * Renvoit une connection vers la base SkyView sélectionnée
	 * @return {@link Statement}
	 * @throws SQLException
	 */
	public static Statement getCurrentSkyView() throws SQLException {
		return DatabaseManager.getCurrent(DatasManager.Type.SkyView);
	}
	
	/**
	 * Renvoit une connection vers la base AIP sélectionnée
	 * @return {@link Statement}
	 * @throws SQLException
	 */
	public static Statement getCurrentAIP() throws SQLException {
		return DatabaseManager.getCurrent(DatasManager.Type.AIP);
	}
	
	public static void closeAll(){
			try {
				if(instance.currentPays != null) { instance.currentPays.close(); instance.currentPays = null;}
				if(instance.currentStip != null) { instance.currentStip.close();instance.currentStip= null;}
				if(instance.currentExsa != null) { instance.currentExsa.close();instance.currentExsa= null;}
				if(instance.currentStpv != null) { instance.currentStpv.close();instance.currentStpv= null;}
				if(instance.currentEdimap != null) { instance.currentEdimap.close();instance.currentEdimap= null;}
				if(instance.currentODS != null) { instance.currentODS.close();instance.currentODS = null;}
				if(instance.currentRadioCov != null) { instance.currentRadioCov.close();instance.currentRadioCov = null;}
				if(instance.currentSkyView != null) {instance.currentSkyView.close(); instance.currentSkyView = null;}
				if(instance.currentAIP!=null) {instance.currentAIP.close(); instance.currentAIP = null;}
				if(instance.databases != null) { instance.databases.close(); instance.databases = null;}
			} catch (SQLException e) {
				e.printStackTrace();
			}
	}
	
	/**
	 * Indique au DatabaseManager que l'import d'une base de données est terminé
	 * @param type Type de la base de données importées
	 */
	public static void importFinished(DatasManager.Type type){
		Logging.logger().info("La base de type "+type.toString()+" a changée.");
		instance.support.firePropertyChange(BASE_SELECTED, null, type);
	}
	
	/**
	 * Indique au DatabaseManager que l'import d'une base de données est terminé
	 * @param type Type de la base de données importées
	 */
	public static void importFinished(String type){
		DatabaseManager.importFinished(stringToType(type));
	}
	
	/**
	 * Convertit un type sous forme de string en Type
	 * @param type Chaine de caractères à convertir
	 * @return Le Type correspondant
	 */
	public static DatasManager.Type stringToType(String type){
		if(type.equalsIgnoreCase("STIP")) {
			return DatasManager.Type.STIP;
		} else if(type.equalsIgnoreCase("PAYS")){
			return DatasManager.Type.PAYS;
		} else if(type.equalsIgnoreCase("STPV")){
			return DatasManager.Type.STPV;
		} else if(type.equalsIgnoreCase("EXSA")){
			return DatasManager.Type.EXSA;
		} else if(type.equalsIgnoreCase("Edimap")){
			return DatasManager.Type.Edimap;
		} else if(type.equalsIgnoreCase("Ods")){
			return DatasManager.Type.Ods;
		} else if(type.equalsIgnoreCase("RadioCov")){
			return DatasManager.Type.RadioCov;
		} else if(type.equalsIgnoreCase("SkyView")){
			return DatasManager.Type.SkyView;
		} else if(type.equalsIgnoreCase("AIP")){
			return DatasManager.Type.AIP;
		} //else if(type.equalsIgnoreCase("KML")){
//				return Type.KML;
//		}		
		return null;
	}
	
	/**
	 * Get a list of all displayables objects
	 * TODO move this in each controller
	 * @param type
	 * @return liste des éléments visibles
	 * @throws SQLException
	 */
	public static List<ItemCouple> getAllVisibleObjects(DatasManager.Type type, Omnibox omnibox) throws SQLException{
		List<ItemCouple> items = new LinkedList<ItemCouple>();
		VidesoController controller = DatasManager.getController(type);
		Statement st;
		ResultSet rs;
		switch (type) {
		case STIP:	
			st = DatabaseManager.getCurrentStip();
			if(st != null){
				rs = st.executeQuery("select name, publicated from balises");
				while(rs.next()){
					items.add(new ItemCouple(controller, new Couple<Integer,String>(StipController.BALISES, rs.getString(1))));
				}
				rs = st.executeQuery("select name from routes");
				while(rs.next()){
					items.add(new ItemCouple(controller, new Couple<Integer,String>(StipController.ROUTES, rs.getString(1))));
				}
				rs = st.executeQuery("select nom from secteurs");
				while(rs.next()){
					items.add(new ItemCouple(controller, new Couple<Integer,String>(StipController.SECTEUR, rs.getString(1))));
				}
			}
			return items;
		case AIP:
			st = DatabaseManager.getCurrentAIP();
			if(st != null){
				rs = st.executeQuery("select nom, type from volumes " +
								"UNION select nom, type from routes " +
								"UNION select nom, type from NavFix " +
								"UNION select nom, type from aerodromes");
				HashSet<String> CTLset = new HashSet<String>();
				while(rs.next()){
					try{
					int typeInt = AIP.string2type(rs.getString(2));
					String nom = rs.getString(1);
					if(typeInt == AIP.AERODROME){
						PreparedStatement ps = DatabaseManager.prepareStatement(DatasManager.Type.AIP, "select code from aerodromes where nom=?");
						ps.setString(1, nom);
						ResultSet rs2 = ps.executeQuery();
						items.add(new ItemCouple(controller, new Couple<Integer,String>(typeInt, rs2.getString(1)+" -- "+nom)));
					}else if(typeInt == AIP.CTL){
						nom = nom.split(" ")[0].trim();
						if(!CTLset.contains(nom)){
							items.add(new ItemCouple(controller, new Couple<Integer,String>(typeInt, nom)));	
							CTLset.add(nom);
						}
					}else{
						items.add(new ItemCouple(controller, new Couple<Integer,String>(typeInt, nom)));
					}
					}catch(Exception e){
						e.printStackTrace();
					}
				}
			}
			return items;
		case Edimap:
			st = DatabaseManager.getCurrentEdimap();
			if(st != null){
				rs = st.executeQuery("select name, type from cartes");
				while(rs.next()) {
					items.add(new ItemCouple(controller, new Couple<Integer,String>(Cartes.string2type(rs.getString(2)), rs.getString(1))));
				}
			}
			return items;
		case SkyView:
			st = DatabaseManager.getCurrentSkyView();
			if(st != null){
				rs = st.executeQuery("select ident from waypoint");
				while(rs.next()){
					items.add(new ItemCouple(controller, new Couple<Integer,String>(SkyViewController.TYPE_WAYPOINT, rs.getString(1))));
				}
				rs = st.executeQuery("select ident from airport");
				while(rs.next()){
					items.add(new ItemCouple(controller, new Couple<Integer,String>(SkyViewController.TYPE_AIRPORT, rs.getString(1))));
				}
				rs = st.executeQuery("select distinct ident from airway");
				while(rs.next()){
					items.add(new ItemCouple(controller, new Couple<Integer,String>(SkyViewController.TYPE_ROUTE, rs.getString(1))));
				}
			}
			return items;
		default:
			return null;
		}
	}


	/**
	 * Returns all selected databases but PAYS
	 * @return liste des bases de données sélectionnées
	 */
	public static List<DatasManager.Type> getSelectedDatabases(){
		List<DatasManager.Type> bases = new ArrayList<DatasManager.Type>();
		try {
			Statement st = DatabaseManager.getCurrent(DatasManager.Type.Databases);
			ResultSet rs = st.executeQuery("select type from databases where selected ='1'");
			while(rs.next()){
				DatasManager.Type t = DatabaseManager.stringToType(rs.getString(1));
				if(! t.equals(DatasManager.Type.PAYS)) bases.add(DatabaseManager.stringToType(rs.getString(1)));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return bases;
	}

	/**
	 * Ajoute un commentaire à la base
	 * @param id Id de la base concernée
	 * @param comment Commentaire à ajouter
	 */
	public static void setComment(int id, String comment){
		try {
			Statement st = DatabaseManager.getCurrent(DatasManager.Type.Databases);
			st.executeUpdate("update databases set commentaire = '"+comment+"' where id ='"+id+"'");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	/* ****************************************************** */
	/* *************** Gestion des listeners **************** */
	/* ****************************************************** */
	
	public static void addPropertyChangeListener(PropertyChangeListener l){
		instance.support.addPropertyChangeListener(l);
	}
	
	public static void addPropertyChangeListener(String propertyName, PropertyChangeListener l){
		instance.support.addPropertyChangeListener(propertyName, l);
	}
	
	public static void removePropertyChangeListener(PropertyChangeListener l){
		instance.support.removePropertyChangeListener(l);
	}
	
	public static void removePropertyChangeListener(String propertyName, PropertyChangeListener l){
		instance.support.removePropertyChangeListener(propertyName, l);
	}
	
	public static void fireBaseSelected(DatasManager.Type type){
		instance.support.firePropertyChange(BASE_SELECTED, null, type);
	}
}
