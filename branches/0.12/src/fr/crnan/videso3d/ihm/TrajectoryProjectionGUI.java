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

import fr.crnan.videso3d.formats.VidesoTrack;
import fr.crnan.videso3d.trajectography.TracksStatsProducer;
import gov.nasa.worldwind.globes.Globe;

import java.util.List;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeriesCollection;
/**
 * IHM de visualisation de projection de trajectoires
 * @author Bruno Spyckerelle
 * @version 0.1.0
 */
public class TrajectoryProjectionGUI extends JFrame{

	public TrajectoryProjectionGUI(List<VidesoTrack> tracks, Globe globe){
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu mnParamtres = new JMenu("Paramètres");
		menuBar.add(mnParamtres);
		
		JMenuItem mntmNewMenuItem = new JMenuItem("New menu item");
		mnParamtres.add(mntmNewMenuItem);
		
		double ref = TracksStatsProducer.computeReferenceAltitude(tracks);
		XYSeriesCollection dataset = new XYSeriesCollection();
		
		for(VidesoTrack t : tracks){
			dataset.addSeries(TracksStatsProducer.computeDevelopedPath(t, ref, false, globe));
		}
		
		JFreeChart chart = ChartFactory.createXYLineChart("Projection",
				"Distance",
				"Altitude (m)",
				dataset,
				PlotOrientation.VERTICAL,
				true,
				true,
				false);
		ChartPanel panel = new ChartPanel(chart);
		setContentPane(panel);
		pack();
	}
	
	
}
