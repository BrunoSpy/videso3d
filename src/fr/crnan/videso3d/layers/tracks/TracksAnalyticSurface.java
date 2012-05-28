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
package fr.crnan.videso3d.layers.tracks;

import java.util.ArrayList;

import fr.crnan.videso3d.formats.VidesoTrack;
import fr.crnan.videso3d.trajectography.TracksModel;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.tracks.TrackPoint;
import gov.nasa.worldwindx.examples.analytics.AnalyticSurface;
/**
 * 
 * @author Bruno Spyckerelle
 * @version 0.1.0
 */
public class TracksAnalyticSurface extends AnalyticSurface {

	public TracksAnalyticSurface(int width, int height,  int scale, TracksModel model){
		super(new Sector(Angle.fromDegrees(40), Angle.fromDegrees(52), Angle.fromDegrees(-8.0), Angle.fromDegrees(10)), 5000.0, width, height);
		this.setAltitudeMode(WorldWind.ABSOLUTE);
		
		int[][] grid = new int[height][width];
		for(VidesoTrack t : model.getAllTracks()){
			for(TrackPoint p : t.getTrackPoints()){
				
				double lat = p.getPosition().latitude.degrees - 40.0;
				double lon = p.getPosition().longitude.degrees + 8.0;
				
				if(lat > 0 && lat < 12 && lon > 0 && lon < 18){
					int x = ((int)(lat*height))/12 ;
					int y = ((int)(lon*width))/18 ;
					
					grid[x][y]++;
				}
			}
		}
		//apply the scale and find the maximum
		int max = 0;
		for(int i = height-1;i>=0;i--){
			for(int j = 0;j<width;j++){
				grid[i][j] = grid[i][j];
				if(grid[i][j] > max) max = grid[i][j];
			}
		}
		//populate grid values
		ArrayList<GridPointAttributes> attributes = new ArrayList<AnalyticSurface.GridPointAttributes>();
		for(int i = height-1;i>=0;i--){
			for(int j = 0;j<width;j++){
				attributes.add(AnalyticSurface.createColorGradientAttributes(grid[i][j], 0, max, 240d/360d /*blue*/, 0d/360d /*red*/));
			}
		}
		this.setVerticalScale(scale);
		this.setValues(attributes);
		
	}
	
}
