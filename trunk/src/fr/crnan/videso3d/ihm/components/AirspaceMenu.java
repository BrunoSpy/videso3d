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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

import fr.crnan.videso3d.Pallet;
import fr.crnan.videso3d.VidesoGLCanvas;
import fr.crnan.videso3d.graphics.VPolygon;
import fr.crnan.videso3d.graphics.VidesoObject;
import fr.crnan.videso3d.graphics.editor.PolygonEditorsManager;
import fr.crnan.videso3d.ihm.ContextPanel;
import fr.crnan.videso3d.layers.TrajectoriesLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.airspaces.Airspace;
import gov.nasa.worldwind.render.airspaces.AirspaceAttributes;
import gov.nasa.worldwind.render.airspaces.Polygon;

import javax.swing.JColorChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
/**
 * Contextual menu for {@link Airspace}
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class AirspaceMenu extends JPopupMenu {

	private Airspace airspace;
	private AirspaceAttributes attrs;
	private ContextPanel context;
	private VidesoGLCanvas wwd;
	
	public AirspaceMenu(Airspace airspace, AirspaceAttributes attrs, ContextPanel context, VidesoGLCanvas wwd){
		super("Menu");
		this.airspace = airspace;
		this.attrs = attrs;
		this.context = context;
		this.wwd = wwd;		
		
		this.createMenu();
	}
	
	private JPopupMenu getMenu(){
		return this;
	}
	
	private void createMenu(){
		if(airspace instanceof VidesoObject){
			JMenuItem info = new JMenuItem("Informations...");
			info.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent event) {
					VidesoObject o = (VidesoObject) airspace;
					context.showInfo(o.getDatabaseType(), o.getType(), o.getName());
					context.open();
				}
			});
			this.add(info);
			this.add(new JSeparator());
		}
		
		
		JMenuItem colorItem = new JMenuItem("Couleur...");
		colorItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Color color = JColorChooser.showDialog(getMenu(), "Couleur", attrs.getMaterial().getDiffuse());
				if(color != null) {
					attrs.setMaterial(new Material(color));
					attrs.setOutlineMaterial(new Material(Pallet.makeBrighter(color)));
				}
			}
		});
		this.add(colorItem);
		
		JMenu opacityItem = new JMenu("Opacité ...");
		JSlider slider = new JSlider();
		slider.setMaximum(100);
		slider.setMinimum(0);
		slider.setOrientation(JSlider.VERTICAL);
		slider.setMinorTickSpacing(10);
		slider.setMajorTickSpacing(20);
		slider.setPaintLabels(true);
		slider.setPaintTicks(true);
		opacityItem.add(slider);
		slider.setValue((int)(attrs.getOpacity()*100.0));
		slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider)e.getSource();
				attrs.setOpacity(source.getValue()/100.0);
				wwd.redraw();
			}
		});
		this.add(opacityItem);
		this.add(new JSeparator());
		
		if(airspace instanceof Polygon){
			if(PolygonEditorsManager.isEditing((Polygon) airspace)){
				JMenuItem edit = new JMenuItem("Terminer l'édition");
				edit.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent arg0) {
						PolygonEditorsManager.stopEditAirspace((Polygon) airspace);
					}
				});		
				this.add(edit);
			} else {
				JMenuItem edit = new JMenuItem("Editer");
				edit.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent arg0) {
						VPolygon polygon = new VPolygon(((Polygon)airspace).getLocations());
						polygon.setAltitudes(((Polygon)airspace).getAltitudes()[0],((Polygon)airspace).getAltitudes()[1] );
						polygon.setAttributes(airspace.getAttributes());
						wwd.deleteAirspace(airspace);
						PolygonEditorsManager.editAirspace(polygon, true);
					}
				});		
				this.add(edit);
			}
			this.add(new JSeparator());
		}
		
		List<TrajectoriesLayer> trajLayers = new LinkedList<TrajectoriesLayer>();
		for(Layer l : this.wwd.getModel().getLayers()){
			if(l instanceof TrajectoriesLayer){
				if(((TrajectoriesLayer) l).isPolygonFilterable())
					trajLayers.add((TrajectoriesLayer) l);
			}
		}
		if(airspace instanceof VPolygon && trajLayers.size() > 0 ){
			JMenu filter = new JMenu("Filtrer...");
			for(final TrajectoriesLayer l : trajLayers){
				JMenuItem layer = new JMenuItem(l.getName());
				layer.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent arg0) {
						int polygonNumber = l.getPolygonFilters() == null ? 0 : l.getPolygonFilters().size();
						final ProgressMonitor progress = new ProgressMonitor(wwd, "Calcul des trajectoires filtrées", "", 0, l.getSelectedTracks().size()*(polygonNumber+1));
						l.addPropertyChangeListener("progress", new PropertyChangeListener() {
							
							@Override
							public void propertyChange(PropertyChangeEvent evt) {
								progress.setProgress((Integer) evt.getNewValue());
							}
						});
						new SwingWorker<Integer, Integer>() {

							@Override
							protected Integer doInBackground() throws Exception {
								l.addPolygonFilter((VPolygon) airspace);
								return null;
							}
						}.execute();
						
					}
				});
				filter.add(layer);
			}
			this.add(filter);
			this.add(new JSeparator());
		}
		
		JMenuItem save = new JMenuItem("Sauver ...");
		save.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				VFileChooser fileChooser = new VFileChooser();
				fileChooser.setMultiSelectionEnabled(false);
				if(fileChooser.showSaveDialog(getMenu()) == VFileChooser.APPROVE_OPTION){
					File file = fileChooser.getSelectedFile();
					if(!(file.exists()) || 
							(file.exists() &&
							JOptionPane.showConfirmDialog(null, "Le fichier existe déjà.\n\nSouhaitez-vous réellement l'écraser ?",
								"Confirmer la suppression du fichier précédent",
								JOptionPane.OK_CANCEL_OPTION,
								JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION)) {
						String xmlString = airspace.getRestorableState();
						if(xmlString != null){
							try{
								PrintWriter of = new PrintWriter(file);
								of.write(xmlString);
								of.flush();
								of.close();
							}
							catch (Exception e){
								e.printStackTrace();
							}
						}
					}
				}
			}
		});
		this.add(save);
		
		JMenuItem delete = new JMenuItem("Supprimer");
		delete.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				wwd.deleteAirspace(airspace);
			}
		});
		this.add(delete);
	}
	
}
