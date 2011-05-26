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
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.DatasManager;
import fr.crnan.videso3d.DatabaseManager.Type;
import fr.crnan.videso3d.exsa.STRController;
import fr.crnan.videso3d.ihm.components.DataView;
import fr.crnan.videso3d.ihm.components.TitleTwoButtons;
/**
 * Sélecteur de données STR
 * @author Bruno Spyckerelle
 * @version 0.2.8
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
	/**
	 * Stacks
	 */
	private JPanel stacks = new JPanel();
	/**
	 * TMA Filets
	 */
	private JPanel tmaF = new JPanel();
	/**
	 * TMA Filet dans la mosaïque
	 */
	private JPanel tmaFMosaique = new JPanel();
	
	private ItemCheckListener itemCheckListener = new ItemCheckListener();
	
	/**
	 * Liste des checkbox de la vue, afin de pouvoir tous les désélectionner facilement
	 */
	private List<JCheckBox> checkBoxList = new LinkedList<JCheckBox>();
		
	public StrView() {
			
		this.setLayout(new BorderLayout());
		
		JPanel container = new JPanel();
		
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
		
		mosaiques.setBorder(BorderFactory.createTitledBorder("Mosaïques"));
		capa.setBorder(BorderFactory.createTitledBorder("Filtrage capacitif"));
		dyn.setBorder(BorderFactory.createTitledBorder("Filtrage dynamique"));
		zocc.setBorder(BorderFactory.createTitledBorder("Zones d'occultation"));
		vvf.setBorder(BorderFactory.createTitledBorder("VVF"));
		radars.setBorder(BorderFactory.createTitledBorder("Portées radars"));
		stacks.setBorder(BorderFactory.createTitledBorder("Stacks"));
		tmaF.setBorder(BorderFactory.createTitledBorder("TMA Filet"));
		tmaFMosaique.setBorder(BorderFactory.createTitledBorder("Mosaiques des TMA Filet"));
		
		//affichage 2D/3D
		TitleTwoButtons style = new TitleTwoButtons("Style d'affichage : ", "2D", "3D", true);
		style.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				getController().set2D(e.getStateChange() == ItemEvent.SELECTED);
			}
		});
		style.setBorder(BorderFactory.createTitledBorder(" "));
		container.add(style);
		
		try {
			if(DatabaseManager.getCurrentExsa() != null) {
				container.add(this.buildPanel(mosaiques, "select type from centmosai"));
				container.add(this.buildPanel(capa, "select DISTINCT abonne from ficaafniv"));
				container.add(this.buildPanel(dyn, "select DISTINCT abonne from ficaafnic"));
				container.add(this.buildPanel(zocc, "select name from centzocc"));
				container.add(this.buildPanel(vvf, "select name from centflvvf"));
				container.add(this.buildPanel(stacks, "select name from centstack"));
				container.add(this.buildPanel(tmaF, "select name from centtmaf"));
				container.add(this.buildPanel(tmaFMosaique, "select DISTINCT name from centsctma"));
				container.add(this.buildPanel(radars, "select name from radrtechn"));
				//container.add(Box.createVerticalGlue());
			}
		} catch (SQLException e){
			e.printStackTrace();
		}
		
		JScrollPane scrollPane = new JScrollPane(container);
		scrollPane.setBorder(null);
		
		this.add(scrollPane, BorderLayout.CENTER);
		
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
				if(panel.equals(tmaFMosaique)){//TODO faire ça un peu mieux ...
					for(int v = 1; v<=3;v++){
						JCheckBox chk = new JCheckBox(rs.getString(1)+" V"+v);
						chk.addItemListener(itemCheckListener);
						checkBoxList.add(chk);
						panel.add(chk);	
						i++;
					}
				} else {
					JCheckBox chk = new JCheckBox(rs.getString(1));
					chk.addItemListener(itemCheckListener);
					checkBoxList.add(chk);
					panel.add(chk);	
					i++;
				}
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
				} else if(stacks.equals(parent)){
					getController().showObject(STRController.STACK,((JCheckBox)e.getSource()).getText());
				} else if(tmaF.equals(parent)){
					getController().showObject(STRController.TMA_F,((JCheckBox)e.getSource()).getText());
				} else if(tmaFMosaique.equals(parent)){
					getController().showObject(STRController.TMA_F_M,((JCheckBox)e.getSource()).getText());
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
				} else if(stacks.equals(parent)){
					getController().hideObject(STRController.STACK,((JCheckBox)e.getSource()).getText());
				} else if(tmaF.equals(parent)){
					getController().hideObject(STRController.TMA_F,((JCheckBox)e.getSource()).getText());
				}else if(tmaFMosaique.equals(parent)){
					getController().hideObject(STRController.TMA_F_M,((JCheckBox)e.getSource()).getText());
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
