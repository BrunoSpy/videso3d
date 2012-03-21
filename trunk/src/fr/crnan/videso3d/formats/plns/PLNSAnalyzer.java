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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.jfree.data.category.DefaultCategoryDataset;

import fr.crnan.videso3d.DatabaseManager;

/**
 * Analyse d'une base PLNS.<br />
 * Nécessite une connexion à une base STPV.
 * @author Bruno Spyckerelle
 * @version 0.1.0
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
	 * @deprecated
	 * @return
	 */
	public DefaultCategoryDataset getCategoryCodesRepartition(){
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		HashMap<String, Integer> categories = new HashMap<String, Integer>();
		try {
			//initialisation des catégories
			Statement st = DatabaseManager.getCurrentStpv();
			ResultSet rs = st.executeQuery("select distinct name from cat_code where 1");
			while(rs.next()){
				categories.put(rs.getString(1), 0);
			}
			Statement st1 = base.createStatement();
			ResultSet rs1 = st1.executeQuery("select code from plns where 1");
			while (rs1.next()){
				rs = st.executeQuery("select name from cat_code where code='"+rs1.getInt(1)+"'");
				if(rs.next()){
					String cat = rs.getString(1);
					int number = categories.get(cat)+1;
					categories.put(cat, number);
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
	
	/**
	 * Retourne la liaison privilégiée utilisée à partir des infos du PLN
	 * @param idpln ID du pln
	 * @return
	 */
	public int getLiaisonPrivilegiee(int idpln){
		int lp = 0;
		
		try {
			Statement st = DatabaseManager.getCurrentStpv();
			ResultSet rs = st.executeQuery("select * from lps where 1");
			Statement st2 = this.base.createStatement();
			ResultSet pln = st2.executeQuery("select * from plns where idpln='"+idpln+"'");
			String adep;
			String adest;
			List<String> sls;
			if(pln.next()){
				sls = new ArrayList<String>();
				adep = pln.getString(7);
				adest = pln.getString(8);
			} else {
				return 0;
			}
			pln = st2.executeQuery("select sl from sls where idpln='"+idpln+"'");
			while(pln.next()){
				sls.add(pln.getString(1));
			}
			st2.close();
			boolean found = false;
			while(rs.next() && !found){
				
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		
		return lp;
	}
	
}
