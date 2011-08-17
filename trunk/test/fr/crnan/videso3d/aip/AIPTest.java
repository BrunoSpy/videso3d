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
package fr.crnan.videso3d.aip;

import static org.junit.Assert.assertEquals;
import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.DatabaseManager.Type;
import gov.nasa.worldwind.util.Logging;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
/**
 * Unit tests for AIP database import
 * @author Adrien Vidal
 * @version 0.1.0
 */
public class AIPTest {
	
	private static AIP aip;
	
	@BeforeClass
	public static void setUp(){
		aip = new AIP("testData/2010-07n.xml");
		aip.addPropertyChangeListener(new PropertyChangeListener() {
			
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
		aip.doInBackground();
		aip.done();
		try {
			DatabaseManager.selectDatabase(aip.getName(), Type.AIP);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@AfterClass
	public static void removeAll() throws SQLException{
		DatabaseManager.deleteDatabase(aip.getName(), Type.AIP);
	}
	
	@Test
	public void testGetName(){
		assertEquals("Nom de la base ", "AIP_2010-07-01", aip.getName());
	}
	
	@Test
	public void testAerodromesLFS() throws SQLException{
		Statement st = DatabaseManager.getCurrentAIP();
		ResultSet rs = st.executeQuery("select count(*) from aerodromes where code LIKE 'LFS%'");
		int count = 0;
		if (rs.next()) {
			count = rs.getInt(1);
		}
		assertEquals("Nombre d'aérodromes dont le code débute par LFS : ", 24, count);
	}
	
	@Test
	public void testNbRoutes() throws SQLException{
		int nbpdr = 0;
		int nbawy = 0;
		Statement st = DatabaseManager.getCurrentAIP();
		ResultSet rs = st.executeQuery("select count(*) from routes where type = 'PDR'");
		if (rs.next()) {
			nbpdr = rs.getInt(1);
		}
		rs = st.executeQuery("select count(*) from routes where type = 'AWY'");
		if (rs.next()) {
			nbawy = rs.getInt(1);
		}
		assertEquals("Nombre d'airways : ", 268, nbawy);
		assertEquals("Nombre de PDR : ", 350, nbpdr);
	}
	
	@Test
	public void testRunway() throws SQLException{
		String nomRunway = "";
		Statement st = DatabaseManager.getCurrentAIP();
		ResultSet rs = st.executeQuery("select runways.nom from runways, aerodromes where runways.pk_ad = aerodromes.pk and aerodromes.code='LFBO' " +
				"and runways.longueur = '3000'");
		if (rs.next()) {
			nomRunway = rs.getString(1);
		}
		assertEquals("Nom de la piste de 3000 mètres à LFBO : ", "14L/32R", nomRunway);		
	}
	
	@Test
	public void testNavFix() throws SQLException{
		int nbTACAN = 0;
		Statement st = DatabaseManager.getCurrentAIP();
		ResultSet rs = st.executeQuery("select count (*) from NavFix where type = 'TACAN'");
		if (rs.next()) {
			nbTACAN = rs.getInt(1);
		}
		assertEquals("Nombre de TACAN : ", 27, nbTACAN);		
	}
	
	@Test
	public void testVolumes() throws SQLException{
		int nbVolumesSeine = 0;
		Statement st = DatabaseManager.getCurrentAIP();
		ResultSet rs = st.executeQuery("select count (*) from volumes where nom LIKE 'SEINE%'");
		if (rs.next()) {
			nbVolumesSeine = rs.getInt(1);
		}
		assertEquals("Nombre de zones concernant Seine : ", 15, nbVolumesSeine);		
	}
}
