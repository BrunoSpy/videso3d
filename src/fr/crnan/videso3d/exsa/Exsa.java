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

package fr.crnan.videso3d.exsa;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;

import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.FileParser;
import fr.crnan.videso3d.DatabaseManager.Type;
import gov.nasa.worldwind.util.Logging;

/**
 * Lecteur de fichiers EXSA<br />
 * Détecte automatiquement le type de fichier (formaté ou non).
 * @author Bruno Spyckerelle
 * @version 0.4.2
 */
public class Exsa extends FileParser {
	/**
	 * Type de fichier
	 * Non formaté par défaut
	 */
	private Boolean formated = false;
	/**
	 * Nom de la base de données EXSA (CARA_GENER->NOM DU FICHIER)
	 */
	protected String name;
	/**
	 * Connection à la base de données
	 */
	private Connection conn;
	
	private PreparedStatement insert;
	
	public Exsa() {
		super();
	}

	/**
	 * Construit la base de données à partir du fichier path 
	 * @param path Chemin vers le fichier
	 * @param db Gestionnaire de bdd
	 */
	public Exsa(String path) {
		super(path);
	}
	

	@Override
	public Integer doInBackground(){
		try {
			//on récupère le nom de la base de données
			this.getName();
			//on crée la connection avec bdd avec ce nom
			this.conn = DatabaseManager.selectDB(Type.EXSA, this.name);
			this.conn.setAutoCommit(false);
			if(!DatabaseManager.databaseExists(this.name)){
				//puis la structure de la base de donnée
				DatabaseManager.createEXSA(this.name);
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
		} catch (ParseException e) {
			e.printStackTrace();
			this.cancel(true);
		}
		return this.numberFiles();
	}
	
	@Override
	public void done(){
		if(this.isCancelled()){
			try {
				DatabaseManager.deleteDatabase(name, Type.EXSA);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			firePropertyChange("done", true, false);
		}  else {
			firePropertyChange("done", false, true);
		}
	}
	
	/**
	 * Récupère le nom de la base de données EXSA
	 * @throws IOException 
	 */
	protected void getName() throws IOException {
		Boolean nameFound = false;

		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(this.path)));
		while (in.ready() && !nameFound){
			String line = in.readLine();
			if (line.startsWith("CARA_GENER")){
				//on prend le premier mot de la ligne
				this.name = line.split("\\s+")[1];
				nameFound = true;
				formated = true;
			} else if (line.startsWith("CARA.GENER")){
				this.name = line.split(",")[2];
				nameFound = true;
			}
		}
		in.close();

		if(!nameFound){
			Logging.logger().warning("Fichier EXSA invalide");
			throw new FileNotFoundException("Fichier EXSA non lisible ou incorrect");
		}
	}

