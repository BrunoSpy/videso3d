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
import fr.crnan.videso3d.exsa.STRController;
import fr.crnan.videso3d.ihm.components.DataView;
import fr.crnan.videso3d.ihm.components.TitleTwoButtons;
/**
 * Sélecteur de données STR
 * @author Bruno Spyckerelle
 * @version 0.2.5
 */
@SuppressWarnings("serial")
public class StrView extends JPanel implements DataView{

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
	/**
	 * Radars
	 */
	private JPanel radars = new JPanel();
	
	private ItemCheckListener itemCheckListener = new ItemCheckListener();
	
	/**
	 * Liste des checkbox de la vue, afin de pouvoir tous les désélectionner facilement
	 */
	private List<JCheckBox> checkBoxList = new LinkedList<JCheckBox>();
		
	public StrView() {
		
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		mosaiques.setBorder(BorderFactory.createTitledBorder("Mosaïques"));
		capa.setBorder(BorderFactory.createTitledBorder("Filtrage capacitif"));
		dyn.setBorder(BorderFactory.createTitledBorder("Filtrage dynamique"));
		zocc.setBorder(BorderFactory.createTitledBorder("Zones d'occultation"));
		vvf.setBorder(BorderFactory.createTitledBorder("VVF"));
		radars.setBorder(BorderFactory.createTitledBorder("Portées radars"));
		
		//affichage 2D/3D
		TitleTwoButtons style = new TitleTwoButtons("Style d'affichage : ", "2D", "3D", true);
		style.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				getController().set2D(e.getStateChange() == ItemEvent.SELECTED);
			}
		});
		style.setBorder(BorderFactory.createTitledBorder(" "));
		this.add(style);
		
		try {
			if(DatabaseManager.getCurrentExsa() != null) {
				this.add(this.buildPanel(mosaiques, "select type from centmosai"));
				this.add(this.buildPanel(capa, "select DISTINCT abonne from ficaafniv"));
				this.add(this.buildPanel(dyn, "select DISTINCT abonne from ficaafnic"));
				this.add(this.buildPanel(zocc, "select name from centzocc"));
				this.add(this.buildPanel(vvf, "select name from centflvvf"));
				this.add(this.buildPanel(radars, "select name from radrtechn"));
				this.add(Box.createVerticalGlue());
			}
		} catch (SQLException e){
			e.printStackTrace();
		}
	}
	
	@Override
	public STRController getController(){
		return (STRController) DatasManager.getController(Type.EXSA);
	}
	
	private JPanel buildPanel(JPanel panel, String query){
		panel.setLayout(new GridLayout(0, 3));
		int i = 0;
		try {
			Statement st = DatabaseManager.getCurrentExsa();
			ResultSet rs = st.executeQuery(query);
			while(rs.next()){
				JCheckBox chk = new JCheckBox(rs.getString(1));
				chk.addItemListener(itemCheckListener);
				checkBoxList.add(chk);
				panel.add(chk);	
				i++;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return panel;
	}
	
	@Override
	public void reset() {
		for(JCheckBox c : checkBoxList){
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
				if(mosaiques.equals(parent)){
					getController().showObject(STRController.MOSAIQUE, ((JCheckBox)e.getSource()).getText());
				} else if (capa.equals(parent)){
					getController().showObject(STRController.MOSAIQUE_CAPA, ((JCheckBox)e.getSource()).getText());
				} else if (dyn.equals(parent)){
					getController().showObject(STRController.MOSAIQUE_DYN, ((JCheckBox)e.getSource()).getText());
				}else if (zocc.equals(parent)){
					getController().showObject(STRController.MOSAIQUE_ZOCC, ((JCheckBox)e.getSource()).getText());
				}else if (vvf.equals(parent)){
					getController().showObject(STRController.MOSAIQUE_VVF, ((JCheckBox)e.getSource()).getText());
				} else if(radars.equals(parent)){
					getController().showObject(STRController.RADAR,((JCheckBox)e.getSource()).getText());
				}
			} else {
				if(mosaiques.equals(parent)){
					getController().hideObject(STRController.MOSAIQUE, ((JCheckBox)e.getSource()).getText());
				} else if (capa.equals(parent)){
					getController().hideObject(STRController.MOSAIQUE_CAPA, ((JCheckBox)e.getSource()).getText());
				} else if (dyn.equals(parent)){
					getController().hideObject(STRController.MOSAIQUE_DYN, ((JCheckBox)e.getSource()).getText());
				}else if (zocc.equals(parent)){
					getController().hideObject(STRController.MOSAIQUE_ZOCC, ((JCheckBox)e.getSource()).getText());
				}else if (vvf.equals(parent)){
					getController().hideObject(STRController.MOSAIQUE_VVF, ((JCheckBox)e.getSource()).getText());
				} else if(radars.equals(parent)){
					getController().hideObject(STRController.RADAR, ((JCheckBox)e.getSource()).getText());
				}
			}
		}
		
	}

	@Override
	public void showObject(int type, String name) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void hideObject(int type, String name) {
		// TODO Auto-generated method stub
		
	}


}
