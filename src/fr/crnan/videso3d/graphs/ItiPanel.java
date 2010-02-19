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

import fr.crnan.videso3d.DatabaseManager;


/**
 * Itis recherchés sour forme de graphe
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class ItiPanel extends ResultGraphPanel {

	public ItiPanel(String balise, String balise2) {
		super(balise, balise2);
	}

	private String findItis(String balise1, String balise2){
		if(balise2.isEmpty()){
			return "select iditi from balitis where balise "+forgeSql(balise1)+ 
			   " UNION select id as iditi from itis where entree "+forgeSql(balise1);
		} else {
			return "select iditi from (select iditi from balitis where balise "+forgeSql(balise1)+ 
				   " UNION select id as iditi from itis where entree "+forgeSql(balise1)+") as ab"+ 
				   " INTERSECT "+ 
				   "select iditi from (select iditi from balitis where balise "+forgeSql(balise2)+ 
				   " UNION select id as iditi from itis where sortie "+forgeSql(balise2)+") as cd";
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see fr.crnan.videso3d.graphs.ResultGraphPanel#createGraphComponent(java.lang.String)
	 */
	@Override
	protected void createGraphComponent(final String balise1, final String balise2){

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
					ResultSet rs = st.executeQuery("select balise, appartient, iditi, balid, entree, sortie from balitis, itis where itis.id = balitis.iditi and iditi in ("+findItis(balise1, balise2)+")");
					
					progressBar.setValue(1);

					//ensemble des itis (conteneurs)
					Set<mxCell> itis = new HashSet<mxCell>();
					
					//ensemble des groupes d'itis
					Set<mxCell> itisRoot = new HashSet<mxCell>();
					
					//liste des balises pour l'ajout de trajet/balint

					HashMap<Integer, HashMap<Integer, mxCell>> balisesByItis = new HashMap<Integer, HashMap<Integer, mxCell>>();

					HashMap<Integer, mxCell> balises = null;

					mxCell itiRoot = null;
					mxCell iti = null;
					mxCell first = null;
					String entree = "";
					int id = 0;
					int count = 0;
					graph.getModel().beginUpdate();
					while(rs.next()){
						String name = rs.getString(1);
						if(id != rs.getInt(3)) {
							count++;
							if(id != 0){//on termine l'iti précédent si il existe
								first.setStyle(GraphStyle.baliseDefault);
								balisesByItis.put(id, balises);
							}
							//nouvel iti
							id = rs.getInt(3);
							balises = new HashMap<Integer, mxCell>();			
							// Swimlane
							if(!entree.equals(rs.getString(5))){
								//nouveau groupe d'itis
								itiRoot = (mxCell) graph.insertVertex(graph.getDefaultParent(), null, rs.getString(5), 0, 0, 80, 50, GraphStyle.groupStyle);
								itiRoot.setConnectable(false);
								itisRoot.add(itiRoot);
								entree = rs.getString(5);
							}
							iti = (mxCell) graph.insertVertex(itiRoot, null, new CellContent(CellContent.TYPE_ITI, id, rs.getString(6)), 0, 0, 80, 50, GraphStyle.groupStyle);
							iti.setConnectable(false);
							itis.add(iti);
							first = (mxCell) graph.insertVertex(iti, null, new CellContent(CellContent.TYPE_BALISE, 0, name), 0, 0, GraphStyle.baliseSize, GraphStyle.baliseSize);
							first.setConnectable(false);
							balises.put(rs.getInt(4), first);
						} else {
							String style = rs.getBoolean(2) ? 
									((nameMatch(balise1, name) || nameMatch(balise2,name)) ? GraphStyle.baliseHighlight : GraphStyle.baliseStyle) : 
										((nameMatch(balise1, name) || nameMatch(balise2,name)) ? GraphStyle.baliseTraversHighlight : GraphStyle.baliseTravers);
									mxCell bal = (mxCell) graph.insertVertex(iti, null, new CellContent(CellContent.TYPE_BALISE, 0, name), 0, 0, GraphStyle.baliseSize, GraphStyle.baliseSize, style);
									bal.setConnectable(false);
									graph.insertEdge(iti, null, "", first, bal, GraphStyle.edgeStyle);
									balises.put(rs.getInt(4), bal);
									first  = bal;
						}
					}
					
					fireNumberResults(count);
					
					progressBar.setValue(2);

					//on termine le dernier iti si il existe
					if(iti != null){
						first.setStyle(GraphStyle.baliseDefault);
						balisesByItis.put(id, balises);
					}

					hasResults = (id != 0);
					
					progressBar.setValue(3);

					rs = st.executeQuery("select iditi, trajetid, raccordement_id, cond1, balise, balid from couple_trajets, baltrajets where couple_trajets.trajetid = baltrajets.idtrajet and iditi in ("+findItis(balise1, balise2)+") ");

					progressBar.setValue(4);
					
					id = 0; //id des trajets
					int idBal = 0;//id de la première balise du trajet
					Object parent = null;
					mxCell second = null;
					//ajout des trajets
					while(rs.next()){
						//String balise = rs.getString(5);
						if(id != rs.getInt(2) || rs.getInt(6) == idBal){
							//nouveau trajet
							first = balisesByItis.get(rs.getInt(1)).get(rs.getInt(6));
							parent = first.getParent();
							id = rs.getInt(2);
							idBal = rs.getInt(6);
							second = null;
						} else {
							if(rs.getInt(3) == rs.getInt(6)){ //on raccorde
								graph.insertEdge(parent, null, "", first, balisesByItis.get(rs.getInt(1)).get(rs.getInt(6)), GraphStyle.edgeTrajet);
							} else {
								if(second == null) {
									second = (mxCell) graph.insertVertex(parent, null, new CellContent(CellContent.TYPE_BALISE, 0, rs.getString(5)), 0, 0, GraphStyle.baliseSize, GraphStyle.baliseSize, GraphStyle.baliseTrajet);
									second.setConnectable(false);
									graph.insertEdge(parent, null, rs.getString(4), first, second, GraphStyle.edgeTrajet);
								} else {
									second = (mxCell) graph.insertVertex(parent, null, new CellContent(CellContent.TYPE_BALISE, 0, rs.getString(5)), 0, 0, GraphStyle.baliseSize, GraphStyle.baliseSize, GraphStyle.baliseTrajet);
									second.setConnectable(false);
									graph.insertEdge(parent, null, "", first, second, GraphStyle.edgeTrajet);
								}
							}
							first = second;
						}
					}

					progressBar.setValue(4);

					for(mxCell o : itis){
						layout.execute(o);
					}
					
					graph.updateGroupBounds(itis.toArray(), graph.getGridSize());

					for(mxCell o : itisRoot){
						stack.execute(o);
						graph.addListener(mxEvent.CELLS_FOLDED, new CellFoldedListener(o, stack));
					}
					
					progressBar.setValue(5);

					
					graph.updateGroupBounds(itisRoot.toArray(), 0);
					
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
