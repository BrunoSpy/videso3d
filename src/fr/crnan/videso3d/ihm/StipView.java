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

import javax.swing.JPanel;
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
 * @version 0.6.0
 */
@SuppressWarnings("serial")
public class StipView extends FilteredMultiTreeTableView{

	public StipView(){

		try {
			if(DatabaseManager.getCurrentStip() != null) { //si pas de bdd, ne pas créer la vue
								
				//Routes
				DefaultMutableTreeNode routesRoot = new DefaultMutableTreeNode("root");
				this.fillRoutesRootNode(routesRoot);
				this.addTableTree(new FilteredTreeTableModel(routesRoot), "Routes", this.createTitleRoutes());

				//Balises
				DefaultMutableTreeNode balisesRoot = new DefaultMutableTreeNode("root");
				this.fillBalisesRootNode(balisesRoot);
				this.addTableTree(new FilteredTreeTableModel(balisesRoot), "Balises", this.createTitleBalises());
				
				DefaultMutableTreeNode secteursRoot = new DefaultMutableTreeNode("root");
				this.fillSecteursRootNode(secteursRoot);
				this.addTableTree(new FilteredTreeTableModel(secteursRoot), "Secteurs", null);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

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
	
	private void fillSecteursRootNode(DefaultMutableTreeNode root){
			
			DefaultMutableTreeNode aix = new DefaultMutableTreeNode(new Couple<String, Boolean>("Aix", false));
			this.fillSecteursNode(aix, "AIX", "F");
			this.fillSecteursNode(aix, "AIX", "U");
			root.add(aix);
			
			DefaultMutableTreeNode bord = new DefaultMutableTreeNode(new Couple<String, Boolean>("Bordeaux", false));
			this.fillSecteursNode(bord, "BORD", "F");
			this.fillSecteursNode(bord, "BORD", "U");		
			root.add(bord);
			
			DefaultMutableTreeNode bst = new DefaultMutableTreeNode(new Couple<String, Boolean>("Brest", false));
			this.fillSecteursNode(bst, "BRST", "F");
			this.fillSecteursNode(bst, "BRST", "U");
			root.add(bst);
			
			DefaultMutableTreeNode paris = new DefaultMutableTreeNode(new Couple<String, Boolean>("Paris", false));
			this.fillSecteursNode(paris, "PARI", "F");
			this.fillSecteursNode(paris, "PARI", "U");
			root.add(paris);
			
			DefaultMutableTreeNode reim = new DefaultMutableTreeNode(new Couple<String, Boolean>("Reims", false));
			this.fillSecteursNode(reim, "REIM", "F");
			this.fillSecteursNode(reim, "REIM", "U");
			root.add(reim);			
			
			DefaultMutableTreeNode autre = new DefaultMutableTreeNode(new Couple<String, Boolean>("Autres", false));
			this.fillSecteursNode(autre, "Autre", "F");
			this.fillSecteursNode(autre, "Autre", "U");
			root.add(autre);

	}
	
	private void fillSecteursNode(DefaultMutableTreeNode root, String centre, String type){
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(new Couple<String, Boolean>(type.equals("F") ? "FIR" : "UIR", false));
		try {
			Statement st = DatabaseManager.getCurrentStip();
			String centreCondition = centre.equals("Autre") ? "centre != 'REIM' and centre != 'PARI' and centre != 'AIX' and centre != 'BRST' and centre != 'BORD'" : "centre = '"+centre+"'" ;
			ResultSet rs = st.executeQuery("select * from secteurs where "+centreCondition+" and espace ='"+type+"' order by nom");
			while(rs.next()){
				node.add(new DefaultMutableTreeNode(new Couple<String, Boolean>(rs.getString("nom"), false)));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		root.add(node);
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

	/**
	 * Titre du panel Routes.<br />
	 * Contient un sélecteur pour choisir la méthode de représentation (2D/3D).
	 * @return JPanel
	 */
	private JPanel createTitleBalises(){
		
		TitleTwoButtons titlePanel = new TitleTwoButtons("Balises", "2D", "3D", true);
		titlePanel.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				Boolean state = e.getStateChange() == ItemEvent.SELECTED;
				getController().setBalisesLayer3D(!state);
			}
		});
		
		return titlePanel; 
	}
	
	@Override
	public void reset() {
		super.reset();
	}

}