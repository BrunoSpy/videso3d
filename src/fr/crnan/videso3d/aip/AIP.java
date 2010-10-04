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

package fr.crnan.videso3d.aip;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.filter.Filter;
import org.jdom.input.SAXBuilder;


import fr.crnan.videso3d.Couple;
import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.FileParser;
import fr.crnan.videso3d.DatabaseManager.Type;

/**
 * Lecteur des exports en xml du SIA
 * @author Adrien Vidal
 * @version 0.3
 */
public class AIP extends FileParser{
//TODO la fenêtre d'information du fileParser ne s'affiche qu'à la fin.
	
	
	private static final Integer numberFiles = 1;
	
	/**
	 * Le nom de la base de données.
	 */
	private String name;
	
	/**
	 * Le chemin du fichier xml utilisé
	 */
	private String filePath;
	
	/**
	 * Connection à la base de données
	 */
	private Connection conn;

	/**
	 * Le <code>document</code> construit à partir du fichier xml.
	 */
	private Document document=null;
	
	
	private List<Couple<Integer,String>> TSAs;
	private List<Couple<Integer,String>> SIVs;
	private List<Couple<Integer,String>> CTRs;
	private List<Couple<Integer,String>> TMAs;
	private List<Couple<Integer,String>> Rs;
	private List<Couple<Integer,String>> Ds;
	
	public final static int Partie=0, TSA = 1, SIV = 2, CTR = 3, TMA = 4, R = 5, D = 6;
	


