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
package fr.crnan.videso3d.edimap;

import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.FileParser;

/**
 * Jeu de cartes Edimap
 * @author Bruno Spyckerelle
 * @version 0.4
 */
public class Cartes extends FileParser{
	
	/**
	 * Nombre de fichiers lus
	 */
	private int numberFiles = 6;
	/**
	 * Ensemble des cartes dynamiques
	 */
	private List<Entity> cartesDynamiques;
	/**
	 * Ensemble des cartes statiques
	 */
	private List<Entity> cartesStatiques;
	/**
	 * Enemble des cartes secteurs
	 */
	private List<Entity> secteurs;
	/**
	 * Date de génération du jeu de cartes
	 */
	private String date;
	/**
	 * Version du jeu de cartes
	 */
	private String version;
	/**
	 * Nom du fichier carac_jeu
	 */
	private String carac_jeu;
	/**
	 * Palette de couleurs
	 */
	private PaletteEdimap palette;
	/**
	 * Chemin vers le répertoire contenant les cartes
	 */
	private String path;
	
	private Connection conn;
	
	/**
	 * Liste des cartes crées
	 */
	HashMap<String, Carte> cartes = new HashMap<String, Carte>();

	
	/**
	 * Récupère les données du fichier carac_jeu
	 * @param path String Chemin vers le fichier carac_jeu
	 * @throws FileNotFoundException 
	 */
	public Cartes(String path) throws FileNotFoundException{
		NectarReader cartes = new NectarReader();
		try {
			cartes = new NectarReader(path);
			cartes.doInBackground();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw e;
		}
		cartesDynamiques = (List<Entity>) cartes.getEntity().getValues("dynamique");
		cartesStatiques = (List<Entity>) cartes.getEntity().getValues("statique");
		secteurs = (List<Entity>) cartes.getEntity().getValues("secteur");
		date = cartes.getEntity().getValue("date");
		version = cartes.getEntity().getValue("name");
	}

	/**
	 * Récupère les données du fichier carac_jeu et les stocke en base de données
	 * @param carac_jeu String Chemin vers le fichier carac_jeu
	 * @param db Gestionnaire de base de données
	 */
	public Cartes(String absoluteDirPath, String carac_jeu) {
		this.path = absoluteDirPath;
		this.carac_jeu = carac_jeu;
	}

