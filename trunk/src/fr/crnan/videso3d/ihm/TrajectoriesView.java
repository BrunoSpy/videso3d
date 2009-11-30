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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;

import org.jdesktop.swingx.JXTable;

import fr.crnan.videso3d.VidesoGLCanvas;
import fr.crnan.videso3d.formats.geo.GEOTrack;
import fr.crnan.videso3d.formats.opas.OPASTrack;
import fr.crnan.videso3d.layers.TrajectoriesLayer;
import gov.nasa.worldwind.avlist.AVKey;
/**
 * Panel de sélection des trajectoires affichées
 * @author Bruno Spyckerelle
 * @version 0.1
 */
@SuppressWarnings("serial")
public class TrajectoriesView extends JPanel {

	private TrajectoriesLayer layer;

	public TrajectoriesView(final VidesoGLCanvas wwd, File file){
		this.layer = wwd.addTrajectoires(file);

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		JPanel filtres = new JPanel();
		filtres.setLayout(new BoxLayout(filtres, BoxLayout.Y_AXIS));
		filtres.setBorder(BorderFactory.createTitledBorder("Filtres"));

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

		JXTable pistes = new JXTable(new TrackTableModel());
		JScrollPane scrollPane = new JScrollPane(pistes);
		scrollPane.setBorder(null);
		table.add(scrollPane);
		this.add(table);

		this.add(Box.createVerticalGlue());

	}

	private class TrackTableModel extends AbstractTableModel {

		String[] columnNames = {"Indicatif", "Départ", "Arrivée", "IAF", "Type"};

		Object[] tracks;

		public TrackTableModel(){
			super();
			tracks = layer.getSelectedTracks();
			layer.addPropertyChangeListener(AVKey.LAYER, new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					tracks = layer.getSelectedTracks();
					fireTableDataChanged();
				}
			});
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
			if(tracks[row] instanceof GEOTrack){
				switch (col) {
				case 0:
					return ((GEOTrack)tracks[row]).getIndicatif();
				case 1:
					return ((GEOTrack)tracks[row]).getDepart();
				case 2:
					return ((GEOTrack)tracks[row]).getArrivee();
				case 3:
					return "";
				case 4:
					return ((GEOTrack)tracks[row]).getType();
				default:
					return "";
				}
			} else if(tracks[row] instanceof OPASTrack){
				switch (col) {
				case 0:
					return ((OPASTrack)tracks[row]).getIndicatif();
				case 1:
					return ((OPASTrack)tracks[row]).getDepart();
				case 2:
					return ((OPASTrack)tracks[row]).getArrivee();
				case 3:
					return ((OPASTrack)tracks[row]).getIaf();
				case 4:
					return "";
				default:
					return "";
				}
			} else {
				return "";
			}
		}

	}
}
