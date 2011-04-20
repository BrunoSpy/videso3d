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

package fr.crnan.videso3d;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.logging.Level;

import javax.swing.UIManager;

import fr.crnan.videso3d.ihm.AnalyzeUI;
import fr.crnan.videso3d.ihm.MainWindow;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.util.Logging;

/**
 * @author Bruno Spyckerelle
 * @version 0.3.0
 */
public class Videso3D {

	public static final String VERSION = "0.9.1";
	
	public static void main(final String[] args)
	{
		if (Configuration.isMacOS())
		{
			System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Videso 3D");
		}

		// prevents flashing during window resizing
		System.setProperty("sun.awt.noerasebackground", "true"); 

		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				//Installs Nimbus Look&Feel (requires Java 6 update 10)
				for (UIManager.LookAndFeelInfo laf : UIManager.getInstalledLookAndFeels() ){
					if ("Nimbus".equals(laf.getName())) {
						try {
							UIManager.setLookAndFeel(laf.getClassName());
						} catch (Exception e) {
							e.printStackTrace();
						}

					}
				}

				Logging.logger().setLevel(Level.ALL);

				if(args.length > 0) {
					final String arg0 = args[0];
					if(arg0.equals("analyze")){
						AnalyzeUI.showAnalyzeUI();
					} else {
						if(new File(arg0).exists()){
							//try to open the file
							final MainWindow main = new MainWindow();
							main.addPropertyChangeListener("done", new PropertyChangeListener() {
								
								@Override
								public void propertyChange(PropertyChangeEvent e) {
									File[] files = {new File(arg0)};
									main.getDataExplorer().addTrajectoriesViews(files);
								}
							});
							
						}
					}
				} else {
					new MainWindow();
				}
			}
		});
	}

}
