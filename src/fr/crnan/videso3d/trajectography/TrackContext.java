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
import javax.swing.SwingWorker;

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
import fr.crnan.videso3d.layers.TrajectoriesLayer;
import fr.crnan.videso3d.stip.StipController;
import gov.nasa.worldwind.globes.Globe;
/**
 * 
 * @author Bruno Spyckerelle
 * @version 0.0.3
 */
public class TrackContext extends Context {

	private TrajectoriesLayer layer;
	private TrackFilesReader reader;
	private Globe globe;	
	private VidesoTrack track;

	public TrackContext(TrajectoriesLayer layer, TrackFilesReader reader, VidesoTrack track, Globe globe){
		this.layer = layer;
		this.reader = reader;
		this.globe = globe;
		this.track = track;
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

		taskPane1.add(new JLabel("<html><b>Nombre de trajectoires : </b>"+reader.getTracks().size()));

		taskpanes.add(taskPane1);	

		final TracksStatsProducer stats = new TracksStatsProducer();
		final ProgressMonitor progress = new ProgressMonitor(null, "", "", 0, 1);
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
		final JXTaskPane taskPane2 = new JXTaskPane("Informations sur "+track.getName());

		taskPane2.add(new JLabel(String.format("<html><b>Longueur :</b> %.2f NM<html>",
				stats.computeLengthBetweenLevels(track, 0, 660, globe)/LatLonCautra.NM)));

		try {
			if(DatabaseManager.getCurrentStip() != null){
				final JPanel stipList = new JPanel();
				stipList.setLayout(new BoxLayout(stipList, BoxLayout.X_AXIS));
				taskPane2.add(new AbstractAction() {

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
										Component action = ((TaskPaneUI)taskPane2.getUI()).createAction(
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
				taskPane2.add(stipList);
			}
			if(DatabaseManager.getCurrentAIP() != null){
				final JPanel aipList = new JPanel();
				aipList.setLayout(new BoxLayout(aipList, BoxLayout.X_AXIS));
				taskPane2.add(new AbstractAction() {

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
										Component action = ((TaskPaneUI)taskPane2.getUI()).createAction(
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
				taskPane2.add(aipList);
				
				final JPanel aipList2 = new JPanel();
				aipList2.setLayout(new BoxLayout(aipList2, BoxLayout.Y_AXIS));
				taskPane2.add(new AbstractAction() {

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
										Component action = ((TaskPaneUI)taskPane2.getUI()).createAction(
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
				taskPane2.add(aipList2);
				
				final JPanel aipList3 = new JPanel();
				aipList3.setLayout(new BoxLayout(aipList3, BoxLayout.Y_AXIS));
				taskPane2.add(new AbstractAction() {

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
										Component action = ((TaskPaneUI)taskPane2.getUI()).createAction(
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
				taskPane2.add(aipList3);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		taskpanes.add(taskPane2);

		return taskpanes;
	}

}
