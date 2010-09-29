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

import it.cnr.imaa.essi.lablib.gui.checkboxtree.CheckboxTree;
import it.cnr.imaa.essi.lablib.gui.checkboxtree.DefaultCheckboxTreeCellRenderer;
import it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingEvent;
import it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingListener;

import java.awt.BorderLayout;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.tree.DefaultMutableTreeNode;

import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.VidesoController;
import fr.crnan.videso3d.aip.AIP;
import fr.crnan.videso3d.aip.AIPController;
import fr.crnan.videso3d.ihm.components.DataView;

/**
 * Sélecteur d'objets AIP.
 * @author Adrien VIDAL
 *
 */
public class AIPView extends JPanel implements DataView{

	private JPanel volumes = new JPanel();
	
	/**
	 * Arbre de checkbox pour tous les "volumes" définis dans le fichier xml du SIA.
	 */
	private CheckboxTree volumesTree;
	
	private AIPController controller;
	

	
	public AIPView(AIPController aipController) {
		this.controller = aipController;
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		volumes.setBorder(BorderFactory.createTitledBorder("Volumes"));
		try {
			if(DatabaseManager.getCurrentAIP() != null) { //si pas de bdd, ne pas créer la vue
				this.buildTreePanel();
				volumes.setBorder(BorderFactory.createTitledBorder("Zones"));
				this.add(volumes);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		this.add(Box.createVerticalGlue());
	}
	
	
	@Override
	public VidesoController getController() {
		return controller;
	}

	@Override
	public void reset() {
		this.controller.reset();
		volumesTree.clearChecking();
	}



	private void buildTreePanel() {
		//Construction du panel avec le checkboxTree qui ne contient que les TSA pour l'instant.
		volumes.setLayout(new BorderLayout());
		DefaultMutableTreeNode volume = new DefaultMutableTreeNode("volumes");
		DefaultMutableTreeNode TSAs = new DefaultMutableTreeNode("TSA");
		this.addNodes("TSA", "TSA", TSAs);
		volume.add(TSAs);
		
		volumesTree = new CheckboxTree(volume);
		volumesTree.setRootVisible(false);
		volumesTree.setCellRenderer(new TreeCellNimbusRenderer());
		volumesTree.setOpaque(false);
		volumesTree.addTreeCheckingListener(new AIPVolumeTreeListener());
		JScrollPane scrollVolumesTree = new JScrollPane(volumesTree);
		scrollVolumesTree.setBorder(null);
		volumes.add(scrollVolumesTree, BorderLayout.CENTER);
	}

	
	/** Ajoute à <code>root</code> les noeuds correspondants
	 * @param type Type des noeuds à ajouter (routes, balises)
	 * @param classe Classe des noeuds à ajouter (FIR, UIR, Publiées, ...)
	 * @param root Noeud recevant
	 */
	private void addNodes(String type, String classe, DefaultMutableTreeNode root){
		try {
			Statement st = DatabaseManager.getCurrentAIP();
			ResultSet rs = st.executeQuery("select * from volumes where type = '"+type+"'");
			while(rs.next()){
				root.add(new DefaultMutableTreeNode(rs.getString("nom")));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	/**
	 * @author Bruno Spyckerelle, Adrien Vidal
	 * @version 0.1
	 */
	private class AIPVolumeTreeListener implements TreeCheckingListener{

		@Override
		public void valueChanged(TreeCheckingEvent e) {
			DefaultMutableTreeNode c = (DefaultMutableTreeNode) e.getPath().getLastPathComponent();
			String name = (String)c.getUserObject();
			if (name.equals("TSA")){
				if(e.isCheckedPath()){
					controller.displayAll(AIP.TSA);
				} else  {
					controller.hideAll(AIP.TSA);
				}
			} //TODO corriger la condition
			else if (((String)((DefaultMutableTreeNode)c.getParent()).getUserObject()).equals("TSA")) {
				if(e.isCheckedPath()){
					controller.showObject(AIP.TSA,name);
				} else {
					controller.hideObject(AIP.TSA,name);
				}
			} 
		}

	}

	
	/**
	 * Classe temporaire pour corriger un bug de rendu avec le style Nimbus
	 * @author Bruno Spyckerelle
	 */
	private class TreeCellNimbusRenderer extends DefaultCheckboxTreeCellRenderer {
		public TreeCellNimbusRenderer(){
			this.setOpaque(false);
			add(this.checkBox);
			add(this.label);
		}
	}
}
