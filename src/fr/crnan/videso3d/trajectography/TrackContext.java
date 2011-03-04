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

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.SwingWorker;

import org.jdesktop.swingx.JXTaskPane;

import fr.crnan.videso3d.Context;
import fr.crnan.videso3d.DatasManager;
import fr.crnan.videso3d.DatabaseManager.Type;
import fr.crnan.videso3d.ProgressSupport;
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
 * @version 0.0.2
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
		
		taskPane2.add(new AbstractAction() {
			
			{
				putValue(Action.NAME, "<html><b>Secteurs traversés :</b> Calculer...</html>");
			}
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				new SwingWorker<Integer, Integer>() {

					Collection<Secteur3D> secteurs;
					
					@Override
					protected Integer doInBackground() throws Exception {
						progress.setNote("Calcul des secteurs traversés");
						secteurs = stats.computeContainingSectors(track, Type.STIP);
						return null;
					}

					/* (non-Javadoc)
					 * @see javax.swing.SwingWorker#done()
					 */
					@Override
					protected void done() {
						putValue(Action.NAME, "<html><b>Secteurs traversés :</b></html>");
						Secteur3D last = null;
						for(Secteur3D s : secteurs){
							if(last == null || !s.getName().split(" ")[0].equals(last.getName().split(" ")[0])){
								final Secteur3D tSecteur = s;
								taskPane2.add(new AbstractAction() {
									{
										putValue(Action.NAME, "\t"+tSecteur.getName());
									}
									@Override
									public void actionPerformed(ActionEvent arg0) {
										DatasManager.getController(Type.STIP).highlight(StipController.SECTEUR, tSecteur.getName());
									}
								});
								last = s;
							}
						}
					}
				}.execute();
			}
		});
		
		taskpanes.add(taskPane2);
		
		return taskpanes;
	}

}
