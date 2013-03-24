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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import fr.crnan.videso3d.Configuration;
import fr.crnan.videso3d.Pallet;
import fr.crnan.videso3d.ihm.components.TitledPanel;
import gov.nasa.worldwind.avlist.AVKey;
/**
 * Interface de configuration
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class ConfigurationUI extends JFrame {
	
	private JPanel all;
	private final ConfigurationUI thisConfUI;

	public ConfigurationUI(){
		super();
		thisConfUI = this;
		this.setTitle("Configuration Videso 3D");
		
		all = new JPanel();
		all.setLayout(new BoxLayout(all, BoxLayout.Y_AXIS));
		all.setBorder(null);
		this.getContentPane().add(all);
		
		//COULEURS
		TitledPanel colorsTitle = new TitledPanel("Couleurs");
		colorsTitle.setAlignmentY(JComponent.TOP_ALIGNMENT);
		all.add(colorsTitle);
		
		JPanel colorsPanel = new JPanel();
		colorsPanel.setAlignmentY(JComponent.TOP_ALIGNMENT);
		colorsPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		colorsPanel.add(new JLabel("Fond des pays : "), c);

		c.weightx = 0;
		c.gridx = 1;

		final JLabel couleur = new JLabel("          ");
		couleur.setOpaque(true);
		couleur.setBackground(Pallet.getColorFondPays());
		Configuration.addPropertyChangeListener(new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if(evt.getPropertyName().equals(Configuration.COLOR_FOND_PAYS)){
					couleur.setBackground(Pallet.getColorFondPays());
				}
			}
		});
		
		colorsPanel.add(couleur, c);
		JButton changeColor = new JButton(new ImageIcon(getClass().getResource("/resources/fill-color.png"))); 
		changeColor.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Pallet.setColorFondPays(JColorChooser.showDialog(null, "Couleur", Pallet.getColorFondPays()));
			}
		});
		c.gridx = 2;
		colorsPanel.add(changeColor, c);
				
		c.gridx = 0;
		c.gridy = 1;
		colorsPanel.add(new JLabel("Marqueurs des balises : "), c);
		final JLabel couleurMarker = new JLabel("          ");
		couleurMarker.setOpaque(true);
		couleurMarker.setBackground(Pallet.getColorBaliseMarker());
		Configuration.addPropertyChangeListener(new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if(evt.getPropertyName().equals(Configuration.COLOR_BALISE_MARKER)){
					couleurMarker.setBackground(Pallet.getColorBaliseMarker());
				}
			}
		});
		c.gridx = 1;
		colorsPanel.add(couleurMarker, c);
		JButton changeColorMarker = new JButton(new ImageIcon(getClass().getResource("/resources/fill-color.png"))); 
		changeColorMarker.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Pallet.setColorBaliseMarker(JColorChooser.showDialog(null, "Couleur", Pallet.getColorBaliseMarker()));
			}
		});
		c.gridx = 2;
		colorsPanel.add(changeColorMarker, c);
		
		c.gridx = 0;
		c.gridy = 2;
		colorsPanel.add(new JLabel("Textes des balises : "), c);
		final JLabel couleurTexte = new JLabel("          ");
		couleurTexte.setOpaque(true);
		couleurTexte.setBackground(Pallet.getColorBaliseText());
		Configuration.addPropertyChangeListener(new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if(evt.getPropertyName().equals(Configuration.COLOR_BALISE_TEXTE)){
					couleurTexte.setBackground(Pallet.getColorBaliseText());
				}
			}
		});
		c.gridx = 1;
		colorsPanel.add(couleurTexte, c);
		
		JButton changeColorTexte = new JButton(new ImageIcon(getClass().getResource("/resources/fill-color.png"))); 
		changeColorTexte.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Pallet.setColorBaliseTexte(JColorChooser.showDialog(null, "Couleur", Pallet.getColorBaliseText()));
			}
		});
		c.gridx = 2;
		colorsPanel.add(changeColorTexte, c);
		
		all.add(colorsPanel);
		
		//PROXY
		all.add(new TitledPanel("Réseau"));
		
		JPanel proxy = new JPanel();
		proxy.setLayout(new BoxLayout(proxy, BoxLayout.LINE_AXIS));
		proxy.add(new JLabel("Proxy hostname : "));
		final JTextField hostname = new JTextField(50);
		hostname.setText(gov.nasa.worldwind.Configuration.getStringValue(AVKey.URL_PROXY_HOST));
		proxy.add(hostname);
		proxy.add(new JLabel("Port : "));
		final JTextField port = new JTextField(7);
		port.setText(gov.nasa.worldwind.Configuration.getStringValue(AVKey.URL_PROXY_PORT));
		proxy.add(port);
		JButton val = new JButton("Valider");
		val.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				//enregistrement des paramètres
				Configuration.setProperty(Configuration.NETWORK_PROXY_HOST, hostname.getText());
				Configuration.setProperty(Configuration.NETWORK_PROXY_PORT, port.getText());
				//application des paramètres
				gov.nasa.worldwind.Configuration.setValue(AVKey.URL_PROXY_HOST, hostname.getText());
				gov.nasa.worldwind.Configuration.setValue(AVKey.URL_PROXY_PORT, port.getText());
				gov.nasa.worldwind.Configuration.setValue(AVKey.URL_PROXY_TYPE, "Proxy.Type.Http");
			}
		});
		proxy.add(val);
		all.add(proxy);
		
		//Trajectographie
		all.add(new TitledPanel("Trajectographie"));
		JPanel trajecto = new JPanel();
		trajecto.setLayout(new BoxLayout(trajecto, BoxLayout.LINE_AXIS));
		trajecto.add(new JLabel("Chevelus à partir de : "));
		trajecto.add(Box.createHorizontalGlue());
		final JTextField chevelus = new JTextField(20);
		chevelus.setText(Configuration.getProperty(Configuration.TRAJECTOGRAPHIE_SEUIL, "20"));
		trajecto.add(chevelus);
		JButton valChevelus = new JButton("Valider");
		valChevelus.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Configuration.setProperty(Configuration.TRAJECTOGRAPHIE_SEUIL, chevelus.getText());
			}
		});
		trajecto.add(valChevelus);
		
		
		
		JPanel trajecto2 = new JPanel();
		trajecto2.setLayout(new BoxLayout(trajecto2, BoxLayout.LINE_AXIS));
		trajecto2.add(new JLabel("Simplifier les tracés à partir de : "));
		trajecto2.add(Box.createHorizontalGlue());
		final JTextField seuilPrecision = new JTextField(20);
		seuilPrecision.setText(Configuration.getProperty(Configuration.TRAJECTOGRAPHIE_SEUIL, "100"));
		trajecto2.add(seuilPrecision);
		JButton valSeuilPrecision = new JButton("Valider");
		valSeuilPrecision.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Configuration.setProperty(Configuration.TRAJECTOGRAPHIE_SEUIL_PRECISION, seuilPrecision.getText());
			}
		});
		trajecto2.add(valSeuilPrecision);
		
		JPanel trajecto3 = new JPanel();
		trajecto3.setLayout(new BoxLayout(trajecto3, BoxLayout.LINE_AXIS));
		trajecto3.add(new JLabel("Précision (en degrés) : "));
		trajecto3.add(Box.createHorizontalGlue());
		final JTextField precision = new JTextField(20);
		precision.setText(Configuration.getProperty(Configuration.TRAJECTOGRAPHIE_PRECISION, "0.01"));
		trajecto3.add(precision);
		JButton valPrecision = new JButton("Valider");
		valPrecision.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Configuration.setProperty(Configuration.TRAJECTOGRAPHIE_PRECISION, precision.getText());
			}
		});
		trajecto3.add(valPrecision);
		
		all.add(trajecto);
		all.add(trajecto2);
		all.add(trajecto3);
		
		
		
		
		TitledPanel svnTitle = new TitledPanel("Dépôts SVN");
		colorsTitle.setAlignmentY(JComponent.TOP_ALIGNMENT);
		all.add(svnTitle);
		String svnRepositories = Configuration.getProperty(Configuration.SVN_REPOSITORIES, "");
		if(!svnRepositories.isEmpty()){
			addSVNRepositoriesPanels(svnRepositories,false);
		}		
		JPanel svnAddRepository = new JPanel();
		svnAddRepository.setLayout(new FlowLayout(FlowLayout.TRAILING));
		JButton addSVNRepositoryButton = new JButton("Ajouter un dépôt");
		addSVNRepositoryButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				SVNRepositoryParamsUI svnAddRepositoryUI = new SVNRepositoryParamsUI(thisConfUI);
				svnAddRepositoryUI.setVisible(true);
			}
		});
		svnAddRepository.add(addSVNRepositoryButton);
		
		all.add(svnAddRepository);
		
		//all.add(new Box.Filler(new Dimension(0, Short.MAX_VALUE), new Dimension(0, Short.MAX_VALUE), new Dimension(0, Short.MAX_VALUE)));
		this.pack();
	}

	
	/**
	 * 
	 * @param svnRepositories
	 * @param secondToLast true si les dépôts doivent être ajoutés en avant dernière position dans le container (utile dans le cas où
	 * on ajoute un dépôt, celui-ci doit être placé avant le bouton  "Ajouter un dépôt")
	 */
	private void addSVNRepositoriesPanels(String svnRepositories, boolean secondToLast) {
		String[] svnRepos = svnRepositories.split("#");
		for(String svnRepo : svnRepos){
			String[] svnRepoParams = svnRepo.split(";");
			addSVNRepositoryPanel(svnRepoParams[0], secondToLast);
		}
	}
	
	public void addSVNRepositoryPanel(final String URL, boolean secondToLast){
		final JPanel repo = new JPanel();
		repo.setLayout(new BoxLayout(repo, BoxLayout.LINE_AXIS));
		JLabel repoLabel = new JLabel("Dépôt "+": ");
		final JTextField repoURL = new JTextField(URL);
		repoURL.setEditable(false);
		JButton modify = new JButton("Modifier");
		modify.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				String[] svnRepoParams = Configuration.getRepository(repoURL.getText()).split(";");
				SVNRepositoryParamsUI svnRepositoryParamsUI = new SVNRepositoryParamsUI(thisConfUI, 
						svnRepoParams[0],
						svnRepoParams.length > 1 ? svnRepoParams[1] : "",
						svnRepoParams.length > 2 ? svnRepoParams[2] : "",
						repo);
				svnRepositoryParamsUI.setVisible(true);
			}
		});
		JButton delete = new JButton("Supprimer");
		delete.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Configuration.removeSVNRepository(URL);
				all.remove(repo);
				getContentPane().revalidate();
			}
		});
		repo.add(repoLabel);
		repo.add(repoURL);
		repo.add(Box.createHorizontalGlue());
		repo.add(modify);
		repo.add(delete);
		if(secondToLast){
			all.add(repo, all.getComponentCount()-1);
		}else
			all.add(repo);
		
		this.getContentPane().revalidate();
		this.pack();
	}


	public void updateRepository(JPanel repoPanel, String newURL) {
		for(Component c : repoPanel.getComponents()){
			if(c.getClass().equals(JLabel.class)){
				((JLabel)c).setText("Dépôt "+" : ");
			}
			if(c.getClass().equals(JTextField.class)){
				((JTextField) c).setText(newURL);
				break;
			}
		}
		getContentPane().revalidate();
	}
	
	
	@Override
	public Dimension getSize(){
		return new Dimension(500,(int) super.getSize().getHeight());
	}
	@Override
	public Dimension getPreferredSize(){
		return new Dimension(500,(int) super.getPreferredSize().getHeight());
	}
	
}
