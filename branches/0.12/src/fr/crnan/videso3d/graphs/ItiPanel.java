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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;

import com.mxgraph.model.mxCell;
import com.mxgraph.util.mxEvent;

import fr.crnan.videso3d.databases.DatabaseManager;
import fr.crnan.videso3d.databases.DatabaseManager.Type;
import fr.crnan.videso3d.databases.stip.StipController;


/**
 * Itis recherchés sour forme de graphe
 * @author Bruno Spyckerelle
 * @version 0.2.0
 */
public class ItiPanel extends ResultGraphPanel {

	private String titleTab = "Iti";
	
	/**
	 * If <code>advanced</code> is <code>true</code>, <code>criteria</code> contains :<br />
	 * <ul><li>entree</li>
	 * <li>sortie</li>
	 * <li>flinf</li>
	 * <li>flsup</li>
	 * <li>first balise</li>
	 * <li>last balise</li>
	 * <li>list of balise</li>
	 * </ul>
	 * If <code>advanced</code> is <code>false</code>, <code>criteria</code> contains two balises
	 * @param advanced
	 * @param criteria 
	 */
	public ItiPanel(boolean advanced, String... criteria) {
		super(advanced, criteria);
		if(advanced){
			int nbBalises = Integer.parseInt(criteria[8]);
			for(int i = 0;i<9+nbBalises;i++){
				if(!criteria[i].isEmpty() && i != 4 && i != 5 && i!=8){
					titleTab += " + "+criteria[i];
				}
			}
			for(int i=9+nbBalises;i<criteria.length;i++){
				if(!criteria[i].isEmpty() ){
					titleTab += " - "+criteria[i];
				}
			}
		} else {
			titleTab += " "+criteria[0]+ (criteria[1].isEmpty()? "" : " + "+criteria[1]);
		}
	}
	
	private String findItis(boolean advanced, String... criteria){
		if (advanced){
			String entree = criteria[0].trim();
			String sortie = criteria[1].trim();
			String first = criteria[6].trim();
			String last = criteria[7].trim();
			Integer flInf = criteria[2].trim().isEmpty() ? 0 : new Integer(criteria[2]);
			Integer flSup = criteria[3].trim().isEmpty() ? 800 : new Integer(criteria[3]);
			int nbBalises = Integer.parseInt(criteria[8]);
			
			ArrayList<String> sql = new ArrayList<String>();
			if(!entree.isEmpty())
				sql.add("select id as iditi from itis where entree "+forgeSql(entree));
			
			if(!sortie.isEmpty())
				sql.add("select id as iditi from itis where sortie "+forgeSql(sortie));
			
			if(!first.isEmpty())
				sql.add("select iditi  from balitis t1 where iditi in (SELECT iditi FROM balitis where balise "+forgeSql(first)+") " +
														"and id=(select min(t2.id) from balitis t2 where t2.iditi = t1.iditi ) " +
														"and balise "+forgeSql(first));
			if(!last.isEmpty())
				sql.add("select iditi  from balitis t1 where iditi in (SELECT iditi FROM balitis where balise "+forgeSql(last)+") " +
														"and id=(select max(t2.id) from balitis t2 where t2.iditi = t1.iditi ) " +
														"and balise "+forgeSql(last));
			
			if(flInf>0)
				sql.add("select id as iditi from itis where flinf "+criteria[4]+"'"+flInf+"'");
			
			if(flSup<800)
				sql.add("select id as iditi from itis where flsup "+criteria[5]+"'"+flSup+"'");
			
			for(int i=9;i<9+nbBalises;i++){
				if(!criteria[i].trim().isEmpty())
					sql.add("select iditi from balitis where balise "+forgeSql(criteria[i].trim()));
			}
			String sqlQuery = "";
			for(int i=0;i<sql.size();i++){
				if(i==0){
					sqlQuery = sql.get(i);
				} else {
					sqlQuery += " INTERSECT "+sql.get(i);
				}
			}
			for(int i=9+nbBalises;i<criteria.length;i++){
				if(!criteria[i].trim().isEmpty())
					sqlQuery+=" EXCEPT select iditi from balitis where balise "+forgeSql(criteria[i].trim());
			}
		
			//TODO pourquoi cette méthode est appelée 2 fois ?
			return sqlQuery;
		} else {
			String balise1 = criteria[0];
			String balise2 = criteria[1];
			if(balise2.isEmpty() && !balise1.isEmpty()){
				return "select iditi from balitis where balise "+forgeSql(balise1)+ 
						" UNION select id as iditi from itis where entree "+forgeSql(balise1);
			} else if(balise1.isEmpty() && !balise2.isEmpty()){
				return "select iditi from balitis where balise "+forgeSql(balise2)+ 
						" UNION select id as iditi from itis where sortie "+forgeSql(balise2);
			} else if(balise1.isEmpty() && balise2.isEmpty()){
				//cas impossible normalement
				return "";
			} else {
				return "select iditi from (select iditi from balitis where balise "+forgeSql(balise1)+ 
						" UNION select id as iditi from itis where entree "+forgeSql(balise1)+") as ab"+ 
						" INTERSECT "+ 
						"select iditi from (select iditi from balitis where balise "+forgeSql(balise2)+ 
						" UNION select id as iditi from itis where sortie "+forgeSql(balise2)+") as cd";
			}
		}
	}

