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

package fr.crnan.videso3d.trajectography;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.plaf.TaskPaneUI;

import fr.crnan.videso3d.Context;
import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.DatasManager;
import fr.crnan.videso3d.DatabaseManager.Type;
import fr.crnan.videso3d.ProgressSupport;
import fr.crnan.videso3d.aip.AIP;
import fr.crnan.videso3d.formats.TrackFilesReader;
import fr.crnan.videso3d.formats.VidesoTrack;
import fr.crnan.videso3d.geom.LatLonCautra;
import fr.crnan.videso3d.graphics.Secteur3D;
import fr.crnan.videso3d.ihm.ProgressMonitor;
import fr.crnan.videso3d.ihm.components.VXTable;
import fr.crnan.videso3d.stip.StipController;
import gov.nasa.worldwind.globes.Globe;
/**
 * 
 * @author Bruno Spyckerelle
 * @version 0.0.3
 */
public class TrackContext extends Context {

	private TrackFilesReader reader;
	private Globe globe;	
	private VidesoTrack track;

	private ProgressMonitor progress = new ProgressMonitor(null, "", "", 0, 1);
	private JXTaskPane trackPane;
	private JXTaskPane layerPane;
	private TracksStatsProducer stats;
	
	public TrackContext(TrackFilesReader reader, VidesoTrack track, Globe globe){
		this.reader = reader;
		this.globe = globe;
		this.track = track;
		
		this.trackPane = new JXTaskPane();
		this.layerPane = new JXTaskPane();
		this.stats = new TracksStatsProducer();
	}

