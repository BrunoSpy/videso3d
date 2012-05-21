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
package fr.crnan.videso3d.formats.fpl;

import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Vector;


import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.Triplet;
import fr.crnan.videso3d.formats.TrackFilesReader;
import fr.crnan.videso3d.formats.VidesoTrack;
import fr.crnan.videso3d.formats.lpln.LPLNTrackPoint;
import fr.crnan.videso3d.geom.LatLonUtils;
import fr.crnan.videso3d.ihm.components.ProgressInputStream;
import fr.crnan.videso3d.stip.PointNotFoundException;
import fr.crnan.videso3d.trajectography.TracksModel;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;

/**
 * Lecteur de plans de vol importés d'IvanWeb ou de plans de vol fictifs définis simplement par une suite de balises, de coordonnées géographiques 
 * ou de routes.
 * Format accepté pour les plans de vol fictifs : 
 * -le plan de vol doit commencer par "(FPL" et se  terminer par une parenthèse fermante ")"
 * -les balises, points ou routes doivent être séparés par au moins un espace
 * -le niveau de vol est précisé par : Fxxx où xxx est le niveau demandé.
 * -le plan de vol peut être sur plusieurs lignes à condition de ne pas couper les noms des balises ou des routes
 * -un point défini par ses coordonnées doit avoir un des formats suivants : 45.9568N5.12E  ou  12d42'12"N,104d23'01"E
 * @author Adrien Vidal
 */
public class FPLReader extends TrackFilesReader {

	private static enum Type{Balise, Point, Route}; 

	private static String msgErreur = "";


	public FPLReader() {
		this.setModel(new TracksModel());
		this.setName("?");
	}


	public FPLReader(File selectedFile, TracksModel model) throws PointNotFoundException {
		super(selectedFile, model);
	}

	public FPLReader(Vector<File> files, TracksModel model, PropertyChangeListener listener) throws PointNotFoundException{
		super(files, model, listener);
	}
	
	public FPLReader(Vector<File> files, TracksModel model) throws PointNotFoundException{
		super(files, model);
	}
	
	public FPLReader(File selectedFile) throws PointNotFoundException {
		super(selectedFile);
	}

	public FPLReader(Vector<File> files) throws PointNotFoundException{
		super(files);
	}

	/**
	 * Renvoie le message d'erreur actuel et l'efface.
	 * @return
	 */
	public String getErrorMessage(){
		String erreur = msgErreur;
		msgErreur = "";
		return erreur;
	}