	private boolean isSearchedBalise(boolean advanced, String name, String... criteria){
		if(advanced){
			boolean match = false;
			for(int i=8;i<criteria.length;i++){
				match = match || nameMatch(criteria[i].trim(), name);
			}
			return match || 
				   nameMatch(criteria[0].trim(), name) || 
				   nameMatch(criteria[1].trim(),name) || 
				   nameMatch(criteria[6].trim(),name) || 
				   nameMatch(criteria[7].trim(),name);
		} else {
			return nameMatch(criteria[0].trim(), name) || nameMatch(criteria[1].trim(),name);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see fr.crnan.videso3d.graphs.ResultGraphPanel#createGraphComponent(java.lang.String)
	 */
	@Override
	protected void createGraphComponent(final boolean advanced, final String... criteria){

		
		
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
					String findItisSQL = findItis(advanced, criteria);				
					ResultSet rs = st.executeQuery("select balise, appartient, iditi, balid, entree, sortie from balitis, itis where itis.id = balitis.iditi and iditi in ("+findItisSQL+")");

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
						int balid = rs.getInt(4);
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
							iti = (mxCell) graph.insertVertex(itiRoot, null, new CellContent(Type.STIP, StipController.ITI, id, rs.getString(6)), 0, 0, 80, 50, GraphStyle.groupStyle);
							iti.setConnectable(false);
							itis.add(iti);
							first = (mxCell) graph.insertVertex(iti, null, new CellContent(Type.STIP, StipController.BALISES, balid, name), 0, 0, GraphStyle.baliseSize, GraphStyle.baliseSize, GraphStyle.baliseDefault);
							first.setConnectable(false);
							balises.put(balid, first);
						} else {
							String style = rs.getBoolean(2) ? 
									(isSearchedBalise(advanced, name, criteria) ? GraphStyle.baliseHighlight : GraphStyle.baliseStyle) : 
										(isSearchedBalise(advanced, name, criteria) ? GraphStyle.baliseTraversHighlight : GraphStyle.baliseTravers);
							mxCell bal = (mxCell) graph.insertVertex(iti, null, new CellContent(Type.STIP, StipController.BALISES, balid, name), 0, 0, GraphStyle.baliseSize, GraphStyle.baliseSize, style);
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

					rs = st.executeQuery("select iditi, trajetid, raccordement_id, cond1, balise, balid from couple_trajets, baltrajets where couple_trajets.trajetid = baltrajets.idtrajet and iditi in ("+findItisSQL+") ");

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
									second = (mxCell) graph.insertVertex(parent, null, new CellContent(Type.STIP, StipController.BALISES, rs.getInt(6), rs.getString(5)), 0, 0, GraphStyle.baliseSize, GraphStyle.baliseSize, GraphStyle.baliseTrajet);
									second.setConnectable(false);
									graph.insertEdge(parent, null, rs.getString(4), first, second, GraphStyle.edgeTrajet);
								} else {
									second = (mxCell) graph.insertVertex(parent, null, new CellContent(Type.STIP, StipController.BALISES, rs.getInt(6), rs.getString(5)), 0, 0, GraphStyle.baliseSize, GraphStyle.baliseSize, GraphStyle.baliseTrajet);
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

	@Override
	public String getTitleTab() {
		return this.titleTab;
	}


}