	@Override
	public List<JXTaskPane> getTaskPanes(int type, String name) {
		List<JXTaskPane> taskpanes = new ArrayList<JXTaskPane>();

		JXTaskPane taskPane1 = new JXTaskPane("Informations générales");

		String listFiles = new String();
		for(File f : reader.getFiles()){
			listFiles += "<li>";
			listFiles += f.getName();
			listFiles += "</li>";
		}
		taskPane1.add(new JLabel("<html><b>Liste des fichiers :</b><br/>" +
				"<ul>" +
				listFiles +
		"</ul></html>"));

		taskPane1.add(new JLabel("<html><b>Nombre de trajectoires : </b>"+reader.getModel().getVisibleTracks().size()));

		taskpanes.add(taskPane1);	
		
		stats.addPropertyChangeListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent p) {
				if(p.getPropertyName().equals(ProgressSupport.TASK_STARTS)){
					progress.setMaximum((Integer) p.getNewValue());
					progress.resetTimer();
				} else if(p.getPropertyName().equals(ProgressSupport.TASK_PROGRESS)){
					progress.setProgress((Integer) p.getNewValue());
				}
			}
		});
		
		if(this.track != null)
			this.updateTrackPane(track);
		taskpanes.add(trackPane);
		
		this.updateLayerPane();
		taskpanes.add(layerPane);
		
		return taskpanes;
	}

	public void updateLayerPane(){
		this.layerPane.removeAll();
		this.layerPane.setTitle("Informations sur les trajectoires affichées");
		
		final JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		Action action = new AbstractAction() {
			{
				putValue(Action.NAME, "<html><b>Répartition en niveaux : </b>Calculer ...</html>");
			}
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				progress.setMaximum(reader.getModel().getVisibleTracks().size());
				progress.setNote("Calcul de la répartition en tranches de niveau des trajectoires");
				progress.resetTimer();
				this.setEnabled(false);
				this.putValue(Action.NAME,"<html><b>Répartition en niveaux : </b></html>");
				
				new SwingWorker<Integer, Integer>(){

					@Override
					protected Integer doInBackground() throws Exception {
						double[] lengths = new double[66];
						int p = 1;
						for(VidesoTrack track : reader.getModel().getVisibleTracks()){
							progress.setProgress(p++);
							double[] temp = stats.computeLengthRepartition(track, globe,10);

							for(int i = 0; i<66;i++){
								lengths[i] += temp[i];
							}
						}
						setEnabled(false);
						double total = 0;
						int lastNonNul = 0;
						ArrayList<Integer> start = new ArrayList<Integer>();
						ArrayList<Integer> end = new ArrayList<Integer>(); 
						ArrayList<Double> percent = new ArrayList<Double>(); 
						for(int i=0;i<66;i++){
							total += lengths[i];
							if(lengths[i] != 0.0) 
								lastNonNul = i;
						}
						
						for(int i=0;i<=lastNonNul;i++){
							start.add(i*10);
							end.add((i+1)*10);
							percent.add(lengths[i]/total*100.0);
						}
						
						DefaultTableModel model = new DefaultTableModel();
						model.addColumn("Du FL", start.toArray());
						model.addColumn("Au FL", end.toArray());
						model.addColumn("%", percent.toArray());
						VXTable table = new VXTable(model);
						table.setEditable(false);
						JScrollPane jsp = new JScrollPane(table);
						jsp.setBorder(null);
						panel.add(jsp);
						return null;
					}
					
				}.execute();
				
			}
		};
		
		this.layerPane.add(action);
		this.layerPane.add(panel);
	}
	
	public void updateTrackPane(final VidesoTrack track){
		trackPane.setTitle("Informations sur "+track.getName());
		trackPane.removeAll();

		trackPane.add(new JLabel(String.format("<html><b>Longueur :</b> %.2f NM<html>",
				stats.computeLengthBetweenLevels(track, 0, 660, globe)/LatLonCautra.NM)));

		try {
			if(DatabaseManager.getCurrentStip() != null){
				final JPanel stipList = new JPanel();
				stipList.setLayout(new BoxLayout(stipList, BoxLayout.X_AXIS));
				trackPane.add(new AbstractAction() {

					{
						putValue(Action.NAME, "<html><b>Secteurs STIP traversés :</b> Calculer...</html>");
					}

					@Override
					public void actionPerformed(ActionEvent arg0) {

						new SwingWorker<Integer, Integer>() {

							Collection<Secteur3D> secteurs;

							@Override
							protected Integer doInBackground() throws Exception {
								progress.setNote("Calcul des secteurs STIP traversés");
								secteurs = stats.computeContainingSectors(track, Type.STIP, StipController.SECTEUR);
								return null;
							}

							/* (non-Javadoc)
							 * @see javax.swing.SwingWorker#done()
							 */
							@Override
							protected void done() {
								putValue(Action.NAME, "<html><b>Secteurs STIP traversés :</b></html>");
								Secteur3D last = null;
								for(Secteur3D s : secteurs){
									if(last == null || !s.getName().split(" ")[0].equals(last.getName().split(" ")[0])){
										final Secteur3D tSecteur = s;
										Component action = ((TaskPaneUI)trackPane.getUI()).createAction(
												new AbstractAction() {
													{
														putValue(Action.NAME, "\t"+tSecteur.getName());
													}
													@Override
													public void actionPerformed(ActionEvent arg0) {
														DatasManager.getController(Type.STIP).showObject(StipController.SECTEUR, tSecteur.getName());
													}
										});
										stipList.add(action);
										stipList.add(new JLabel(" "));
										last = s;
									}
								}
							}
						}.execute();
					}
				});
				trackPane.add(stipList);
			}
			if(DatabaseManager.getCurrentAIP() != null){
				final JPanel aipList = new JPanel();
				aipList.setLayout(new BoxLayout(aipList, BoxLayout.X_AXIS));
				trackPane.add(new AbstractAction() {

					{
						putValue(Action.NAME, "<html><b>Secteurs AIP traversés :</b> Calculer...</html>");
					}

					@Override
					public void actionPerformed(ActionEvent arg0) {

						new SwingWorker<Integer, Integer>() {

							Collection<Secteur3D> secteurs;

							@Override
							protected Integer doInBackground() throws Exception {
								progress.setNote("Calcul des secteurs AIP traversés");
								secteurs = stats.computeContainingSectors(track, Type.AIP, AIP.CTL);
								return null;
							}

							/* (non-Javadoc)
							 * @see javax.swing.SwingWorker#done()
							 */
							@Override
							protected void done() {
								putValue(Action.NAME, "<html><b>Secteurs AIP traversés :</b></html>");
								Secteur3D last = null;
								for(Secteur3D s : secteurs){
									if(last == null || !s.getName().split(" ")[0].equals(last.getName().split(" ")[0])){
										final Secteur3D tSecteur = s;
										Component action = ((TaskPaneUI)trackPane.getUI()).createAction(
												new AbstractAction() {
													{
														putValue(Action.NAME, "\t"+tSecteur.getName().split("\\s+")[0]);
													}
													@Override
													public void actionPerformed(ActionEvent arg0) {
														DatasManager.getController(Type.AIP).showObject(AIP.CTL, tSecteur.getName().split("\\s+")[0]);
													}
										});
										aipList.add(action);
										aipList.add(new JLabel(" "));
										last = s;
									}
								}
							}
						}.execute();
					}
				});
				trackPane.add(aipList);
				
				final JPanel aipList2 = new JPanel();
				aipList2.setLayout(new BoxLayout(aipList2, BoxLayout.Y_AXIS));
				trackPane.add(new AbstractAction() {

					{
						putValue(Action.NAME, "<html><b>TMA AIP traversées :</b> Calculer...</html>");
					}

					@Override
					public void actionPerformed(ActionEvent arg0) {

						new SwingWorker<Integer, Integer>() {

							Collection<Secteur3D> secteurs;

							@Override
							protected Integer doInBackground() throws Exception {
								progress.setNote("Calcul des TMA AIP traversées");
								secteurs = stats.computeContainingSectors(track, Type.AIP, AIP.TMA);
								return null;
							}

							/* (non-Javadoc)
							 * @see javax.swing.SwingWorker#done()
							 */
							@Override
							protected void done() {
								putValue(Action.NAME, "<html><b>TMA AIP traversés :</b></html>");
								Secteur3D last = null;
								for(Secteur3D s : secteurs){
									if(last == null || !s.getName().equals(last.getName())){
										final Secteur3D tSecteur = s;
										Component action = ((TaskPaneUI)trackPane.getUI()).createAction(
												new AbstractAction() {
													{
														putValue(Action.NAME, "\t"+tSecteur.getName());
													}
													@Override
													public void actionPerformed(ActionEvent arg0) {
														DatasManager.getController(Type.AIP).showObject(AIP.TMA, tSecteur.getName());
													}
										});
										aipList2.add(action);
										last = s;
									}
								}
							}
						}.execute();
					}
				});
				trackPane.add(aipList2);
				
				final JPanel aipList3 = new JPanel();
				aipList3.setLayout(new BoxLayout(aipList3, BoxLayout.Y_AXIS));
				trackPane.add(new AbstractAction() {

					{
						putValue(Action.NAME, "<html><b>CTR AIP traversées :</b> Calculer...</html>");
					}

					@Override
					public void actionPerformed(ActionEvent arg0) {

						new SwingWorker<Integer, Integer>() {

							Collection<Secteur3D> secteurs;

							@Override
							protected Integer doInBackground() throws Exception {
								progress.setNote("Calcul des CTR AIP traversées");
								secteurs = stats.computeContainingSectors(track, Type.AIP, AIP.CTR);
								return null;
							}

							/* (non-Javadoc)
							 * @see javax.swing.SwingWorker#done()
							 */
							@Override
							protected void done() {
								putValue(Action.NAME, "<html><b>CTR AIP traversées :</b></html>");
								Secteur3D last = null;
								for(Secteur3D s : secteurs){
									if(last == null || !s.getName().equals(last.getName())){
										final Secteur3D tSecteur = s;
										Component action = ((TaskPaneUI)trackPane.getUI()).createAction(
												new AbstractAction() {
													{
														putValue(Action.NAME, "\t"+tSecteur.getName());
													}
													@Override
													public void actionPerformed(ActionEvent arg0) {
														DatasManager.getController(Type.AIP).showObject(AIP.CTR, tSecteur.getName());
													}
										});
										aipList3.add(action);
										last = s;
									}
								}
							}
						}.execute();
					}
				});
				trackPane.add(aipList3);
				
				trackPane.add(new AbstractAction() {
					{
						putValue(Action.NAME, "<html><b>Coupe 2D :</b> Afficher...</html>");
					}
					
					@Override
					public void actionPerformed(ActionEvent arg0) {
						new Track2DView(track).setVisible(true);
					}
				});
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
}
