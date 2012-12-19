/*
 * This file is part of ViDESO.
 * ViDESO is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ViDESO is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty o
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ViDESO.  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.crnan.videso3d.ihm;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;


import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import java.awt.BorderLayout;
import java.awt.Color;
// import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
//import java.awt.GridBagConstraints;
// import java.awt.GridBagLayout;
// import java.awt.Insets;
// import java.awt.LayoutManager;

import javax.swing.BorderFactory;
import javax.swing.Box;
//import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSlider;
// import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

import fr.crnan.videso3d.databases.radio.FrequenciesInit;
import fr.crnan.videso3d.databases.radio.Frequency;


import fr.crnan.videso3d.graphics.RadioCovPolygon;
import fr.crnan.videso3d.ihm.components.DataView;
import fr.crnan.videso3d.ihm.components.TitleTwoButtons;
import fr.crnan.videso3d.ihm.components.VBiSlider;
// import fr.crnan.videso3d.ihm.components.VBiSlider.*;
import fr.crnan.videso3d.layers.FilterableAirspaceLayer;
import fr.crnan.videso3d.databases.radio.RadioCovController;
import fr.crnan.videso3d.databases.DatabaseManager;
import fr.crnan.videso3d.DatasManager.Type;
import fr.crnan.videso3d.DatasManager;

import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.render.airspaces.Airspace;
import gov.nasa.worldwind.render.airspaces.Curtain;
// import gov.nasa.worldwind.render.airspaces.Curtain;


// Gestion des mouvements de camera
// import gov.nasa.worldwind.animation.*;
// import gov.nasa.worldwind.view.*;
// import com.visutools.nav.bislider.BiSliderAdapter;

import java.util.Hashtable;
import com.visutools.nav.bislider.BiSliderEvent;
import com.visutools.nav.bislider.BiSliderListener;

/**  
 * @author Mickael Papail
 * @author Bruno Spyckerelle
 * @version 0.3
 * Interface de selection des couvertures radios.
 */
public class RadioCovView extends JPanel implements DataView {
	
	private static final float RIGHT_ALIGNEMENT = 0;
	private JPanel topPanel = new JPanel();	
//	private JPanel jPanel1 = new JPanel();
	private Boolean visionMode = false; // 2D:TRUE, 3D:FALSE
	private TitleTwoButtons frequencyTitledPanel = new TitleTwoButtons("Mode", "2D", "3D", visionMode);
	private TitleTwoButtons antennaTitledPanel = new TitleTwoButtons("Mode", "2D", "3D", visionMode);		
	private JPanel frequencyPanel = new JPanel();
	private JPanel antennaPanel = new JPanel();		
	private FrequenciesInit freqList;
	private Frequency frequency; // instance de classe Frequency
//	private int maxPanelHeight; // hauteur maxi du panel					
	public boolean DEBUG = false;	
	private Box vBox = Box.createVerticalBox();	// vBox = vertical Box
	private Box vBox2 = Box.createVerticalBox();
	private Box hBox01 = Box.createVerticalBox();
	private Box[] hBox = new Box[4];	
	private Box hBoxRdoButton = Box.createHorizontalBox();
	private Box vBoxA = Box.createVerticalBox();
	private Box vBoxB = Box.createVerticalBox();
	private List<JCheckBox> checkboxes = new LinkedList<JCheckBox>();	
	private VBiSlider biSlider;	
	private JCheckBox[] jCheckBox = new JCheckBox[4];
	
	private ButtonGroup rdoButtonGroup;
	private JRadioButton button2d; // new JRadioButton("2D", false);
	private JRadioButton button3d; // new JRadioButton("3D", true);
	
	private List<VBiSlider> biSliders = new LinkedList<VBiSlider>();	// Liste de chainée de BiSliders pour gestion de l'interface antennes
	private List<JSlider> sliders = new LinkedList<JSlider>();
	private VBiSlider[] freqBiSlider = new VBiSlider[4]; // tableau de gestion des biSliders de l'interface fréquence [0] = N1, [1]= S1 ; [2] = N2; [3] =S2
	private JSlider[] freqJSlider = new JSlider[4]; // tableau de gestion des jSliders de l'interface fréquence [0] = N1, [1]= S1 ; [2] = N2; [3] =S2
	
	private ItemAntennaListener itemAntennaListener = new ItemAntennaListener();		
	private ItemBiSliderListener itemBiSliderListener = new ItemBiSliderListener();	
	private ItemBiSliderFrequencyListener itemBiSliderFrequencyListener = new ItemBiSliderFrequencyListener();
	private RadioButtonListener titleTwoButtonsListener = new RadioButtonListener();
	private SliderListener sliderListener = new SliderListener();
	
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
		
	/*
	private JPanel createTitleRoutes(){
		
		TitleTwoButtons frequencePanel = new TitleTwoButtons("Fréquences radio", "2D", "3D", true);
		titlePanel.addItemListener(new ItemListener() {			
			@Override
			public void itemStateChanged(ItemEvent e) {
				Boolean state = e.getStateChange() == ItemEvent.SELECTED;
//				getController().toggleLayer(getController().getRoutes2DLayer(), state);
//				getController().toggleLayer(getController().getRoutes3DLayer(), !state);
			}
		});		
		return titlePanel; 
	}
	*/
			
