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

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;


import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.VidesoGLCanvas;
/**
 * Sélecteur d'objets Stip
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class StipView extends JPanel {

	/**
	 * Choix des routes à afficher
	 */
	private JPanel routes = new JPanel();
	/**
	 * Choix des balises à afficher
	 */
	private JPanel balises = new JPanel();
	/**
	 * Choix des secteurs à afficher
	 */
	private JTabbedPane secteurs = new JTabbedPane();	

	/**
	 * Checkboxes
	 */
	private JCheckBox routesAwyChk;
	private JCheckBox routesPDRChk;
	private JCheckBox balisesNPChk;
	private JCheckBox balisesPubChk;

	private ItemCheckBoxListener itemCheckBoxListener = new ItemCheckBoxListener();

	private ItemSecteurListener itemSecteurListener = new ItemSecteurListener();
	
	
	private DatabaseManager db;
	private VidesoGLCanvas wwd;

	public StipView(VidesoGLCanvas wwd, DatabaseManager db){

		this.db = db;
		this.wwd = wwd;

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		routes.setBorder(BorderFactory.createTitledBorder("Routes"));
		balises.setBorder(BorderFactory.createTitledBorder("Balises"));
		secteurs.setBorder(BorderFactory.createTitledBorder("Secteurs"));

		try {
			if(this.db.getCurrentStip() != null) { //si pas de bdd, ne pas créer la vue
				this.add(this.buildRoutesPanel());

				this.add(this.buildBalisesPanel());

				this.add(this.buildSecteursPanel());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		this.add(Box.createVerticalGlue());

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
		scrollPane.setBorder(BorderFactory.createTitledBorder(type.equals("F") ? "FIR" : "UIR"));
		
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
	 * Construit et renvoit le {@link JPanel} permettant l'affichage des différents types de routes
	 * @return {@link JPanel} 
	 */
	private JPanel buildRoutesPanel(){
		routes.setLayout(new BoxLayout(routes, BoxLayout.X_AXIS));

		routesAwyChk = new JCheckBox("AWY");
		routesAwyChk.addItemListener(this.itemCheckBoxListener);

		routesPDRChk = new JCheckBox("PDR");
		routesPDRChk.addItemListener(this.itemCheckBoxListener);


		routes.add(Box.createHorizontalGlue());
		routes.add(routesAwyChk);
		routes.add(Box.createHorizontalGlue());
		routes.add(routesPDRChk);
		routes.add(Box.createHorizontalGlue());

		return routes;
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

	public JPanel getRoutes() {
		return routes;
	}

	public JPanel getBalises() {
		return balises;
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
				if(source == routesAwyChk){
					wwd.toggleLayer(wwd.getRoutesAwy(), true);
				} else if(source == routesPDRChk) {
					wwd.toggleLayer(wwd.getRoutesPDR(), true);
				} else if(source == balisesPubChk){
					wwd.toggleLayer(wwd.getBalisesPubMarkers(), true);
					wwd.toggleLayer(wwd.getBalisesPubTexts(), true);
				} else if(source == balisesNPChk){
					wwd.toggleLayer(wwd.getBalisesNPMarkers(), true);
					wwd.toggleLayer(wwd.getBalisesNPTexts(), true);
				}
			} else {
				if(source == routesAwyChk) {
					wwd.toggleLayer(wwd.getRoutesAwy(), false);	
				} else if(source == routesPDRChk) {
					wwd.toggleLayer(wwd.getRoutesPDR(), false);
				} else if(source == balisesPubChk){
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
}