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

import fr.crnan.videso3d.databases.stip.PointNotFoundException;
import fr.crnan.videso3d.formats.TrackFilesReader;
import fr.crnan.videso3d.formats.VidesoTrack;
import fr.crnan.videso3d.graphics.PolygonAnnotation;
import fr.crnan.videso3d.ihm.components.ProgressInputStream;
import fr.crnan.videso3d.trajectography.TracksModel;
import fr.crnan.videso3d.trajectography.TrajectoryFileFilter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * Lecteur de fichiers Elvira GEO.<br />
 * @author Bruno Spyckerelle
 * @version 0.2.2
 */
public class GEOReader extends TrackFilesReader{
	private boolean importRapide = false;	
	private PolygonAnnotation polygon;
	
	public GEOReader(Vector<File> files) throws PointNotFoundException {
		super(files);
		this.setModel(new TracksModel());
	}
	
	public GEOReader(File selectedFile) throws PointNotFoundException {
		super(selectedFile);
		this.setModel(new TracksModel());
	}

	public GEOReader(File selectedFile, TracksModel model) throws PointNotFoundException {
		super(selectedFile, model);
	}
	
	public GEOReader(Vector<File> geoFile, TracksModel model) throws PointNotFoundException {
		super(geoFile, model);
	}
	
	public GEOReader(Vector<File> files, TracksModel model,
			List<TrajectoryFileFilter> filters, boolean disjunctive, boolean importRapide) throws PointNotFoundException {
		super(files, model, filters, disjunctive);
		this.importRapide=importRapide;
	}