	/**
	 * Création du tabbedPane
	 * */
	public void initGUI() {
				
		//int height = 0;
		/*
		JPanel panel1 = new JPanel();
		JPanel panel2 = new JPanel();
		JTabbedPane onglet = new JTabbedPane();
		this.setLayout((LayoutManager) onglet);		
		onglet.add(panel1);
		onglet.add(panel2);
		onglet.setVisible(true); 
		*/					
		//jPanel2.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));					
		//jPanel1.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		
		// modif de la config du panel de la classe TitleTwoButtons 
		// frequencyPanel.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		// modif de la config du panel de la classe TitleTwoButtons
		// antennaPanel.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		//onglets
		JTabbedPane onglet = new JTabbedPane();
		//onglet.setSize(new java.awt.Dimension(260,300));
		
		topPanel.setLayout(new BorderLayout());
		topPanel.add(onglet);
		this.add(topPanel);						
		this.setVisible(true);
		onglet.add("Fréquences",frequencyPanel);	
		// initialisation de la liste des fréquences
					
		onglet.add("Antennes",antennaPanel);
		topPanel.setVisible(true);
	
		/*Bouton radio Visu 2D/3D des  couvertures*/
		rdoButtonGroup = new ButtonGroup();
		button2d = new JRadioButton("2D",false);
		// button2d.setText("2D");		
		button3d = new JRadioButton("3D",true);
		// button3d.setText("3D");
		//buttonGroup.add(rdoButtonGroup);
		rdoButtonGroup.add(button2d);
		rdoButtonGroup.add(button3d);
		hBoxRdoButton.add(button2d);		
		hBoxRdoButton.add(button3d);
		hBoxRdoButton.setAlignmentX(LEFT_ALIGNMENT);				
	}
		
