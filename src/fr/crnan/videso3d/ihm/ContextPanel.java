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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;
import org.jdom.Element;

import fr.crnan.videso3d.Couple;
import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.VidesoGLCanvas;
import fr.crnan.videso3d.aip.AIP;
import fr.crnan.videso3d.aip.AIP.Altitude;
import fr.crnan.videso3d.aip.AIPController;
import fr.crnan.videso3d.geom.LatLonCautra;
import fr.crnan.videso3d.geom.Latitude;
import fr.crnan.videso3d.geom.Longitude;
import fr.crnan.videso3d.graphics.Balise2D;
import fr.crnan.videso3d.graphics.ObjectAnnotation;
import fr.crnan.videso3d.graphics.Route;
import fr.crnan.videso3d.graphics.Route2D;
import fr.crnan.videso3d.graphics.Secteur3D;
import fr.crnan.videso3d.graphics.Secteur.Type;
import fr.crnan.videso3d.ihm.components.TitledPanel;
import fr.crnan.videso3d.stip.Stip;
import fr.crnan.videso3d.stip.StipController;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.Annotation;
/**
 * Panel d'infos contextuelles
 * @author Bruno Spyckerelle
 * @version 0.3.2
 */
public class ContextPanel extends JPanel implements SelectListener {

	private JXTaskPaneContainer content = new JXTaskPaneContainer();	
	
	private TitledPanel titleAreaPanel = new TitledPanel("Informations");

	private VidesoGLCanvas wwd = null;	
	private StipController stipController;
	private AIPController aipController;
	
	private Annotation lastSegmentAnnotation;
	
	public ContextPanel(){
		super();
		this.setPreferredSize(new Dimension(300, 0));
		this.setLayout(new BorderLayout());

		this.add(titleAreaPanel, BorderLayout.NORTH);

		this.add(content, BorderLayout.CENTER);
	}

