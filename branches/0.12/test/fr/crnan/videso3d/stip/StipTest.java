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
package fr.crnan.videso3d.stip;

import static org.junit.Assert.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import fr.crnan.videso3d.databases.DatabaseManager;
import fr.crnan.videso3d.DatasManager.Type;
import fr.crnan.videso3d.databases.stip.Stip;
import gov.nasa.worldwind.util.Logging;
/**
 * Unit tests for Stip database import
 * @author Bruno Spyckerelle
 * @version 0.1.0
 */
public class StipTest {

	private static Stip stip;
	
	@BeforeClass
	public static void setUp(){
		stip = new Stip("testData/SATIN_1006v2");
		stip.addPropertyChangeListener(new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if(evt.getPropertyName().equals("file")){
					Logging.logger().info("Import fichier "+evt.getNewValue());
				} else if(evt.getPropertyName().equals("done")){
					if((Boolean)evt.getNewValue()){
						Logging.logger().info("Import correctement terminé");
					} else {
						Logging.logger().severe("Erreur lors de l'import");
					}
				}
			}
		});
		stip.doInBackground();
		stip.done();
		try {
			DatabaseManager.selectDatabase(stip.getName(), Type.STIP);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@AfterClass
	public static void removeAll() throws SQLException{
		DatabaseManager.deleteDatabase(stip.getName(), Type.STIP);
	}
	
	@Test
	public void testGetName(){
		assertEquals("Nom de la base ", "STIP_10-02-11.26-01-11", stip.getName());
	}
	
	@Test
	public void testItiSortieLFPG() throws SQLException{
		Statement st = DatabaseManager.getCurrentStip();
		ResultSet rs = st.executeQuery("select count(*) from itis where sortie LIKE 'LFPG%'");
		int count = 0;
		if (rs.next()) {
			count = rs.getInt(1);
		}
		assertEquals("Nombre d'itis avec sortie LFPG", 282, count);
	}
	

}
