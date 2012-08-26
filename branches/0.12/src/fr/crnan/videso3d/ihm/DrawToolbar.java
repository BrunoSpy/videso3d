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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;

import fr.crnan.videso3d.DatasManager;
import fr.crnan.videso3d.Pallet;
import fr.crnan.videso3d.VidesoGLCanvas;
import fr.crnan.videso3d.graphics.MovableBalise3D;
import fr.crnan.videso3d.graphics.VPolygon;
import fr.crnan.videso3d.graphics.VidesoObject;
import fr.crnan.videso3d.graphics.editor.EllipsoidFactory;
import fr.crnan.videso3d.graphics.editor.PolygonEditorsManager;
import fr.crnan.videso3d.graphics.editor.ShapeEditorsManager;
import fr.crnan.videso3d.ihm.components.DropDownButton;
import fr.crnan.videso3d.ihm.components.VFileChooser;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.airspaces.BasicAirspaceAttributes;
import gov.nasa.worldwindx.examples.shapebuilder.RigidShapeEditor;
import gov.nasa.worldwindx.examples.util.ShapeUtils;
/**
 * Toolbar de dessin
 * @author Bruno Spyckerelle
 * @version 0.1.1
 */
public class DrawToolbar extends JToolBar {

	private VidesoGLCanvas wwd;
	
	public DrawToolbar(VidesoGLCanvas ww){
		this.wwd = ww;
		
		this.createToolbar();
	}
	
	public void createToolbar(){
		final DropDownButton addAirspace = new DropDownButton(new ImageIcon(getClass().getResource("/resources/draw-polygon_22_1.png")));
		addAirspace.setToolTipText("Créer un polygone 3D");

		final JMenuItem addPolygon = new JMenuItem("Nouveau");
		addPolygon.setToolTipText("Nouveau polygone");
		addPolygon.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent evt) {
				VPolygon polygon = new VPolygon();
				polygon.setAltitudes(0.0, 0.0);
				polygon.setTerrainConforming(true, false);
				BasicAirspaceAttributes attrs = new BasicAirspaceAttributes();
				attrs.setDrawOutline(true);
				attrs.setMaterial(new Material(Color.CYAN));
				attrs.setOutlineMaterial(new Material(Pallet.makeBrighter(Color.CYAN)));
				attrs.setOpacity(0.2);
				attrs.setOutlineOpacity(0.9);
				attrs.setOutlineWidth(1.5);
				polygon.setAttributes(attrs);

				Position position = ShapeUtils.getNewShapePosition(wwd);
				Angle heading = ShapeUtils.getNewShapeHeading(wwd, true);
				double sizeInMeters = ShapeUtils.getViewportScaleFactor(wwd);

				java.util.List<LatLon> locations = ShapeUtils.createSquareInViewport(wwd, position, heading, sizeInMeters);

				double maxElevation = -Double.MAX_VALUE;
				Globe globe = wwd.getModel().getGlobe();

				for (LatLon ll : locations)
				{
					double e = globe.getElevation(ll.getLatitude(), ll.getLongitude());
					if (e > maxElevation)
						maxElevation = e;
				}

				polygon.setAltitudes(0.0, maxElevation + sizeInMeters);
				polygon.setTerrainConforming(true, false);
				polygon.setLocations(locations);

				PolygonEditorsManager.editAirspace(polygon, true);
			}
		});

		JMenuItem addFromFile = new JMenuItem("Charger un fichier");
		addFromFile.setToolTipText("Nouveau polygone depuis un fichier");
		addFromFile.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				VFileChooser fileChooser = new VFileChooser();
				fileChooser.setMultiSelectionEnabled(false);
				if(fileChooser.showOpenDialog(null) == VFileChooser.APPROVE_OPTION){
					File file = fileChooser.getSelectedFile();
					//TODO prendre en charge d'autres formes
					BasicAirspaceAttributes attrs = new BasicAirspaceAttributes();
					attrs.setDrawOutline(true);
					attrs.setMaterial(new Material(Color.CYAN));
					attrs.setOutlineMaterial(new Material(Pallet.makeBrighter(Color.CYAN)));
					attrs.setOpacity(0.2);
					attrs.setOutlineOpacity(0.9);
					attrs.setOutlineWidth(1.5);
					VPolygon p = new VPolygon();
					p.setAttributes(attrs);
					BufferedReader input = null;
					try {
						input = new BufferedReader(new FileReader(file));
						String s = input.readLine();
						p.restoreState(s);
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						if(input != null)
							try {
								input.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
					}
					PolygonEditorsManager.editAirspace(p, true);
				}
			}
		});

		JMenuItem addFromText = new JMenuItem("Texte");
        addFromText.setToolTipText("Entrer/Copier des coordonnées");
        addFromText.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent arg0) {
                        new PolygonImportUI().setVisible(true);
                }
        });
		
		addAirspace.getPopupMenu().add(addPolygon);
		addAirspace.getPopupMenu().add(addFromFile);
		addAirspace.getPopupMenu().add(addFromText);

		addAirspace.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				addPolygon.doClick();
			}
		});
		addAirspace.addToToolBar(this);

		//Ajouter points
		final DropDownButton points = new DropDownButton(new ImageIcon(getClass().getResource("/resources/add_point_22.png")));
		points.setToolTipText("Créer un point");
		
		final JMenuItem addPoint = new JMenuItem("Ajouter un point");
		addPoint.setToolTipText("Ajouter un point sur le globe");
		addPoint.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				MovableBalise3D point = new MovableBalise3D("Nouveau point", ShapeUtils.getNewShapePosition(wwd));
				try {
					DatasManager.getUserObjectsController(wwd).addObject(point);
				} catch (Exception e) {
					e.printStackTrace();
				}
				wwd.getDraggerListener().addDraggableObject(point);
			}
		});

		this.add(addPoint);

		JMenuItem addPoints = new JMenuItem("Ajouter plusieurs points");
		addPoints.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				MultiplePointsAddGUI gui = new MultiplePointsAddGUI();
				if(gui.showDialog() == JOptionPane.OK_OPTION){
					for(VidesoObject o : gui.getObjects()){
						try {
							DatasManager.getUserObjectsController(wwd).addObject(o);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				};
			}
		});
		points.getPopupMenu().add(addPoint);
		points.getPopupMenu().add(addPoints);
		points.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				addPoint.doClick();
			}
		});

		points.addToToolBar(this);


		//Ajout d'une ellipse
		final JButton ellipse = new JButton(new ImageIcon(getClass().getResource("/resources/add_ellipse_22.png")));
		ellipse.setToolTipText("Créer une ellipse 3D");
		ellipse.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				ShapeEditorsManager.editShape(new EllipsoidFactory().createShape(wwd, true), RigidShapeEditor.TRANSLATION_MODE);
			}
		});
		this.add(ellipse);
	}
	
}
