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
package fr.crnan.videso3d.databases.pays;

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
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


import fr.crnan.videso3d.DatasManager;
import fr.crnan.videso3d.DatasManager.Type;
import fr.crnan.videso3d.FileParser;
import fr.crnan.videso3d.databases.DatabaseManager;
import gov.nasa.worldwind.geom.LatLon;
/**
 * Lecteur des fichiers STIP relatifs aux contours des pays.<br />
 * Ces fichiers nécessitent un traitement spécial car ils peuvent ne pas être distribués avec les autres fichiers CA lors d'une livraison par le CESNAC.<br />
 * Il s'agit des fichiers PAYS, CONTPAYS, et POINPAYS
 * @author Bruno Spyckerelle
 * @version 0.2.6
 */
public class Pays extends FileParser {

	/**
	 * Nombre de fichiers gérés
	 */
	private int numberFiles = 3;
	
	private final String[] fileNames = {"PAYS", "CONTPAYS", "POINPAYS"};
	
	/**
	 * Version des fichiers Stip PAYS
	 * = PAYS + Version CA
	 */
	private String name;

	/**
	 * Connection à la base de données correspondante
	 */
	private Connection conn;	

	public Pays(){
		super();
	}
	
	public Pays(String path){
		super(path);
	}
	
	public static boolean containsPaysFiles(Collection<File> files) {
		Iterator<File> iterator = files.iterator();
		boolean found = false;
		while(iterator.hasNext() && !found){
			String name = iterator.next().getName();
			found = name.equalsIgnoreCase("PAYS");
		}
		return found;
	}
	
	@Override
	protected void getFromFiles() throws SQLException, IOException {
		this.setProgress(0);
		this.setFile("PAYS");
		this.setPays(this.path + "/PAYS");
		this.setProgress(1);
		this.setFile("CONTPAYS");
		this.setContPays(this.path + "/CONTPAYS");
		this.setProgress(2);
		this.setFile("POINPAYS");
		this.setPoinPays(this.path + "/POINPAYS");
		this.setProgress(3);
	}

	/**
	 * Lecteur de fichier POINPAYS
	 * @param path Chemin vers le fichier POINPAYS
	 * @throws SQLException 
	 * @throws IOException 
	 */
	private void setPoinPays(String path) throws SQLException, IOException {
		PreparedStatement insert = this.conn.prepareStatement("insert into poinpays (ref, latitude, longitude) " +
		"values (?, ?, ?)");
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
			while (in.ready()){
				String line = in.readLine();
				if(line.startsWith("PTP")){
					PoinPays point = new PoinPays(line); 
					this.insertPoinPays(point, insert);
				}
			}
		} catch (IOException e){
			//handle the exception at a higher level
			throw e;
		} finally {
			if(in != null)
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		insert.executeBatch();
		insert.close();
	}
	/**
	 * Insère dans la table PoinPays
	 * @param point une ligne du fichier PoinPays
	 * @throws SQLException 
	 */
	private void insertPoinPays(PoinPays point, PreparedStatement insert) throws SQLException{
		insert.setString(1, point.getReference());
		insert.setDouble(2, point.getLatitude().toDecimal());
		insert.setDouble(3, point.getLongitude().toDecimal());
		insert.addBatch();
	}

	/**
	 * Lecteur de fichier CONTPAYS
	 * @param path Chemin vers le fichier CONTPAYS
	 * @throws SQLException 
	 * @throws IOException 
	 */
	private void setContPays(String path) throws SQLException, IOException {
		PreparedStatement insert = this.conn.prepareStatement("insert into contpays (refcontour, refpoint) " +
		"values (?, ?)");
		BufferedReader in = null;
		try{
			in = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
			String contName = new String();
			while (in.ready()){
				String line = in.readLine();
				if(line.startsWith("CTR")){
					contName = line.substring(4,9).trim();
				} else if(line.startsWith("PTC")){
					this.insertContPays(contName, line.substring(4, 10).trim(), insert);
				}
			}
		}  catch (IOException e){
			//handle the exception at a higher level
			throw e;
		} finally {
			if(in != null)
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		insert.executeBatch();
		insert.close();
	}
	/**
	 * INsère une ligne dans la table contpays.
	 * @param contName Nom du contour
	 * @param point Nom du point
	 * @throws SQLException 
	 */
	private void insertContPays(String contName, String point, PreparedStatement insert) throws SQLException{
		insert.setString(1, contName);
		insert.setString(2, point);
		insert.addBatch();
	}
	/**
	 * Lecteur de fichier Pays
	 * @param path Chemin vers le fichier PAYS
	 * @throws SQLException 
	 * @throws IOException 
	 */
	private void setPays(String path) throws SQLException, IOException {
		PreparedStatement insert = this.conn.prepareStatement("insert into pays (pays, contour, refcontour) " +
				"values (?, ?, ?)");
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(new FileInputStream(path)));

			String pays = new String();
			String contName = new String();
			while (in.ready()){
				String line = in.readLine();
				if(line.startsWith("PAY")){
					pays = line.substring(4, 34).trim();
					contName = line.substring(35, 65).trim();
				} else if(line.startsWith("CTR")){
					this.insertPays(pays, contName, line.substring(4, 9).trim(), insert);
				}
			}
		} catch (IOException e){
			//handle the exception at a higher level
			throw e;
		} finally {
			if(in != null)
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		insert.executeBatch();
		insert.close();
	}
	/**
	 * INsère une ligne dans la table pays
	 * @param pays Nom du pays
	 * @param contName Nom du contour
	 * @param refcontour référence du contour
	 * @throws SQLException 
	 */
	private void insertPays(String pays, String contName, String refcontour, PreparedStatement insert) throws SQLException{
		insert.setString(1, pays);
		insert.setString(2, contName);
		insert.setString(3, refcontour);
		insert.addBatch();
	}

