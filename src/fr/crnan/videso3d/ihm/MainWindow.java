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

package fr.crnan.videso3d.ihm;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.UIManager;

import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.DatabaseManager.Type;
import fr.crnan.videso3d.graphics.Balise2D;
import fr.crnan.videso3d.graphics.Route3D;
import fr.crnan.videso3d.graphics.Secteur3D;
import fr.crnan.videso3d.layers.TextLayer;
import fr.crnan.videso3d.stip.Secteur;
import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.layers.AirspaceLayer;
import gov.nasa.worldwind.layers.SurfaceShapeLayer;
/**
 * Fenêtre principale
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class MainWindow extends JFrame {

	private DatabaseManager db;
	
	public MainWindow(DatabaseManager db){
		
		this.db = db;
		
		this.setNimbus();
		this.build();
		
	}
	
	private void build(){
		WorldWindowGLCanvas wwd = new WorldWindowGLCanvas();
		wwd.setPreferredSize(new java.awt.Dimension(1000, 800));
		this.getContentPane().add(wwd, java.awt.BorderLayout.CENTER);
		this.pack();

		wwd.setModel(new BasicModel());

		AirspaceLayer routeLayer = new AirspaceLayer();
		routeLayer.setName("Routes");
		wwd.getModel().getLayers().add(routeLayer);

		//	            db = new DatabaseManager();
		//	            
		//	        	Pays pays = new Pays("/home/datas/Projets/Videso3D/datas", db);
		//	        	 pays.addPropertyChangeListener(new PropertyChangeListener() {
		//						
		//						@Override
		//						public void propertyChange(PropertyChangeEvent evt) {
		//							if("progress".equals(evt.getPropertyName())){
		//								System.out.println("Pays "+evt.getNewValue());
		//							} else if("file".equals(evt.getPropertyName())){
		//								System.out.println("Pays "+evt.getNewValue());
		//							}	
		//						}
		//					});
		//	            Stip stip = new Stip("/home/datas/Projets/Videso3D/datas/091202_v7", db);
		//	            stip.addPropertyChangeListener(new PropertyChangeListener() {
		//					
		//					@Override
		//					public void propertyChange(PropertyChangeEvent evt) {
		//						if("progress".equals(evt.getPropertyName())){
		//							System.out.println("Stip "+evt.getNewValue());
		//						} else if("file".equals(evt.getPropertyName())){
		//							System.out.println("Stip "+evt.getNewValue());
		//						}	
		//					}
		//				});
		//       	
		//	            pays.execute();
		//	        	stip.execute();

		/*-------- TEST -------*/
		try {

			Route3D R10 = new Route3D(Route3D.Type.FIR);
			Statement st = db.getCurrentStip();
			ResultSet rs = st.executeQuery("select * from routebalise, balises where route ='R10' and routebalise.balise = balises.name and appartient = 1");
			LatLon[] loc = new LatLon[20];
			int i = 0;
			while(rs.next()){
				loc[i] = LatLon.fromDegrees(rs.getDouble("latitude"), rs.getDouble("longitude"));
				i++;
			}
			R10.setLocations(Arrays.asList(loc));
			routeLayer.addAirspace(R10);	

			Secteur3D AP3D = new Secteur3D("AP", 265, 315);
			Secteur AP = new Secteur("AP", 318, db.getCurrentStip());
			AP.setConnectionPays(db.getCurrent(Type.PAYS));
			AP3D.setLocations(AP.getContour(315));
			routeLayer.addAirspace(AP3D);

			//GeographicTextRenderer textRenderer = new GeographicTextRenderer();
			SurfaceShapeLayer surfaceLayer = new SurfaceShapeLayer();
			TextLayer textLayer = new TextLayer();
			wwd.getModel().getLayers().add(surfaceLayer);
			wwd.getModel().getLayers().add(textLayer);

			st = db.getCurrentStip();
			rs = st.executeQuery("select * from balises");
			while(rs.next()){
				Balise2D balise = new Balise2D(rs.getString("name"), LatLon.fromDegrees(rs.getDouble("latitude"), rs.getDouble("longitude")));
				balise.addToLayer(surfaceLayer, textLayer);
			}

			rs.close();
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Utilise le L&F Nimbus au lieu du L&F Metal
	 */
	private void setNimbus(){				
		for (UIManager.LookAndFeelInfo laf : UIManager.getInstalledLookAndFeels() ){
			if ("Nimbus".equals(laf.getName())) {
				try {
					UIManager.setLookAndFeel(laf.getClassName());
					System.out.println("Nimbus found. Applying style.");
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}
	}
}
