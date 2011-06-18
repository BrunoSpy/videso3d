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
import com.mxgraph.util.mxEvent;

import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.DatabaseManager.Type;
import fr.crnan.videso3d.stip.StipController;
import fr.crnan.videso3d.stpv.StpvController;
/**
 * 
 * @author Bruno Spyckerelle
 * @version 0.1.0
 */
public class StarPanel extends ResultGraphPanel {

	public StarPanel(String balise, String balise2) {
		super(balise, balise2);
	}

	private String findStars(String balise1, String balise2){
		if(balise2.isEmpty() && !balise1.isEmpty()){
			return "select id from lieu90 where oaci "+forgeSql(balise1)+ 
			" UNION select id from lieu90 where bal1 "+forgeSql(balise1)+
			" UNION select id from lieu90 where bal2 "+forgeSql(balise1)+
			" UNION select id from lieu90 where bal3 "+forgeSql(balise1)+
			" UNION select id from lieu90 where bal4 "+forgeSql(balise1)+
			" UNION select id from lieu90 where bal5 "+forgeSql(balise1)+
			" UNION select id from lieu90 where bal6 "+forgeSql(balise1)+
			" UNION select id from lieu90 where bal7 "+forgeSql(balise1)+
			" UNION select id from lieu90 where bal8 "+forgeSql(balise1)+
			" UNION select lieu90 as id from lieu901 where name "+forgeSql(balise1);
		} else if(balise1.isEmpty() && !balise2.isEmpty()){
			return "select id from lieu90 where oaci "+forgeSql(balise2)+ 
			" UNION select id from lieu90 where bal1 "+forgeSql(balise2)+
			" UNION select id from lieu90 where bal2 "+forgeSql(balise2)+
			" UNION select id from lieu90 where bal3 "+forgeSql(balise2)+
			" UNION select id from lieu90 where bal4 "+forgeSql(balise2)+
			" UNION select id from lieu90 where bal5 "+forgeSql(balise2)+
			" UNION select id from lieu90 where bal6 "+forgeSql(balise2)+
			" UNION select id from lieu90 where bal7 "+forgeSql(balise2)+
			" UNION select id from lieu90 where bal8 "+forgeSql(balise2)+
			" UNION select lieu90 as id from lieu901 where name "+forgeSql(balise2);
		} else if(balise1.isEmpty() && balise2.isEmpty()){
			//cas impossible normalement
			return "";
		} else {
			return "select id from (select id from lieu90 where oaci "+forgeSql(balise1)+ 
			" UNION select id from lieu90 where bal1 "+forgeSql(balise1)+
			" UNION select id from lieu90 where bal2 "+forgeSql(balise1)+
			" UNION select id from lieu90 where bal3 "+forgeSql(balise1)+
			" UNION select id from lieu90 where bal4 "+forgeSql(balise1)+
			" UNION select id from lieu90 where bal5 "+forgeSql(balise1)+
			" UNION select id from lieu90 where bal6 "+forgeSql(balise1)+
			" UNION select id from lieu90 where bal7 "+forgeSql(balise1)+
			" UNION select id from lieu90 where bal8 "+forgeSql(balise1)+
			" UNION select lieu90 as id from lieu901 where name "+forgeSql(balise1)+") as ab"+ 
			" INTERSECT "+ 
			"select id from (select id from lieu90 where oaci "+forgeSql(balise2)+ 
			" UNION select id from lieu90 where bal1 "+forgeSql(balise2)+
			" UNION select id from lieu90 where bal2 "+forgeSql(balise2)+
			" UNION select id from lieu90 where bal3 "+forgeSql(balise2)+
			" UNION select id from lieu90 where bal4 "+forgeSql(balise2)+
			" UNION select id from lieu90 where bal5 "+forgeSql(balise2)+
			" UNION select id from lieu90 where bal6 "+forgeSql(balise2)+
			" UNION select id from lieu90 where bal7 "+forgeSql(balise2)+
			" UNION select id from lieu90 where bal8 "+forgeSql(balise2)+
			" UNION select lieu90 as id from lieu901 where name "+forgeSql(balise2)+") as cd";
		}
	}
	