	/**
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
		
	public boolean initRadioFrequencies() {		
		for (Layer layer : getController().getLayers()) {
			if (layer instanceof FilterableAirspaceLayer && layer.getName()=="Radio Coverage") {
				freqList = new FrequenciesInit((FilterableAirspaceLayer)layer);				
				return true;
			}
		}
		return false;
	}
				
	/**
	 page de sélection de fréquences 
	 */
	public void create3DPage1() {							
						
		frequencyTitledPanel.addItemListener(titleTwoButtonsListener);									

		vBox2.add(Box.createVerticalStrut(5));
		vBox2.setPreferredSize(new Dimension(240,450));
		vBox2.setMinimumSize(new Dimension(240,450));
//		vBox2.setBorder(BorderFactory.createTitledBorder("Fréquences radio"));
					
		/*boite de sélection du numéro de fréquence*/					
		hBox01 = Box.createHorizontalBox();	
		ComboBoxModel comboFreqModel = new DefaultComboBoxModel(freqList.getFreqValues());														
		final JComboBox comboFreq = new JComboBox();					
		for (int i=0;i<freqList.getFreqValues().length;i++) {			
			comboFreq.addItem(freqList.getFreqValues()[i]);
		}
		
//		comboFreq.addItem(freqList.getFreqValues());
		comboFreq.setModel(comboFreqModel);
		AutoCompleteDecorator.decorate(comboFreq);						
					
		comboFreq.setVisible(true);
		hBox01.add(comboFreq);
	 	//comboFreq.setSize(new java.awt.Dimension(120,30));
				
		/*initialisation des 4 biSliders de gestion des fréquences*/
		for (int i =0;i<4;i++) {
			freqBiSlider[i] = new VBiSlider();
			freqJSlider[i] = new JSlider(JSlider.HORIZONTAL);
			hBox[i] = Box.createHorizontalBox();  // box horizontale liée au VBiSlider	
			hBox[i].setPreferredSize(new java.awt.Dimension(240,90));
			hBox[i].setMinimumSize(new java.awt.Dimension(240,90));
			// biSlider1 = new VBiSlider(VBiSlider.HSB,false); Vertical si parametre false
			// init des biSliders
			freqBiSlider[i] = new VBiSlider(VBiSlider.HSB);		
			freqBiSlider[i].setMinimumColor(Color.BLUE);
			freqBiSlider[i].setMaximumColor(Color.YELLOW);
			freqBiSlider[i].setOpaque(true);
			freqBiSlider[i].setMinimumSize(new java.awt.Dimension(120,30));										
			freqBiSlider[i].setMaximumSize(new java.awt.Dimension(120,30));
			freqBiSlider[i].setPreferredSize(new java.awt.Dimension(120, 30));
			freqBiSlider[i].setSize(120, 30);
			freqBiSlider[i].setMinimumValue(0);								
			freqBiSlider[i].setMaximumValue(600);		
			freqBiSlider[i].setMinimumColoredValue(0);
			freqBiSlider[i].setMaximumColoredValue(600);
			freqBiSlider[i].setName("");
			freqBiSlider[i].setUnit(" (FL)");
			freqBiSlider[i].setDecimalFormater(null);
			freqBiSlider[i].setSegmentSize(((int)((freqBiSlider[i].getMaximumValue()-freqBiSlider[i].getMinimumValue())/6)));				
			freqBiSlider[i].setAutoscrolls(false);			
			freqBiSlider[i].setUnhighlighted();		
			freqBiSlider[i].addBiSliderListener(itemBiSliderFrequencyListener);
			
			//init des JSliders
			freqJSlider[i].setSize(new java.awt.Dimension(120,30));
			freqJSlider[i].setMinorTickSpacing(100);
			freqJSlider[i].setMajorTickSpacing(600);
			freqJSlider[i].setPaintTicks(true);
			freqJSlider[i].setPaintLabels(true);
			freqJSlider[i].setValue(0);					
			freqJSlider[i].setVisible(false);
			freqJSlider[i].setMinimum(0);
			freqJSlider[i].setMaximum(600);
			freqJSlider[i].setPreferredSize(new java.awt.Dimension(120,40));
			freqJSlider[i].setMaximumSize(new java.awt.Dimension(120,40));
			freqJSlider[i].setMinimumSize(new java.awt.Dimension(120,40));
			freqJSlider[i].setName("");	
			freqJSlider[i].addChangeListener(sliderListener);
			
			
			jCheckBox[i] = new JCheckBox();		
			jCheckBox[i].setText("");			
			jCheckBox[i].addItemListener(itemAntennaListener);
			jCheckBox[i].setAlignmentX(LEFT_ALIGNMENT);
			jCheckBox[i].setEnabled(false);
									
			hBox[i].add(jCheckBox[i]);		
			hBox[i].add(Box.createVerticalStrut(0)); // utile car le VerticalStrut de la Box fille (qui est horizontale) Ã©tend la largeur de ma Box fille au maximum (de maniÃ¨re Ã  ce que ses cÃ´tÃ© gauche et droit soient respectivements confondus avec les cÃ´tÃ©s gauche et droit de la Box mÃ¨re
			hBox[i].add(Box.createHorizontalStrut(2));	
			
			freqBiSlider[i].setAlignmentX(RIGHT_ALIGNMENT);		
			freqBiSlider[i].setName(jCheckBox[i].getName());	
			freqJSlider[i].setAlignmentX(RIGHT_ALIGNMENT);		
			freqJSlider[i].setName(jCheckBox[i].getName());			
			hBox[i].add(freqBiSlider[i]);			
			hBox[i].add(freqJSlider[i]);
			//vBox2.add(hBox1);			
						
			switch(i) {
			case 0:
				hBox[i].setBorder(BorderFactory.createTitledBorder("Normale 1"));
				jCheckBox[i].setName("N1");				
				vBoxA.add(hBox[i]);				
				break;
			case 1:
				hBox[i].setBorder(BorderFactory.createTitledBorder("Secours 1"));
				jCheckBox[i].setName("S1");
				vBoxA.add(hBox[i]);
				break;
			case 2:
				hBox[i].setBorder(BorderFactory.createTitledBorder("Normale 2"));
				jCheckBox[i].setName("N2");
				vBoxA.add(hBox[i]);
				break;
			case 3:
				hBox[i].setBorder(BorderFactory.createTitledBorder("Secours 2"));
				jCheckBox[i].setName("S2");
				vBoxA.add(hBox[i]);
				break;
			}					
		}					
		comboFreq.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {				
				//System.out.println("sélection dans la combobox");
				reset();
				String string = (String)comboFreq.getSelectedItem();				
				matchFrequency(string);   /* Initialise le texte des checkBox (methode setText, ainsi que celui des biSliders */				
				/*sur la page des fréquences , récupération des noms  */				
				// display.setText((String)comboFreq.getSelectedItem());
			}				
		});	
		
	// vBox2.add(hBoxRdoButton);
	vBox2.add(hBox01);
	vBox2.add(vBoxA);
	vBox2.add(vBoxB);
	vBox2.add(Box.createVerticalStrut(5));
	vBox2.setBorder(BorderFactory.createTitledBorder("Visu Fréquences"));
	
	frequencyPanel.setLayout(new FlowLayout());
	frequencyPanel.setSize(new java.awt.Dimension(240,800));
	frequencyPanel.setPreferredSize(new Dimension(240,800));
	frequencyPanel.setMinimumSize(new Dimension(240,800));
	frequencyPanel.add(frequencyTitledPanel);
	frequencyPanel.add(vBox2);		
	}		
	
	/***
	 * page de sélection des antennes
	 */
	public void create3DPage2() {		
		
		int height = 0;
		vBox.add(Box.createVerticalStrut(4));
		vBox.setPreferredSize(new Dimension(280,150));		
		vBox.setBorder(BorderFactory.createTitledBorder("Visu Antennes"));
		antennaPanel.add(vBox);
				
		for (Airspace airspace : radioCovAirspaces.getAirspaces()) {
			Box hBox = Box.createHorizontalBox();
			
			if ((airspace  instanceof RadioCovPolygon)) {
				currentAirspace = airspace;																		
				tabRadioCov = ((RadioCovPolygon) currentAirspace).getCurtains().toArray();					
				JCheckBox check = new JCheckBox(((RadioCovPolygon) airspace).getName()); //new jCheckBox(String)
				check.setName(((RadioCovPolygon)airspace).getName());
				System.out.println("Nom de la checkBox "+check.getName());		
				
				final VBiSlider biSlider = new VBiSlider(VBiSlider.HSB);
				biSlider.addBiSliderListener(itemBiSliderListener);
																			
				this.biSlider = biSlider;
				biSlider.setName(((RadioCovPolygon) airspace).getName());
										
				biSlider.setOpaque(true);
				biSlider.setMinimumSize(new java.awt.Dimension(120,30));										
				biSlider.setMaximumSize(new java.awt.Dimension(120,30));
				biSlider.setPreferredSize(new java.awt.Dimension(120,30));
				//biSlider.setSize(120, 30);
/*				
				biSlider.setMinimumValue(((Curtain)tabRadioCov[0]).getAltitudes()[0]);								
				biSlider.setMaximumValue(((Curtain)tabRadioCov[maxIndex]).getAltitudes()[1]);						
				biSlider.setMinimumColoredValue(((Curtain)tabRadioCov[0]).getAltitudes()[0]);
				biSlider.setMaximumColoredValue(((Curtain)tabRadioCov[maxIndex]).getAltitudes()[1]);
*/				
				biSlider.setMinimumValue(0);												
				biSlider.setMaximumValue(600);		
				biSlider.setUnit(" (FL)");
				biSlider.setDecimalFormater(null);
				
				biSlider.setMinimumColoredValue(0);
				//biSlider.setMaximumColoredValue(19500);				
				biSlider.setMaximumColoredValue(600);
				biSlider.setSegmentSize(((int)((biSlider.getMaximumValue()-biSlider.getMinimumValue())/6)));				
				biSlider.setAutoscrolls(false);			
				biSlider.setUnhighlighted();
							
				final JSlider slider = new JSlider(JSlider.HORIZONTAL);
				slider.setSize(new java.awt.Dimension(120,30));
				slider.setMinorTickSpacing(((int)((biSlider.getMaximumValue()-biSlider.getMinimumValue())/6)));
				slider.setMajorTickSpacing(600);
				slider.setPaintTicks(true);
				slider.setPaintLabels(true);
				slider.setValue(0);					
				slider.setVisible(false);
				slider.setMinimum(0);
				slider.setMaximum(600);
				slider.setPreferredSize(new java.awt.Dimension(120,40));
				slider.setMaximumSize(new java.awt.Dimension(120,40));
				slider.setMinimumSize(new java.awt.Dimension(120,40));
				slider.setName(((RadioCovPolygon) airspace).getName());	
				slider.addChangeListener(sliderListener);
								
				Hashtable<Integer, JLabel> labels =
	            new Hashtable<Integer, JLabel>();			
				labels.put(0, new JLabel("FL 0"));
				labels.put(600, new JLabel("FL 600"));					
				slider.setLabelTable(labels);	 
				slider.setPaintLabels(true);
								
				
				// alignement des composants
				check.setAlignmentX(LEFT_ALIGNMENT);
				hBox.add(check);
				checkboxes.add(check);
				hBox.add(Box.createVerticalStrut(0)); // utile car le VerticalStrut de la Box fille (qui est horizontale) Ã©tend la largeur de ma Box fille au maximum (de maniÃ¨re Ã  ce que ses cÃ´tÃ© gauche et droit soient respectivements confondus avec les cÃ´tÃ©s gauche et droit de la Box mÃ¨re
				hBox.add(Box.createHorizontalStrut(2));	
				
				biSlider.setAlignmentX(RIGHT_ALIGNMENT);
				hBox.add(biSlider);										
				
				// slider.setAlignmentX(RIGHT_ALIGNEMENT);
				hBox.add(slider);			 			
				
				hBox.setBorder(BorderFactory.createEmptyBorder(2,2,2,2)); 
				hBox.setPreferredSize(new java.awt.Dimension(240,45));
				hBox.setMinimumSize(new java.awt.Dimension(240,45));
				hBox.setMaximumSize(new java.awt.Dimension(240,45));
				biSliders.add(biSlider);
				sliders.add(slider);
			
				vBox.add(hBox);
				// vBox.add(Box.createVerticalStrut(5));
				
				check.addItemListener(itemAntennaListener);
				height +=50;
			}
		vBox.setPreferredSize(new Dimension(250,height));
		antennaPanel.setLayout(new FlowLayout());
		antennaPanel.setSize(new java.awt.Dimension(280,height));
		antennaPanel.setPreferredSize(new Dimension(280,height));
		antennaPanel.setMinimumSize(new Dimension(280,height));
		antennaPanel.add(antennaTitledPanel);
		antennaPanel.add(vBox);						
		}
		antennaTitledPanel.addItemListener(titleTwoButtonsListener);
	}		
		
