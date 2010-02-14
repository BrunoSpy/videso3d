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
import java.awt.Component;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;

import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxEvent;

import fr.crnan.videso3d.DatabaseManager;

/**
 * Panneau de résultat représentant les trajets correspondants à la recherche
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class TrajetPanel extends ResultGraphPanel {

	public TrajetPanel(String balise) {
		super(balise);
	}

	/*
	 * (non-Javadoc)
	 * @see fr.crnan.videso3d.graphs.ResultGraphPanel#createGraphComponent(java.lang.String)
	 */
	@Override
	protected void createGraphComponent(final String balise) {

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
					ResultSet rs = st.executeQuery("select trajets.*, baltrajets.balise, baltrajets.balid, baltrajets.appartient from trajets, baltrajets where trajets.id = baltrajets.idtrajet and trajets.id in (select idtrajet from baltrajets where balise='"+balise+"')");

					progressBar.setValue(1);

					//ensemble des routes
					Set<mxCell> trajets = new HashSet<mxCell>();

					mxCell trajet = null;
					mxCell first = null;
					int eclatement_id = 0;
					int raccordement_id = 0;
					int idTrajet = 0;

					graph.getModel().beginUpdate();
					while(rs.next()){
						String name = rs.getString(16); 
						if(idTrajet != rs.getInt(1)){
							idTrajet = rs.getInt(1);
							//nouveau trajet
							if(rs.getInt(3) != eclatement_id || rs.getInt(5) != raccordement_id){
								//nouveau groupe
								trajet = (mxCell) graph.insertVertex(graph.getDefaultParent(), null, new CellContent(CellContent.TYPE_TRAJET, idTrajet, rs.getString(2)), 0, 0, 80, 50, GraphStyle.groupStyle);
								trajet.setConnectable(false);
								trajets.add(trajet);
								eclatement_id = rs.getInt(3);
								raccordement_id = rs.getInt(5);
							}
							first = (mxCell) graph.insertVertex(trajet, null, new CellContent(CellContent.TYPE_BALISE, rs.getInt(17), name), 0, 0, GraphStyle.baliseSize, GraphStyle.baliseSize, (balise.equals(name)? GraphStyle.baliseHighlight : GraphStyle.baliseStyle));
							first.setConnectable(false);
						} else {
							mxCell second = (mxCell) graph.insertVertex(trajet, null, new CellContent(CellContent.TYPE_BALISE, rs.getInt(17), name), 0, 0, GraphStyle.baliseSize, GraphStyle.baliseSize, (balise.equals(name)? GraphStyle.baliseHighlight : GraphStyle.baliseStyle));
							second.setConnectable(false);
							graph.insertEdge(trajet, null, "", first, second, GraphStyle.edgeStyle);
							first = second;
						}
					}

					progressBar.setValue(2);

					for(mxCell o : trajets){
						layout.execute(o);
						graph.addListener(mxEvent.CELLS_FOLDED, new CellFoldedListener(o, stack));
					}

					progressBar.setValue(3);

					graph.updateGroupBounds(trajets.toArray(), graph.getGridSize());

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
					component = new mxGraphComponent(graph);
					component.setBorder(null);
				} else {
					component = new JTextArea("\n Aucun résultat.");
				}
				
				add(component, BorderLayout.CENTER);
			}

		}.execute();
	}

}
