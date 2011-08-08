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
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.DatasManager;
import fr.crnan.videso3d.DatabaseManager.Type;
import fr.crnan.videso3d.ihm.components.DataView;
import fr.crnan.videso3d.stpv.StpvController;
/**
 * Sélecteur de données STPV
 * @author Bruno Spyckerelle
 * @version 0.2.1
 */
@SuppressWarnings("serial")
public class StpvView extends JPanel implements DataView{

	/**
	 * Mosaique
	 */
	private JPanel mosaique = new JPanel();
	
	private ItemCheckListener itemCheckListener = new ItemCheckListener();
	
	private List<JCheckBox> chkList = new LinkedList<JCheckBox>();
	
	public StpvView(){

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		mosaique.setBorder(BorderFactory.createTitledBorder("Mosaique"));
		try{
			if(DatabaseManager.getCurrentStpv() != null) {
				this.add(buildPanel(mosaique, "select type from mosaique"));
				
				this.add(Box.createVerticalStrut(1000));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public StpvController getController() {
		return (StpvController) DatasManager.getController(Type.STPV);
	}
	
	private JPanel buildPanel(JPanel panel, String query){
		panel.setLayout(new GridLayout(0, 3));
		try {
			Statement st = DatabaseManager.getCurrentStpv();
			ResultSet rs = st.executeQuery(query);
			while(rs.next()){
				JCheckBox chk = new JCheckBox(rs.getString(1));
				chk.addItemListener(itemCheckListener);
				chkList.add(chk);
				panel.add(chk);	
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return panel;
	}
	
	@Override
	public void reset() {
		for(JCheckBox c : chkList){
			if(c.isSelected()){
				c.setSelected(false);
			}
		}
	}
	
	/*---------------------------------------------------------*/
	/*------------------- Listeners ---------------------------*/
	/*---------------------------------------------------------*/
	
	private class ItemCheckListener implements ItemListener {

		@Override
		public void itemStateChanged(ItemEvent e) {
			Object parent = ((JCheckBox)e.getSource()).getParent();
			if(e.getStateChange() == ItemEvent.SELECTED) {
				if(mosaique.equals(parent)){
					getController().showObject(StpvController.MOSAIQUE, ((JCheckBox)e.getSource()).getText());
				} 
			} else {
				if(mosaique.equals(parent)){
					getController().hideObject(StpvController.MOSAIQUE, ((JCheckBox)e.getSource()).getText());
				}
			}
		}
		
	}

	
	/**
	 * Recherche une checkBox dans un des JPanel
	 * @param type pour préciser le JPanel dans lequel il faut chercher
	 * @param name le nom de la checkbox à chercher
	 */
	private JCheckBox getCheckBox(int type, String name){
		JPanel panel = null;
		switch(type){
		case StpvController.MOSAIQUE :
			panel = mosaique;
			break;
		case StpvController.BALISE :
			break;
		case StpvController.SECTEUR :
			break;
		case StpvController.STAR :
			break;
		}
		if(panel!=null){
			for(Component c : panel.getComponents()){
				if(c instanceof JCheckBox){
					if(((JCheckBox)c).getText().equals(name))
						return (JCheckBox)c;
				}
			}
		}
		return null;
	}
	
	@Override
	public void showObject(int type, String name) {
		JCheckBox c = getCheckBox(type, name);
		if(c!=null)
			//pas de risque de boucle infinie avec le controller car cette méthode ne déclenche pas d'event
			c.setSelected(true);	
	}

	@Override
	public void hideObject(int type, String name) {
		JCheckBox c = getCheckBox(type, name);
		if(c!=null)
			//pas de risque de boucle infinie avec le controller car cette méthode ne déclenche pas d'event
			c.setSelected(false);
	}


}

