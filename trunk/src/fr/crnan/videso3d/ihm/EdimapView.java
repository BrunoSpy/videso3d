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
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.jdesktop.swingx.JXMultiSplitPane;

import fr.crnan.videso3d.DatasManager;
import fr.crnan.videso3d.databases.DatabaseManager;
import fr.crnan.videso3d.databases.edimap.Cartes;
import fr.crnan.videso3d.databases.edimap.EdimapController;
import fr.crnan.videso3d.databases.edimap.Entity;
import fr.crnan.videso3d.ihm.components.DataView;
import fr.crnan.videso3d.ihm.components.VerticalMultipleSplitPanes;

/**
 * Sélecteur de cartes edimap
 * @author Bruno Spyckerelle
 * @version 0.3.2
 */
@SuppressWarnings("serial")
public class EdimapView extends JPanel implements DataView{
	
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
	 
	private ItemCheckListener itemCheckListener = new ItemCheckListener();
	
	/**
	 * Listes des checkbox de la vue, afin de pouvoir tous les désélectionner facilement
	 */
	private HashMap<String,JCheckBox> checkBoxMapDyn = new HashMap<String,JCheckBox>();
	private HashMap<String,JCheckBox> checkBoxMapStat = new HashMap<String,JCheckBox>();
	private HashMap<String,JCheckBox> checkBoxMapVol = new HashMap<String,JCheckBox>();
	private HashMap<String,JCheckBox> checkBoxMapSect = new HashMap<String,JCheckBox>();
	
	private JXMultiSplitPane container = new VerticalMultipleSplitPanes();
	
	
	public EdimapView(){
		
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		this.add(container);
		
		statiques.setBorder(BorderFactory.createTitledBorder("Cartes statiques"));
		dynamiques.setBorder(BorderFactory.createTitledBorder("Cartes dynamiques"));
		secteurs.setBorder(BorderFactory.createTitledBorder("Cartes secteurs"));
		volumes.setBorder(BorderFactory.createTitledBorder("Cartes volumes"));
		try {
			if(DatabaseManager.getCurrentEdimap() != null) {
				Cartes cartes = new Cartes();
				
				container.add(this.buildPanel(statiques, cartes.getCartesStatiques(),Cartes.EDIMAP_STATIC));
				if(!cartes.getVolumes().isEmpty()) container.add(this.buildPanel(volumes, cartes.getVolumes(), Cartes.EDIMAP_VOLUME));
				container.add(this.buildPanel(secteurs, cartes.getSecteurs(), Cartes.EDIMAP_SECTOR));
				container.add(this.buildPanel(dynamiques, cartes.getCartesDynamiques(), Cartes.EDIMAP_DYNAMIC));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}

	private Component buildPanel(JPanel panel, List<Entity> liste, int type) {
		JPanel list = new JPanel();
		JScrollPane scrollPane = new JScrollPane(list);
		scrollPane.setBorder(null);
		list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		Iterator<Entity> iterator = liste.iterator();
		while(iterator.hasNext()){
			String name = iterator.next().getValue("name");
			JCheckBox checkBox = new JCheckBox(name);
			checkBox.addItemListener(itemCheckListener);
			if(type == Cartes.EDIMAP_DYNAMIC)
				checkBoxMapDyn.put(name, checkBox);
			else if(type == Cartes.EDIMAP_STATIC)
				checkBoxMapStat.put(name, checkBox);
			else if(type == Cartes.EDIMAP_VOLUME)
				checkBoxMapVol.put(name, checkBox);
			else if(type == Cartes.EDIMAP_SECTOR)
				checkBoxMapSect.put(name, checkBox);
			list.add(checkBox);
		}
		panel.add(scrollPane);
		return panel;
	}
	
	@Override
	public void reset() {
		List<JCheckBox> checkBoxList = new LinkedList<JCheckBox>();
		checkBoxList.addAll(checkBoxMapDyn.values());
		checkBoxList.addAll(checkBoxMapStat.values());
		checkBoxList.addAll(checkBoxMapSect.values());
		checkBoxList.addAll(checkBoxMapVol.values());
		for(JCheckBox c : checkBoxList){
			if(c.isSelected()){
				c.setSelected(false);
			}
		}
		//cacher les objets qui ont été activés sans passer par la vue
		this.getController().reset();
	}
	
	@Override
	public EdimapController getController(){
		return (EdimapController) DatasManager.getController(DatasManager.Type.Edimap);
	}
	
	/**
	 * Listener des checkbox
	 * @author Bruno Spyckerelle
	 * @version 0.1
	 */
	private class ItemCheckListener implements ItemListener {
		@Override
		public void itemStateChanged(ItemEvent e) {
			String name = ((JCheckBox)e.getSource()).getText();
			int type = -1;
			if(dynamiques.isAncestorOf((Component) e.getSource())){
				type = Cartes.EDIMAP_DYNAMIC;
			} else if (statiques.isAncestorOf((Component) e.getSource())){
				type = Cartes.EDIMAP_STATIC;
			} else if (secteurs.isAncestorOf((Component) e.getSource())){
				type = Cartes.EDIMAP_SECTOR;
			} else if (volumes.isAncestorOf((Component) e.getSource())){
				type = Cartes.EDIMAP_VOLUME;
			} 
			
			if(e.getStateChange() == ItemEvent.SELECTED){
				getController().showObject(type, name);
			} else {
				getController().hideObject(type, name);
			}
		}
	}

	/**
	 * Recherche une checkBox dans un des JPanel
	 * @param type pour préciser le JPanel dans lequel il faut chercher
	 * @param name le nom de la checkbox à chercher
	 */
	private JCheckBox getCheckBox(int type, String name){
		if(type == Cartes.EDIMAP_DYNAMIC){
			return checkBoxMapDyn.get(name);
		}
		if(type == Cartes.EDIMAP_STATIC){
			return checkBoxMapStat.get(name);
		}
		if(type == Cartes.EDIMAP_VOLUME){
			return checkBoxMapVol.get(name);
		}
		if(type == Cartes.EDIMAP_SECTOR){
			return checkBoxMapSect.get(name);
		}
		return null;
	}
	
	@Override
	public void showObject(int type, String name) {
		JCheckBox c = getCheckBox(type, name);
		if(c!=null)
			c.setSelected(true);
	}

	@Override
	public void hideObject(int type, String name) {
		JCheckBox c = getCheckBox(type, name);
		if(c!=null)
			c.setSelected(false);
	}


}
