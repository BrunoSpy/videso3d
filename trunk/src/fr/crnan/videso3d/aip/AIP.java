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
	
	private final Integer numberFiles = 18;
	
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
	
	//TODO pour diminuer la consommation de mémoire, on peut faire une requête sur la BDD au lieu de maintenir ces listes.
	// Mais bien vérifier le fonctionnement de showObject avant de supprimer les listes (surtout pour les CTL).
	private List<Couple<Integer,String>> TSAs;
	private List<Couple<Integer,String>> SIVs;
	private List<Couple<Integer,String>> CTRs;
	private List<Couple<Integer,String>> TMAs;
	private List<Couple<Integer,String>> Rs;
	private List<Couple<Integer,String>> Ds;
	private List<Couple<Integer,String>> FIRs;
	private List<Couple<Integer,String>> UIRs;
	private List<Couple<Integer,String>> LTAs;
	private List<Couple<Integer,String>> UTAs;
	private List<Couple<Integer,String>> CTAs;
	private List<Couple<Integer,String>> CTLs;
	private List<Couple<Integer,String>> Pjes;
	private List<Couple<Integer,String>> Aers;
	private List<Couple<Integer,String>> Vols;
	private List<Couple<Integer,String>> Bals;
	private List<Couple<Integer,String>> TrPlas;
	
	
	public final static int Partie=0, TSA = 1, SIV = 2, CTR = 3, TMA = 4, R = 5, 
							D = 6, FIR = 7, UIR = 8, LTA = 9, UTA = 10, CTA = 11, 
							CTL = 12, Pje = 13, Aer = 14, Vol=15, Bal = 16, TrPla = 17,
							AWY = 20, PDR = 21, TAC = 23;
	


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
		this.FIRs = new LinkedList<Couple<Integer,String>>();
		this.UIRs = new LinkedList<Couple<Integer,String>>();
		this.LTAs = new LinkedList<Couple<Integer,String>>();
		this.UTAs = new LinkedList<Couple<Integer,String>>();
		this.CTAs = new LinkedList<Couple<Integer,String>>();
		this.CTLs = new LinkedList<Couple<Integer,String>>();
		this.Pjes = new LinkedList<Couple<Integer,String>>();
		this.Aers = new LinkedList<Couple<Integer,String>>();
		this.Vols = new LinkedList<Couple<Integer,String>>();
		this.Bals = new LinkedList<Couple<Integer,String>>();
		this.TrPlas = new LinkedList<Couple<Integer,String>>();
		
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
				//on récupère tous les volumes
				ResultSet rSet = aipDB.executeQuery("select * from volumes");
				while(rSet.next()){
					Couple<Integer, String> id_name = new Couple<Integer, String>(rSet.getInt(2),rSet.getString(4));
					String type = rSet.getString(3);
					getZones(string2type(type)).add(id_name);
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
		return this.numberFiles();
	}
	
	/**
	 * 
	 * @return L'élément Situation, qui contient tous les objets qui nous intéressent.
	 */
	public Element getDocumentRoot(){
		return document.getRootElement().getChild("Situation");
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
		this.TSAs = new LinkedList<Couple<Integer,String>>();
		this.SIVs = new LinkedList<Couple<Integer,String>>();
		this.CTRs = new LinkedList<Couple<Integer,String>>();
		this.TMAs = new LinkedList<Couple<Integer,String>>();
		this.Rs = new LinkedList<Couple<Integer,String>>();
		this.Ds = new LinkedList<Couple<Integer,String>>();
		this.FIRs = new LinkedList<Couple<Integer,String>>();
		this.UIRs = new LinkedList<Couple<Integer,String>>();
		this.LTAs = new LinkedList<Couple<Integer,String>>();
		this.UTAs = new LinkedList<Couple<Integer,String>>();
		this.CTAs = new LinkedList<Couple<Integer,String>>();
		this.CTLs = new LinkedList<Couple<Integer,String>>();
		this.Pjes = new LinkedList<Couple<Integer,String>>();
		this.Aers = new LinkedList<Couple<Integer,String>>();
		this.Vols = new LinkedList<Couple<Integer,String>>();
		this.Bals = new LinkedList<Couple<Integer,String>>();
		this.TrPlas = new LinkedList<Couple<Integer,String>>();
		this.setFile("TSA");
		this.setProgress(0);
		this.getTSAs(racineVolumes);
		this.setFile("SIV");
		this.setProgress(1);
		this.getZones(racineVolumes,"SIV");
		this.setFile("CTR");
		this.setProgress(2);
		this.getZones(racineVolumes,"CTR");
		this.setFile("TMA");
		this.setProgress(3);
		this.getZones(racineVolumes,"TMA");
		this.setFile("R");
		this.setProgress(4);
		this.getZones(racineVolumes,"R");
		this.setFile("D");
		this.setProgress(5);
		this.getZones(racineVolumes,"D");
		this.setFile("FIR");
		this.setProgress(6);
		this.getZones(racineVolumes,"FIR");
		this.setFile("UIR");
		this.setProgress(7);
		this.getZones(racineVolumes,"UIR");
		this.setFile("LTA");
		this.setProgress(8);
		this.getZones(racineVolumes,"LTA");
		this.setFile("UTA");
		this.setProgress(9);
		this.getZones(racineVolumes,"UTA");
		this.setFile("CTA");
		this.setProgress(10);
		this.getZones(racineVolumes,"CTA");
		this.setFile("CTL");
		this.setProgress(11);
		this.getZones(racineVolumes,"CTL");
		this.setFile("Parachutages");
		this.setProgress(12);
		this.getZones(racineVolumes,"Pje");
		this.setFile("Aer");
		this.setProgress(13);
		this.getZones(racineVolumes,"Aer");
		this.setFile("Voltige");
		this.setProgress(14);
		this.getZones(racineVolumes,"Vol");
		this.setFile("Ballons");
		this.setProgress(15);
		this.getZones(racineVolumes,"Bal");
		this.setFile("Treuils planeurs");
		this.setProgress(16);
		this.getZones(racineVolumes,"TrPla");
		this.setFile("Routes");
		this.setProgress(17);
		this.getRoutes();
		this.setProgress(18);
		
	}
	
	
	
	
	@SuppressWarnings("unchecked")
	private void getRoutes(){
		Element racineRoutes = document.getRootElement().getChild("Situation").getChild("RouteS");
		List<Element> routes = racineRoutes.getChildren();
		for(Element route : routes){
			insertRoute(route);
		}
	}
	
	private void insertRoute(Element route){
		String pkRoute = route.getAttributeValue("pk");
		int routeID = Integer.parseInt(pkRoute);
		String routeName = route.getChildText("Prefixe")+" "+route.getChildText("Numero");
		String territoireID = route.getChild("Territoire").getAttributeValue("pk");
		if( ! territoireID.equals("100")){
			routeName += " - "+getTerritoireName(territoireID); 
		}
		PreparedStatement ps;
		try {
			ps = this.conn.prepareStatement("insert into routes (pk,type,nom) VALUES (?, ?, ?)");
			ps.setInt(1, routeID);
			ps.setString(2, route.getChildText("RouteType"));
			ps.setString(3, routeName);
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		Element racineSegments = document.getRootElement().getChild("Situation").getChild("SegmentS");
		List<Element> segments = findElementsByChildId(racineSegments, "Route", pkRoute);
		try {
			for(Element segment : segments){
				int segmentID = Integer.parseInt(segment.getAttributeValue("pk"));
				int sequence = Integer.parseInt(segment.getChildText("Sequence"));
				ps = this.conn.prepareStatement("insert into segments (pk, pkRoute, sequence) VALUES (?, ?, ?)");
				ps.setInt(1, segmentID);
				ps.setInt(2, routeID);
				ps.setInt(3, sequence);
				ps.executeUpdate();	

				//Insertion des centres traversés par la route : pour chaque segment, on ajoute le centre à la liste des centres traversés
				String nomACC = segment.getChildText("Acc");
				if(nomACC != null){
					if(nomACC.contains(" ")){
						String[] nomsACCs = nomACC.split(" ");
						insertACCs(nomsACCs, pkRoute);
					}else if(nomACC.contains("#")){
						String[] nomsACCs = nomACC.split("#");
						insertACCs(nomsACCs, pkRoute);
					}else{
						insertACC(nomACC, pkRoute);
						
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	

	private void insertACCs(String[] nomsACCs, String pkRoute) throws SQLException{
		for (String nom : nomsACCs){
			insertACC(nom,pkRoute);
		}
	}

	private void insertACC(String nomACC, String pkRoute) throws SQLException{
		boolean insertACC = true;
		PreparedStatement ps = this.conn.prepareStatement("select * from ACCTraverses where routes_pk = ?");
		ps.setString(1, pkRoute);
		ResultSet rs = ps.executeQuery();
		while(rs.next()){
			if(rs.getString(2).equals(nomACC)){
				insertACC = false;
				break;
			}
		}
		if(insertACC){
			ps = this.conn.prepareStatement("insert into ACCTraverses (routes_pk, nomACC) VALUES (?, ?)");
			ps.setString(1, pkRoute);
			ps.setString(2, nomACC);	
			ps.executeUpdate();	
		}
	}

	

	/**
	 * Cherche tous les éléments Volume qui sont des TSA, et insère leur nom dans la base de données
	 */
	private void getTSAs(Element racine) {
		List<Element> cbaList = findVolumes(racine,"lk", "CBA");
		List<Element> tsaList = findVolumes(racine, "lk","TSA");
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
		List<Element> zoneList = findVolumes(racine, "lk",type);
		Iterator<Element> it1 = zoneList.iterator();
		Iterator<Element> it2 = zoneList.iterator();
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
		//on teste s'il s'agit d'une zone Pje, Aer, Vol, Bal ou TrPla en regardant si le deuxième caractère est en minuscule
		if(type.length()>1){
			if (Character.isLowerCase(type.charAt(1))){
				String usualName = getUsualName(zone);
				if(usualName != null){
					zoneName += " -- "+usualName;
				}
			}
		}
		if(displaySequence){
			zoneName+=" "+zone.getChildText("Sequence");
		}
		getZones(string2type(type)).add(new Couple<Integer,String>(zoneID,zoneName));
		PreparedStatement ps = this.conn.prepareStatement("insert into volumes (pk,type,nom) VALUES (?, ?, ?)");
		ps.setInt(1, zoneID);
		ps.setString(2, type);
		ps.setString(3, removeType(zoneName));
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

	private String getUsualName(Element zone){
		String usualName=null;
		String partieID = zone.getChild("Partie").getAttributeValue("pk");
		Element partie = this.findElement(document.getRootElement().getChild("Situation").getChild("PartieS"), partieID);
		usualName = partie.getChildText("NomUsuel");
		return usualName;
	}
	
	
	/**
	 * Vérifie si le nom de la zone commence par le type (CTR, TMA,...)
	 * @param name Le nom à vérifier
	 * @return Le nom amputé du type de zone, sauf si c'est une zone R ou D.
	 */
	private String removeType(String name){
		int lettersToRemove = 0;
		if(name.startsWith("SIV")
				||name.startsWith("CTR")
				||name.startsWith("TMA")
				||name.startsWith("FIR")
				||name.startsWith("UIR")
				||name.startsWith("LTA")
				||name.startsWith("UTA")
				||name.startsWith("CTA")
				||name.startsWith("CTL")
				||name.startsWith("Pje")
				||name.startsWith("Aer")
				||name.startsWith("Vol")
				||name.startsWith("Bal")){
			lettersToRemove = 4;
		}
		if(name.startsWith("TrPla"))
				lettersToRemove = 6;
		return name.substring(lettersToRemove);
	}
	
	
	@Override
	public int numberFiles() {
		return this.numberFiles;
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
		case FIR:
			return FIRs;
		case UIR:
			return UIRs;
		case LTA:
			return LTAs;
		case UTA:
			return UTAs;
		case CTA:
			return CTAs;
		case CTL:
			return CTLs;
		case Pje:
			return Pjes;
		case Aer:
			return Aers;
		case Vol:
			return Vols;
		case Bal:
			return Bals;
		case TrPla:
			return TrPlas;
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
		case FIR:
			return "FIR";
		case UIR:
			return "UIR";
		case LTA:
			return "LTA";
		case UTA:
			return "UTA";
		case CTA:
			return "CTA";
		case CTL:
			return "CTL";
		case Pje:
			return "Pje";
		case Aer:
			return "Aer";
		case Vol:
			return "Vol";
		case Bal:
			return "Bal";
		case TrPla:
			return "TrPla";
		case AWY :
			return "AWY";
		case PDR :
			return "PDR";
		case TAC :
			return "TAC";
		default:
			return "";
		}
	}
	
	public static int string2type(String type){
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
		if (type.equals("FIR")){
			return FIR;
		}
		if (type.equals("UIR")){
			return UIR;
		}
		if (type.equals("LTA")){
			return LTA;
		}
		if (type.equals("UTA")){
			return UTA;
		}
		if (type.equals("CTA")){
			return CTA;
		}
		if (type.equals("CTL")){
			return CTL;
		}
		if (type.equals("Pje")){
			return Pje;
		}
		if (type.equals("Aer")){
			return Aer;
		}
		if (type.equals("Vol")){
			return Vol;
		}
		if (type.equals("Bal")){
			return Bal;
		}
		if (type.equals("TrPla")){
			return TrPla;
		}
		if(type.equals("AWY")){
			return AWY;
		}
		if(type.equals("PDR")){
			return PDR;
		}
		if(type.equals("TAC")){
			return TAC;
		}
		return -1;
	}
	
	public String RouteType2AIPType(String routeName, fr.crnan.videso3d.graphics.Route.Type type){
		if(routeName.startsWith("T")&& routeName.charAt(1)!=' '){
			return "TAC";
		}else{
			return type.equals(fr.crnan.videso3d.graphics.Route.Type.FIR) ? "AWY" : "PDR";
		}
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
	 * Liste tous les éléments dont le champ fieldParam <b>commence</b> par la chaîne de caractères "value", parmi les fils de l'élément racine.
	 * <i>A n'utiliser que pour chercher des volumes par leur nom.</i>
	 * @param root Le noeud contenant les éléments parmi lesquels on effectue la recherche
	 * @param fieldParam Le champ sur lequel porte la recherche
	 * @param value La valeur du champ fieldParam
	 * @return la liste des éléments répondant au critère.
	 */
	@SuppressWarnings("unchecked")
	public List<Element> findVolumes(Element root,String fieldParam,String value){
		final String field = fieldParam;
		final String identity = value;
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
	
	
	/**
	 * Renvoie l'élément dont l'attribut "pk" correspond à <code>idNumber</code> parmi les fils de l'élément <code>racine</code>
	 * @param racine 
	 * @param idNumber
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Element findElement(Element racine, String idNumber){
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
	
	
	
	@SuppressWarnings("unchecked")
	public List<Element> findElementsByChildId(Element root,String childParam,String value){
		final String child = childParam;
		final String childID = value;
		Filter f = new Filter(){
			@Override
			public boolean matches(Object o) {
				if(!(o instanceof Element)){return false;}
				Element element = (Element)o;
				String pkChild = element.getChild(child).getAttributeValue("pk");
				if (pkChild.equals(childID)){
					return true;
				}
				return false;
			}
		
		};
		return root.getContent(f);	
	}
	
	
	
	public static String getID(int type, String name){
		String pk=null;
		switch(type){
		
		case AIP.AWY :
		case AIP.PDR :
		case AIP.TAC :
			try {
				PreparedStatement st = DatabaseManager.prepareStatement(Type.AIP, "select pk from routes where nom = ?");
				st.setString(1, name);
				ResultSet rs = st.executeQuery();
				if(rs.next()){
					pk=rs.getString(1);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			break;
			
		default :
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
		}
		return pk;
	}
	
	
	
	public String getZoneAttributeValue(String zoneID, String attribute){
		Element zone = findElement(document.getRootElement().getChild("Situation").getChild("VolumeS"),zoneID);
		return zone != null ? zone.getChildText(attribute) : null;
	}

	/**
	 * Même chose que org.jdom.Element.getChildText mais renvoie null si l'enfant n'existe pas au lieu de lever une exception.
	 * @param e
	 * @param childName
	 * @return
	 */
	public String getChildText(Element e, String childName){
		Element child = e.getChild(childName);
		return child != null ? child.getText() : null;
	}
	

		
	
	
	
	public String getTerritoireName(String territoireID){
		Element territoire = findElement(document.getRootElement().getChild("Situation").getChild("TerritoireS"), territoireID);
		return territoire.getChildText("Nom");
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
				originalValue = 660;
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
		
		public boolean isTerrainConforming(){
			if(ref == Altitude.asfc || ref == Altitude.refSFC) 
				return true;
			return false;
		}
		
		public int getMeters(){
			if(unite == Altitude.sfc)
				return 0;
			if(unite == Altitude.ft)
				return (int) (((double) originalValue)/3.2808);
			return (int) (((double) originalValue*100)/3.2808);
		}
	}

}
