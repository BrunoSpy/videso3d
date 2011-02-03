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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import fr.crnan.videso3d.formats.fpl.FPLReader;

/**
 * Fenêtre permettant à l'utilisateur de taper son plan de vol ou de le rentrer par copier/coller. Accepte les plans de vol au format IvanWeb et les 
 * plans de vol non formatés (suite de balises, de routes et de points géographiques). Voir {@link FPLReader}.
 * N'accepte qu'un seul plan de vol à la fois.
 * @author Adrien Vidal
 *
 */
public class FPLImportUI extends JFrame implements ActionListener{

	private JButton importButton, cancelButton;
	private JTextArea fplArea;
	private JTextField indiTextField;
	private DataExplorer dataExplorer;
	
	public FPLImportUI(DataExplorer dataExplorer){
		this.dataExplorer = dataExplorer;
		this.setTitle("Importer un plan de vol...");
		
		JLabel indicatif = new JLabel("Indicatif (facultatif)");
		indiTextField = new JTextField(10);
		indiTextField.setText("?");
		JPanel indiPane = new JPanel();
		indiPane.setLayout(new FlowLayout(FlowLayout.LEADING));
		indiPane.add(indicatif);
		indiPane.add(indiTextField);
		JLabel enterFPL = new JLabel("Rentrez votre plan de vol ci-dessous : ");
		JPanel enterFPLPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
		enterFPLPanel.add(enterFPL);
		fplArea = new JTextArea(10,80);
		JPanel fplPanel = new JPanel();
		fplPanel.setLayout(new BoxLayout(fplPanel, BoxLayout.PAGE_AXIS));
		fplPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		fplPanel.add(indiPane);
		fplPanel.add(enterFPLPanel);
		fplPanel.add(fplArea);
		
		importButton = new JButton("Importer");
		cancelButton = new JButton("Annuler");
		importButton.addActionListener(this);
		cancelButton.addActionListener(this);
		
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
		buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		buttonPane.add(Box.createHorizontalGlue());
		buttonPane.add(cancelButton);
		buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
		buttonPane.add(importButton);

		this.getRootPane().setDefaultButton(importButton);
		
		this.getContentPane().add(fplPanel, BorderLayout.CENTER);
		this.getContentPane().add(buttonPane, BorderLayout.PAGE_END);
		this.pack();
		this.setLocation(100, 50);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource()==importButton){
			String text = fplArea.getText();
			LinkedList<String> fpl = new LinkedList<String>();
			for(String s : text.split("\\n")){
				if(s.equals(""))
					break;
				fpl.add(s);
			}
			if(fpl.size()>0){
				if(!fpl.getFirst().startsWith("(FPL")){
					fpl.set(0, "(FPL "+fpl.getFirst());
					fpl.getLast().concat(")");
				}
				FPLReader fplR = new FPLReader();
				String indicatif = indiTextField.getText();
				if (indicatif.equals("?")) 
					fplR.setName(FPLReader.getIndicatif(fpl));
				else
					fplR.setName(indicatif);
				fplR.parseFPL(fpl, indicatif);
				dataExplorer.addTrajectoriesView(fplR);
				setVisible(false);
				dispose();
			}
		}else if(e.getSource()==cancelButton){
			dispose();
		}
	}

	
}
