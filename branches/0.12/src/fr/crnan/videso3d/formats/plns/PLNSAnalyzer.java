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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.jfree.data.category.DefaultCategoryDataset;

import fr.crnan.videso3d.ProgressSupport;
import fr.crnan.videso3d.databases.DatabaseManager;
import fr.crnan.videso3d.databases.DatabaseNotFoundException;
import fr.crnan.videso3d.databases.stip.PointNotFoundException;
import fr.crnan.videso3d.ihm.components.VFileChooser;
import gov.nasa.worldwind.util.Logging;

/**
 * Analyse d'une base PLNS.<br />
 * Nécessite une connexion à une base STPV.
 * @author Bruno Spyckerelle
 * @version 0.2.1
 */
public class PLNSAnalyzer extends ProgressSupport{

	private Connection base;
	

	/**
	 * 
	 * @param path Chemin vers la base de données
	 */
	public void setPath(String path){
		try {
			//Chargement du driver
			Class.forName("org.sqlite.JDBC");
			//Connexion
			base = DriverManager.getConnection("jdbc:sqlite:"+path);
			//Verify if it is a SQlite file
			base.createStatement().executeQuery("select * from plns");
			//une base SQL a bien été donnée
			fireTaskEnds();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			//e.printStackTrace();
			if(JOptionPane.showOptionDialog(null, "<html>La base de données ne semble pas être correcte.<br />" +
					"Si vous avez sélectionné un fichier PLNS brut, il faut d'abord extraire les données.<br />" +
					"Souhaitez vous tenter d'extraire les données du fichier sélectionné ?", 
					"Problème lors de l'import des données",
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, "") == JOptionPane.OK_OPTION) {
				
				VFileChooser fileChooser = new VFileChooser();
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fileChooser.setMultiSelectionEnabled(false);
				fileChooser.setDialogTitle("Sélectionner le fichier de base de données");
				fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
				final File database;
				if(fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
					database = fileChooser.getSelectedFile();
					try{
						
						new PLNSReader(new File[]{new File(path)},
								database, 
								null,
								new PropertyChangeListener() {


							@Override
							public void propertyChange(PropertyChangeEvent evt) {
								if(evt.getPropertyName().equals(ProgressSupport.TASK_STARTS)){
									fireTaskStarts((Integer) evt.getNewValue());
								} else if(evt.getPropertyName().equals(ProgressSupport.TASK_PROGRESS)){
									fireTaskProgress((Integer) evt.getNewValue());
								} else if(evt.getPropertyName().equals(ProgressSupport.TASK_INFO)){
									fireTaskInfo((String) evt.getNewValue());
								} else if(evt.getPropertyName().equals(ProgressSupport.TASK_ENDS)){
									try {
										base = DriverManager.getConnection("jdbc:sqlite:"+database.getAbsolutePath());
									} catch (SQLException e) {
										e.printStackTrace();
									}
									fireTaskEnds();
								}

							}
						});
						
					} catch(PointNotFoundException e1){
						Logging.logger().warning("Point non trouvé : "+e1.getName());
						JOptionPane.showMessageDialog(null, "<html><b>Problème :</b><br />Impossible de trouver certains points du fichier ("+e1.getName()+").<br /><br />" +
								"<b>Solution :</b><br />Vérifiez qu'une base de données STIP existe et qu'elle est cohérente avec le fichier de trajectoires.</html>",
								"Erreur", JOptionPane.ERROR_MESSAGE);
					}
				}
			} else {
				base = null;
			}
		}
	}
	
	/**
	 * @return {@link DefaultCategoryDataset} contenant la répartition des catégories de code
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
	
	/**
	 * 
	 * @return {@link DefaultCategoryDataset} contenant la répartition des LP
	 */
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
	
	public Connection getConnection(){
		return base;
	}
}
