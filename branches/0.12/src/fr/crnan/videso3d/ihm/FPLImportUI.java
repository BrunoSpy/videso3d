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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import fr.crnan.videso3d.formats.fpl.FPLReader;
import fr.crnan.videso3d.layers.tracks.FPLTracksLayer;
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
	private MainWindow mainWindow;
	
	public FPLImportUI(MainWindow mainWindow){
		this.mainWindow = mainWindow;
		this.setTitle("Importer un plan de vol...");
		
		JLabel indicatif = new JLabel("Indicatif (facultatif)");
		indiTextField = new JTextField(10);
		indiTextField.setText("?");
		JPanel indiPane = new JPanel();
		indiPane.setLayout(new FlowLayout(FlowLayout.LEADING));
		indiPane.add(indicatif);
		indiPane.add(indiTextField);
		JLabel enterFPL = new JLabel("<html>Entrez vos plans de vol ci-dessous : <br/><i>Deux plans de vol doivent être séparés par un saut de ligne</i></html>");
		JPanel enterFPLPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
		enterFPLPanel.add(enterFPL);
		fplArea = new JTextArea(15,80);
		JPanel fplPanel = new JPanel();
		fplPanel.setLayout(new BoxLayout(fplPanel, BoxLayout.PAGE_AXIS));
		fplPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		fplPanel.add(indiPane);
		fplPanel.add(enterFPLPanel);
		JScrollPane jsp = new JScrollPane(fplArea);
		jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		fplPanel.add(jsp);
		
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
			final String text = fplArea.getText();
	
			final SwingWorker<String, Void> sw = new SwingWorker<String, Void>(){

				FPLReader fplR = new FPLReader();
				FPLTracksLayer fplTracksLayer = new FPLTracksLayer(fplR.getModel());
				@Override
				protected String doInBackground() throws Exception {
					LinkedList<String> fpl = new LinkedList<String>();
					String msgErreur = "";
					boolean newFPL = false;
					String[] lines = text.split("\\n");
					for(int i = 0; i<lines.length; i++){
						String s = lines[i];
						setProgress( (int) (  ((double)(i+1)/(double)lines.length)  *100  )  );
						if(s.matches("\\s*") && newFPL == false){
							newFPL=true;
							if(fpl.size()>0)
								msgErreur += parseFPL(fpl);
						}else if(!s.matches("\\s*")){
							if(newFPL){
								fpl = new LinkedList<String>();
								newFPL = false;
							}
							if(s.matches("\\(FPL.+")){
								if(fpl.size()>0)
									msgErreur += parseFPL(fpl);

								fpl = new LinkedList<String>();
							}
							fpl.add(s);
							if(s.matches(".+\\)\\s*")){
								newFPL = true;
								msgErreur += parseFPL(fpl);
							}
						}
					}
					if(newFPL == false && fpl.size()>0)
						msgErreur+= parseFPL(fpl);
					return msgErreur;
				}


				@Override
				protected void done(){
					String msgErreur="";
					try {
						msgErreur = get();
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (ExecutionException e) {
						e.printStackTrace();
					}
					if(!msgErreur.isEmpty()){
						JOptionPane.showMessageDialog(null, msgErreur, "Erreur lors de la lecture du plan de vol", JOptionPane.ERROR_MESSAGE);
					}
					if(fplR.getModel().getAllTracks().size()>0){
						mainWindow.addTrajectoriesView(fplR, fplTracksLayer);
					}
				}
				
				private String parseFPL(LinkedList<String> pln){
					if(pln.size()>0){
						if(!pln.getFirst().startsWith("(FPL")){
							pln.set(0, "(FPL "+pln.getFirst());
							pln.getLast().concat(")");
						}
						String indicatif = indiTextField.getText();
						if(fplR.getName().equals("?")){
							if (indicatif.equals("?")) 
								fplR.setName(FPLReader.getIndicatif(pln));
							else
								fplR.setName(indicatif);
						}
						try{
							fplR.parseFPL(pln, indicatif);
						}catch(FPLReader.UnrecognizedFPLException ex){
							return ex.getMessage()+"\n";
						}
					}
					return "";
				}
			};

			final ProgressMonitor pm = new ProgressMonitor(null, "Extraction des plans de vol...", "", 0, 100);

			sw.addPropertyChangeListener(new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent e) {
						pm.setProgress(sw.getProgress());
				}
			});
			
			sw.execute();
			pm.close();
			setVisible(false);
			dispose();


			
			
		}else if(e.getSource()==cancelButton){
			dispose();
		}

	}

	


}
