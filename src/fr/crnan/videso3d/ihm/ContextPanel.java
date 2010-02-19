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

package fr.crnan.videso3d.ihm;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;

import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.geom.LatLonCautra;
import fr.crnan.videso3d.geom.Latitude;
import fr.crnan.videso3d.geom.Longitude;
import fr.crnan.videso3d.graphics.Balise2D;
import fr.crnan.videso3d.graphics.Secteur3D;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
/**
 * Panel d'infos contextuelles
 * @author Bruno Spyckerelle
 * @version 0.3
 */
public class ContextPanel extends JPanel implements SelectListener {

	private JXTaskPaneContainer content = new JXTaskPaneContainer();	
	
	private TitledPanel titleAreaPanel = new TitledPanel("Informations");

	public ContextPanel(){
		super();
		this.setPreferredSize(new Dimension(300, 0));
		this.setLayout(new BorderLayout());

		this.add(titleAreaPanel, BorderLayout.NORTH);

		this.add(content, BorderLayout.CENTER);
	}

	/**
	 * Ouvre le panneau si le parent est un {@link JSplitPane}
	 */
	public void open(){
		if(this.getParent() instanceof JSplitPane) {
			if(((JSplitPane)this.getParent()).getLeftComponent().equals(this)){
				((JSplitPane)this.getParent()).setDividerLocation(250);
			} else {
				((JSplitPane)this.getParent()).setDividerLocation(this.getParent().getWidth()-250);
			}
		}
	}

	@Override
	public void selected(SelectEvent event) {
		if(event.getEventAction() == SelectEvent.LEFT_DOUBLE_CLICK){
			this.open();
			if(event.getTopObject() instanceof Balise2D){
				this.showBalise(((Balise2D)event.getTopObject()).getName());
			} else if(event.getTopObject() instanceof Secteur3D){
				this.showSecteur(((Secteur3D)event.getTopObject()).getName());
			}
		}
	}

