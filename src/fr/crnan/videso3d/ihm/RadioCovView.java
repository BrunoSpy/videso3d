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

/**  
 * @author mickaël PAPAIL
 * Interface de sélection des couvertures radios.
 */

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;


import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

//import java.awt.BorderLayout;
import java.awt.Dimension;
//import java.awt.Font;
//import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
//import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
//import javax.swing.JLabel;
import javax.swing.JPanel;
//import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
//import javax.swing.JTabbedPane;
//import javax.swing.tree.DefaultMutableTreeNode;

import fr.crnan.videso3d.graphics.RadioCovPolygon;
import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.VidesoGLCanvas;

import gov.nasa.worldwind.layers.AirspaceLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.render.airspaces.Airspace;

public class RadioCovView extends JPanel {//implements ItemListener{
	
	private Boolean DEBUG = false;	
	private JPanel jPanel1= new JPanel();
	private Box box = Box.createVerticalBox();
	
	private ItemAntennaListener itemAntennaListener = new ItemAntennaListener();	
	//tabbedPane1.setBorder(BorderFactory.createTitledBorder("couvertures radio"));
	
	private VidesoGLCanvas wwd;
	private Layer layer;
	private AirspaceLayer radioCovAirspaces=(AirspaceLayer)layer;
	private LayerList layers;
				
	public RadioCovView(VidesoGLCanvas wwd) {		
	
		this.wwd = wwd;			
		layers = wwd.getModel().getLayers();
		if (DEBUG) System.out.println("Valeur de layers :"+layers);
		
		
		//try {
			//if(DatabaseManager.getCurrentRadioCov() != null) { 						
			initGUI();
			initRadioCovAirspaces();
			feedPanel();		
		//	}
		//}			
		//catch (SQLException e) {
		//	e.printStackTrace();
		//}	
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
	public void initRadioCovAirspaces() {
		if (layers != null) {
			for (Layer layer : layers) {				
						if (layer instanceof AirspaceLayer && layer.getName()=="Radio Coverage") {						
							radioCovAirspaces = (AirspaceLayer)layer;
						}								
			}
		}	
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
				box.add(Box.createVerticalStrut(5));
				check.addItemListener(itemAntennaListener);
			}
		}
	}
		
	/**
	 * Listener de sélection des couvertures.
	 */
		
	public class ItemAntennaListener implements ItemListener {
		public void itemStateChanged(ItemEvent e) {
			String antennaName= ((JCheckBox)e.getSource()).getText();
			if (e.getStateChange() == ItemEvent.SELECTED) {
				wwd.addRadioCov(antennaName);
			}
			else {wwd.removeRadioCov(antennaName);}
		}
	}			
}	

