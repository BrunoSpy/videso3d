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

import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.DatabaseManager.Type;
import fr.crnan.videso3d.stip.StipController;
/**
 * Affichage des résultats de type Route
 * @author Bruno Spyckerelle
 * @version 0.1.1
 */
public class RoutePanel extends ResultGraphPanel {

	private String titleTab = "Route";
	
	public RoutePanel(boolean advanced, String... criteria){
		super(advanced, criteria);
	}

	private String findRoute(String balise1, String balise2){
		if(balise2.isEmpty()){
			return "select routeid as id from routebalise where balise "+forgeSql(balise1)+" UNION select id from routes where name "+forgeSql(balise1);
		} else if(balise1.isEmpty()){
			return "select routeid as id from routebalise where balise "+forgeSql(balise2)+" UNION select id from routes where name "+forgeSql(balise2);
		} else {
			return "select routeid as id from routebalise where balise "+forgeSql(balise1)+" INTERSECT select routeid as id from routebalise where balise "+forgeSql(balise2);
		}
	}
	
	protected void createGraphComponent(boolean advanced, final String... criteria) {

		final String balise1 = criteria[0];
		final String balise2 = criteria[1];
		
		titleTab += " "+balise1+ (balise2.isEmpty()? "" : " + "+balise2);
		
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
					ResultSet rs = st.executeQuery("select routebalise.routeid, routebalise.route, routebalise.id as balid, balise, appartient, sens, espace from routes, routebalise where routes.id = routebalise.routeid and routes.id in ("+findRoute(balise1, balise2)+") order by balid");

					progressBar.setValue(1);

					//ensemble des routes
					Set<mxCell> routes = new HashSet<mxCell>();

					mxCell route = null;
					mxCell first = null;
					String sens = "";
					int idRoute = 0;
					int count = 0;
					graph.getModel().beginUpdate();
					while(rs.next()){
						String name = rs.getString(4); 
						if(idRoute != rs.getInt(1)){
							count++;
							//ajout des sorties à la dernière route
							if(idRoute != 0){
								ResultSet rsSorties = DatabaseManager.getCurrentStip().executeQuery("select routesorties.sortie, routes.id from routes, routesorties where routes.id = routesorties.routeid and routes.id = '"+idRoute+"'");
								while(rsSorties.next()){
									mxCell sortie = (mxCell) graph.insertVertex(route, null, rsSorties.getString(1), 0, 0, GraphStyle.baliseSize, GraphStyle.baliseSize, GraphStyle.baliseTravers);
									graph.insertEdge(route, null, "", first, sortie, GraphStyle.edgeRouteSensUnique);
								}
								rsSorties.close();
							}
							
							idRoute = rs.getInt(1);
							//nouvelle route
							route = (mxCell) graph.insertVertex(graph.getDefaultParent(), null, new CellContent(Type.STIP, StipController.ROUTES, idRoute, rs.getString(2)), 0, 0, 80, 50, GraphStyle.groupStyleHorizontal);
							route.setConnectable(false);
							routes.add(route);
							String style = rs.getBoolean(5) ? 
									((nameMatch(balise1, name) || nameMatch(balise2,name)) ? GraphStyle.baliseHighlight : GraphStyle.baliseStyle) : 
										((nameMatch(balise1, name) || nameMatch(balise2,name)) ? GraphStyle.baliseTraversHighlight : GraphStyle.baliseTravers);
							first = (mxCell) graph.insertVertex(route, null, new CellContent(Type.STIP, StipController.BALISES, rs.getInt(3), name), 0, 0, GraphStyle.baliseSize, GraphStyle.baliseSize, style);
							first.setConnectable(false);
							//insertion des entrees
							ResultSet rsEntrees = DatabaseManager.getCurrentStip().executeQuery("select routeentrees.entree, routes.id from routes, routeentrees where routes.id = routeentrees.routeid and routes.id = '"+idRoute+"'");
							while(rsEntrees.next()){
								mxCell entree = (mxCell) graph.insertVertex(route, null, rsEntrees.getString(1), 0, 0, GraphStyle.baliseSize, GraphStyle.baliseSize, GraphStyle.baliseTravers);
								graph.insertEdge(route, null, "", entree, first, GraphStyle.edgeRouteSensUnique);
							}
							rsEntrees.close();
							sens = rs.getString(6);
						} else {
							String style = rs.getBoolean(5) ? 
									((nameMatch(balise1, name) || nameMatch(balise2,name)) ? GraphStyle.baliseHighlight : GraphStyle.baliseStyle) : 
										((nameMatch(balise1, name) || nameMatch(balise2,name)) ? GraphStyle.baliseTraversHighlight : GraphStyle.baliseTravers);
							mxCell second = (mxCell) graph.insertVertex(route, null, new CellContent(Type.STIP, StipController.BALISES, rs.getInt(3), name), 0, 0, GraphStyle.baliseSize, GraphStyle.baliseSize, style);
							second.setConnectable(false);
							style = "";
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

					//ajout des sorties à la dernière route
					if(idRoute != 0){
						ResultSet rsSorties = DatabaseManager.getCurrentStip().executeQuery("select routesorties.sortie, routes.id from routes, routesorties where routes.id = routesorties.routeid and routes.id = '"+idRoute+"'");
						while(rsSorties.next()){
							mxCell sortie = (mxCell) graph.insertVertex(route, null, rsSorties.getString(1), 0, 0, GraphStyle.baliseSize, GraphStyle.baliseSize, GraphStyle.baliseTravers);
							graph.insertEdge(route, null, "", first, sortie, GraphStyle.edgeRouteSensUnique);
						}
						rsSorties.close();
					}
					
					fireNumberResults(count);
					
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
				} catch (Exception e) {
					e.printStackTrace();
				}

				return null;
			}

			@Override
			protected void done() {
				super.done();
				progressBar.setVisible(false);
				final JComponent component;

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

	@Override
	public String getTitleTab() {
		return titleTab;
	}

}
