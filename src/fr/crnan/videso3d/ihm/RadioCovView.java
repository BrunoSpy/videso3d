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
 * *
 * @author mickaël PAPAIL
 * Interface de sélection des couvertures radios.
 */

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.tree.DefaultMutableTreeNode;

import fr.crnan.videso3d.graphics.RadioCovPolygon;
import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.VidesoGLCanvas;

import gov.nasa.worldwind.layers.AirspaceLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.render.airspaces.Airspace;

public class RadioCovView extends JPanel{
	
	private Boolean DEBUG = false;	
	private JPanel jPanel1= new JPanel();
	private JCheckBox jCheckBox1 = new JCheckBox();
	private JTabbedPane tabbedPane1 = new JTabbedPane(); 	
	
	private ItemAntennaListener itemAntennaListener = new ItemAntennaListener();	
	//tabbedPane1.setBorder(BorderFactory.createTitledBorder("couvertures radio"));
	
	private VidesoGLCanvas wwd;
	private Layer layer;
	private LayerList layers;
	private AirspaceLayer radioCovAirspaces=(AirspaceLayer)layer;
		
	
	public RadioCovView(VidesoGLCanvas wwd) {
		this.wwd = wwd;	
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));	
		layers = wwd.getModel().getLayers();
	
		try {
			if(DatabaseManager.getCurrentRadioCov() != null) { 
				initRadioCovAirspaces();
				initPanel();
			}
		}			
		catch (SQLException e) {
			e.printStackTrace();
		}	
	}			
		
	/***
	 * Recherche du layer contenant les couvertures radios à partir de la Layer List
	 */
	public void initRadioCovAirspaces() {
		for (Layer layer : layers) {
			if (layer instanceof AirspaceLayer && layer.getName()=="Radio Coverage") {		
				radioCovAirspaces =(AirspaceLayer)layer;				
				for (Airspace airspace : radioCovAirspaces.getAirspaces()) {
					if ((airspace  instanceof RadioCovPolygon)) {						
						//radioCov=(RadioCovPolygon)airspace;													
						//tabRadioCov=radioCov.getCurtains().toArray();		
					}			
				}
			}
		}
	}
		
	public void initPanel() {
		JScrollPane scrollPane = new JScrollPane(jPanel1);
		for (Airspace airspace : radioCovAirspaces.getAirspaces()) {
			if ((airspace  instanceof RadioCovPolygon)) {
				JCheckBox check = new JCheckBox(((RadioCovPolygon) airspace).getName());
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


