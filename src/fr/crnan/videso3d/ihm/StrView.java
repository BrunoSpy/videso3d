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

import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.VidesoGLCanvas;
/**
 * Sélecteur de données STR
 * @author Bruno Spyckerelle
 * @version 0.2.1
 */
@SuppressWarnings("serial")
public class StrView extends JPanel {

	/**
	 * Choix des mosaïques à afficher
	 */
	private JPanel mosaiques = new JPanel();
	/**
	 * Filtrage capacitif
	 */
	private JPanel capa = new JPanel();
	/**
	 * Filtrage dynamique
	 */
	private JPanel dyn = new JPanel();
	/**
	 * Zone d'occultation
	 */
	private JPanel zocc = new JPanel();
	/**
	 * VVF
	 */
	private JPanel vvf = new JPanel();
	
	private ItemCheckListener itemCheckListener = new ItemCheckListener();
	
	private DatabaseManager db;
	private VidesoGLCanvas wwd;
	
	private JRadioButton flat;
	
	public StrView( final VidesoGLCanvas wwd, DatabaseManager db){
		this.db = db;
		this.wwd = wwd;
		
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		mosaiques.setBorder(BorderFactory.createTitledBorder("Mosaïques"));
		capa.setBorder(BorderFactory.createTitledBorder("Filtrage capacitif"));
		dyn.setBorder(BorderFactory.createTitledBorder("Filtrage dynamique"));
		zocc.setBorder(BorderFactory.createTitledBorder("Zones d'occultation"));
		vvf.setBorder(BorderFactory.createTitledBorder("VVF"));

		//affichage 2D/3D
		JLabel label = new JLabel("Style d'affichage : ");
		flat = new JRadioButton("2D");
		flat.setSelected(true);
		flat.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				wwd.toggleMosaique2D(e.getStateChange() == ItemEvent.SELECTED);
			}
		});
		JRadioButton round = new JRadioButton("3D");
		ButtonGroup style = new ButtonGroup();
		style.add(flat);
		style.add(round);
		JPanel stylePanel = new JPanel();
		stylePanel.setBorder(BorderFactory.createTitledBorder(" "));
		stylePanel.setLayout(new BoxLayout(stylePanel, BoxLayout.X_AXIS));
		stylePanel.add(label);
		stylePanel.add(flat);
		stylePanel.add(round);
		this.add(stylePanel);
		
		try {
			if(this.db.getCurrentExsa() != null) {
				this.add(this.buildPanel(mosaiques, "select type from centmosai"));
				this.add(this.buildPanel(capa, "select DISTINCT abonne from ficaafniv"));
				this.add(this.buildPanel(dyn, "select DISTINCT abonne from ficaafnic"));
				this.add(this.buildPanel(zocc, "select name from centzocc"));
				this.add(this.buildPanel(vvf, "select name from centflvvf"));

				this.add(Box.createVerticalGlue());
			}
		} catch (SQLException e){
			e.printStackTrace();
		}
	}
	
	private JPanel buildPanel(JPanel panel, String query){
		panel.setLayout(new GridLayout(0, 3));
		int i = 0;
		try {
			Statement st = this.db.getCurrentExsa();
			ResultSet rs = st.executeQuery(query);
			while(rs.next()){
				JCheckBox chk = new JCheckBox(rs.getString(1));
				chk.addItemListener(itemCheckListener);
				panel.add(chk);	
				i++;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return panel;
	}
	
	/*---------------------------------------------------------*/
	/*------------------- Listeners ---------------------------*/
	/*---------------------------------------------------------*/
	
	private class ItemCheckListener implements ItemListener {

		@Override
		public void itemStateChanged(ItemEvent e) {
			Object parent = ((JCheckBox)e.getSource()).getParent();
			if(e.getStateChange() == ItemEvent.SELECTED) {
				if(mosaiques.equals(parent)){
					wwd.toggleMosaiqueLayer("mosaique", ((JCheckBox)e.getSource()).getText(), true, flat.isSelected());
				} else if (capa.equals(parent)){
					wwd.toggleMosaiqueLayer("capa", ((JCheckBox)e.getSource()).getText(), true, flat.isSelected());
				} else if (dyn.equals(parent)){
					wwd.toggleMosaiqueLayer("dyn", ((JCheckBox)e.getSource()).getText(), true, flat.isSelected());
				}else if (zocc.equals(parent)){
					wwd.toggleMosaiqueLayer("zocc", ((JCheckBox)e.getSource()).getText(), true, flat.isSelected());
				}else if (vvf.equals(parent)){
					wwd.toggleMosaiqueLayer("vvf", ((JCheckBox)e.getSource()).getText(), true, flat.isSelected());
				}
			} else {
				if(mosaiques.equals(parent)){
					wwd.toggleMosaiqueLayer("mosaique", ((JCheckBox)e.getSource()).getText(), false, flat.isSelected());
				} else if (capa.equals(parent)){
					wwd.toggleMosaiqueLayer("capa", ((JCheckBox)e.getSource()).getText(), false, flat.isSelected());
				} else if (dyn.equals(parent)){
					wwd.toggleMosaiqueLayer("dyn", ((JCheckBox)e.getSource()).getText(), false, flat.isSelected());
				}else if (zocc.equals(parent)){
					wwd.toggleMosaiqueLayer("zocc", ((JCheckBox)e.getSource()).getText(), false, flat.isSelected());
				}else if (vvf.equals(parent)){
					wwd.toggleMosaiqueLayer("vvf", ((JCheckBox)e.getSource()).getText(), false, flat.isSelected());
				}
			}
		}
		
	}
}