	public AIP(String path) {
		super(path);
		this.filePath=path;
		//construction du document à partir du fichier xml
		SAXBuilder sxb = new SAXBuilder();
		try {
			document = sxb.build(new File(path));
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

	public AIP(){
		this.TSAs = new LinkedList<Couple<Integer,String>>();
		this.SIVs = new LinkedList<Couple<Integer,String>>();
		this.CTRs = new LinkedList<Couple<Integer,String>>();
		this.TMAs = new LinkedList<Couple<Integer,String>>();
		this.Rs = new LinkedList<Couple<Integer,String>>();
		this.Ds = new LinkedList<Couple<Integer,String>>();
		
		SAXBuilder sxb = new SAXBuilder();
		try {
			//on récupère le chemin d'accès au fichier xml à parser
			Statement st = DatabaseManager.getCurrent(DatabaseManager.Type.Databases);
			ResultSet rs;
			rs = st.executeQuery("select * from clefs where name='path' and type='"+DatabaseManager.getCurrentName(DatabaseManager.Type.AIP)+"'");
			if(rs.next()){
				this.filePath = rs.getString(4);
				document = sxb.build(new File(filePath));
			}
			//TODO prendre en compte la possibilité qu'il n'y ait pas de bdd AIP
			Statement aipDB = DatabaseManager.getCurrentAIP();
			if(aipDB != null){
				//on récupère toutes les TSA de la bdd.
				ResultSet rSet = aipDB.executeQuery("select * from volumes");
				while(rSet.next()){
					Couple<Integer, String> id_name = new Couple<Integer, String>(rSet.getInt(2),rSet.getString(4));
					String type = rSet.getString(3);
					getZones(getTypeInt(type)).add(id_name);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Integer doInBackground() {
		//récupération du nom de la base à créer
		this.getName();
		try {
			//création de la connection à la base de données
			this.conn = DatabaseManager.selectDB(Type.AIP, this.name);
			this.conn.setAutoCommit(false); //fixes performance issue
			if(!DatabaseManager.databaseExists(this.name)){
				//création de la structure de la base de données
				DatabaseManager.createAIP(this.name,path);
				//parsing des fichiers et stockage en base
				this.getFromFiles();
				this.setProgress(1);
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
		return AIP.numberFiles;
	}
	
	
	
	/**
	 * Crée le nom de la base : AIP_date de publication
	 */
	private void getName() {		
		this.name = "AIP_"+document.getRootElement().getChild("Situation").getAttributeValue("pubDate");
	}

	@Override
	protected void getFromFiles() {
		Element racineVolumes = document.getRootElement().getChild("Situation").getChild("VolumeS");
		TSAs = new LinkedList<Couple<Integer,String>>();
		SIVs = new LinkedList<Couple<Integer,String>>();
		CTRs = new LinkedList<Couple<Integer,String>>();
		TMAs = new LinkedList<Couple<Integer,String>>();
		this.Rs = new LinkedList<Couple<Integer,String>>();
		this.Ds = new LinkedList<Couple<Integer,String>>();
		this.setFile("TSA");
		this.getTSAs(racineVolumes);
		this.setFile("SIV");
		this.getZones(racineVolumes,"SIV");
		this.setFile("CTR");
		this.getZones(racineVolumes,"CTR");
		this.setFile("TMA");
		this.getZones(racineVolumes,"TMA");
		this.setFile("R");
		this.getZones(racineVolumes,"R");
		this.setFile("D");
		this.getZones(racineVolumes,"D");
	}

	/**
	 * Cherche tous les éléments Volume qui sont des TSA, et insère leur nom dans la base de données
	 */
	private void getTSAs(Element racine) {
		List<Element> cbaList = findElements(racine,"lk", "CBA");
		List<Element> tsaList = findElements(racine, "lk","TSA");
		Iterator<Element> itCBA = cbaList.iterator();
		Iterator<Element> itTSA = tsaList.iterator();
		try {
			while(itCBA.hasNext()){
				this.insertZone(itCBA.next(),false,"TSA");
			}
			while(itTSA.hasNext()){
				this.insertZone(itTSA.next(),false,"TSA");
			}	
		} catch (SQLException e) {
			e.printStackTrace();
		}	
	}
	

	
	/**
	 * 
	 * @param racine
	 * @param type
	 */
	private void getZones(Element racine, String type){
		//TODO le probleme est ici !
		List<Element> zoneList = findElements(racine, "lk",type);
		Iterator<Element> it1 = zoneList.iterator();
		Iterator<Element> it2 = zoneList.iterator();
		//identifiants des zones (du même type) qui ont le même nom pour leur rajouter le numéro de séquence à la  fin du nom.
		HashSet<String> sameNames = new HashSet<String>();
		ArrayList<String> names = new ArrayList<String>();
		try{
			while(it1.hasNext()){
				Element zone = it1.next();
				String zoneName = getVolumeName(zone.getAttributeValue("lk"));
				if(!names.contains(zoneName)){
					names.add(zoneName);
				}else{
					sameNames.add(zoneName);
				}
			}
			while(it2.hasNext()){
				Element zoneElement = it2.next();
				if(sameNames.contains(getVolumeName(zoneElement.getAttributeValue("lk")))){
					this.insertZone(zoneElement,true,type);
				}else{
					this.insertZone(zoneElement,false,type);
				}
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * Insère dans la base de données la zone passée en paramètre
	 * @param zone Le secteur (ou zone...) à insérer dans la base.
	 * @param displaySequence Booléen indiquant s'il faut afficher le numéro de séquence de la zone ou pas.
	 * @param type
	 * @throws SQLException
	 */
	private void insertZone(Element zone, boolean displaySequence, String type) throws SQLException {
		int zoneID = Integer.parseInt(zone.getAttributeValue("pk"));
		String zoneName = getVolumeName(zone.getAttributeValue("lk"));
		if(displaySequence){
			zoneName+=" "+zone.getChildText("Sequence");
		}
		getZones(getTypeInt(type)).add(new Couple<Integer,String>(zoneID,zoneName));
		PreparedStatement ps = this.conn.prepareStatement("insert into volumes (pk,type,nom) VALUES (?, ?, ?)");
		ps.setInt(1, zoneID);
		ps.setString(2, type);
		ps.setString(3, zoneName);
		ps.executeUpdate();
	}
	

	/**
	 * Récupère le nom du volume en enlevant les [] et les caractères inutiles.
	 */
	private String getVolumeName(String fullName) {
		String name = fullName.substring(5);
		String region = fullName.substring(1, 3);
		if(name.charAt(1)==']'){
			name = name.substring(3);
		}
		int firstBracket = name.indexOf("[");
		if(!name.substring(firstBracket+1,firstBracket+3).equals(".]")){
			name = name.replaceFirst("[\\]]", " ");
			name = name.replaceFirst("[\\[]", "");
		}
		int bracketIndex=name.indexOf(']');
		return region.equals("LF")? name.substring(0, bracketIndex) : name.substring(0, bracketIndex)+" "+region;
	}

	
	@Override
	public int numberFiles() {
		return 1;
	}

	
	public List<Couple<Integer,String>> getZones(int type){
		switch(type){
		case TSA:
			return TSAs;
		case SIV:
			return SIVs;
		case CTR:
			return CTRs;
		case TMA:
			return TMAs;
		case R:
			return Rs;
		case D:
			return Ds;
		}
		return null;
	}
	
	
	public static String getTypeString(int type){
		switch(type){
		case TSA:
			return "TSA";
		case SIV: 
			return "SIV";
		case CTR:
			return "CTR";
		case TMA:
			return "TMA";
		case R:
			return "R";
		case D:
			return "D";
		default:
			return "";
		}
	}
	
	public static int getTypeInt(String type){
		if (type.equals("TSA")){
			return TSA;
		}
		if (type.equals("TMA")){
			return TMA;
		}
		if (type.equals("CTR")){
			return CTR;
		}
		if (type.equals("SIV")){
			return SIV;
		}
		if (type.equals("R")){
			return R;
		}
		if (type.equals("D")){
			return D;
		}
		return -1;
	}
	
	

	@Override
	public void done() {
		if(this.isCancelled()){//si le parsing a été annulé, on fait le ménage
			try {
				DatabaseManager.deleteDatabase(name, Type.AIP);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		firePropertyChange("done", false, true);
	}
	
	
	
	/**
	 * Liste tous les éléments dont le champ fieldParam <b>commence</b> par la chaîne de caractères identityValue, parmi les fils de l'élément racine.
	 * @param root Le noeud contenant les éléments parmi lesquels on effectue la recherche
	 * @param fieldParam Le champ sur lequel porte la recherche
	 * @param identityValue La valeur du champ fieldParam
	 * @return la liste des éléments répondant au critère.
	 */
	@SuppressWarnings("unchecked")
	public List<Element> findElements(Element root,String fieldParam,String identityValue){
		final String field = fieldParam;
		final String identity = identityValue;
		Filter f = new Filter(){
			@Override
			public boolean matches(Object o) {
				if(!(o instanceof Element)){return false;}
				Element element = (Element)o;
				String name = getVolumeName(element.getAttributeValue(field));
				if (name.startsWith(identity)){
					return true;
				}
				return false;
			}
		
		};
		return root.getContent(f);	
	}
	
	
	/**
	 * Permet de trouver un élément par son nom et son type (TMA, CTA,...)
	 * @param type
	 * @param name
	 * @return l'élément recherché.
	 */
	public Element findElementByName(int type, String name){
		Element racine = document.getRootElement().getChild("Situation").getChild("VolumeS");
		return findElement(racine, getID(type,name));
	}
	
	
	private String getID(int type, String name){
		String pk=null;
		String typeString=getTypeString(type);
		try {
			PreparedStatement st = DatabaseManager.prepareStatement(Type.AIP, "select * from volumes where type = ? AND nom = ?");
			st.setString(1, typeString);
			st.setString(2, name);
			ResultSet rs = st.executeQuery();
			if(rs.next()){
				pk=rs.getString(2);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return pk;
	}
	
	

	/**
	 * Renvoie l'élément de type Partie identifié par id.
	 * @param id L'identifiant de la Partie (champ "pk").
	 * @return La partie recherchée.
	 */
	public Element getPartie(String id){
		return findElement(document.getRootElement().getChild("Situation").getChild("PartieS"), id);
	}
	
	/**
	 * Renvoie l'élément dont l'attribut "pk" correspond à <code>idNumber</code> parmi les fils de l'élément <code>racine</code>
	 * @param racine 
	 * @param idNumber
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Element findElement(Element racine, String idNumber){
		final String id = idNumber;
		Filter f = new Filter(){
			@Override
			public boolean matches(Object o) {
				if(!(o instanceof Element)){return false;}
				Element element = (Element)o;
				if (element.getAttributeValue("pk").equals(id)){
					return true;
				}
				return false;
			}
		};
		return ((List<Element>) racine.getContent(f)).get(0);
	}
		
	
	
	
	
	
	/**
	 * Renvoie les niveaux plancher et plafond d'une zone, sous forme de couple d'<code>Altitude</code>. 
	 * Attention : Ne teste pas si l'élément contient bien un champ plafond et un champ plancher.
	 * @param e L'élément dont on cherche le plancher et le plafond.
	 * @return Un couple d'<code>Altitude</code> dont le premier élément est le plancher et le second est le plafond.
	 * @see Altitude
	 */
	public Couple<Altitude,Altitude> getLevels(Element e){
		Altitude plancher = new Altitude(e.getChild("PlancherRefUnite").getValue(), Integer.parseInt(e.getChild("Plancher").getValue()));
		Altitude plafond = new Altitude(e.getChild("PlafondRefUnite").getValue(), Integer.parseInt(e.getChild("Plafond").getValue()));
		return new Couple<Altitude,Altitude>(plancher,plafond);
	}
	
	
	
	
	
	
	
	/**
	 * Classe conservant l'unité et la référence (AMSL,ASFC...) d'un plafond ou plancher fourni pour un Volume, et qui permet d'avoir l'équivalent en FL. 
	 * @author VIDAL Adrien
	 *
	 */
	public class Altitude{
		private int unite;
		private int ref;
		private int originalValue;
		private int FL;
		private String fullText;
		
		static final int sfc=0,ft=1,fl=2;
		static final int refSFC=0,amsl=1,asfc=2,qnh=3,unl=4;
		
		public Altitude(String refUnite, int value){
			originalValue=value;
			if(refUnite.startsWith("ft")){
				FL=value/100;
				unite=Altitude.ft;
				String reference = refUnite.substring(3);
				if(reference.equals("ASFC")){
					this.fullText=value+" ft ASFC";
					ref=Altitude.asfc;
				}else{
					this.fullText=value+" ft AMSL";
					ref=Altitude.amsl;
				}
			}
			if(refUnite.equals("SFC")){
				FL=0;
				unite=Altitude.sfc;
				ref=Altitude.refSFC;
				this.fullText="SFC";
			}
			if(refUnite.equals("FL")){
				FL=value;
				unite=Altitude.fl;
				ref=Altitude.qnh;
				String valueString=""+value;
				if(value<100){
					valueString="0"+value;
				}
				if(value<10){
					valueString="00"+value;
				}
				this.fullText="FL "+valueString;
			}
			if(refUnite.equals("UNL")){
				FL=660;
				unite=Altitude.fl;
				ref=Altitude.unl;
				this.fullText="ILLIMITÉ";
			}
		}
		
		public int getUnite(){
			return unite;
		}
		
		public int getRef(){
			return ref;
		}
		
		public int getOriginalValue(){
			return originalValue;
		}
		
		public int getFL(){
			return FL;
		}
		
		public String getFullText(){
			return fullText;
		}
	}

}
