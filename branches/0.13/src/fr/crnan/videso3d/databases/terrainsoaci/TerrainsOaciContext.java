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

package fr.crnan.videso3d.databases.terrainsoaci;

import java.awt.event.ActionEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JLabel;

import org.jdesktop.swingx.JXTaskPane;

import fr.crnan.videso3d.Context;
import fr.crnan.videso3d.DatasManager;
import fr.crnan.videso3d.databases.DatabaseManager;
import fr.crnan.videso3d.databases.terrainsoaci.TerrainsOaciController;
import fr.crnan.videso3d.geom.LatLonCautra;
import fr.crnan.videso3d.geom.Latitude;
import fr.crnan.videso3d.geom.Longitude;
/**
 * Informations contextuelles des Terrains OACI
 * @author David Granado
 * @version 0.1
 */
public class TerrainsOaciContext extends Context {

	private TerrainsOaciController getController() {
		return (TerrainsOaciController) DatasManager.getController(DatasManager.Type.TerrainsOACI);
	}
	
	@Override
	public List<JXTaskPane> getTaskPanes(int type, final String idoaci) {
		JXTaskPane taskpane = new JXTaskPane();
		taskpane.setTitle("Terrain OACI");
		try{
			final Statement st = DatabaseManager.getCurrentTerrainsOACI();
			ResultSet rs = st.executeQuery("select * from terrainsoaci where idoaci='"+idoaci+"'");
			if(rs.next()){
				final LatLonCautra coor = LatLonCautra.fromDegrees(rs.getDouble("latitude"), rs.getDouble("longitude"));
				Latitude lat = new Latitude(coor.getLatitude().degrees);
				Longitude lon = new Longitude(coor.getLongitude().degrees);
				final String latitude = lat.getDegres()+"°"+lat.getMinutes()+"\'"+lat.getSecondes()+"\""+ lat.getSens();
				final String longitude = Math.abs(lon.getDegres())+"°"+lon.getMinutes()+"\'"+lon.getSecondes()+"\""+ lon.getSens();
				taskpane.add(new JLabel("<html><b>Nom</b> : "+ rs.getString("name")+"</html>"));
				taskpane.add(new JLabel("<html><b>Code OACI</b> : "+ rs.getString("idoaci")+"</html>"));
				taskpane.add(new JLabel("<html><b>Code IATA</b> : "+ (rs.getString("idiata")==null ? "Non renseigné" : rs.getString("idiata"))+"</html>"));
				String tertyp = rs.getString("type");
				switch (tertyp) {
				case "AD": tertyp = "Aérodrome"; break;
				case "HP": tertyp = "Héliport"; break;
				case "AH": tertyp = "Aérodrome avec zone hélicoptère"; break;
				case "LS": tertyp = "Zone d'atterrissage"; break;
				default : tertyp = "Non renseigné"; break;
				}
				taskpane.add(new JLabel("<html><b>Type</b> : "+ tertyp+"</html>"));
				taskpane.add(new JLabel("<html><b>Pays</b> : "+ rs.getString("country")+"</html>"));
				taskpane.add(new JLabel("<html><b>Ville desservie</b> : "+ (rs.getString("city")==null ? "Non renseignée" : rs.getString("city"))+"</html>"));
				taskpane.add(new JLabel("<html><b>Cooordonnées</b> :</html>"));
				taskpane.add(new AbstractAction() {
					{
						putValue(Action.NAME, "  WGS84 : "+latitude+", "+longitude);
						putValue(Action.SHORT_DESCRIPTION, "Centrer le globe sur ces coordonnées.");
					}
					@Override
					public void actionPerformed(ActionEvent e) {
						getController().highlight(idoaci);
					}
				});
				taskpane.add(new AbstractAction() {
					{
						putValue(Action.NAME, "  Cautra : X: "+String.format("%7.2f",coor.getCautra()[0])+" Y: "+String.format("%7.2f",coor.getCautra()[1]));
						putValue(Action.SHORT_DESCRIPTION, "Centrer le globe sur ces coordonnées.");
					}
					@Override
					public void actionPerformed(ActionEvent e) {
						getController().highlight(idoaci);
					}
				});
			}
		} catch (SQLException e){
			e.printStackTrace();
		}
		List<JXTaskPane> list = new ArrayList<JXTaskPane>();
		list.add(taskpane);
		return list;
	}
}