	public ContextPanel(VidesoGLCanvas wwd){
		this();
		this.wwd = wwd;
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
				if(((Secteur3D)event.getTopObject()).getType()==Type.Secteur){
					this.showSecteur(((Secteur3D)event.getTopObject()).getName());
				}else{
					this.showAIPZone((Secteur3D)event.getTopObject());
				}
			}
		}
	}
	

	/**
	 * Détermine le type de l'objet envoyé et affiche les infos en conséquence
	 * @param name Nom de l'objet
	 */
	public void showInfo(String name){
		String type = Stip.getTypeFromName(name);
		if(name != null && type != null){
			this.open();
			if(type.equals(Stip.STIP_BALISE)){
				this.showBalise(name);
			} else if(type.equals(Stip.STIP_ROUTE)){
				this.showRoute(name);
			} else if(type.equals(Stip.STIP_SECTEUR)){
				this.showSecteur(name);
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
			
			final boolean pub = rs.getBoolean(3);
			JXTaskPane balise = new JXTaskPane();
			balise.setTitle("Informations générales");
			balise.add(new JLabel("<html><b>Publiée</b> : "+(pub?"oui":"non")+"</html>"));
			balise.add(new JLabel("<html><b>Commentaires</b> : "+ rs.getString("definition")+"</html>"));
			balise.add(new JLabel("<html><b>Cooordonnées</b> :</html>"));
			balise.add(new AbstractAction() {
				{
					putValue(Action.NAME, "  WGS84 : "+latitude+", "+longitude);
					putValue(Action.SHORT_DESCRIPTION, "Centrer le globe sur ces coordonnées.");
				}
				@Override
				public void actionPerformed(ActionEvent e) {
					if(stipController!=null){
						if(pub) {
							stipController.highlight(StipController.BALISES_PUB, name);
						} else {
							stipController.highlight(StipController.BALISES_NP, name);
						}
					}
				}
			});
			balise.add(new AbstractAction() {
				{
					putValue(Action.NAME, "  Cautra : X: "+String.format("%7.2f",coor.getCautra()[0])+" Y: "+String.format("%7.2f",coor.getCautra()[1]));
					putValue(Action.SHORT_DESCRIPTION, "Centrer le globe sur ces coordonnées.");
				}
				@Override
				public void actionPerformed(ActionEvent e) {
					if(stipController!=null){
						if(pub) {
							stipController.highlight(StipController.BALISES_PUB, name);
						} else {
							stipController.highlight(StipController.BALISES_NP, name);
						}
					}
				}
			});
			balise.add(new JLabel("<html><b>Affectée au centre</b> : "+rs.getString("centre")+"</html>"));
			balise.add(new JLabel("<html><b>Affectée aux secteurs</b> :</html>"));
		
			int plafond = -1;
			String secteur = null;
			for(int i = 9; i>= 1; i--){
				int plancher = rs.getInt("limit"+i);
				if(plancher != -1){
					if(secteur!=null){
						final int tPlafond = plafond;
						final int tPlancher = plancher;
						final String tSecteur = secteur;
						balise.add(new AbstractAction() {
							{
								putValue(Action.NAME, "\nDu "+tPlafond+" au "+tPlancher+" : "+tSecteur);
							}
							@Override
							public void actionPerformed(ActionEvent e) {
								stipController.highlight(StipController.SECTEUR, tSecteur);
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
				balise.add(new AbstractAction() {
					{
						putValue(Action.NAME, "\nDu "+tPlafond+" au 0 : "+tSecteur);
					}
					@Override
					public void actionPerformed(ActionEvent e) {
						stipController.highlight(StipController.SECTEUR, tSecteur);
					}
				});
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
					if(wwd!=null){
						AnalyzeUI.setWWD(wwd);
					}
				}
			});
			
			stip.add(new AbstractAction() {
				{
					putValue(Action.NAME, "Appartient à "+(st.executeQuery("select COUNT(*) from balitis where balitis.balise = '"+name+"'")).getInt(1)+" itis.");
				}
				@Override
				public void actionPerformed(ActionEvent e) {
					AnalyzeUI.showResults("iti", name, "");
					if(wwd!=null){
						AnalyzeUI.setWWD(wwd);
					}
				}
			});
			
			stip.add(new AbstractAction() {
				{
					putValue(Action.NAME, "Appartient à "+(st.executeQuery("select COUNT(*) from baltrajets where baltrajets.balise = '"+name+"'")).getInt(1)+" trajets.");
				}
				@Override
				public void actionPerformed(ActionEvent e) {
					AnalyzeUI.showResults("trajet", name, "");
					if(wwd!=null){
						AnalyzeUI.setWWD(wwd);
					}
				}
			});
			
			stip.add(new AbstractAction() {
				{
					putValue(Action.NAME, "Appartient à "+(st.executeQuery("select COUNT(*) from (select distinct balconnexions.idconn from connexions, balconnexions where connexions.id = balconnexions.idconn and (balconnexions.balise = '"+name+"' or connexions.connexion = '"+name+"'))")).getInt(1)
							+" connexions.");
				}
				@Override
				public void actionPerformed(ActionEvent e) {
					AnalyzeUI.showResults("connexion", name, "");
					if(wwd!=null){
						AnalyzeUI.setWWD(wwd);
					}
				}
			});
			
			stip.add(new AbstractAction() {
				{
					putValue(Action.NAME, "Appartient à "+(st.executeQuery("select COUNT(*) from balint where bal1 = '"+name+"' or bal2='"+name+"' or balise='"+name+"'")).getInt(1)+" balint.");
				}
				@Override
				public void actionPerformed(ActionEvent e) {
					AnalyzeUI.showResults("balise", name, "");
					if(wwd!=null){
						AnalyzeUI.setWWD(wwd);
					}
				}
			});
			
			stip.add(new AbstractAction() {
				{
					putValue(Action.NAME, "Possède "+(st.executeQuery("select COUNT(*) from consignes where balise='"+name+"' or oaci='"+name+"'" )).getInt(1)+" consignes.");
				}
				@Override
				public void actionPerformed(ActionEvent e) {
					AnalyzeUI.showResults("balise", name, "");
					if(wwd!=null){
						AnalyzeUI.setWWD(wwd);
					}
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
						AnalyzeUI.showResults("balise", name, "");
						if(wwd!=null){
							AnalyzeUI.setWWD(wwd);
						}
					}
				});
				stpv.add(new AbstractAction() {
					{
						putValue(Action.NAME, "Possède "+(st2.executeQuery("select COUNT(*) from lieu27 where balise ='"+name+"'")).getInt(1)+" lieu(x) 27.");
					}
					@Override
					public void actionPerformed(ActionEvent e) {
						AnalyzeUI.showResults("balise", name, "");
						if(wwd!=null){
							AnalyzeUI.setWWD(wwd);
						}
					}
				});
				stpv.add(new AbstractAction() {
					{
						putValue(Action.NAME, "Possède "+(st2.executeQuery("select COUNT(*) from lieu8 where depart ='"+name+"' or arrivee = '"+name+"'")).getInt(1)+" lieu(x) 8.");
					}
					@Override
					public void actionPerformed(ActionEvent e) {
						AnalyzeUI.showResults("balise", name, "");
						if(wwd!=null){
							AnalyzeUI.setWWD(wwd);
						}
					}
				});
				stpv.add(new AbstractAction() {
					{
						putValue(Action.NAME, "Possède "+(st2.executeQuery("select COUNT(*) from lieu91 where bal1 ='"+name+"' or bal2='"+name+"'")).getInt(1)+" lieu(x) 91.");
					}
					@Override
					public void actionPerformed(ActionEvent e) {
						AnalyzeUI.showResults("balise", name, "");
						if(wwd!=null){
							AnalyzeUI.setWWD(wwd);
						}
					}
				});
				stpv.add(new AbstractAction() {
					{
						putValue(Action.NAME, "Possède "+(st2.executeQuery("select COUNT(*) from lieu6 where oaci ='"+name+"' or bal1='"+name+"'")).getInt(1)+" lieu(x) 6.");
					}
					@Override
					public void actionPerformed(ActionEvent e) {
						AnalyzeUI.showResults("balise", name, "");
						if(wwd!=null){
							AnalyzeUI.setWWD(wwd);
						}
					}
				});
			}
			
			content.add(stpv);
			content.validate();

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
	 * Affiche les infos de la connexion
	 * @param id
	 */
	public void showConnexion(int id){
		content.removeAll();
		try {
			Statement st = DatabaseManager.getCurrentStip();
			final ResultSet rs = st.executeQuery("select * from connexions where id ='"+id+"'");
			String name = rs.getString(2);
			titleAreaPanel.setTitle("Connexion : "+name);	
			
			JXTaskPane infos = new JXTaskPane();
			infos.setTitle("Informations générales");
			
			infos.add(new JLabel("<html><b>Type</b> : "+rs.getString(4)+"</html>"));
			infos.add(new JLabel("<html><b>Plafond</b> : "+rs.getString(7)+"</html>"));
			infos.add(new JLabel("<html><b>Plancher</b> : "+rs.getString(6)+"</html>"));
			infos.add(new JLabel("<html><b>Balise de connexion</b> : "+rs.getString(3)+"</html>"));
			if(rs.getString(9).compareTo("0") != 0){
				infos.add(new JLabel("<html><b>Vitesse</b> : "+rs.getString(8)+rs.getString(9)+"</html>"));
			}
			content.add(infos);
			rs.close();
			st.close();
		} catch (SQLException e){
			e.printStackTrace();
		} catch (Exception e){
			e.printStackTrace();
		}
		content.validate();
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
			final String name = rs.getString(2);
			titleAreaPanel.setTitle("Route "+name);
			
			JXTaskPane route = new JXTaskPane();
			route.setTitle("Informations générales");
			
			route.add(new JLabel("<html><b>Espace</b> : "+(rs.getString(3).equals("U")?"UIR":"FIR")+"</html>"));
			
			content.add(route);
			
			JXTaskPane vue = new JXTaskPane();
			vue.setTitle("Vue 3D");
			vue.add(new AbstractAction() {
				{
					putValue(Action.NAME, "<html>Afficher/centrer la route.</html>");
				}
				@Override
				public void actionPerformed(ActionEvent arg0) {
					stipController.highlight(StipController.ROUTES, name);
				}
			});
			vue.add(new AbstractAction() {
				boolean show = true;
				{
					putValue(Action.NAME, "<html>Afficher les balises.</html>");
				}
				@Override
				public void actionPerformed(ActionEvent arg0) {
					if(show) {
						stipController.showRoutesBalises(name);
						putValue(Action.NAME, "<html>Cacher les balises.</html>");
						show = false;
					} else {
						stipController.hideRoutesBalises(name);
						putValue(Action.NAME, "<html>Afficher les balises.</html>");
						show = true;
					}
				}
			});
			content.add(vue);
			
			content.validate();
		} catch (SQLException e){
			e.printStackTrace();
		}
	}
	
	public void showRoute(String name) {
		try {
			Statement st = DatabaseManager.getCurrentStip();
			this.showRoute(st.executeQuery("select id from routes where name ='"+name+"'").getInt(1));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	public void showSecteur(final String name){
		content.removeAll();
		try {
			final Statement st = DatabaseManager.getCurrentStip();
			ResultSet rs = st.executeQuery("select * from secteurs where nom='"+name+"'");
			titleAreaPanel.setTitle("Secteur "+rs.getString(2));
			
			JXTaskPane secteur = new JXTaskPane();
			secteur.setTitle("Informations générales");
			
			secteur.add(new JLabel("<html><b>Espace</b> : "+(rs.getString(4).equals("U")?"UIR":"FIR")+"</html>"));
			secteur.add(new JLabel("<html><b>Appartient au centre</b> : "+rs.getString(3)+"</html>"));
			secteur.add(new JLabel("<html><b>Plafond</b> : "+rs.getString(7)+"</html>"));
			secteur.add(new JLabel("<html><b>Plancher</b> : "+rs.getString(6)+"</html>"));
			secteur.add(new JLabel("<html><b>Mode S</b> : "+(rs.getBoolean(8)?"Oui":"Non")));
			
			content.add(secteur);

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
			
			JXTaskPane stip = new JXTaskPane();
			stip.setTitle("Eléments Stip");
			
			stip.add(new AbstractAction() {
				boolean show = true;
				{
					putValue(Action.NAME, "Afficher les "+balises.size()+" balises.");
				}
				@Override
				public void actionPerformed(ActionEvent e) {
					if(show){
						for(String name : balises){
							stipController.showObject(StipController.BALISES_NP, name);
							stipController.showObject(StipController.BALISES_PUB, name);
						}
						putValue(Action.NAME, "Cacher les "+balises.size()+" balises.");
						show = false;
					} else {
						for(String name : balises){
							stipController.hideObject(StipController.BALISES_NP, name);
							stipController.hideObject(StipController.BALISES_PUB, name);
						}
						putValue(Action.NAME, "Afficher les "+balises.size()+" balises.");
						show = true;
					}
				}
			});
			
			content.add(stip);
			
			JXTaskPane stpv = new JXTaskPane();
			stpv.setTitle("Eléments Stpv");
			
			if(DatabaseManager.getCurrentStpv() == null){
				stpv.add(new JLabel("<html><i>Aucune base STPV configurée.</i></html>"));
			} else {
				Statement st2 = DatabaseManager.getCurrentStpv();
				ResultSet rs2 = st2.executeQuery("select freq from sect where nom = '"+name+"'");
				if(rs2.next()){
					stpv.add(new JLabel("<html><b>Fréquence : </b>"+rs2.getString(1)+"</html>"));
				}
				st2.close();
			}
			
			content.add(stpv);
//			
//			JXTaskPane ods = new JXTaskPane();
//			ods.setTitle("Eléments ODS");
//			
//			content.add(ods);
			
			content.validate();
		} catch (SQLException e){
			e.printStackTrace();
		}
	}
	
	public void showAIPZone(Secteur3D zone) {
		String zoneID = AIP.getID(AIP.string2type(zone.getType().toString()), zone.getName());
		content.removeAll();
		titleAreaPanel.setTitle(zone.getName());
		
		JXTaskPane infos = new JXTaskPane();
		infos.setTitle("Informations diverses");
		String classe = aipController.getAIP().getZoneAttributeValue(zoneID, "Classe");
		String hor = aipController.getAIP().getZoneAttributeValue(zoneID, "HorTxt");
		String act = aipController.getAIP().getZoneAttributeValue(zoneID, "Activite");
		String rmq = aipController.getAIP().getZoneAttributeValue(zoneID, "Remarque");
		
		if(classe != null){
			infos.add(new JLabel("<html><b>Classe</b> : " + classe+"</html>"));
		}
		if(zone.getType()==Secteur3D.Type.R){
			if(aipController.getAIP().getZoneAttributeValue(zoneID, "Rtba")!=null)
				infos.add(new JLabel("<html><b>RTBA</b></html>"));
		}
		if(hor != null){
			infos.add(new JLabel("<html><b>Horaires</b> : " + hor.replaceAll("#", "<br/>")+"</html>"));
		}
		if(act != null){
			infos.add(new JLabel("<html><b>Activité</b> : " + act.replaceAll("#", "<br/>")+"</html>"));
		}
		if(rmq != null){
			infos.add(new JLabel("<html><b>Remarques</b> : " + rmq.replaceAll("#", "<br/>")+"</html>"));
		}
		
		content.add(infos);
	}
	
	public void showAIPRoute(Route segment) {
		AIP aip = aipController.getAIP();
		String route = segment.getName().split("-")[0].trim();
		String[] splittedSegmentName = segment.getName().split("-");
		String sequence = splittedSegmentName[splittedSegmentName.length-1].trim();
		fr.crnan.videso3d.graphics.Route.Type type = segment.getType();
		String pkRoute = null;
		String typeRoute = aip.RouteType2AIPType(route, type);
		StringBuilder ACCTraverses = new StringBuilder();
		try {
			pkRoute = aipController.getRouteIDFromSegmentName(route, typeRoute);
			PreparedStatement st = DatabaseManager.prepareStatement(DatabaseManager.Type.AIP, "select nomACC from ACCTraverses where routes_pk = ?");
			st.setString(1, pkRoute);
			ResultSet rs = st.executeQuery();
			while(rs.next()){
				ACCTraverses.append(rs.getString(1)+" ");
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		content.removeAll();
		titleAreaPanel.setTitle("Route "+route+" - Segment "+sequence);
		List<Element> segmentsXML = aip.findElementsByChildId(aip.getDocumentRoot().getChild("SegmentS"), "Route", pkRoute);
		Element monSegmentXML = null;
		for(Element segmentXML : segmentsXML){
			if(segmentXML.getChildText("Sequence").equals(sequence)){
				monSegmentXML = segmentXML;
			}
		}		
		
		Element maRoute = aip.findElement(aip.getDocumentRoot().getChild("RouteS"), pkRoute);
		
		JXTaskPane infosRoute = new JXTaskPane();
		infosRoute.setTitle("Informations sur la route");
		String CRType = maRoute.getChildText("TypeCompteRendu");
		String rmqRoute = maRoute.getChildText("Remarque");
		infosRoute.add(new JLabel("<html><b>Type de la route</b> : " + typeRoute+"</html>"));
		if(CRType != null){
			infosRoute.add(new JLabel("<html><b>Compte-rendu</b> : " + CRType+"</html>"));
		}
		infosRoute.add(new JLabel("<html><b>ACC traversés</b> : " + ACCTraverses+"</html>"));
		if(rmqRoute != null){
			infosRoute.add(new JLabel("<html><b>Remarque</b> : " + rmqRoute+"</html>"));
		}
		final String pkRouteFinal = pkRoute;
		infosRoute.add(new AbstractAction("Afficher les balises"){
			boolean afficher = true;
			@Override
			public void actionPerformed(ActionEvent e) {
				if(afficher){
					displayNavFix(pkRouteFinal, true);
					putValue(Action.NAME, "Cacher les balises");
					afficher = false;
				}else{
					displayNavFix(pkRouteFinal, false);
					putValue(Action.NAME, "Afficher les balises");
					afficher = true;
				}
				
			}
		});
		
		JXTaskPane infosSegment = new JXTaskPane();
		infosSegment.setTitle("Informations sur le segment");
		String CR = aip.getChildText(monSegmentXML, "CompteRendu");
		String circul = aip.getChildText(monSegmentXML, "Circulation");
		String rnp = aip.getChildText(monSegmentXML, "CodeRnp");
		String dist = aip.getChildText(monSegmentXML, "Distance");
		String routeM = aip.getChildText(monSegmentXML, "RouteMag");
		String ACC = aip.getChildText(monSegmentXML, "Acc");
		String rmq = aip.getChildText(monSegmentXML, "Remarque");
		Couple<Altitude, Altitude> levels = aip.getLevels(monSegmentXML);
		
		if(CR != null){
			infosSegment.add(new JLabel("<html><b>Compte-rendu</b> : " + CR+"</html>"));
		}
		if(circul != null){
			infosSegment.add(new JLabel("<html><b>Circulation</b> : " + circul+"</html>"));
		}
		if(rnp != null){
			infosSegment.add(new JLabel("<html><b>Code Rnp</b> : " + rnp+"</html>"));
		}
		infosSegment.add(new JLabel("<html><b>Plafond</b> : " + levels.getSecond().getFullText()+"</html>"));
		infosSegment.add(new JLabel("<html><b>Plancher</b> : " + levels.getFirst().getFullText()+"</html>"));
		if(dist != null){
			infosSegment.add(new JLabel("<html><b>Longueur du segment</b> : " + dist+"nm</html>"));
		}
		if(routeM != null){
			infosSegment.add(new JLabel("<html><b>Route magnétique</b> : " + routeM+"</html>"));
		}
		if(ACC != null){
			infosSegment.add(new JLabel("<html><b>ACC</b> : " + ACC+"</html>"));
		}
		if(rmq != null){
			infosSegment.add(new JLabel("<html><b>Remarque</b> : " + rmq+"</html>"));
		}
		boolean route3D = true;
		if(segment instanceof Route2D){
			route3D = false;
		}
		final Route segmentPrecedent = aipController.getPrevious(route, sequence, typeRoute, route3D); 
		final Route segmentSuivant = aipController.getNext(route, sequence, typeRoute, route3D);
		
		if(segmentPrecedent != null){
			AbstractAction previous = new AbstractAction("<html><font color=\"blue\">&lt;&lt; Segment précédent</font></html>"){
				@Override
				public void actionPerformed(ActionEvent arg0) {
					displayAnnotationAndGoTo(segmentPrecedent);
					showAIPRoute(segmentPrecedent);
				}

			};
			infosSegment.add(previous);
		}
		if(segmentSuivant != null){
			AbstractAction next = new AbstractAction("<html><font color=\"blue\">Segment suivant &gt;&gt;</font></html>"){
				@Override
				public void actionPerformed(ActionEvent arg0) {
					displayAnnotationAndGoTo(segmentSuivant);
					showAIPRoute(segmentSuivant);
				}

			};
			infosSegment.add(next);
		}
		content.add(infosRoute);
		content.add(infosSegment);
	}
	
	private void displayAnnotationAndGoTo(Route segment){
		Position annotationPosition = new Position(segment.getLocations().iterator().next(), 0);
		Annotation annotation = ((ObjectAnnotation)segment).getAnnotation(annotationPosition);
		if(lastSegmentAnnotation != null)
			lastSegmentAnnotation.getAttributes().setVisible(false);
		lastSegmentAnnotation = annotation;
		annotation.getAttributes().setVisible(true);
		wwd.getAnnotationLayer().addAnnotation(annotation);
		wwd.getView().goTo(annotationPosition, wwd.getView().getEyePosition().elevation);
		wwd.redraw();
	}
	
	private void displayNavFix(String pkRoute, boolean display){
		LinkedList<String> navFixExtremites = new LinkedList<String>();
		try {
			PreparedStatement st = DatabaseManager.prepareStatement(DatabaseManager.Type.AIP, "select nom from NavFix, segments where segments.pkRoute = ? AND segments.navFixExtremite = NavFix.pk");
			st.setString(1, pkRoute);
			ResultSet rs = st.executeQuery();
			while(rs.next()){
				navFixExtremites.add(rs.getString(1));
			}
			st = DatabaseManager.prepareStatement(DatabaseManager.Type.AIP, "select NavFix.nom from NavFix, routes where routes.pk = ? AND routes.navFixExtremite = NavFix.pk");
			st.setString(1, pkRoute);
			rs = st.executeQuery();
			if(rs.next()){
				navFixExtremites.add(rs.getString(1));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if(display){
			for(String navFix : navFixExtremites){
				aipController.showObject(AIP.DMEATT, navFix);
			}
		}else{
			for(String navFix : navFixExtremites){
				aipController.hideObject(AIP.DMEATT, navFix);
			}	
		}
	}

	
	
	
	

	public void setWWD(VidesoGLCanvas wwd) {
		this.wwd = wwd;
	}
	
	//TODO faire autrement
	public void setStipController(StipController controller){
		this.stipController = controller;
	}
	
	//TODO faire autrement
	public void setAIPController(AIPController aip){
		this.aipController = aip;
	}

	
}
