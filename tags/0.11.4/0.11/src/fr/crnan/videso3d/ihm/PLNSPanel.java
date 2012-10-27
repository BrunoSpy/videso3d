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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.SwingWorker;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.jdbc.JDBCCategoryDataset;
import org.jfree.data.jdbc.JDBCPieDataset;
import org.jfree.data.jdbc.JDBCXYDataset;

import fr.crnan.videso3d.ProgressSupport;
import fr.crnan.videso3d.DatabaseNotFoundException;
import fr.crnan.videso3d.formats.plns.PLNSAnalyzer;
import fr.crnan.videso3d.formats.plns.PLNSChartMouseListener;
import fr.crnan.videso3d.ihm.components.TilingDesktopPane;

/**
 * Fenêtre d'analyse d'une base PLNS
 * 
 * @author Bruno Spyckerelle
 * @version 0.1.2
 */
public class PLNSPanel extends ResultPanel {

	private PLNSAnalyzer plnsAnalyzer;

	private List<ChartPanel> chartPanels;

	private TilingDesktopPane desktop;

	private PLNSChartMouseListener chartMouseListener;
	
	/**
	 * 
	 * @param path
	 *            Chemin vers la base de données
	 */
	public PLNSPanel(final String path) {
		this.setLayout(new BorderLayout());

		this.add(createToolbar(), BorderLayout.NORTH);

		desktop = new TilingDesktopPane();
		desktop.setPreferredSize(new Dimension(800, 600));
		this.add(desktop);

		chartPanels = new ArrayList<ChartPanel>();

		final ProgressMonitor progressMonitorT = new ProgressMonitor(this, "Extraction des données", "", 0, 100, false, true, false);
		progressMonitorT.setMillisToDecideToPopup(0);
		progressMonitorT.setMillisToPopup(0);
		progressMonitorT.setNote("Extraction des fichiers compressés...");
		
		plnsAnalyzer = new PLNSAnalyzer();
		
		//au cas où il faille importer les données, on écoute le ProgressSupport et on ne lance la création de la fenêtre qu'à la fin
		plnsAnalyzer.addPropertyChangeListener(new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if(evt.getPropertyName().equals(ProgressSupport.TASK_STARTS)){

				} else if(evt.getPropertyName().equals(ProgressSupport.TASK_PROGRESS)){
					progressMonitorT.setProgress((Integer) evt.getNewValue());
				} else if(evt.getPropertyName().equals(ProgressSupport.TASK_INFO)){
					progressMonitorT.setNote((String) evt.getNewValue());
				} else if(evt.getPropertyName().equals(ProgressSupport.TASK_ENDS)){
					createIHM();
				}
			}
		});

		new SwingWorker<Void, Void>() {

			@Override
			protected Void doInBackground() throws Exception {
				plnsAnalyzer.setPath(path);
				return null;
			}
		}.execute();
	}

	private void createIHM(){
		CategoryDataset dataset;
		try {
			dataset = plnsAnalyzer.getCategoryCodesRepartition();
			addChart(ChartFactory.createBarChart(
					"Répartition de l'utilisation des codes par catégorie",
					"Catégorie", "Total", dataset, PlotOrientation.VERTICAL,
					false, true, false));

		} catch (DatabaseNotFoundException e) {
			e.printStackTrace();
		}

		dataset = plnsAnalyzer.getLPCodesRepartition();
		// CategoryAxisLabel is used in PLNSChartMouseListener to determine the
		// type of the entity
		addChart(ChartFactory.createBarChart(
				"Répartition de l'utilisation des codes par LP", "LP", "Total",
				dataset, PlotOrientation.VERTICAL, false, true, false));

	}
	
	@Override
	public void setContext(ContextPanel context) {
		chartMouseListener = new PLNSChartMouseListener(context);
		for (ChartPanel p : chartPanels) {
			p.addChartMouseListener(chartMouseListener);
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
				PLNSChartCreateUI chartCreator = new PLNSChartCreateUI(getThis());
				JFreeChart chart = null;
				try {
					if(chartCreator.showDialog(getThis())){
						switch (chartCreator.getChartType()) {
						case 0://XY
							JDBCXYDataset dataset = new JDBCXYDataset(plnsAnalyzer.getConnection());
							dataset.executeQuery(chartCreator.getRequest());
							chart = ChartFactory.createXYAreaChart(chartCreator.getChartTitle(),
														chartCreator.getAbscissesTitle(), chartCreator.getOrdonneesTitle(), dataset, PlotOrientation.VERTICAL, false, true, false);
							break;
						case 1://Pie
							JDBCPieDataset dataset1 = new JDBCPieDataset(plnsAnalyzer.getConnection());
							dataset1.executeQuery(chartCreator.getRequest());
							chart = ChartFactory.createPieChart3D(chartCreator.getChartTitle(), dataset1, false, true, false);
							break;
						case 2://Category
							JDBCCategoryDataset dataset2 = new JDBCCategoryDataset(plnsAnalyzer.getConnection());
							dataset2.executeQuery(chartCreator.getRequest());
							chart = ChartFactory.createBarChart(chartCreator.getChartTitle(), chartCreator.getAbscissesTitle(), chartCreator.getOrdonneesTitle(), 
									dataset2, PlotOrientation.VERTICAL, false, true, false);
							break;
						default:
							break;
						}
					}
				} catch (SQLException e1) {
					e1.printStackTrace();
					JOptionPane.showMessageDialog(getThis(), "<html>L'exécution de la requête a échoué :<br />" +
							e1+
							"</html>", "Impossible de créer le graphique", JOptionPane.ERROR_MESSAGE);
				}
				if(chart != null) addChart(chart);

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

	private void addChart(JFreeChart chart) {
		ChartPanel panel = new ChartPanel(chart);
		chartPanels.add(panel);
		JInternalFrame frame = new JInternalFrame(chart.getTitle().getText(),
				true, false, true, true);
		frame.add(panel);
		frame.pack();
		frame.setVisible(true);
		desktop.add(frame);
		desktop.tile(true);
		if(chartMouseListener != null){
			panel.addChartMouseListener(chartMouseListener);
		}
	}

	private PLNSPanel getThis() {
		return this;
	}
}
