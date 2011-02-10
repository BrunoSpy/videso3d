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

package fr.crnan.videso3d.graphs;

import java.awt.BorderLayout;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;

import com.mxgraph.model.mxCell;
import com.mxgraph.util.mxEvent;

import fr.crnan.videso3d.DatabaseManager;

/**
 * Panneau de résultat représentant les trajets correspondants à la recherche
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class TrajetPanel extends ResultGraphPanel {

	public TrajetPanel(String balise, String balise2) {
		super(balise, balise2);
	}

	private String findTrajets(String balise1, String balise2){
		if(balise2.isEmpty()){
			return "select idtrajet as id from baltrajets where balise "+forgeSql(balise1);
		} else if(balise1.isEmpty()){
			return "select idtrajet as id from baltrajets where balise "+forgeSql(balise2);
		} else {
			return "select idtrajet as id from baltrajets where balise "+forgeSql(balise1)+ 
				   " INTERSECT "+ 
				   "select idtrajet as id from baltrajets where balise "+forgeSql(balise2);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see fr.crnan.videso3d.graphs.ResultGraphPanel#createGraphComponent(java.lang.String)
	 */
	@Override
	protected void createGraphComponent(final String balise1, final String balise2) {

		progressBar.setMinimum(0);
		progressBar.setMaximum(6);
		progressBar.setVisible(true);
		this.add(progressBar, BorderLayout.NORTH);

		//remplissage du graphe dans un swingworker pour éviter de figer l'IHM
		new SwingWorker<Integer, String>() {

			private Boolean hasResults = false;

			@Override
			protected Integer doInBackground() throws Exception {
				progressBar.setValue(0);

				try {
					Statement st = DatabaseManager.getCurrentStip();
					ResultSet rs = st.executeQuery("select trajets.*, baltrajets.balise, baltrajets.balid, baltrajets.appartient from trajets, baltrajets where trajets.id = baltrajets.idtrajet and trajets.id in ("+findTrajets(balise1, balise2)+")");

					progressBar.setValue(1);

					//ensemble des trajets
					Set<mxCell> trajets = new HashSet<mxCell>();
					Set<mxCell> trajetsGroupes = new HashSet<mxCell>();

					mxCell trajetGroupe = null;
					mxCell trajet = null;
					mxCell first = null;
					int eclatement_id = 0;
					int raccordement_id = 0;
					int idTrajet = 0;
					int count = 0;
					graph.getModel().beginUpdate();
					while(rs.next()){
						String name = rs.getString(16); 
						if(idTrajet != rs.getInt(1)){
							count++;
							idTrajet = rs.getInt(1);
							//nouveau trajet
							if(rs.getInt(3) != eclatement_id || rs.getInt(5) != raccordement_id){
								//nouveau groupe
								trajetGroupe = (mxCell) graph.insertVertex(graph.getDefaultParent(), null, new CellContent(CellContent.TYPE_TRAJET_GROUPE, idTrajet, rs.getString(2)), 0, 0, 80, 50, GraphStyle.groupStyle);
								trajetGroupe.setConnectable(false);
								trajetsGroupes.add(trajetGroupe);
								eclatement_id = rs.getInt(3);
								raccordement_id = rs.getInt(5);
							}
							String condition = "FL < "+rs.getString(7) + " - "+rs.getString(8)+" "+rs.getString(9);
							if(rs.getString(10) != null){
								condition += " + "+rs.getString(10)+" "+rs.getString(11);
							}
							if(rs.getString(12) != null){
								condition += " + "+rs.getString(12)+" "+rs.getString(13);
							}
							if(rs.getString(14) != null){
								condition += " + "+rs.getString(14)+" "+rs.getString(15);
							}
							trajet = (mxCell) graph.insertVertex(trajetGroupe, null, new CellContent(CellContent.TYPE_TRAJET, idTrajet, condition), 0, 0, 0, 0, GraphStyle.groupStyleFolded);
							trajet.setConnectable(false);
							trajets.add(trajet);
							first = (mxCell) graph.insertVertex(trajet, null, new CellContent(CellContent.TYPE_BALISE, rs.getInt(17), name), 0, 0, GraphStyle.baliseSize, GraphStyle.baliseSize, ((nameMatch(balise1, name) || nameMatch(balise2,name))? GraphStyle.baliseHighlight : GraphStyle.baliseStyle));
							first.setConnectable(false);
						} else {
							mxCell second = (mxCell) graph.insertVertex(trajet, null, new CellContent(CellContent.TYPE_BALISE, rs.getInt(17), name), 0, 0, GraphStyle.baliseSize, GraphStyle.baliseSize, ((nameMatch(balise1, name) || nameMatch(balise2,name))? GraphStyle.baliseHighlight : GraphStyle.baliseStyle));
							second.setConnectable(false);
							graph.insertEdge(trajet, null, "", first, second, GraphStyle.edgeStyle);
							first = second;
						}
					}
					fireNumberResults(count);
					progressBar.setValue(2);

					for(mxCell o : trajets){
						layout.execute(o);
						
					}

					graph.updateGroupBounds(trajets.toArray(), graph.getGridSize());
					
					for(mxCell o : trajetsGroupes){
						stack.execute(o);
						graph.addListener(mxEvent.CELLS_FOLDED, new CellFoldedListener(o, stack));
					}
					
					progressBar.setValue(3);

					graph.updateGroupBounds(trajetsGroupes.toArray(), 0);

					progressBar.setValue(4);

					stack.execute(graph.getDefaultParent());

					progressBar.setValue(5);

					graph.getModel().endUpdate();

					progressBar.setValue(6);

					graph.setCellsLocked(true);

					hasResults = (idTrajet != 0);

				} catch (SQLException e) {
					e.printStackTrace();
				}

				return null;
			}

			@Override
			protected void done() {
				super.done();
				progressBar.setVisible(false);

				JComponent component;
				
				if(hasResults){
					component = new VGraphComponent(graph);
					component.setBorder(null);
				} else {
					component = new JTextArea("\n Aucun résultat.");
				}
				
				add(component, BorderLayout.CENTER);
			}

		}.execute();
	}

}