	public static Boolean isFPLFile(File file){
		Boolean fpl = false;
		try {
			BufferedReader in = new BufferedReader(
					new InputStreamReader(
							new FileInputStream(file)));
			int count = 0; //nombre de lignes lues
			while(in.ready() && !fpl && count < 10){
				String line = in.readLine();
				if(line.startsWith("(FPL")){
					fpl = true;
				}
				count++;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fpl;
	}

	public static boolean isIvanWeb(LinkedList<String> fpl){
		if(fpl.size()>4 && fpl.get(1).startsWith("-"))
			return true;
		return false;
	}

	public static String getIndicatif(LinkedList<String> fpl){
		if(isIvanWeb(fpl))
			return fpl.getFirst().split("-")[1];
		return "?";
	}

	@Override
	protected void doReadStream(ProgressInputStream stream){
		String line;
		BufferedReader in = new BufferedReader(
				new InputStreamReader(stream), 32);
		try{
			while(in.ready()){
				line = in.readLine();
				if(line.startsWith("(FPL")){
					LinkedList<String> fpl = new LinkedList<String>();
					boolean endOfFPL=false;
					while( !endOfFPL ){
						fpl.add(line);
						if(line.matches(".*\\)\\s*")){
							endOfFPL = true;
						}else{
							if(in.ready())
								line = in.readLine();
						}
					}
					try{
						parseFPL(fpl, "?");
					}catch(UnrecognizedFPLException e){
						msgErreur+=e.getMessage()+"\n";
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public void parseFPL(LinkedList<String> fpl, String indicatif) throws UnrecognizedFPLException{
		if(isIvanWeb(fpl))
			parseIvanWebFPL(fpl);
		else
			parseFreeFPL(fpl, indicatif);
	}


	/**
	 * Lit le plan de vol pour construire la trajectoire (<code>FPLTrack</code>) correspondante.
	 * @param fpl Les lignes qui composent le plan de vol
	 */
	public void parseIvanWebFPL(LinkedList<String> fpl) throws UnrecognizedFPLException{
		FPLTrack track = null;
		try{
			String indicatif = fpl.getFirst().split("-")[1];
			String type = fpl.get(1).substring(1, 5);
			String depart = fpl.get(2).substring(1,5).toUpperCase();
			int derniereLigneRoute = fpl.getLast().startsWith("-DOF")? fpl.size()-2 : fpl.size()-1;
			String arrivee = fpl.get(derniereLigneRoute).substring(1,5).toUpperCase();
			track = new FPLTrack(indicatif);
			track.setIndicatif(indicatif);
			track.setType(type);
			track.setDepart(depart);
			track.setArrivee(arrivee);
			LinkedList<LPLNTrackPoint> pointsList = new LinkedList<LPLNTrackPoint>();
			addAirportToTrack(pointsList, depart);
			parseRoute(new LinkedList<String>(fpl.subList(3,derniereLigneRoute)), pointsList);
			addAirportToTrack(pointsList, arrivee);	
			addKnownPointsListToTrack(pointsList, track);
		}catch(Exception e){
			e.printStackTrace();
			throw new UnrecognizedFPLException(fpl.getFirst());
		}
		if(track.getNumPoints()>1){
			this.getModel().addTrack(track);
		}else{
			throw new UnrecognizedFPLException(fpl.getFirst());
		}
	}

	public void parseFreeFPL(LinkedList<String> fpl, String indicatif) throws UnrecognizedFPLException{
		FPLTrack track = new FPLTrack(indicatif);
		track.setIndicatif(indicatif);
		track.setType("?");
		try{
			fpl.set(0, fpl.getFirst().replace("(FPL", ""));
			String firstLine = fpl.getFirst();
			String firstElement = firstLine.split("\\s+")[1].trim().toUpperCase();
			if(firstElement.matches("F\\d{3}"))
				firstElement = "?";
			track.setDepart(firstElement);
			LinkedList<LPLNTrackPoint> pointsList = new LinkedList<LPLNTrackPoint>();
			boolean arptDepart = false;
			if(!firstElement.equals("?")&& firstElement.matches("\\p{Alpha}{4}"));
					arptDepart = addAirportToTrack(pointsList, firstElement);
			if(arptDepart)
				fpl.set(0, firstLine.substring(firstLine.indexOf(firstElement)+firstElement.length()));
			fpl.set(fpl.size()-1, fpl.getLast().replace(")", "").trim());
			parseRoute(fpl, pointsList);
			String[] lastElements = fpl.getLast().split("\\s+");
			int length = lastElements.length;
			boolean arptArrivee = false;
			String lastPoint = lastElements[length-1].toUpperCase();
			track.setArrivee(lastPoint);
			if(lastPoint.matches("\\p{Alpha}{4}")){
				arptArrivee = addAirportToTrack(pointsList, lastPoint);
				if(arptArrivee){
					if(pointsList.get(pointsList.size()-2).getName().equals(lastPoint)){
						pointsList.remove(pointsList.size()-2);
					}
				}
			}
			addKnownPointsListToTrack(pointsList, track);
		}catch(Exception e){
			e.printStackTrace();
			throw new UnrecognizedFPLException(fpl.getFirst());
		}
		if(track.getNumPoints()>0){
			this.getModel().addTrack(track);
		}else{
			throw new UnrecognizedFPLException(fpl.getFirst());
		}
	}


	private void addKnownPointsListToTrack(LinkedList<LPLNTrackPoint> pointsList, FPLTrack track){
		int i=0;
		boolean incertain = false;
		LinkedList<Integer> segmentsIncertains = new LinkedList<Integer>();
		while(i<pointsList.size()){
			if(pointsList.get(i).getPosition()==null){
				if(!incertain){
					incertain = true;
					segmentsIncertains.add(i);
				}
				pointsList.remove(i);
			}else{
				incertain = false;
				i++;
			}
		}	
		track.getTrackPoints().addAll(pointsList);
		if(!segmentsIncertains.isEmpty()){
			if(segmentsIncertains.getLast()>=track.getNumPoints())
				segmentsIncertains.removeLast();
			track.setSegmentIncertain(segmentsIncertains);
		}
	}

	private void parseRoute(LinkedList<String> route, LinkedList<LPLNTrackPoint> track){
		boolean premiereBaliseTrouvee = false;
		ArrayList<Triplet<String, Type, Double>> array = new ArrayList<Triplet<String, Type, Double>>();
		double elevation = 0;


		for(String line : route){
			if(line.startsWith("-")){
				line = line.substring(1);
			}
			//Remplacement des caractères spéciaux que l'on peut trouver dans les coordonnées géographiques du SIA
			line = line.replaceAll("--", " ");
			line = line.replaceAll("\\u00ba", "d");
			line = line.replaceAll("\\u2019", "'");
			line = line.replaceAll("\\u201d", "\"");
			line = line.trim();
			String[] elements = line.split("\\s+");
			//On construit un tableau où pour chaque élément on indique le nom, le type d'élément et l'altitude.
			for(String e : elements){
				Triplet<String, Type, Double> typeElevation = findElementTypeAndElevation(e.toUpperCase(), elevation);
				elevation = typeElevation.getThird();
				if(typeElevation.getFirst()!=null && typeElevation.getSecond()!=null)
					array.add(typeElevation);
			}
		}
		//On parcourt ensuite ce tableau pour extraire la route
		for(int i = 0; i < array.size(); i++){
			Triplet<String, Type, Double> t = array.get(i);
			if(t.getSecond()==Type.Balise){
				if(i>1){
					//Si c'est une balise, on regarde si l'élément précédent est une route et si l'élément encore avant est 
					//une balise : dans ce cas, on cherche les points de la route entre les deux balises.
					Triplet<String, Type, Double> r = array.get(i-1);
					Triplet<String, Type, Double> b = array.get(i-2);
					if(r.getSecond()==Type.Route){
						if(b.getSecond()==Type.Balise){
							LinkedList<LPLNTrackPoint> liste = getRouteBetween(b.getFirst(), t.getFirst(), r.getFirst(), r.getThird());
							int k = 0;
							while(!premiereBaliseTrouvee && k<liste.size()){
								if(liste.get(k).getPosition()!=null)
									premiereBaliseTrouvee=true;
								k++;
							}
							track.addAll(liste);
						}
					}
				}
				track.add(getBalise(t.getFirst(), t.getThird()));
				int size = track.size();
				//Si on a les 3 derniers points inconnus, on arrête.
				if(premiereBaliseTrouvee){
					boolean lost = true;
					if(size>2){
						for(int k = size-1; k>size-4; k--){
							if(track.get(k).getPosition()!=null){
								lost = false;
								break;
							}
						}
					}else
						lost = false;
					if(lost)
						break;
				}
			}else if(t.getSecond()==Type.Point){
				LPLNTrackPoint p = pointToLPLNPoint(t.getFirst(), t.getThird());
				if(p!=null)
					track.add(p);
			}
		}
	}


	/**
	 * 
	 * @param name
	 * @param elevation
	 * @return Un LPLNTrackPoint correspondant à la balise, avec une position nulle si on n'a trouvé la balise dans aucune des bases.
	 */
	private LPLNTrackPoint getBalise(String name, double elevation){
		LPLNTrackPoint tp = getBaliseSTIP(name, elevation);
		// si on ne trouve pas la balise, on crée quand même un trackpoint avec une position nulle.
		if(tp==null){
			tp = new LPLNTrackPoint();
			tp.setName(name);
		}
		return tp;
	}

	/**
	 * Cherche la balise <code>name</code> dans la base STIP, puis dans la base AIP si elle n'est pas dans le STIP où si la base STIP n'est pas 
	 * configurée, et enfin dans la base SkyView si la balise n'a pas été trouvée dans l'AIP.
	 * @param name
	 * @param elevation
	 * @return Un LPLNTrackPoint ayant pour nom <code>name</code>, avec une position nulle si la balise n'a été trouvée dans aucune des bases.
	 */
	private LPLNTrackPoint getBaliseSTIP(String name, double elevation){
		LPLNTrackPoint p = new LPLNTrackPoint();
		try{
			if(DatabaseManager.getCurrentStip()!=null){
				Statement st = DatabaseManager.getCurrentStip();
				ResultSet rs = st.executeQuery("select latitude, longitude from balises where name ='"+name+"'");
				if(rs.next()){
					p.setPosition(Position.fromDegrees(rs.getDouble(1), rs.getDouble(2), elevation));
					p.setName(name);
					return p;
				}else{
					return getBaliseAIP(name, elevation);
				}
			}else{
				return getBaliseAIP(name, elevation);
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	private LPLNTrackPoint getBaliseAIP(String name, double elevation){
		LPLNTrackPoint p = new LPLNTrackPoint();
		try{
			if(DatabaseManager.getCurrentAIP()!=null){
				Statement st = DatabaseManager.getCurrentAIP();
				ResultSet rs = st.executeQuery("select lat, lon, type from NavFix where nom ='"+name+"' OR nom LIKE '"+name+" - %'");

				Position lastPosition = null;
				//on cherche si on a une balise du type WPT, sinon on prend la dernière.
				while(rs.next()){
					if(rs.getString(3).equals("WPT")){
						p.setPosition(Position.fromDegrees(rs.getDouble(1), rs.getDouble(2), elevation));
						break;
					}
					lastPosition = Position.fromDegrees(rs.getDouble(1), rs.getDouble(2), elevation);
				}
				if(p.getPosition()==null && lastPosition != null){
					p.setPosition(lastPosition);
				}else if(p.getPosition()==null && lastPosition==null){					
					return getBaliseSkyView(name, elevation);
				}
				p.setName(name);
				return p;

			}else{
				return getBaliseSkyView(name, elevation);
			}		
		}catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 
	 * @param name
	 * @param elevation
	 * @return Un LPLNTrackPoint correspondant à la balise ou null si la balise est inconnue de SkyView ou si la base SkyView n'est pas configurée.
	 */
	private LPLNTrackPoint getBaliseSkyView(String name, double elevation){
		LPLNTrackPoint p = null;
		try{
			if(DatabaseManager.getCurrentSkyView()!=null){
				Statement st = DatabaseManager.getCurrentSkyView();
				ResultSet rs = st.executeQuery("select LATITUDE, LONGITUDE from WAYPOINT where IDENT ='"+name+"'");
				if(rs.next()){
					p = new LPLNTrackPoint();
					p.setName(name);
					p.setPosition(new Position(LatLonUtils.computeLatLonFromSkyviewString(rs.getString(1), rs.getString(2)), elevation));
				}
				st.close();
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
		return p;
	}


	private LPLNTrackPoint pointToLPLNPoint(String s, double elevation){
		LPLNTrackPoint trackPoint = null;
		LatLon latlon = LatLonUtils.computeLatLonFromString(s);
		if(latlon != null){
			trackPoint = new LPLNTrackPoint();
			trackPoint.setName(s);
			trackPoint.setPosition(new Position(latlon, elevation));
		}
		return trackPoint;
	}

	/**
	 * 
	 * @param b1
	 * @param b2
	 * @param r le nom de la route
	 * @param elevation l'altitude des points de la route.
	 * @return Les points de la route r entre b1 et b2 exclues. 
	 */
	private LinkedList<LPLNTrackPoint> getRouteBetween(String b1, String b2, String r, double elevation){	
		return getRouteSTIPBetween(b1, b2, r, elevation);
	}

	/**
	 * 
	 * @param b1
	 * @param b2
	 * @param r
	 * @param elevation
	 * @return
	 */
	private LinkedList<LPLNTrackPoint> getRouteSTIPBetween(String b1, String b2, String r, double elevation) {
		try {
			if(DatabaseManager.getCurrentStip() != null){
				boolean routeKnownBySTIP = isRouteKnown(b1, b2, r, DatabaseManager.Type.STIP);
				if(routeKnownBySTIP){
					return findKnownSTIPRoute(b1, b2, r, elevation);
				}else{
					return getRouteAIPBetween(b1, b2, r, elevation);
				}
			}else{
				return getRouteAIPBetween(b1, b2, r, elevation);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	private LinkedList<LPLNTrackPoint> getRouteAIPBetween(String b1, String b2, String r, double elevation){
		try {
			if(DatabaseManager.getCurrentAIP() != null){
				boolean routeKnownByAIP = isRouteKnown(b1, b2, r, DatabaseManager.Type.AIP);
				if(routeKnownByAIP){
					return findKnownAIPRoute(b1, b2, r, elevation);
				}else{
					return getRouteSkyViewBetween(b1, b2, r, elevation);
				}
			}else{
				return getRouteSkyViewBetween(b1, b2, r, elevation);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	private LinkedList<LPLNTrackPoint> getRouteSkyViewBetween(String b1, String b2, String r, double elevation){
		LinkedList<LPLNTrackPoint> pointList = new LinkedList<LPLNTrackPoint>();
		try{
			if(DatabaseManager.getCurrentSkyView() != null){
				Statement st = DatabaseManager.getCurrentSkyView();
				int fromSEQ = -1; 
				int toSEQ = -1;
				boolean fromIsLast = false, toIsFirst = false;
				ResultSet rs1 = st.executeQuery("select SEQ from AIRWAY where IDENT ='"+r+"'AND FROM_FIX_IDENT ='"+b1+"'");
				if(rs1.next()){
					fromSEQ = rs1.getInt(1);
				}
				ResultSet rs2 = st.executeQuery("select SEQ from AIRWAY where IDENT ='"+r+"'AND TO_FIX_IDENT ='"+b2+"'");
				if(rs2.next()){
					toSEQ = rs2.getInt(1);
				}
				if(fromSEQ==-1){
					//Si b2 est la dernière balise de la route dans la table, elle n'apparaîtra pas dans FROM_FIX_IDENT.
					ResultSet rs3 = st.executeQuery("select SEQ from AIRWAY where IDENT ='"+r+"'AND TO_FIX_IDENT ='"+b1+"'");
					if(rs3.next()){
						fromSEQ = rs3.getInt(1);
						fromIsLast = true;
					}
				}
				if(toSEQ==-1){
					//Si b1 est la première balise de la route dans la table, elle n'apparaîtra pas dans TO_FIX_IDENT.
					ResultSet rs4 = st.executeQuery("select SEQ from AIRWAY where IDENT ='"+r+"' AND FROM_FIX_IDENT ='"+b2+"'");
					if(rs4.next()){
						toSEQ = rs4.getInt(1);
						toIsFirst = true;
					}
				}
				if(fromSEQ != -1 && toSEQ!=-1){
					String query = "select ";
					if(fromSEQ < toSEQ && !fromIsLast && !toIsFirst){
						query += "TO_FIX_IDENT from AIRWAY WHERE IDENT ='"+r+"' AND SEQ >="+fromSEQ+" AND SEQ <"+toSEQ+" ORDER BY SEQ ASC";
					}else if(fromSEQ==toSEQ){
						return pointList;
					}else{
						query += "FROM_FIX_IDENT from AIRWAY WHERE IDENT='"+r+"' ";
						if(!fromIsLast && !toIsFirst){
							query +="AND SEQ >"+toSEQ+" AND SEQ <"+fromSEQ+" ";
						}else if(toIsFirst && ! fromIsLast){
							query +="AND SEQ <"+fromSEQ+" ";
						}else if(!toIsFirst && fromIsLast){
							query +="AND SEQ >"+toSEQ+" ";
						}
						query+= "ORDER BY SEQ DESC";
					}
					ResultSet rs5 = st.executeQuery(query);
					LinkedList<String> balises = new LinkedList<String>();
					while(rs5.next()){
						balises.add(rs5.getString(1));
					}
					for (int i = 0; i<balises.size(); i++){
						String balise = balises.get(i);
						LPLNTrackPoint point =  new LPLNTrackPoint();
						ResultSet rs6 = st.executeQuery("select LATITUDE, LONGITUDE from WAYPOINT WHERE IDENT ='"+balise+"'");
						if(rs6.next()){
							point.setPosition(new Position(LatLonUtils.computeLatLonFromSkyviewString(rs6.getString(1), rs6.getString(2)), elevation));
						}
						point.setName(balise);
						pointList.add(point);
					}
					st.close();
				}else{
					st.close();
					pointList.add(new LPLNTrackPoint());
				}
			}else{
				pointList.add(new LPLNTrackPoint());
				return pointList;
			}
		}catch(SQLException e){
			e.printStackTrace();
		}		
		return pointList;
	}


	/**
	 * 
	 * @param b1 le nom d'une balise
	 * @param b2 le nom d'une autre balise
	 * @param r le nom de la route
	 * @param t le type de la base dans laquelle on veut chercher
	 * @return true si la route est connue de la base <code>t</code> et contient les deux balises, false sinon.
	 */
	private boolean isRouteKnown(String b1, String b2, String r, DatabaseManager.Type t){
		try{
			if(DatabaseManager.getCurrent(t) != null){
				Statement st = DatabaseManager.getCurrent(t);
				if(t == DatabaseManager.Type.STIP){
					ResultSet rs = st.executeQuery("select id from routebalise where route ='"+r+"'" +
							" AND (balise='"+b1+"' OR balise='"+b2+"')");
					if(rs.next()){
						if(rs.next())
							return true;
					}
				}else if(t == DatabaseManager.Type.AIP){
					int[] pk = findAIPRouteAndBalises(b1, b2, r);
					if(pk[0]!=-1 && pk[1]!=-1 && pk[2]!=-1){
						ResultSet rs = st.executeQuery("select navFixExtremite from segments where pkRoute ="+pk[0]+" " +
								"AND ( navFixExtremite="+pk[1]+" OR navFixExtremite="+pk[2]+")");
						if(rs.next()){
							int navFixExtremite = rs.getInt(1);
							if(rs.next()){
								return true;
							}else{
								int pkb = pk[1];
								if(navFixExtremite==pk[1])
									pkb=pk[2];
								ResultSet rs2 = st.executeQuery("select pk from routes where pk='"+pk[0]+"'" +
										" AND navFixExtremite="+pkb);	
								if(rs2.next())
									return true;
							}
						}


					}
				}else if(t == DatabaseManager.Type.SkyView){
					ResultSet rs = st.executeQuery("select SEQ from AIRWAY where IDENT ='"+r+"'" +
							" AND (FROM_FIX_IDENT='"+b1+"' OR TO_FIX_IDENT='"+b1+"')");
					if(rs.next()){
						ResultSet rs2 = st.executeQuery("select SEQ from AIRWAY where IDENT ='"+r+"'" +
								" AND (FROM_FIX_IDENT='"+b2+"' OR TO_FIX_IDENT='"+b2+"')");
						if(rs2.next())
							return true;
					}
				}
			}
		}catch(SQLException e){
			e.printStackTrace();
		}
		return false;
	}

	private LinkedList<LPLNTrackPoint> findKnownSTIPRoute(String b1, String b2, String r, double elevation){
		LinkedList<LPLNTrackPoint> pointList = new LinkedList<LPLNTrackPoint>();
		try {
			Statement st = DatabaseManager.getCurrentStip();
			ResultSet rs = st.executeQuery("select balise, appartient from routebalise where route ='"+r+"'");
			boolean between = false;
			int sens = 1;
			while(rs.next()){
				if(rs.getInt(2)!=0){
					String baliseName = rs.getString(1);
					if(between){	
						if(baliseName.equals(b2) || baliseName.equals(b1)){
							between = false;
						}else{
							pointList.add(getBaliseSTIP(baliseName, elevation));
						}
					}else{
						if(baliseName.equals(b1)){
							between = true;
						}
						if(baliseName.equals(b2)){
							between = true;
							sens = -1;
						}
					}
				}
			}
			if(sens<0)
				Collections.reverse(pointList);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return pointList;	
	}

	private LinkedList<LPLNTrackPoint> findKnownAIPRoute(String b1, String b2, String r, double elevation){
		LinkedList<LPLNTrackPoint> pointList = new LinkedList<LPLNTrackPoint>();
		try {
			Statement st = DatabaseManager.getCurrentAIP();
			int[] pk = findAIPRouteAndBalises(b1, b2, r);
			int seq1 = -1, seq2 = -1;
			ResultSet rs = st.executeQuery("select sequence from segments where pkRoute ="+pk[0]+" " +
					" AND navFixExtremite="+pk[1]);
			if(rs.next())
				seq1 = rs.getInt(1);
			ResultSet rs2 = st.executeQuery("select sequence from segments where pkRoute ="+pk[0]+" " +
					" AND navFixExtremite="+pk[2]);
			if(rs2.next())
				seq2 = rs2.getInt(1);
			String query = null;
			if(seq1<seq2)
				query = "select NavFix.nom, NavFix.lat, NavFix.lon from segments, NavFix where pkRoute ="+pk[0]+
				" AND NavFix.pk = segments.navFixExtremite AND sequence>"+seq1+" AND sequence<"+seq2+" ORDER BY sequence";
			else
				query = "select NavFix.nom, NavFix.lat, NavFix.lon from segments, NavFix where pkRoute ="+pk[0]+
				" AND NavFix.pk = segments.navFixExtremite AND sequence>"+seq2+" AND sequence<"+seq1+" ORDER BY sequence DESC";	
			ResultSet rs3 = st.executeQuery(query);
			while(rs3.next()){
				LPLNTrackPoint p = new LPLNTrackPoint();
				p.setName(rs.getString(1));
				p.setPosition(Position.fromDegrees(rs.getDouble(2), rs.getDouble(3), elevation));
				pointList.add(p);
			}		
		}catch (SQLException e) {
			e.printStackTrace();
		}
		return pointList;
	}




	/**
	 * 
	 * @param b1
	 * @param b2
	 * @param r
	 * @return un tableau contenant, dans l'ordre, l'identifiant (pk) de la route, de b1 et de b2.
	 */
	private int[] findAIPRouteAndBalises(String b1, String b2, String r){
		int pk1=-1, pk2=-1, pkr=-1;
		try {
			Statement st = DatabaseManager.getCurrentAIP();
			ResultSet rs = st.executeQuery("select pk from NavFix where nom ='"+b1+"' OR nom LIKE '"+b1+" - %'");

			if(rs.next()){
				pk1 = rs.getInt(1);
				ResultSet rs2 = st.executeQuery("select pk from NavFix where nom ='"+b2+"' OR nom LIKE '"+b2+" - %'");
				if(rs2.next()){
					pk2 = rs2.getInt(1);
					String AIPRouteName = r.charAt(0)+"";
					if(Character.isLetter(r.charAt(1)))
						AIPRouteName += r.charAt(1)+" "+r.substring(2);
					else
						AIPRouteName += " "+r.substring(1);
					ResultSet rs3 = st.executeQuery("select pk from routes where nom='"+AIPRouteName+"' OR nom LIKE'"+AIPRouteName+" - %'");	
					if(rs3.next()){
						pkr = rs3.getInt(1);
					}
				}
			}
		}catch(SQLException e){
			e.printStackTrace();
		}
		return new int[]{pkr,pk1,pk2};
	}

	/**
	 * 
	 * @param e
	 * @param elevation
	 * @return Un triplet contenant le nom de l'élément, son type et son altitude. Il se peut que le triplet ne contienne qu'une altitude.
	 */
	private Triplet<String, Type, Double> findElementTypeAndElevation(String e, double elevation){
		Triplet<String, Type, Double> triplet = new Triplet<String, Type, Double>();
		if(e.matches("\\p{Alpha}{2,5}/([KMN]\\d{3,4})?[SF]\\d{3,4}")){
			String[] baliseNiveau = e.split("/");
			triplet.setFirst(baliseNiveau[0]);
			triplet.setSecond(Type.Balise);
			triplet.setThird(parseElevation(baliseNiveau[1]));
		}else{
			if(e.matches("([KMN]\\d{3,4})?[FS]\\d{3,4}")){
				triplet.setThird(parseElevation(e));
			}else if (e.matches("[A-Z]{1,2}\\d{1,3}")){
				triplet.setFirst(e);
				triplet.setSecond(Type.Route);
				triplet.setThird(elevation);
			}else if(e.matches("\\d{1,3}([\\.,]\\d{1,4})?[NS]\\d{1,3}([\\.,]\\d{1,4})?[EW]")
					|| e.matches("\\d{1,3}D(\\d{1,2}')?(\\d{1,2}\")?[NS],?\\d{1,3}D(\\d{1,2}')?(\\d{1,2}\")?[EW]")){
				triplet.setFirst(e);
				triplet.setSecond(Type.Point);
				triplet.setThird(elevation);
			}else if(e.equals("DCT") || e.equals("NATC")){
				triplet.setThird(elevation);
			}else if(!(e.equals(""))){
				triplet.setFirst(e);
				triplet.setSecond(Type.Balise);
				triplet.setThird(elevation);
			}
		}
		return triplet;
	}


	/**
	 * 
	 * @param s
	 * @return -1 si l'altitude n'a pas pu être déterminé
	 */
	private double parseElevation(String s){
		if(s.matches("([KMN]\\d+)?F\\d+"))
			return Double.parseDouble(s.substring(s.indexOf("F")+1))*30.48;
		else if(s.matches("([KMN]\\d+)?S\\d+"))
			return Double.parseDouble(s.substring(s.indexOf("S")+1))*10;
		return -1;
	}

	/**
	 * Va chercher l'aéroport dans les données AIP d'abord, puis si nécessaire dans les données SkyView et enfin dans les données STIP,
	 * et ajoute un LPLNTrackPoint correspondant à l'aéroport <code>code</code> à la trajectoire <code>track</code>.
	 * @param track
	 * @param code
	 * @return true si l'aéroport a été trouvé dans les données AIP, SkyView ou STIP, false sinon.
	 */
	private boolean addAirportToTrack(LinkedList<LPLNTrackPoint> track, String code){
		LPLNTrackPoint airport = null;
		try{
			if(DatabaseManager.getCurrentAIP()!=null){
				PreparedStatement st = DatabaseManager.prepareStatement(DatabaseManager.Type.AIP, "select latRef, lonRef from aerodromes where code = ?");
				st.setString(1, code);
				ResultSet rs = st.executeQuery();
				if(rs.next()){
					airport = new LPLNTrackPoint();
					airport.setName(code);
					airport.setPosition(Position.fromDegrees(rs.getDouble(1), rs.getDouble(2), 0));
				}
				st.close();
			}else if(DatabaseManager.getCurrentSkyView()!=null && airport == null){
				PreparedStatement st = DatabaseManager.prepareStatement(DatabaseManager.Type.SkyView, "select LATITUDE, LONGITUDE from AIRPORT where ident = ?");
				st.setString(1, code);
				ResultSet rs = st.executeQuery();
				if(rs.next()){
					airport = new LPLNTrackPoint();
					airport.setName(code);
					airport.setPosition(new Position(LatLonUtils.computeLatLonFromSkyviewString(rs.getString(1), rs.getString(2)), 0));
				}
				st.close();
			}else if(DatabaseManager.getCurrentStip()!=null && airport == null){
				PreparedStatement st = DatabaseManager.prepareStatement(DatabaseManager.Type.STIP, "select LATITUDE, LONGITUDE from balises where name = ?");
				st.setString(1, code);
				ResultSet rs = st.executeQuery();
				if(rs.next()){
					airport = new LPLNTrackPoint();
					airport.setName(code);
					airport.setPosition(Position.fromDegrees(rs.getDouble(1), rs.getDouble(2), 0));
				}
				st.close();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		if(airport !=null){
			track.add(airport);
			return true;
		}
		return false;
	}

	
	public class UnrecognizedFPLException extends Exception{
		
		private String message = "<html>Plan de vol ";
		
		/**
		 * 
		 * @param firstLine la première ligne du plan de vol
		 * @param type
		 */
		public UnrecognizedFPLException(String firstLine){
			message += "<i><b><font color=\"#771111\">"+firstLine.replace("(FPL", "")+"</font></b></i> : <br/> " +
					"Format incorrect ou route inconnue.</html>";
		}
		
		public String getMessage(){
			return message;
		}
	}


	@Override
	protected boolean isTrackValid(VidesoTrack track) {
		// TODO Auto-generated method stub
		return true;
	}
	
}