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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import fr.crnan.videso3d.databases.DatabaseManager;
import fr.crnan.videso3d.databases.DatabaseManager.Type;
import fr.crnan.videso3d.databases.radio.RadioCovController;
import fr.crnan.videso3d.graphics.RadioCovPolygon;
import fr.crnan.videso3d.ihm.components.DataView;
import fr.crnan.videso3d.ihm.components.VBiSlider;
import fr.crnan.videso3d.ihm.components.VBiSlider.*;
import fr.crnan.videso3d.layers.FilterableAirspaceLayer;
import fr.crnan.videso3d.DatasManager;

import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.render.airspaces.Airspace;
import gov.nasa.worldwind.render.airspaces.Curtain;

import com.visutools.nav.bislider.BiSliderAdapter;
import com.visutools.nav.bislider.BiSliderEvent;
import com.visutools.nav.bislider.BiSliderListener;

/**  
 * @author Mickael Papail
 * @author Bruno Spyckerelle
 * @version 0.2.2
 * Interface de selection des couvertures radios.
 */
public class RadioCovView extends JPanel implements DataView {
	
	private JPanel jPanel1= new JPanel();
	public boolean DEBUG = false;
	
	private Box vBox = Box.createVerticalBox();	// vBox = vertical Box
	private List<JCheckBox> checkboxes = new LinkedList<JCheckBox>();	
	private VBiSlider biSlider;
	private List<VBiSlider> biSliders = new LinkedList<VBiSlider>();	
	private ItemAntennaListener itemAntennaListener = new ItemAntennaListener();	
	private ItemBiSliderListener itemBiSliderListener = new ItemBiSliderListener();
	private FilterableAirspaceLayer radioCovAirspaces;
	private Object[] tabRadioCov;	
	private Airspace currentAirspace;
	
	public RadioCovView() {							
		try {			
			if(DatabaseManager.getCurrentRadioCov() != null) { 									
				initGUI();					
			}
		}
		catch (SQLException e) {	
			e.printStackTrace();
		}
	}	
													
