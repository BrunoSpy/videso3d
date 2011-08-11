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
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import fr.crnan.videso3d.SimpleGLCanvasFactory;
import fr.crnan.videso3d.ihm.components.EpaisseurSpinner;
import fr.crnan.videso3d.ihm.components.JColorButton;
import fr.crnan.videso3d.ihm.components.OpacitySlider;
import fr.crnan.videso3d.ihm.components.TitledPanel;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.ShapeAttributes;

import javax.swing.AbstractButton;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JTextField;
/**
 * Dialog to change attributes of a shape
 * @author Bruno Spyckerelle
 * @version 0.1.0
 */
public class ShapeAttributesDialog extends JDialog {
	private final JPanel contentPanel = new JPanel();
	private WorldWindowGLCanvas wwd;
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
	private JTextField pointille;
	private JTextField pointilleSurligne;
	private JSpinner repetitionSurligne;
	private JSpinner repetition;

	/**
	 * Create the dialog.
	 */
	public ShapeAttributesDialog(final ShapeAttributes attrsN, final ShapeAttributes attrsH) {
		super();
		
		this.setModalityType(DEFAULT_MODALITY_TYPE);
		
		final ShapeAttributes attrsNormal = new BasicShapeAttributes(attrsN);
		final ShapeAttributes attrsHighlight = new BasicShapeAttributes(attrsH);
		
		wwd = SimpleGLCanvasFactory.SimpleGLCanvasPolygonShape(attrsNormal, attrsHighlight);
		
		setBounds(100, 100, 851, 757);
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
		
		pointille = new JTextField();
		pointille.setText(String.format("%x", (short)attrsNormal.getOutlineStipplePattern()));
		pointille.setColumns(4);
		pointille.setToolTipText("Specifies a 16-bit integer that defines which pixels are rendered in the shape's outline.");
		pointille.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				attrsNormal.setOutlineStipplePattern(Integer.decode("0x"+pointille.getText()).shortValue());
				wwd.redraw();
			}
		});
		repetition = new JSpinner(new SpinnerNumberModel(attrsNormal.getOutlineStippleFactor(), 0, 10, 1));
		repetition.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				attrsNormal.setOutlineStippleFactor((Integer)repetition.getValue());
				wwd.redraw();
			}
		});
		couleurInterieur = new JLabel("          ");
		interieurColorBtn = new JColorButton(wwd, couleurInterieur, attrsNormal, true);
		couleurInterieur.setOpaque(true);
		
		couleurInterieur.setBackground(attrsNormal.getInteriorMaterial().getDiffuse());
		

		opaciteInterieur = new OpacitySlider(wwd, attrsNormal, true);
		
		couleurContourSurligne = new JLabel("          ");
		couleurContourSurligneBtn = new JColorButton(wwd, couleurContourSurligne, attrsHighlight, false);
		
		couleurContourSurligne.setBackground(attrsHighlight.getOutlineMaterial().getDiffuse());
		couleurContourSurligne.setOpaque(true);
		
		opaciteContourSurligne = new OpacitySlider(wwd, attrsHighlight, false);
		pointilleSurligne = new JTextField();
		pointilleSurligne.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				attrsHighlight.setOutlineStipplePattern(Integer.decode("0x"+pointilleSurligne.getText()).shortValue());
				wwd.redraw();
			}
		});
		pointilleSurligne.setToolTipText("Specifies a 16-bit integer that defines which pixels are rendered in the shape's outline.");
		pointilleSurligne.setColumns(4);
		pointilleSurligne.setText(String.format("%x",(short)attrsHighlight.getOutlineStipplePattern()));
		repetitionSurligne = new JSpinner(new SpinnerNumberModel(attrsHighlight.getOutlineStippleFactor(), 0, 10, 1));
		repetitionSurligne.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				attrsHighlight.setOutlineStippleFactor((Integer)repetitionSurligne.getValue());
				wwd.redraw();
			}
		});
		epaisseurContourSurligne = new EpaisseurSpinner(wwd, attrsHighlight);
		
		couleurInterieurSurligne = new JLabel("          ");
		couleurInterieurSurligneBtn = new JColorButton(wwd, couleurInterieurSurligne, attrsHighlight, true);

		couleurInterieurSurligne.setOpaque(true);
		couleurInterieurSurligne.setBackground(attrsHighlight.getInteriorMaterial().getDiffuse());

		opaciteInterieurSurligne = new OpacitySlider(wwd, attrsHighlight, true);


		JPanel wwdPanel = new JPanel();
		contentPanel.add(wwdPanel);
		wwdPanel.setLayout(new BorderLayout(0, 0));
		wwd.setPreferredSize(new java.awt.Dimension(200, 200));
		wwdPanel.add(wwd, BorderLayout.CENTER);




		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);

		JButton okButton = new JButton("Appliquer");
		okButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				attrsN.setInteriorMaterial(attrsNormal.getInteriorMaterial());				
				attrsN.setOutlineMaterial(attrsNormal.getOutlineMaterial());				
				attrsN.setInteriorOpacity(attrsNormal.getInteriorOpacity());				
				attrsN.setOutlineOpacity(attrsNormal.getOutlineOpacity());				
				attrsN.setOutlineWidth(attrsNormal.getOutlineWidth());
				attrsN.setDrawOutline(attrsN.isDrawOutline());
				attrsH.setInteriorMaterial(attrsHighlight.getInteriorMaterial());
				attrsH.setOutlineMaterial(attrsHighlight.getOutlineMaterial());				
				attrsH.setInteriorOpacity(attrsHighlight.getInteriorOpacity());				
				attrsH.setOutlineOpacity(attrsHighlight.getOutlineOpacity());				
				attrsH.setOutlineWidth(attrsHighlight.getOutlineWidth());
				attrsH.setDrawOutline(attrsHighlight.isDrawOutline());
				wwd.shutdown();
				dispose();
			}
		});
		buttonPane.add(okButton);
		getRootPane().setDefaultButton(okButton);


		JButton cancelButton = new JButton("Annuler");
		cancelButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				wwd.shutdown();
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
		JLabel lblPointille = new JLabel("Pointillés : ");		
		JLabel lblRepetition = new JLabel("Répétition : ");		
		JLabel lblPointilleSurligne = new JLabel("Pointillés : ");		
		JLabel lblRepetitionSurligne = new JLabel("Répétition : ");
		
		
		GroupLayout gl_attrsPanel = new GroupLayout(attrsPanel);
		gl_attrsPanel.setHorizontalGroup(
			gl_attrsPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_attrsPanel.createSequentialGroup()
					.addGroup(gl_attrsPanel.createParallelGroup(Alignment.TRAILING)
						.addComponent(contourTitle, GroupLayout.DEFAULT_SIZE, 410, Short.MAX_VALUE)
						.addGroup(gl_attrsPanel.createSequentialGroup()
							.addComponent(lblCouleurContour)
							.addPreferredGap(ComponentPlacement.RELATED, 270, Short.MAX_VALUE)
							.addComponent(couleurContour)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(contourColorBtn, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addComponent(interieurTitle, GroupLayout.DEFAULT_SIZE, 410, Short.MAX_VALUE)
						.addGroup(gl_attrsPanel.createSequentialGroup()
							.addComponent(lblInterieurCouleur)
							.addPreferredGap(ComponentPlacement.RELATED, 270, Short.MAX_VALUE)
							.addComponent(couleurInterieur)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(interieurColorBtn, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGroup(gl_attrsPanel.createSequentialGroup()
							.addComponent(lblOpaciteInterieur)
							.addGap(18)
							.addComponent(opaciteInterieur, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
						.addComponent(contourSurligneTitle, GroupLayout.DEFAULT_SIZE, 410, Short.MAX_VALUE)
						.addGroup(gl_attrsPanel.createSequentialGroup()
							.addComponent(lblCouleurContourSurligne)
							.addPreferredGap(ComponentPlacement.RELATED, 270, Short.MAX_VALUE)
							.addComponent(couleurContourSurligne)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(couleurContourSurligneBtn, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGroup(gl_attrsPanel.createSequentialGroup()
							.addGroup(gl_attrsPanel.createParallelGroup(Alignment.LEADING)
								.addComponent(lblOpaciteContourSurligne)
								.addComponent(lblEpaisseurContourSurligne)
								.addComponent(lblPointilleSurligne))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(gl_attrsPanel.createParallelGroup(Alignment.LEADING)
								.addComponent(opaciteContourSurligne, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addGroup(gl_attrsPanel.createSequentialGroup()
									.addGroup(gl_attrsPanel.createParallelGroup(Alignment.TRAILING, false)
										.addComponent(pointilleSurligne, Alignment.LEADING, 0, 0, Short.MAX_VALUE)
										.addComponent(epaisseurContourSurligne, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(lblRepetitionSurligne)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(repetitionSurligne, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))))
						.addComponent(interieurSurligneTitle, GroupLayout.DEFAULT_SIZE, 410, Short.MAX_VALUE)
						.addGroup(gl_attrsPanel.createSequentialGroup()
							.addComponent(lblCouleurInterieurSurligne)
							.addPreferredGap(ComponentPlacement.RELATED, 306, Short.MAX_VALUE)
							.addGroup(gl_attrsPanel.createParallelGroup(Alignment.TRAILING)
								.addComponent(couleurInterieurSurligne)
								.addComponent(couleurInterieurSurligneBtn, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
						.addGroup(gl_attrsPanel.createSequentialGroup()
							.addGroup(gl_attrsPanel.createParallelGroup(Alignment.LEADING)
								.addComponent(lblOpacit)
								.addComponent(lblEpaisseur)
								.addComponent(lblPointille))
							.addGroup(gl_attrsPanel.createParallelGroup(Alignment.LEADING)
								.addComponent(opaciteContour, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addGroup(gl_attrsPanel.createSequentialGroup()
									.addGroup(gl_attrsPanel.createParallelGroup(Alignment.TRAILING, false)
										.addComponent(pointille, Alignment.LEADING, 0, 0, Short.MAX_VALUE)
										.addComponent(epaisseurContour, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(lblRepetition)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(repetition, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))))
						.addGroup(gl_attrsPanel.createSequentialGroup()
							.addComponent(lblOpaciteInterieurSurligne)
							.addGap(18)
							.addComponent(opaciteInterieurSurligne, GroupLayout.DEFAULT_SIZE, 341, Short.MAX_VALUE)))
					.addContainerGap())
		);
		gl_attrsPanel.setVerticalGroup(
			gl_attrsPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_attrsPanel.createSequentialGroup()
					.addComponent(contourTitle, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_attrsPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblCouleurContour)
						.addComponent(contourColorBtn, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
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
					.addGroup(gl_attrsPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(pointille, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblRepetition)
						.addComponent(repetition, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblPointille))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(interieurTitle, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_attrsPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblInterieurCouleur)
						.addComponent(interieurColorBtn, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
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
						.addComponent(couleurContourSurligneBtn, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
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
					.addGroup(gl_attrsPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(pointilleSurligne, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblRepetitionSurligne)
						.addComponent(repetitionSurligne, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblPointilleSurligne))
					.addGroup(gl_attrsPanel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_attrsPanel.createSequentialGroup()
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(interieurSurligneTitle, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(gl_attrsPanel.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblCouleurInterieurSurligne)
								.addComponent(couleurInterieurSurligneBtn, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
						.addGroup(gl_attrsPanel.createSequentialGroup()
							.addGap(20)
							.addComponent(couleurInterieurSurligne)))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_attrsPanel.createParallelGroup(Alignment.LEADING)
						.addComponent(lblOpaciteInterieurSurligne)
						.addComponent(opaciteInterieurSurligne, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addContainerGap(136, Short.MAX_VALUE))
		);
		attrsPanel.setLayout(gl_attrsPanel);
	}
}
