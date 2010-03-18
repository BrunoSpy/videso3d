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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import org.jdesktop.swingx.JXTable;

import fr.crnan.videso3d.VidesoGLCanvas;
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
 * @version 0.2
 */
@SuppressWarnings("serial")
public class TrajectoriesView extends JPanel {

	private TrajectoriesLayer layer;

	private VidesoGLCanvas wwd;
	
	public TrajectoriesView(final VidesoGLCanvas wwd, File file){
		this.layer = wwd.addTrajectoires(file);
		this.wwd = wwd;
		
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		this.add(this.createTitleSwitch());
		
		JPanel filtres = new JPanel();
		filtres.setLayout(new BoxLayout(filtres, BoxLayout.Y_AXIS));
		filtres.setBorder(BorderFactory.createTitledBorder(""));
		

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
		JLabel aDepLabel = new JLabel("Aéroport de départ : ");
		final JTextField aDepField = new JTextField(10);
		aDepField.setMaximumSize(new Dimension(100, 30));
		aDep.add(aDepLabel);
		aDep.add(Box.createHorizontalGlue());
		aDep.add(aDepField);

		filtres.add(aDep);

		JPanel aDest = new JPanel();
		aDest.setLayout(new BoxLayout(aDest, BoxLayout.X_AXIS));
		JLabel aDestLabel = new JLabel("Aéroport d'arrivée : ");
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
				aDepField.setText("");
				aDestField.setText("");
				iafField.setText("");
				typeField.setText("");
				layer.removeFilter();
				layer.update();
			}
		});
		filtres.add(validate);
		this.add(filtres);

		JPanel table = new JPanel(new BorderLayout());
		table.setBorder(BorderFactory.createTitledBorder("Pistes affichées"));
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
			pistes.getColumnExt("Affiché").setVisible(false);
		} else if (layer instanceof OPASTracksLayer) {
			pistes.getColumnExt("Type").setVisible(false);
			pistes.getColumnExt("Affiché").setVisible(false);
		}
		JScrollPane scrollPane = new JScrollPane(pistes);
		scrollPane.setBorder(null);
		table.add(scrollPane);
		this.add(table);

		this.add(Box.createVerticalGlue());

	}

	/**
	 * Crée la zone de titre avec un switch et/ou
	 * @return JPanel
	 */
	private JPanel createTitleSwitch(){
		JPanel titre = new JPanel();
		titre.setLayout(new BoxLayout(titre, BoxLayout.X_AXIS));
		titre.setBorder(BorderFactory.createEmptyBorder(0, 17, 1, 3));
		
		JLabel titreLabel = new JLabel("Filtres");
		titreLabel.setFont(titreLabel.getFont().deriveFont(Font.BOLD));
		titre.add(titreLabel);
		
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
