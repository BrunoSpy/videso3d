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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.mode.ExtendedMode;

import fr.crnan.videso3d.Context;
import fr.crnan.videso3d.DatasManager;
import fr.crnan.videso3d.DatasManager.Type;
import fr.crnan.videso3d.databases.aip.AIP;
import fr.crnan.videso3d.databases.stip.StipController;
import fr.crnan.videso3d.databases.stpv.StpvController;
import fr.crnan.videso3d.ihm.components.TitledPanel;
/**
 * Panel d'infos contextuelles
 * @author Bruno Spyckerelle
 * @version 0.4.6
 */
public class ContextPanel extends JPanel{

	private JXTaskPaneContainer content = new JXTaskPaneContainer();	
	
	private TitledPanel titleAreaPanel = new TitledPanel("Informations");

	private HashMap<DatasManager.Type, Context> taskpanes = new HashMap<DatasManager.Type, Context>();
		
	private DefaultSingleCDockable dockable;
	
	/**
	 * True if <code>content</code> contains at least one taskpane
	 */
	private boolean hasTaskpanes = false;
	
	public ContextPanel(){
		super();
		this.setPreferredSize(new Dimension(250, 0));
		this.setLayout(new BorderLayout());

		this.add(titleAreaPanel, BorderLayout.NORTH);

		JScrollPane scrollPane = new JScrollPane(content);
		scrollPane.setBorder(null);
		this.add(scrollPane, BorderLayout.CENTER);
	}
	
	public void setDockable(DefaultSingleCDockable dockable){
		this.dockable = dockable;
		this.remove(titleAreaPanel);
	}
	
	/**
	 * Ouvre le panneau
	 */
	public void open(){
		//open the context panel only if it contains taskpanes
		if(hasTaskpanes){
			if(this.getParent() instanceof JSplitPane) {
				if(((JSplitPane)this.getParent()).getLeftComponent().equals(this)){
					((JSplitPane)this.getParent()).setDividerLocation(250);
				} 
			} else if(this.dockable != null && this.dockable.getExtendedMode() == ExtendedMode.MINIMIZED){
				this.dockable.setExtendedMode(ExtendedMode.NORMALIZED);
			}
		}
	}
	
	/**
	 * Ajoute un JTaskPane avec ses éléments associés
	 * @param pane
	 * @param base type de base données à laquelle se réfère ces données
	 */
	public void addTaskPane(Context pane, DatasManager.Type base){
			this.taskpanes.put(base, pane);
	}

	public void removeTaskPane(DatasManager.Type base){
		this.taskpanes.remove(base);
	}
	
	/**
	 * Affiche les infos pertinentes pour l'objet en fonction de son type et de son nom
	 * @param name Nom de l'objet
	 */
	public void showInfo(DatasManager.Type base, int type, String name){
		content.removeAll();
		hasTaskpanes = false;
		if(base != null) {
			if(dockable != null){
				dockable.setTitleText("Informations sur "+name);
			} else {
				titleAreaPanel.setTitle("Informations sur "+name);
			}
			switch (base) {
			case STIP:
				switch (type) {
				case StipController.ROUTES:
					this.addTaskpanes(DatasManager.Type.STIP, type, name);
					this.addTaskpanes(DatasManager.Type.AIP, AIP.AWY, name);
					break;
				case StipController.SECTEUR:
					this.addTaskpanes(DatasManager.Type.STIP, type, name);
					this.addTaskpanes(DatasManager.Type.STPV, StpvController.SECTEUR, name);
					break;
				case StipController.BALISES:
					this.addTaskpanes(DatasManager.Type.STIP, type, name);
					this.addTaskpanes(DatasManager.Type.STPV, StpvController.BALISE, name);
					break;
				case StipController.ITI:
					this.addTaskpanes(DatasManager.Type.STIP, type, name);
					break;
				case StipController.CONNEXION:
					this.addTaskpanes(DatasManager.Type.STIP, type, name);
					break;
				case StipController.TRAJET:
					this.addTaskpanes(DatasManager.Type.STIP, type, name);
					break;
				}
				break;
			case AIP:
				if(type == AIP.WPT){
					this.addTaskpanes(DatasManager.Type.STIP, StipController.BALISES, name);
					this.addTaskpanes(DatasManager.Type.STPV, StpvController.BALISE, name);
					this.addTaskpanes(base, type, name);
				} else {
					this.addTaskpanes(base, type, name);
				}
				break;
			default:
				this.addTaskpanes(base, type, name);
			}
			content.validate();
			open();
		}else{
			this.setTitle("Informations");
		}
	}
	
	
	public void setTitle(String title) {
		if(dockable != null){
			dockable.setTitleText(title);
		} else {
			this.titleAreaPanel.setTitle(title);
		}
	}
	
	public void setTaskPanes(Collection<JXTaskPane> taskpanes){
		content.removeAll();
		hasTaskpanes = false;
		if(taskpanes != null){
			for(JXTaskPane t : taskpanes){
				content.add(t, null);
				hasTaskpanes = true;
			}
		}
		content.validate();
	}

	/**
	 * Ajoute les {@link JXTaskPane} demandés.<br />
	 * Prends en compte l'existence de la base demandée.
	 * @param base
	 * @param type
	 * @param name
	 */
	private void addTaskpanes(DatasManager.Type base, int type, String name){
		if(taskpanes.get(base) != null) {
			List<JXTaskPane> panesList = taskpanes.get(base).getTaskPanes(type, name);
			if(panesList != null){
				for(JXTaskPane pane : panesList){
					hasTaskpanes = true;
					content.add(pane, null);
				}
			}
		}
	}
	
	
}
