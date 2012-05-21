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

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYPolygonAnnotation;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.TextAnchor;

import fr.crnan.videso3d.DatasManager;
import fr.crnan.videso3d.DatabaseManager.Type;
import fr.crnan.videso3d.aip.AIP;
import fr.crnan.videso3d.aip.AIPController;
import fr.crnan.videso3d.formats.VidesoTrack;
import fr.crnan.videso3d.formats.lpln.LPLNTrack;
import fr.crnan.videso3d.formats.lpln.LPLNTrackPoint;
import fr.crnan.videso3d.geom.LatLonCautra;
import fr.crnan.videso3d.graphics.Secteur3D;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.globes.Earth;
/**
 * Coupe en 2D d'un track avec secteurs traversés
 * @author Bruno Spyckerelle
 * @version 0.0.1
 * @deprecated En attendant de finir le travail...
 */
public class Track2DView extends JFrame{

	public Track2DView(VidesoTrack track) {
		if(track instanceof LPLNTrack){
			XYSeries dataset = new XYSeries(track.getName());
			List<ValueMarker> markers = new LinkedList<ValueMarker>();
			double distance = 0;
			LPLNTrackPoint last = null;
			for(LPLNTrackPoint p : ((LPLNTrack)track).getTrackPoints()){
				if(last != null){
					distance += Position.ellipsoidalDistance(last.getPosition(), p.getPosition(),
							Earth.WGS84_EQUATORIAL_RADIUS, Earth.WGS84_POLAR_RADIUS)/LatLonCautra.NM;
				}
				dataset.add(distance, p.getElevation()/30.48);
				ValueMarker marker = new ValueMarker(distance);
				marker.setLabel(p.getName());
				marker.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
		        marker.setLabelTextAnchor(TextAnchor.TOP_CENTER);
				markers.add(marker);
				last = p;
			}
			JFreeChart chart = ChartFactory.createXYLineChart("Coupe 2D", "NM", "FL", new XYSeriesCollection(dataset), PlotOrientation.VERTICAL, false, false, false);
			for(ValueMarker m : markers){
				chart.getXYPlot().addDomainMarker(m);
			}
			//ajout des secteurs AIP avec des XYPolygonAnnotation
			Collection<Object> secteurs;
			AIPController controller = (AIPController) DatasManager.getController(Type.AIP);
			secteurs = controller.getObjects(AIP.CTL);
			for(int i=0;i<=600;i+=10){
				last = null;
				Secteur3D lastSecteur = null;
				double lastBoundary = 0.0;
				for(LPLNTrackPoint point : ((LPLNTrack)track).getTrackPoints()){
					Position p = new Position(point.getPosition(), i*30.48);
					//calcul du secteur contenant le point en cours
					Iterator<Object> iterator = secteurs.iterator();
					boolean contain = false;
					Secteur3D secteur = null;
					while(iterator.hasNext() && !contain){
						Secteur3D temp = (Secteur3D) iterator.next();
						if(temp.contains(p)){
							contain = true;
							secteur = temp;
						}
					}
					
					//si premier point, on enregistre simplement le secteur trouvé
					if(last == null){
						lastSecteur = secteur;
					} else {
						if(lastSecteur != secteur){
							//si le secteur a changé, on dessine le secteur précédent
							//sauf si ce dernier n'existait pas
							if(lastSecteur != null){
								//dans ce cas, on calcule le point d'intersection entre le secteur et le segment formé par les deux points
								//lastSecteur != null => last !=null
								Set<Point2D> intersects = lastSecteur.getIntersections(new Line2D.Double(last.getLatitude(), last.getLongitude(),
										p.getLatitude().degrees, p.getLongitude().degrees), true);
								if(!intersects.isEmpty()){
									Point2D intersect = intersects.iterator().next();
									distance = Position.ellipsoidalDistance(new LatLonCautra(intersect.getX(), intersect.getY()),
											last.getPosition(), Earth.WGS84_EQUATORIAL_RADIUS, Earth.WGS84_POLAR_RADIUS)/LatLonCautra.NM;
									//et ajout de l'annotation
									XYPolygonAnnotation annotation = new XYPolygonAnnotation(new double[]{lastBoundary, i, lastBoundary+distance, i, lastBoundary+distance, i+10, lastBoundary, i+10});
									chart.getXYPlot().addAnnotation(annotation);
									lastBoundary += distance;
								}
							}
							lastSecteur = secteur;
						}
					}
					last = point;
				}
			}
			//espace en haut pour les marqueurs
			chart.getXYPlot().getRangeAxis().setUpperMargin(0.05);
			ChartPanel chartPanel = new ChartPanel(chart);
			this.setContentPane(chartPanel);
			this.pack();
		} 
	}

}