	@Override
	protected void createGraphComponent(final String balise1, final String balise2) {
		progressBar.setMinimum(0);
		progressBar.setMaximum(8);
		progressBar.setVisible(true);
		this.add(progressBar, BorderLayout.NORTH);

		//remplissage du graphe dans un swingworker pour éviter de figer l'IHM
		new SwingWorker<Integer, String>() {

			private Boolean hasResults;

			@Override
			protected Integer doInBackground() throws Exception {

				progressBar.setValue(0);

				try {
					Statement st = DatabaseManager.getCurrentStpv();
					ResultSet rs = st.executeQuery("select oaci, balini, bal1, bal2, bal3, bal4, " +
							"bal5, bal6, bal7, bal8, hel, jet, fir, uir, lieu90, conf, name from lieu90, lieu901 where" +
							" lieu90.id = lieu901.lieu90 and lieu90 in ("+findStars(balise1, balise2)+")");

					progressBar.setValue(1);

					//ensemble des stars (conteneurs)
					Set<mxCell> stars = new HashSet<mxCell>();

					//ensemble des groupes de stars
					Set<mxCell> starsRoot = new HashSet<mxCell>();


					mxCell starRoot = null;
					mxCell star = null;
					mxCell first = null;
					String terrain = "";
					int id = 0;
					int count = 0;
					graph.getModel().beginUpdate();
					while(rs.next()){
						count++;
						//nouvelle star
						id = rs.getInt(15);			
						// Swimlane
						if(!terrain.equals(rs.getString(1))){
							//nouveau groupe de stars
							starRoot = (mxCell) graph.insertVertex(graph.getDefaultParent(), null, rs.getString(1), 0, 0, 80, 50, GraphStyle.groupStyle);
							starRoot.setConnectable(false);
							
							starsRoot.add(starRoot);
							terrain = rs.getString(1);
						}
						star = (mxCell) graph.insertVertex(starRoot, null, new CellContent(Type.STPV, StpvController.STAR, id, rs.getString(17)), 0, 0, 80, 50, GraphStyle.groupStyle);
						star.setConnectable(false);
						stars.add(star);
						first = null;
						for(int i=0; i<8;i++){
							if(!rs.getString(3+i).trim().isEmpty()){
								String name = rs.getString(3+i);
								String style = ((nameMatch(balise1, name) || nameMatch(balise2,name)) ? GraphStyle.baliseHighlight : GraphStyle.baliseStyle);
								mxCell bal = (mxCell) graph.insertVertex(star, null, new CellContent(Type.STIP, StipController.BALISES, 0, name), 0, 0, GraphStyle.baliseSize, GraphStyle.baliseSize, style);
								bal.setConnectable(false);
								if(first != null){
									graph.insertEdge(star, null, "", first, bal, GraphStyle.edgeStyle);
								}
								first = bal;
							}
						}
						
					}

					fireNumberResults(count);

					progressBar.setValue(2);

					hasResults = (count != 0);

					progressBar.setValue(3);

					for(mxCell o : stars){
						layout.execute(o);
					}

					graph.updateGroupBounds(stars.toArray(), graph.getGridSize());

					for(mxCell o : starsRoot){
						stack.execute(o);
						graph.addListener(mxEvent.CELLS_FOLDED, new CellFoldedListener(o, stack));
					}

					progressBar.setValue(5);


					graph.updateGroupBounds(starsRoot.toArray(), 0);

					progressBar.setValue(6);

					stack.execute(graph.getDefaultParent());

					progressBar.setValue(7);

					graph.getModel().endUpdate();

					progressBar.setValue(8);

					//les cellules ne doivent pas pouvoir bouger
					graph.setCellsLocked(true);

				} catch (SQLException e) {
					e.printStackTrace();
				} catch (Exception e){
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
