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
package fr.crnan.videso3d.formats.plns;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.jfree.data.category.DefaultCategoryDataset;

import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.DatabaseNotFoundException;

/**
 * Analyse d'une base PLNS.<br />
 * Nécessite une connexion à une base STPV.
 * @author Bruno Spyckerelle
 * @version 0.1.1
 */
public class PLNSAnalyzer {

	private Connection base;
	
	/**
	 * 
	 * @param path Chemin vers la base de données PLNS
	 * @throws SQLException 
	 */
	public PLNSAnalyzer(String path){

		try {
			//Chargement du driver
			Class.forName("org.sqlite.JDBC");
			//Connexion
			base = DriverManager.getConnection("jdbc:sqlite:"+path);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	/**
	 * @return
	 * @throws DatabaseNotFoundException 
	 */
	public DefaultCategoryDataset getCategoryCodesRepartition() throws DatabaseNotFoundException {
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		SortedMap<String, Integer> categories = new TreeMap<String, Integer>();
		try {
			//initialisation des catégories
			Statement st = DatabaseManager.getCurrentStpv();
			if(st == null){
				throw new DatabaseNotFoundException(DatabaseManager.Type.STPV);
			}
			ResultSet rs = st.executeQuery("select distinct name from cat_code where 1");
			while(rs.next()){
				categories.put(rs.getString(1), 0);
			}
			Statement st1 = base.createStatement();
			ResultSet rs1 = st1.executeQuery("select lp from plns where 1");
			while (rs1.next()){
				rs = st.executeQuery("select cat_code from lps where id='"+rs1.getInt(1)+"'");
				if(rs.next()){
					String cat = rs.getString(1);
					if(cat != null){
						int number = categories.get(cat)+1;
						categories.put(cat, number);
					}
				}
			}
			st.close();
			st1.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		for(Entry<String, Integer> e : categories.entrySet()){
			dataset.setValue(e.getValue(), "Total", e.getKey());
		}
		return dataset;
	}
	
	public DefaultCategoryDataset getLPCodesRepartition(){
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		
		try {
			Statement st = base.createStatement();
			ResultSet rs = st.executeQuery("select max(lp) from plns");
			int max = rs.getInt(1);
			int[] lps = new int[max];
			rs = st.executeQuery("select lp from plns where 1");
			while (rs.next()){
				int lp = rs.getInt(1);
				if(lp > 0){
					lps[lp-1] = lps[lp-1] +1;
				}
			}
			st.close();
			
			for(int i=0;i<lps.length;i++){
				dataset.setValue(lps[i], "Total", new Integer(i+1));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return dataset;
	}
	
}
