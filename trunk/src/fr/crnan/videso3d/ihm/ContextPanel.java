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
import java.awt.Color;
import java.awt.Dimension;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

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
 * @version 0.2
 */
public class ContextPanel extends JPanel implements SelectListener {

	private JPanel content = new JPanel();

	private TitledPanel titleAreaPanel = new TitledPanel("Informations");

	public ContextPanel(){
		super();
		this.setPreferredSize(new Dimension(300, 0));
		this.setLayout(new BorderLayout());

		this.add(titleAreaPanel, BorderLayout.NORTH);
		content.setLayout(new BoxLayout(content, BoxLayout.PAGE_AXIS));

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
	public void showBalise(String name){		
		titleAreaPanel.setTitle("Balise : "+name);
		content.removeAll();
		try {
			Statement st = DatabaseManager.getCurrentStip();
			ResultSet rs = st.executeQuery("select * from balises where name='"+name+"'");
			LatLonCautra coor = LatLonCautra.fromDegrees(rs.getDouble("latitude"), rs.getDouble("longitude"));
			Latitude lat = new Latitude(coor.getLatitude().degrees);
			Longitude lon = new Longitude(coor.getLongitude().degrees);
			String latitude = lat.getDegres()+"°"+lat.getMinutes()+"\'"+lat.getSecondes()+"\" N";
			String longitude = Math.abs(lon.getDegres())+"°"+lon.getMinutes()+"\'"+lon.getSecondes()+"\""+ (lon.getDegres() < 0 ? "E" : "O");
			String t = "\nCommentaire : " + rs.getString("definition")+"\n\n"+
			"Coordonnées :\n" +
			"   WGS84 : " + latitude + ", "+longitude+"\n"+
			"   Cautra : "+String.format("%7.2f",coor.getCautra()[0])+", "+String.format("%7.2f",coor.getCautra()[1])+"\n" +
			"\n" +
			"Affectée au centre : "+rs.getString("centre")+"\n\n" +
			"Affectée aux secteurs :";
			int plancher = 0;
			for(int i = 1; i<= 9; i++){
				int plafond = rs.getInt("limit"+i);
				if(plafond != -1) t += "\n   du "+plancher+" au "+plafond+" : "+rs.getString("sect"+i);
				plancher = plafond;
			}
			JTextArea text = new JTextArea();
			text.setText(t+"\n");
			text.setEditable(false);
			text.setOpaque(true);
			text.setBackground(/*UIManager.getColor("background")*/new Color(214,217,223));
			text.setBorder(null);

			content.add(text);

			content.add(new TitledPanel("Eléments Stip"));
			rs = st.executeQuery("select COUNT(*) from routebalise where balise = '"+name+"'");
			String stipText = "\n Appartient à "+rs.getInt(1)+" routes.\n";
			rs = st.executeQuery("select COUNT(*) from balitis where balitis.balise = '"+name+"'");
			stipText += "\n Appartient à "+rs.getInt(1)+" itinéraires.\n";
			rs = st.executeQuery("select COUNT(*) from baltrajets where baltrajets.balise = '"+name+"'");
			stipText += "\n Appartient à "+rs.getInt(1)+" trajets.\n";
			rs = st.executeQuery("select COUNT(*) from balint where bal1 = '"+name+"' or bal2='"+name+"' or balise='"+name+"'");
			stipText += "\n Appartient à "+rs.getInt(1)+" balint.\n"; 
			rs = st.executeQuery("select COUNT(*) from consignes where balise='"+name+"'");
			stipText += "\n Possède "+rs.getInt(1)+" consignes.\n";			
			JTextArea stip = new JTextArea();
			stip.setText(stipText);
			stip.setBorder(null);
			stip.setEditable(false);
			stip.setOpaque(true);
			stip.setBackground(/*UIManager.getColor("background")*/new Color(214,217,223));
			content.add(stip);
			rs.close();
			st.close();

			st = DatabaseManager.getCurrentStpv();
			String stpvText ="";
			content.add(new TitledPanel("Eléments Stpv"));
			if(st != null){
				rs = st.executeQuery("select COUNT(*) from lieu26 where balise ='"+name+"'");
				stpvText = "\n Possède "+rs.getInt(1)+" lieu(x) 26.\n";
				rs = st.executeQuery("select COUNT(*) from lieu27 where balise ='"+name+"'");
				stpvText += "\n Possède "+rs.getInt(1)+" lieu(x) 27.\n";
				rs.close();
				st.close();
			}else {
				stpvText = "\n Aucune base STPV configurée.";
			}
			JTextArea stpv = new JTextArea();
			stpv.setBorder(null);
			stpv.setText(stpvText);
			stpv.setEditable(false);
			stpv.setOpaque(true);
			stpv.setBackground(/*UIManager.getColor("background")*/new Color(214,217,223));

			content.add(stpv);

			content.add(Box.createVerticalStrut(1000));

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
			ResultSet rs = st.executeQuery("select * from itis where id ='"+id+"'");
			String name = rs.getString(2)+"->"+rs.getString(3);
			titleAreaPanel.setTitle("Iti : "+name);	
			
			String itiText = "\n Entrée : "+rs.getString(2);
			itiText += "\n Sortie : "+rs.getString(3);
			itiText += "\n\n Plancher : "+rs.getInt(4);
			itiText += "\n Plafond : "+rs.getInt(5);
			
			JTextArea iti =  new JTextArea();
			iti.setBorder(null);
			iti.setText(itiText);
			iti.setEditable(false);
			iti.setOpaque(true);
			iti.setBackground(/*UIManager.getColor("background")*/new Color(214,217,223));
			
			content.add(iti);
			
			content.add(Box.createVerticalStrut(1000));
		} catch (SQLException e){
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
			
			String trajetText = "";
			int count = 1;
			while(rs.next()){
				trajetText += "\n Trajet "+count+" : ";
				trajetText += "\n     Type : "+rs.getString(6);
				trajetText += "\n     Plafond : "+rs.getInt(7);
				trajetText += "\n     Condition 1 : "+rs.getString(8)+" "+rs.getString(9);
				if(rs.getString(10) != null){
					trajetText += "\n     Condition 2 : "+rs.getString(10)+" "+rs.getString(11);
				}
				if(rs.getString(12) != null){
					trajetText += "\n     Condition 3 : "+rs.getString(12)+" "+rs.getString(15);
				}
				if(rs.getString(14) != null){
					trajetText += "\n     Condition 4 : "+rs.getString(14)+" "+rs.getString(15);
				}
				trajetText += "\n";
				count++;
			}
			
			JTextArea trajet =  new JTextArea();
			trajet.setBorder(null);
			trajet.setText(trajetText);
			trajet.setEditable(false);
			trajet.setOpaque(true);
			trajet.setBackground(/*UIManager.getColor("background")*/new Color(214,217,223));
			
			content.add(trajet);
			
			content.add(Box.createVerticalStrut(1000));
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
			
			JTextArea trajet =  new JTextArea();
			trajet.setBorder(null);
			trajet.setText("\n Espace : "+(rs.getString(3).equals("U")?"UIR":"FIR"));
			trajet.setEditable(false);
			trajet.setOpaque(true);
			trajet.setBackground(/*UIManager.getColor("background")*/new Color(214,217,223));
			
			content.add(Box.createVerticalStrut(1000));
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
			
			JTextArea secteur =  new JTextArea();
			secteur.setBorder(null);
			secteur.setText("\n Espace : "+(rs.getString(3).equals("U")?"UIR":"FIR")
					+"\n Appartient au centre : "+rs.getString(3)
					+"\n Plancher : "+rs.getInt(6)
					+"\n Plafond : "+rs.getInt(7));
			secteur.setEditable(false);
			secteur.setOpaque(true);
			secteur.setBackground(/*UIManager.getColor("background")*/new Color(214,217,223));
			content.add(secteur);
			content.add(Box.createVerticalStrut(1000));
		} catch (SQLException e){
			e.printStackTrace();
		}
	}
	
}
