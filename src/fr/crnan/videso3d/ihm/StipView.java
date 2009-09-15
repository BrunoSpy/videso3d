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

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
/**
 * Sélecteur d'objets Stip
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class StipView extends JPanel {

	private JPanel routes = new JPanel();
	
	private JPanel balises = new JPanel();
	
	private JPanel secteurs = new JPanel();
	
	public StipView(){
		
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		routes.setBorder(BorderFactory.createTitledBorder("Routes"));
		balises.setBorder(BorderFactory.createTitledBorder("Balises"));
		secteurs.setBorder(BorderFactory.createTitledBorder("Secteurs"));
		
		this.add(routes);
		this.add(balises);
		this.add(secteurs);
		
	}

	public JPanel getRoutes() {
		return routes;
	}

	public JPanel getBalises() {
		return balises;
	}

	public JPanel getSecteurs() {
		return secteurs;
	}
	
	
}
