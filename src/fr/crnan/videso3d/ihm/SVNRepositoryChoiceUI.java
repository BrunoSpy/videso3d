package fr.crnan.videso3d.ihm;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JDialog;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.JButton;

import fr.crnan.videso3d.Configuration;

public class SVNRepositoryChoiceUI extends JDialog implements ActionListener {
	private JTextField repoField;
	private JComboBox<String> repoCombobox;
	private JButton okButton;
	private JButton cancelButton;
	private DatabaseManagerUI dbmUI;
	/**
	 * La clé de la hashmap est le type, la valeur est la chaîne de caractères "type;url;id;password"
	 */
	private HashMap<String,String> svnRepositoryMap = new HashMap<String, String>();
	
	
	public SVNRepositoryChoiceUI(DatabaseManagerUI parent) {
		super(parent);
		this.dbmUI = parent;
		getRepositories();
		setTitle("Choix du dépôt SVN");
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		
		JPanel repoChoicePanel = new JPanel();
		getContentPane().add(repoChoicePanel);
		
		JLabel repoChoiceLabel = new JLabel("Choisir un dépôt : ");
		repoChoicePanel.add(repoChoiceLabel);
		
		repoCombobox = new JComboBox<String>();
		DefaultComboBoxModel<String> comboboxModel = new DefaultComboBoxModel<String>(svnRepositoryMap.keySet().toArray(new String[0]));
		repoCombobox.setModel(comboboxModel);
		repoCombobox.addActionListener(this);
		repoChoicePanel.add(repoCombobox);
		
		JPanel repoDisplayPanel = new JPanel();
		getContentPane().add(repoDisplayPanel);
		
		repoField = new JTextField();
		repoField.setText((String) repoCombobox.getSelectedItem());
		repoField.setEditable(false);
		repoField.setAlignmentX(JTextField.CENTER_ALIGNMENT);
		repoField.setColumns(20);
		repoDisplayPanel.add(repoField);
		
		JPanel buttonsPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) buttonsPanel.getLayout();
		flowLayout.setAlignment(FlowLayout.TRAILING);
		getContentPane().add(buttonsPanel);
		
		okButton = new JButton("OK");
		okButton.addActionListener(this);
		buttonsPanel.add(okButton);
		
		cancelButton = new JButton("Annuler");
		cancelButton.addActionListener(this);
		buttonsPanel.add(cancelButton);
		
		this.getRootPane().setDefaultButton(okButton);
		this.pack();
		this.setAlwaysOnTop(true);
	}
	
	
	private void getRepositories() {
		String[] svnRepositories = Configuration.getProperty(Configuration.SVN_REPOSITORIES, "").split("#");
		for(String svnRepo : svnRepositories){
			this.svnRepositoryMap.put(svnRepo.split(";")[0], svnRepo);
		}
	}


	@SuppressWarnings("unchecked")
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource().equals(repoCombobox)){
			repoField.setText((String)((JComboBox<String>)e.getSource()).getSelectedItem());
		}else if(e.getSource().equals(okButton)){
			new SVNRepositoryUI(svnRepositoryMap.get(repoField.getText()), dbmUI).setVisible(true);
			dispose();
		}else if(e.getSource().equals(cancelButton)){
			dispose();
		}
	}

}