/**************************************  Listeners ***************************************/
	
	// si visionMode à true : firstButton sélectionné
	//						  secondButton sélectionné
	public class RadioButtonListener implements ItemListener{
		@Override
		public void itemStateChanged(ItemEvent e) {
			visionMode = e.getStateChange() == ItemEvent.SELECTED;
			switch2D3DVision();
			/* mode 2D */
			if (visionMode ==  Boolean.TRUE) {
				// frequencyTitledPanel.setComponentZOrder(comp, index)
				if (frequencyTitledPanel.getFirstButton().isSelected()) {
					antennaTitledPanel.getFirstButton().setSelected(true);
					antennaTitledPanel.getSecondButton().setSelected(false);
				}				
				if (antennaTitledPanel.getFirstButton().isSelected()) {
					frequencyTitledPanel.getFirstButton().setSelected(true);
					frequencyTitledPanel.getSecondButton().setSelected(false);
				}
				
				// gestion du panel antennes : On rend visible les JSliders et on retire les biSliders
				for (VBiSlider b :biSliders) {
					b.setVisible(false);
				}
				for (JSlider s :sliders) {
					s.setVisible(true);
				}
				// Mode 2d : gestion du panel des fréquences : On retire les 4 JSliders et on positionne les biSliders
				for (int i=0;i<4;i++) {															
					//freqBiSlider[i].setName("");
					freqBiSlider[i].setVisible(false);
					freqJSlider[i].setVisible(true);									
				}
								
			// TODO Mouvement de camera vertical (cf FlyToOrbitViewAnimator)
			// TODO Exageration verticale
			}	
			/*Mode 3D*/
			if (visionMode == Boolean.FALSE) {
				if (!frequencyTitledPanel.getFirstButton().isSelected()) {
					antennaTitledPanel.getSecondButton().setSelected(true);
					antennaTitledPanel.getFirstButton().setSelected(false);
				}
				if (!antennaTitledPanel.getFirstButton().isSelected()) {
					frequencyTitledPanel.getSecondButton().setSelected(true);
					frequencyTitledPanel.getFirstButton().setSelected(false);
				}
				//TODO mouvement de camera inclinaison 45°
				// TODO exageration verticale																
			
				// Mode 3d : gestion du panel antennes On retire les JSliders et on positionne les biSliders
				for (VBiSlider b : biSliders) {
					b.setVisible(true);
					// bugfix  Correction de bug : On force la suppression de la la couleur aux biSliders qui ne sont pas sélectionnés ; le passage du 2D ou 3D génère des bigs
					for (JCheckBox c : checkboxes) {
						if (b.getName().equals(c.getName()) && !(c.isSelected() )) { 
							 b.setUnhighlighted();
						}
						if (b.getName().equals(c.getName()) && (c.isSelected() )) { 
							 b.setHighlighted();
						}					
					}
				}
				for (JSlider s :sliders) {
					s.setVisible(false);
				}						
				// Mode 3d : gestion du panel des fréquences : On retire les 4 JSliders et on positionne les biSliders
				for (int i=0;i<4;i++) {															
					//freqBiSlider[i].setName("");
					freqBiSlider[i].setVisible(true);
					freqJSlider[i].setVisible(false);									
				}		
			
			}			
			//if (e.getItem())			
			//getController().toggleLayer(getController().getRoutes2DLayer(), state);
			//mouvement de camera
			//toggleVision(); // dans le controller ?
			//modif exageration verticale.
			//getController().toggleLayer(getController().getRoutes3DLayer(), !state);
		}						
	}
		
