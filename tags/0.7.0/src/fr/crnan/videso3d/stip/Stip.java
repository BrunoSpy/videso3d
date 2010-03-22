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

package fr.crnan.videso3d.stip;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import fr.crnan.videso3d.Couple;
import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.FileParser;
import fr.crnan.videso3d.DatabaseManager.Type;

/**
 * Lecteur de fichiers STIP
 * Toutes les infos concernant les fichiers SATIN sont dans le DDI Satin
 * @author Bruno Spyckerelle
 * @version 0.3
 */
public class Stip extends FileParser{

	/**
	 * Type Route
	 */
	public final static String STIP_ROUTE = "stip.route";
	/**
	 * Type Balise
	 */
	public final static String STIP_BALISE = "stip.balise";
	/**
	 * Type Route
	 */
	public final static String STIP_SECTEUR = "stip.secteur";
	
	/**
	 * Nombre de fichiers gérés
	 */
	private int numberFiles = 13;

	/**
	 * Version des fichiers Stip
	 * = Version CA + Livraison
	 */
	private String name;

	/**
	 * Connection à la base de données
	 */
	private Connection conn;

	/**
	 * Table des balises, nécessaire pour accélerer l'import afin de ne pas faire des requêtes à chaque insertion de balise
	 */
	private HashMap<String, Integer> balises = new HashMap<String, Integer>();
	
	public Stip(){
		super();
	}

	public Stip(String path) {
		super(path);
	}

