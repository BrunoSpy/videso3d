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

import fr.crnan.videso3d.ihm.MainWindow;

import gov.nasa.worldwind.Configuration;

/**
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class Videso3D {
	
	public static void main(String[] args)
	{
		if (Configuration.isMacOS())
		{
			System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Videso 3D");
		}

		java.awt.EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				// Create an AppFrame and immediately make it visible. As per Swing convention, this
				// is done within an invokeLater call so that it executes on an AWT thread.
			
				new MainWindow(new DatabaseManager()).setVisible(true);
			}
		});
	}

}
