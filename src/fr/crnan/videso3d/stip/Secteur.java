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

import fr.crnan.videso3d.pays.Pays;
import gov.nasa.worldwind.geom.LatLon;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Secteur de contrôle
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class Secteur {

	/**
	 * Connection vers la base de données
	 */
	private Statement st;
	/**
	 * Connection vers une base de données PAYS
	 */
	private Statement pays;
	
	/**
	 * Nom du secteur
	 */
	private String name;
	/**
	 * Numéro du secteur
	 */
	private Integer numero;
	
	/**
	 * Crée un secteur
	 * @param name Nom du secteur
	 */
	public Secteur(String name, Integer id, Statement st){
		this.name = name;
		this.setConnection(st);
		if(!secteurExists()){
			throw new NoSuchElementException("Secteur "+name+" inconnu.");
		} else {
			this.numero = id;
		}
	}
	
	/**
	 * Vérifie si le nom du secteur existe dans la base de données
	 * @return Boolean True si le secteur existe dans la base de données
	 */
	private boolean secteurExists() {
		Boolean r = false;
		try {
			ResultSet result = st.executeQuery("select nom from secteurs where nom = '"+this.name+"'");
			r = result.next();
			result.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return r;
	}

	/**
	 *  Met en place une connection vers une base de données Stip
	 * @param conn Connection vers une base de données Stip
	 */
	public void setConnection(Statement st){
		this.st = st;
	}
	/**
	 * Met en place une connection vers une base de données de type PAYS
	 * Nécessaire pour créer les contours d'un secteur
	 * @param conn Connection vers une base de données PAYS
	 */
	public void setConnectionPays(Statement st){
		this.pays = st;
	}
	
	/**
	 * Retourne la liste des points formant le contour du secteur au niveau spécifié
	 * Nécessite une base PAYS
	 * @return {@link Iterable} Liste des coordonnées du contour
	 */
	public Iterable<? extends LatLon> getContour(int flsup){
		List<LatLon> loc = new LinkedList<LatLon>();
		try {
			ResultSet rs = this.st.executeQuery("select refcontour, pointref, latitude, longitude from cartepoint, poinsect where cartepoint.pointref = poinsect.ref and sectnum ='"+ numero +"' and flsup = '"+ flsup +"'");
			String pointFrontiere = "";
			String refContour = "";
			String firstPoint = "";
			while(rs.next()){
				//on enregistre la référence du premier point
				if(rs.isFirst()){
					firstPoint = rs.getString("pointref");
				}
				if(rs.getString("pointref").startsWith("F")){//point frontière
					if(refContour.isEmpty()){
						if(!(rs.getString("refcontour")).isEmpty()){//point frontière avec contour
							//initilisation, on attend la deuxième ligne pour dessiner
							refContour = rs.getString("refcontour");
							pointFrontiere = rs.getString("pointref");
						}
					} else {
						if((rs.getString("refcontour")).isEmpty()){//point frontière sans contour
							loc.addAll(Pays.getContour(refContour, pointFrontiere, rs.getString("pointref"), this.pays));
							refContour = "";
							pointFrontiere = "";
						} else { //point frontiere avec contour : tracé du sous contour précédent jusqu'au point frontière suivant
							loc.addAll(Pays.getContour(refContour, pointFrontiere, rs.getString("pointref"), this.pays));
							refContour = rs.getString("refcontour");
							pointFrontiere = rs.getString("pointref");
						}
					}
				} else {//point normal
					loc.add(LatLon.fromDegrees(rs.getDouble("latitude"), rs.getDouble("longitude")));
				}
				
			}
			//cas particulier du dernier point, si c'est un point frontière avec contour
			if(!refContour.isEmpty()){	
				loc.addAll(Pays.getContour(refContour, pointFrontiere, firstPoint, this.pays));
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return loc;
	}
}