	@Override
	public Integer doInBackground() {
		//récupération du nom de la base à créer
		this.getName();
		try {
			//création de la connection à la base de données
			this.conn = DatabaseManager.selectDB(Type.STIP, this.name);
			this.conn.setAutoCommit(false); //fixes performance issue
			if(!DatabaseManager.databaseExists(this.name)){
				//création de la structure de la base de données
				DatabaseManager.createSTIP(this.name);
				//parsing des fichiers et stockage en base
				this.getFromFiles();
				//table d'association traj->iti
				this.setProgress(12);
				this.setFile("COUPLES ITI");
				this.insertCoupleBalItis();

				//	this.insertTrajIti(); //Table d'association trajet->iti, pas forcément utile et long à générer
				try {
					this.conn.commit();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				this.setProgress(this.numberFiles());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e){
			e.printStackTrace();
		}
		return this.numberFiles;
	}

	@Override
	public void done(){
		if(this.isCancelled()){//si le parsing a été annulé, on fait le ménage
			try {
				DatabaseManager.deleteDatabase(name, Type.STIP);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		firePropertyChange("done", false, true);
	}

	/**
	 * Forge le nom de la base de données
	 * = date_CA.date_livraison
	 */
	private void getName(){
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(this.path + "/REF")));
			String line = in.readLine();
			this.name = line.substring(33,41) + "." + line.substring(55,63);
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}


	}
	@Override
	protected void getFromFiles() {
		this.setFile("CENTRE");
		this.setProgress(0);
		this.setCentre(this.path + "/CENTRE");
		this.setProgress(1);
		this.setFile("SECT");
		this.setSecteur(this.path + "/SECT");
		this.setProgress(2);
		this.setFile("balise");
		this.setBalise(this.path);
		this.setProgress(3);
		this.setFile("POINSECT");
		this.setPoinSect(this.path + "/POINSECT");
		this.setProgress(4);
		this.setFile("ROUTSECT");
		this.setRoutSect(this.path + "/ROUTSECT");
		this.setProgress(5);
		this.setFile("ROUTE");
		this.setRoute(this.path + "/ROUTE");
		this.setProgress(6);
		this.setFile("ITI");
		this.setItis(this.path + "/ITI");
		this.setProgress(7);
		this.setFile("LIEUX");
		this.setLieux(this.path + "/LIEUX");
		this.setProgress(8);
		this.setFile("TRAJET");
		this.setTrajets(this.path+ "/TRAJET");
		this.setProgress(9);
		this.setFile("BALINT");
		this.setBalInt(this.path+ "/BALINT");
		this.setProgress(10);
		this.setFile("CONNEXION");
		this.setConnexion(this.path+"/CONNEX");
		this.setProgress(11);
	}

	/**
	 * Lecteur de fichier CONNEX
	 * @param string
	 */
	private void setConnexion(String path) {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
			in.readLine(); //suppression de la première ligne FORMAT
			while(in.ready()){
				String line = in.readLine();
				if(line.length() >= 30)  
					this.insertConnexion(new Connexion(line));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void insertConnexion(Connexion connexion) throws SQLException {
		PreparedStatement insert = this.conn.prepareStatement("insert into connexions (terrain, type, perfo, flinf, flsup, vitessesigne, vitesse) " +
		"values (?, ?, ?, ?, ?, ?, ?)");
		insert.setString(1, connexion.getTerrain());
		insert.setString(2, connexion.getType());
		insert.setString(3, connexion.getPerfo());
		insert.setInt(4, connexion.getFlinf());
		insert.setInt(5, connexion.getFlsup());
		insert.setString(6, connexion.getVitesseCompar());
		insert.setInt(7, connexion.getVitesseValue());
		insert.executeUpdate();
		int id = insert.getGeneratedKeys().getInt(1);
		insert = this.conn.prepareStatement("insert into balconnexions (idconn, balise, balid, appartient) values (?, ?, ?, ?)");
		insert.setInt(1, id);
		for(Couple<String, Boolean> b : connexion.getBalises()){
			insert.setString(2, b.getFirst());
			insert.setInt(3, balises.get(b.getFirst()));
			insert.setBoolean(4, b.getSecond());
			insert.addBatch();
		}
		insert.executeBatch();
		insert.close();
	}

	/**
	 * Lecteur de fichier BalInt
	 * @param path Chemin vers le fichier
	 */
	private void setBalInt(String path){
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
			in.readLine(); //suppression de la première ligne FORMAT
			while(in.ready()){
				String line = in.readLine();
				if(line.length() >= 30)  
					this.insertBalInt(new BalInt(line));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void insertBalInt(BalInt balInt) throws SQLException {
		PreparedStatement insert = this.conn.prepareStatement("insert into balint (fir, uir, bal1, bal2, balise, appartient) " +
		"values (?, ?, ?, ?, ?, ?)");
		insert.setBoolean(1, balInt.getFir());
		insert.setBoolean(2, balInt.getUir());
		insert.setString(3, balInt.getBalise1());
		insert.setString(4, balInt.getBalise2());
		insert.setString(5, balInt.getBalise());
		insert.setBoolean(6, balInt.getAppartient());
		insert.executeUpdate();
	}

	/**
	 * Lecteur de fichiers TRAJET
	 * @param path Chemin du fichier
	 */
	private void setTrajets(String path) {
		Trajet trajet = null;
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
			in.readLine(); //suppression de la première ligne FORMAT
			while(in.ready()){
				String line = in.readLine();
				if(line.length() > 12 && !line.substring(7, 12).trim().isEmpty()) { //carte 1
					if(trajet != null) this.insertTrajet(trajet); //insertion du trajet en base de données
					trajet = new Trajet(line);
				} else if(line.length()>30) { //cartes 2
					trajet.addBalises(line);
				}
			}
			if(trajet != null) this.insertTrajet(trajet);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void insertTrajet(Trajet trajet) {
		try {
			PreparedStatement insert = this.conn.prepareStatement("insert into trajets (eclatement, eclatement_id, raccordement, raccordement_id, type, fl, cond1, etat1, cond2, etat2, cond3, etat3, cond4, etat4) " +
			"values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			insert.setString(1, trajet.getEclatement());
			insert.setInt(2, balises.get(trajet.getEclatement()));
			insert.setString(3, trajet.getRaccordement());
			insert.setInt(4, balises.get(trajet.getRaccordement()));
			insert.setString(5, trajet.getType());
			insert.setInt(6, trajet.getFl());
			int i = 0;
			for(Couple<String, String> condition : trajet.getConditions()){
				insert.setString(7+i, condition.getFirst());
				insert.setString(8+i, condition.getSecond());
				i+=2;;
			}
			insert.executeUpdate();
			int id = insert.getGeneratedKeys().getInt(1);
			insert = this.conn.prepareStatement("insert into baltrajets (idtrajet, balise, balid, appartient) " +
			"values (?, ?, ?, ?)");
			insert.setInt(1, id);
			for(Couple<String, Boolean> balise : trajet.getBalises()){
				insert.setString(2, balise.getFirst());
				insert.setInt(3, balises.get(balise.getFirst()));
				insert.setBoolean(4, balise.getSecond());
				insert.addBatch();
			}
			insert.executeBatch();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Lecteur de fichier LIEUX<br />
	 * Les cartes * ne sont pas traitées, leur signification m'étant inconnue
	 * @param path
	 */
	private void setLieux(String path) {
		Lieux lieux = null;
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
			while(in.ready()){
				String line = in.readLine();
				if(line.startsWith("1")){
					if(lieux != null) this.insertLieux(lieux);
					lieux = new Lieux(line);
				} else if (line.startsWith("M") || line.startsWith("D") || line.startsWith("R")){
					lieux.addCarte(line);
				}
			}
			if(lieux != null) this.insertLieux(lieux);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void insertLieux(Lieux lieux) {
		try {
			PreparedStatement insert = this.conn.prepareStatement("insert into lieux (oaci, type, centre1, distance1, pp1, nc1, centre2, distance2, pp2, nc2, centre3, distance3, pp3, nc3, centre4, distance4, pp4, nc4) " +
			"values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			insert.setString(1, lieux.getOaci());
			insert.setString(2, lieux.getDistanceType());
			for(int i = 0; i< 4; i++) {
				insert.setString(3+i, lieux.getCentre()[i].toString());
				insert.setInt(4+i, lieux.getDistance()[i]);
				insert.setString(5+i, lieux.getPp()[i]);
				insert.setString(6+i, lieux.getNc()[i]);
			}
			insert.executeUpdate();
			int id = insert.getGeneratedKeys().getInt(1);
			insert = this.conn.prepareStatement("insert into consignes (idlieu, type, oaci, balise, niveau, ecart, eve, act, mod, base) " +
			"values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			insert.setInt(1, id);
			Iterator<Consigne> iterator = lieux.getConsignes().iterator();
			while(iterator.hasNext()){
				Consigne c = iterator.next();
				insert.setString(2, c.getType().toString());
				insert.setString(3, c.getOaci());
				insert.setString(4, c.getBalise());
				insert.setInt(5, c.getNiveau());
				insert.setInt(6, c.getEcart());
				insert.setBoolean(7, c.getEveil());
				insert.setBoolean(8, c.getAct());
				insert.setBoolean(9, c.getMod());
				insert.setInt(10, c.getBase());
				insert.addBatch();
			}
			insert.executeBatch();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Lecteur de fichier ITI
	 * @param path
	 */
	private void setItis(String path){
		Iti iti = null;
		String line = "";
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
			in.readLine(); //suppression de la première ligne
			while (in.ready()){
				line = in.readLine();
				//reconnaissance d'une carte 2
				if(line.length()> 27 && line.startsWith("                       ")){
					//enregistrement de l'iti précédent
					iti.addBalises(line);
				} else if (line.length() >= 35 ){
					if(iti != null)
						try {
							this.insertIti(iti);
						} catch (SQLException e) {
							e.printStackTrace();
						}
						iti = new Iti(line);
				}
			}
		} catch (FileNotFoundException e1){
			//TODO faire une fenêtre d'avertissement
			e1.printStackTrace();
		}catch (IOException e1) {
			e1.printStackTrace();
		} 
		//insertion du dernier iti créé
		if(iti != null){
			try {
				this.insertIti(iti);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	private void insertIti(Iti iti) throws SQLException {
		PreparedStatement insert = this.conn.prepareStatement("insert into itis (entree, sortie, flinf, flsup) " +
		"values (?, ?, ?, ?)");
		insert.setString(1, iti.getEntree());
		insert.setString(2, iti.getSortie());
		insert.setInt(3, iti.getFlinf());
		insert.setInt(4, iti.getFlsup());
		insert.executeUpdate();
		insert.close();
		int id = insert.getGeneratedKeys().getInt(1);
		insert = this.conn.prepareStatement("insert into balitis (iditi, balid, balise, appartient) " +
		"values (?, ?, ?, ?)");
		Iterator<Couple<String, Boolean>> iterator = iti.getBalises().iterator();
		insert.setInt(1, id);
		Statement st = this.conn.createStatement();
		while(iterator.hasNext()){
			Couple<String, Boolean> balise = iterator.next();
			insert.setInt(2, balises.get(balise.getFirst()));
			insert.setString(3, balise.getFirst());
			insert.setBoolean(4, balise.getSecond());
			insert.addBatch();
		}
		insert.executeBatch();
		insert.close();
		st.close();
	}

	/**
	 * Lecteur de fichier Route
	 * @param path Chemin vers le fichier ROUTE
	 */
	private void setRoute(String path) {
		String name = "";
		Route route = null;
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
			while (in.ready()){
				String line = in.readLine();
				if(!line.startsWith("FORMAT") && line.length()>9){
					if(line.substring(7, 9).compareTo("RO") == 0){//TODO traiter les lignes ES
						if(line.substring(0, 7).trim().compareTo(name) == 0) {
							route.addBalises(line.substring(15, 80));
						} else {
							//nouvelle route on insère la route précédente avant d'en créer une nouvelle
							if(route != null) {
								try {
									this.insertRoute(route);
								} catch (SQLException e) {
									e.printStackTrace();
								}
							}
							route = new Route(line);
							name = route.getName();
						}
					}
				}
			}
		} catch (FileNotFoundException e1){
			//TODO faire une fenêtre d'avertissement
			e1.printStackTrace();
		}catch (IOException e1) {
			e1.printStackTrace();
		} 
		//insertion de la dernière route
		if(route != null) {
			try {
				this.insertRoute(route);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * Insertion d'une route en base de données
	 * @param route
	 * @throws SQLException 
	 */
	private void insertRoute(Route route) throws SQLException{
		PreparedStatement insert = this.conn.prepareStatement("insert into routes (name, espace) " +
		"values (?, ?)");
		//insertion des données dans la table route

		insert.setString(1, route.getName());
		insert.setString(2, route.getEspace());
		insert.executeUpdate();
		insert.close();
		int id = insert.getGeneratedKeys().getInt(1);
		//puis insertion des balises
		insert = this.conn.prepareStatement("insert into routebalise (route, routeid, balise, appartient, sens) " +
		"values (?, ?, ?, ?, ?)");
		Iterator<Couple<String, Boolean>> balises = route.getBalises().iterator();
		Iterator<String> sens = route.getSens().iterator();
		insert.setString(1, route.getName());
		insert.setInt(2, id);
		while(balises.hasNext()){
			Couple<String, Boolean> balise = balises.next();
			String sensTr;
			if(sens.hasNext()){
				sensTr = sens.next();
			} else {
				//dernière balise de la liste : sans objet
				sensTr = "";
			}
			insert.setString(3, balise.getFirst());
			insert.setBoolean(4, balise.getSecond());
			insert.setString(5, sensTr);
			insert.addBatch();
		}
		insert.executeBatch();
		insert.close();
	}

	/**
	 * Lecteur de fichier ROUTSECT
	 * @param path Chemin vers le fichier ROUTSECT
	 */
	private void setRoutSect(String path) {
		Integer sectNum = -1;
		CarteSecteur carteSect = null;
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
			while (in.ready()){
				String line = in.readLine();
				if(line.startsWith("SRT")){
					sectNum = new Integer(line.substring(17, 21));
				} else if(line.startsWith("NIV")) {
					carteSect = new CarteSecteur(sectNum, line);
				} else if(line.startsWith("ETI")){
					carteSect.addEtiquette(line);
					try {
						this.insertCarteSect(carteSect);
					} catch (SQLException e) {
						e.printStackTrace();
					}
				} else if(line.startsWith("PT")){
					try {
						if(line.startsWith("PTF")){
							this.insertCartePoint(new CartePoint(sectNum, carteSect.getFlsup(), line), line.substring(11, 16).trim());
						} else {
							this.insertCartePoint(new CartePoint(sectNum, carteSect.getFlsup(), line), "");
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		} catch (FileNotFoundException e1){
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	private void insertCarteSect(CarteSecteur carteSect) throws SQLException{
		PreparedStatement insert = this.conn.prepareStatement("insert into cartesect (sectnum, flinf, flsup, lateti, longeti) " +
		"values (?, ?, ?, ?, ?)");
		insert.setInt(1, carteSect.getSectnum());
		insert.setInt(2, carteSect.getFlinf());
		insert.setInt(3, carteSect.getFlsup());
		insert.setString(4, carteSect.getLateti().toString());
		insert.setString(5, carteSect.getLongeti().toString());
		insert.executeUpdate();
		insert.close();
	}

	private void insertCartePoint(CartePoint cartePoint, String contour) throws SQLException{
		PreparedStatement insert = this.conn.prepareStatement("insert into cartepoint (sectnum, flsup, pointref, refcontour) " +
		"values (?, ?, ?, ?)");
		insert.setInt(1, cartePoint.getSectnum());
		insert.setInt(2, cartePoint.getFlsup());
		insert.setString(3, cartePoint.getPointRef());
		insert.setString(4, contour);
		insert.executeUpdate();
		insert.close();
	}

	/**
	 * Lecteur de fichier POINSECT
	 * @param path Chemin vers le fichier POINSECT
	 */
	private void setPoinSect(String path) {
		try {
			PreparedStatement insert = this.conn.prepareStatement("insert into poinsect (ref, latitude, longitude) " +
			"values (?, ?, ?)");
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
			while (in.ready()){
				String line = in.readLine();
				if(line.startsWith("PTS")){
					PoinSect point = new PoinSect(line); 
					try {
						this.insertPoinSect(point, insert);
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
			insert.executeBatch();
			insert.close();
		} catch (FileNotFoundException e1){
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Insère dans la table PoinSect
	 * @param point une ligne du fichier PoinSect
	 * @throws SQLException 
	 */
	private void insertPoinSect(PoinSect point, PreparedStatement insert) throws SQLException{
		insert.setString(1, point.getReference());
		insert.setDouble(2, point.getLatitude().toDecimal());
		insert.setDouble(3, point.getLongitude().toDecimal());
		insert.addBatch();
	}


	/**
	 * Lecteur de fichier SECT
	 * @param path chemin vers le fichier SECT
	 */
	private void setSecteur(String path) {
		try {
			PreparedStatement insert = this.conn.prepareStatement("insert into secteurs (nom, centre, espace, numero, flinf, flsup, modes) " +
			"values (?, ?, ?, ?, ?, ?, ?)");
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
			while (in.ready()){
				String line = in.readLine();
				if(!line.startsWith("FORMAT") && (line.length()>20)){
					if(!line.substring(4, 8).equalsIgnoreCase("SCAG") && !line.substring(20, 43).equalsIgnoreCase("* SECTEUR NON UTILISE *")){
						SecteurLigne secteur = new SecteurLigne(line);
						this.insertSecteur(secteur, insert);
					}
				}
			}
			insert.executeBatch();
			insert.close();
		} catch (FileNotFoundException e1){
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Insère dans la table secteurs
	 * @param secteur une ligne du fichier SECT
	 * @throws SQLException 
	 */
	private void insertSecteur(SecteurLigne secteur, PreparedStatement insert) throws SQLException {
		insert.setString(1, secteur.getNom());
		insert.setString(2, secteur.getCentre());
		insert.setString(3, secteur.getEspace());
		insert.setInt(4, secteur.getNumero());
		insert.setInt(5, secteur.getFlinf());
		insert.setInt(6, secteur.getFlsup());
		insert.setBoolean(7, secteur.getModeS());
		insert.addBatch();
	}

	/**
	 * Lecteur de fichier CENTRE
	 * @param path chemin vers le répertoire contenant le fichier Satin
	 */
	private void setCentre(String path) {
		try {
			PreparedStatement insert = this.conn.prepareStatement("insert into centres (name, identite, numero, type) " +
			"values (?, ?, ?, ?)");
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
			while (in.ready()){
				String line = in.readLine();
				//TODO gérer un peu mieux la dernière ligne
				if(!line.startsWith("FORMAT") && (line.length()>20)){ //on gère la première et la dernière ligne
					Centre centre = new Centre(line);
					this.insertCentre(centre, insert);
				}
			}
			insert.executeBatch();
			insert.close();
		}catch (SQLException e) {
			e.printStackTrace();
		}catch (FileNotFoundException e1){
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Insère dans la table centres 
	 * @param centre une ligne du fichier CENTRE
	 * @throws SQLException 
	 */
	private void insertCentre(Centre centre, PreparedStatement insert) throws SQLException {
		insert.setString(1, centre.getNom());
		insert.setString(2, centre.getIdentite());
		insert.setInt(3, centre.getNumero());
		insert.setString(4, centre.getType());
		insert.addBatch();	
	}

	/**
	 * Lecteur de fichier balise
	 * @param path Chemin vers le fichier balise
	 */
	private void setBalise(String path) {
		String balisePath = path;
		Boolean precision = false;
		if(new File(path+"/BALISEP").exists()){
			balisePath += "/BALISEP";
			precision = true;
		} else if(new File(path+"/balisep").exists()){
			balisePath += "/balisep";
			precision = true;
		} else if(new File(path+"/BALISE").exists()){
			balisePath += "/BALISE";
		} else if(new File(path+"/balise").exists()){
			balisePath += "/balise";
		}
		try {
			PreparedStatement insert = this.conn.prepareStatement("insert into balises (name, publicated, latitude, longitude, centre, definition, sccag, sect1, limit1, sect2, limit2, sect3, limit3, sect4, limit4, sect5, limit5, sect6, limit6, sect7, limit7, sect8, limit8, sect9, limit9) " +
			"values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(balisePath)));
			Balise balise = new Balise(precision);
			while (in.ready()){
				String line = in.readLine();
				if (line.startsWith("1")) {
					//la balise est vide lors du premier passage
					if(balise.getIndicatif() != null) {//alors on stocke la balise précédente
						this.insertBalise(balise, insert);
					}
					balise = new Balise(line, precision);
				} else if (line.startsWith("2")) {
					balise.setLigne2(line);
				} else if (line.startsWith("3") && !line.startsWith("31") && !line.startsWith("32") ) {
					balise.setLigne3(line);
				} else if (line.startsWith("31") && !line.startsWith("32")) {
					balise.addLigne3(line);
				} else if (line.startsWith("32")) {
					balise.addLigne3(line);
				}
			}
			//on n'oublie pas de stocker la dernière balise lue
			if(balise != null) {
				this.insertBalise(balise, insert);
			}
			insert.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}

	/**
	 * Insère une balise en base de données
	 * @param balise
	 * @throws SQLException 
	 */
	private void insertBalise(Balise balise, PreparedStatement insert) throws SQLException {
		insert.setString(1, balise.getIndicatif());
		insert.setBoolean(2, balise.getPublication());
		insert.setDouble(3, balise.getLatitude().toDecimal());
		insert.setDouble(4, balise.getLongitude().toDecimal());
		insert.setString(5, balise.getCentre());
		insert.setString(6, balise.getDefinition());
		insert.setString(7, balise.getSccag());
		LinkedList<Couple<String, Integer>> secteurs = balise.getSecteurs();
		Iterator<Couple<String, Integer>> iterator = secteurs.listIterator();
		Integer compteur = 8;
		while(iterator.hasNext()){
			Couple<String, Integer> secteur = (Couple<String, Integer>) iterator.next();
			insert.setString(compteur, secteur.getFirst());
			compteur++;
			insert.setInt(compteur, secteur.getSecond());
			compteur++;
		}
		//on remplit les derniers champs secteurs
		for (Integer i = compteur; i<=25; i++){
			insert.setString(i, "");
			i++;
			insert.setInt(i, -1);
		}
		insert.executeUpdate();
		int id = insert.getGeneratedKeys().getInt(1);
		balises.put(balise.getIndicatif(), id);
		
	}

	@SuppressWarnings("unused")
	private void insertTrajIti() throws SQLException {
		Statement st = this.conn.createStatement();
		ResultSet rs = st.executeQuery("select id, eclatement, raccordement from trajets");
		LinkedList<ArrayList<Object>> trajets = new LinkedList<ArrayList<Object>>();
		while(rs.next()){
			ArrayList<Object> trajet = new ArrayList<Object>();
			trajet.add(rs.getInt(1));
			trajet.add(rs.getString(2));
			trajet.add(rs.getString(3));
			trajets.add(trajet);
		}
		List<Couple<Integer, Integer>> table = new LinkedList<Couple<Integer,Integer>>();
		for(ArrayList<Object> trajet : trajets){
			String eclatement = (String) trajet.get(1);
			String raccordement = (String) trajet.get(2);
			//tous les itis ayant les deux balises qui se succèdent dans le bon ordre
			rs = st.executeQuery("SELECT *,COUNT(*) as count, sum(balitis.id) as total from itis, balitis "+
					"WHERE itis.id = balitis.iditi and (balitis.balise='"+eclatement+"'  or balitis.balise = '"+raccordement+"') "+
					"GROUP BY iditi HAVING count = 2 and total = 2*balitis.id -1 and balise = '"+raccordement+"'");
			while(rs.next()) 
				table.add(new Couple<Integer, Integer>((Integer) trajet.get(0), rs.getInt(1)));
		}
		st.close();
		//enregistrement
		PreparedStatement insert = this.conn.prepareStatement("insert into trajiti (idtraj, iditi) values (?, ?)");
		for(Couple<Integer, Integer> c : table){
			insert.setInt(1, c.getFirst());
			insert.setInt(2, c.getSecond());
			insert.addBatch();
		}
		insert.executeBatch();
		insert.close();
	}

	private void insertCoupleBalItis() throws SQLException{
		Statement st = this.conn.createStatement();
		ResultSet rs = st.executeQuery("select iditi, balise, balid from balitis");
		PreparedStatement insert = this.conn.prepareStatement("insert into couplebalitis (iditi, idbal1, idbal2, bal1, bal2) values (?, ?, ?, ?, ?)");
		int idIti = 0;
		String first = "";
		int firstId = 0;
		while(rs.next()){
			if(idIti != rs.getInt(1)){
				//nouvel iti
				idIti = rs.getInt(1);
				first = rs.getString(2);
				firstId = rs.getInt(3);
			} else {
				String second = rs.getString(2);
				int secondId = rs.getInt(3);
				insert.setInt(1, idIti);
				insert.setInt(2, firstId);
				insert.setInt(3, secondId);
				insert.setString(4, first);
				insert.setString(5, second);
				insert.addBatch();
				first = second;
				firstId = secondId;
			}
		}
		insert.executeBatch();
		insert.close();
		//et enfin, on crée les index et la table adéquate, toujours pour des raisons de performances
		st.executeUpdate("create index idx_trajets on trajets (eclatement_id, raccordement_id)");
		st.executeUpdate("create index idx_couples on couplebalitis (idbal1, idbal2)");
		st.executeUpdate("create index idx_baltrajets on baltrajets (idtrajet)");
		st.executeUpdate("create index idx_balitis on balitis (iditi)");
		st.executeUpdate("create index idx_balitis2 on balitis (balise)");
		st.executeUpdate("create table couple_trajets as select couplebalitis.*, trajets.id as trajetid, trajets.*  from couplebalitis, trajets where eclatement_id = idbal1 and raccordement_id = idbal2");
		//et on supprime la table intermédiaire
//		st.executeUpdate("drop index idx_couples");
//		st.executeUpdate("drop table couplebalitis");
		st.close();
		
	}

	@Override
	public int numberFiles() {
		return this.numberFiles;
	}

	/**
	 * Détermine le type de l'objet en fonction de son nom
	 * @param name Nom de l'objet
	 * @return Type de l'objet
	 */
	public static String getTypeFromName(String name){
		try {
			Statement st = DatabaseManager.getCurrentStip();
			ResultSet rs = st.executeQuery("select name from routes where name = '"+name+"'");
			if(rs.next()){
				return STIP_ROUTE;
			}
			rs = st.executeQuery("select * from secteurs where nom = '"+name+"'");
			if(rs.next()){
				return STIP_SECTEUR;
			}
			rs = st.executeQuery("select * from balises where name = '"+name+"'");
			if(rs.next()){
				return STIP_BALISE;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;
	}
	
}