	public void initGUI() {
		//int height = 0;
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));					
		vBox.add(Box.createVerticalStrut(5));
		vBox.setPreferredSize(new Dimension(250,150));		
		vBox.setBorder(BorderFactory.createTitledBorder("Couvertures radios"));
		jPanel1.add(vBox);
		this.add(jPanel1);
		this.setVisible(true);					
	}
		
	/***
	 * Recherche du layer contenant les couvertures radio à  partir de la Layer List.
	 */
	public boolean initRadioCovAirspaces() {
		for (Layer layer : getController().getLayers()) {				
			if (layer instanceof FilterableAirspaceLayer && layer.getName()=="Radio Coverage") {						
				radioCovAirspaces = (FilterableAirspaceLayer)layer;								
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
		int height = 0;
		/*
	   GridBagLayout jPanel2Layout = new GridBagLayout();
		jPanel2Layout.columnWidths = new int[] {7};
		jPanel2Layout.rowHeights = new int[] {37, 7};
		jPanel2Layout.columnWeights = new double[] {0.1};
		jPanel2Layout.rowWeights = new double[] {0.0, 0.1};
		getRootPane().add(jPanel2, new GridBagConstraints(2, 2, 1, 2, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		jPanel2.setLayout(jPanel2Layout);
		jPanel2.setBorder(BorderFactory.createTitledBorder("N1"));
		jPanel2.setPreferredSize(new java.awt.Dimension(100, 255));
		*/
		for (Airspace airspace : radioCovAirspaces.getAirspaces()) {
			Box hBox = Box.createHorizontalBox();
			
			if ((airspace  instanceof RadioCovPolygon)) {
				currentAirspace = airspace;																		
				tabRadioCov = ((RadioCovPolygon) currentAirspace).getCurtains().toArray();	
				
				JCheckBox check = new JCheckBox(((RadioCovPolygon) airspace).getName());
				// jPanel2.add(check,new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0, 0) );
		
				final VBiSlider biSlider = new VBiSlider(VBiSlider.HSB);
				biSlider.addBiSliderListener(itemBiSliderListener);
			    //jPanel2.add(biSlider, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0, 0));				
				this.biSlider = biSlider;
				biSlider.setName(((RadioCovPolygon) airspace).getName());
				
				/*
				biSlider.setMinimumColor(Color.BLUE);				
				biSlider.setMaximumColor(Color.YELLOW);
				*/
				biSlider.setOpaque(true);
				biSlider.setMinimumSize(new java.awt.Dimension(120,30));				
				
				// biSlider.setVisible(false);				
				biSlider.setMaximumSize(new java.awt.Dimension(120,30));
				biSlider.setPreferredSize(new java.awt.Dimension(120, 30));
				biSlider.setSize(120, 30);
/*				
				biSlider.setMinimumValue(((Curtain)tabRadioCov[0]).getAltitudes()[0]);								
				biSlider.setMaximumValue(((Curtain)tabRadioCov[maxIndex]).getAltitudes()[1]);						
				biSlider.setMinimumColoredValue(((Curtain)tabRadioCov[0]).getAltitudes()[0]);
				biSlider.setMaximumColoredValue(((Curtain)tabRadioCov[maxIndex]).getAltitudes()[1]);
*/				
				biSlider.setMinimumValue(0);								
				biSlider.setMaximumValue(19500);						
				biSlider.setMinimumColoredValue(0);
				biSlider.setMaximumColoredValue(19500);				
				biSlider.setSegmentSize(((int)((biSlider.getMaximumValue()-biSlider.getMinimumValue())/6)));				
				biSlider.setAutoscrolls(false);
				//biSlider.setEnabled(false); // inexploitable par défaut
				//biSlider.setFocusable(false);
				//biSlider.setOpaque(true);
				biSlider.setUnhighlighted();
			
			//	biSlider.setVisible(false);
			/*
				biSlider.addBiSliderListener(new BiSliderAdapter() {
					public void newMaxValue(BiSliderEvent e) {}
					public void newMinValue(BiSliderEvent e) {}
					public void newColors(BiSliderEvent e) {}
					public void newSegments(BiSliderEvent e) {}
					public void newValues(BiSliderEvent e) {}
				});
			*/
				check.setAlignmentX(LEFT_ALIGNMENT);
				hBox.add(check);
				checkboxes.add(check);
				hBox.add(Box.createVerticalStrut(0)); // utile car le VerticalStrut de la Box fille (qui est horizontale) étend la largeur de ma Box fille au maximum (de manière à ce que ses côté gauche et droit soient respectivements confondus avec les côtés gauche et droit de la Box mère
				hBox.add(Box.createHorizontalStrut(2));	
				
				biSlider.setAlignmentX(RIGHT_ALIGNMENT);
				hBox.add(biSlider);
				biSliders.add(biSlider);
			
				vBox.add(hBox);
				// vBox.add(Box.createVerticalStrut(5));
				
				check.addItemListener(itemAntennaListener);
				height +=45;
			}
		vBox.setPreferredSize(new Dimension(250,height));
		}
	}
	
	
	/*
	 * Listener du biSlider
	 * */
	public class ItemBiSliderListener implements BiSliderListener {

		@Override
		public void newColors(BiSliderEvent BiSliderEvent_Arg) {
			// TODO Auto-generated method stub		
		}

		@Override
		public void newMaxValue(BiSliderEvent BiSliderEvent_Arg) {
			if (DEBUG) System.out.println("Appel newMaxValue ");	
			// TODO Auto-generated method stub		
		}

		@Override
		public void newMinValue(BiSliderEvent BiSliderEvent_Arg) {
			if (DEBUG) System.out.println("Appel newMinValue ");	
			// TODO Auto-generated method stub		7
		}

		@Override
		public void newSegments(BiSliderEvent BiSliderEvent_Arg) {
			if (DEBUG) System.out.println("Appel newSegments ");	
		// TODO Auto-generated method stub		
		}

		@Override
		public void newValues(BiSliderEvent BiSliderEvent_Arg) {
			if (DEBUG) System.out.println("Appel newValues ");
			if (DEBUG) System.out.println("Dans radioCovView : valeur du maximumValue" + biSlider.getMaximumColoredValue());
			if (DEBUG) System.out.println("Dans radioCovView : valeur du minimumValue" + biSlider.getMinimumColoredValue());

			// biSlider.compute((Object)frequency.getVolumes()[0]);			
			// RadioCovView.this.biSlider.setVisible(false);
			
			String antennaName= ((VBiSlider)BiSliderEvent_Arg.getSource()).getName();
			for(VBiSlider b : biSliders){
				if(b.getName()==antennaName){
					for (Airspace airspace : radioCovAirspaces.getAirspaces()) {
						if ((airspace  instanceof RadioCovPolygon)) {			
							if (((RadioCovPolygon)airspace).getName()==antennaName) {					
							if (DEBUG)	System.out.println("Le nom du polygone actuel est "+((RadioCovPolygon)airspace).getName());
								b.compute((Object)airspace,b.getMinimumColoredValue(),b.getMaximumColoredValue());														
								getController().redrawNow();
							}
						}
					}					
				}					
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
	}		
	
	/**
	 * Listener de selection des couvertures.
	 */		
	public class ItemAntennaListener implements ItemListener {
		public void itemStateChanged(ItemEvent e) {
			String antennaName= ((JCheckBox)e.getSource()).getText();
			if (e.getStateChange() == ItemEvent.SELECTED) {
				// biSlider visible
				for(VBiSlider b : biSliders){
					if ( b.getName()==antennaName) {b.setHighlighted();}
				}
				getController().showObject(RadioCovController.ANTENNE, antennaName);				
			}
			else {
				// biSlider non visible
				getController().hideObject(RadioCovController.ANTENNE, antennaName);
				for(VBiSlider b : biSliders){
					if ( b.getName()==antennaName) {b.setUnhighlighted();}
				}
			}
		}
	}
	
	@Override
	public RadioCovController getController() {
		return (RadioCovController) DatasManager.getController(Type.RadioCov);
	}
	
	@Override
	public void showObject(int type, String name) {
		for(JCheckBox c : checkboxes){
			if(c.getText()==name){
				c.setSelected(true);
				break;
			}
		}
	}
	@Override
	public void hideObject(int type, String name) {
		for(JCheckBox c : checkboxes){
			if(c.getText() == name){
				c.setSelected(false);
				break;
			}
		}
	}				

}	
