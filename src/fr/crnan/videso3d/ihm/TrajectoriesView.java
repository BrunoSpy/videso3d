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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;

import fr.crnan.videso3d.Triplet;
import fr.crnan.videso3d.VidesoGLCanvas;
import fr.crnan.videso3d.formats.TrackFilesReader;
import fr.crnan.videso3d.formats.geo.GEOTrack;
import fr.crnan.videso3d.formats.lpln.LPLNTrack;
import fr.crnan.videso3d.formats.opas.OPASTrack;
import fr.crnan.videso3d.layers.GEOTracksLayer;
import fr.crnan.videso3d.layers.LPLNTracksLayer;
import fr.crnan.videso3d.layers.OPASTracksLayer;
import fr.crnan.videso3d.layers.TrajectoriesLayer;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.tracks.Track;

/**
 * Panel de sélection des trajectoires affichées
 * @author Bruno Spyckerelle
 * @version 0.4
 */
@SuppressWarnings("serial")
public class TrajectoriesView extends JPanel {

	private JXTaskPaneContainer content = new JXTaskPaneContainer();
	
	private List<Triplet<String, String, Color>> colorFilters;
	
	private TrajectoriesLayer layer;

	private VidesoGLCanvas wwd;
	
	public TrajectoriesView(final VidesoGLCanvas wwd, TrackFilesReader reader){
		this.layer = wwd.addTrajectoires(reader);
		this.wwd = wwd;
		
		this.setLayout(new BorderLayout());
		JScrollPane scrollContent = new JScrollPane(content);
		scrollContent.setBorder(null);
		this.add(scrollContent, BorderLayout.CENTER);		
		
		JXTaskPane table = new JXTaskPane("Trajectoires affichées");
		final JXTable pistes = new JXTable(new TrackTableModel());
		//listener pour le highlight des lignes sélectionnées
		pistes.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if(!e.getValueIsAdjusting()){
					if(e.getFirstIndex() != -1){
						for(int i = e.getFirstIndex(); i <= e.getLastIndex(); i++){
							layer.highlightTrack(
									(Track)((TrackTableModel)pistes.getModel()).getTrackAt(pistes.convertRowIndexToModel(i)),
									pistes.isRowSelected(i));
						}
					}
				}
			}
		});
		pistes.setColumnControlVisible(true);			
		

		if(layer instanceof LPLNTracksLayer){
			pistes.getColumnExt("IAF").setVisible(false);
		} else if (layer instanceof GEOTracksLayer) {
			pistes.getColumnExt("IAF").setVisible(false);
		} else if (layer instanceof OPASTracksLayer) {
			pistes.getColumnExt("Type").setVisible(false);
		}
		pistes.getColumnExt("Affiché").setVisible(layer.isTrackHideable());
		pistes.packAll();
		
		JScrollPane scrollPane = new JScrollPane(pistes);
		scrollPane.setBorder(null);
		table.add(scrollPane);
				
		content.add(this.createStylePane(), null);
		content.add(this.createFilterPane(), null);
		if(layer.isTrackColorFiltrable()) content.add(this.createColorFilterPane(), null);
		content.add(table, null);

	}

	private JXTaskPane createStylePane(){
		JXTaskPane stylePane = new JXTaskPane("Style des trajectoires");
		stylePane.setCollapsed(true);
		
		stylePane.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		
		stylePane.add(new JLabel("Style du tracé"), c);
		
		c.gridx = 1;
		final JComboBox styles = new JComboBox();
		for(Integer style : layer.getStylesAvailable()){
			switch(style) {
			case TrajectoriesLayer.STYLE_CURTAIN:
				styles.addItem("Rideau");
				break;
			case TrajectoriesLayer.STYLE_PROFIL:
				styles.addItem("Profil avec balises");
				break;
			case TrajectoriesLayer.STYLE_SHADED:
				styles.addItem("Fil de fer dégradé");
				break;
			case TrajectoriesLayer.STYLE_SIMPLE:
				styles.addItem("Fil de fer");
				break;
			}
		}

		switch(layer.getStyle()) {
		case TrajectoriesLayer.STYLE_CURTAIN:
			styles.setSelectedItem("Rideau");
			break;
		case TrajectoriesLayer.STYLE_PROFIL:
			styles.setSelectedItem("Profil avec balises");
			break;
		case TrajectoriesLayer.STYLE_SHADED:
			styles.setSelectedItem("Fil de fer dégradé");
			break;
		case TrajectoriesLayer.STYLE_SIMPLE:
			styles.setSelectedItem("Fil de fer");
			break;
		}
		
		stylePane.add(styles, c);
		
		c.gridx = 0;
		c.gridy = 1;
		
		stylePane.add(new JLabel("Couleur interne"), c);
		
		c.gridx = 1;
		
		final JButton changeColor1 = new JButton(new ImageIcon(getClass().getResource("/resources/fill-color.png"))); 
		changeColor1.setBackground(layer.getDefaultInsideColor());
		changeColor1.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				changeColor1.setBackground(JColorChooser.showDialog(null, "Couleur", changeColor1.getBackground()));
			}
		});
		stylePane.add(changeColor1, c);
		
		c.gridx = 0;
		c.gridy = 2;
		
		stylePane.add(new JLabel("Couleur externe"), c);
		
		c.gridx = 1;
		
		final JButton changeColor2 = new JButton(new ImageIcon(getClass().getResource("/resources/fill-color.png"))); 
		changeColor2.setBackground(layer.getDefaultOutsideColor());
		changeColor2.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				changeColor2.setBackground(JColorChooser.showDialog(null, "Couleur", changeColor2.getBackground()));
			}
		});
		stylePane.add(changeColor2, c);
		
		c.gridx = 0;
		c.gridy = 3;
		
		stylePane.add(new JLabel("Opacité"), c);
		
		c.gridx = 1;
		
		final JTextField opacity = new JTextField(15);
		opacity.setToolTipText("Valeur comprise entre 0 (transparent) et 100.");
		opacity.setText(String.valueOf(layer.getDefaultOpacity()*100));
		
		stylePane.add(opacity, c);
		
		c.gridx = 0;
		c.gridy = 4;
		
		stylePane.add(new JLabel("Largeur du tracé"), c);
		
		c.gridx = 1;
		
		final JTextField width = new JTextField(15);
		width.setToolTipText("Valeur comprise entre 0 (transparent) et 100.");
		width.setText(String.valueOf(layer.getDefaultWidth()));
		
		stylePane.add(width, c);
		
		c.gridx = 1;
		c.gridy = 5;
		
		JButton validate = new JButton("Valider");
		
		validate.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				String itemSelected = (String) styles.getSelectedItem();
				if(itemSelected.equals("Rideau")){
					layer.setStyle(TrajectoriesLayer.STYLE_CURTAIN);
				} else if(itemSelected.equals("Fil de fer")){
					layer.setStyle(TrajectoriesLayer.STYLE_SIMPLE);
				} else if(itemSelected.equals("Fil de fer dégradé")){
					layer.setStyle(TrajectoriesLayer.STYLE_SHADED);
				} else if(itemSelected.equals("Profil")){
					layer.setStyle(TrajectoriesLayer.STYLE_PROFIL);
				}
				
				layer.setDefaultOutsideColor(changeColor2.getBackground());
				layer.setDefaultInsideColor(changeColor1.getBackground());
				layer.setDefaultWidth(Double.parseDouble(width.getText()));
				layer.setDefaultOpacity(Double.parseDouble(opacity.getText())/100.0);
				
				layer.update();
			}
		});
		
		stylePane.add(validate, c);	

		
		return stylePane;
	}
	
	private JXTaskPane createColorFilterPane(){
		JXTaskPane colorFilterPane = new JXTaskPane("Filtres de couleurs");
		colorFilterPane.setCollapsed(true);
		
		//Gestion des couleurs
		final TrajectoriesColorsDialog colors = new TrajectoriesColorsDialog(colorFilters);
		colors.addPropertyChangeListener("valuesChanged", new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent p) {
				layer.resetFilterColor();
				if(p.getNewValue() != null) {
					for(Triplet<String, String, Color> filter : (List<Triplet<String, String, Color>>)p.getNewValue()){
						layer.addFilterColor(TrajectoriesLayer.string2type(filter.getFirst()), filter.getSecond(), filter.getThird());
					}
				}
			}
		});
		colorFilterPane.add(colors);
		
		return colorFilterPane;
	}
	
	private JXTaskPane createFilterPane(){
		JXTaskPane filterPane = new JXTaskPane("Filtres");
		filterPane.setCollapsed(true);
		
		filterPane.add(this.createTitleSwitch());
		
		JPanel filtres = new JPanel();
		filtres.setLayout(new BoxLayout(filtres, BoxLayout.Y_AXIS));		

		JPanel indicatif = new JPanel();
		indicatif.setLayout(new BoxLayout(indicatif, BoxLayout.X_AXIS));
		JLabel indicLabel = new JLabel("Indicatif : ");
		final JTextField indicField = new JTextField(10);
		indicField.setMaximumSize(new Dimension(100, 30));
		indicatif.add(indicLabel);
		indicatif.add(Box.createHorizontalGlue());
		indicatif.add(indicField);

		filtres.add(indicatif);
		
		JPanel aDep = new JPanel();
		aDep.setLayout(new BoxLayout(aDep, BoxLayout.X_AXIS));
		JLabel aDepLabel = new JLabel("Aéroport départ : ");
		final JTextField aDepField = new JTextField(10);
		aDepField.setMaximumSize(new Dimension(100, 30));
		aDep.add(aDepLabel);
		aDep.add(Box.createHorizontalGlue());
		aDep.add(aDepField);

		filtres.add(aDep);

		JPanel aDest = new JPanel();
		aDest.setLayout(new BoxLayout(aDest, BoxLayout.X_AXIS));
		JLabel aDestLabel = new JLabel("Aéroport arrivée : ");
		final JTextField aDestField = new JTextField(10);
		aDestField.setMaximumSize(new Dimension(100, 30));
		aDest.add(aDestLabel);
		aDest.add(Box.createHorizontalGlue());
		aDest.add(aDestField);

		filtres.add(aDest);

		JPanel iaf = new JPanel();
		iaf.setLayout(new BoxLayout(iaf, BoxLayout.X_AXIS));
		JLabel iafLabel = new JLabel("IAF : ");
		final JTextField iafField = new JTextField(10);
		iafField.setMaximumSize(new Dimension(100, 30));
		iaf.add(iafLabel);
		iaf.add(Box.createHorizontalGlue());
		iaf.add(iafField);

		filtres.add(iaf);

		JPanel type = new JPanel();
		type.setLayout(new BoxLayout(type, BoxLayout.X_AXIS));
		JLabel typeLabel = new JLabel("Type avion : ");
		final JTextField typeField = new JTextField(10);
		typeField.setMaximumSize(new Dimension(100, 30));
		type.add(typeLabel);
		type.add(Box.createHorizontalGlue());
		type.add(typeField);

		filtres.add(type);
		
		JPanel validate = new JPanel();
		validate.setLayout(new BoxLayout(validate, BoxLayout.X_AXIS));
		JButton val = new JButton("Filtrer");
		validate.add(val);
		val.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				layer.removeFilter();
				if(!indicField.getText().isEmpty()){
					layer.addFilter(TrajectoriesLayer.FIELD_INDICATIF, indicField.getText());
				}
				if(!aDepField.getText().isEmpty()){
					layer.addFilter(TrajectoriesLayer.FIELD_ADEP, aDepField.getText());
				}
				if(!aDestField.getText().isEmpty()){
					layer.addFilter(TrajectoriesLayer.FIELD_ADEST, aDestField.getText());
				}
				if(!iafField.getText().isEmpty()){
					layer.addFilter(TrajectoriesLayer.FIELD_IAF, iafField.getText());
				}
				if(!typeField.getText().isEmpty()){
					layer.addFilter(TrajectoriesLayer.FIELD_TYPE_AVION, typeField.getText());
				}
				layer.update();
			}
		});
		JButton cancel = new JButton("Effacer");
		validate.add(cancel);
		cancel.addActionListener(new ActionListener() {	
			@Override
			public void actionPerformed(ActionEvent e) {
				indicField.setText("");
				aDepField.setText("");
				aDestField.setText("");
				iafField.setText("");
				typeField.setText("");
				layer.removeFilter();
			}
		});
		filtres.add(validate);
		filterPane.add(filtres);		
		return filterPane;
	}
	
	/**
	 * Crée la zone de titre avec un switch et/ou
	 * @return JPanel
	 */
	private JPanel createTitleSwitch(){
		JPanel titre = new JPanel();
		titre.setLayout(new BoxLayout(titre, BoxLayout.X_AXIS));
		titre.setBorder(BorderFactory.createEmptyBorder(0, 17, 1, 3));
		
		JRadioButton et = new JRadioButton("Et");
		et.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				layer.setFilterDisjunctive(!(e.getStateChange() == ItemEvent.SELECTED));
			}
		});
		JRadioButton ou = new JRadioButton("Ou");
		ou.setSelected(true);
		ButtonGroup group = new ButtonGroup();
		group.add(et);
		group.add(ou);
		
		JPanel groupPanel = new JPanel();
		groupPanel.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));
		groupPanel.setLayout(new BoxLayout(groupPanel, BoxLayout.X_AXIS));
		groupPanel.add(Box.createHorizontalGlue());
		groupPanel.add(et);
		groupPanel.add(ou);
		titre.add(groupPanel);
		
		return titre;
	}
	
	/**
	 * Supprime le layer associé au sélecteur.
	 */
	public void delete(){
		this.wwd.getModel().getLayers().remove(layer);
	}
	
	private class TrackTableModel extends AbstractTableModel {

		String[] columnNames = {"Indicatif", "Départ", "Arrivée", "IAF", "Type", "Affiché"};

		Object[] tracks = null;

		Collection<? extends Track> tracksCollection;
		
		public TrackTableModel(){
			super();
			tracks = layer.getSelectedTracks().toArray();//TODO gérer les mauvais fichiers
			tracksCollection = layer.getSelectedTracks();
			layer.addPropertyChangeListener(AVKey.LAYER, new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					if(!tracksCollection.equals(layer.getSelectedTracks())){
						tracks = layer.getSelectedTracks().toArray();
						tracksCollection = layer.getSelectedTracks();
						fireTableDataChanged();
					}
				}
			});
		}
		
		public Object getTrackAt(int row){
			return tracks[row];
		}
		
		@Override
		public String getColumnName(int col) {
	        return columnNames[col];
	    }

		
		@Override
		public int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public int getRowCount() {
			return tracks.length;
		}

		@Override
		public Object getValueAt(int row, int col) {
			Track t = (Track) tracks[row];
			if(t instanceof GEOTrack){
				switch (col) {
				case 0:
					return ((GEOTrack)t).getIndicatif();
				case 1:
					return ((GEOTrack)t).getDepart();
				case 2:
					return ((GEOTrack)t).getArrivee();
				case 3:
					return "";
				case 4:
					return ((GEOTrack)t).getType();
				case 5:
					return layer.isVisible((Track)t);
				default:
					return "";
				}
			} else if(t instanceof OPASTrack){
				switch (col) {
				case 0:
					return ((OPASTrack)t).getIndicatif();
				case 1:
					return ((OPASTrack)t).getDepart();
				case 2:
					return ((OPASTrack)t).getArrivee();
				case 3:
					return ((OPASTrack)t).getIaf();
				case 4:
					return "";
				case 5:
					return layer.isVisible((Track)t);
				default:
					return "";
				}
			} else if(t instanceof LPLNTrack) {
				switch (col) {
				case 0:
					return ((LPLNTrack)t).getIndicatif();
				case 1:
					return ((LPLNTrack)t).getDepart();
				case 2:
					return ((LPLNTrack)t).getArrivee();
				case 3:
					return "";
				case 4:
					return ((LPLNTrack)t).getType();
				case 5:
					return layer.isVisible((Track)t);
				default:
					return "";
				}
			} else {
				return "";
			}
		}

		
		/* (non-Javadoc)
		 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
		 */
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			if(columnIndex == 5){
				return Boolean.class;
			} else {
				return String.class;
			}
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
		 */
		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			if(columnIndex == 5){
				return true;
			} else {
				return false;
			}
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
		 */
		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			if(columnIndex == 5){
				layer.setVisible((Boolean)aValue, (Track)tracks[rowIndex]);
				fireTableDataChanged();
			}
		}

		
		
	}
}
