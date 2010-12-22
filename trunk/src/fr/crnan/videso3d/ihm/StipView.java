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
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.tree.DefaultMutableTreeNode;

import fr.crnan.videso3d.Couple;
import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.DatasManager;
import fr.crnan.videso3d.DatabaseManager.Type;
import fr.crnan.videso3d.ihm.components.FilteredMultiTreeTableView;
import fr.crnan.videso3d.ihm.components.FilteredTreeTableModel;
import fr.crnan.videso3d.ihm.components.TitleTwoButtons;
import fr.crnan.videso3d.stip.StipController;
/**
 * Sélecteur d'objets Stip
 * @author Bruno Spyckerelle
 * @version 0.5.0
 */
@SuppressWarnings("serial")
public class StipView extends FilteredMultiTreeTableView{

	/**
	 * Arbre des routes et balises
	 */
	private JPanel routes = new JPanel();

	/**
	 * Choix des secteurs à afficher
	 */
	private JTabbedPane secteurs = new JTabbedPane();	

	private ItemSecteurListener itemSecteurListener = new ItemSecteurListener();

	/**
	 * Liste des checkbox de la vue, afin de pouvoir tous les désélectionner facilement
	 */
	private List<JCheckBox> checkBoxList = new LinkedList<JCheckBox>();
	
	public StipView(){
		
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));


		routes.setBorder(BorderFactory.createTitledBorder(""));
		secteurs.setBorder(BorderFactory.createTitledBorder("Secteurs"));

		try {
			if(DatabaseManager.getCurrentStip() != null) { //si pas de bdd, ne pas créer la vue
				
				//Routes
				DefaultMutableTreeNode routesRoot = new DefaultMutableTreeNode("root");
				this.fillRoutesRootNode(routesRoot);
				this.addTableTree(new FilteredTreeTableModel(routesRoot), "Routes", this.createTitleRoutes());
				
				//Balises
				DefaultMutableTreeNode balisesRoot = new DefaultMutableTreeNode("root");
				this.fillBalisesRootNode(balisesRoot);
				this.addTableTree(new FilteredTreeTableModel(balisesRoot), "Balises", null);
				
				this.add(this.buildSecteursPanel());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		this.add(Box.createVerticalGlue());

	}
	
	@Override
	public StipController getController(){
		return (StipController) DatasManager.getController(Type.STIP);
	}
	
	private void fillRoutesRootNode(DefaultMutableTreeNode root){
		try{
			Statement st = DatabaseManager.getCurrentStip();
			
			DefaultMutableTreeNode awy = new DefaultMutableTreeNode(new Couple<String, Boolean>("AWY", false));
			root.add(awy);
			ResultSet rs = st.executeQuery("select name from routes where espace='F' order by name");
			while(rs.next()){
				awy.add(new DefaultMutableTreeNode(new Couple<String, Boolean>(rs.getString(1), false)));
			}
			DefaultMutableTreeNode pdr = new DefaultMutableTreeNode(new Couple<String, Boolean>("PDR", false));
			root.add(pdr);
			rs = st.executeQuery("select name from routes where espace='U' order by name");
			while(rs.next()){
				pdr.add(new DefaultMutableTreeNode(new Couple<String, Boolean>(rs.getString(1), false)));
			}
			rs.close();
			
			st.close();
		} catch (SQLException e){
			e.printStackTrace();
		}
	}
	
	private void fillBalisesRootNode(DefaultMutableTreeNode root){
		try{
			Statement st = DatabaseManager.getCurrentStip();
			
			DefaultMutableTreeNode pub = new DefaultMutableTreeNode(new Couple<String, Boolean>("Publiées", false));
			root.add(pub);
			ResultSet rs = st.executeQuery("select name from balises where publicated='1' order by name");
			while(rs.next()){
				pub.add(new DefaultMutableTreeNode(new Couple<String, Boolean>(rs.getString(1), false)));
			}
			DefaultMutableTreeNode np = new DefaultMutableTreeNode(new Couple<String, Boolean>("Non publiées", false));
			root.add(np);
			rs = st.executeQuery("select name from balises where publicated='0' order by name");
			while(rs.next()){
				np.add(new DefaultMutableTreeNode(new Couple<String, Boolean>(rs.getString(1), false)));
			}
			rs.close();
			st.close();
		} catch (SQLException e){
			e.printStackTrace();
		}
	}
	
	/**
	 * Titre du panel Routes.<br />
	 * Contient un sélecteur pour choisir la méthode de représentation (2D/3D).
	 * @return JPanel
	 */
	private JPanel createTitleRoutes(){
		
		TitleTwoButtons titlePanel = new TitleTwoButtons("Routes", "2D", "3D", true);
		titlePanel.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				Boolean state = e.getStateChange() == ItemEvent.SELECTED;
				getController().toggleLayer(getController().getRoutes2DLayer(), state);
				getController().toggleLayer(getController().getRoutes3DLayer(), !state);
			}
		});
		
		return titlePanel; 
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
	
	@Override
	public void reset() {
		super.reset();
		for(JCheckBox c : checkBoxList){
			if(c.isSelected()){
				c.setSelected(false);
			}
		}
	}
	
	/*--------------------------------------------------*/
	/*------------------ Listeners ---------------------*/

	/**
	 * Listener des checkbox secteurs
	 * @author Bruno Spyckerelle
	 */
	private class ItemSecteurListener implements ItemListener {
		@Override
		public void itemStateChanged(ItemEvent e) {
			String name = ((JCheckBox)e.getSource()).getText();
			if(e.getStateChange() == ItemEvent.SELECTED){
				getController().showObject(StipController.SECTEUR, name);
			}
			else {
				getController().hideObject(StipController.SECTEUR, name);
			}
		}

	}

}