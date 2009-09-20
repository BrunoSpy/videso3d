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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;

import fr.crnan.videso3d.Couple;
import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.FileParser;
import fr.crnan.videso3d.DatabaseManager.Type;

/**
 * Lecteur de fichiers STIP
 * Toutes les infos concernant les fichiers SATIN sont dans le DDI Satin
 * @author Bruno Spyckerelle
 * @version 0.2
 */
public class Stip extends FileParser{

	/**
	 * Nombre de fichiers gérés
	 */
	private int numberFiles = 6;
	
	/**
	 * Version des fichiers Stip
	 * = Version CA + Livraison
	 */
	private String name;

	/**
	 * Connection à la base de données
	 */
	private Connection conn;
	


	public Stip(){
		super();
	}
	
	public Stip(String path, DatabaseManager db) {
		super(path, db);
	}

	@Override
	public Integer doInBackground() {
		//récupération du nom de la base à créer
		this.getName();
		try {
			//création de la connection à la base de données
			this.conn = this.db.selectDB(Type.STIP, this.name);
			if(!this.db.databaseExists(this.name)){
				//création de la structure de la base de données
				this.db.createSTIP(this.name);
				//parsing des fichiers et stockage en base
				this.getFromFiles();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return this.numberFiles;
	}
	@Override
	public void done(){
		if(this.isCancelled()){//si le parsing a été annulé, on fait le ménage
			try {
				this.db.deleteDatabase(name, Type.STIP);
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
	
	protected void getFromFiles() {

		this.setFile("CENTRE");
		this.setProgress(0);
		this.setCentre(this.path + "/CENTRE");
		this.setProgress(1);
		this.setFile("SECT");
		this.setSecteur(this.path + "/SECT");
		this.setProgress(2);
		this.setFile("balise");
		this.setBalise(this.path + "/balise");
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
		//puis insertion des balises
		insert = this.conn.prepareStatement("insert into routebalise (route, balise, appartient, sens) " +
				"values (?, ?, ?, ?)");
		Iterator<Couple<String, Boolean>> balises = route.getBalises().iterator();
		Iterator<String> sens = route.getSens().iterator();
		while(balises.hasNext()){
			Couple<String, Boolean> balise = balises.next();
			String sensTr;
			if(sens.hasNext()){
				sensTr = sens.next();
			} else {
				//dernière balise de la liste : sans objet
				sensTr = "";
			}
			insert.setString(1, route.getName());
			insert.setString(2, balise.getFirst());
			insert.setBoolean(3, balise.getSecond());
			insert.setString(4, sensTr);
			insert.executeUpdate();
		}
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
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
			while (in.ready()){
				String line = in.readLine();
				if(line.startsWith("PTS")){
					PoinSect point = new PoinSect(line); 
					try {
						this.insertPoinSect(point);
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		} catch (FileNotFoundException e1){
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Insère dans la table PoinSect
	 * @param point une ligne du fichier PoinSect
	 * @throws SQLException 
	 */
	private void insertPoinSect(PoinSect point) throws SQLException{
		PreparedStatement insert = this.conn.prepareStatement("insert into poinsect (ref, latitude, longitude) " +
		"values (?, ?, ?)");
		insert.setString(1, point.getReference());
		insert.setDouble(2, point.getLatitude().toDecimal());
		insert.setDouble(3, point.getLongitude().toDecimal());
		insert.executeUpdate();
		insert.close();
	}
	

	/**
	 * Lecteur de fichier SECT
	 * @param path chemin vers le fichier SECT
	 */
	private void setSecteur(String path) {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
			while (in.ready()){
				String line = in.readLine();
				if(!line.startsWith("FORMAT") && (line.length()>20)){
					if(!line.substring(4, 8).equalsIgnoreCase("SCAG") && !line.substring(20, 43).equalsIgnoreCase("* SECTEUR NON UTILISE *")){
						SecteurLigne secteur = new SecteurLigne(line);
						try {
							this.insertSecteur(secteur);
						} catch (SQLException e) {
							e.printStackTrace();
						}
					}
				}
			}
		} catch (FileNotFoundException e1){
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Insère dans la table secteurs
	 * @param secteur une ligne du fichier SECT
	 * @throws SQLException 
	 */
	private void insertSecteur(SecteurLigne secteur) throws SQLException {
		PreparedStatement insert = this.conn.prepareStatement("insert into secteurs (nom, centre, espace, numero, flinf, flsup, modes) " +
		"values (?, ?, ?, ?, ?, ?, ?)");
		insert.setString(1, secteur.getNom());
		insert.setString(2, secteur.getCentre());
		insert.setString(3, secteur.getEspace());
		insert.setInt(4, secteur.getNumero());
		insert.setInt(5, secteur.getFlinf());
		insert.setInt(6, secteur.getFlsup());
		insert.setBoolean(7, secteur.getModeS());
		insert.executeUpdate();
		insert.close();
	}

	/**
	 * Lecteur de fichier CENTRE
	 * @param path chemin vers le répertoire contenant le fichier Satin
	 */
	private void setCentre(String path) {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
			while (in.ready()){
				String line = in.readLine();
				//TODO gérer un peu mieux la dernière ligne
				if(!line.startsWith("FORMAT") && (line.length()>20)){ //on gère la première et la dernière ligne
					Centre centre = new Centre(line);
					this.insertCentre(centre);
				}
			}
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
	private void insertCentre(Centre centre) throws SQLException {
		PreparedStatement insert = this.conn.prepareStatement("insert into centres (name, identite, numero, type) " +
				"values (?, ?, ?, ?)");
		insert.setString(1, centre.getNom());
		insert.setString(2, centre.getIdentite());
		insert.setInt(3, centre.getNumero());
		insert.setString(4, centre.getType());
		insert.executeUpdate();
		insert.close();		
	}

	/**
	 * Lecteur de fichier balise
	 * @param path Chemin vers le fichier balise
	 */
	private void setBalise(String path) {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
			Balise balise = new Balise();
			while (in.ready()){
				String line = in.readLine();
				if (line.startsWith("1")) {
					//la balise est vide lors du premier passage
					if(balise.getIndicatif() != null) {//alors on stocke la balise précédente
							this.insertBalise(balise);
					}
					balise = new Balise(line);
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
				this.insertBalise(balise);
			}
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
	private void insertBalise(Balise balise) throws SQLException {
		PreparedStatement insert = this.conn.prepareStatement("insert into balises (name, publicated, latitude, longitude, centre, definition, sccag, sect1, limit1, sect2, limit2, sect3, limit3, sect4, limit4, sect5, limit5, sect6, limit6, sect7, limit7, sect8, limit8, sect9, limit9) " +
	       "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
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
		insert.close();
	}

	@Override
	protected int numberFiles() {
		return this.numberFiles;
	}

}
