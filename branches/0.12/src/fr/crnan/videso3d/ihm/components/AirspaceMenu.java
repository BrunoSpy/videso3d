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
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import fr.crnan.videso3d.DatasManager;
import fr.crnan.videso3d.Pallet;
import fr.crnan.videso3d.VidesoController;
import fr.crnan.videso3d.VidesoGLCanvas;
import fr.crnan.videso3d.databases.DatabaseManager.Type;
import fr.crnan.videso3d.databases.aip.AIPController;
import fr.crnan.videso3d.databases.edimap.EdimapController;
import fr.crnan.videso3d.databases.exsa.STRController;
import fr.crnan.videso3d.databases.stip.StipController;
import fr.crnan.videso3d.graphics.DatabaseVidesoObject;
import fr.crnan.videso3d.graphics.Secteur3D;
import fr.crnan.videso3d.graphics.VPolygon;
import fr.crnan.videso3d.graphics.VidesoObject;
import fr.crnan.videso3d.graphics.editor.PolygonEditorsManager;
import fr.crnan.videso3d.ihm.AirspaceAttributesDialog;
import fr.crnan.videso3d.ihm.ContextPanel;
import fr.crnan.videso3d.layers.tracks.TrajectoriesLayer;
import fr.crnan.videso3d.trajectography.PolygonsSetFilter;
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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
/**
 * Contextual menu for {@link Airspace}
 * @author Bruno Spyckerelle
 * @version 0.1.3
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
			if(airspace instanceof DatabaseVidesoObject) {
				JMenuItem info = new JMenuItem("Informations...");
				info.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent event) {
						DatabaseVidesoObject o = (DatabaseVidesoObject) airspace;
						context.showInfo(o.getDatabaseType(), o.getType(), o.getName());
					}
				});
				this.add(info);
				this.add(new JSeparator());
			}
			JMenuItem colorItem = new JMenuItem("Couleur...");
			colorItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					new AirspaceAttributesDialog((AirspaceAttributes)(((VidesoObject) airspace).getNormalAttributes()),
							(AirspaceAttributes)(((VidesoObject) airspace).getHighlightAttributes())).setVisible(true);
					wwd.redraw();
				}
			});
			this.add(colorItem);
			
		} else {


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

			OpacityMenuItem opacityItem = new OpacityMenuItem();
			opacityItem.setValue((int)(attrs.getOpacity()*100.0));
			opacityItem.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					JSlider source = (JSlider)e.getSource();
					attrs.setOpacity(source.getValue()/100.0);
					wwd.redraw();
				}
			});
			this.add(opacityItem);

		}
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
						if(airspace instanceof Secteur3D){
							VPolygon polygon = new VPolygon(((Polygon)airspace).getLocations());
							polygon.setAltitudes(((Polygon)airspace).getAltitudes()[0],((Polygon)airspace).getAltitudes()[1] );
							polygon.setAttributes(airspace.getAttributes());
							wwd.delete(airspace);
							PolygonEditorsManager.editAirspace(polygon, true);
						} else {
							PolygonEditorsManager.editAirspace((Polygon) airspace, true);
						}
					}
				});		
				this.add(edit);
			}
			this.add(new JSeparator());
		}
		
		List<TrajectoriesLayer> trajLayers = new ArrayList<TrajectoriesLayer>();
		for(Layer l : this.wwd.getModel().getLayers()){
			if(l instanceof TrajectoriesLayer){
			//	if(((TrajectoriesLayer) l).isPolygonFilterable())
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
						String name = new String();
						PolygonsSetFilter filter = null;
						if(airspace instanceof Secteur3D){
							name = ((Secteur3D) airspace).getName().split(" ")[0];
							if(((Secteur3D) airspace).getDatabaseType() == Type.STIP){
								filter = new PolygonsSetFilter(name, ((StipController)DatasManager.getController(((Secteur3D) airspace).getDatabaseType())).getPolygons(((Secteur3D) airspace).getName()));
							}else if(((Secteur3D) airspace).getDatabaseType() == Type.AIP){
								filter = new PolygonsSetFilter(name, ((AIPController)DatasManager.getController(((Secteur3D) airspace).getDatabaseType())).getPolygons(((Secteur3D) airspace).getType(), ((Secteur3D) airspace).getName()));
							} 							
						} else {
							name = "Polygone";
							List<VPolygon> polygons = new ArrayList<VPolygon>();
							polygons.add((VPolygon) airspace);
							filter = new PolygonsSetFilter(name, polygons);
						}
						
						l.getModel().addPolygonFilter(filter);
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
		
		//Coordonnées
		if(airspace instanceof DatabaseVidesoObject){
			VidesoController c = DatasManager.getController(((DatabaseVidesoObject) airspace).getDatabaseType());
			if(!(c instanceof STRController || c instanceof EdimapController)){
				final int type = ((DatabaseVidesoObject) airspace).getType();
				final String name = ((DatabaseVidesoObject) airspace).getName();
				final boolean locationsVisible = c.areLocationsVisible(type, name);
				JMenuItem locationsItem = new JMenuItem((locationsVisible ? "Cacher" : "Afficher") +" les coordonnées");
				this.add(locationsItem);
				locationsItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						VidesoController c = DatasManager.getController(((DatabaseVidesoObject) airspace).getDatabaseType());
						c.setLocationsVisible(type, name, !locationsVisible);
					}
				});
			}
		}
		
		JMenuItem delete = new JMenuItem("Supprimer");
		delete.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				wwd.delete(airspace);
			}
		});
		this.add(delete);
	}
	
}