	@Override
	public int numberFiles() {
		return this.numberFiles;
	}

	@Override
	public Integer doInBackground() {
		try {
			//récupération du nom de la base à créer
			this.createName();
			if(!DatabaseManager.databaseExists(DatasManager.Type.PAYS, this.name)){
				this.conn = DatabaseManager.selectDB(DatasManager.Type.PAYS, this.name);
				this.conn.setAutoCommit(false);
				//création de la structure de la base de données
				DatabaseManager.createPays(this.name);
				//parsing des fichiers et stockage en base
				this.getFromFiles();
				this.createIndexes();
				this.conn.commit();
			} else {
				DatabaseManager.selectDatabase(this.name, DatasManager.Type.PAYS);
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
	
	private void createIndexes() throws SQLException {
		Statement st = this.conn.createStatement();
		st.executeUpdate("CREATE INDEX idx_contpays ON contpays (refpoint ASC)");
		st.executeUpdate("CREATE UNIQUE INDEX idx_refpoint ON poinpays (ref ASC)");	
		st.close();
	}

	public void done(){
		if(this.isCancelled()){
			try {
				DatabaseManager.deleteDatabase(name, DatasManager.Type.PAYS);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			firePropertyChange("done", true, false);
		} else {
			firePropertyChange("done", false, true);
		}
	}
	
	/**
	 * Forge le nom de la base de données
	 * = PAYS.date_CA
	 * @throws IOException 
	 */
	private void createName() {
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(new FileInputStream(this.path + "/PAYS")));
			String line = in.readLine();
			this.name = "PAYS." + line.substring(33,41);
		}  catch (IOException e){
			e.printStackTrace();
		} finally {
			if(in != null)
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	/**
	 * Renvoit la portion de contour comprise entre point1 et point2.\n
	 * La liste des points est ordonnée de façon à ce que point1 soit le premier point du sous-contour.
	 * @param contour String Nom du contour
	 * @param point1 String Référence du premier point
	 * @param point2 String Référence du deuxième point
	 * @param st Statement Statement sur une base de données Pays 
	 * @return List<LatLon> Liste de points Lat/Lon représentant le sous-contour
	 */
	public static List<LatLon> getContour(String contour, String point1, String point2, Statement st) {
		List<LatLon> polygon = new LinkedList<LatLon>();
		Integer pointF1 = new Integer(point1.substring(1));
		Integer pointF2 = new Integer(point2.substring(1));
		try {
			ResultSet rs = st.executeQuery("select refcontour, refpoint, latitude, longitude from contpays, poinpays where poinpays.ref = contpays.refpoint and contpays.refcontour = '" + contour + "'");
			//on enregistre d'abord les resultats dans une liste car le driver SQlite est pourri et ne permet pas de faire des mouvements de curseur ...
			ArrayList<String> refs = new ArrayList<String>();
			ArrayList<Double> latitudes = new ArrayList<Double>();
			ArrayList<Double> longitudes = new ArrayList<Double>();
			int i = 0;
			while(rs.next()){
				refs.add(i, rs.getString(2));
				latitudes.add(i, rs.getDouble(3));
				longitudes.add(i, rs.getDouble(4));
				i++;
			}
			rs.close();
			boolean point1Found = false;
			boolean point2Found = false;
			int point1R = 0; //position du point1 dans la table des résultats
			int point2R = 0;

			i = 0;
			while(i<refs.size() && (!point1Found  || !point2Found)) {		
				Integer point = new Integer(refs.get(i).substring(1));
				if(pointF1.compareTo(point) == 0 && !point1Found ) {
					point1Found = true;
					point1R = i;
				}
				if(pointF2.compareTo(point) == 0 && !point2Found ) {
					point2Found = true;
					point2R = i;
				}
				i++;
			}
			//puis on parcours le contour toujours en commençant par le premier point fourni
			//jusqu'au deuxième point fourni, en remontant ou redescendant les résultats selon le cas
			if(point1R < point2R){
				for(int j=point1R; j<= point2R; j++){
					polygon.add(LatLon.fromDegrees(latitudes.get(j), longitudes.get(j)));
				}

			} else {
				for(int j=point1R; j>= point2R; j--){
					polygon.add(LatLon.fromDegrees(latitudes.get(j), longitudes.get(j)));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return polygon;
	}
	/**
	 * Renvoit la fin du contour à partir de point1
	 * @param contour String Nom du contour
	 * @param point1 String Référence du premier point
	 * @param st Statement
	 * @return List<LatLon> Liste de points représentant le sous-contour
	 */
	public static List<LatLon> getContour(String contour, String point1, Statement st) {
		List<LatLon> polygon = new LinkedList<LatLon>();
		Integer pointF1 = new Integer(point1.substring(1));
		try {
			ResultSet rs = st.executeQuery("select refcontour, refpoint, latitude, longitude from contpays, poinpays where poinpays.ref = contpays.refpoint and contpays.refcontour = '" + contour + "'");
			while(rs.next()) {
				Integer point = new Integer((rs.getString(2)).substring(1));
				if(point>= pointF1){
					polygon.add(LatLon.fromDegrees(rs.getDouble(3), rs.getDouble(4)));
				}
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return polygon;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public DatasManager.Type getType() {
		return DatasManager.Type.PAYS;
	}
	
	@Override
	public List<String> getRelevantFileNames() {
		return Arrays.asList(this.fileNames);
	}

}
