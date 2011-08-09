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

import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.airspaces.AirspaceAttributes;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
/**
 * JButton that pops up a JColorChooser and change the color of a JLabel
 * @author Bruno Spyckerelle
 * @version 0.2.0
 */
public class JColorButton extends JButton {

	private JLabel label;
	
	public JColorButton(final WorldWindowGLCanvas wwd, JLabel lbl, final AirspaceAttributes attrs, final boolean inner){
		super(" ");
		this.setIcon(new ImageIcon(getClass().getResource("/resources/fill-color.png")));
		this.label = lbl;
		this.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Color color = JColorChooser.showDialog(null, "Couleur", label.getBackground());
				if(color != null){
					label.setBackground(color);
					if(inner)
						attrs.setMaterial(new Material(color));
					else
						attrs.setOutlineMaterial(new Material(color));
					wwd.redraw();
				}
			}
		});
	}

	public JColorButton(final WorldWindowGLCanvas wwd, JLabel lbl,
			final ShapeAttributes attrs, final boolean inner) {
		super(" ");
		this.setIcon(new ImageIcon(getClass().getResource("/resources/fill-color.png")));
		this.label = lbl;
		this.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Color color = JColorChooser.showDialog(null, "Couleur", label.getBackground());
				if(color != null){
					label.setBackground(color);
					if(inner)
						attrs.setInteriorMaterial(new Material(color));
					else
						attrs.setOutlineMaterial(new Material(color));
					wwd.redraw();
				}
			}
		});
	}

}
