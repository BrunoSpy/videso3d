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

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import fr.crnan.videso3d.Configuration;
import fr.crnan.videso3d.ihm.components.TitledPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;

/**
 * Fenêtre d'ajout ou de modification d'un dépôt SVN
 * @author Adrien Vidal
 * @author Bruno Spyckerelle
 */
public class SVNRepositoryParamsUI extends JDialog implements ActionListener{
	private JTextField urlField;
	private JTextField idField;
	private JTextField pwdField;
	private JButton add_modButton;
	private JButton cancelButton;
	private boolean mod;
	private String oldURL;
	private JPanel repoPanel;
	private ConfigurationUI configurationUI;	
	
	/**
	 * 
	 * @param confUI
	 * @param mod true pour modifier un dépôt existant. 
	 */
	public SVNRepositoryParamsUI(ConfigurationUI confUI, boolean mod) {
		configurationUI = confUI;
		this.mod = mod;
		setTitle(mod?"Modification d'un dépôt":"Ajout d'un dépôt");
		
		this.setModal(true);
		
		this.setPreferredSize(new Dimension(490, 200));
		
		TitledPanel titlePanel = new TitledPanel("Dépôt SVN");
		getContentPane().add(titlePanel, BorderLayout.NORTH);
		
		JPanel contentPanel = new JPanel();
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		
		JLabel urlLabel = new JLabel("URL du dépôt : ");
		
		urlField = new JTextField();
		urlField.setColumns(10);
		
		JLabel idLabel = new JLabel("Identifiant : ");
		
		idField = new JTextField();
		idField.setColumns(10);
		
		JLabel pwdLabel = new JLabel("Mot de passe : ");
		
		pwdField = new JTextField();
		pwdField.setColumns(10);
		
		cancelButton = new JButton("Annuler");
		cancelButton.addActionListener(this);
		
		add_modButton = new JButton(mod?"Modifier le dépôt":"Ajouter le dépôt");
		add_modButton.addActionListener(this);
		
		GroupLayout gl_contentPanel = new GroupLayout(contentPanel);
		gl_contentPanel.setHorizontalGroup(
			gl_contentPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPanel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_contentPanel.createSequentialGroup()
							.addGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
								.addComponent(urlLabel)
								.addComponent(idLabel)
								.addComponent(pwdLabel))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
								.addComponent(idField, GroupLayout.DEFAULT_SIZE, 366, Short.MAX_VALUE)
								.addComponent(urlField, GroupLayout.DEFAULT_SIZE, 366, Short.MAX_VALUE)
								.addComponent(pwdField, GroupLayout.DEFAULT_SIZE, 366, Short.MAX_VALUE)))
						.addGroup(Alignment.TRAILING, gl_contentPanel.createSequentialGroup()
							.addComponent(add_modButton)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(cancelButton)))
					.addContainerGap())
		);
		gl_contentPanel.setVerticalGroup(
			gl_contentPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPanel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(urlLabel)
						.addComponent(urlField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(idLabel)
						.addComponent(idField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(pwdLabel)
						.addComponent(pwdField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(cancelButton)
						.addComponent(add_modButton))
					.addContainerGap(49, Short.MAX_VALUE))
		);
		contentPanel.setLayout(gl_contentPanel);
		
		this.getRootPane().setDefaultButton(add_modButton);
		this.pack();
	}
	
	/**
	 * @wbp.parser.constructor
	 */
	public SVNRepositoryParamsUI(ConfigurationUI confUI){
		this(confUI, false);
	}
	
	public SVNRepositoryParamsUI(ConfigurationUI confUI, final String url, final String id, final String pwd, 
			JPanel repositoryPanel){
		this(confUI, true);
		repoPanel = repositoryPanel;
		oldURL = url;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				urlField.setText(url);
				idField.setText(id);
				pwdField.setText(pwd);
			}
		});
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource().equals(add_modButton)){
			String url = urlField.getText();
			if(url.isEmpty()){
				if(url.isEmpty()){
					urlField.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
				}
			}else{
				if(mod){
					Configuration.removeSVNRepository(oldURL);
					configurationUI.updateRepository(repoPanel, url);
				}else{
					configurationUI.addSVNRepositoryPanel(url, true);
				}
				Configuration.addSVNRepository(url, idField.getText(), pwdField.getText());
				dispose();
			}
		}else if(e.getSource().equals(cancelButton)){
			dispose();
		}
	}
}