	/**
	 * Récupère les données des cartes en base de données
	 */
	public Cartes(){
		this.cartesDynamiques = new LinkedList<Entity>();
		this.cartesStatiques = new LinkedList<Entity>();		
		this.secteurs = new LinkedList<Entity>();
		try {
			//TODO prendre en compte la possibilité qu'il n'y ait pas de bdd Edimap
			Statement edimapDB = DatabaseManager.getCurrentEdimap();
			if(edimapDB != null){
				ResultSet rs = edimapDB.executeQuery("select * from cartes where type = 'dynamique' order by name");
				while(rs.next()){
					List<Entity> values = new LinkedList<Entity>();
					values.add(new Entity("name", rs.getString("name")));
					values.add(new Entity("fichierActive", rs.getString("fichier")));
					Entity carte = new Entity("dynamique", values);
					this.cartesDynamiques.add(carte);
				}
				rs = edimapDB.executeQuery("select * from cartes where type = 'statique' order by name");
				while(rs.next()){
					List<Entity> values = new LinkedList<Entity>();
					values.add(new Entity("name", rs.getString("name")));
					values.add(new Entity("fichier", rs.getString("fichier")));
					Entity carte = new Entity("statique", values);
					this.cartesStatiques.add(carte);
				}
				rs = edimapDB.executeQuery("select * from cartes where type = 'secteur' order by name");
				while(rs.next()){
					List<Entity> values = new LinkedList<Entity>();
					values.add(new Entity("name", rs.getString("name")));
					values.add(new Entity("fichierSousControle", rs.getString("fichier")));
					Entity carte = new Entity("secteur", values);
					this.secteurs.add(carte);
				}
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	

	@Override
	protected void getFromFiles() {
		try {
			this.conn = DatabaseManager.selectDB(DatabaseManager.Type.Edimap, this.version);
			this.conn.setAutoCommit(false); //fixes performance issue
			if(!DatabaseManager.databaseExists(this.version)){
				DatabaseManager.createEdimap(this.version, this.path);
				this.insertCartes();
				try {
					this.conn.commit();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public Integer doInBackground() {
		this.setFile("carac_jeu");
		this.setProgress(0);
		NectarReader cartes = new NectarReader();
		try {
			cartes.setPath(this.path+"/"+this.carac_jeu);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		cartes.doInBackground();
		this.setFile("cartes dynamiques");
		this.setProgress(1);
		cartesDynamiques = (List<Entity>) cartes.getEntity().getValues("dynamique");
		this.setFile("cartes statiques");
		this.setProgress(2);
		cartesStatiques = (List<Entity>) cartes.getEntity().getValues("statique");
		this.setFile("cartes secteur");
		this.setProgress(3);
		secteurs = (List<Entity>) cartes.getEntity().getValues("secteur");
		date = cartes.getEntity().getValue("date");
		version = cartes.getEntity().getValue("name");
		this.setFile("palette de couleur");
		this.setProgress(4);
		this.setPalette();
		this.setFile("Insertion en base de données");
		this.setProgress(5);
		this.getFromFiles();
		this.setProgress(6);
		this.firePropertyChange("done", false, true);
		return this.numberFiles();
		}
	
	/**
	 * Insère les données en base
	 * @throws DatabaseError
	 */
	private void insertCartes() throws SQLException{
		//insertion des cartes en base de données
		PreparedStatement insert = this.conn.prepareStatement("insert into cartes (name, type, fichier) values " +
				"(?, ?, ?)");
		Iterator<Entity> iterator = this.getCartesDynamiques().iterator();
		while(iterator.hasNext()){
			Entity carte = iterator.next();
			insert.setString(2, "dynamique");
			insert.setString(1, carte.getValue("name"));
			insert.setString(3, carte.getValue("fichierActive"));
			insert.executeUpdate();
		}
		iterator = this.getCartesStatiques().iterator();
		while(iterator.hasNext()){
			Entity carte = iterator.next();
			insert.setString(2, "statique");
			insert.setString(1, carte.getValue("name"));
			insert.setString(3, carte.getValue("fichier"));
			insert.executeUpdate();
		}
		iterator = this.getSecteurs().iterator();
		while(iterator.hasNext()){
			Entity carte = iterator.next();
			insert.setString(2, "secteur");
			insert.setString(1, carte.getValue("name"));
			insert.setString(3, carte.getValue("fichierSousControle"));
			insert.executeUpdate();
		}
	}
	
	/**
	 * Renvoit la carte correspondante
	 * @param name Nom de la carte
	 * @return {@link Carte}
	 * @throws SQLException
	 * @throws FileNotFoundException
	 */
	public Carte getCarte(String name) throws SQLException, FileNotFoundException{
		if(cartes.containsKey(name)) {
			return cartes.get(name);
		} else {
			Statement st = DatabaseManager.getCurrent(DatabaseManager.Type.Databases);
			ResultSet rs = st.executeQuery("select * from clefs where name='path' and type='"+DatabaseManager.getCurrentName(DatabaseManager.Type.Edimap)+"'");
			if(rs.next()){
				this.path = rs.getString(4);
			} 
			st = DatabaseManager.getCurrentEdimap();
			rs = st.executeQuery("select * from cartes where name='"+name+"'");
			rs.next();
			String cartePath = this.path + "/"+ rs.getString(4) + ".NCT"; //TODO gérer l'extension
			NectarReader carte = new NectarReader();
			try {
				carte = new NectarReader(cartePath);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				throw e;
			}
			//		this.setProgress(carte.getProgress());
			carte.doInBackground();
			Carte c = new Carte(carte.getEntity(), this.getPalette());
			cartes.put(name, c);
			return c;
		}
	}
	
	
	private void setPalette(){
		NectarReader paletteFichier = new NectarReader();
		try {
			paletteFichier = new NectarReader(this.path+"/palette");
			paletteFichier.doInBackground();
			this.palette = new PaletteEdimap(paletteFichier.getEntity());
		} catch (FileNotFoundException e) {
			this.palette = new PaletteEdimap();
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Palette de couleurs
	 * @return PaletteEdimap
	 */
	public PaletteEdimap getPalette() {	
		if(palette == null){
			this.setPalette();
		}
		return this.palette;
	}

	//Getters
	public List<Entity> getCartesDynamiques() {
		return cartesDynamiques;
	}

	public List<Entity> getCartesStatiques() {
		return cartesStatiques;
	}

	public List<Entity> getSecteurs() {
		return secteurs;
	}

	public String getDate() {
		return date;
	}

	public String getVersion() {
		return version;
	}

	@Override
	public int numberFiles() {
		return this.numberFiles;
	}

	@Override
	public void done() {
		if(this.isCancelled()){
			
		}
	}

	
}
