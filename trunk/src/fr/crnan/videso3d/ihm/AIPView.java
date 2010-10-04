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
		DefaultMutableTreeNode SIVs = new DefaultMutableTreeNode("SIV");
		DefaultMutableTreeNode CTRs = new DefaultMutableTreeNode("CTR");
		DefaultMutableTreeNode TMAs = new DefaultMutableTreeNode("TMA");
		DefaultMutableTreeNode Rs = new DefaultMutableTreeNode("R");
		DefaultMutableTreeNode Ds = new DefaultMutableTreeNode("D");
		this.addNodes("TSA", "TSA", TSAs);
		this.addNodes("SIV", "SIV", SIVs);
		this.addNodes("CTR", "CTR", CTRs);
		this.addNodes("TMA", "TMA", TMAs);
		this.addNodes("R", "R", Rs);
		this.addNodes("D", "D", Ds);
		volume.add(TSAs);
		volume.add(SIVs);
		volume.add(CTRs);
		volume.add(TMAs);
		volume.add(Rs);
		volume.add(Ds);
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
	 * @param classe Classe des noeuds à ajouter
	 * @param root Noeud recevant
	 */
	private void addNodes(String type, String classe, DefaultMutableTreeNode root){
		try {
			Statement st = DatabaseManager.getCurrentAIP();
			ResultSet rs = st.executeQuery("select * from volumes where type = '"+type+"' ORDER BY nom");
			while(rs.next()){
				String nomAffiche = rs.getString("nom");
				//si le type est indiqué dans le nom de la zone, on l'enlève 
				int lettersToRemove = startsWithType(nomAffiche);
				nomAffiche = nomAffiche.substring(lettersToRemove);
				
				root.add(new DefaultMutableTreeNode(nomAffiche));
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
			if(!c.getUserObject().equals("volumes")){
				String name = (String)c.getUserObject();
				int type=-1;
				type = getNodeType(c);
				if(!((String)((DefaultMutableTreeNode)c.getParent()).getUserObject()).equals("volumes")
						&& !((String)((DefaultMutableTreeNode)c.getParent()).getUserObject()).equals("TSA")
						&& !((String)((DefaultMutableTreeNode)c.getParent()).getUserObject()).equals("R")
						&& !((String)((DefaultMutableTreeNode)c.getParent()).getUserObject()).equals("D"))
				{
					name = (String)((DefaultMutableTreeNode) c.getParent()).getUserObject()+ " " + name;
				}

				
				if (((String)((DefaultMutableTreeNode)c.getParent()).getUserObject()).equals("volumes")) {
					if(e.isCheckedPath()){
						controller.displayAll(type);
					} else  {
						controller.hideAll(type);
					}
				}
				else{
					if(e.isCheckedPath()){
						controller.showObject(type,name);
					} else {
						controller.hideObject(type,name);
					}
				}
			}
		}
		
		private int getNodeType(DefaultMutableTreeNode c){
			String type=null;
			if(((String)((DefaultMutableTreeNode)c.getParent()).getUserObject()).equals("volumes")){
				type = (String) c.getUserObject();
			}else{
				type = (String)((DefaultMutableTreeNode)c.getParent()).getUserObject();
			}
			return AIP.getTypeInt(type);
		}

	}

	
	/**
	 * Vérifie si le nom de la zone commence par le type (CTR, TMA,...)
	 * @param name Le nom à vérifier
	 * @return Le nombre de lettres à enlever, où 0 si le type de la zone n'est pas indiqué dans le nom.
	 */
	private int startsWithType(String name){
		if(name.startsWith("SIV")
				||name.startsWith("CTR")
				||name.startsWith("TMA")){
			return 4;
		}
		return 0;
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
