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
import fr.crnan.videso3d.ihm.components.EpaisseurSpinner;
import fr.crnan.videso3d.ihm.components.JColorButton;
import fr.crnan.videso3d.ihm.components.OpacitySlider;
import fr.crnan.videso3d.ihm.components.TitledPanel;
import gov.nasa.worldwind.render.airspaces.AirspaceAttributes;
import gov.nasa.worldwind.render.airspaces.BasicAirspaceAttributes;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import java.awt.GridLayout;
/**
 * Dialog for modifying attributes of an Airspace
 * @author Bruno Spyckerelle
 * @version 0.1.0
 */
public class AirspaceAttributesDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private SimpleGLCanvas wwd;
	private JPanel attrsPanel;
	private JSlider opaciteContour;
	private JSpinner epaisseurContour;
	private JSlider opaciteInterieur;
	private JLabel couleurInterieur;
	private JButton interieurColorBtn;
	private JSlider opaciteContourSurligne;
	private JSpinner epaisseurContourSurligne;
	private JSlider opaciteInterieurSurligne;
	private JLabel couleurInterieurSurligne;
	private JButton couleurInterieurSurligneBtn;
	private JLabel couleurContourSurligne;
	private AbstractButton couleurContourSurligneBtn;
	private JLabel couleurContour;
	private AbstractButton contourColorBtn;

	/**
	 * Create the dialog.
	 */
	public AirspaceAttributesDialog(final AirspaceAttributes attrsN, final AirspaceAttributes attrsH) {
		super();
		
		this.setModalityType(DEFAULT_MODALITY_TYPE);
		
		wwd = new SimpleGLCanvas();
		
		final AirspaceAttributes attrsNormal = new BasicAirspaceAttributes(attrsN);
		final AirspaceAttributes attrsHighlight = new BasicAirspaceAttributes(attrsH);
		
		setBounds(100, 100, 851, 545);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new GridLayout(0, 2, 0, 0));

		attrsPanel = new JPanel();
		contentPanel.add(attrsPanel);
		
		couleurContour = new JLabel("          ");
		contourColorBtn = new JColorButton(wwd, couleurContour, attrsNormal, false);		
		
		couleurContour.setOpaque(true);
		couleurContour.setBackground(attrsNormal.getOutlineMaterial().getDiffuse());

		opaciteContour = new OpacitySlider(wwd, attrsNormal, false);
		epaisseurContour = new EpaisseurSpinner(wwd, attrsNormal);
		epaisseurContour.setValue(attrsNormal.getOutlineWidth());
		
		couleurInterieur = new JLabel("          ");
		interieurColorBtn = new JColorButton(wwd, couleurInterieur, attrsNormal, true);
		
		couleurInterieur.setBackground(attrsNormal.getMaterial().getDiffuse());
		couleurInterieur.setOpaque(true);

		opaciteInterieur = new OpacitySlider(wwd, attrsNormal, true);
		
		couleurContourSurligne = new JLabel("          ");
		couleurContourSurligneBtn = new JColorButton(wwd, couleurContourSurligne, attrsHighlight, false);
		
		couleurContourSurligne.setBackground(attrsHighlight.getOutlineMaterial().getDiffuse());
		couleurContourSurligne.setOpaque(true);
		
		opaciteContourSurligne = new OpacitySlider(wwd, attrsHighlight, false);

		epaisseurContourSurligne = new EpaisseurSpinner(wwd, attrsHighlight);
		
		couleurInterieurSurligne = new JLabel("          ");
		couleurInterieurSurligneBtn = new JColorButton(wwd, couleurInterieurSurligne, attrsHighlight, true);

		couleurInterieurSurligne.setOpaque(true);
		couleurInterieurSurligne.setBackground(attrsHighlight.getMaterial().getDiffuse());

		opaciteInterieurSurligne = new OpacitySlider(wwd, attrsHighlight, true);


		JPanel wwdPanel = new JPanel();
		contentPanel.add(wwdPanel);
		wwdPanel.setLayout(new BorderLayout(0, 0));
		wwd.addSamplePolygon(attrsNormal, attrsHighlight);
		wwd.setPreferredSize(new java.awt.Dimension(200, 200));
		wwdPanel.add(wwd, BorderLayout.CENTER);




		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);

		JButton okButton = new JButton("Appliquer");
		okButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				attrsN.setMaterial(attrsNormal.getMaterial());				
				attrsN.setOutlineMaterial(attrsNormal.getOutlineMaterial());				
				attrsN.setOpacity(attrsNormal.getOpacity());				
				attrsN.setOutlineOpacity(attrsNormal.getOutlineOpacity());				
				attrsN.setOutlineWidth(attrsNormal.getOutlineWidth());
				attrsH.setMaterial(attrsHighlight.getMaterial());
				attrsH.setOutlineMaterial(attrsHighlight.getOutlineMaterial());				
				attrsH.setOpacity(attrsHighlight.getOpacity());				
				attrsH.setOutlineOpacity(attrsHighlight.getOutlineOpacity());				
				attrsH.setOutlineWidth(attrsHighlight.getOutlineWidth());
				
				dispose();
			}
		});
		buttonPane.add(okButton);
		getRootPane().setDefaultButton(okButton);


		JButton cancelButton = new JButton("Annuler");
		cancelButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		buttonPane.add(cancelButton);


		this.applyLayout();
	}
	
	private void applyLayout(){
		
		JPanel contourTitle = new TitledPanel("Contour");
		JLabel lblCouleurContour = new JLabel("Couleur : ");
		JLabel lblOpacit = new JLabel("Opacité : ");
		JLabel lblEpaisseur = new JLabel("Epaisseur : ");
		TitledPanel interieurTitle = new TitledPanel("Intérieur");
		JLabel lblInterieurCouleur = new JLabel("Couleur : ");
		JLabel lblOpaciteInterieur = new JLabel("Opacité : ");
		TitledPanel contourSurligneTitle = new TitledPanel("Contour surligné");
		JLabel lblCouleurContourSurligne = new JLabel("Couleur : ");
		JLabel lblOpaciteContourSurligne = new JLabel("Opacité : ");
		JLabel lblEpaisseurContourSurligne = new JLabel("Epaisseur : ");
		TitledPanel interieurSurligneTitle = new TitledPanel("Intérieur surligné");
		JLabel lblCouleurInterieurSurligne = new JLabel("Couleur : ");
		JLabel lblOpaciteInterieurSurligne = new JLabel("Opacité : ");
		
		GroupLayout gl_attrsPanel = new GroupLayout(attrsPanel);
		gl_attrsPanel.setHorizontalGroup(
				gl_attrsPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_attrsPanel.createSequentialGroup()
						.addGroup(gl_attrsPanel.createParallelGroup(Alignment.LEADING)
								.addComponent(contourTitle, GroupLayout.DEFAULT_SIZE, 343, Short.MAX_VALUE)
								.addGroup(gl_attrsPanel.createSequentialGroup()
										.addComponent(lblCouleurContour)
										.addPreferredGap(ComponentPlacement.RELATED, 203, Short.MAX_VALUE)
										.addComponent(couleurContour)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(contourColorBtn))
										.addGroup(gl_attrsPanel.createSequentialGroup()
												.addGroup(gl_attrsPanel.createParallelGroup(Alignment.LEADING)
														.addComponent(lblOpacit)
														.addComponent(lblEpaisseur))
														.addPreferredGap(ComponentPlacement.RELATED)
														.addGroup(gl_attrsPanel.createParallelGroup(Alignment.LEADING)
																.addComponent(epaisseurContour, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
																.addComponent(opaciteContour, GroupLayout.DEFAULT_SIZE, 271, Short.MAX_VALUE)))
																.addComponent(interieurTitle, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 343, Short.MAX_VALUE)
																.addGroup(gl_attrsPanel.createSequentialGroup()
																		.addComponent(lblInterieurCouleur)
																		.addPreferredGap(ComponentPlacement.RELATED, 203, Short.MAX_VALUE)
																		.addComponent(couleurInterieur)
																		.addPreferredGap(ComponentPlacement.RELATED)
																		.addComponent(interieurColorBtn))
																		.addGroup(gl_attrsPanel.createSequentialGroup()
																				.addComponent(lblOpaciteInterieur)
																				.addGap(18)
																				.addComponent(opaciteInterieur, GroupLayout.DEFAULT_SIZE, 274, Short.MAX_VALUE))
																				.addComponent(contourSurligneTitle, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 343, Short.MAX_VALUE)
																				.addGroup(gl_attrsPanel.createSequentialGroup()
																						.addComponent(lblCouleurContourSurligne)
																						.addPreferredGap(ComponentPlacement.RELATED, 203, Short.MAX_VALUE)
																						.addComponent(couleurContourSurligne)
																						.addPreferredGap(ComponentPlacement.RELATED)
																						.addComponent(couleurContourSurligneBtn))
																						.addGroup(gl_attrsPanel.createSequentialGroup()
																								.addGroup(gl_attrsPanel.createParallelGroup(Alignment.LEADING)
																										.addComponent(lblOpaciteContourSurligne)
																										.addComponent(lblEpaisseurContourSurligne))
																										.addPreferredGap(ComponentPlacement.RELATED)
																										.addGroup(gl_attrsPanel.createParallelGroup(Alignment.LEADING)
																												.addComponent(epaisseurContourSurligne, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
																												.addComponent(opaciteContourSurligne, GroupLayout.DEFAULT_SIZE, 271, Short.MAX_VALUE)))
																												.addComponent(interieurSurligneTitle, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 349, Short.MAX_VALUE)
																												.addGroup(gl_attrsPanel.createSequentialGroup()
																														.addComponent(lblCouleurInterieurSurligne)
																														.addPreferredGap(ComponentPlacement.RELATED, 178, Short.MAX_VALUE)
																														.addComponent(couleurInterieurSurligne)
																														.addPreferredGap(ComponentPlacement.RELATED)
																														.addComponent(couleurInterieurSurligneBtn))
																														.addGroup(gl_attrsPanel.createSequentialGroup()
																																.addComponent(lblOpaciteInterieurSurligne)
																																.addGap(18)
																																.addComponent(opaciteInterieurSurligne, GroupLayout.DEFAULT_SIZE, 274, Short.MAX_VALUE)))
																																.addContainerGap())
				);
		gl_attrsPanel.setVerticalGroup(
				gl_attrsPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_attrsPanel.createSequentialGroup()
						.addComponent(contourTitle, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(gl_attrsPanel.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblCouleurContour)
								.addComponent(contourColorBtn)
								.addComponent(couleurContour))
								.addPreferredGap(ComponentPlacement.RELATED)
								.addGroup(gl_attrsPanel.createParallelGroup(Alignment.LEADING)
										.addComponent(lblOpacit)
										.addComponent(opaciteContour, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(ComponentPlacement.RELATED)
										.addGroup(gl_attrsPanel.createParallelGroup(Alignment.BASELINE)
												.addComponent(lblEpaisseur)
												.addComponent(epaisseurContour, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
												.addPreferredGap(ComponentPlacement.RELATED)
												.addComponent(interieurTitle, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)
												.addPreferredGap(ComponentPlacement.RELATED)
												.addGroup(gl_attrsPanel.createParallelGroup(Alignment.BASELINE)
														.addComponent(lblInterieurCouleur)
														.addComponent(interieurColorBtn)
														.addComponent(couleurInterieur))
														.addPreferredGap(ComponentPlacement.RELATED)
														.addGroup(gl_attrsPanel.createParallelGroup(Alignment.LEADING)
																.addComponent(lblOpaciteInterieur)
																.addComponent(opaciteInterieur, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
																.addPreferredGap(ComponentPlacement.RELATED)
																.addComponent(contourSurligneTitle, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)
																.addPreferredGap(ComponentPlacement.RELATED)
																.addGroup(gl_attrsPanel.createParallelGroup(Alignment.BASELINE)
																		.addComponent(lblCouleurContourSurligne)
																		.addComponent(couleurContourSurligneBtn)
																		.addComponent(couleurContourSurligne))
																		.addPreferredGap(ComponentPlacement.RELATED)
																		.addGroup(gl_attrsPanel.createParallelGroup(Alignment.LEADING)
																				.addComponent(lblOpaciteContourSurligne)
																				.addComponent(opaciteContourSurligne, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
																				.addPreferredGap(ComponentPlacement.RELATED)
																				.addGroup(gl_attrsPanel.createParallelGroup(Alignment.BASELINE)
																						.addComponent(lblEpaisseurContourSurligne)
																						.addComponent(epaisseurContourSurligne, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
																						.addPreferredGap(ComponentPlacement.RELATED)
																						.addComponent(interieurSurligneTitle, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)
																						.addPreferredGap(ComponentPlacement.RELATED)
																						.addGroup(gl_attrsPanel.createParallelGroup(Alignment.BASELINE)
																								.addComponent(lblCouleurInterieurSurligne)
																								.addComponent(couleurInterieurSurligneBtn)
																								.addComponent(couleurInterieurSurligne))
																								.addPreferredGap(ComponentPlacement.RELATED)
																								.addGroup(gl_attrsPanel.createParallelGroup(Alignment.LEADING)
																										.addComponent(lblOpaciteInterieurSurligne)
																										.addComponent(opaciteInterieurSurligne, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
																										.addContainerGap(20, Short.MAX_VALUE))
				);
		attrsPanel.setLayout(gl_attrsPanel);
	}
}
