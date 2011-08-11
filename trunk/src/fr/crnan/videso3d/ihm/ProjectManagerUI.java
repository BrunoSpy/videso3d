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
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.zip.ZipException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import fr.crnan.videso3d.DatabaseManager.Type;
import fr.crnan.videso3d.ProjectManager;
import fr.crnan.videso3d.ihm.components.TitledPanel;
import fr.crnan.videso3d.ihm.components.VFileChooser;
import gov.nasa.worldwind.util.Logging;

/**
 * IHM to choose which objects to save
 * @author Bruno Spyckerelle
 * @version 0.1.0
 */
public class ProjectManagerUI extends JDialog {
	
	private ProjectManager projectManager;
	
	public ProjectManagerUI(Window parent){
		super(parent);
			
		projectManager = new ProjectManager();
		projectManager.prepareSaving();

		if(projectManager.getTypes().isEmpty()){
			error();
		} else {

			this.setTitle("Création d'un fichier projet");

			this.setMinimumSize(new Dimension(300, 100));
			
			this.setLayout(new BorderLayout());

			this.build();	

			this.pack();

			this.setModal(true);
			
			this.setLocationRelativeTo(parent);
			this.setLocation(this.getLocation().x-this.getWidth()/2, this.getLocation().y-this.getHeight()/2);
		
			this.setVisible(true);
		}
	}

	private void error(){
		JOptionPane.showMessageDialog(this, "<html><b>Problème :</b><br />Aucun objet à enregistrer, le projet est vide.<br /><br />" +
				"<b>Solution :</b><br />Vérifiez que des objets sont affichés.</html>",
				"Erreur", JOptionPane.ERROR_MESSAGE);
		this.setVisible(false);
	}
	
	private void build() {
		
		this.add(new TitledPanel("Création d'un fichier projet"), BorderLayout.NORTH);
		
		Box content = Box.createVerticalBox();
		
		JPanel list = new JPanel();
		list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
		list.setBorder(BorderFactory.createTitledBorder("1. Choisir les données à exporter :"));
		for(Type type : this.projectManager.getTypes()){
			Box element = Box.createHorizontalBox();
			JCheckBox checkbox = new JCheckBox(type.toString());
			element.add(checkbox);
			element.add(Box.createHorizontalGlue());
			list.add(element);
		}
		content.add(list);
		
		JPanel optionsPanel = new JPanel();
		optionsPanel.setBorder(BorderFactory.createTitledBorder("2. Options"));
		optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
		Box databasesBox = Box.createHorizontalBox();
		final JCheckBox databases = new JCheckBox("Enregistrer les bases de données");
		databasesBox.add(databases);
		databasesBox.add(Box.createHorizontalGlue());
		optionsPanel.add(databasesBox);
		content.add(optionsPanel);
		
		JPanel filePanel = new JPanel();
		filePanel.setBorder(BorderFactory.createTitledBorder("3. Choisir l'emplacement du fichier"));
		filePanel.setLayout(new BoxLayout(filePanel, BoxLayout.X_AXIS));
		final JTextField filePath = new JTextField();
		filePath.setToolTipText("Chemin complet vers le fichier");
		filePath.setColumns(30);
		filePanel.add(filePath);
		filePanel.add(Box.createHorizontalGlue());
		JButton fileChooserButton = new JButton(new ImageIcon(getClass().getResource("/resources/load_project_22.png")));
		fileChooserButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				VFileChooser fileChooser = new VFileChooser();
				if(fileChooser.showDialog(getThis(), "Sélectionner") == JFileChooser.APPROVE_OPTION){
					final File file = fileChooser.getSelectedFile();
					if(!(file.exists()) || 
							(file.exists() &&
									JOptionPane.showConfirmDialog(null, "Le fichier existe déjà.\n\nSouhaitez-vous réellement l'écraser ?",
											"Confirmer la suppression du fichier précédent",
											JOptionPane.OK_CANCEL_OPTION,
											JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION)) {
						String path = file.getAbsolutePath();
						if(!path.toLowerCase().endsWith(".vpj"))
							path += ".vpj";
						filePath.setText(path);
					}
				}
			}
		});
		filePanel.add(fileChooserButton);
		content.add(filePanel);
		
		content.add(Box.createVerticalGlue());
		
		this.add(content, BorderLayout.CENTER);
		
		JPanel bottom = new JPanel();
		bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
		bottom.add(Box.createHorizontalGlue());
		
		JButton save = new JButton("Enregistrer");
		save.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					projectManager.saveProject(new File(filePath.getText()), databases.isSelected());
					getThis().dispose();
				} catch(ZipException e) {
					JOptionPane.showMessageDialog(null, "Aucun fichier projet sauvé, vérifiez qu'il y a bien des objets à sauver.",
							"Impossible de créer un fichier projet", JOptionPane.ERROR_MESSAGE);
					Logging.logger().warning("Impossible de créer un fichier projet");
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		});
		bottom.add(save);
		
		JButton cancel = new JButton("Annuler");
		cancel.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				getThis().setVisible(false);
				getThis().dispose();
			}
		});
		bottom.add(cancel);
		
		this.add(bottom, BorderLayout.SOUTH);
	}
	
	private JDialog getThis(){
		return this;
	}
}
