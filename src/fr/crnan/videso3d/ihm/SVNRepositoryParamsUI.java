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
import javax.swing.BoxLayout;
import javax.swing.SwingUtilities;

import fr.crnan.videso3d.Configuration;
import fr.crnan.videso3d.ihm.components.TitledPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

/**
 * Fenêtre d'ajout ou de modification d'un dépôt SVN
 * @author vidal
 *
 */
public class SVNRepositoryParamsUI extends JDialog implements ActionListener{
	private JTextField urlField;
	private JTextField idField;
	private JTextField pwdField;
	private JTextField typeField;
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
	 * @wbp.parser.constructor
	 */
	public SVNRepositoryParamsUI(ConfigurationUI confUI, boolean mod) {
		configurationUI = confUI;
		this.mod = mod;
		setTitle(mod?"Modification d'un dépôt":"Ajout d'un dépôt");
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		
		TitledPanel titlePanel = new TitledPanel("Dépôt SVN");
		getContentPane().add(titlePanel);
		
		JPanel typePanel = new JPanel();
		getContentPane().add(typePanel);
		typePanel.setLayout(new BoxLayout(typePanel, BoxLayout.X_AXIS));
		
		JLabel lblNewLabel = new JLabel("Type de dépôt (ex. STIP, STPV...) : ");
		typePanel.add(lblNewLabel);
		
		typeField = new JTextField();
		typePanel.add(typeField);
		typeField.setColumns(10);
		JPanel urlPanel = new JPanel();
		getContentPane().add(urlPanel);
		urlPanel.setLayout(new BoxLayout(urlPanel, BoxLayout.X_AXIS));
		
		JLabel urlLabel = new JLabel("URL du dépôt : ");
		urlPanel.add(urlLabel);
		
		urlField = new JTextField();
		urlPanel.add(urlField);
		urlField.setColumns(10);
		
		JPanel identificationPanel = new JPanel();
		getContentPane().add(identificationPanel);
		identificationPanel.setLayout(new BoxLayout(identificationPanel, BoxLayout.X_AXIS));
		
		JLabel idLabel = new JLabel("Identifiant : ");
		identificationPanel.add(idLabel);
		
		idField = new JTextField();
		idField.setText("");
		identificationPanel.add(idField);
		idField.setColumns(10);
		
		JLabel pwdLabel = new JLabel("Mot de passe : ");
		identificationPanel.add(pwdLabel);
		
		pwdField = new JTextField();
		identificationPanel.add(pwdField);
		pwdField.setColumns(10);
		
		JPanel addPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) addPanel.getLayout();
		flowLayout.setAlignment(FlowLayout.TRAILING);
		getContentPane().add(addPanel);
		
		add_modButton = new JButton(mod?"Modifier le dépôt":"Ajouter le dépôt");
		add_modButton.addActionListener(this);
		addPanel.add(add_modButton);
		
		cancelButton = new JButton("Annuler");
		cancelButton.addActionListener(this);
		addPanel.add(cancelButton);
		
		this.getRootPane().setDefaultButton(add_modButton);
		this.pack();
	}
	
	public SVNRepositoryParamsUI(ConfigurationUI confUI){
		this(confUI, false);
	}
	
	public SVNRepositoryParamsUI(ConfigurationUI confUI, final String type, final String url, final String id, final String pwd, 
			JPanel repositoryPanel){
		this(confUI, true);
		repoPanel = repositoryPanel;
		oldURL = url;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				typeField.setText(type);
				urlField.setText(url);
				idField.setText(id);
				pwdField.setText(pwd);
			}
		});
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource().equals(add_modButton)){
			String type = typeField.getText();
			String url = urlField.getText();
			if(type.isEmpty()||url.isEmpty()){
				if(type.isEmpty()){
					typeField.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
				}
				if(url.isEmpty()){
					urlField.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
				}
			}else{
				if(mod){
					Configuration.removeSVNRepository(oldURL);
					configurationUI.updateRepository(repoPanel, type, url);
				}else{
					configurationUI.addSVNRepositoryPanel(type, url, true);
				}
				Configuration.addSVNRepository(type, url, idField.getText(), pwdField.getText());
				dispose();
			}
		}else if(e.getSource().equals(cancelButton)){
			dispose();
		}
	}

}
