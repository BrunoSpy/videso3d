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
import java.util.Collection;
import java.util.List;

import fr.crnan.videso3d.DatasManager;
import fr.crnan.videso3d.Pallet;
import fr.crnan.videso3d.VidesoController;
import fr.crnan.videso3d.VidesoGLCanvas;
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
 * @version 0.2.0
 */
public class AirspaceMenu extends JMenu {

	private List<Airspace> airspaces;
	private ContextPanel context;
	private VidesoGLCanvas wwd;
	
	public AirspaceMenu(List<Airspace> airspaces, ContextPanel context, VidesoGLCanvas wwd){
		super("Airspaces");
		this.airspaces = airspaces;
		this.context = context;
		this.wwd = wwd;		
		
		this.createMenu();
	}
	
	public AirspaceMenu(Airspace airspace, ContextPanel context, VidesoGLCanvas wwd){
		super("Airspaces");
		this.airspaces = new ArrayList<Airspace>();
		this.airspaces.add(airspace);
		this.context = context;
		this.wwd = wwd;		
		
		this.createMenu();
	}
	
	private void createMenu(){
		if(airspaces.size() == 1 && context != null){
			//only shows infos if there's only one selected airspace
			if(airspaces.get(0) instanceof DatabaseVidesoObject) {
				JMenuItem info = new JMenuItem("Informations...");
				info.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent event) {
						DatabaseVidesoObject o = (DatabaseVidesoObject) airspaces.get(0);
						context.showInfo(o.getDatabaseType(), o.getType(), o.getName());
					}
				});
				this.add(info);
				this.add(new JSeparator());
			}
		}
		if(airspaces.size() == 1 && airspaces.get(0) instanceof VidesoObject){
			
			final Airspace airspace = airspaces.get(0);
			
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
					Color initialColor;
					if(airspaces.get(0).getAttributes() != null){
						initialColor = airspaces.get(0).getAttributes().getMaterial().getDiffuse();
					} else {
						initialColor = Color.BLUE;
					}
					Color color = JColorChooser.showDialog(AirspaceMenu.this, "Couleur", initialColor);
					if(color != null) {
						for(Airspace a : airspaces){
							if(a instanceof VidesoObject && ((VidesoObject) a).getNormalAttributes() != null){
								((AirspaceAttributes)((VidesoObject) a).getNormalAttributes()).setMaterial(new Material(color));
								((AirspaceAttributes)((VidesoObject) a).getNormalAttributes()).setOutlineMaterial(new Material(Pallet.makeBrighter(color)));
							} else if(a.getAttributes() != null){
								a.getAttributes().setMaterial(new Material(color));
								a.getAttributes().setOutlineMaterial(new Material(Pallet.makeBrighter(color)));
							}
						}
					}
				}
			});
			this.add(colorItem);

			OpacityMenuItem opacityItem = new OpacityMenuItem();
			opacityItem.setValue((int)(airspaces.get(0).getAttributes().getOpacity()*100.0));
			opacityItem.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					JSlider source = (JSlider)e.getSource();
					for(Airspace a : airspaces) {
						if(a.getAttributes() != null)
							a.getAttributes().setOpacity(source.getValue()/100.0);
					}
					wwd.redraw();
				}
			});
			this.add(opacityItem);

		}
		this.add(new JSeparator());
		
		if(airspaces.size() == 1){
			final Airspace airspace = airspaces.get(0);
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
								DatasManager.getUserObjectsController(wwd).addObject(airspace);
								PolygonEditorsManager.editAirspace(polygon);
							} else {
								PolygonEditorsManager.editAirspace((Polygon) airspace);
							}
						}
					});		
					this.add(edit);
				}
				this.add(new JSeparator());
			}
		}
		
		List<TrajectoriesLayer> trajLayers = new ArrayList<TrajectoriesLayer>();
		for(Layer l : this.wwd.getModel().getLayers()){
			if(l instanceof TrajectoriesLayer){
				//	if(((TrajectoriesLayer) l).isPolygonFilterable())
				trajLayers.add((TrajectoriesLayer) l);
			}
		}
		
		boolean vpolygons = true;
		for(Airspace a : airspaces){
			vpolygons = vpolygons && (a instanceof VPolygon);
		}
		//every airspace has to be a VPolygon to be used as a filter
		if(vpolygons && trajLayers.size() > 0 ){
			JMenu filter = new JMenu("Filtrer...");
			for(final TrajectoriesLayer l : trajLayers){
				JMenuItem layer = new JMenuItem(l.getName());
				layer.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent arg0) {
						String name = new String();
						PolygonsSetFilter filter = null;
						if(airspaces.size() == 1 && airspaces.get(0) instanceof Secteur3D){
							Airspace airspace = airspaces.get(0);
							name = ((Secteur3D) airspace).getName().split(" ")[0];
							if(((Secteur3D) airspace).getDatabaseType() == DatasManager.Type.STIP){
								filter = new PolygonsSetFilter(name, ((StipController)DatasManager.getController(((Secteur3D) airspace).getDatabaseType())).getPolygons(((Secteur3D) airspace).getName()));
							}else if(((Secteur3D) airspace).getDatabaseType() == DatasManager.Type.AIP){
								filter = new PolygonsSetFilter(name, ((AIPController)DatasManager.getController(((Secteur3D) airspace).getDatabaseType())).getPolygons(((Secteur3D) airspace).getType(), ((Secteur3D) airspace).getName()));
							} 							
						} else {
							name = "Polygones";
							List<VPolygon> polygons = new ArrayList<VPolygon>();
							polygons.addAll((Collection<? extends VPolygon>) airspaces);
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
		
		if(airspaces.size() == 1){
			
			final Airspace airspace = airspaces.get(0);
			
			//TODO encore utile avec la gestion de projet ???
			JMenuItem save = new JMenuItem("Sauver ...");
			save.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					VFileChooser fileChooser = new VFileChooser();
					fileChooser.setMultiSelectionEnabled(false);
					if(fileChooser.showSaveDialog(AirspaceMenu.this) == VFileChooser.APPROVE_OPTION){
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
		}
		JMenuItem delete = new JMenuItem("Supprimer");
		delete.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				for(Airspace a : airspaces)
					wwd.delete(a);
			}
		});
		this.add(delete);
	}
	
}
