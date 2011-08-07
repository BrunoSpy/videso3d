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

import fr.crnan.videso3d.SimpleGLCanvas;
import fr.crnan.videso3d.ihm.components.TitledPanel;
import gov.nasa.worldwind.render.airspaces.AirspaceAttributes;
import gov.nasa.worldwind.render.airspaces.BasicAirspaceAttributes;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.LayoutStyle.ComponentPlacement;
import java.awt.Color;
import javax.swing.ImageIcon;
import javax.swing.JSlider;
import javax.swing.JSpinner;
/**
 * Dialog for modifying attributes of an Airspace
 * @author Bruno Spyckerelle
 * @version 0.1.0
 */
public class AirspaceAttributesDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private SimpleGLCanvas wwd;

	/**
	 * Create the dialog.
	 */
	public AirspaceAttributesDialog(AirspaceAttributes attrs) {
		super();
		setBounds(100, 100, 600, 309);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.X_AXIS));
		{
			JPanel attrsPanel = new JPanel();
			contentPanel.add(attrsPanel);
			JLabel lblCouleurDuContour = new JLabel("Couleur : ");
			
			JLabel contourColorLbl = new JLabel("          ");
			contourColorLbl.setBackground(Color.RED);
			contourColorLbl.setOpaque(true);
			
			JButton colorContourBtn = new JButton(" ");
			colorContourBtn.setIcon(new ImageIcon(AirspaceAttributesDialog.class.getResource("/resources/fill-color.png")));
			
			JLabel lblOpacitContour = new JLabel("Opacité : ");
			
			JPanel titleContour = new TitledPanel("Contour");
			titleContour.setBorder(null);
			
			JSlider opaciteContour = new JSlider();
			
			JLabel lblEpaisseur = new JLabel("Epaisseur : ");
			
			JSpinner spinner = new JSpinner();
			
			TitledPanel panel = new TitledPanel("Intérieur");
			
			JLabel colorInterieurLbl = new JLabel("Couleur : ");
			
			JLabel label_1 = new JLabel("Opacité : ");
			
			JSlider opaciteInterieur = new JSlider();
			
			JLabel label_2 = new JLabel("          ");
			label_2.setOpaque(true);
			label_2.setBackground(Color.RED);
			
			JButton colorInterieurBtn = new JButton(" ");
			colorInterieurBtn.setIcon(new ImageIcon(AirspaceAttributesDialog.class.getResource("/resources/fill-color.png")));
			GroupLayout gl_attrsPanel = new GroupLayout(attrsPanel);
			gl_attrsPanel.setHorizontalGroup(
				gl_attrsPanel.createParallelGroup(Alignment.LEADING)
					.addGroup(gl_attrsPanel.createSequentialGroup()
						.addContainerGap()
						.addGroup(gl_attrsPanel.createParallelGroup(Alignment.LEADING)
							.addComponent(titleContour, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 286, Short.MAX_VALUE)
							.addGroup(Alignment.TRAILING, gl_attrsPanel.createSequentialGroup()
								.addComponent(lblCouleurDuContour)
								.addPreferredGap(ComponentPlacement.RELATED, 128, Short.MAX_VALUE)
								.addComponent(contourColorLbl)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(colorContourBtn))
							.addGroup(Alignment.TRAILING, gl_attrsPanel.createSequentialGroup()
								.addGroup(gl_attrsPanel.createParallelGroup(Alignment.LEADING)
									.addComponent(lblOpacitContour)
									.addComponent(lblEpaisseur))
								.addPreferredGap(ComponentPlacement.RELATED, 12, Short.MAX_VALUE)
								.addGroup(gl_attrsPanel.createParallelGroup(Alignment.LEADING)
									.addComponent(spinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
									.addComponent(opaciteContour, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
							.addComponent(panel, GroupLayout.PREFERRED_SIZE, 286, GroupLayout.PREFERRED_SIZE)
							.addGroup(gl_attrsPanel.createSequentialGroup()
								.addComponent(colorInterieurLbl)
								.addGap(128)
								.addComponent(label_2, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
								.addGap(6)
								.addComponent(colorInterieurBtn, GroupLayout.PREFERRED_SIZE, 52, GroupLayout.PREFERRED_SIZE))
							.addGroup(gl_attrsPanel.createSequentialGroup()
								.addComponent(label_1)
								.addGap(26)
								.addComponent(opaciteInterieur, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
						.addContainerGap())
			);
			gl_attrsPanel.setVerticalGroup(
				gl_attrsPanel.createParallelGroup(Alignment.LEADING)
					.addGroup(gl_attrsPanel.createSequentialGroup()
						.addComponent(titleContour, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(gl_attrsPanel.createParallelGroup(Alignment.BASELINE)
							.addComponent(lblCouleurDuContour)
							.addComponent(colorContourBtn)
							.addComponent(contourColorLbl))
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(gl_attrsPanel.createParallelGroup(Alignment.LEADING)
							.addComponent(lblOpacitContour)
							.addComponent(opaciteContour, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(gl_attrsPanel.createParallelGroup(Alignment.BASELINE)
							.addComponent(lblEpaisseur)
							.addComponent(spinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addComponent(panel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(gl_attrsPanel.createParallelGroup(Alignment.LEADING)
							.addGroup(gl_attrsPanel.createSequentialGroup()
								.addGap(7)
								.addComponent(colorInterieurLbl))
							.addGroup(gl_attrsPanel.createSequentialGroup()
								.addGap(7)
								.addComponent(label_2))
							.addComponent(colorInterieurBtn, GroupLayout.PREFERRED_SIZE, 28, GroupLayout.PREFERRED_SIZE))
						.addGap(6)
						.addGroup(gl_attrsPanel.createParallelGroup(Alignment.LEADING)
							.addComponent(label_1)
							.addComponent(opaciteInterieur, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addContainerGap(157, Short.MAX_VALUE))
			);
			attrsPanel.setLayout(gl_attrsPanel);
		}
		{
			JPanel wwdPanel = new JPanel();
			contentPanel.add(wwdPanel);
			wwdPanel.setLayout(new BorderLayout(0, 0));
			wwd = new SimpleGLCanvas();
			wwd.addSamplePolygon(new BasicAirspaceAttributes());
            wwd.setPreferredSize(new java.awt.Dimension(200, 200));
            wwdPanel.add(wwd, BorderLayout.CENTER);
		}
		

		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Annuler");
				cancelButton.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						dispose();
					}
				});
				buttonPane.add(cancelButton);
			}
		}
	}
}
