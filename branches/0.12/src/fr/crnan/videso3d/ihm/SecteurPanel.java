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
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import org.jdesktop.swingx.JXTable;

import fr.crnan.videso3d.databases.DatabaseManager;
import fr.crnan.videso3d.databases.DatabaseManager.Type;
import fr.crnan.videso3d.ihm.components.TitledPanel;
import fr.crnan.videso3d.databases.stip.Stip;
import fr.crnan.videso3d.databases.stip.StipController;
import fr.crnan.videso3d.databases.stpv.Stpv;
import fr.crnan.videso3d.databases.stpv.StpvController;

/**
 * Résultats de données Stip/Stpv sur un secteur
 * @author Adrien Vidal
 * @version 0.3
 */
public class SecteurPanel extends ResultPanel {

	private String titleTab = "Secteur";
	
	private ContextPanel context;
	
	public SecteurPanel(String secteur){
		
		titleTab += " "+secteur;
		
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		Boolean stip = false;
		Boolean stpv = false;
		try {
			stip = DatabaseManager.getCurrentStip() != null;
			stpv = DatabaseManager.getCurrentStpv() != null;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		Component panel = null;
		
		JPanel top = new JPanel();
		top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));
		
		JPanel bottom = null;
		
		Vector<Component> panels = new Vector<Component>();
		
		if(stip && (panel = this.createBalisesTable(secteur)) != null) {
			panels.add(panel);
		}
		if(stpv && (panel = this.createLieu91Table(secteur)) != null){
			panels.add(panel);
		}
		if(stpv && (panel = this.createCoorTable(secteur)) != null) {
			panels.add(panel);
		}
		if(stpv && (panel = this.createDoubleTable(secteur)) != null) {
			panels.add(panel);
		}
		
		if(panels.size() <= 2){ //cas particulier, en dessous de 2 éléments, on mets les résultats sur une seule ligne
			for(Component c : panels) {
				top.add(c);
			}
		} else {
			bottom = new JPanel();
			bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
			double middle = panels.size() /2.;
			for(int i = 0; i < Math.ceil(middle); i++){
				top.add(panels.get(i));
			}
			for(int i = (int) Math.ceil(middle); i<panels.size(); i++){
				bottom.add(panels.get(i));
			}
		}
		