//TODO : Factoriser les listeners
	/**
	 * Listener du biSlider des antennes
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
		// TODO Auto-generated method stub		
	}

	@Override
	public void newSegments(BiSliderEvent BiSliderEvent_Arg) {
		if (DEBUG) System.out.println("Appel newSegments ");	
		// TODO Auto-generated method stub		
	}

	@Override
	public void newValues(BiSliderEvent BiSliderEvent_Arg) {
	
//		RadioCovView.this.biSlider.setVisible(false);			
		String antennaName = ((VBiSlider)BiSliderEvent_Arg.getSource()).getName();
		for(VBiSlider b : biSliders){
			if(b.getName().equals(antennaName)){
				for (Airspace airspace : radioCovAirspaces.getAirspaces()) {
					if ((airspace  instanceof RadioCovPolygon)) {			
						if (((RadioCovPolygon)airspace).getName().equals(antennaName)) {					
//							if (DEBUG)	System.out.println("Le nom du polygone actuel est "+((RadioCovPolygon)airspace).getName());
							//32.5 est le rapport 19500/FL600 pour passer des niveaux de vol aux Flight Level
							b.compute((Object)airspace,b.getMinimumColoredValue()*32.5,b.getMaximumColoredValue()*32.5);														
							// on met à jour le biSlider de la page des fréquences
							for (int i=0;i<4;i++) {
								// test sur (!null) pour eviter de lancer une exception si aucune fréquence n'est sélectionnée dans le tableau fréquence et qu'une antenne est sélectionnée
								if (freqBiSlider[i].getName()!=null) {
									if (freqBiSlider[i].getName().equals(antennaName)){
										freqBiSlider[i].setColoredValues(b.getMinimumColoredValue(), b.getMaximumColoredValue());
									}
								}
							} 
							getController().redrawNow();
						}
					}
				}													
			}
		}						
	}								
	}
			
	/**
	 * Listener du biSlider des fréquences=
	 * */
	public class ItemBiSliderFrequencyListener implements BiSliderListener {

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
			// TODO Auto-generated method stub		
		}

		@Override
		public void newSegments(BiSliderEvent BiSliderEvent_Arg) {
			if (DEBUG) System.out.println("Appel newSegments ");	
		// TODO Auto-generated method stub		
		}

		@Override
		public void newValues(BiSliderEvent BiSliderEvent_Arg) {
			
			String antennaName = ((VBiSlider)BiSliderEvent_Arg.getSource()).getName();
			for (int i=0;i<4;i++) {
				if (freqBiSlider[i].getName().equals(antennaName)){
					for (Airspace airspace : radioCovAirspaces.getAirspaces()) {
						if ((airspace  instanceof RadioCovPolygon)) {			
							if (((RadioCovPolygon)airspace).getName().equals(antennaName)) {					
//								if (DEBUG)	System.out.println("Le nom du polygone actuel est "+((RadioCovPolygon)airspace).getName());
								//32.5 est le rapport 19500/FL600 pour passer des niveaux de vol aux Flight Level
								freqBiSlider[i].compute((Object)airspace,freqBiSlider[i].getMinimumColoredValue()*32.5,freqBiSlider[i].getMaximumColoredValue()*32.5);														
								// On met à jour le biSlider correspondant de la page des antennes.
								for(VBiSlider b : biSliders){
									if(b.getName().equals(antennaName)){										
										b.setColoredValues(freqBiSlider[i].getMinimumColoredValue(), freqBiSlider[i].getMaximumColoredValue());
									}
								}																								
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
		getController().hideAllRadioCovLayers();
		getController().redrawNow();
		for (int i=0;i<4;i++) {
			//if (jCheckBox[i].isSelected())
			jCheckBox[i].setSelected(false);
		}
		for(JCheckBox check : checkboxes){
		//	if(check.isSelected()){
			check.setSelected(false);
		//	}
		}
		
	}		
		
	/**
	 * A voir pour une éventuelle factorisation*/
	/**
	 * Titre du panel Routes.<br />
	 * Contient un sélecteur pour choisir la méthode de représentation (2D/3D).
	 * @return JPanel
	 */
	
	private JPanel createTitleRadio(){		
		TitleTwoButtons titlePanel = new TitleTwoButtons("Routes", "2D", "3D", true);
		titlePanel.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				Boolean state = e.getStateChange() == ItemEvent.SELECTED;
				//getController().toggleLayer(getController()./*getRoutes2DLayer(), state);
				//getController().toggleLayer(getController().getRoutes3DLayer(), !state);
			}
		});		
		return titlePanel; 
	}

	/**
	 * Listener checkBox des couvertures pour la page des antennes et des fréquences.
	 */		
	public class ItemAntennaListener implements ItemListener {
		public void itemStateChanged(ItemEvent e) {									
			String antennaName= ((JCheckBox)e.getSource()).getText();			
			if (e.getStateChange() == ItemEvent.SELECTED) {				
				/* VisionMode a false : */
				if (visionMode==false) {
					/*Gestion des biSliders de la page des antennes*/
					// biSlider visible
					for(VBiSlider b : biSliders){ 
						if ( b.getName().equals(antennaName)) {b.setHighlighted();}
						// si la checkBox est sélectionnée dans le tableau des antennes, on la sélectionne aussi dans le tableau des fréquences :
						for (int i=0;i<4;i++) {
							if (jCheckBox[i].getName().equals(antennaName)&&(!jCheckBox[i].isSelected())) {jCheckBox[i].setSelected(true);}
						}
					}	
					for(int i=0;i<4;i++){															
						if ( freqBiSlider[i].getName()!=null && freqBiSlider[i].getName().equals(antennaName)) {freqBiSlider[i].setHighlighted();}					
					}
					// 	Affichage de l'objet sélectionné
					getController().showObject(RadioCovController.ANTENNE, antennaName);	
					// on envoit le visionMode au controleur pour commander l'affichage de la couverture en mode 3D ou 2D.
					getController().setVisionMode(false);
				}
				if (visionMode==true) {
					// on envoit le visionMode au controleur pour commander l'affichage de la couverture en mode 3D ou 2D.
					getController().setVisionMode(true);
					getController().showObject(RadioCovController.ANTENNE, antennaName);	
					// TODO : code a mettre en place ici pour afficher l'objet en 2D ou en 3D !!!
					switch2D3DVision();				
				}
			}
			else {
				if (visionMode == false) {				
					// BiSlider non visible		
					for(VBiSlider b : biSliders){
						if ( b.getName().equals(antennaName)) {b.setUnhighlighted();}
						// si la checkBox est désélectionnée dans le tableau des antennes, on la désélectionne aussi dans le tableau des fréquences :
						for (int i=0;i<4;i++) {
							if (jCheckBox[i].getName().equals(antennaName)&&(jCheckBox[i].isSelected())) {jCheckBox[i].setSelected(false);}
						}
					}				
					// 	Objet désélectionné effacé
					for(int i=0;i<4;i++){					
						if ( freqBiSlider[i].getName()!=null && freqBiSlider[i].getName().equals(antennaName)) {						
							freqBiSlider[i].setUnhighlighted();												
						}
					}
					getController().hideObject(RadioCovController.ANTENNE, antennaName);												
				}
				if (visionMode==true) {
					// on envoit le visionMode au controleur pour commander l'affichage de la couverture en mode 3D ou 2D.
					getController().setVisionMode(true);
					getController().hideObject(RadioCovController.ANTENNE, antennaName);	
				}			
			}				
		}
	}	
	/**
	 * Listener JSlider
	 * */
	public class SliderListener implements ChangeListener {
		@Override
		public void stateChanged(ChangeEvent e) {			
			JSlider currentFreqJSlider,currentAntennaJSlider; // référence du jSlider de fréquence et d'antennes courant. Permet de faire des modifs dessus sans nouvelle recherche
			String antennaName= ((JSlider)e.getSource()).getName();	
			Object[] tabRadioCov;
			RadioCovPolygon radioCov;
			for(JSlider s : sliders){
				if(s.getName().equals(antennaName)){
					currentAntennaJSlider = s;
					for (Airspace airspace : radioCovAirspaces.getAirspaces()) {
						if ((airspace  instanceof RadioCovPolygon)) {			
							if (((RadioCovPolygon)airspace).getName().equals(antennaName)) {	
								radioCov = (RadioCovPolygon)airspace;
								tabRadioCov = radioCov.getCurtains().toArray();
								// traitement : Tout ce qui est en-dessous dedu FL est invisible
								// Tout ce qui est au-dessus du FL est invisible.
								//32.5 est le rapport 19500/FL600 pour passer des niveaux de vol aux Flight Level
								double currentAlt = (s.getValue()*32.5);
								double[] alt = new double[tabRadioCov.length]; 								
									int i =0;
									List<Curtain> list = radioCov.getCurtains();									
									for (Curtain c : list) {
										double[] alti = c.getAltitudes();
										alt[i] = alti [0];
										i++;
									}																
								// Double[] tableau, double val, int deb, int fin
								int indice = altSearch(alt,currentAlt,0,tabRadioCov.length-1);								
								switch(indice) {
								case -1 : 
									radioCov.setVisible(0,tabRadioCov.length, false);
									break;
								case 0 :	
									radioCov.setVisible(0,tabRadioCov.length, false);
									radioCov.setVisible(0,true);									
								default :
									radioCov.setVisible(0,tabRadioCov.length, false);
									radioCov.setVisible(indice,true);																
									break;
								}
								/*
								switch(s.getValue()) {
								case 0:
									radioCov.setVisible(0,tabRadioCov.length-1, false);
									radioCov.setVisible(0,true);
									break;
								case 600:
									radioCov.setVisible(tabRadioCov.length-1,true);
								default :
									radioCov.setVisible(0,tabRadioCov.length-1, false);
									radioCov.setVisible(currentValue,true);	
									break;
								}
								*/
								// on met à jour le biSlider de la page des fréquences
								getController().redrawNow();
							}
						}
					}													
				}
			}				
			for (int i=0;i<4;i++) {
				double currentAlt = 0;
				if(freqJSlider[i].getName().equals(antennaName)){	
				 currentAlt = (freqJSlider[i].getValue()*32.5);
				 currentFreqJSlider = freqJSlider[i];		
				 for (Airspace airspace : radioCovAirspaces.getAirspaces()) {
					 if ((airspace  instanceof RadioCovPolygon)) {			
						 if (((RadioCovPolygon)airspace).getName().equals(antennaName)) {	
							 radioCov = (RadioCovPolygon)airspace;
							 tabRadioCov = radioCov.getCurtains().toArray();						
							 currentAlt = (freqJSlider[i].getValue()*32.5);
							 double[] alt = new double[tabRadioCov.length]; 								
							 int j =0;
							 List<Curtain> list = radioCov.getCurtains();									
							 for (Curtain c : list) {
								 double[] alti = c.getAltitudes();
								 alt[i] = alti [0];
								 j++;
							 }																						
							 int indice = altSearch(alt,currentAlt,0,tabRadioCov.length-1);								
							 switch(indice) {
							 	case -1 : 
							 		radioCov.setVisible(0,tabRadioCov.length, false);
							 		break;
							 	case 0 :	
							 		radioCov.setVisible(0,tabRadioCov.length, false);
							 		radioCov.setVisible(0,true);									
							 	default :
							 		radioCov.setVisible(0,tabRadioCov.length, false);
							 		radioCov.setVisible(indice,true);																
							 		break;
							 }	
						 }
					 }				
				}
				}
			}		
		}	
	}
	
	/**
	 * @param freq
	 * Initialise les checkBox et BiSliders de la page des fréquences après sélection d'une fréquence dans la comboBox.
	 */
	public void matchFrequency(String freq) {	
		for (Frequency f : freqList.getFrequencies()) {			
			if (f.getSectorName() == freq) {
				frequency = f;					
				f.setColors();				
				/*Liste des CheckBox */
				for (int i=0;i<4;i++) {				
					if (f.getVolumes()[i]==null) {
						jCheckBox[i].setEnabled(false); 
						jCheckBox[i].setSelected(false);
						jCheckBox[i].setText("");
						freqBiSlider[i].setName("");
						freqBiSlider[i].setVisible(false);
						freqJSlider[i].setVisible(false);
						freqJSlider[i].setName("");
					}				
					else {					
							// System.out.println("volume "+i+" "+frequency.getVolumes()[i].getName());						
							jCheckBox[i].setEnabled(true); 
						// seules les couvertures normales sont sélectionnées par défaut //  
						if ((i==0) || (i==2)) {
							jCheckBox[i].setSelected(true);		
						}
						if ((i==1) || (i==3)) {
							jCheckBox[i].setSelected(false);
							freqBiSlider[i].setUnhighlighted();
							// Force l'effacement de l'objet affiché (Une couverture secours pouvait etre une couverture normale précédemment.)
						}
// 	TODO				Rechercher le radioCovPolygon qui correspond au name et ensuite utiliser la ligne écrite ci-dessous.				
// 	!!!      			biSlider.setName(((RadioCovPolygon) airspace).getName());
						freqBiSlider[i].setName(frequency.getVolumes()[i].getName()); // Utilisé pour définir un nom au biSlider quand sélection de la fréquence => Permet au itemAntennaListener de retrouver le'objet sur lequel biSlider bosse.
						freqJSlider[i].setName(frequency.getVolumes()[i].getName());
						jCheckBox[i].setText(frequency.getVolumes()[i].getName()); // Lors de la sélection de la fréquence le nom de la checkBox est initialisé
						jCheckBox[i].setName(frequency.getVolumes()[i].getName());
//						freqBiSlider[i].setName(frequency.getVolumes()[i].getName());
//						biSlider1.setMinMaxValue((Object)frequency.getVolumes()[0]);	
/*
						if (visionMode==true) {
							freqBiSlider[i].setVisible(true);
						}
						if (visionMode==false) {
							freqJSlider[i].setVisible(true);
						}
*/						
						if (jCheckBox[i].isSelected()) {							
							// pour mettre en surbrillance les biSliders qui ont leurs checkBox de sélectionnée pour le mode visu 3D
							if (visionMode==false) {freqBiSlider[i].setHighlighted();}
							
							// déclencher l'affichage de la couverture radio de la checkBox sélectionnée
							// frequency.getVolumes[i].getName = antennaName
							
							//getController().showObject(RadioCovController.ANTENNE, frequency.getVolumes()[i].getName());
						}																			
					}
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
				
	
		
	/**
	 * Appel du switch du mode de vision.
	* but est de pouvoir switcher la visu, et surtout qu'elle reste en mémoire lorsqu'on passe du mode 2D au mode 3D.
	 **/
	
	public void switch2D3DVision() {
		// passage au mode 2d
		// on scanne chaque element pour leur donner la forme qu'on souhaite.
		RadioCovPolygon radioCov = null;		
		String antennaName=null;
		double currentAlt = 0;
		double currentMinAlt=0;
		double currentMaxAlt=0;
		if (visionMode== true) {
			//for(JSlider s : sliders){					
			for (JCheckBox check : checkboxes)	{
				if (check.isSelected()) {
//					System.out.println("check sélectionnée");
					antennaName = check.getName();
					System.out.println(antennaName);
					for (Airspace airspace : radioCovAirspaces.getAirspaces()) {
					if ((airspace  instanceof RadioCovPolygon && ((RadioCovPolygon)airspace).getName().equals(antennaName))) {																														
							radioCov = (RadioCovPolygon)airspace;
							for(JSlider s : sliders){if (s.getName().equals(antennaName)) {currentAlt = (s.getValue()*32.5);}}															
							// récupération de la liste des altitudes disponibles dans un objet RadioCov
							double[] alt = new double[tabRadioCov.length]; 								
							alt = radioObjectAltitudes(radioCov);
							int indice = altSearch(alt,currentAlt,0,tabRadioCov.length-1);															
							switch(indice) {
							case -1 : 
								radioCov.setVisible(0,tabRadioCov.length, false);
								break;
							/*
							case 0 :	
								radioCov.setVisible(0,tabRadioCov.length, false);
								radioCov.setVisible(0,true);
								*/									
							default :
								radioCov.setVisible(0,tabRadioCov.length, false);
								radioCov.setVisible(indice,true);																
								break;
							}
						//	for (JCheckBox c : checkboxes) {								
						//		if (radioCov!=null && c.getName()!=null && c.isSelected() && (c.getName().equals(((RadioCovPolygon)airspace).getName()))) {
									// on n'affiche que les couvertures qui étaient sélectionnées dans la liste des checkBox !							
										getController().showObject(RadioCovController.ANTENNE, antennaName);
					//				}
					//			}
							}					
					}	
				}													
			}															
		}
		/*
		for (Airspace airspace : radioCovAirspaces.getAirspaces()) {
			if ((airspace  instanceof RadioCovPolygon)) {			
				if (((RadioCovPolygon)airspace).getName().equals(antennaName)) {	
					radioCov = (RadioCovPolygon)airspace;
		*/
		
		if (visionMode == false) {
			// passage au mode 3d												
			// on scanne de nouveau les éléments pour afficher tous les segments du volume
			System.out.println("On est dans le mode 3d");
			
			for (JCheckBox check : checkboxes)	{
				if (check.isSelected()) {					
					antennaName = check.getName();
//					System.out.println(antennaName);
					for (Airspace airspace : radioCovAirspaces.getAirspaces()) {
					if ((airspace  instanceof RadioCovPolygon && ((RadioCovPolygon)airspace).getName().equals(antennaName))) {																														
							radioCov = (RadioCovPolygon)airspace;
							for(VBiSlider s : biSliders){if (s.getName().equals(antennaName)) {currentMinAlt = (s.getMinimumColoredValue()*32.5);currentMaxAlt = (s.getMaximumColoredValue()*32.5);}}															
							// récupération de la liste des altitudes disponibles dans un objet RadioCov
							double[] alt = new double[tabRadioCov.length]; 								
							alt = radioObjectAltitudes(radioCov);
							int indiceMin = altSearch(alt,currentMinAlt,0,tabRadioCov.length);
							int indiceMax = altSearch(alt,currentMaxAlt,0,tabRadioCov.length);														
//							System.out.println("indice min :"+indiceMin);
//							System.out.println("indice max :"+indiceMax);							
							radioCov.setVisible(0,tabRadioCov.length, false);
							radioCov.setVisible(indiceMin,indiceMax,true);																																				
							getController().showObject(RadioCovController.ANTENNE, antennaName);					
							}					
					}	
				}													
			}						
		}
	}			
	
	/**
	 *  Récupération de la liste des altitudes en memoire dans un objet RadioCov
	 * */
	private double[] radioObjectAltitudes(RadioCovPolygon radioCov) {
		 Object[] tabRadioCov;
		if (radioCov !=null) {
			tabRadioCov = radioCov.getCurtains().toArray();
			double[] alt = new double[tabRadioCov.length]; 								
			int i =0;
			List<Curtain> list = radioCov.getCurtains();									
			for (Curtain c : list) {
				double[] alti = c.getAltitudes();
				alt[i] = alti [0];
			}
			return alt;
		}
		return null;
	}
	
	/**
	 * Recherche de l'indice le plus proche de l'altitude (ou les deux altitudes)  gardée(s) en mémoire dans un JSlider ou un biSlider
	 * */
	public int altSearch(double[] alt, double val, int deb, int fin) {
		System.out.println("valeur de l'altitude de la JSlider "+val);
		int cpt = 0;
		for (int i=deb;i<fin;i++) {			
			if (val >= alt[i]) cpt++; 			
			if (val==0) return 0;
		}		
		return cpt;
	}
	
	
	
	
}	