	/**
	 * Récupère les données d'un fichier EXSA
	 * @throws SQLException 
	 * @throws ParseException 
	 * @throws IOException 
	 */
	protected void getFromFiles() throws SQLException, IOException, ParseException{
		String line = "";
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(this.path)));
		while (in.ready()){
			line = in.readLine();
			if (line.startsWith(formated ? "CARA_GENER" : "CARA.GENER")){
				this.setFile("CARA_GENER");
				this.setProgress(0);
				insert = this.conn.prepareStatement("INSERT INTO caraGener (name, date, jeu, type, oasis, boa, videomap, edimap, satin, calcu, contexte) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
				this.setCaraGener(line, formated);
			} /*else if (line.startsWith("CARA_STRMV")) {

				} else if (line.startsWith("CARA_CALCU")) {

				} else if (line.startsWith("CARA_PMIXT")) {

				} */else if (line.startsWith(formated ? "CENT_CENTR" : "CENT.CENTR")) {
					this.setFile("CENT_CENTR.");
					this.setProgress(1);
					insert = this.conn.prepareStatement("insert into centcentr (name, sl, type, str, plafondmsaw, rvsm, plancherrvsm, plafondrvsm, typedonnees, versiondonnees)" +
					" values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
					this.setCentCentr(line, formated);
				} else if (line.startsWith(formated ? "CENT_MOSAI" : "CENT.MOSAI")) {
					this.setFile("CENT_CENTR.");
					this.setProgress(2);
					insert = this.conn.prepareStatement("insert into centmosai (latitude, longitude, xcautra, ycautra, lignes, colonnes, type) " +
					"values (?, ?, ?, ?, ?, ?, ?)");
					this.setCentMosai(line, formated);
				} /*else if (line.startsWith("CENT_V_V_F")) {

				} else if (line.startsWith("CENT_Z_ARR")) {

				} else if (line.startsWith("CENT_Z_DEP")) {

				} else if (line.startsWith("CENT_SPACE")) {

				} */else if (line.startsWith(formated ? "CENT_FLVVF" : "CENT.FLVVF")) {
					this.setFile("CENT_FLVVF.");
					this.setProgress(3);
					insert = this.conn.prepareStatement("insert into centflvvf (name) " +
					"values (?)");
					this.setCentFlvvf(line, formated);
				} else if (line.startsWith(formated ? "CENT_STACK" : "CENT.STACK")) {
					this.setFile("CENT_STACK");
					this.setProgress(4);
					insert = this.conn.prepareStatement("insert into centflvvf (name, latitude, longitude, xcautra, ycautra, rayonint, rayonext, flinf, flsup, type) " +
					"values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
					this.setCentStack(line, formated);
				} /*else if (line.startsWith("CENT_TMA-F")) {

				}*/ else if (line.startsWith(formated ? "CENT_Z_OCC" : "CENT.Z_OCC")) {
					this.setFile("CENT_Z_OCC.");
					this.setProgress(4);
					insert = this.conn.prepareStatement("insert into centzocc (name, espace, terrains) " +
					"values (?, ?, ?)");
					this.setCentZOcc(line, formated);
				} /*else if (line.startsWith("CENT_ZONES")) {

				} */else if (line.startsWith(formated ? "CENT_SCVVF" : "CENT.SCVVF")) {
					this.setFile("CENT_SCVVF.");
					this.setProgress(5);
					insert = this.conn.prepareStatement("insert into centscvvf (carre, souscarre, vvfs, plafonds, planchers) " +
					"values (?, ?, ?, ?, ?)");
					this.setCentScvvf(line, formated);
				}else if (line.startsWith(formated ? "CENT_SCODF" : "CENT.SCODF")){
					this.setFile("CENT_SCODF");
					this.setProgress(6);
					insert = this.conn.prepareStatement("insert into centscodf (vvf, debut, fin, espaces) " +
					"values (?, ?, ?, ?)");
					this.setCentSCodf(line, formated);
				}/* else if (line.startsWith("CENT_SCTMA")) {

				}*/ else if (line.startsWith(formated ? "CENT_SCZOC" : "CENT.SCZOC")) {
					this.setFile("CENT_SCZOC.");
					this.setProgress(7);
					insert = this.conn.prepareStatement("insert into centsczoc (carre, souscarre, zone, plafond) " +
					"values (?, ?, ?, ?)");
					this.setCentSczoc(line, formated);
					insert.executeBatch();
				} /*else if (line.startsWith("CENT_ARVSM")) {

				} else if (line.startsWith("CENT_CRVSM")) {

				} else if (line.startsWith("CENT_ZONEV")) {

				} else if (line.startsWith("CENT_ZPOUR")) {

				} else if (line.startsWith("CENT_SCODR")) {

				} else if (line.startsWith("CENT_SCODE")) {

				} else if (line.startsWith("CENT_CODED")) {

				} else if (line.startsWith("CENT_AEROP")) {

				} else if (line.startsWith("CENT_SEUIL")) {

				} else if (line.startsWith("CENT_FIVIS")) {

				} else if (line.startsWith("CENT_CIMIL")) {

				} else if (line.startsWith("COMM_LIGNE")) {

				} else if (line.startsWith("COMM_OLSGT")) {

				} else if (line.startsWith("COMM_LICOM")) {

				} else if (line.startsWith("COMM_ADLGQ")) {

				} else if (line.startsWith("COMM_PNTAC")) {

				} else if (line.startsWith("COMM_IPRES")) {

				} else if (line.startsWith("COMM_A_GLT")) {

				} else if (line.startsWith("COMM_ASX25")) {

				} else if (line.startsWith("COMM_FSPEC")) {

				} else if (line.startsWith("COMM_FILTR")) {

				} else if (line.startsWith("COMM_MODAD")) {

				} else if (line.startsWith("COMM_TMAMU")) {

				}*/ else if (line.startsWith(formated ? "RADR_GENER" : "RADR.GENER")) {
					this.setFile("RADR_GENER.");
					this.setProgress(8);
					insert = this.conn.prepareStatement("insert into radrgener (name, numero, type, nommosaique, latitude, longitude, xcautra, ycautra, " +
							"ecart, radarrelation, typerelation, typeplots, typeradar, codepays, coderadar, militaire) " +
					"values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )");
					this.setRadrGener(line, formated);
				} else if (line.startsWith(formated ? "RADR_TECHN" : "RADR.TECHN")) {
					this.setFile("RADR_TECHN");
					this.setProgress(9);
					insert = this.conn.prepareStatement("insert into radrtechn (name, vitesse, hauteur, portee, deport) " +
					"values (?, ?, ?, ?, ?)");
					this.setRadrTechn(line, formated);
				} /*else if (line.startsWith("RADR_CALAG")) {

				} else if (line.startsWith("RADR_INITL")) {

				} else if (line.startsWith("RADR_LICOM")) {

				} else if (line.startsWith("RADR_PELSA")) {

				} else if (line.startsWith("VISU_CHAIN")) {

				} else if (line.startsWith("VISU_POSTE")) {

				} else if (line.startsWith("VISU_PARAM")) {

				} else if (line.startsWith("VISU_BUF_P")) {

				} else if (line.startsWith("VISU_BALIS")) {

				} else if (line.startsWith("VISU_VERGS")) {

				} else if (line.startsWith("VISU_FRVSM")) {

				} else if (line.startsWith("VISU_SELEC")) {

				} else if (line.startsWith("CTRL_ECRAN")) {

				} else if (line.startsWith("CTRL_TTCOD")) {

				} else if (line.startsWith("CTRL_ROUTE")) {

				} else if (line.startsWith("CTRL_POSIT")) {

				} else if (line.startsWith("CTRL_ARRSP")) {

				} else if (line.startsWith("CTRL_SECVO")) {

				} else if (line.startsWith("CTRL_ACTRL")) {

				} else if (line.startsWith("CTRL_OLVIS")) {

				} else if (line.startsWith("CTRL_F_GST")) {

				} else if (line.startsWith("CTRL_CLVIS")) {

				} else if (line.startsWith("CTRL_CLVOL")) {

				} else if (line.startsWith("CTRL_CIE_A")) {

				} else if (line.startsWith("CTRL_COMPA")) {

				} else if (line.startsWith("CTRL_C_STA")) {

				} else if (line.startsWith("CTRL_B_CAR")) {

				} else if (line.startsWith("CTRL_C_DYN")) {

				} else if (line.startsWith("CTRL_O_DYN")) {

				} else if (line.startsWith("CTRL_ALSTR")) {

				} else if (line.startsWith("CTRL_ASSOC")) {

				} else if (line.startsWith("CTRL_CONFI")) {

				} else if (line.startsWith("CTRL_S_TDC")) {

				} else if (line.startsWith("CTRL_ETIQU")) {

				} else if (line.startsWith("CTRL_LEADE")) {

				} else if (line.startsWith("CTRL_SEC_U")) {

				} else if (line.startsWith("CTRL_COD_S")) {

				} else if (line.startsWith("ALAR_ALARM")) {

				} else if (line.startsWith("ALAR_S_PER")) {

				} else if (line.startsWith("SUPR_EOS_I")) {

				} else if (line.startsWith("SUPR_EOS_E")) {

				} else if (line.startsWith("SUPR_CDEOS")) {

				} else if (line.startsWith("SUPR_P_EOS")) {

				} else if (line.startsWith("SUPR_S_EOS")) {

				} else if (line.startsWith("SUPR_C_EOS")) {

				} else if (line.startsWith("SUPR_CDGDS")) {

				} else if (line.startsWith("SUPR_CDGDS")) {

				} else if (line.startsWith("SUPR_P_GDS")) {

				} else if (line.startsWith("SUPR_S_GDS")) {

				} else if (line.startsWith("SUPR_C_GDS")) {

				} else if (line.startsWith("SUPR_FONCT")) {

				} else if (line.startsWith("SUPR_ARTAS")) {

				} else if (line.startsWith("SUPR_PARSP")) {

				} else if (line.startsWith("SUPR_I_PHI")) {

				} else if (line.startsWith("SUPR_AC_ME")) {

				} else if (line.startsWith("SUPR_D_T_L")) {

				} else if (line.startsWith("SUPR_MODIM")) {

				} else if (line.startsWith("SUPR_C_PHI")) {

				} else if (line.startsWith("SUPR_G_PIS")) {

				} else if (line.startsWith("SUPR_P_PHI")) {

				} else if (line.startsWith("SUPR_D_PHI")) {

				} else if (line.startsWith("SUPR_PPPHI")) {

				} else if (line.startsWith("SUPR_TMA_P")) {

				} else if (line.startsWith("SUPR_DMC_P")) {

				} else if (line.startsWith("SUPR_FIREG")) {

				} else if (line.startsWith("SUPR_SMSAW")) {

				} else if (line.startsWith("SUPR_CIMIL")) {

				} else if (line.startsWith("SUPR_VICCR")) {

				} else if (line.startsWith("SUPR_TRTSL")) {

				} else if (line.startsWith("SUPR_ERART")) {

				} else if (line.startsWith("SUPR_BSC_P")) {

				} else if (line.startsWith("SUPR_MODSC")) {

				} else if (line.startsWith("FICA_AFILT")) {

				} else if (line.startsWith("FICA_AFCOD")) {

				}*/ else if (line.startsWith(formated ? "FICA_AFNIV" : "FICA.AFNIV")) {
					this.setFile("FICA_AFNIV.");
					this.setProgress(10);
					insert = this.conn.prepareStatement("insert into ficaafniv (abonne, carre, plancher, plafond, elimine, firstcode, lastcode) " +
					"values (?, ?, ?, ?, ?, ?, ?)");
					this.setFicaAfniv(line, formated);
				} else if (line.startsWith(formated ? "FICA_AFNIC" : "FICA.AFNIC")) {
					this.setFile("FICA_AFNIC.");
					this.setProgress(11);
					insert = this.conn.prepareStatement("insert into ficaafnic (abonne, carre, plancher, plafond, firstcode, lastcode) " +
					"values (?, ?, ?, ?, ?, ?)");
					this.setFicaAfnic(line, formated);
				}/* else if (line.startsWith("FICA_CORLI")) {

				} else if (line.startsWith("CMON_ELIPS")) {

				} else if (line.startsWith("CMON_SVGDE")) {

				} else if (line.startsWith("CMON_TERRV")) {

				} else if (line.startsWith("DADA_PDADA")) {

				} else if (line.startsWith("STCA_ANTIC")) {

				} else if (line.startsWith("MTEO_METEO")) {

				}*/
		}
		//	insert.executeBatch();
		insert.close();
		this.setProgress(this.numberFiles());
	}


	private void setCentStack(String line, Boolean formated) throws SQLException, ParseException {
		CentStack centStack = new CentStack(line, formated);
		insert.setString(1, centStack.getName());
		insert.setDouble(2, centStack.getLatitude().toDecimal());
		insert.setDouble(3, centStack.getLongitude().toDecimal());
		insert.setDouble(4, centStack.getX());
		insert.setDouble(5, centStack.getY());
		insert.setInt(6, centStack.getRayonInt());
		insert.setInt(7, centStack.getRayonExt());
		insert.setInt(8, centStack.getFlInf());
		insert.setInt(9, centStack.getFlSup());
		insert.setString(10, centStack.getType());
		insert.executeUpdate();
	}
	
	private void setRadrTechn(String line, Boolean formated) throws SQLException, ParseException {
		RadrTechn radrTechn = new RadrTechn(line, formated);
		insert.setString(1, radrTechn.getNom());
		insert.setDouble(2, radrTechn.getVitesse());
		insert.setDouble(3, radrTechn.getHauteur());
		insert.setInt(4, radrTechn.getPortee());
		insert.setBoolean(5, radrTechn.getDeport());
		insert.executeUpdate();
	}

	private void setRadrGener(String line, Boolean formated) throws SQLException, ParseException {
		RadrGener radrGener = new RadrGener(line, formated);
		insert.setString(1, radrGener.getNom());
		insert.setInt(2, radrGener.getNumero());
		insert.setString(3, radrGener.getType());
		insert.setString(4, radrGener.getNomMosaique());
		insert.setDouble(5, radrGener.getLatitude().toDecimal());
		insert.setDouble(6, radrGener.getLongitude().toDecimal());
		insert.setDouble(7, radrGener.getX());
		insert.setDouble(8, radrGener.getY());
		insert.setDouble(9, radrGener.getEcartNord());
		insert.setString(10, radrGener.getRadarRelation());
		insert.setString(11, radrGener.getTypeRelation());
		insert.setString(12, radrGener.getTypePlots());
		insert.setString(13, radrGener.getTypeRadar());
		insert.setInt(14, radrGener.getCodePays());
		insert.setInt(15, radrGener.getCodeRadar());
		insert.setBoolean(16, radrGener.getMilitaire());
		insert.executeUpdate();
	}

	private void setCentScvvf(String line, Boolean formated) throws SQLException, ParseException {
		CentScvvf centScvvf = new CentScvvf(line, formated);
		insert.setInt(1, centScvvf.getCarre());
		insert.setInt(2, centScvvf.getSousCarre());
		insert.setString(3, centScvvf.getVvfs());
		insert.setString(4, centScvvf.getPlafonds());
		insert.setString(5, centScvvf.getPlanchers());
		insert.executeUpdate();
	}
	
	private void setCentFlvvf(String line, Boolean formated) throws SQLException, ParseException {
		CentFlvvf centFlvvf = new CentFlvvf(line, formated);
		insert.setString(1, centFlvvf.getName());
		insert.executeUpdate();
	}
	
	private void setCentSCodf(String line, boolean formated) throws SQLException, ParseException {
		CentSCodf centScodf = new CentSCodf(line, formated);
		insert.setString(1, centScodf.getName());
		insert.setString(2, centScodf.getDebut());
		insert.setString(3, centScodf.getFin());
		insert.setString(4, centScodf.getEspaces());
		insert.executeUpdate();
	}

	private void setFicaAfnic(String line, Boolean formated) throws SQLException, ParseException {
		FicaAfnic ficaAfnic = new FicaAfnic(line, formated);
		insert.setString(1, ficaAfnic.getAbonne());
		insert.setInt(2, ficaAfnic.getCarre());
		insert.setInt(3, ficaAfnic.getPlancher());
		insert.setInt(4, ficaAfnic.getPlafond());
		insert.setInt(5, ficaAfnic.getFirstCode());
		insert.setInt(6, ficaAfnic.getLastCode());
		insert.executeUpdate();
	}

	private void setCentSczoc(String line, Boolean formated) throws SQLException, ParseException {
		CentSczoc centSczoc = new CentSczoc(line, formated);
		insert.setInt(1, centSczoc.getCarre());
		insert.setInt(2, centSczoc.getSousCarre());
		insert.setString(3, centSczoc.getZone());
		insert.setInt(4, centSczoc.getPlafond());
		insert.addBatch();
	}

	private void setCentZOcc(String line, Boolean formated) throws SQLException, ParseException {
		CentZOcc centZOcc = new CentZOcc(line, formated);
		insert.setString(1, centZOcc.getName());
		insert.setString(2, centZOcc.getEspace());
		insert.setString(3, centZOcc.getTerrains());
		insert.executeUpdate();
	}

	/**
	 * Stocke une ligne FICA_AFNIV en bdd
	 * @param line
	 * @param formated 
	 * @throws SQLException 
	 * @throws ParseException 
	 */
	private void setFicaAfniv(String line, Boolean formated) throws SQLException, ParseException {
		FicaAfniv ficaAfniv = new FicaAfniv(line, formated);
		insert.setString(1, ficaAfniv.getAbonne());
		insert.setInt(2, ficaAfniv.getCarré());
		insert.setInt(3, ficaAfniv.getPlancher());
		insert.setInt(4, ficaAfniv.getPlafond());
		insert.setBoolean(5, ficaAfniv.getElimine());
		insert.setInt(6, ficaAfniv.getFirstCode());
		insert.setInt(7, ficaAfniv.getLastCode());
		insert.executeUpdate();
	}

	/**
	 * Stocke une ligne CENT_MOSAI en base de données
	 * @param line
	 * @param formated 
	 * @throws SQLException 
	 * @throws ParseException 
	 */
	private void setCentMosai(String line, Boolean formated) throws SQLException, ParseException {
		CentMosai centMosai = new CentMosai(line, formated);
		insert.setDouble(1, centMosai.getLatitude().toDecimal());
		insert.setDouble(2, centMosai.getLongitude().toDecimal());
		insert.setDouble(3, centMosai.getX());
		insert.setDouble(4, centMosai.getY());
		insert.setInt(5, centMosai.getLignes());
		insert.setInt(6, centMosai.getColonnes());
		insert.setString(7, centMosai.getType());
		insert.executeUpdate();
	}

	/**
	 * Stocke la ligne CARA_GENER en base de données
	 * @param line
	 * @throws SQLException 
	 * @throws ParseException 
	 */
	private void setCaraGener(String line, Boolean formated) throws SQLException, ParseException {
		//on récupère les données dans un objet temporaire
		CaraGener caraGener = new CaraGener(line, formated);
		//remplissage de la base de données
		insert.setString(1, caraGener.getName());
		insert.setString(2, caraGener.getDate());
		insert.setString(3, caraGener.getJeu());
		insert.setString(4, caraGener.getRadar());
		insert.setDouble(5, caraGener.getOasis());
		insert.setInt(6, caraGener.getBoa());
		insert.setString(7, caraGener.getVideomap());
		insert.setString(8, caraGener.getEdimap());
		insert.setString(9, caraGener.getSatin());
		insert.setString(10, caraGener.getCalculateur());
		insert.setString(11, caraGener.getContexte());
		insert.executeUpdate();		
	}
	/**
	 * Stocke la ligne CENT_CENTR en base de données
	 * @param line
	 * @throws ParseException 
	 * @throws SQLException 
	 * @parame formated
	 */
	private void setCentCentr(String line, Boolean formated) throws ParseException, SQLException{
		CentCentr centCentr = new CentCentr(line, formated);
		insert.setString(1, centCentr.getName());
		insert.setInt(2, centCentr.getSl());
		insert.setString(3, centCentr.getTypeCentre());
		insert.setInt(4, centCentr.getSic());
		insert.setInt(5, centCentr.getNivMsaw());
		insert.setString(6, centCentr.getRvsm());
		insert.setInt(7, centCentr.getNivPlancherRvsm());
		insert.setInt(8, centCentr.getNivPlafondRvsm());
		insert.setString(9, centCentr.getTypeDonnees());
		insert.setString(10, centCentr.getVersionADP());
		insert.executeUpdate();
	}

	@Override
	public int numberFiles() {
		return 11;
	}

}
