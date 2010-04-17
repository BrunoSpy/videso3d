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
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.tree.DefaultMutableTreeNode;

import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.VidesoGLCanvas;
import fr.crnan.videso3d.layers.BaliseLayer;
/**
 * Sélecteur d'objets Stip
 * @author Bruno Spyckerelle
 * @version 0.3.1
 */
@SuppressWarnings("serial")
public class StipView extends JPanel {

	/**
	 * Arbre des routes et balises
	 */
	private JPanel routes = new JPanel();

	private JPanel balises = new JPanel();

	private JCheckBox balisesNPChk;
	private JCheckBox balisesPubChk;
	/**
	 * Choix des secteurs à afficher
	 */
	private JTabbedPane secteurs = new JTabbedPane();	

	private ItemCheckBoxListener itemCheckBoxListener = new ItemCheckBoxListener();

	private ItemSecteurListener itemSecteurListener = new ItemSecteurListener();

	private VidesoGLCanvas wwd;

	/**
	 * Liste des checkbox de la vue, afin de pouvoir tous les désélectionner facilement
	 */
	private List<JCheckBox> checkBoxList = new LinkedList<JCheckBox>();
	private CheckboxTree routesTree;
	
	public StipView(VidesoGLCanvas wwd){

		this.wwd = wwd;

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));


		routes.setBorder(BorderFactory.createTitledBorder(""));
		balises.setBorder(BorderFactory.createTitledBorder("Balises"));
		secteurs.setBorder(BorderFactory.createTitledBorder("Secteurs"));

		try {
			if(DatabaseManager.getCurrentStip() != null) { //si pas de bdd, ne pas créer la vue
				this.add(this.buildBalisesPanel());
				this.buildTreePanel();
				this.add(this.createTitleRoutes());
				this.add(routes);
				this.add(this.buildSecteursPanel());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		this.add(Box.createVerticalGlue());

	}

	/**
	 * Titre du panel Routes.<br />
	 * Contient un sélecteur pour choisir la méthode de représentation (2D/3D).
	 * @return JPanel
	 */
	private JPanel createTitleRoutes(){
		JPanel titleRoute = new JPanel();
		titleRoute.setLayout(new BoxLayout(titleRoute, BoxLayout.X_AXIS));
		titleRoute.setBorder(BorderFactory.createEmptyBorder(0, 17, 1, 3));
		JLabel routeLabel = new JLabel("Routes");
		routeLabel.setFont(routeLabel.getFont().deriveFont(Font.BOLD));
		titleRoute.add(routeLabel);

		JRadioButton flatRoutes = new JRadioButton("2D");
		flatRoutes.setSelected(true);
		flatRoutes.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				Boolean state = e.getStateChange() == ItemEvent.SELECTED;
				wwd.toggleLayer(wwd.getRoutes2DLayer(), state);
				wwd.toggleLayer(wwd.getRoutes3DLayer(), !state);
			}
		});
		JRadioButton round = new JRadioButton("3D");
		ButtonGroup style = new ButtonGroup();
		style.add(flatRoutes);
		style.add(round);
		JPanel stylePanel = new JPanel();
		stylePanel.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));
		stylePanel.setLayout(new BoxLayout(stylePanel, BoxLayout.X_AXIS));
		stylePanel.add(Box.createHorizontalGlue());
		//	stylePanel.add(label);
		stylePanel.add(flatRoutes);
		stylePanel.add(round);
		//	stylePanel.add(Box.createHorizontalGlue());
		titleRoute.add(stylePanel);

		return titleRoute;
	}

	private JPanel buildBalisesPanel(){
		balises.setLayout(new BoxLayout(balises, BoxLayout.X_AXIS));

		balisesNPChk = new JCheckBox("Non publiées");
		checkBoxList.add(balisesNPChk);
		balisesNPChk.addItemListener(this.itemCheckBoxListener);

		balisesPubChk = new JCheckBox("Publiées");
		checkBoxList.add(balisesPubChk);
		balisesPubChk.addItemListener(this.itemCheckBoxListener);

		balises.add(Box.createHorizontalGlue());
		balises.add(balisesPubChk);
		balises.add(Box.createHorizontalGlue());
		balises.add(balisesNPChk);
		balises.add(Box.createHorizontalGlue());

		return balises;
	}

	private JTabbedPane buildSecteursPanel() {

		secteurs.addTab("Paris", this.buildTabSecteur("PARI"));
		secteurs.addTab("Reims", this.buildTabSecteur("REIM"));
		secteurs.addTab("Aix", this.buildTabSecteur("AIX"));
		secteurs.addTab("Brest", this.buildTabSecteur("BRST"));
		secteurs.addTab("Bordeaux", this.buildTabSecteur("BORD"));
		secteurs.addTab("Autre", this.buildTabSecteur("Autre"));
		return secteurs;
	}

	private JPanel buildTabSecteur(String centre){
		JPanel tab = new JPanel();
		tab.setLayout(new BoxLayout(tab, BoxLayout.X_AXIS));

		tab.add(this.buildListSecteur(centre, "F"));
		tab.add(this.buildListSecteur(centre, "U"));

		return tab;
	}

	private JScrollPane buildListSecteur(String centre, String type){

		JPanel list = new JPanel();
		list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
		JScrollPane scrollPane = new JScrollPane(list);
		scrollPane.setBorder(BorderFactory.createTitledBorder(/*BorderFactory.createEmptyBorder(),*/ type.equals("F") ? "FIR" : "UIR"));

		try {
			Statement st = DatabaseManager.getCurrentStip();
			String centreCondition = centre.equals("Autre") ? "centre != 'REIM' and centre != 'PARI' and centre != 'AIX' and centre != 'BRST' and centre != 'BORD'" : "centre = '"+centre+"'" ;
			ResultSet rs = st.executeQuery("select * from secteurs where "+centreCondition+" and espace ='"+type+"' order by nom");
			while(rs.next()){
				JCheckBox chk = new JCheckBox(rs.getString("nom"));
				chk.addItemListener(itemSecteurListener);
				list.add(chk);
				checkBoxList.add(chk);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}




		return scrollPane;
	}
	/**
	 * Construit et renvoit le {@link JPanel} permettant l'affichage des routes
	 * @return {@link JPanel} 
	 */
	private void buildTreePanel(){
		routes.setLayout(new BorderLayout());

		DefaultMutableTreeNode route = new DefaultMutableTreeNode("routes");
		DefaultMutableTreeNode awy = new DefaultMutableTreeNode("AWY");
		this.addNodes("routes", "F", awy);
		route.add(awy);
		DefaultMutableTreeNode pdr = new DefaultMutableTreeNode("PDR");
		this.addNodes("routes", "U", pdr);
		route.add(pdr);
		routesTree = new CheckboxTree(route);
		routesTree.setRootVisible(false);
		routesTree.setCellRenderer(new TreeCellNimbusRenderer());
		routesTree.setOpaque(false);
		routesTree.addTreeCheckingListener(new StipTreeListener());

		JScrollPane scrollRouteTree = new JScrollPane(routesTree);
		scrollRouteTree.setBorder(null);

		routes.add(scrollRouteTree, BorderLayout.CENTER);

	}

	/**
	 * Ajoute à <code>root</code> les noeuds correspondants
	 * @param type Type des noeuds à ajouter (routes, balises)
	 * @param classe Classe des noeuds à ajouter (FIR, UIR, Publiées, ...)
	 * @param root Noeud recevant
	 */
	private void addNodes(String type, String classe, DefaultMutableTreeNode root){
		try {
			Statement st = DatabaseManager.getCurrentStip();
			String where = type.equals("routes")? "espace" : "publicated";
			ResultSet rs = st.executeQuery("select * from "+type+" where "+ where +" ='"+classe+"'");
			while(rs.next()){
				root.add(new DefaultMutableTreeNode(rs.getString("name")));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}


	public JTabbedPane getSecteurs() {
		return secteurs;
	}	

	
	/**
	 * Décoche tous les éléments cochés
	 */
	public void reset() {
		for(JCheckBox c : checkBoxList){
			if(c.isSelected()){
				c.setSelected(false);
			}
		}
		routesTree.clearChecking();
	}
	
	/*--------------------------------------------------*/
	/*------------------ Listeners ---------------------*/

	/**
	 * Listener de la checkbox AWY
	 * @author Bruno Spyckerelle
	 */
	private class ItemCheckBoxListener implements ItemListener {
		@Override
		public void itemStateChanged(ItemEvent e) {
			Object source = e.getItemSelectable();
			if(e.getStateChange() == ItemEvent.SELECTED ) {
				if(source == balisesPubChk){
					((BaliseLayer)wwd.getBalisesPubLayer()).showAll();
					((BaliseLayer)wwd.getBalisesPubLayer()).setLocked(true);
//					wwd.toggleLayer(wwd.getBalisesPubLayer(), true);
				} else if(source == balisesNPChk){
					((BaliseLayer)wwd.getBalisesNPLayer()).showAll();
					((BaliseLayer)wwd.getBalisesNPLayer()).setLocked(true);
//					wwd.toggleLayer(wwd.getBalisesNPLayer(), true);
				}
			} else {
				if(source == balisesPubChk){
					((BaliseLayer)wwd.getBalisesPubLayer()).setLocked(false);
					((BaliseLayer)wwd.getBalisesPubLayer()).removeAllBalises();
//					wwd.toggleLayer(wwd.getBalisesPubLayer(), false);
				} else if(source == balisesNPChk){
					((BaliseLayer)wwd.getBalisesNPLayer()).setLocked(false);
					((BaliseLayer)wwd.getBalisesNPLayer()).removeAllBalises();
//					wwd.toggleLayer(wwd.getBalisesNPLayer(), false);
				}
			}
		}      
	}

	/**
	 * @author Bruno Spyckerelle
	 * @version 0.1
	 */
	private class StipTreeListener implements TreeCheckingListener{

		@Override
		public void valueChanged(TreeCheckingEvent e) {
			DefaultMutableTreeNode c = (DefaultMutableTreeNode) e.getPath().getLastPathComponent();
			String name = (String)c.getUserObject();
			if(name.equals("routes")){
				if(e.isCheckedPath()){
					wwd.getRoutes2DLayer().displayAllRoutes();
					wwd.getRoutes3DLayer().displayAllRoutes();
				} else  {
					wwd.getRoutes2DLayer().hideAllRoutes();
					wwd.getRoutes3DLayer().hideAllRoutes();
				}
			} else if (name.equals("AWY")){
				if(e.isCheckedPath()){
					wwd.getRoutes2DLayer().displayAllRoutesAwy();
					wwd.getRoutes3DLayer().displayAllRoutesAwy();
				} else  {
					wwd.getRoutes2DLayer().hideAllRoutesAWY();
					wwd.getRoutes3DLayer().hideAllRoutesAWY();
				}
			} else if(name.equals("PDR")) {
				if(e.isCheckedPath()){
					wwd.getRoutes2DLayer().displayAllRoutesPDR();
					wwd.getRoutes3DLayer().displayAllRoutesPDR();
				} else  {
					wwd.getRoutes2DLayer().hideAllRoutesPDR();
					wwd.getRoutes3DLayer().hideAllRoutesPDR();
				}
				//TODO corriger la condition
			} else if (((String)((DefaultMutableTreeNode)c.getParent()).getUserObject()).equals("AWY") ||
					((String)((DefaultMutableTreeNode)c.getParent()).getUserObject()).equals("PDR")) {
				if(e.isCheckedPath()){
					wwd.getRoutes2DLayer().displayRoute(name);
					wwd.getRoutes3DLayer().displayRoute(name);
				} else {
					wwd.getRoutes2DLayer().hideRoute(name);
					wwd.getRoutes3DLayer().hideRoute(name);
					wwd.hideRoutesBalises(name);
				}
			} 
		}

	}

	/**
	 * Listener des checkbox secteurs
	 * @author Bruno Spyckerelle
	 */
	private class ItemSecteurListener implements ItemListener {
		@Override
		public void itemStateChanged(ItemEvent e) {
			String name = ((JCheckBox)e.getSource()).getText();
			if(e.getStateChange() == ItemEvent.SELECTED){
				wwd.addSecteur3D(name);
			}
			else {
				wwd.removeSecteur3D(name);
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