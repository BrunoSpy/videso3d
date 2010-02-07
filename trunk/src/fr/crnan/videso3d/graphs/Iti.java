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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;

import javax.swing.SwingConstants;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.view.mxGraph;

import fr.crnan.videso3d.Couple;
import fr.crnan.videso3d.DatabaseManager;

/**
 * Graph representing an iti
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class Iti {

	public static Object addIti(mxGraph graph, Object parent, Integer id){
		try {
			if(DatabaseManager.getCurrentStip() == null){
				throw new NullPointerException("No Stip Database available");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		graph.getModel().beginUpdate();
		mxCell iti = null;
		try {
			LinkedList<Couple<mxCell, mxCell>> couples = new LinkedList<Couple<mxCell,mxCell>>();
			
			Statement st = DatabaseManager.getCurrentStip();
			ResultSet rs = st.executeQuery("select * from itis where id='"+id+"'");
			iti = (mxCell) graph.insertVertex(parent, null, new CellContent(CellContent.TYPE_ITI, id, rs.getString("entree")), 0, 0, 80, 50, GraphStyle.groupStyle);
			iti.setConnectable(false);
			mxCell entree = (mxCell) graph.insertVertex(iti, null, new CellContent(CellContent.TYPE_BALISE, 0, rs.getString("entree")), 0, 0, GraphStyle.baliseSize, GraphStyle.baliseSize);
			entree.setConnectable(false);
			mxCell sortie = (mxCell) graph.insertVertex(iti, null, new CellContent(CellContent.TYPE_BALISE, 0, rs.getString("sortie")), 0, 0, GraphStyle.baliseSize, GraphStyle.baliseSize);
			sortie.setConnectable(false);
			rs = st.executeQuery("select * from balitis where iditi = '"+id+"'");
			Object first = entree;
			while(rs.next()){
				if(rs.getBoolean("appartient")){
					Object bal = graph.insertVertex(iti, null, new CellContent(CellContent.TYPE_BALISE, 0, rs.getString("balise")), 0, 0, GraphStyle.baliseSize, GraphStyle.baliseSize, GraphStyle.baliseStyle);
					((mxCell)bal).setConnectable(false);
					
					graph.insertEdge(iti, null, "", first, bal, GraphStyle.edgeStyle);
					
					//on sauve temporairement les couples de balises pour la recherche de trajets
					couples.add(new Couple<mxCell, mxCell>((mxCell)first, (mxCell)bal));
					
					first = bal;
				}
			}
			couples.add(new Couple<mxCell, mxCell>((mxCell)first, (mxCell)sortie));
			graph.insertEdge(iti, null, "", first, sortie, GraphStyle.edgeStyle);			
			//ajout des trajets
			for(Couple<mxCell, mxCell> c : couples){
				rs = st.executeQuery("select * from trajets, baltrajets where eclatement = '"+c.getFirst().getValue()+"' and raccordement='"+c.getSecond().getValue()+"' and trajets.id = baltrajets.idtrajet");
				mxCell firstBalise = null;
				mxCell second = null;
				int idIti = 0;
				while(rs.next()){
					if(rs.getInt(1) != idIti){
						//la première balise du trajet est celle du couple
						firstBalise = c.getFirst();
						idIti = rs.getInt(1);
						second = null;
					} else {
						String name = rs.getString("balise");
						if(name.equals(c.getSecond().getValue().toString())){
							//on ferme le trajet
							graph.insertEdge(iti, null, "", firstBalise, c.getSecond(), GraphStyle.edgeTrajet);
						} else {
							if(second == null) {
								second = (mxCell) graph.insertVertex(iti, null, new CellContent(CellContent.TYPE_BALISE, 0, rs.getString("balise")), 0, 0, GraphStyle.baliseSize, GraphStyle.baliseSize, GraphStyle.baliseStyle);
								graph.insertEdge(iti, null, rs.getString("cond1"), firstBalise, second, GraphStyle.edgeTrajet);
							} else {
								second = (mxCell) graph.insertVertex(iti, null, new CellContent(CellContent.TYPE_BALISE, 0, rs.getString("balise")), 0, 0, GraphStyle.baliseSize, GraphStyle.baliseSize, GraphStyle.baliseStyle);
								graph.insertEdge(iti, null, "", firstBalise, second, GraphStyle.edgeTrajet);
							}		
						}
						firstBalise = second;
					}
				}
			}
			rs.close();
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		graph.getModel().endUpdate();
		mxHierarchicalLayout layout = new mxHierarchicalLayout(graph, SwingConstants.WEST);
		layout.setParallelEdgeSpacing(5.0);
		layout.setInterRankCellSpacing(20.0);//edge size
		layout.setInterHierarchySpacing(10.0);
		layout.execute(iti);
		
		return iti;
	}
	
}
