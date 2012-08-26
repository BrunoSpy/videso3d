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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;

import com.mxgraph.model.mxCell;
import com.mxgraph.util.mxEvent;

import fr.crnan.videso3d.DatasManager;
import fr.crnan.videso3d.DatasManager.Type;
import fr.crnan.videso3d.databases.DatabaseManager;
import fr.crnan.videso3d.databases.stip.StipController;

/**
 * Connexions recherchées, sous forme de graphe
 * @author Bruno Spyckerelle
 * @version 0.1.1
 */
public class ConnexPanel extends ResultGraphPanel {

	private String titleTab = "Connex";

	public ConnexPanel(boolean advanced, String... criteria){
		super(advanced, criteria);
	}

	private String findConnex(String balise1, String balise2){
		if(balise2.isEmpty() && !balise1.isEmpty()){
			return "select idconn from balconnexions where balise "+forgeSql(balise1)+ 
			" UNION select id as idconn from connexions where connexion "+forgeSql(balise1) +" or terrain "+forgeSql(balise1);
		} else if(balise1.isEmpty() && !balise2.isEmpty()){
			return "select idconn from balconnexions where balise "+forgeSql(balise2);
		} else if(balise1.isEmpty() && balise2.isEmpty()){
			//cas impossible normalement
			return "";
		} else {
			return "select idconn from (select idconn from balconnexions where balise "+forgeSql(balise1)+ 
			" UNION select id as idconn from connexions where connexion "+forgeSql(balise1)+" or terrain "+forgeSql(balise1)+") as ab"+ 
			" INTERSECT "+ 
			"select idconn from (select idconn from balconnexions where balise "+forgeSql(balise2)+ 
			" UNION select id as idconn from connexions where connexion "+forgeSql(balise2)+" or terrain "+forgeSql(balise2)+") as cd";
		}
	}
	
