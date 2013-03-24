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

import javax.swing.DefaultComboBoxModel;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JComboBox;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.JButton;

import fr.crnan.videso3d.Configuration;
import fr.crnan.videso3d.ihm.components.TitledPanel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
/**
 * 
 * @author Adrien Vidal
 * @version 0.1.0
 */
public class SVNRepositoryChoiceUI extends JDialog implements ActionListener {
	private JComboBox<String> repoCombobox;
	private JButton okButton;
	private JButton cancelButton;
	/**
	 * La clé de la hashmap est l'url, la valeur est la chaîne de caractères "url;id;password"
	 */
	private HashMap<String,String> svnRepositoryMap = new HashMap<String, String>();
	
	private int result = JOptionPane.CANCEL_OPTION;
	
	public SVNRepositoryChoiceUI() {
		super();

		this.setModal(true);
		
		getRepositories();
		setTitle("Choix du dépôt SVN");
		
		getContentPane().add(new TitledPanel("Choix du dépot à parcourir : "), BorderLayout.NORTH);
		
		JPanel contentPanel = new JPanel();
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		
		JLabel repoChoiceLabel = new JLabel("Choisir un dépôt : ");
		
		repoCombobox = new JComboBox<String>();
		DefaultComboBoxModel<String> comboboxModel = new DefaultComboBoxModel<String>(svnRepositoryMap.keySet().toArray(new String[0]));
		repoCombobox.setModel(comboboxModel);
		repoCombobox.addActionListener(this);
		
		cancelButton = new JButton("Annuler");
		cancelButton.addActionListener(this);
		
		okButton = new JButton("OK");
		okButton.addActionListener(this);
		
		GroupLayout gl_contentPanel = new GroupLayout(contentPanel);
		gl_contentPanel.setHorizontalGroup(
			gl_contentPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPanel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_contentPanel.createSequentialGroup()
							.addComponent(repoChoiceLabel)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(repoCombobox, 0, 278, Short.MAX_VALUE))
						.addGroup(Alignment.TRAILING, gl_contentPanel.createSequentialGroup()
							.addComponent(okButton)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(cancelButton)))
					.addContainerGap())
		);
		gl_contentPanel.setVerticalGroup(
			gl_contentPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPanel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(repoChoiceLabel)
						.addComponent(repoCombobox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(cancelButton)
						.addComponent(okButton))
					.addContainerGap(74, Short.MAX_VALUE))
		);
		contentPanel.setLayout(gl_contentPanel);
		
		this.setPreferredSize(new Dimension(400, 140));
		
		this.getRootPane().setDefaultButton(okButton);
		this.pack();

	}
	
	
	private void getRepositories() {
		String[] svnRepositories = Configuration.getProperty(Configuration.SVN_REPOSITORIES, "").split("#");
		for(String svnRepo : svnRepositories){
			this.svnRepositoryMap.put(svnRepo.split(";")[0], svnRepo);
		}
	}

	public int showDialog(Component parent){
		this.setLocationRelativeTo(parent);
		this.setVisible(true);
		return result;
	}

	public String getSelectedRepo(){
		return svnRepositoryMap.get(repoCombobox.getSelectedItem());
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource().equals(okButton)){
			this.result = JOptionPane.OK_OPTION;
			this.setVisible(false);
		}else if(e.getSource().equals(cancelButton)){
			this.result = JOptionPane.CANCEL_OPTION;
			this.setVisible(false);
		}
	}
}
