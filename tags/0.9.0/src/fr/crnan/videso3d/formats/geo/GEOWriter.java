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

package fr.crnan.videso3d.formats.geo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import fr.crnan.videso3d.Configuration;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.util.Logging;
/**
 * Export de trajectoires au format GEO<br />
 * Si <code>lite</code> est vrai, n'enregistre que les points pertinents selon {@link Configuration}
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class GEOWriter {

    private final PrintWriter printWriter;
        
    private int numPiste = 0;
	
    private Boolean lite = false;
    
    private Position lastPos;
    
    private String path;
    
	public GEOWriter(String path) throws IOException {
		if (path == null)
        {
            String msg = Logging.getMessage("nullValue.PathIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
		this.path = path;
        this.printWriter = new PrintWriter(new BufferedWriter(new FileWriter(path)));
        this.printWriter.println("!\tVersion 0");
        this.printWriter.println("Voie\tNum Traj\tNum Piste\tHeure\tLat\tLong\tModeC\tAltitude\tVitesse\tCap\tVz\tModeA\tIndicatif\tMsaw\tType\tAvion\tAdresse ModeS\tDépart\tArrivée");
	}
	
	public GEOWriter(String path, Boolean lite) throws IOException{
		this(path);
		this.lite = lite;
	}
	
	public void writeTrack(GEOTrack track){
        if (track == null)
        {
            String msg = Logging.getMessage("nullValue.TrackIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        doWriteTrack(track,this.printWriter);
        doFlush();
	}
	
    public void close()
    {
        doFlush();
        this.printWriter.close();
    }
    
    private void doWriteTrack(GEOTrack track, PrintWriter out)  {
    	if (track != null && track.getTrackPoints() != null) {
    		numPiste++;
    		lastPos = null ;
    		for (GEOTrackPoint ts : track.getTrackPoints()) {
    			if(lite) {
    				if(lastPos == null || Position.greatCircleDistance(lastPos, ts.getPosition()).degrees > Double.parseDouble(Configuration.getProperty(Configuration.TRAJECTOGRAPHIE_PRECISION, "0.01"))) {
    					doWriteTrackPoint(track, ts, out);
    	    			lastPos = ts.getPosition();
    				}
    			} else {
    				doWriteTrackPoint(track, ts, out);
    			}
    		}
    	}
    }

    private void doWriteTrackPoint(GEOTrack track, GEOTrackPoint point, PrintWriter out) {
    	if (point != null)   {
    		out.println("0\t"+
    				track.getNumTraj()+"\t" +
    				numPiste+"\t" +
    				point.getDecimalTime()+"\t" +
    				point.getLatitude()+"\t" +
    				point.getLongitude()+"\t"+
    				"\t"+
    				point.getElevation()*3.28083+"\t"+
    				point.getVitesse()+"\t"+
    				"\t"+
    				"\t"+
    				"\t"+
    				track.getIndicatif()+"\t"+
    				"\t"+
    				track.getType()+"\t"+
    				"\t"+
    				track.getDepart()+"\t"+
    				track.getArrivee()+"\t");
    	}
    }

    private void doFlush() {
        this.printWriter.flush();
    }

	public void cancel() {
		doFlush();
		this.printWriter.close();
		File file = new File(path);
		if(file.exists())
			file.delete();
	}
}