	public static Boolean isGeoFile(File file){
		BufferedReader in = null;
		Boolean geo = false;
		try {
			in = new BufferedReader(
					new InputStreamReader(
					new FileInputStream(file)));
			int count = 0; //nombre de lignes lues
			while(in.ready() && !geo && count < 10){//on ne lit que les 10 premières lignes pour détecter le type de fichier
				String line = in.readLine();
				if(line.startsWith("!	Version") || line.startsWith("!	voie")){
					geo = true;
				}
				count++;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(in != null)
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		return geo;
	}
	
    @Override
    protected void doReadStream(ProgressInputStream stream)
    {
        String sentence;

        BufferedReader in = null;
        
        Double timeFileFilterBegin = null;
        Double timeFileFilterEnd = null;
        //Find if a Time filter is set
        if(filters != null && filters.size() != 0){
        	for(TrajectoryFileFilter f : filters){
        		if(f.getField() == TracksModel.FIELD_TIME_BEGIN){
        			String[] time = f.getRegexp().split(":");
        			timeFileFilterBegin = (double) ((new Integer(time[0])*3600)+(new Integer(time[1])*60)+(new Integer(time[2])));
        		} else if(f.getField() == TracksModel.FIELD_TIME_END){
        			String[] time = f.getRegexp().split(":");
        			timeFileFilterEnd = (double) ((new Integer(time[0])*3600)+(new Integer(time[1])*60)+(new Integer(time[2])));
        		}
        	}
        }

        try
        {
        	in =  new BufferedReader(new InputStreamReader(stream));
        	GEOTrack track = null;
        	boolean trackValid = true;
        	sentence = "!";
    		while(sentence.startsWith("!")  || !sentence.startsWith("Voie")){
    			sentence = in.readLine();
    		}
    		if(!importRapide || (timeFileFilterBegin==null && timeFileFilterEnd==null)){
    			while(in.ready() && !isCanceled()){
    				sentence = in.readLine();
    				if (sentence != null){
    					if(track == null || track.getNumTraj().compareTo(new Integer(sentence.split("\t")[1]))!=0){
    						if(track != null) {
    							if(timeFileFilterEnd!=null){
    								//Si on a atteint une trajectoire dont l'heure de début est postérieure à l'heure du filtre de fin, on arrête la lecture.
    								if(Double.parseDouble(sentence.split("\t")[3])>timeFileFilterEnd)
    									break;
    							}
    							if(trackValid && track.getNumPoints()>1) this.addTrackToModel(track, trackValid);
    						}
    						track = new GEOTrack(sentence);
    						trackValid = this.isTrackValid(track);
    					} 
    					if(trackValid){
    						GEOTrackPoint point = new GEOTrackPoint(sentence);	
    						//si filtres horaires
    						//on ne garde que les points compris dans la période filtrée
    						if((timeFileFilterBegin == null || (timeFileFilterBegin != null && point.getDecimalTime() > timeFileFilterBegin))
    								&& (timeFileFilterEnd == null || (timeFileFilterEnd != null && point.getDecimalTime() < timeFileFilterEnd))){
    							track.addTrackPoint(sentence);
    						}
    					}
    				}
    			}
    		} else {
    			if(timeFileFilterBegin!=null && timeFileFilterEnd==null){
    					while(in.ready() && !isCanceled()){
    					sentence = in.readLine();
    					if(Double.parseDouble(sentence.split("\t")[3])<timeFileFilterBegin){
    						for(int i=0;i<30;i++)
    							in.readLine();
    					}else{
    						if(track == null || track.getNumTraj().compareTo(new Integer(sentence.split("\t")[1]))!=0){
    							if(track != null) {
    								if(trackValid && track.getNumPoints()>1) this.getModel().addTrack(track);
    							}
    							track = new GEOTrack(sentence);
    							trackValid = this.isTrackValid(track);
    						} 
    						if(trackValid)
    							track.addTrackPoint(sentence);
    					}
    				}
    			}else if(timeFileFilterBegin==null && timeFileFilterEnd!=null){
    				while(in.ready() && !isCanceled()){
    					if(Double.parseDouble(sentence.split("\t")[3])>timeFileFilterEnd){
    						if(track.getNumTraj().compareTo(new Integer(sentence.split("\t")[1]))!=0)
    							break;
    						for(int i=0;i<30;i++)
    							in.readLine();
    					}else{
    						if(track == null || track.getNumTraj().compareTo(new Integer(sentence.split("\t")[1]))!=0){
    							if(track != null) {
    								if(trackValid && track.getNumPoints()>1) this.getModel().addTrack(track);
    							}
    							track = new GEOTrack(sentence);
    							trackValid = this.isTrackValid(track);
    						} 
    						if(trackValid)
    							track.addTrackPoint(sentence);
    					}
    				}
    			}else if(timeFileFilterBegin!=null && timeFileFilterEnd!=null){
    				while(in.ready() && !isCanceled()){
    					double currentTime = Double.parseDouble(sentence.split("\t")[3]);
    					if(currentTime < timeFileFilterBegin){
    						for(int i=0;i<30;i++)
    							in.readLine();
    					}else if(currentTime > timeFileFilterEnd){
    						if(track.getNumTraj().compareTo(new Integer(sentence.split("\t")[1]))!=0)
    							break;
    						for(int i=0;i<30;i++)
    							in.readLine();
    					}else{
    						if(track == null || track.getNumTraj().compareTo(new Integer(sentence.split("\t")[1]))!=0){
    							if(track != null) {
    								if(trackValid && track.getNumPoints()>1) this.addTrackToModel(track, trackValid);
    							}
    							track = new GEOTrack(sentence);
    							trackValid = this.isTrackValid(track);
    						} 
    						if(trackValid)
    							track.addTrackPoint(sentence);
    					}
    				}
    			}
    		}


    		//last Track
    		if(track != null){
    			this.addTrackToModel(track, trackValid);
    		}
        } catch (IOException e) {
        	e.printStackTrace();
        } catch (Exception e){
        	e.printStackTrace();
        } finally {
        	if(in != null)
        		try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
    }

    private void addTrackToModel(GEOTrack track, boolean trackValid){
    	if(track.getNumPoints() <= 1)
    		return;
    	
    	if(!hasFilters() && !hasPolygonFilter())
    		this.getModel().addTrack(track);

    	if(this.disjunctive){
    		if((hasFilters() ? trackValid : false) || (hasPolygonFilter() ? isInsidePolygon(track) : false))
    			this.getModel().addTrack(track);
    	} else {
    		if((hasFilters() ? trackValid : true) && (hasPolygonFilter() ? isInsidePolygon(track) : true))
    			this.getModel().addTrack(track);
    	}
    }
    
    private boolean isInsidePolygon(GEOTrack track){
    	if(this.polygon != null) {
    		Iterator<GEOTrackPoint> iterator = track.getTrackPoints().iterator();
    		boolean pointInside = false;
    		while(iterator.hasNext() && !pointInside){
    			GEOTrackPoint point = iterator.next();
    			pointInside = pointInside || this.polygon.contains(point.getPosition());
    		}
    		return pointInside;
    	} else {
    		return true;
    	}
    }
    
    private Boolean hasFilter = null;
    /**
     * 
     * @return <code>true</code> if at least one filter is set, except a polygon filter
     */
    private boolean hasFilters(){
    	if(hasFilter == null){
    		if(filters == null){
    			hasFilter = false;
    		} else {
    			int count = 0;
    			for(TrajectoryFileFilter f : filters){
    				if(f.getField() == TracksModel.FIELD_ADEP ||
    						f.getField() == TracksModel.FIELD_ADEST ||
    						f.getField() == TracksModel.FIELD_MODE_A ){
    					count++;
    				}
    			}
    			hasFilter = count > 0;
    		}
    	}
    	return hasFilter;
    }

    private Boolean hasPolygon = null; //store calculation
    /**
     * Save the polygon
     * @return true if one was found
     */
    private boolean hasPolygonFilter(){
    	if(hasPolygon == null){
    		if(filters == null){
    			hasPolygon = false;
    		} else {
    			int count = 0;
    			for(TrajectoryFileFilter f : filters){
    				if(f.getField() == TracksModel.FIELD_POLYGON && f.getPolygon() != null ){
    					count++;
    					this.polygon = f.getPolygon();
    				}
    			}
    			hasPolygon = count > 0;
    		}
    	}
    	return hasPolygon;
    }

    @Override
    protected boolean isTrackValid(VidesoTrack track) {
    	if(filters == null)
    		return true;

    	if(!hasFilters())
    		return true;

    	boolean result = !this.disjunctive;
    	for(TrajectoryFileFilter f : filters){
    		switch (f.getField()) {
    		case TracksModel.FIELD_ADEP:
    			result = this.disjunctive
    			? result || track.getDepart().matches(f.getRegexp())
    					: result && track.getDepart().matches(f.getRegexp());
    			break;
    		case TracksModel.FIELD_ADEST:
    			result = this.disjunctive
    			? result || track.getArrivee().matches(f.getRegexp())
    					: result && track.getArrivee().matches(f.getRegexp());
    			break;
    		case TracksModel.FIELD_MODE_A:
    			result = this.disjunctive
    			? result || track.getModeA().toString().matches(f.getRegexp())
    					: result && track.getModeA().toString().matches(f.getRegexp());
    			break;
    		default:
    			break;
    		}
    	}
    	return result;

    }

}
