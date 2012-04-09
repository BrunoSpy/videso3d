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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JToolBar;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;

import fr.crnan.videso3d.DatabaseNotFoundException;
import fr.crnan.videso3d.formats.plns.PLNSAnalyzer;
import fr.crnan.videso3d.formats.plns.PLNSChartMouseListener;
import fr.crnan.videso3d.ihm.components.TilingDesktopPane;

/**
 * Fenêtre d'analyse d'une base PLNS
 * @author Bruno Spyckerelle
 * @version 0.1.1
 */
public class PLNSPanel extends ResultPanel {

	private PLNSAnalyzer plnsAnalyzer;
	
	private List<ChartPanel> chartPanels;
	
	private TilingDesktopPane desktop;
	
	/**
	 * 
	 * @param path Chemin vers la base de données
	 */
	public PLNSPanel(String path){
		this.setLayout(new BorderLayout());
		
		this.add(createToolbar(), BorderLayout.NORTH);
		
		desktop = new TilingDesktopPane();
		desktop.setPreferredSize(new Dimension(800, 600));
		this.add(desktop);
		
		chartPanels = new ArrayList<ChartPanel>();
		
		plnsAnalyzer = new PLNSAnalyzer(path);
		CategoryDataset dataset;
		try {
			dataset = plnsAnalyzer.getCategoryCodesRepartition();
			JFreeChart chart = ChartFactory.createBarChart("Répartition de l'utilisation des codes par catégorie", "Catégorie", "Total", dataset, PlotOrientation.VERTICAL, false, true, false);
			
			ChartPanel panel = new ChartPanel(chart);
			chartPanels.add(panel);
			JInternalFrame frame = new JInternalFrame("Répartition de l'utilisation des codes par catégorie", true, false, true, true);
			frame.add(panel);
			frame.pack();
			frame.setVisible(true);
			desktop.add(frame);
		} catch (DatabaseNotFoundException e) {
			e.printStackTrace();
		}

		dataset = plnsAnalyzer.getLPCodesRepartition();
		//CategoryAxisLabel is used in PLNSChartMouseListener to determine the type of the entity
		JFreeChart chart = ChartFactory.createBarChart("Répartition de l'utilisation des codes par LP", "LP", "Total", dataset, PlotOrientation.VERTICAL, false, true, false);
		ChartPanel panel = new ChartPanel(chart);
		chartPanels.add(panel);
		JInternalFrame frame = new JInternalFrame("Répartition de l'utilisation des codes par LP", true, false, true, true);
		frame.add(panel);
		frame.pack();
		frame.setVisible(true);
		desktop.add(frame);
		
		//tile frames when the ancestor is made visible
		this.addAncestorListener(new AncestorListener() {
			
			@Override
			public void ancestorRemoved(AncestorEvent event) {	}
			
			@Override
			public void ancestorMoved(AncestorEvent event) {}
			
			@Override
			public void ancestorAdded(AncestorEvent event) {
				desktop.tile(true);
			}
		});
		
	}
	
	@Override
	public void setContext(ContextPanel context) {
		PLNSChartMouseListener listener = new PLNSChartMouseListener(context);
		for(ChartPanel p : chartPanels){
			p.addChartMouseListener(listener);
		}
	}

	@Override
	public String getTitleTab() {
		return "PLNS";
	}

	private JToolBar createToolbar(){
		JToolBar toolbar = new JToolBar();
		
		JButton newGraph = new JButton("Nouveau");
		newGraph.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				//TODO créer fenêtre de création de graph
			}
		});
		
		toolbar.add(newGraph);
		
		JButton retile = new JButton("Réarranger");
		retile.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				desktop.tile(true);
			}
		});
		toolbar.add(retile);
		
		return toolbar;
	}
}
