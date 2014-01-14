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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;

import fr.crnan.videso3d.Configuration;
import fr.crnan.videso3d.Triplet;
import fr.crnan.videso3d.VidesoGLCanvas;
import fr.crnan.videso3d.formats.TrackFilesReader;
import fr.crnan.videso3d.formats.VidesoTrack;
import fr.crnan.videso3d.formats.geo.GEOReader;
import fr.crnan.videso3d.formats.geo.GEOTrack;
import fr.crnan.videso3d.formats.geo.GEOWriter;
import fr.crnan.videso3d.formats.lpln.LPLNReader;
import fr.crnan.videso3d.formats.opas.OPASReader;
import fr.crnan.videso3d.formats.plns.PLNSReader;
import fr.crnan.videso3d.ihm.components.VFileChooser;
import fr.crnan.videso3d.ihm.components.VXTable;
import fr.crnan.videso3d.ihm.contextualmenus.TrajectoriesMenu;
import fr.crnan.videso3d.layers.tracks.TrajectoriesLayer;
import fr.crnan.videso3d.trajectography.PolygonsSetFilter;
import fr.crnan.videso3d.trajectography.TrackContext;
import fr.crnan.videso3d.trajectography.TracksModel;
import fr.crnan.videso3d.trajectography.TracksModelListener;
import gov.nasa.worldwind.avlist.AVKey;

/**
 * Panel de sélection des trajectoires affichées
 * @author Bruno Spyckerelle
 * @version 0.5.3
 */
public class TrajectoriesView extends JPanel {

	private JXTaskPaneContainer content = new JXTaskPaneContainer();
	
	private TrajectoriesColorsDialog colorsDialog;
	
	private VidesoGLCanvas wwd;
	
	private ContextPanel context;
	
	private TrackContext trackContext;
	
	private TrajectoriesLayer layer;
	
	//fields for regex filters
	private JTextField aDepField;
	private JTextField indicField;
	private JTextField aDestField;
	private JTextField iafField;
	private JTextField typeField;
	private JTextField modeAField; 
	
