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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;

import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;

import fr.crnan.videso3d.DatabaseManager;
/**
 * Affichage des résultats de type Route
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class RoutePanel extends ResultGraphPanel {

	public RoutePanel(String balise, String balise2){
		super(balise, balise2);
	}

	protected void createGraphComponent(final String balise, final String balise2) {

		progressBar.setMinimum(0);
		progressBar.setMaximum(6);
		progressBar.setVisible(true);
		this.add(progressBar, BorderLayout.NORTH);

		//remplissage du graphe dans un swingworker pour éviter de figer l'IHM
		new SwingWorker<Integer, String>() {

			private Boolean hasResults;
			
			@Override
			protected Integer doInBackground() {
				progressBar.setValue(0);


				try {
					Statement st = DatabaseManager.getCurrentStip();
					ResultSet rs = st.executeQuery("select routebalise.routeid, routebalise.route, routebalise.id as balid, balise, appartient, sens, espace from routes, routebalise where routes.id = routebalise.routeid and routes.id in (select routeid from routebalise where balise='"+balise+"')");

					progressBar.setValue(1);

					//ensemble des routes
					Set<mxCell> routes = new HashSet<mxCell>();

					mxCell route = null;
					mxCell first = null;
					String sens = "";
					int idRoute = 0;

					graph.getModel().beginUpdate();
					while(rs.next()){
						String name = rs.getString(4); 
						if(idRoute != rs.getInt(1)){
							idRoute = rs.getInt(1);
							//nouvelle route
							route = (mxCell) graph.insertVertex(graph.getDefaultParent(), null, new CellContent(CellContent.TYPE_ROUTE, idRoute, rs.getString(2)), 0, 0, 80, 50, GraphStyle.groupStyleHorizontal);
							route.setConnectable(false);
							routes.add(route);
							first = (mxCell) graph.insertVertex(route, null, new CellContent(CellContent.TYPE_BALISE, rs.getInt(3), name), 0, 0, GraphStyle.baliseSize, GraphStyle.baliseSize, (balise.equals(name)? GraphStyle.baliseHighlight : GraphStyle.baliseStyle));
							first.setConnectable(false);
							sens = rs.getString(6);
						} else {
							mxCell second = (mxCell) graph.insertVertex(route, null, new CellContent(CellContent.TYPE_BALISE, rs.getInt(3), name), 0, 0, GraphStyle.baliseSize, GraphStyle.baliseSize, (balise.equals(name)? GraphStyle.baliseHighlight : GraphStyle.baliseStyle));
							second.setConnectable(false);
							String style = "";
							if(sens.equals("=")){
								style = GraphStyle.edgeRoute;
							} else if(sens.equals(">")){
								style = GraphStyle.edgeRouteSensUnique;
							} else if(sens.equals("<")){
								style = GraphStyle.edgeRouteSensUniqueInverse;
							} else if(sens.equals("+")){
								style = GraphStyle.edgeRouteSensInterdit;
							}
							graph.insertEdge(route, null, "", first, second, style);
							sens = rs.getString(6);
							first = second;
						}
					}

					progressBar.setValue(2);

					for(mxCell o : routes){
						layout.execute(o);
					}

					progressBar.setValue(3);

					graph.updateGroupBounds(routes.toArray(), graph.getGridSize());

					progressBar.setValue(4);

					stack.execute(graph.getDefaultParent());

					progressBar.setValue(5);

					graph.getModel().endUpdate();

					progressBar.setValue(6);

					graph.setCellsLocked(true);
					
					hasResults = (idRoute != 0);

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
