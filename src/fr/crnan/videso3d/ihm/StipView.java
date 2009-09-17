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
import java.awt.Component;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
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
	
	private StipViewListener listener;
	
	public StipView(StipViewListener listener){
		
		this.listener = listener;
		
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		routes.setBorder(BorderFactory.createTitledBorder("Routes"));
		balises.setBorder(BorderFactory.createTitledBorder("Balises"));
		secteurs.setBorder(BorderFactory.createTitledBorder("Secteurs"));
		
		this.add(this.buildRoutesPanel());

		this.add(this.buildBalisesPanel());

		this.add(this.buildSecteursPanel());
		
		this.add(Box.createVerticalGlue());
		
	}

	private JTabbedPane buildSecteursPanel() {
		
		secteurs.addTab("Paris", this.buildTabSecteur("Paris"));
		secteurs.addTab("Reims", this.buildTabSecteur("Paris"));
		secteurs.addTab("Aix", this.buildTabSecteur("Paris"));
		secteurs.addTab("Brest", this.buildTabSecteur("Paris"));
		secteurs.addTab("Bordeaux", this.buildTabSecteur("Paris"));
		secteurs.addTab("Autre", this.buildTabSecteur("Paris"));
		return secteurs;
	}

	private JPanel buildTabSecteur(String secteur){
		JPanel tab = new JPanel();
		tab.setLayout(new BoxLayout(tab, BoxLayout.X_AXIS));
		
		JPanel fir = new JPanel();
		fir.setBorder(BorderFactory.createTitledBorder("FIR"));
		JList firList = new JList();
		
//		Vector<String> firData = new Vector<String>();
//		firData.add("AP");
//		firData.add("UK");
//		firData.add("UZ");
//		firList.setListData(firData);
		fir.add(firList, BorderLayout.CENTER);
		
		JPanel uir = new JPanel();
		uir.setBorder(BorderFactory.createTitledBorder("UIR"));
		uir.add(new JList(), BorderLayout.CENTER);
		
		tab.add(fir);
		tab.add(uir);
		
		return tab;
	}
	
	/**
	 * Construit et renvoit le {@link JPanel} permettant l'affichage des différents types de routes
	 * @return {@link JPanel} 
	 */
	private JPanel buildRoutesPanel(){
		routes.setLayout(new BoxLayout(routes, BoxLayout.X_AXIS));
		
		JCheckBox routesAwyChk = new JCheckBox();
		routesAwyChk.addItemListener(listener.getRouteAwyListener());
		
		JCheckBox routesPDRChk = new JCheckBox();
		routesPDRChk.addItemListener(listener.getRoutePDRListener());
		
		JLabel routesAwyLbl = new JLabel("AWY");
		JLabel routesPDRLbl = new JLabel("PDR");
		
		routes.add(Box.createHorizontalGlue());
		routes.add(routesAwyChk);
		routes.add(routesAwyLbl);
		routes.add(Box.createHorizontalGlue());
		routes.add(routesPDRChk);
		routes.add(routesPDRLbl);
		routes.add(Box.createHorizontalGlue());
		
		return routes;
	}
	
	private JPanel buildBalisesPanel(){
		balises.setLayout(new BoxLayout(balises, BoxLayout.X_AXIS));
		
		JCheckBox balisesNPChk = new JCheckBox();
		balisesNPChk.addItemListener(listener.getBalisesNPListener());
		JLabel balisesNPLbl = new JLabel("Non publiées");
		
		JCheckBox balisesPubChk = new JCheckBox();
		balisesPubChk.addItemListener(listener.getBalisesPubListener());
		JLabel balisesPubLbl = new JLabel("Publiées");
		
		balises.add(Box.createHorizontalGlue());
		balises.add(balisesPubChk);
		balises.add(balisesPubLbl);
		balises.add(Box.createHorizontalGlue());
		balises.add(balisesNPChk);
		balises.add(balisesNPLbl);
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
	
}
