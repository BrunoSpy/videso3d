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


import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import fr.crnan.videso3d.graphics.RadioCovPolygon;
import fr.crnan.videso3d.ihm.components.DataView;
import fr.crnan.videso3d.radio.RadioCovController;
import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.VidesoController;

import gov.nasa.worldwind.layers.AirspaceLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.render.airspaces.Airspace;

/**  
 * @author Mickaël Papail
 * @author Bruno Spyckerelle
 * @version 0.2
 * Interface de sélection des couvertures radios.
 */
public class RadioCovView extends JPanel implements DataView {
	
	private JPanel jPanel1= new JPanel();
	private Box box = Box.createVerticalBox();
	
	private List<JCheckBox> checkboxes = new LinkedList<JCheckBox>();
	
	private ItemAntennaListener itemAntennaListener = new ItemAntennaListener();	

	private AirspaceLayer radioCovAirspaces;
			
	private RadioCovController controller;
	
	public RadioCovView(RadioCovController c) {		
	
		this.controller = c;
						
		try {			
			if(DatabaseManager.getCurrentRadioCov() != null) { 									
				initGUI();
				if (initRadioCovAirspaces()){					
					feedPanel();
				}						
			}
		}
		catch (SQLException e) {	
			e.printStackTrace();
		}
	}	
													
	public void initGUI() {
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));					
		box.add(Box.createVerticalStrut(10));
		box.setPreferredSize(new Dimension(150,150));		
		box.setBorder(BorderFactory.createTitledBorder("Couvertures radios"));
		jPanel1.add(box);
		this.add(jPanel1);
		this.setVisible(true);					
	}
		
	/***
	 * Recherche du layer contenant les couvertures radio à partir de la Layer List.
	 */
	public boolean initRadioCovAirspaces() {
		for (Layer layer : controller.getLayers()) {				
			if (layer instanceof AirspaceLayer && layer.getName()=="Radio Coverage") {						
				radioCovAirspaces = (AirspaceLayer)layer;
				return true;
			}								
		}

		return false;
	}
		
	/***
	 * Alimentation du Panel en infos.
	 */
	public void feedPanel() {		
		//JScrollPane scrollPane = new JScrollPane(jPanel1);		
		for (Airspace airspace : radioCovAirspaces.getAirspaces()) {
			if ((airspace  instanceof RadioCovPolygon)) {
				JCheckBox check = new JCheckBox(((RadioCovPolygon) airspace).getName());
				check.setAlignmentX(LEFT_ALIGNMENT);
				box.add(check);
				checkboxes.add(check);
				box.add(Box.createVerticalStrut(5));
				check.addItemListener(itemAntennaListener);
			}
		}
	}
		
	@Override
	public void reset() {	
		for(JCheckBox check : checkboxes){
			if(check.isSelected()){
				check.setSelected(false);
			}
		}
		controller.reset();
	}
	
	
	/**
	 * Listener de sélection des couvertures.
	 */
		
	public class ItemAntennaListener implements ItemListener {
		public void itemStateChanged(ItemEvent e) {
			String antennaName= ((JCheckBox)e.getSource()).getText();
			if (e.getStateChange() == ItemEvent.SELECTED) {
				controller.showObject(RadioCovController.ANTENNE, antennaName);
			}
			else {
				controller.hideObject(RadioCovController.ANTENNE, antennaName);
			}
		}
	}


	@Override
	public VidesoController getController() {
		return controller;
	}			
}	

