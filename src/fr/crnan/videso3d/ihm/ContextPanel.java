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

import com.mxgraph.model.mxCell;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.view.mxGraph;

import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.geom.LatLonCautra;
import fr.crnan.videso3d.geom.Latitude;
import fr.crnan.videso3d.geom.Longitude;
import fr.crnan.videso3d.graphics.Balise2D;
import fr.crnan.videso3d.graphs.CellContent;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
/**
 * Panel d'infos contextuelles
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class ContextPanel extends JPanel implements mxIEventListener, SelectListener {

	private mxGraph graph;
	
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
	
	public ContextPanel(mxGraph graph){
		this();
		this.graph = graph;

	}
	
	public void setGraph(mxGraph graph){
		this.graph = graph;
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
	public void invoke(Object sender, mxEventObject evt) {
		mxCell cell = (mxCell) graph.getSelectionCell();
		if(cell.getValue() instanceof CellContent){
			CellContent content = (CellContent) cell.getValue();
			if(content.getType().equals(CellContent.TYPE_BALISE)){
				this.showBalise(((CellContent)cell.getValue()).getName());
			}
		}
	}

	@Override
	public void selected(SelectEvent event) {
		if(event.getEventAction() == SelectEvent.LEFT_DOUBLE_CLICK){
			this.open();
			if(event.getTopObject() instanceof Balise2D){
				this.showBalise(((Balise2D)event.getTopObject()).getName());
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
			
			content.add(new TitledPanel("Eléments Stpv"));
			st = DatabaseManager.getCurrentStpv();
			rs = st.executeQuery("select COUNT(*) from lieu26 where balise ='"+name+"'");
			String stpvText = "\n Possède "+rs.getInt(1)+" lieu(x) 26.\n";
			rs = st.executeQuery("select COUNT(*) from lieu27 where balise ='"+name+"'");
			stpvText += "\n Possède "+rs.getInt(1)+" lieu(x) 27.\n";
			
			JTextArea stpv = new JTextArea();
			stpv.setBorder(null);
			stpv.setText(stpvText);
			stpv.setEditable(false);
			stpv.setOpaque(true);
			stpv.setBackground(/*UIManager.getColor("background")*/new Color(214,217,223));
			
			content.add(stpv);
			content.add(Box.createVerticalStrut(1000));
			
			rs.close();
			st.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
}
