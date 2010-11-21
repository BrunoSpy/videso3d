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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;

import fr.crnan.videso3d.Context;
import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.DatabaseManager.Type;
import fr.crnan.videso3d.VidesoGLCanvas;
import fr.crnan.videso3d.ihm.components.TitledPanel;
import fr.crnan.videso3d.stip.StipController;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
/**
 * Panel d'infos contextuelles
 * @author Bruno Spyckerelle
 * @version 0.4.0
 */
public class ContextPanel extends JPanel {

	private JXTaskPaneContainer content = new JXTaskPaneContainer();	
	
	private TitledPanel titleAreaPanel = new TitledPanel("Informations");

	private HashMap<DatabaseManager.Type, Context> taskpanes = new HashMap<DatabaseManager.Type, Context>();
	
	private VidesoGLCanvas wwd = null;	
	
	
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
	
	/**
	 * Ajoute un JTaskPane avec ses éléments associés
	 * @param pane
	 * @param base type de base données à laquelle se réfère ces données
	 */
	public void addTaskPane(Context pane, DatabaseManager.Type base){
		this.taskpanes.put(base, pane);
	}

	public void removeTaskPane(DatabaseManager.Type base){
		this.taskpanes.remove(base);
	}
	
	/**
	 * Affiche les infos pertinentes pour l'objet en fonction de son type et de son nom
	 * @param name Nom de l'objet
	 */
	public void showInfo(DatabaseManager.Type base, int type, String name){
		titleAreaPanel.setTitle("Informations sur "+name);
		content.removeAll();
		switch (base) {
		case STIP:
			switch (type) {
			case StipController.ROUTES:
				this.addTaskpanes(Type.STIP, type, name);
				break;
			case StipController.SECTEUR:
				this.addTaskpanes(Type.STIP, type, name);
				this.addTaskpanes(Type.STPV, type, name);
				break;
			case StipController.BALISES:
				this.addTaskpanes(Type.STIP, type, name);
				this.addTaskpanes(Type.STPV, type, name);
				break;
			case StipController.ITI:				
				break;
			case StipController.CONNEXION:
				break;
			case StipController.TRAJET:
				break;
			}
			break;
			
		default:
			break;
		}
		content.validate();
	}
	
	private void addTaskpanes(DatabaseManager.Type base, int type, String name){
		for(JXTaskPane pane : taskpanes.get(base).getTaskPanes(type, name)){
			content.add(pane);
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

	public void setWWD(VidesoGLCanvas wwd2) {
		this.wwd = wwd;
	}

	
}
