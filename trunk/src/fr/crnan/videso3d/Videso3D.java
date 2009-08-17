package fr.crnan.videso3d;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;


import fr.crnan.videso3d.graphics.Route3D;
import fr.crnan.videso3d.pays.Pays;
import fr.crnan.videso3d.stip.Stip;
import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.layers.AirspaceLayer;



public class Videso3D {
	
	 private static class AppFrame extends javax.swing.JFrame
	    {
		 private DatabaseManager db;
	        public AppFrame()
	        {
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
//	        	Pays pays = new Pays("V:\\SPYCKERELLE\\Dev\\Projets\\ViDESO\\Datas", db);
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
//	            Stip stip = new Stip("V:\\SPYCKERELLE\\Dev\\Projets\\ViDESO\\Datas\\091202_v7", db);
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
					db = new DatabaseManager();
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
					rs.close();
					st.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
	           
	            
	        }
	    }

	    public static void main(String[] args)
	    {
	        if (Configuration.isMacOS())
	        {
	            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Hello World Wind");
	        }

	        java.awt.EventQueue.invokeLater(new Runnable()
	        {
	            public void run()
	            {
	                // Create an AppFrame and immediately make it visible. As per Swing convention, this
	                // is done within an invokeLater call so that it executes on an AWT thread.
	                new AppFrame().setVisible(true);
	            }
	        });
	    }
	
}
