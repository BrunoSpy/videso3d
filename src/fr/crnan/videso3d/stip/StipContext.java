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

import java.awt.event.ActionEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JLabel;

import org.jdesktop.swingx.JXTaskPane;

import fr.crnan.videso3d.Context;
import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.DatabaseManager.Type;
import fr.crnan.videso3d.DatasManager;
import fr.crnan.videso3d.geom.LatLonCautra;
import fr.crnan.videso3d.geom.Latitude;
import fr.crnan.videso3d.geom.Longitude;
import fr.crnan.videso3d.ihm.AnalyzeUI;
/**
 * Informations contextuelles des objets STIP
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class StipContext extends Context {

	private StipController getController(){
		return (StipController) DatasManager.getController(Type.STIP);
	}

	@Override
	public List<JXTaskPane> getTaskPanes(int type, final String name) {
		List<JXTaskPane> taskpanes = new LinkedList<JXTaskPane>();
		switch (type) {
		case StipController.SECTEUR:
			JXTaskPane taskpane1 = new JXTaskPane();
			taskpane1.setTitle("Infos générales Stip");
			try {
				final Statement st = DatabaseManager.getCurrentStip();
				ResultSet rs = st.executeQuery("select * from secteurs where nom='"+name+"'");
				taskpane1.add(new JLabel("<html><b>Espace</b> : "+(rs.getString(4).equals("U")?"UIR":"FIR")+"</html>"));
				taskpane1.add(new JLabel("<html><b>Appartient au centre</b> : "+rs.getString(3)+"</html>"));
				taskpane1.add(new JLabel("<html><b>Plafond</b> : "+rs.getString(7)+"</html>"));
				taskpane1.add(new JLabel("<html><b>Plancher</b> : "+rs.getString(6)+"</html>"));
				taskpane1.add(new JLabel("<html><b>Mode S</b> : "+(rs.getBoolean(8)?"Oui":"Non")));
			} catch (SQLException e){
				e.printStackTrace();
			}
			taskpanes.add(taskpane1);

			JXTaskPane taskpane2 = new JXTaskPane();
			taskpane2.setTitle("Eléments Stip");
			try {
				final Statement st = DatabaseManager.getCurrentStip();
				ResultSet rs = st.executeQuery("select * from secteurs where nom='"+name+"'");
				rs = st.executeQuery("select name from balises where sect1 ='"+name+"' or " +
						"sect2 ='"+name+"' or " +
						"sect3 ='"+name+"' or " +
						"sect4 ='"+name+"' or " +
						"sect5 ='"+name+"' or " +
						"sect6 ='"+name+"' or " +
						"sect7 ='"+name+"' or " +
						"sect8 ='"+name+"' or " +
						"sect9 ='"+name+"'");

				final List<String> balises = new LinkedList<String>();
				while(rs.next()){
					balises.add(rs.getString(1));
				}
				taskpane2.add(new AbstractAction() {
					boolean show = true;
					{
						putValue(Action.NAME, "Afficher les "+balises.size()+" balises.");
					}
					@Override
					public void actionPerformed(ActionEvent e) {
						if(show){
							for(String name : balises){
								getController().showObject(StipController.BALISES, name);
							}
							putValue(Action.NAME, "Cacher les "+balises.size()+" balises.");
							show = false;
						} else {
							for(String name : balises){
								getController().hideObject(StipController.BALISES, name);
							}
							putValue(Action.NAME, "Afficher les "+balises.size()+" balises.");
							show = true;
						}
					}
				});
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			taskpanes.add(taskpane2);
			break;
			
		case StipController.BALISES:
			taskpane1 = new JXTaskPane();
			taskpane1.setTitle("Infos générales Stip");

			try{
				final Statement st = DatabaseManager.getCurrentStip();
				ResultSet rs = st.executeQuery("select * from balises where name='"+name+"'");
				final LatLonCautra coor = LatLonCautra.fromDegrees(rs.getDouble("latitude"), rs.getDouble("longitude"));
				Latitude lat = new Latitude(coor.getLatitude().degrees);
				Longitude lon = new Longitude(coor.getLongitude().degrees);
				final String latitude = lat.getDegres()+"°"+lat.getMinutes()+"\'"+lat.getSecondes()+"\" N";
				final String longitude = Math.abs(lon.getDegres())+"°"+lon.getMinutes()+"\'"+lon.getSecondes()+"\""+ (lon.getDegres() < 0 ? "E" : "O");

				final boolean pub = rs.getBoolean(3);
				taskpane1.add(new JLabel("<html><b>Publiée</b> : "+(pub?"oui":"non")+"</html>"));
				taskpane1.add(new JLabel("<html><b>Commentaires</b> : "+ rs.getString("definition")+"</html>"));
				taskpane1.add(new JLabel("<html><b>Cooordonnées</b> :</html>"));
				taskpane1.add(new AbstractAction() {
					{
						putValue(Action.NAME, "  WGS84 : "+latitude+", "+longitude);
						putValue(Action.SHORT_DESCRIPTION, "Centrer le globe sur ces coordonnées.");
					}
					@Override
					public void actionPerformed(ActionEvent e) {
						getController().highlight(StipController.BALISES, name);
					}
				});
				taskpane1.add(new AbstractAction() {
					{
						putValue(Action.NAME, "  Cautra : X: "+String.format("%7.2f",coor.getCautra()[0])+" Y: "+String.format("%7.2f",coor.getCautra()[1]));
						putValue(Action.SHORT_DESCRIPTION, "Centrer le globe sur ces coordonnées.");
					}
					@Override
					public void actionPerformed(ActionEvent e) {
						getController().highlight(StipController.BALISES, name);
					}
				});
				taskpane1.add(new JLabel("<html><b>Affectée au centre</b> : "+rs.getString("centre")+"</html>"));
				taskpane1.add(new JLabel("<html><b>Affectée aux secteurs</b> :</html>"));

				int plafond = -1;
				String secteur = null;
				for(int i = 9; i>= 1; i--){
					int plancher = rs.getInt("limit"+i);
					if(plancher != -1){
						if(secteur!=null){
							final int tPlafond = plafond;
							final int tPlancher = plancher;
							final String tSecteur = secteur;
							taskpane1.add(new AbstractAction() {
								{
									putValue(Action.NAME, "\nDu "+tPlafond+" au "+tPlancher+" : "+tSecteur);
								}
								@Override
								public void actionPerformed(ActionEvent e) {
									getController().highlight(StipController.SECTEUR, tSecteur);
								}
							});
						}	
						plafond = plancher;
						secteur = rs.getString("sect"+i);
					}
				}
				if(secteur != null) {
					final int tPlafond = plafond;
					final String tSecteur = secteur;
					taskpane1.add(new AbstractAction() {
						{
							putValue(Action.NAME, "\nDu "+tPlafond+" au 0 : "+tSecteur);
						}
						@Override
						public void actionPerformed(ActionEvent e) {
							getController().highlight(StipController.SECTEUR, tSecteur);
						}
					});
				}
			} catch (SQLException e2){
				e2.printStackTrace();
			}
			taskpanes.add(taskpane1);

			taskpane2 = new JXTaskPane();
			taskpane2.setTitle("Eléments Stip");
			try{
				final Statement st = DatabaseManager.getCurrentStip();		
				taskpane2.add(new AbstractAction() {
					{
						putValue(Action.NAME, "Appartient à "+(st.executeQuery("select COUNT(*) from routebalise where balise = '"+name+"'")).getInt(1)+" routes.");
					}
					@Override
					public void actionPerformed(ActionEvent e) {
						AnalyzeUI.showResults("route", name, "");
					}
				});

				taskpane2.add(new AbstractAction() {
					{
						putValue(Action.NAME, "Appartient à "+(st.executeQuery("select COUNT(*) from balitis where balitis.balise = '"+name+"'")).getInt(1)+" itis.");
					}
					@Override
					public void actionPerformed(ActionEvent e) {
						AnalyzeUI.showResults("iti", name, "");
					}
				});

				taskpane2.add(new AbstractAction() {
					{
						putValue(Action.NAME, "Appartient à "+(st.executeQuery("select COUNT(*) from baltrajets where baltrajets.balise = '"+name+"'")).getInt(1)+" trajets.");
					}
					@Override
					public void actionPerformed(ActionEvent e) {
						AnalyzeUI.showResults("trajet", name, "");
					}
				});

				taskpane2.add(new AbstractAction() {
					{
						putValue(Action.NAME, "Appartient à "+(st.executeQuery("select COUNT(*) from (select distinct balconnexions.idconn from connexions, balconnexions where connexions.id = balconnexions.idconn and (balconnexions.balise = '"+name+"' or connexions.connexion = '"+name+"'))")).getInt(1)
								+" connexions.");
					}
					@Override
					public void actionPerformed(ActionEvent e) {
						AnalyzeUI.showResults("connexion", name, "");
					}
				});

				taskpane2.add(new AbstractAction() {
					{
						putValue(Action.NAME, "Appartient à "+(st.executeQuery("select COUNT(*) from balint where bal1 = '"+name+"' or bal2='"+name+"' or balise='"+name+"'")).getInt(1)+" balint.");
					}
					@Override
					public void actionPerformed(ActionEvent e) {
						AnalyzeUI.showResults("balise", name, "");
					}
				});

				taskpane2.add(new AbstractAction() {
					{
						putValue(Action.NAME, "Possède "+(st.executeQuery("select COUNT(*) from consignes where balise='"+name+"' or oaci='"+name+"'" )).getInt(1)+" consignes.");
					}
					@Override
					public void actionPerformed(ActionEvent e) {
						AnalyzeUI.showResults("balise", name, "");
					}
				});
			} catch(SQLException e) {
				e.printStackTrace();
			}
			break;
		case StipController.ROUTES:
			taskpane1 = new JXTaskPane();
			taskpane1.setTitle("Infos générales Stip");

			try {
				Statement st = DatabaseManager.getCurrentStip();
				int id = st.executeQuery("select id from routes where name ='"+name+"'").getInt(1);
				ResultSet rs = st.executeQuery("select * from routes where id='"+id+"'");		
				taskpane1.add(new JLabel("<html><b>Espace</b> : "+(rs.getString(3).equals("U")?"UIR":"FIR")+"</html>"));

				taskpane1.add(new AbstractAction() {
					{
						putValue(Action.NAME, "<html>Afficher/centrer la route.</html>");
					}
					@Override
					public void actionPerformed(ActionEvent arg0) {
						getController().highlight(StipController.ROUTES, name);
					}
				});
				taskpane1.add(new AbstractAction() {
					boolean show = true;
					{
						putValue(Action.NAME, "<html>Afficher les balises.</html>");
					}
					@Override
					public void actionPerformed(ActionEvent arg0) {
						if(show) {
							((StipController) getController()).showRoutesBalises(name);
							putValue(Action.NAME, "<html>Cacher les balises.</html>");
							show = false;
						} else {
							((StipController) getController()).hideRoutesBalises(name);
							putValue(Action.NAME, "<html>Afficher les balises.</html>");
							show = true;
						}
					}
				});
			} catch (SQLException e) {
				e.printStackTrace();
			}
			taskpanes.add(taskpane1);
			break;
		default:
			break;
		}
		return taskpanes;
	}

}