	/**
	 * Affiche les informations de la balise <code>name</code>
	 * @param name
	 */
	public void showBalise(final String name){		
		titleAreaPanel.setTitle("Balise : "+name);
		content.removeAll();
		try {
			final Statement st = DatabaseManager.getCurrentStip();
			ResultSet rs = st.executeQuery("select * from balises where name='"+name+"'");
			final LatLonCautra coor = LatLonCautra.fromDegrees(rs.getDouble("latitude"), rs.getDouble("longitude"));
			Latitude lat = new Latitude(coor.getLatitude().degrees);
			Longitude lon = new Longitude(coor.getLongitude().degrees);
			final String latitude = lat.getDegres()+"°"+lat.getMinutes()+"\'"+lat.getSecondes()+"\" N";
			final String longitude = Math.abs(lon.getDegres())+"°"+lon.getMinutes()+"\'"+lon.getSecondes()+"\""+ (lon.getDegres() < 0 ? "E" : "O");

			JXTaskPane balise = new JXTaskPane();
			balise.setTitle("Informations générales");
			balise.add(new JLabel("<html><b>Commentaires</b> : "+ rs.getString("definition")+"</html>"));
			balise.add(new JLabel("<html><b>Cooordonnées</b> :</html>"));
			balise.add(new AbstractAction() {
				{
					putValue(Action.NAME, "  WGS84 : "+latitude+", "+longitude);
				}
				@Override
				public void actionPerformed(ActionEvent e) {
					//TODO centrer la vue sur ces coordonnées
				}
			});
			balise.add(new AbstractAction() {
				{
					putValue(Action.NAME, "  Cautra : X: "+String.format("%7.2f",coor.getCautra()[0])+" Y: "+String.format("%7.2f",coor.getCautra()[1]));
				}
				@Override
				public void actionPerformed(ActionEvent e) {
					//TODO centrer la vue sur ces coordonnées
				}
			});
			balise.add(new JLabel("<html><b>Affectée au centre</b> : "+rs.getString("centre")+"</html>"));
			balise.add(new JLabel("<html><b>Affectée aux secteurs</b> :</html>"));
			int plancher = 0;
			for(int i = 1; i<= 9; i++){
				int plafond = rs.getInt("limit"+i);
				if(plafond != -1) balise.add(new JLabel("\n   du "+plancher+" au "+plafond+" : "+rs.getString("sect"+i)));
				plancher = plafond;
			}
			content.add(balise);
			

			JXTaskPane stip = new JXTaskPane();
			stip.setTitle("Eléments STIP");
			stip.add(new AbstractAction() {
				{
					putValue(Action.NAME, "Appartient à "+(st.executeQuery("select COUNT(*) from routebalise where balise = '"+name+"'")).getInt(1)+" routes.");
				}
				@Override
				public void actionPerformed(ActionEvent e) {
					AnalyzeUI.showResults("route", name, "");
				}
			});
			
			stip.add(new AbstractAction() {
				{
					putValue(Action.NAME, "Appartient à "+(st.executeQuery("select COUNT(*) from balitis where balitis.balise = '"+name+"'")).getInt(1)+" itis.");
				}
				@Override
				public void actionPerformed(ActionEvent e) {
					AnalyzeUI.showResults("iti", name, "");
				}
			});
			
			stip.add(new AbstractAction() {
				{
					putValue(Action.NAME, "Appartient à "+(st.executeQuery("select COUNT(*) from baltrajets where baltrajets.balise = '"+name+"'")).getInt(1)+" trajets.");
				}
				@Override
				public void actionPerformed(ActionEvent e) {
					AnalyzeUI.showResults("trajet", name, "");
				}
			});
			
			stip.add(new AbstractAction() {
				{
					putValue(Action.NAME, "Appartient à "+(st.executeQuery("select COUNT(*) from balint where bal1 = '"+name+"' or bal2='"+name+"' or balise='"+name+"'")).getInt(1)+" balint.");
				}
				@Override
				public void actionPerformed(ActionEvent e) {
					
				}
			});
			
			stip.add(new AbstractAction() {
				{
					putValue(Action.NAME, "Possède "+(st.executeQuery("select COUNT(*) from consignes where balise='"+name+"'")).getInt(1)+" consignes.");
				}
				@Override
				public void actionPerformed(ActionEvent e) {

				}
			});
			content.add(stip);
			
			JXTaskPane stpv = new JXTaskPane();
			stpv.setTitle("Eléments STPV");
			
			final Statement st2 = DatabaseManager.getCurrentStpv();
			if(st2 == null){
				stpv.add(new JLabel("<html><i>Aucune base STPV configurée.</i></html>"));
			} else {
				stpv.add(new AbstractAction() {
					{
						putValue(Action.NAME, "Possède "+(st2.executeQuery("select COUNT(*) from lieu26 where balise ='"+name+"'")).getInt(1)+" lieu(x) 26.");
					}
					@Override
					public void actionPerformed(ActionEvent e) {
						
					}
				});
				stpv.add(new AbstractAction() {
					{
						putValue(Action.NAME, "Possède "+(st2.executeQuery("select COUNT(*) from lieu27 where balise ='"+name+"'")).getInt(1)+" lieu(x) 27.");
					}
					@Override
					public void actionPerformed(ActionEvent e) {
						
					}
				});
			}
			
			content.add(stpv);

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Affiche les infos de l'iti <code>id</code>
	 * @param id
	 */
	public void showIti(int id){
		content.removeAll();
		try {
			Statement st = DatabaseManager.getCurrentStip();
			final ResultSet rs = st.executeQuery("select * from itis where id ='"+id+"'");
			String name = rs.getString(2)+"->"+rs.getString(3);
			titleAreaPanel.setTitle("Iti : "+name);	
			
			JXTaskPane infos = new JXTaskPane();
			infos.setTitle("Informations générales");
			
			infos.add(new JLabel("<html><b>Entrée</b> : "+rs.getString(2)+"</html>"));
			infos.add(new JLabel("<html><b>Sortie</b> : "+rs.getString(3)+"</html>"));
			infos.add(new JLabel("<html><b>Plancher</b> : "+rs.getString(4)+"</html>"));
			infos.add(new JLabel("<html><b>Plafond</b> : "+rs.getString(5)+"</html>"));
			content.add(infos);
			content.validate(); //corrige un bug d'affichage
			rs.close();
			st.close();
		} catch (SQLException e){
			e.printStackTrace();
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * Affiche les infos du trajet <code>id</code>
	 * @param id
	 */
	public void showTrajet(int id){
		content.removeAll();
		try {
			Statement st = DatabaseManager.getCurrentStip();
			ResultSet rs = st.executeQuery("select * from trajets where id='"+id+"'");
			int ecl_id = rs.getInt(3);
			int rac_id = rs.getInt(5);
			rs = st.executeQuery("select * from trajets where eclatement_id='"+ecl_id+"' and raccordement_id = '"+rac_id+"'");
			String name = rs.getString(2)+"->"+rs.getString(4);
			titleAreaPanel.setTitle("Trajet : "+name);	
						
			int count = 1;
			while(rs.next()){
				JXTaskPane trajet = new JXTaskPane();
				trajet.setTitle("Trajet "+count);
				
				trajet.add(new JLabel("<html><b>Type</b> : "+rs.getString(6)));
				trajet.add(new JLabel("<html><b>Plafond</b> : "+rs.getString(7)));
				trajet.add(new JLabel("<html><b>Condition 1</b> : "+rs.getString(8)+" "+rs.getString(9)));
				
				if(rs.getString(10) != null){
					trajet.add(new JLabel("<html><b>Condition 2</b> : "+rs.getString(10)+" "+rs.getString(11)));
				}
				if(rs.getString(12) != null){
					trajet.add(new JLabel("<html><b>Condition 3</b> : "+rs.getString(12)+" "+rs.getString(13)));
				}
				if(rs.getString(14) != null){
					trajet.add(new JLabel("<html><b>Condition 4</b> : "+rs.getString(14)+" "+rs.getString(15)));
				}
				content.add(trajet);
				count++;
			}
			content.validate();
		} catch (SQLException e){
			e.printStackTrace();
		}
	}
	
	public void showRoute(int id){
		content.removeAll();
		try {
			Statement st = DatabaseManager.getCurrentStip();
			ResultSet rs = st.executeQuery("select * from routes where id='"+id+"'");
			titleAreaPanel.setTitle("Route "+rs.getString(2));
			
			JXTaskPane route = new JXTaskPane();
			route.setTitle("Informations générales");
			
			route.add(new JLabel("<html><b>Espace</b> : "+(rs.getString(3).equals("U")?"UIR":"FIR")+"</html>"));
			
			content.add(route);
		} catch (SQLException e){
			e.printStackTrace();
		}
	}
	
	public void showSecteur(String name){
		content.removeAll();
		try {
			Statement st = DatabaseManager.getCurrentStip();
			ResultSet rs = st.executeQuery("select * from secteurs where nom='"+name+"'");
			titleAreaPanel.setTitle("Secteur "+rs.getString(2));
			
			JXTaskPane secteur = new JXTaskPane();
			secteur.setTitle("Informations générales");
			
			secteur.add(new JLabel("<html><b>Espace</b> : "+(rs.getString(4).equals("U")?"UIR":"FIR")+"</html>"));
			secteur.add(new JLabel("<html><b>Appartient au centre</b> : "+rs.getString(3)+"</html>"));
			secteur.add(new JLabel("<html><b>Plancher</b> : "+rs.getString(6)+"</html>"));
			secteur.add(new JLabel("<html><b>Plafond</b> : "+rs.getString(7)+"</html>"));			
			
			content.add(secteur);
		} catch (SQLException e){
			e.printStackTrace();
		}
	}
	
}
