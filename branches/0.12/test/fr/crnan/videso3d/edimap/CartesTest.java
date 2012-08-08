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
package fr.crnan.videso3d.edimap;

import static org.junit.Assert.*;
import fr.crnan.videso3d.databases.DatabaseManager;
import fr.crnan.videso3d.databases.DatabaseManager.Type;
import fr.crnan.videso3d.databases.edimap.Cartes;
import gov.nasa.worldwind.util.Logging;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.SQLException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 
 * @author Bruno Spyckerelle
 * @version 0.1.0
 */
public class CartesTest {

	private static Cartes cartes;
	
	@BeforeClass
	public static void setUp(){
		cartes = new Cartes("testData/EDIMAP_0904", "carac_jeu.NCT");
		cartes.addPropertyChangeListener(new PropertyChangeListener() {
			
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
		cartes.doInBackground();
		cartes.done();
	}
	
	@AfterClass
	public static void removeAll() throws SQLException{
		DatabaseManager.deleteDatabase(cartes.getName(), Type.Edimap);
	}
	
	@Test
	public void testDynamiques(){
		int count = cartes.getCartesDynamiques().size();
		assertEquals("Nombre de cartes dynamiques", 150, count);
	}
	
	@Test
	public void testStatiques(){
		int count = cartes.getCartesStatiques().size();
		assertEquals("Nombre de cartes statiques", 38, count);
	}
	
	@Test
	public void testSecteurs(){
		int count = cartes.getSecteurs().size();
		assertEquals("Nombre de cartes secteurs", 34, count);
	}
	
	@Test
	public void testVolumes(){
		int count = cartes.getVolumes().size();
		assertEquals("Nombre de cartes volumes", 34, count);
		assertEquals("Autant de cartes secteurs que de cartes volumes", cartes.getSecteurs().size(), count);
	}
}
