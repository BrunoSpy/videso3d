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
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.FileParser;

/**
 * Jeu de cartes Edimap
 * @author Bruno Spyckerelle
 * @version 0.3
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
	
	private boolean cancel = false;
	
	public Cartes(){
		super();
	}
	
	/**
	 * Récupère les données du fichier carac_jeu
	 * @param path String Chemin vers le fichier carac_jeu
	 * @throws FileNotFoundException 
	 */
	public Cartes(String path) throws FileNotFoundException{
		NectarReader cartes = new NectarReader();
		try {
			cartes = new NectarReader(path);
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
	public Cartes(String absoluteDirPath, String carac_jeu, DatabaseManager db) {
		this.path = absoluteDirPath;
		this.db = db;
		this.carac_jeu = carac_jeu;
	}

	/**
	 * Récupère les données des cartes en base de données
	 * @param db Gestionnaire de base de données
	 */
	public Cartes(DatabaseManager db){
		this.db = db;
		this.cartesDynamiques = new LinkedList<Entity>();
		this.cartesStatiques = new LinkedList<Entity>();		
		this.secteurs = new LinkedList<Entity>();
		try {
			//TODO prendre en compte la possibilité qu'il n'y ait pas de bdd Edimap
			QSqlDatabase edimapDB = this.db.getCurrentEdimap();
			if(edimapDB != null){
				QSqlQuery query = new QSqlQuery(this.db.getCurrentEdimap());
				query.exec("select * from cartes where type = 'dynamique'");
				while(query.next()){
					List<Entity> values = new LinkedList<Entity>();
					values.add(new Entity("name", query.value(1).toString()));
					values.add(new Entity("fichierActive", query.value(3).toString()));
					Entity carte = new Entity("dynamique", values);
					this.cartesDynamiques.add(carte);
				}
				query.exec("select * from cartes where type = 'statique'");
				while(query.next()){
					List<Entity> values = new LinkedList<Entity>();
					values.add(new Entity("name", query.value(1).toString()));
					values.add(new Entity("fichier", query.value(3).toString()));
					Entity carte = new Entity("statique", values);
					this.cartesStatiques.add(carte);
				}
				query.exec("select * from cartes where type = 'secteur'");
				while(query.next()){
					List<Entity> values = new LinkedList<Entity>();
					values.add(new Entity("name", query.value(1).toString()));
					values.add(new Entity("fichierSousControle", query.value(3).toString()));
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
		this.db.connectDB(this.version);
		try {
			if(!this.db.databaseExists(this.version)){
				this.db.createEdimap(this.version, this.path);
				this.insertCartes();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Pas de gestion d'annulation d'import
	 */
	public void cancel(){
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
		cartes.run();
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
		QSqlQuery insert = new QSqlQuery(this.db.selectDB(this.version));
		insert.prepare("insert into cartes (name, type, fichier) values " +
				"(:name, :type, :fichier)");
		Iterator<Entity> iterator = this.getCartesDynamiques().iterator();
		while(iterator.hasNext()){
			Entity carte = iterator.next();
			insert.bindValue(":type", "dynamique");
			insert.bindValue(":name", carte.getValue("name"));
			insert.bindValue("fichier", carte.getValue("fichierActive"));
			if(!insert.exec()){
				throw new DatabaseError(insert.lastError());
			}
		}
		iterator = this.getCartesStatiques().iterator();
		while(iterator.hasNext()){
			Entity carte = iterator.next();
			insert.bindValue(":type", "statique");
			insert.bindValue(":name", carte.getValue("name"));
			insert.bindValue("fichier", carte.getValue("fichier"));
			if(!insert.exec()){
				throw new DatabaseError(insert.lastError());
			}
		}
		iterator = this.getSecteurs().iterator();
		while(iterator.hasNext()){
			Entity carte = iterator.next();
			insert.bindValue(":type", "secteur");
			insert.bindValue(":name", carte.getValue("name"));
			insert.bindValue("fichier", carte.getValue("fichierSousControle"));
			if(!insert.exec()){
				throw new DatabaseError(insert.lastError());
			}
		}
	}
	
	public Carte getCarte(String name) throws SQLException, FileNotFoundException{
		QSqlQuery query = new QSqlQuery(this.db.selectDB("default"));
		if(!query.exec("select * from clefs where name='path' and type='"+this.db.getCurrentName("Edimap")+"'")){
			throw new DatabaseError(query.lastError());
		}
		query.next();
		this.path = query.value(3).toString();
		query = new QSqlQuery(this.db.getCurrentEdimap());
		if(!query.exec("select * from cartes where name='"+name+"'")){
			throw new DatabaseError(query.lastError());
		}
		query.next();
		String cartePath = this.path + "/"+ query.value(3).toString() + ".NCT"; //TODO gérer l'extension
		NectarReader carte = new NectarReader();
		try {
			carte = new NectarReader(cartePath);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw e;
		}
		carte.percentage.connect(this.percentage);
		carte.run();
//		QThread thread = new QThread(carte);
//		thread.run();
		return new Carte(carte.getEntity(), this.getPalette());
	}
	
	
	private void setPalette(){
		NectarReader paletteFichier = new NectarReader();
		try {
			paletteFichier = new NectarReader(this.path+"/palette");
			paletteFichier.run();
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
