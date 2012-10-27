package fr.crnan.videso3d.ihm;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.ImageIcon;

import fr.crnan.videso3d.ihm.components.TitledPanel;
import javax.swing.JSplitPane;
import java.awt.Color;
import javax.swing.border.MatteBorder;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EmptyBorder;
import javax.swing.SwingConstants;
/**
 * Configuration IHM
 * @author Bruno Spyckerelle
 * @version 0.2.0
 *
 */
public class ConfigurationUI2 extends JDialog {
	private JTextField urlField;
	private JTextField portField;


	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			ConfigurationUI2 dialog = new ConfigurationUI2();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public ConfigurationUI2() {
		setTitle("Configuration Videso 3D");
		setBounds(100, 100, 736, 460);
		getContentPane().setLayout(new BorderLayout());



		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		{
			JPanel colorPanel = new JPanel();
			tabbedPane.addTab("Couleurs", new ImageIcon(ConfigurationUI2.class.getResource("/resources/fill-color.png")), colorPanel, null);
			
			JLabel lblFondDesPays = new JLabel("Fond des pays : ");
			
			JButton colorFondBtn = new JButton(" ");
			colorFondBtn.setIcon(new ImageIcon(ConfigurationUI2.class.getResource("/resources/fill-color.png")));
			
			JLabel colorFond = new JLabel("          ");
			colorFond.setBackground(Color.RED);
			colorFond.setOpaque(true);
			
			JLabel lblMarqueursDesBalises = new JLabel("Marqueurs des balises : ");
			
			JButton colorMarqueursBtn = new JButton(" ");
			colorMarqueursBtn.setIcon(new ImageIcon(ConfigurationUI2.class.getResource("/resources/fill-color.png")));
			
			JLabel colorMarqueurs = new JLabel("          ");
			colorMarqueurs.setBackground(Color.RED);
			colorMarqueurs.setOpaque(true);
			
			JLabel lblTexteDesBalises = new JLabel("Texte des balises : ");
			
			JButton button = new JButton(" ");
			button.setIcon(new ImageIcon(ConfigurationUI2.class.getResource("/resources/fill-color.png")));
			
			JLabel label = new JLabel("          ");
			label.setBackground(Color.RED);
			label.setOpaque(true);
			GroupLayout gl_colorPanel = new GroupLayout(colorPanel);
			gl_colorPanel.setHorizontalGroup(
				gl_colorPanel.createParallelGroup(Alignment.LEADING)
					.addGroup(gl_colorPanel.createSequentialGroup()
						.addContainerGap()
						.addGroup(gl_colorPanel.createParallelGroup(Alignment.LEADING)
							.addComponent(lblFondDesPays)
							.addComponent(lblMarqueursDesBalises)
							.addComponent(lblTexteDesBalises))
						.addPreferredGap(ComponentPlacement.RELATED, 262, Short.MAX_VALUE)
						.addGroup(gl_colorPanel.createParallelGroup(Alignment.LEADING)
							.addComponent(colorFond)
							.addComponent(colorMarqueurs)
							.addComponent(label))
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(gl_colorPanel.createParallelGroup(Alignment.LEADING)
							.addComponent(button)
							.addComponent(colorMarqueursBtn)
							.addComponent(colorFondBtn))
						.addContainerGap())
			);
			gl_colorPanel.setVerticalGroup(
				gl_colorPanel.createParallelGroup(Alignment.LEADING)
					.addGroup(gl_colorPanel.createSequentialGroup()
						.addContainerGap()
						.addGroup(gl_colorPanel.createParallelGroup(Alignment.BASELINE)
							.addComponent(lblFondDesPays)
							.addComponent(colorFondBtn)
							.addComponent(colorFond))
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(gl_colorPanel.createParallelGroup(Alignment.BASELINE)
							.addComponent(lblMarqueursDesBalises)
							.addComponent(colorMarqueursBtn)
							.addComponent(colorMarqueurs))
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(gl_colorPanel.createParallelGroup(Alignment.BASELINE)
							.addComponent(lblTexteDesBalises)
							.addComponent(button)
							.addComponent(label))
						.addContainerGap(263, Short.MAX_VALUE))
			);
			
			colorPanel.setLayout(gl_colorPanel);
		}
		{
			JPanel networkPanel = new JPanel();
			tabbedPane.addTab("Réseau", new ImageIcon(ConfigurationUI2.class.getResource("/resources/network-wired_16.png")), networkPanel, null);
			
			JLabel lblUrl = new JLabel("Adresse du proxy : ");
			
			JLabel lblPort = new JLabel("Port : ");
			
			urlField = new JTextField();
			urlField.setColumns(10);
			
			portField = new JTextField();
			portField.setColumns(10);
			GroupLayout gl_networkPanel = new GroupLayout(networkPanel);
			gl_networkPanel.setHorizontalGroup(
				gl_networkPanel.createParallelGroup(Alignment.LEADING)
					.addGroup(gl_networkPanel.createSequentialGroup()
						.addContainerGap()
						.addGroup(gl_networkPanel.createParallelGroup(Alignment.LEADING)
							.addComponent(lblUrl)
							.addComponent(lblPort))
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addGroup(gl_networkPanel.createParallelGroup(Alignment.LEADING)
							.addComponent(portField, GroupLayout.DEFAULT_SIZE, 382, Short.MAX_VALUE)
							.addComponent(urlField, GroupLayout.DEFAULT_SIZE, 382, Short.MAX_VALUE))
						.addContainerGap())
			);
			gl_networkPanel.setVerticalGroup(
				gl_networkPanel.createParallelGroup(Alignment.LEADING)
					.addGroup(gl_networkPanel.createSequentialGroup()
						.addContainerGap()
						.addGroup(gl_networkPanel.createParallelGroup(Alignment.BASELINE)
							.addComponent(lblUrl)
							.addComponent(urlField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addGroup(gl_networkPanel.createParallelGroup(Alignment.BASELINE)
							.addComponent(lblPort)
							.addComponent(portField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addContainerGap(317, Short.MAX_VALUE))
			);
			networkPanel.setLayout(gl_networkPanel);
		}
		{
			JPanel trajectoPanel = new JPanel();
			tabbedPane.addTab("Trajectographie", null, trajectoPanel, null);
		}


		JPanel helpPanel = new JPanel(new BorderLayout());
		helpPanel.add(new TitledPanel("Aide"), BorderLayout.NORTH);
		helpPanel.setMinimumSize(new Dimension(200, 0));
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tabbedPane, helpPanel);
		
		JLabel helpText = new JLabel("Text");
		helpText.setBorder(new EmptyBorder(5, 5, 5, 5));
		helpText.setVerticalAlignment(SwingConstants.TOP);
		helpPanel.add(helpText, BorderLayout.CENTER);
		splitPane.setResizeWeight(1.0);
		splitPane.setBorder(new MatteBorder(0, 0, 1, 0, (Color) new Color(0, 0, 0)));
		splitPane.setOneTouchExpandable(true);
		getContentPane().add(splitPane, BorderLayout.CENTER);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("Valider");
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Annuler");
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}
}
