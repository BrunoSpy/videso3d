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
package fr.crnan.videso3d.ihm.components;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;


import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToolBar;

import org.jdesktop.swingx.autocomplete.AutoCompleteComboBoxEditor;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

import fr.crnan.videso3d.Couple;
import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.DatabaseManager.Type;
import fr.crnan.videso3d.VidesoController;
import fr.crnan.videso3d.VidesoGLCanvas;
import fr.crnan.videso3d.geom.LatLonUtils;
import fr.crnan.videso3d.ihm.ContextPanel;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;

/**
 * Universal search box
 * @author Bruno Spyckerelle
 * @version 0.2
 */
public class Omnibox {
	
	private Hashtable<DatabaseManager.Type, Couple<VidesoController, List<Couple<Integer, String>>>> bases = new Hashtable<DatabaseManager.Type, Couple<VidesoController,List<Couple<Integer, String>>>>();
		
	private DatabaseManager.Type selectedBase;
	
	private HashMap<Type, JRadioButtonMenuItem> buttons = new HashMap<DatabaseManager.Type, JRadioButtonMenuItem>();
	
	private DropDownLabel chooseButton;
	private ButtonGroup engines;
	private JComboBox searchBox;
	
	private ContextPanel context;
	
	/**
	 * Creates an omnibox and adds it to the toolbar
	 * @param toolbar
	 */
	public Omnibox(final VidesoGLCanvas wwd, ContextPanel c){
		this.context = c;

		chooseButton = new DropDownLabel(new ImageIcon(getClass().getResource("/resources/zoom-original.png")));
		engines = new ButtonGroup();
		JRadioButtonMenuItem allEngine = new JRadioButtonMenuItem("Toutes les données", true);
		allEngine.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED){
					selectedBase = null;
					update();
				}
			}
		});
		engines.add(allEngine);		
		chooseButton.getPopupMenu().add(allEngine);	
		
		searchBox = new JComboBox(new SortedComboBoxModel(new ItemCoupleComparator()));
		searchBox.setEditable(true);
		searchBox.setToolTipText("<html>Recherche universelle.<br />" +
				"<ul><li>Permet de rechercher dans les bases sélectionnées.</li>" +
				"<li>Permet de centrer la vue sur des coordonnées. Syntaxe acceptée :" +
				"<ul><li>45N 123W</li><li>+45.1234, -123.12</li><li>45.1234N 123.12W</li>" +
				"<li>45° 30' 00\"N, 50° 30'W</li><li>45°30' -50°30'</li><li>45 30 N 50 30 W</li></ul></ul></html>");
		searchBox.setPreferredSize(new Dimension(300,25));
		AutoCompleteDecorator.decorate(searchBox);
		
		//gestion des coordonnées
		searchBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if(e.getActionCommand().equals("comboBoxEdited")){
					Object input = ((JComboBox)e.getSource()).getSelectedItem();
					if(input instanceof String) {
						//try to convert into latlon first
						LatLon coord = LatLonUtils.computeLatLonFromString((String)input);
						if(coord != null) {
							wwd.getView().goTo(new Position(coord, 0), 1e6);
						} 
					} else if(input instanceof ItemCouple){
						Couple<Integer, String> item = ((ItemCouple) input).getSecond();
						//context.showInfo(((ItemCouple) input).getSecond());
						((ItemCouple) input).getFirst().highlight(item.getFirst(), item.getSecond());
					}
				}
			}
		});
	}

	public void addToToolbar(JToolBar toolbar){
		chooseButton.addToToolBar(toolbar);
		toolbar.add(searchBox);
	}

	public void addDatabase(final DatabaseManager.Type type, VidesoController controller, List<Couple<Integer, String>> items){
		if(items == null) {
			if(bases.containsKey(type)) removeDatabase(type);
		} else {
			if(bases.containsKey(type)) {
				bases.remove(type);
			} else {
				JRadioButtonMenuItem newButton = new JRadioButtonMenuItem(type.toString());
				newButton.addItemListener(new ItemListener() {
					
					@Override
					public void itemStateChanged(ItemEvent e) {
						if(e.getStateChange() == ItemEvent.SELECTED){
							selectedBase = type;
							update();
						}
					}
				});
				buttons.put(type, newButton);
				engines.add(newButton);
				chooseButton.getPopupMenu().add(newButton);		
			}
			bases.put(type, new Couple<VidesoController, List<Couple<Integer, String>>>(controller, items));
		}
		update();
	}
	
	public void removeDatabase(DatabaseManager.Type type){
		if(bases.containsKey(type)) {
			buttons.get(type).setSelected(false);
			bases.remove(type);
			engines.remove(buttons.get(type));
			chooseButton.getPopupMenu().remove(buttons.get(type));
		}
		update();
	}
	
	/**
	 * Updates the combobox
	 */
	private void update(){
		searchBox.removeAllItems();
//		searchBox.addItem("");
		if(selectedBase == null){
			for(Couple<VidesoController, List<Couple<Integer, String>>> items : bases.values()){
				for(Couple<Integer, String> item : items.getSecond()){
					searchBox.addItem(new ItemCouple(items.getFirst(), item));
				}
			}
		} else {
			for(Couple<Integer, String> item : bases.get(selectedBase).getSecond()){
				searchBox.addItem(new ItemCouple(bases.get(selectedBase).getFirst(), item));
			}
		}
	}
	
	public void addActionListener(ActionListener listener){
		searchBox.addActionListener(listener);
	}
	
	private class ItemCoupleComparator implements Comparator<ItemCouple> {

		@Override
		public int compare(ItemCouple arg0, ItemCouple arg1) {
			return arg0.getSecond().getSecond().compareTo(arg1.getSecond().getSecond());
		}
		
	}
	
	private class ItemCouple extends Couple<VidesoController, Couple<Integer, String>>{

		public ItemCouple(VidesoController first, Couple<Integer, String> item) {
			super(first, item);
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return getSecond().getSecond().toString()+" ("+getFirst().type2string(getSecond().getFirst())+")";
		}
		
	}
	
}