	@Override
	protected void createGraphComponent(boolean advanced, final String... criteria) {
		
		final String balise1 = criteria[0];
		final String balise2 = criteria[1];
		
		titleTab += " "+balise1+ (balise2.isEmpty()? "" : " + "+balise2);

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
					Statement st = DatabaseManager.getCurrentStip();
					ResultSet rs = st.executeQuery("select * from connexions, balconnexions where connexions.id = balconnexions.idconn and idconn in ("+findConnex(balise1, balise2)+")");

					progressBar.setValue(1);

					//ensemble des connexions (conteneurs)
					Set<mxCell> connexions = new HashSet<mxCell>();

					//ensemble des groupes de connexions
					Set<mxCell> connexionsRoot = new HashSet<mxCell>();

					//liste des balises pour l'ajout de trajet/balint

//					HashMap<Integer, HashMap<Integer, mxCell>> balisesByItis = new HashMap<Integer, HashMap<Integer, mxCell>>();

					HashMap<Integer, mxCell> balises = null;

					mxCell connexRoot = null;
					mxCell connex = null;
					mxCell first = null;
					String connexion = "";
					String terrain = "";
					String type = "";
					int id = 0;
					int count = 0;
					graph.getModel().beginUpdate();
					while(rs.next()){
						String name = rs.getString(12);
						if(id != rs.getInt(11)) {
							count++;
							if(id != 0){//on termine l'iti précédent si il existe
								if(type.equals("D")){
									mxCell balConnex = (mxCell) graph.insertVertex(connex, null, new CellContent(DatasManager.Type.STIP, StipController.BALISES, 0, connexion), 0, 0, GraphStyle.baliseSize, GraphStyle.baliseSize, GraphStyle.baliseDefault);
									graph.insertEdge(connex, null, "", first, balConnex, GraphStyle.edgeStyle);
								}
								
//								first.setStyle(GraphStyle.baliseDefault);
//								balisesByItis.put(id, balises);
							}
							//nouvelle connexion
							id = rs.getInt(11);
							balises = new HashMap<Integer, mxCell>();			
							// Swimlane
							if(!terrain.equals(rs.getString(2))){
								//nouveau groupe de connexions
								connexRoot = (mxCell) graph.insertVertex(graph.getDefaultParent(), null, rs.getString(2), 0, 0, 80, 50, GraphStyle.groupStyle);
								connexRoot.setConnectable(false);
								connexionsRoot.add(connexRoot);
								terrain = rs.getString(2);
							}
							connex = (mxCell) graph.insertVertex(connexRoot, null, new CellContent(DatasManager.Type.STIP, StipController.CONNEXION, id, rs.getString(6)+"->"+rs.getString(7)), 0, 0, 80, 50, GraphStyle.groupStyle);
							connex.setConnectable(false);
							connexions.add(connex);							
							
							String style = rs.getBoolean(14) ? 
									((nameMatch(balise1, name) || nameMatch(balise2,name)) ? GraphStyle.baliseHighlight : GraphStyle.baliseStyle) : 
										((nameMatch(balise1, name) || nameMatch(balise2,name)) ? GraphStyle.baliseTraversHighlight : GraphStyle.baliseTravers);
							first = (mxCell) graph.insertVertex(connex, null, new CellContent(DatasManager.Type.STIP, StipController.BALISES, 0, name), 0, 0, GraphStyle.baliseSize, GraphStyle.baliseSize, style);
							first.setConnectable(false);
							balises.put(rs.getInt(13), first);
							
							if(rs.getString(4).equals("A")){
								connexion = rs.getString(3);
								mxCell balConnex = (mxCell) graph.insertVertex(connex, null, new CellContent(DatasManager.Type.STIP, StipController.BALISES, 0, connexion), 0, 0, GraphStyle.baliseSize, GraphStyle.baliseSize, GraphStyle.baliseDefault);
								graph.insertEdge(connex, null, "", balConnex, first, GraphStyle.edgeStyle);
							}
							
						} else {
							String style = rs.getBoolean(14) ? 
									((nameMatch(balise1, name) || nameMatch(balise2,name)) ? GraphStyle.baliseHighlight : GraphStyle.baliseStyle) : 
										((nameMatch(balise1, name) || nameMatch(balise2,name)) ? GraphStyle.baliseTraversHighlight : GraphStyle.baliseTravers);
							mxCell bal = (mxCell) graph.insertVertex(connex, null, new CellContent(DatasManager.Type.STIP, StipController.BALISES, 0, name), 0, 0, GraphStyle.baliseSize, GraphStyle.baliseSize, style);
							bal.setConnectable(false);
							graph.insertEdge(connex, null, "", first, bal, GraphStyle.edgeStyle);
							balises.put(rs.getInt(13), bal);
							first  = bal;
						}
						//on enregistre le nom de la balise de connexion pour pouvoir fermer la connexion en cas de type D
						connexion = rs.getString(3);
						type = rs.getString(4);
					}

					fireNumberResults(count);

					progressBar.setValue(2);

					//on termine le dernier iti si il existe
					if(connex != null){
						if(type.equals("D")){
							mxCell balConnex = (mxCell) graph.insertVertex(connex, null, new CellContent(DatasManager.Type.STIP, StipController.BALISES, 0, connexion), 0, 0, GraphStyle.baliseSize, GraphStyle.baliseSize, GraphStyle.baliseDefault);
							graph.insertEdge(connex, null, "", first, balConnex, GraphStyle.edgeStyle);
						}
//						first.setStyle(GraphStyle.baliseDefault);
//						balisesByItis.put(id, balises);
					}

					hasResults = (id != 0);

					progressBar.setValue(3);

//					rs = st.executeQuery("select iditi, trajetid, raccordement_id, cond1, balise, balid from couple_trajets, baltrajets where couple_trajets.trajetid = baltrajets.idtrajet and iditi in ("+findConnex(balise1, balise2)+") ");
//
//					progressBar.setValue(4);
//
//					id = 0; //id des trajets
//					int idBal = 0;//id de la première balise du trajet
//					Object parent = null;
//					mxCell second = null;
//					//ajout des trajets
//					while(rs.next()){
//						//String balise = rs.getString(5);
//						if(id != rs.getInt(2) || rs.getInt(6) == idBal){
//							//nouveau trajet
//							first = balisesByItis.get(rs.getInt(1)).get(rs.getInt(6));
//							parent = first.getParent();
//							id = rs.getInt(2);
//							idBal = rs.getInt(6);
//							second = null;
//						} else {
//							if(rs.getInt(3) == rs.getInt(6)){ //on raccorde
//								graph.insertEdge(parent, null, "", first, balisesByItis.get(rs.getInt(1)).get(rs.getInt(6)), GraphStyle.edgeTrajet);
//							} else {
//								if(second == null) {
//									second = (mxCell) graph.insertVertex(parent, null, new CellContent(CellContent.TYPE_BALISE, 0, rs.getString(5)), 0, 0, GraphStyle.baliseSize, GraphStyle.baliseSize, GraphStyle.baliseTrajet);
//									second.setConnectable(false);
//									graph.insertEdge(parent, null, rs.getString(4), first, second, GraphStyle.edgeTrajet);
//								} else {
//									second = (mxCell) graph.insertVertex(parent, null, new CellContent(CellContent.TYPE_BALISE, 0, rs.getString(5)), 0, 0, GraphStyle.baliseSize, GraphStyle.baliseSize, GraphStyle.baliseTrajet);
//									second.setConnectable(false);
//									graph.insertEdge(parent, null, "", first, second, GraphStyle.edgeTrajet);
//								}
//							}
//							first = second;
//						}
//					}

					progressBar.setValue(4);

					for(mxCell o : connexions){
						layout.execute(o);
					}

					graph.updateGroupBounds(connexions.toArray(), graph.getGridSize());

					for(mxCell o : connexionsRoot){
						stack.execute(o);
						graph.addListener(mxEvent.CELLS_FOLDED, new CellFoldedListener(o, stack));
					}

					progressBar.setValue(5);


					graph.updateGroupBounds(connexionsRoot.toArray(), 0);

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

	@Override
	public String getTitleTab() {
		return this.titleTab;
	}

}
