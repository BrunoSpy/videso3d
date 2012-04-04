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

import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;

import fr.crnan.videso3d.DatabaseNotFoundException;
import fr.crnan.videso3d.formats.plns.PLNSAnalyzer;
import fr.crnan.videso3d.formats.plns.PLNSChartMouseListener;

/**
 * Fenêtre d'analyse d'une base PLNS
 * @author Bruno Spyckerelle
 * @version 0.1.1
 */
public class PLNSPanel extends ResultPanel {

	private PLNSAnalyzer plnsAnalyzer;
	
	private List<ChartPanel> chartPanels;
	
	/**
	 * 
	 * @param path Chemin vers la base de données
	 */
	public PLNSPanel(String path){
		this.setLayout(new FlowLayout());
		
		chartPanels = new ArrayList<ChartPanel>();
		
		plnsAnalyzer = new PLNSAnalyzer(path);
		CategoryDataset dataset;
		try {
			dataset = plnsAnalyzer.getCategoryCodesRepartition();
			JFreeChart chart = ChartFactory.createBarChart("Répartition de l'utilisation des codes par catégorie", "Catégorie", "Total", dataset, PlotOrientation.VERTICAL, false, true, false);
			
			ChartPanel panel = new ChartPanel(chart);
			chartPanels.add(panel);
			
			this.add(panel);
		} catch (DatabaseNotFoundException e) {
			e.printStackTrace();
		}

		dataset = plnsAnalyzer.getLPCodesRepartition();
		//CategoryAxisLabel is used in PLNSChartMouseListener to determine the type of the entity
		JFreeChart chart = ChartFactory.createBarChart("Répartition de l'utilisation des codes par LP", "LP", "Total", dataset, PlotOrientation.VERTICAL, false, true, false);
		ChartPanel panel = new ChartPanel(chart);
		chartPanels.add(panel);
		this.add(panel);
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

}
