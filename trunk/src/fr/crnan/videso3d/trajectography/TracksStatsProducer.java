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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import fr.crnan.videso3d.DatasManager;
import fr.crnan.videso3d.DatasManager.Type;
import fr.crnan.videso3d.ProgressSupport;
import fr.crnan.videso3d.VidesoController;
import fr.crnan.videso3d.databases.aip.AIPController;
import fr.crnan.videso3d.databases.stip.StipController;
import fr.crnan.videso3d.formats.VidesoTrack;
import fr.crnan.videso3d.geom.LatLonUtils;
import fr.crnan.videso3d.graphics.Secteur3D;
import fr.crnan.videso3d.graphics.VidesoObject;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.tracks.TrackPoint;
import gov.nasa.worldwind.util.Logging;

/**
 * 
 * @author Bruno Spyckerelle
 * @version 0.0.3
 */
public class TracksStatsProducer extends ProgressSupport {
		
	/**
	 * 
	 * @param track
	 * @param type {@link VidesoObject#getDatabaseType()}
	 * @param objectType {@link VidesoObject#getType()}
	 * @return Tous les secteurs traversés par <code>track</code>
	 */
	public Collection<Secteur3D> computeContainingSectors(final VidesoTrack track, DatasManager.Type type, int objectType){
		if(DatasManager.getController(type) == null){
			Logging.logger().severe("Controller inexistant");
			return null;
		}
		List<Secteur3D> containingSecteurs = new ArrayList<Secteur3D>();
		Collection<Object> secteurs;
		VidesoController controller;
		if(type.equals(DatasManager.Type.STIP)){
			controller = (StipController) DatasManager.getController(type);
			((ProgressSupport) controller).addPropertyChangeListener(new PropertyChangeListener() {

				@Override
				public void propertyChange(PropertyChangeEvent p) {
					if(p.getPropertyName().equals(ProgressSupport.TASK_STARTS)){
						fireTaskStarts((Integer) p.getNewValue());
					} else if(p.getPropertyName().equals(ProgressSupport.TASK_PROGRESS)){
						fireTaskProgress((Integer) p.getNewValue());
					}
				}
			});
			secteurs = controller.getObjects(StipController.SECTEUR);
		} else if(type.equals(DatasManager.Type.AIP)){
			controller = (AIPController) DatasManager.getController(type);
			((ProgressSupport) controller).addPropertyChangeListener(new PropertyChangeListener() {

				@Override
				public void propertyChange(PropertyChangeEvent p) {
					if(p.getPropertyName().equals(ProgressSupport.TASK_STARTS)){
						fireTaskStarts((Integer) p.getNewValue());
					} else if(p.getPropertyName().equals(ProgressSupport.TASK_PROGRESS)){
						fireTaskProgress((Integer) p.getNewValue());
					}
				}
			});
			secteurs = controller.getObjects(objectType);
		}
		else {
			return null;
		}

		Secteur3D last = null;
		for(TrackPoint point : track.getTrackPoints()){
			
			boolean contain = false;
			Iterator<Object> iterator = secteurs.iterator();
			while(iterator.hasNext() && !contain){
				Secteur3D secteur = (Secteur3D) iterator.next();
				if(secteur.contains(point.getPosition())){
					contain = true;
					if(!secteur.equals(last)){
						last = secteur;
						containingSecteurs.add(secteur);
					}
				}
			}
		}
		
		return containingSecteurs;
	}
	
	/**
	 * Calcule la distance parcourue dans chaque tranche de <code>step</code> FL entre le niveau 0 et le niveau 660.<br />
	 * La valeur à l'indice i correspond à la distance parcourue entre le niveau i*5 et (i+1)*5.<br />
	 * @param track
	 * @param globe
	 * @return
	 */
	public double[] computeLengthRepartition(VidesoTrack track, Globe globe, int step){
		double[] lengths = new double[660/step];
		Iterator<? extends TrackPoint> points = track.getTrackPoints().iterator();
		TrackPoint first = null;
		while(points.hasNext()){
			if(first == null){
				first = points.next();
			} else {
				TrackPoint second = points.next();
				double length = LatLonUtils.computeDistance(first.getPosition(), second.getPosition(), globe);
				if(!Double.isNaN(length)){
					int flMax = (int)(Math.max(first.getPosition().elevation, second.getPosition().elevation)/30.48);
					int flMin = (int)(Math.min(first.getPosition().elevation, second.getPosition().elevation)/30.48);
					if(flMax == flMin){
						lengths[Math.abs(flMax)/step] += length;
					} else {
						for(int i = (flMax/step);i<=(flMin/step);i++){
							if((i+1)*step > flMax){
								lengths[i] += length * (((double)(flMax%step))/((double)((flMax-flMin))));
							} else if(i*step<flMin) {
								lengths[i] += length * (((double)(step-flMin%step))/((double)((flMax-flMin))));
							} else {
								lengths[i] += length * ((double)step/((double)((flMax-flMin))));
							}
						}
					}
					first = second;
				}
			}
		}
		return lengths;
	}
	
	public double computeLengthBetweenLevels(VidesoTrack track, int lowFL, int highFL, Globe globe){
		double length = 0;
		Iterator<? extends TrackPoint> points = track.getTrackPoints().iterator();
		TrackPoint first = null;
		while(points.hasNext()){
			if(first == null){
				first = points.next();
			} else {
				TrackPoint second = points.next();
				if(first.getElevation() >= lowFL*30.48 && first.getElevation() <= highFL*30.48
					&& second.getElevation() >= lowFL*30.48 && second.getElevation() <= highFL*30.48){
					length += LatLonUtils.computeDistance(first.getPosition(), second.getPosition(), globe);
				}
				first = second;
			}
		}
		return length;
	}
	
	
}
