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

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.VidesoGLCanvas;
import fr.crnan.videso3d.edimap.Cartes;
import fr.crnan.videso3d.edimap.Entity;

/**
 * Sélecteur de cartes edimap
 * @author Bruno Spyckerelle
 * @version 0.2.1
 */
@SuppressWarnings("serial")
public class EdimapView extends JPanel {

	/**
	 * Cartes statiques
	 */
	private JPanel statiques = new JPanel();
	/**
	 * Cartes dynamiques
	 */
	private JPanel dynamiques = new JPanel();
	/**
	 * Cartes secteurs
	 */
	private JPanel secteurs = new JPanel();
	/**
	 * Cartes volumes
	 */
	private JPanel volumes = new JPanel();
	
	private VidesoGLCanvas wwd;
	
	private Cartes cartes;
	 
	private ItemCheckListener itemCheckListener = new ItemCheckListener();
	
	/**
	 * Liste des checkbox de la vue, afin de pouvoir tous les désélectionner facilement
	 */
	private List<JCheckBox> checkBoxList = new LinkedList<JCheckBox>();
	
	public EdimapView(final VidesoGLCanvas wwd){
		this.wwd = wwd;
		
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		statiques.setBorder(BorderFactory.createTitledBorder("Cartes statiques"));
		dynamiques.setBorder(BorderFactory.createTitledBorder("Cartes dynamiques"));
		secteurs.setBorder(BorderFactory.createTitledBorder("Cartes secteurs"));
		volumes.setBorder(BorderFactory.createTitledBorder("Cartes volumes"));
		try {
			if(DatabaseManager.getCurrentEdimap() != null) {
				cartes = new Cartes();
				this.add(this.buildPanel(secteurs, cartes.getSecteurs()));
				if(!cartes.getVolumes().isEmpty()) this.add(this.buildPanel(volumes, cartes.getVolumes()));
				this.add(this.buildPanel(statiques, cartes.getCartesStatiques()));
				this.add(this.buildPanel(dynamiques, cartes.getCartesDynamiques()));
				this.add(Box.createVerticalGlue());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private Component buildPanel(JPanel panel, List<Entity> liste) {
		JPanel list = new JPanel();
		JScrollPane scrollPane = new JScrollPane(list);
		scrollPane.setBorder(null);
		list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		Iterator<Entity> iterator = liste.iterator();
		while(iterator.hasNext()){
			JCheckBox checkBox = new JCheckBox(iterator.next().getValue("name"));
			checkBox.addItemListener(itemCheckListener);
			checkBoxList.add(checkBox);
			list.add(checkBox);
		}
		panel.add(scrollPane);
		return panel;
	}
	
	public void reset() {
		for(JCheckBox c : checkBoxList){
			if(c.isSelected()){
				c.setSelected(false);
			}
		}
	}
	
	/**
	 * Listener des checkbox
	 * @author Bruno Spyckerelle
	 * @version 0.1
	 */
	private class ItemCheckListener implements ItemListener {
		@Override
		public void itemStateChanged(ItemEvent e) {
			try {
				String name = ((JCheckBox)e.getSource()).getText();
				String type = "";
				if(dynamiques.isAncestorOf((Component) e.getSource())){
					type = "dynamique";
				} else if (statiques.isAncestorOf((Component) e.getSource())){
					type = "statique";
				} else if (secteurs.isAncestorOf((Component) e.getSource())){
					type = "secteur";
				} else if (volumes.isAncestorOf((Component) e.getSource())){
					type = "volume";
				} 
				wwd.addEdimapLayer(cartes.getCarte(name, type));
				wwd.toggleLayer(cartes.getCarte(name, type), e.getStateChange() == ItemEvent.SELECTED);
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}

	}


}