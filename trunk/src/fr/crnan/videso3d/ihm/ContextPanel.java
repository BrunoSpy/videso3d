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
import java.util.HashMap;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;

import fr.crnan.videso3d.Context;
import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.DatabaseManager.Type;
import fr.crnan.videso3d.graphics.VidesoObject;
import fr.crnan.videso3d.ihm.components.TitledPanel;
import fr.crnan.videso3d.stip.StipController;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
/**
 * Panel d'infos contextuelles
 * @author Bruno Spyckerelle
 * @version 0.4.1
 */
public class ContextPanel extends JPanel implements SelectListener {

	private JXTaskPaneContainer content = new JXTaskPaneContainer();	
	
	private TitledPanel titleAreaPanel = new TitledPanel("Informations");

	private HashMap<DatabaseManager.Type, Context> taskpanes = new HashMap<DatabaseManager.Type, Context>();
		
	public ContextPanel(){
		super();
		this.setPreferredSize(new Dimension(300, 0));
		this.setLayout(new BorderLayout());

		this.add(titleAreaPanel, BorderLayout.NORTH);

		this.add(new JScrollPane(content), BorderLayout.CENTER);
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
			if(event.getTopObject() instanceof VidesoObject){
				VidesoObject o = (VidesoObject) event.getTopObject();
				this.showInfo(o.getDatabaseType(), o.getType(), o.getName());
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
		content.removeAll();
		if(base != null) {
			titleAreaPanel.setTitle("Informations sur "+name);
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
					this.addTaskpanes(Type.STIP, type, name);
					break;
				case StipController.CONNEXION:
					this.addTaskpanes(Type.STIP, type, name);
					break;
				case StipController.TRAJET:
					this.addTaskpanes(Type.STIP, type, name);
					break;
				}
				break;
			case AIP:
				this.addTaskpanes(Type.AIP, type, name);
				break;
			default:
				break;
			}
			content.validate();
		}
	}
	/**
	 * Ajoute les {@link JXTaskPane} demandés.<br />
	 * Prends en compte l'existence de la base demandée.
	 * @param base
	 * @param type
	 * @param name
	 */
	public void setTitle(String title) {
		this.titleAreaPanel.setTitle(title);
	}
	
	private void addTaskpanes(DatabaseManager.Type base, int type, String name){
		if(taskpanes.get(base) != null) {
			for(JXTaskPane pane : taskpanes.get(base).getTaskPanes(type, name)){
				content.add(pane, null);
			}
		}
	}
	
	
}
