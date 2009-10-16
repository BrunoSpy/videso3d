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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.tree.DefaultMutableTreeNode;

import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.VidesoGLCanvas;
/**
 * Sélecteur d'objets Stip
 * @author Bruno Spyckerelle
 * @version 0.2
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
	
	
	private DatabaseManager db;
	private VidesoGLCanvas wwd;

	public StipView(VidesoGLCanvas wwd, DatabaseManager db){

		this.db = db;
		this.wwd = wwd;

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(null);
		
		routes.setBorder(BorderFactory.createTitledBorder("Routes"));
		panel.add(routes);
		
		balises.setBorder(BorderFactory.createTitledBorder("Balises"));
		panel.add(balises);
		
		
		secteurs.setBorder(BorderFactory.createTitledBorder("Secteurs"));

		try {
			if(this.db.getCurrentStip() != null) { //si pas de bdd, ne pas créer la vue
				this.buildTreePanel();
				
				this.add(this.buildBalisesPanel());
				
				this.add(panel);

				this.add(this.buildSecteursPanel());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		this.add(Box.createVerticalGlue());

	}

	private JPanel buildBalisesPanel(){
        balises.setLayout(new BoxLayout(balises, BoxLayout.X_AXIS));

        balisesNPChk = new JCheckBox("Non publiées");
        balisesNPChk.addItemListener(this.itemCheckBoxListener);

        balisesPubChk = new JCheckBox("Publiées");
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
			Statement st = this.db.getCurrentStip();
			String centreCondition = centre.equals("Autre") ? "centre != 'REIM' and centre != 'PARI' and centre != 'AIX' and centre != 'BRST' and centre != 'BORD'" : "centre = '"+centre+"'" ;
			ResultSet rs = st.executeQuery("select * from secteurs where "+centreCondition+" and espace ='"+type+"' order by nom");
			while(rs.next()){
				JCheckBox chk = new JCheckBox(rs.getString("nom"));
				chk.addItemListener(itemSecteurListener);
				list.add(chk);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		

		

		return scrollPane;
	}
	/**
	 * Construit et renvoit le {@link JPanel} permettant l'affichage des routes et balises
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
		CheckboxTree routesTree = new CheckboxTree(route);
		routesTree.setRootVisible(false);
		routesTree.setCellRenderer(new TreeCellNimbusRenderer());
		routesTree.setOpaque(false);
		routesTree.addTreeCheckingListener(new StipTreeListener());
		
		JScrollPane scrollRouteTree = new JScrollPane(routesTree);
		scrollRouteTree.setBorder(null);
		
		routes.add(scrollRouteTree);
		
		
//		balises.setLayout(new BorderLayout());
//		
//		DefaultMutableTreeNode b = new DefaultMutableTreeNode("balises");
//		DefaultMutableTreeNode pub = new DefaultMutableTreeNode("Publiées");
//		this.addNodes("balises", "1", pub);
//		b.add(pub);
//		DefaultMutableTreeNode npub = new DefaultMutableTreeNode("Non publiées");
//		this.addNodes("balises", "0", npub);
//		b.add(npub);
//		CheckboxTree balisesTree = new CheckboxTree(b);
//		balisesTree.setRootVisible(false);
//		balisesTree.setCellRenderer(new TreeCellNimbusRenderer());
//		balisesTree.setOpaque(false);
//		balisesTree.addTreeCheckingListener(new StipTreeListener());
//		
//		JScrollPane scrollBaliseTree = new JScrollPane(balisesTree);
//		scrollBaliseTree.setBorder(null);
//		
//		balises.add(scrollBaliseTree);
		
		
		

	}

	/**
	 * Ajoute à <code>root</code> les noeuds correspondants
	 * @param type Type des noeuds à ajouter (routes, balises)
	 * @param classe Classe des noeuds à ajouter (FIR, UIR, Publiées, ...)
	 * @param root Noeud recevant
	 */
	private void addNodes(String type, String classe, DefaultMutableTreeNode root){
		try {
			Statement st = this.db.getCurrentStip();
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
                                    wwd.toggleLayer(wwd.getBalisesPubMarkers(), true);
                                    wwd.toggleLayer(wwd.getBalisesPubTexts(), true);
                            } else if(source == balisesNPChk){
                                    wwd.toggleLayer(wwd.getBalisesNPMarkers(), true);
                                    wwd.toggleLayer(wwd.getBalisesNPTexts(), true);
                            }
                    } else {
                            if(source == balisesPubChk){
                                    wwd.toggleLayer(wwd.getBalisesPubMarkers(), false);
                                    wwd.toggleLayer(wwd.getBalisesPubTexts(), false);
                            } else if(source == balisesNPChk){
                                    wwd.toggleLayer(wwd.getBalisesNPMarkers(), false);
                                    wwd.toggleLayer(wwd.getBalisesNPTexts(), false);
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
					wwd.getRoutesLayer().displayAllRoutes();
				} else  {
					wwd.getRoutesLayer().hideAllRoutes();
				}
			} else if (name.equals("AWY")){
				if(e.isCheckedPath()){
					wwd.getRoutesLayer().displayAllRoutesAwy();
				} else  {
					wwd.getRoutesLayer().hideAllRoutesAWY();
				}
			} else if(name.equals("PDR")) {
				if(e.isCheckedPath()){
					wwd.getRoutesLayer().displayAllRoutesPDR();
				} else  {
					wwd.getRoutesLayer().hideAllRoutesPDR();
				}
			} else if (((String)((DefaultMutableTreeNode)c.getParent()).getUserObject()).equals("AWY")) {
				if(e.isCheckedPath()){
					wwd.getRoutesLayer().displayRouteAwy(name);
				} else {
					wwd.getRoutesLayer().hideRouteAwy(name);
				}
			} else if (((String)((DefaultMutableTreeNode)c.getParent()).getUserObject()).equals("PDR")) {
				if(e.isCheckedPath()){
					wwd.getRoutesLayer().displayRoutePDR(name);
				} else {
					wwd.getRoutesLayer().hideRoutePDR(name);
				}
			} 
			wwd.redraw();
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