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

package fr.crnan.videso3d.stpv;

import java.awt.event.ActionEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JLabel;

import org.jdesktop.swingx.JXTaskPane;

import fr.crnan.videso3d.Context;
import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.DatasManager;
import fr.crnan.videso3d.DatabaseManager.Type;
import fr.crnan.videso3d.ihm.AnalyzeUI;
/**
 * 
 * @author Bruno Spyckerelle
 * @version 0.2
 */
public class StpvContext extends Context {

	@Override
	public List<JXTaskPane> getTaskPanes(int type, final String name) {
		JXTaskPane taskpane = new JXTaskPane();
		taskpane.setTitle("Eléments STPV");
		switch (type) {
		case StpvController.SECTEUR:
			try {
				if(DatabaseManager.getCurrentStpv() == null){
					taskpane.add(new JLabel("<html><i>Aucune base STPV configurée.</i></html>"));
				} else {
					Statement st2 = DatabaseManager.getCurrentStpv();
					ResultSet rs2 = st2.executeQuery("select freq from sect where nom = '"+name+"'");
					if(rs2.next()){
						taskpane.add(new JLabel("<html><b>Fréquence : </b>"+rs2.getString(1)+"</html>"));
					}
					st2.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			break;
		case StpvController.BALISE:
			try{
				final Statement st2 = DatabaseManager.getCurrentStpv();
				if(st2 == null){
					taskpane.add(new JLabel("<html><i>Aucune base STPV configurée.</i></html>"));
				} else {
					taskpane.add(new AbstractAction() {
						{
							putValue(Action.NAME, "Possède "+(st2.executeQuery("select COUNT(*) from lieu26 where balise ='"+name+"'")).getInt(1)+" lieu(x) 26.");
						}
						@Override
						public void actionPerformed(ActionEvent e) {
							AnalyzeUI.showResults(false, "balise", name, "", "");
						}
					});
					taskpane.add(new AbstractAction() {
						{
							putValue(Action.NAME, "Possède "+(st2.executeQuery("select COUNT(*) from lieu27 where balise ='"+name+"'")).getInt(1)+" lieu(x) 27.");
						}
						@Override
						public void actionPerformed(ActionEvent e) {
							AnalyzeUI.showResults(false, "balise", name, "", "");
						}
					});
					taskpane.add(new AbstractAction() {
						{
							putValue(Action.NAME, "Possède "+(st2.executeQuery("select COUNT(*) from lieu8 where depart ='"+name+"' or arrivee = '"+name+"'")).getInt(1)+" lieu(x) 8.");
						}
						@Override
						public void actionPerformed(ActionEvent e) {
							AnalyzeUI.showResults(false, "balise", name, "", "");
						}
					});
					taskpane.add(new AbstractAction() {
						{
							putValue(Action.NAME, "Possède "+(st2.executeQuery("select COUNT(*) from lieu91 where bal1 ='"+name+"' or bal2='"+name+"'")).getInt(1)+" lieu(x) 91.");
						}
						@Override
						public void actionPerformed(ActionEvent e) {
							AnalyzeUI.showResults(false, "balise", name, "", "");
						}
					});
					taskpane.add(new AbstractAction() {
						{
							putValue(Action.NAME, "Possède "+(st2.executeQuery("select COUNT(*) from lieu6 where oaci ='"+name+"' or bal1='"+name+"'")).getInt(1)+" lieu(x) 6.");
						}
						@Override
						public void actionPerformed(ActionEvent e) {
							AnalyzeUI.showResults(false, "balise", name, "", "");
						}
					});
					taskpane.add(new AbstractAction() {
						{
							putValue(Action.NAME, "Appartient à "+(st2.executeQuery("select COUNT(*) from lieu90, lieu901 where lieu90.id = lieu901.lieu90 and (oaci ='"+name+"' or bal1='"+name+"'"
									+" or bal2='"+name+"' or bal3='"+name+"' or bal4='"+name+"'"
									+" or bal5='"+name+"' or bal6='"+name+"' or bal7='"+name+"' or bal8='"+name+"')")).getInt(1)+" star(s).");
						}
						@Override
						public void actionPerformed(ActionEvent e) {
							AnalyzeUI.showResults(false, "stars", name, "", "");
						}
					});
					taskpane.add(new JLabel("<html><b>Imprimabilité</b> :</html>"));
					ResultSet rs1 = st2.executeQuery("select nom_SLCT from imprSLCT where nom_balise ='"+name+"'");
					while(rs1.next()){
						taskpane.add(new JLabel("<html>Imprimable à <b>"+rs1.getString(1)+"</b></html>"));
					}
					ResultSet rs2 = st2.executeQuery("select * from bali where name='"+name+"'");
					if(rs2.next()){
						if(rs2.getInt("TMA")==0)
							taskpane.add(new JLabel("<html>Non imprimable sur strips tour</html>"));
						else if(rs2.getInt("TMA")==1)
							taskpane.add(new JLabel("<html>Imprimable sur strips tour</html>"));							
						for(int i = 4; i<13; i++){
							if(rs2.getInt(i)==1)
								taskpane.add(new JLabel("<html>Sect. "+(i-3)+" : oui</html>"));
							if(rs2.getInt(i)==0)
								taskpane.add(new JLabel("<html>Sect. "+(i-3)+" : non</html>"));
						}
					}
				}
			} catch (SQLException e){
				e.printStackTrace();
			}
			break;
		case StpvController.STAR:
			Integer id = new Integer(name);
			try {
				if(DatabaseManager.getCurrentStpv() == null){
					taskpane.add(new JLabel("<html><i>Aucune base STPV configurée.</i></html>"));
				} else {
					taskpane.add(new AbstractAction() {
						{
							putValue(Action.NAME, "Afficher la STAR sur la vue 3D");
						}
						@Override
						public void actionPerformed(ActionEvent e) {
							DatasManager.getController(Type.STPV).highlight(StpvController.STAR, name);
						}
					});
					Statement st3 = DatabaseManager.getCurrentStpv();
					ResultSet rs3 = st3.executeQuery("select * from lieu90 where id = '"+id+"'");
					if(rs3.next()){
						taskpane.add(new JLabel("<html><b>Hélices : </b>"+(rs3.getBoolean(12) ? "Oui" : "Non")+"</html>"));
						taskpane.add(new JLabel("<html><b>Jets : </b>"+(rs3.getBoolean(13) ? "Oui" : "Non")+"</html>"));
						taskpane.add(new JLabel("<html><b>FIR : </b>"+(rs3.getBoolean(14) ? "Oui" : "Non")+"</html>"));
						taskpane.add(new JLabel("<html><b>UIR : </b>"+(rs3.getBoolean(15) ? "Oui" : "Non")+"</html>"));
					}
					st3.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			break;
		default:
			break;
		}
		List<JXTaskPane> list = new ArrayList<JXTaskPane>();
		list.add(taskpane);
		return list;
	}

}