	public TrajectoriesView(final VidesoGLCanvas wwd, final TrackFilesReader reader, TrajectoriesLayer l, final ContextPanel contxt){
		this.context = contxt;
		this.layer = l;
		this.wwd = wwd;
		this.trackContext = new TrackContext(reader, null, this.wwd.getModel().getGlobe());

		final JXTaskPane filterPolygonPane = this.createPolygonFilterPane();
		
		
		this.setLayout(new BorderLayout());
		JScrollPane scrollContent = new JScrollPane(content);
		scrollContent.setBorder(null);
		this.add(scrollContent, BorderLayout.CENTER);		
		
		final JXTaskPane table = new JXTaskPane("Trajectoires affichées ("+reader.getModel().getVisibleTracks().size()+")");
		final VXTable pistes = new VXTable(reader.getModel());
		pistes.setFillsViewportHeight(true);
		//listener pour le highlight des lignes sélectionnées
		pistes.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if(!e.getValueIsAdjusting()){
					if(e.getFirstIndex() != -1){
						for(int i = e.getFirstIndex(); i < e.getLastIndex(); i++){
							reader.getModel().setSelected(pistes.isRowSelected(i), 
									(VidesoTrack) ((TracksModel)pistes.getModel()).getTrackAt(pistes.convertRowIndexToModel(i)));
						}
					}
				}
			}
		});
		//synchronize selection with model
		reader.getModel().addTableModelListener(new TracksModelListener() {
			
			@Override
			public void tableChanged(TableModelEvent e) {}
			
			@Override
			public void trackVisibilityChanged(Collection<VidesoTrack> track,
					boolean visible) {}
			
			@Override
			public void trackVisibilityChanged(VidesoTrack track, boolean visible) {}
			
			@Override
			public void trackSelectionChanged(Collection<VidesoTrack> tracks,
					boolean selected) {
				for(VidesoTrack t : tracks){
					trackSelectionChanged(t, selected);
				}
			}
			
			@Override
			public void trackSelectionChanged(VidesoTrack track, boolean selected) {
				int index = pistes.convertRowIndexToView(layer.getModel().getRow(track));
				if(selected){
					pistes.getSelectionModel().addSelectionInterval(index, index);
				} else {
					pistes.getSelectionModel().removeSelectionInterval(index, index);
				}
			}
			
			@Override
			public void trackRemoved(Collection<VidesoTrack> track) {}
			
			@Override
			public void trackRemoved(VidesoTrack track) {}
			
			@Override
			public void trackAdded(Collection<VidesoTrack> track) {}
			
			@Override
			public void trackAdded(VidesoTrack track) {}
			
		});
		
		pistes.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent arg0) {}
			
			@Override
			public void mousePressed(MouseEvent arg0) {}
			
			@Override
			public void mouseExited(MouseEvent arg0) {}
			
			@Override
			public void mouseEntered(MouseEvent arg0) {}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 2){
					int row = pistes.rowAtPoint(e.getPoint());
					final VidesoTrack t = (VidesoTrack)((TracksModel)pistes.getModel()).getTrackAt(pistes.convertRowIndexToModel(row));
					trackContext.updateTrackPane(t);
					context.setTaskPanes(trackContext.getTaskPanes(0, null));
					context.setTitle(t.getName());
					context.open();
					wwd.centerView(t);
				} else if(e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON3 && pistes.getSelectedRows().length > 0){
					final List<VidesoTrack> selectedTracks = new ArrayList<VidesoTrack>();
					for(int row : pistes.getSelectedRows()){
						selectedTracks.add((VidesoTrack)((TracksModel)pistes.getModel()).getTrackAt(pistes.convertRowIndexToModel(row)));
					}
					JPopupMenu menu = new JPopupMenu(selectedTracks + " trajectoires sélectionnées");
					JMenu trajMenu = new TrajectoriesMenu(selectedTracks, wwd);
					for(Component c : trajMenu.getMenuComponents()){
						menu.add(c);
					}
					menu.show(pistes, e.getX(), e.getY());
				}
			}
		});		

		if(reader instanceof LPLNReader || reader instanceof GEOReader || reader instanceof PLNSReader){
			pistes.getColumnExt("IAF").setVisible(false);
		}
		if (reader instanceof OPASReader) {
			pistes.getColumnExt("Type").setVisible(false);
		}
		if(reader instanceof OPASReader || reader instanceof LPLNReader || reader instanceof PLNSReader) {
			pistes.getColumnExt("Mode A").setVisible(false);
		}
		
		
		pistes.setColumnControlVisible(true);
		pistes.packAll();
		
		JScrollPane scrollPistes = new JScrollPane(pistes);
		scrollPistes.setBorder(null);
		scrollPistes.setPreferredSize(new Dimension(600,600));
		table.add(scrollPistes);
				
		content.add(this.createStylePane(), null);
		content.add(this.createFilterPane(), null);
		if(layer.isTrackColorFiltrable()) content.add(this.createColorFilterPane(), null);
		content.add(filterPolygonPane, null);
		filterPolygonPane.setVisible(layer.getModel().getPolygonFilters() != null && layer.getModel().getPolygonFilters().size()>0);
		content.add(table, null);
		
		layer.addPropertyChangeListener(AVKey.LAYER, new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent arg0) {
				table.setTitle("Trajectoires affichées ("+layer.getModel().getVisibleTracks().size()+")");
				filterPolygonPane.setVisible((layer.getModel().getPolygonFilters() != null && layer.getModel().getPolygonFilters().size() > 0));
			}
		});
		
		if(reader instanceof GEOReader) {
			final JButton save = new JButton("Sauver");
			save.setToolTipText("Enregistrer les trajectoires affichées");
			save.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent arg0) {
					VFileChooser fileChooser = new VFileChooser();
					if(fileChooser.showSaveDialog(save) == JFileChooser.APPROVE_OPTION){
						final String file = fileChooser.getSelectedFile().getAbsolutePath();
						if(!(new File(file).exists()) || 
								(new File(file).exists() &&
										JOptionPane.showConfirmDialog(null, "Le fichier existe déjà.\n\nSouhaitez-vous réellement l'écraser ?",
												"Confirmer la suppression du fichier précédent",
												JOptionPane.OK_CANCEL_OPTION,
												JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION)) {

							Collection<? extends VidesoTrack> tracks = layer.getModel().getVisibleTracks();
							final javax.swing.ProgressMonitor progress = new javax.swing.ProgressMonitor(null, "Sauvegarde des trajectoires sélectionnées", "",
									0, tracks.size()-1);
							progress.setMillisToDecideToPopup(0);
							progress.setMillisToPopup(0);
							new SwingWorker<Integer, Integer>() {

								@Override
								protected Integer doInBackground()
								throws Exception {
									GEOWriter writer = new GEOWriter(file, true);
									int i = 0;
									for(VidesoTrack track : layer.getModel().getVisibleTracks()){
										if(progress.isCanceled()){
											writer.cancel();
											return null;
										}
										i++;
										progress.setProgress(i);
										progress.setNote(i+" trajectoires sur "+progress.getMaximum());
										writer.writeTrack((GEOTrack) track);
									}
									writer.close();
									return null;
								}

							}.execute();

							progress.close();

						}
					}
				}
			});
			this.add(save, BorderLayout.SOUTH);
		}


	}
	
	public List<Triplet<String, String, Color>> getColorFilters(){
		return this.colorsDialog.getColors();
	}
	
	public void setColorFilters(List<Triplet<String, String, Color>> colors){
		this.colorsDialog.setColors(colors);
	}
	
	public HashMap<Integer, String> getFilters(){		
		return this.layer.getModel().getRegexFilters();
		
	}
	
	public void setRegexFilters(HashMap<Integer, String> filters){
		indicField.setText("");
		aDepField.setText("");
		aDestField.setText("");
		iafField.setText("");
		typeField.setText("");
		modeAField.setText("");
		for(Entry<Integer, String> filter : filters.entrySet()){
			switch (filter.getKey()) {
			case TracksModel.FIELD_ADEP:
				aDepField.setText(filter.getValue());
				break;
			case TracksModel.FIELD_ADEST:
				aDestField.setText(filter.getValue());
				break;
			case TracksModel.FIELD_IAF:
				iafField.setText(filter.getValue());
				break;
			case TracksModel.FIELD_MODE_A:
				modeAField.setText(filter.getValue());
				break;
			case TracksModel.FIELD_TYPE_AVION:
				typeField.setText(filter.getValue());
				break;
			case TracksModel.FIELD_INDICATIF:
				indicField.setText(filter.getValue());
				break;
			default:
				break;
			}
		}
		this.applyRegexFilters();
	}
	
	public void removeRegexFilters(){
		indicField.setText("");
		aDepField.setText("");
		aDestField.setText("");
		iafField.setText("");
		typeField.setText("");
		modeAField.setText("");
		layer.getModel().removeFilter();
		layer.getModel().applyFilters();
		trackContext.updateLayerPane();
	}
	
	public void applyRegexFilters(){
		layer.getModel().removeFilter();
		if(!indicField.getText().isEmpty()){
			layer.getModel().addFilter(TracksModel.FIELD_INDICATIF, indicField.getText());
		}
		if(!aDepField.getText().isEmpty()){
			layer.getModel().addFilter(TracksModel.FIELD_ADEP, aDepField.getText());
		}
		if(!aDestField.getText().isEmpty()){
			layer.getModel().addFilter(TracksModel.FIELD_ADEST, aDestField.getText());
		}
		if(!iafField.getText().isEmpty()){
			layer.getModel().addFilter(TracksModel.FIELD_IAF, iafField.getText());
		}
		if(!typeField.getText().isEmpty()){
			layer.getModel().addFilter(TracksModel.FIELD_TYPE_AVION, typeField.getText());
		}
		if(!modeAField.getText().isEmpty()){
			layer.getModel().addFilter(TracksModel.FIELD_MODE_A, modeAField.getText());
		}
		layer.getModel().applyFilters();
		trackContext.updateLayerPane();
	}
	
	/**
	 * Hide trajectories based on their numTraj id
	 * @param numTraj
	 */
	public void hideTrajectories(List<Integer> numTraj){
		this.layer.getModel().hideTracks(numTraj);
	}
	
	private JXTaskPane createPolygonFilterPane(){
		JXTaskPane filterPolygonPane = new JXTaskPane("Filtres volumiques");
		final JXTable polygonsTable = new JXTable(new PolygonTableModel());
		polygonsTable.setColumnControlVisible(true);
		polygonsTable.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent arg0) {}
			
			@Override
			public void mousePressed(MouseEvent arg0) {}
			
			@Override
			public void mouseExited(MouseEvent arg0) {}
			
			@Override
			public void mouseEntered(MouseEvent arg0) {}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getButton() == MouseEvent.BUTTON3){
					int rowView = polygonsTable.rowAtPoint(e.getPoint());
					polygonsTable.setRowSelectionInterval(rowView, rowView);
					final int rowModel = polygonsTable.convertRowIndexToModel(rowView);
					final JPopupMenu menu = new JPopupMenu();
					JMenuItem delete = new JMenuItem("Supprimer");
					delete.addActionListener(new ActionListener() {
						
						@Override
						public void actionPerformed(ActionEvent arg0) {
							((PolygonTableModel)polygonsTable.getModel()).deleteRow(rowModel);
							menu.setVisible(false);
						}
					});
					menu.add(delete);
					menu.setLocation(e.getPoint());
					menu.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});
		
		final ProgressMonitor progress = new ProgressMonitor(this, "Mise à jour des polygones", "", 0, 1);
		progress.setMillisToDecideToPopup(200);
		progress.setMillisToPopup(1000);
		layer.addPropertyChangeListener(new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent p) {
				if(p.getPropertyName().equals("change")){
					progress.setMaximum((Integer) p.getNewValue());
					progress.resetTimer();
				} else if(p.getPropertyName().equals("progress")){
					progress.setProgress((Integer) p.getNewValue());
				}
			}
		});
		
		JPanel container = new JPanel(new BorderLayout());
		container.add(polygonsTable.getTableHeader(), BorderLayout.NORTH);
		container.add(polygonsTable, BorderLayout.CENTER);
		filterPolygonPane.add(container);
		return filterPolygonPane;
	}
	
	private JXTaskPane createStylePane(){
		final JXTaskPane stylePane = new JXTaskPane("Style des trajectoires");
		stylePane.setCollapsed(true);
		
		stylePane.setLayout(new GridBagLayout());
		
		final JComboBox<String> styles = new JComboBox<String>();
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
			case TrajectoriesLayer.STYLE_MULTI_COLOR:
				styles.addItem("Fil de fer multicolor");
				break;
			case TrajectoriesLayer.STYLE_ANALYTICS:
				styles.addItem("Zone d'analyse");
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
		case TrajectoriesLayer.STYLE_MULTI_COLOR:
			styles.setSelectedItem("Fil de fer multicolor");
			break;
		case TrajectoriesLayer.STYLE_ANALYTICS:
			styles.setSelectedItem("Zone d'analyse");
			break;
		}
		
		styles.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				updateStylePane(styles, stylePane, null, null);
			}
		});
				
		updateStylePane(styles, stylePane, null, null);
		
		return stylePane;
	}
	
	private List<JButton> colorButtons;
	private List<JTextField> altitudeFields;
	private void updateStylePane(final JComboBox<String> styles, final JXTaskPane stylePane, List<JButton> colorButton, List<JTextField> altitudeField){
		this.colorButtons = colorButton;
		this.altitudeFields = altitudeField;
		stylePane.removeAll();
		int i = 0;

		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;

		stylePane.add(new JLabel("Style du tracé"), c);
		
		c.gridx = 1;
		
		stylePane.add(styles, c);
		
		/******** Paramètre pour la couleur *******/
		
		final JRadioButton speed = new JRadioButton("Vitesse (kt)");
		if(styles.getSelectedItem().equals("Fil de fer dégradé") ||
				styles.getSelectedItem().equals("Fil de fer multicolor")){
			JRadioButton altitude = new JRadioButton("Altitude (FL)");
			ButtonGroup btnGroup = new ButtonGroup();
			btnGroup.add(altitude);
			btnGroup.add(speed);

			speed.setSelected(layer.getParamColor() == TrajectoriesLayer.PARAM_SPEED);
			altitude.setSelected(layer.getParamColor() == TrajectoriesLayer.PARAM_ALTITUDE);
			
			i++;
			c.gridx = 0 ;
			c.gridy = i;
			stylePane.add(new JLabel("Paramètre : "),c);
			c.gridx = 1;
			stylePane.add(speed, c);
			c.gridx = 2;
			stylePane.add(altitude, c);
		}
		
		
		/******** Couleur interne *******/

		final JButton changeColor1 = new JButton(new ImageIcon(getClass().getResource("/resources/fill-color.png"))); 
		changeColor1.setBackground(layer.getDefaultInsideColor());
		changeColor1.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				changeColor1.setBackground(JColorChooser.showDialog(null, "Couleur", changeColor1.getBackground()));
			}
		});
		
		if(	styles.getSelectedItem().equals("Rideau") ||
				styles.getSelectedItem().equals("Profil avec balises")) {
			i++;
			c.gridx = 0;
			c.gridy = i;

			stylePane.add(new JLabel("Couleur interne"), c);

			c.gridx = 1;
			stylePane.add(changeColor1, c);
		}
		/******** Couleur externe *******/
		
		final JButton changeColor2 = new JButton(new ImageIcon(getClass().getResource("/resources/fill-color.png"))); 
		changeColor2.setBackground(layer.getDefaultOutsideColor());
		changeColor2.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				changeColor2.setBackground(JColorChooser.showDialog(null, "Couleur", changeColor2.getBackground()));
			}
		});

		if(styles.getSelectedItem().equals("Fil de fer") ||
				styles.getSelectedItem().equals("Rideau") ||
				styles.getSelectedItem().equals("Profil avec balises")){
			
			c.gridx = 0;
			c.gridy = 1+i;
			stylePane.add(new JLabel("Couleur externe"), c);

			c.gridx = 1;
			stylePane.add(changeColor2, c);
			i++;
		}

		/******** Dégradé de couleur *********/
		final JTextField minAltitude = new JTextField();
		final JTextField maxAltitude = new JTextField();
		final JButton minColor = new JButton(new ImageIcon(getClass().getResource("/resources/fill-color.png"))); 
		final JButton maxColor = new JButton(new ImageIcon(getClass().getResource("/resources/fill-color.png"))); 
		if(layer.getStylesAvailable().contains(TrajectoriesLayer.STYLE_SHADED)){
			minColor.setBackground(layer.getMinColor());
			minColor.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					minColor.setBackground(JColorChooser.showDialog(null, "Couleur", minColor.getBackground()));
				}
			});

			maxColor.setBackground(layer.getMaxColor());
			maxColor.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					maxColor.setBackground(JColorChooser.showDialog(null, "Couleur", maxColor.getBackground()));
				}
			});

			if(speed.isSelected()){
				minAltitude.setText(""+layer.getMinValue());

				maxAltitude.setText(""+layer.getMaxValue());
			} else {
				minAltitude.setText(""+layer.getMinValue()/30.47);

				maxAltitude.setText(""+layer.getMaxValue()/30.47);
			}

			if(styles.getSelectedItem().equals("Fil de fer dégradé")) {
				c.gridx = 0;
				c.gridy = 1+i;

				stylePane.add(new JLabel("Max : "),c);

				c.gridx = 1;
				stylePane.add(maxAltitude,c);

				c.gridx = 2;
				stylePane.add(maxColor, c);

				i++;

				c.gridx = 0;
				c.gridy = 1+i;

				stylePane.add(new JLabel("Min : "),c);

				c.gridx = 1;
				stylePane.add(minAltitude,c);

				c.gridx = 2;
				stylePane.add(minColor, c);

				i++;
			}
		}
		/******** Multicolor *******/
		if(layer.getStylesAvailable().contains(TrajectoriesLayer.STYLE_MULTI_COLOR)){
			if(colorButtons == null){
				colorButtons = new ArrayList<JButton>();
				altitudeFields = new ArrayList<JTextField>();
				for(int j = 0; j<layer.getMultiColors().getSecond().length;j++){
					final JButton button = new JButton(new ImageIcon(getClass().getResource("/resources/fill-color.png"))); 
					button.setBackground(layer.getMultiColors().getSecond()[j]);
					button.addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent arg0) {
							button.setBackground(JColorChooser.showDialog(null, "Couleur", button.getBackground()));
						}
					});
					colorButtons.add(button);
					if(layer.getParamColor() == TrajectoriesLayer.PARAM_SPEED){
						altitudeFields.add(new JTextField(""+layer.getMultiColors().getFirst()[j]));
					} else {
						altitudeFields.add(new JTextField(""+layer.getMultiColors().getFirst()[j]/30.47));
					}
				}
				if(layer.getParamColor() == TrajectoriesLayer.PARAM_SPEED){
					altitudeFields.add(new JTextField(""+layer.getMultiColors().getFirst()[layer.getMultiColors().getFirst().length-1]));
				} else {
					altitudeFields.add(new JTextField(""+layer.getMultiColors().getFirst()[layer.getMultiColors().getFirst().length-1]/30.47));
				}
			}
			if(styles.getSelectedItem().equals("Fil de fer multicolor")) {
				int j = 1;
				for(JButton button : colorButtons){
					c.gridx = 0;
					c.gridy = 1+i;
					stylePane.add(new JLabel("Tranche "+j+" : "),c);
					c.gridx = 1;
					stylePane.add(altitudeFields.get(j-1),c);
					c.gridx = 2;
					stylePane.add(button,c);
					i++;
					j++;
				}
				c.gridx = 0;
				c.gridy = 1+i;
				stylePane.add(new JLabel("Tranche "+j+" : "),c);
				c.gridx = 1;
				stylePane.add(altitudeFields.get(j-1),c);
				c.gridx = 2;
				JButton plus = new JButton("+");
				plus.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						final JButton button = new JButton(new ImageIcon(getClass().getResource("/resources/fill-color.png"))); 
						button.setBackground(layer.getMultiColors().getSecond()[layer.getMultiColors().getSecond().length-1]);
						button.addActionListener(new ActionListener() {

							@Override
							public void actionPerformed(ActionEvent arg0) {
								button.setBackground(JColorChooser.showDialog(null, "Couleur", button.getBackground()));
							}
						});
						colorButtons.add(button);
						if(layer.getParamColor() == TrajectoriesLayer.PARAM_SPEED){
							altitudeFields.add(new JTextField(""+layer.getMultiColors().getFirst()[layer.getMultiColors().getFirst().length-1]));
						} else {
							altitudeFields.add(new JTextField(""+layer.getMultiColors().getFirst()[layer.getMultiColors().getFirst().length-1]/30.47));
						}
						updateStylePane(styles, stylePane, colorButtons, altitudeFields);
					}
				});
				stylePane.add(plus,c);
				i++;
			}
		}
		/******** Opacité *******/
		final JTextField opacity = new JTextField(15);
		if(!styles.getSelectedItem().equals("Zone d'analyse")){
			c.gridx = 0;
			c.gridy = 1+i;

			stylePane.add(new JLabel("Opacité"), c);

			c.gridx = 1;

			opacity.setToolTipText("Valeur comprise entre 0 (transparent) et 100.");
			opacity.setText(String.valueOf(layer.getDefaultOpacity()*100));
			stylePane.add(opacity, c);
			i++;
		}
		
		/******** Largeur *******/
		final JTextField width = new JTextField(15);
		if(!styles.getSelectedItem().equals("Zone d'analyse")){
			c.gridx = 0;
			c.gridy = 1+i;

			stylePane.add(new JLabel("Largeur du tracé"), c);

			c.gridx = 1;

			width.setToolTipText("Valeur comprise entre 0 (transparent) et 100.");
			width.setText(String.valueOf(layer.getDefaultWidth()));

			stylePane.add(width, c);
			i++;
		}

		/******** Zone d'analyse *******/
		final JTextField widthAnalytic = new JTextField(15);
		final JTextField heightAnalytic = new JTextField(15);
		final JTextField scaleAnalytic = new JTextField(15);
		if(styles.getSelectedItem().equals("Zone d'analyse")){
			c.gridx = 0;
			c.gridy = 1+i;
			
			stylePane.add(new JLabel("Largeur"), c);
			
			c.gridx = 1;
			widthAnalytic.setText(String.valueOf(layer.getAnalyticWidth()));
			stylePane.add(widthAnalytic, c);
			i++;
			
			c.gridx = 0;
			c.gridy = 1+i;
			
			stylePane.add(new JLabel("Hauteur"), c);
			c.gridx = 1;
			heightAnalytic.setText(String.valueOf(layer.getAnalyticHeight()));
			
			stylePane.add(heightAnalytic, c);
			i++;
			
			c.gridx = 0;
			c.gridy = 1+i;
			
			stylePane.add(new JLabel("Échelle"), c);
			c.gridx = 1;
			
			scaleAnalytic.setText(String.valueOf(layer.getAnalyticScale()));
			stylePane.add(scaleAnalytic, c);
			
			i++;
			
		}
		
		c.gridx = 1;
		c.gridy = 1+i;
		
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
				} else if(itemSelected.equals("Profil avec balises")){
					layer.setStyle(TrajectoriesLayer.STYLE_PROFIL);
				} else if(itemSelected.equals("Fil de fer multicolor")){
					layer.setStyle(TrajectoriesLayer.STYLE_MULTI_COLOR);
				} else if(itemSelected.equals("Zone d'analyse")){
					layer.setStyle(TrajectoriesLayer.STYLE_ANALYTICS);
				}
				layer.setDefaultOutsideColor(changeColor2.getBackground());
				layer.setDefaultInsideColor(changeColor1.getBackground());
				if(!width.getText().isEmpty()) layer.setDefaultWidth(Double.parseDouble(width.getText()));
				if(!opacity.getText().isEmpty()) {
					layer.setDefaultOpacity(Double.parseDouble(opacity.getText())/100.0);
				}
				if(layer.getStylesAvailable().contains(TrajectoriesLayer.STYLE_SHADED)){
					layer.setShadedColors(
							speed.isSelected() ? TrajectoriesLayer.PARAM_SPEED : TrajectoriesLayer.PARAM_ALTITUDE,
							speed.isSelected() ? Double.parseDouble(minAltitude.getText()) : Double.parseDouble(minAltitude.getText())*30.47,
							speed.isSelected() ? Double.parseDouble(maxAltitude.getText()) : Double.parseDouble(maxAltitude.getText())*30.47,
							minColor.getBackground(), maxColor.getBackground());
				}
				if(altitudeFields != null){
					ArrayList<Double> values = new ArrayList<Double>();
					for(JTextField field : altitudeFields){
						if(speed.isSelected()){
							values.add(Double.parseDouble(field.getText()));
						} else {
							values.add(Double.parseDouble(field.getText())*30.47);
						}
					}
					ArrayList<Color> colors = new ArrayList<Color>();
					for(JButton color : colorButtons){
						colors.add(color.getBackground());
					}
					
					//enregistrement des paramètres dans le fichier de conf
					Configuration.setProperty(Configuration.TRAJECTOGRAPHIE_MULTICOLOR_PARAM, speed.isSelected() ? TrajectoriesLayer.PARAM_SPEED+"" : TrajectoriesLayer.PARAM_ALTITUDE+"");
					StringBuffer colorsString = new StringBuffer();
					for(Color c : colors){
						colorsString.append(c.getRGB()+"/");
					}
					Configuration.setProperty(Configuration.TRAJECTOGRAPHIE_MULTICOLOR_COLORS, colorsString.toString());
					StringBuffer valuesString = new StringBuffer();
					for(Double v : values){
						valuesString.append(v+"/");
					}
					Configuration.setProperty(Configuration.TRAJECTOGRAPHIE_MULTICOLOR_VALUES, valuesString.toString());
					
					layer.setMultiColors(speed.isSelected() ? TrajectoriesLayer.PARAM_SPEED : TrajectoriesLayer.PARAM_ALTITUDE,
									values.toArray(new Double[]{}),
									colors.toArray(new Color[]{}));
				}
				if(!widthAnalytic.getText().isEmpty()) layer.setAnalyticWidth(Integer.parseInt(widthAnalytic.getText()));
				if(!heightAnalytic.getText().isEmpty()) layer.setAnalyticHeight(Integer.parseInt(heightAnalytic.getText()));
				if(!scaleAnalytic.getText().isEmpty()) layer.setAnalyticScale(Integer.parseInt(scaleAnalytic.getText()));
				layer.update();
			}
		});
		
		stylePane.add(validate, c);	
		
		stylePane.validate();
	}
	
	private JXTaskPane createColorFilterPane(){
		JXTaskPane colorFilterPane = new JXTaskPane("Filtres de couleurs");
		colorFilterPane.setCollapsed(true);
		
		//Gestion des couleurs
		colorsDialog = new TrajectoriesColorsDialog();
		colorsDialog.addPropertyChangeListener("valuesChanged", new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent p) {
				layer.resetFilterColor();
				if(p.getNewValue() != null) {
					for(Triplet<String, String, Color> filter : (List<Triplet<String, String, Color>>)p.getNewValue()){
						layer.addFilterColor(TracksModel.string2type(filter.getFirst()), filter.getSecond(), filter.getThird());
					}
				}
			}
		});
		colorFilterPane.add(colorsDialog);
		
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
		indicField = new JTextField(10);
		indicField.setMaximumSize(new Dimension(100, 30));
		indicatif.add(indicLabel);
		indicatif.add(Box.createHorizontalGlue());
		indicatif.add(indicField);

		filtres.add(indicatif);
		
		JPanel aDep = new JPanel();
		aDep.setLayout(new BoxLayout(aDep, BoxLayout.X_AXIS));
		JLabel aDepLabel = new JLabel("Aéroport départ : ");
		aDepField = new JTextField(10);
		aDepField.setMaximumSize(new Dimension(100, 30));
		aDep.add(aDepLabel);
		aDep.add(Box.createHorizontalGlue());
		aDep.add(aDepField);

		filtres.add(aDep);

		JPanel aDest = new JPanel();
		aDest.setLayout(new BoxLayout(aDest, BoxLayout.X_AXIS));
		JLabel aDestLabel = new JLabel("Aéroport arrivée : ");
		aDestField = new JTextField(10);
		aDestField.setMaximumSize(new Dimension(100, 30));
		aDest.add(aDestLabel);
		aDest.add(Box.createHorizontalGlue());
		aDest.add(aDestField);

		filtres.add(aDest);

		JPanel iaf = new JPanel();
		iaf.setLayout(new BoxLayout(iaf, BoxLayout.X_AXIS));
		JLabel iafLabel = new JLabel("IAF : ");
		iafField = new JTextField(10);
		iafField.setMaximumSize(new Dimension(100, 30));
		iaf.add(iafLabel);
		iaf.add(Box.createHorizontalGlue());
		iaf.add(iafField);

		filtres.add(iaf);

		JPanel type = new JPanel();
		type.setLayout(new BoxLayout(type, BoxLayout.X_AXIS));
		JLabel typeLabel = new JLabel("Type avion : ");
		typeField = new JTextField(10);
		typeField.setMaximumSize(new Dimension(100, 30));
		type.add(typeLabel);
		type.add(Box.createHorizontalGlue());
		type.add(typeField);

		filtres.add(type);
		
		JPanel modeA = new JPanel();
		modeA.setLayout(new BoxLayout(modeA, BoxLayout.X_AXIS));
		JLabel modeALbl = new JLabel("Mode A : ");
		modeAField = new JTextField(10);
		modeAField.setMaximumSize(new Dimension(100, 30));
		modeA.add(modeALbl);
		modeA.add(Box.createHorizontalGlue());
		modeA.add(modeAField);
		
		filtres.add(modeA);
		
		JPanel validate = new JPanel();
		validate.setLayout(new BoxLayout(validate, BoxLayout.X_AXIS));
		JButton val = new JButton("Filtrer");
		validate.add(val);
		val.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				applyRegexFilters();
			}
		});
		JButton cancel = new JButton("Effacer");
		validate.add(cancel);
		cancel.addActionListener(new ActionListener() {	
			@Override
			public void actionPerformed(ActionEvent e) {
				removeRegexFilters();
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
				layer.getModel().setFilterDisjunctive(!(e.getStateChange() == ItemEvent.SELECTED));
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
	 * Supprime le layer associé au sélecteur.<br />
	 */
	public void delete(){
		this.wwd.getModel().getLayers().remove(layer);
	}
	
	public TrajectoriesLayer getLayer(){
		return this.layer;
	}
	

	
	private class PolygonTableModel extends AbstractTableModel {

		private String[] columnNames = {"Nom", "Trajectoires", "Actif"};

		private List<PolygonsSetFilter> polygons;
						
		public PolygonTableModel(){
			super();
			if(layer.getModel().getPolygonFilters() != null){
				this.polygons = layer.getModel().getPolygonFilters();
			}
			
			layer.addPropertyChangeListener(AVKey.LAYER, new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					if(layer.getModel().getPolygonFilters() != null){
						polygons = layer.getModel().getPolygonFilters();
						fireTableDataChanged();
					}
				}
			});
		}
		
		public void deleteRow(int rowModel) {
			PolygonsSetFilter filters = (PolygonsSetFilter) this.polygons.get(rowModel);
			this.polygons.remove(rowModel);
			layer.getModel().removePolygonFilter(filters);
			this.fireTableDataChanged();
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
			if(polygons == null){
				return 0;
			} else {
				return polygons.size();
			}
		}

		@Override
		public Object getValueAt(int row, int col) {
			switch (col) {
			case 0:
				return this.polygons.get(row).getName();
			case 1:
				return this.polygons.get(row).getContainedTrajectories();
			case 2:
				return this.polygons.get(row).isActive();
			default:
				return "";
			}
			
		}

		
		/* (non-Javadoc)
		 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
		 */
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			if(columnIndex == 2){
				return Boolean.class;
			} else if(columnIndex == 1){
				return Integer.class;
			} else {
				return String.class;
			}
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
		 */
		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			if(columnIndex == 2){
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
			if(columnIndex == 2){
				if((Boolean) aValue){
					layer.getModel().enablePolygonFilter(this.polygons.get(rowIndex));
				} else {
					layer.getModel().disablePolygonFilter(this.polygons.get(rowIndex));
				}
				fireTableDataChanged();
			}
		}

		
		
	}
}