		this.add(top);
		if(bottom != null) this.add(bottom);
	}

	private Component createBalisesTable(String secteur) {
		final JXTable table = new JXTable(){
			//Redéfinition de processKeyEvent pour faire le copier/coller au même format que dans la BDS
			@Override
			protected void processKeyEvent(KeyEvent e){
				String balise = new String();
				if(e.isControlDown() && e.getKeyCode() == KeyEvent.VK_C){
					int[] rows = this.getSelectedRows();
					for(int i=0; i< rows.length; i++){
						int id = (Integer)this.getModel().getValueAt(this.convertRowIndexToModel(this.getSelectedRows()[i]), this.getModel().getColumnCount()-1);
						balise += Stip.getString(StipController.BALISES, id)+"\n";
					}
					balise+="\n";
					Clipboard clipBoard = Toolkit.getDefaultToolkit().getSystemClipboard();
					StringSelection ss = new StringSelection(balise);
					clipBoard.setContents(ss , ss);
				}
			}
		};
		table.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent e){
				if(e.getClickCount()==2){
					JXTable target = (JXTable)e.getSource();
					int row = target.getSelectedRow();
					context.showInfo(Type.STIP, StipController.BALISES, (String) target.getValueAt(row, 0));
				}
			}
		});
		table.setHorizontalScrollEnabled(true);
		table.setEditable(false);
		table.setColumnControlVisible(true);
		String[] columns = {"Nom", "FL inf.", "FL sup.", "Commentaire", "id"};
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		model.setColumnIdentifiers(columns);
		try {
			Statement st = DatabaseManager.getCurrentStip();
			ResultSet rs = st.executeQuery("select name, definition, sect1, limit1, sect2, limit2, sect3, limit3, sect4, limit4, sect5, limit5," +
					" sect6, limit6, sect7, limit7, sect8, limit8, sect9, limit9, id from balises " +
					"where sect1 ='"+secteur+"' or sect2 ='"+secteur+"' or sect3 ='"+secteur+"' or sect4 ='"+secteur+"' or sect5 ='"+secteur+"'" +
					" or sect6 ='"+secteur+"' or sect7 ='"+secteur+"' or sect8 ='"+secteur+"' or sect9 ='"+secteur+"'");
			while(rs.next()){
				Object[] objects = new Object[columns.length];
				objects[0] = rs.getString(1);
				for(int i=0; i<9; i++){
					if(rs.getString(3+2*i).equals(secteur)){
						if(i>0)
							objects[1] = rs.getInt(2+2*i);
						else
							objects[1] = 0;
						objects[2] = rs.getInt(4+2*i);
					}
				}
				objects[3] = rs.getString(2);
				objects[4] = rs.getInt(21);
				
				model.addRow(objects);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch(Exception e){
			e.printStackTrace();
		}
		if(model.getRowCount() > 0){
			table.setModel(model);
			table.removeColumn(table.getColumn(table.getColumnCount()-1));
			table.packAll();
			JPanel balises = new JPanel();
			balises.setLayout(new BorderLayout());
			balises.add(new TitledPanel("Balises"), BorderLayout.PAGE_START);
			balises.add(new JScrollPane(table), BorderLayout.CENTER);
			return balises;
		} else {
			return null;
		}
	}

	private Component createLieu91Table(String secteur) {
		final JXTable table = new JXTable(){
			//Redéfinition de processKeyEvent pour faire le copier/coller au même format que dans la BDS
			@Override
			protected void processKeyEvent(KeyEvent e){
				String tfl = new String();
				if(e.isControlDown() && e.getKeyCode() == KeyEvent.VK_C){
					int[] rows = this.getSelectedRows();
					for(int i=0; i< rows.length; i++){
						int id = (Integer)this.getModel().getValueAt(this.convertRowIndexToModel(this.getSelectedRows()[i]), this.getModel().getColumnCount()-1);
						tfl += Stpv.getString(StpvController.TFL, id)+"\n";
					}
					tfl+="\n";
					Clipboard clipBoard = Toolkit.getDefaultToolkit().getSystemClipboard();
					StringSelection ss = new StringSelection(tfl);
					clipBoard.setContents(ss , ss);
				}
			}
		};
		table.setHorizontalScrollEnabled(true);
		table.setEditable(false);
		table.setColumnControlVisible(true);
		String[] columns = {"Terrain", "Type", "Donnant", "Recevant", "Balise 1", "Balise 2", "Piste", "Avion", "TFL", "Terrain 1", "Conf 1", 
				"Terrain 2", "Conf 2", "id"};
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		model.setColumnIdentifiers(columns);

		try {
			Statement st = DatabaseManager.getCurrentStpv();
			ResultSet rs = st.executeQuery("select oaci, indicateur, secteur_donnant, secteur_recevant, bal1, bal2, piste, avion, tfl, terrain1, conf1, " +
					"terrain2, conf2, id from lieu91 where secteur_donnant ='"+secteur+"' or secteur_recevant ='"+secteur+"'");
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();
			while(rs.next()){
				Object[] objects = new Object[columnCount];
				for(int i=0; i<columnCount;i++){
					objects[i] = rs.getObject(i+1);
				}
				model.addRow(objects);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch(Exception e){
			e.printStackTrace();
		}
		if(model.getRowCount() > 0){
			table.setModel(model);
			table.removeColumn(table.getColumn(table.getColumnCount()-1));
			table.packAll();

			JPanel lieux91 = new JPanel();
			lieux91.setLayout(new BorderLayout());
			lieux91.add(new TitledPanel("TFL (lieu 91)"), BorderLayout.PAGE_START);
			lieux91.add(new JScrollPane(table), BorderLayout.CENTER);
			return lieux91;
		} else {
			return null;
		}
	}


	private Component createDoubleTable(String secteur) {
		final String[] toolTips = {"Entité d'origine du vol","Entité destinataire du doublement","Identifiant du doublement",
				"Première balise de l'axe du doublement","Deuxième balise de l'axe du doublement","Niveau plancher sur la balise 1",
				"Niveau plafond sur la balise 1","Niveau plancher sur la balise 2","Niveau plafond sur la balise 2"};
		final JXTable t = new JXTable(){
			//Implement table header tool tips.
            protected JTableHeader createDefaultTableHeader() {
                return new JTableHeader(columnModel) {
                    public String getToolTipText(MouseEvent e) {
                        java.awt.Point p = e.getPoint();
                        int index = columnModel.getColumnIndexAtX(p.x);
                        int realIndex = columnModel.getColumn(index).getModelIndex();
                        return toolTips[realIndex];
                    }
                };
            }
			
			//Redéfinition de processKeyEvent pour faire le copier/coller au même format que dans la BDS
			@Override
			protected void processKeyEvent(KeyEvent e){
				String doublements = new String();
				if(e.isControlDown() && e.getKeyCode() == KeyEvent.VK_C){
					int[] rows = this.getSelectedRows();
					for(int i=0; i< rows.length; i++){
						int row = rows[i];
						doublements += "CONF 12D "+( getValueInView(this, row, 3).equals("Oui") ? "     " : "INHI ")+getValueInView(this, row, 0)+"    "
						+getValueInView(this, row, 2)+"    "+getValueInView(this, row, 1)+"          "
						+Stip.completerBalise((String)getValueInView(this, row, 4))+" "+Stip.completerBalise((String) getValueInView(this, row, 5));
						String flinf1 = (String) getValueInView(this, row, 6);
						if(flinf1!=null){
							doublements+="   "+flinf1+"   "+getValueInView(this, row, 7);
							String flinf2 = (String) getValueInView(this, row, 8);
							if(flinf2!=null){
								doublements+="   "+flinf2+"   "+getValueInView(this, row, 9);
							}
						}
						doublements+="\n";
					}
					doublements+="\n";
					Clipboard clipBoard = Toolkit.getDefaultToolkit().getSystemClipboard();
					StringSelection ss = new StringSelection(doublements);
					clipBoard.setContents(ss , ss);
				}
			}
		};
		t.setHorizontalScrollEnabled(true);
		t.setEditable(false);
		t.setColumnControlVisible(true);
		String[] columns = {"Entité", "Destinataire", "Ident.", "Strip", "Balise 1", "Balise 2", "FL inf 1", "FL sup 1", 
				"FL inf 2", "FL sup 2"};
		DefaultTableModel model = (DefaultTableModel) t.getModel();
		model.setColumnIdentifiers(columns);

		try {
			Statement st = DatabaseManager.getCurrentStpv();
			ResultSet rs = st.executeQuery("select * from double where entite ='"+secteur+"' or destinataire ='"+secteur+"' order by entite");
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();
			while(rs.next()){
				Object[] objects = new Object[columnCount-1];
				for(int i=0; i<objects.length;i++){
					objects[i] = rs.getObject(i+2);//on commence à 2 car on n'a pas besoin de l'id
				}
				model.addRow(objects);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch(Exception e){
			e.printStackTrace();
		}
		if(model.getRowCount() > 0){
			t.setModel(model);
			t.packAll();
			JPanel doubles = new JPanel();
			doubles.setLayout(new BorderLayout());
			doubles.add(new TitledPanel("Doubles"), BorderLayout.PAGE_START);
			doubles.add(new JScrollPane(t), BorderLayout.CENTER);
			return doubles;
		} else {
			return null;
		}
	}
	
	private Component createCoorTable(String secteur) {
		final String[] toolTips = {"COOR 30 : coordination entre secteurs; COOR 40 : coordination sur un axe","Secteur donnant","Secteur recevant",
				"Première balise de l'axe d'échange","Deuxième balise de l'axe d'échange","COP = balise de référence",
				"Distance en Nm entre le COP et la limite entre les deux secteurs",
				"Temps en minutes à retrancher à l'heure de passage sur la limite secteur pour obtenir la sortie du strip secteur suivant",
				"Distance en Nm à retrancher à l'heure de passage sur la limite secteur pour obtenir la sortie du strip secteur suivant",
				"Temps en minutes à retrancher à l'heure de passage du COP pour obtenir l'heure d'éveil sur le secteur suivant"};
		final JXTable t = new JXTable(){
			//Implement table header tool tips.
            protected JTableHeader createDefaultTableHeader() {
                return new JTableHeader(columnModel) {
                    public String getToolTipText(MouseEvent e) {
                        java.awt.Point p = e.getPoint();
                        int index = columnModel.getColumnIndexAtX(p.x);
                        int realIndex = columnModel.getColumn(index).getModelIndex();
                        return toolTips[realIndex];
                    }
                };
            }
		};
		t.setHorizontalScrollEnabled(true);
		t.setEditable(false);
		t.setColumnControlVisible(true);
		String[] columns = {"COOR 30/40", "Donnant", "Recevant", "Balise 1", "Balise 2", "COP", "COP -> limite sect", "Sortie strip suivant(temps)", 
				"Sortie strip suivant(distance)", "Éveil suivant"};
		DefaultTableModel model = (DefaultTableModel) t.getModel();
		model.setColumnIdentifiers(columns);

		try {
			Statement st = DatabaseManager.getCurrentStpv();
			ResultSet rs = st.executeQuery("select * from coor30 where donnant ='"+secteur+"' or recevant ='"+secteur+"'");
			while(rs.next()){
				Object[] objects = new Object[10];
				objects[0] = "COOR 30";
				objects[1] = rs.getString(2);
				objects[2] = rs.getString(3);
				for(int i=3; i<6;i++){
					objects[i]="";
				}
				for(int i=6; i<columns.length;i++){
					objects[i]=rs.getString(i-2);
				}
				model.addRow(objects);
			}
			rs = st.executeQuery("select * from coor40 where donnant ='"+secteur+"' or recevant ='"+secteur+"'");
			while(rs.next()){
				Object[] objects = new Object[10];
				objects[0] = "COOR 40";
				
				for(int i=1; i<columns.length;i++){
					objects[i]=rs.getString(i+1);;
				}
				model.addRow(objects);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch(Exception e){
			e.printStackTrace();
		}
		if(model.getRowCount() > 0){
			t.setModel(model);
			t.packAll();
			JPanel doubles = new JPanel();
			doubles.setLayout(new BorderLayout());
			doubles.add(new TitledPanel("Coordinations"), BorderLayout.PAGE_START);
			doubles.add(new JScrollPane(t), BorderLayout.CENTER);
			return doubles;
		} else {
			return null;
		}
	}

	private Object getValueInView(JXTable t, int row, int col){
		return t.getModel().getValueAt(t.convertRowIndexToModel(row), col);
	}

	
	@Override
	public void setContext(ContextPanel context) {
		this.context = context;
	}



	@Override
	public String getTitleTab() {
		return this.titleTab;
	}

}
