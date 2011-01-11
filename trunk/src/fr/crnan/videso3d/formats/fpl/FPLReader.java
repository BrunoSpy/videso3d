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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Vector;

import javax.swing.ProgressMonitorInputStream;

import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.formats.TrackFilesReader;
import fr.crnan.videso3d.formats.lpln.LPLNTrackPoint;
import fr.crnan.videso3d.geom.LatLonUtils;
import gov.nasa.worldwind.geom.Position;

/**
 * Lecteur de plans de vol importés d'IvanWeb.
 * @author Adrien Vidal
 *
 */
public class FPLReader extends TrackFilesReader {
	
	private int trackLost = 0;
	
	public FPLReader(File selectedFile) {
		super(selectedFile);
	}

	public FPLReader(Vector<File> files){
		super(files);
	}
	
	
	
	public static Boolean isLPLNFile(File file){
		Boolean fpl = false;
		try {
			BufferedReader in = new BufferedReader(
					new InputStreamReader(
							new FileInputStream(file)));
			int count = 0; //nombre de lignes lues
			while(in.ready() && !fpl && count < 10){
				String line = in.readLine();
				if(line.startsWith("(FPL-")){
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
	
	
	@Override
	protected void doReadStream(FileInputStream stream) {
		String line;

		BufferedReader in = new BufferedReader(
				new InputStreamReader(
						new ProgressMonitorInputStream(null, 
								"Extraction du fichier FPL ...",
								stream)));

		try{
			String firstLine = null;
			while(in.ready()){
				if(firstLine == null){
					line = in.readLine();
				}else{
					line = firstLine;
				}
				if(line.startsWith("(FPL-")){
					LinkedList<String> fpl = new LinkedList<String>();
					fpl.add(line);
					boolean endOfFPL = false;
					while(in.ready() && !endOfFPL){
						line = in.readLine();
						if(line.startsWith("(FPL-")){
							endOfFPL = true;
							firstLine = line;
						}else{
							if(!line.matches("\\s*")){
								fpl.add(line);
							}
						}
					}
					parseFPL(fpl);
				}
			}
		}catch(NoSuchElementException e){
			e.printStackTrace();
			return;
		}catch(IOException e){
			e.printStackTrace();
			return;
		}
	}
	/**
	 * Lit le plan de vol pour construire la trajectoire (<code>FPLTrack</code>) correspondante.
	 * @param fpl Les lignes qui composent le plan de vol
	 */
	public void parseFPL(LinkedList<String> fpl){
		String indicatif = fpl.getFirst().split("-")[1];
		String type = fpl.get(1).substring(1, 5);
		String depart = fpl.get(2).substring(1,5);
		String arrivee = fpl.getLast().substring(1,5);
		FPLTrack track = new FPLTrack(indicatif);
		track.setIndicatif(indicatif);
		track.setType(type);
		track.setDepart(depart);
		track.setArrivee(arrivee);
		addAirportToTrack(track, depart);
		double elevation = 0;
		for(int i = 3; i<(fpl.size()-1); i++){
			//Si deux balises consécutives sont inconnues du STIP et de SkyView, on abandonne et on arrête la route ici.
			if(trackLost>2 && track.getNumPoints()>1)
				break;
			String line = fpl.get(i);
			String route = null;
			String balisePrecedente = "";
			if(line.startsWith("-")){
				line = line.substring(1);
			}
			String[] elements = line.split(" |/");
			for(String e : elements){
				if(trackLost>2 && track.getNumPoints()>1){
					break;
				}
				if(e.matches("[KMN]\\d+F\\d+")){
					elevation = Double.parseDouble(e.substring(6))*30.48;
				}else if(e.matches("[KMN]\\d+S\\d+")){
					elevation = Double.parseDouble(e.substring(6))*10;
				}else if (e.matches("[A-Z]{1,2}\\d+")){
					route = e;
				}else if(!e.equals("DCT") && !(e.equals(""))){
					try {
						balisePrecedente = addBalisesToTrack(track, e, balisePrecedente, route, elevation);
					} catch (SQLException exc) {
						exc.printStackTrace();
					}
					route = null;
				}
			}
		}	
		trackLost = 0;
		addAirportToTrack(track, arrivee);
		if(track.getNumPoints()>0)
			this.getTracks().add(track);
	}
	
	/**
	 * Va chercher les coordonnées de la balise dans la base STIP et renvoie un LPLNTrackPoint correspondant à la balise, au niveau précisé en paramètre.
	 * @param balise
	 * @param elevation
	 * @return 
	 */
	private LPLNTrackPoint baliseName2TrackPoint(String balise, double elevation){
		LPLNTrackPoint point = null;
		try {		
			Statement st = DatabaseManager.getCurrentStip();
			ResultSet rs = st.executeQuery("select latitude, longitude from balises where name ='"+balise+"'");
			if(rs.next()){
				point =  new LPLNTrackPoint();
				point.setName(balise);
				point.setPosition(Position.fromDegrees(rs.getDouble(1), rs.getDouble(2), elevation));
			};
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return point;
	}
	
	
	/**
	 * Va chercher l'aéroport dans les données STIP d'abord, puis si nécessaire dans les données SkyView et ajoute un LPLNTrackPoint correspondant à 
	 * l'aéroport à la trajectoire <code>track</code>.
	 * @param track
	 * @param code
	 */
	private void addAirportToTrack(FPLTrack track, String code){
		LPLNTrackPoint airport = null;
		try{
			Statement st = DatabaseManager.getCurrentAIP();
			ResultSet rs = st.executeQuery("select latRef, lonRef from aerodromes where code ='"+code+"'");
			if(rs.next()){
				airport = new LPLNTrackPoint();
				airport.setName(code);
				airport.setPosition(Position.fromDegrees(rs.getDouble(1), rs.getDouble(2), 0));
			}else if(DatabaseManager.getCurrentSkyView()!=null){
				ResultSet rs2 = DatabaseManager.getCurrentSkyView().executeQuery("select LATITUDE, LONGITUDE from AIRPORT where ident='"+code+"'");
				if(rs2.next()){
					airport = new LPLNTrackPoint();
					airport.setName(code);
					airport.setPosition(new Position(LatLonUtils.computeLatLonFromSkyviewString(rs2.getString(1), rs2.getString(2)), 0));
				}
			}
		}catch(SQLException e){
			e.printStackTrace();
		}
		if(airport !=null)
			track.addPoint(airport);
	}
	
	/**
	 * Ajoute la balise <code>balise</code> à la trajectoire <code>track</code>, et ajoute également toutes les balises qui se trouvent 
	 * sur la <code>route</code> entre <code>balise</code> et <code>balisePrecedente</code>.
	 * @param track
	 * @param balise
	 * @param balisePrecedente
	 * @param route
	 * @param elevation
	 * @return la nouvelle valeur de balisePrecedente
	 * @throws SQLException 
	 */
	private String addBalisesToTrack(FPLTrack track, String balise, String balisePrecedente, String route, double elevation) throws SQLException{
		LPLNTrackPoint point = baliseName2TrackPoint(balise, elevation);
		boolean skyViewPoint = false;
		if(point == null && DatabaseManager.getCurrentSkyView() != null){
				ResultSet rs = DatabaseManager.getCurrentSkyView().executeQuery("select * from WAYPOINT where IDENT='"+balise+"'");
				if(rs.next()){
					point = new LPLNTrackPoint();
					point.setName(balise);
					point.setPosition(new Position(LatLonUtils.computeLatLonFromSkyviewString(rs.getString(7), rs.getString(8)), elevation));
					skyViewPoint = true;
				}
		}
		if(point != null){	
			if(route == null || balisePrecedente == null){
				track.addPoint(point);
				//Si la route demandée dans le plan de vol n'est pas connue (balise inconnue ou route inconnue), on le signale à la trajectoire track.
				if(trackLost>0)
					track.setSegmentIncertain(balise);
			}else{
				List<LPLNTrackPoint> points = null;
				if(skyViewPoint){
					points = getSkyViewPointsBetween(balisePrecedente, balise, route, elevation);
				}else{
					points = getPointsBetween(balisePrecedente, balise, route, elevation);
				}
				for(LPLNTrackPoint p : points){
					track.addPoint(p);
				}
			}
			balisePrecedente = balise;
		}else{
			trackLost+=1;
			balisePrecedente = null;
		}
		return balisePrecedente;
	}
	
	/**
	 * 
	 * @param b1
	 * @param b2
	 * @param route
	 * @param elevation
	 * @return Les balises sur la route <code>route</code> comprises entre les balises b1 exclue et b2 incluse dans les données STIP.
	 */
	private List<LPLNTrackPoint> getPointsBetween(String b1, String b2, String route, double elevation){
		LinkedList<LPLNTrackPoint> pointList = new LinkedList<LPLNTrackPoint>();
		try {		
			Statement st = DatabaseManager.getCurrentStip();
			ResultSet rs = st.executeQuery("select balise, appartient from routebalise where route ='"+route+"'");
			boolean between = false;
			int sens = 1;
			while(rs.next()){
				if(rs.getInt(2)!=0){
					String baliseName = rs.getString(1);
					if(between){
						LPLNTrackPoint p = baliseName2TrackPoint(baliseName, elevation);
						if(p!=null){
							pointList.add(p);
						}
						if(baliseName.equals(b2) || baliseName.equals(b1)){
							between = false;
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
	
	/**
	 * 
	 * @param b1
	 * @param b2
	 * @param route
	 * @param elevation
	 * @return Les balises sur la route <code>route</code> comprises entre les balises b1 exclue et b2 incluse dans les données SkyView.
	 */
	private List<LPLNTrackPoint> getSkyViewPointsBetween(String b1, String b2, String route, double elevation){
		LinkedList<LPLNTrackPoint> pointList = new LinkedList<LPLNTrackPoint>();
		try {	
			Statement st = DatabaseManager.getCurrentSkyView();
			if(st==null)
				return pointList;
			int fromSEQ = -1; 
			int toSEQ = -1;
			boolean fromIsLast = false, toIsFirst = false;
			ResultSet rs1 = st.executeQuery("select SEQ from AIRWAY where IDENT ='"+route+"'AND FROM_FIX_IDENT ='"+b1+"'");
			if(rs1.next()){
				fromSEQ = rs1.getInt(1);
			}
			ResultSet rs2 = st.executeQuery("select SEQ from AIRWAY where IDENT ='"+route+"'AND TO_FIX_IDENT ='"+b2+"'");
			if(rs2.next()){
				toSEQ = rs2.getInt(1);
			}
			if(fromSEQ==-1){
				//Si b2 est la dernière balise de la route dans la table, elle n'apparaîtra pas dans FROM_FIX_IDENT.
				ResultSet rs3 = st.executeQuery("select SEQ from AIRWAY where IDENT ='"+route+"'AND TO_FIX_IDENT ='"+b1+"'");
				if(rs3.next()){
					fromSEQ = rs3.getInt(1);
					fromIsLast = true;
				}
			}
			if(toSEQ==-1){
				//Si b1 est la première balise de la route dans la table, elle n'apparaîtra pas dans TO_FIX_IDENT.
				ResultSet rs4 = st.executeQuery("select SEQ from AIRWAY where IDENT ='"+route+"' AND FROM_FIX_IDENT ='"+b2+"'");
				if(rs4.next()){
					toSEQ = rs4.getInt(1);
					toIsFirst = true;
				}
			}
			if(fromSEQ != -1 && toSEQ!=-1){
				String query = "select ";
				if(fromSEQ < toSEQ && !fromIsLast && !toIsFirst){
					query += "TO_FIX_IDENT from AIRWAY WHERE IDENT ='"+route+"' AND SEQ >="+fromSEQ+" AND SEQ <="+toSEQ+" ORDER BY SEQ ASC";
				}else{
					query += "FROM_FIX_IDENT from AIRWAY WHERE IDENT='"+route+"' ";
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
				for (String balise : balises){
					ResultSet rs6 = st.executeQuery("select LATITUDE, LONGITUDE from WAYPOINT WHERE IDENT ='"+balise+"'");
					if(rs6.next()){
						LPLNTrackPoint point =  new LPLNTrackPoint();
						point.setName(balise);
						point.setPosition(new Position(LatLonUtils.computeLatLonFromSkyviewString(rs6.getString(1), rs6.getString(2)), elevation));
						pointList.add(point);
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return pointList;		
	}
}
