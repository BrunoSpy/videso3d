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

package fr.crnan.videso3d.graphics;


/**
 * Radio coverage polygons.
 *TODO : clone() method, remove() method
 *
 */

import gov.nasa.worldwind.geom.*;
//import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Logging;
//import gov.nasa.worldwind.util.RestorableSupport;
import gov.nasa.worldwind.render.airspaces.*;

import java.util.ArrayList;
import java.util.Collection;
//import java.util.Collections;
import java.util.List;


public class RadioCovPolygon extends AbstractAirspace {

	private boolean DEBUG = false;
	private ArrayList<Curtain> curtains = new ArrayList<Curtain>();
	private ArrayList<double[]> refAltitudes = new ArrayList<double[]>(); //reference altitudes, en cas de mofification des valeurs min et max de chaque curtain.
//	private ArrayList<Curtain> saveCurtains =new ArrayList<Curtain>(); // liste de base des curtains.
	private boolean enableInnerCaps = true;
	private boolean curtainsOutOfDate = true;	
	private String name;
	
	private double minAltitude=0;
	private double maxAltitude=0;
	
	public void setName (String name) {this.name=name;}
	public String getName() {return this.name;}
	
	public void setMaxAltitude(double maxAltitude) {
		this.maxAltitude=maxAltitude;
	}
	
	public void setMinAltitude(double minAltitude) {
		this.minAltitude=minAltitude;
	}
	
	public void setVisible(int minIndex,int maxIndex, boolean visible/*,double minValue, double maxValue*/) {
		for (int i=minIndex;i<maxIndex;i++) {
			curtains.get(i).setVisible(visible);
		}
	}
	
	public void setVisible(int index,Boolean visible) {
		curtains.get(index).setVisible(visible);
	}
	
	public void setVisible(Boolean visible) {
		super.setVisible(visible);
		for (Curtain c : curtains ) {
			c.setVisible(visible); 			
		}
	}
	
	public void setRefAltitudes() {
		int index=0;
    	for (Curtain c : curtains) {    		    		    		
    	refAltitudes.add(index,c.getAltitudes());
    	index++;
    	}
	}
	
	public double getMaxAltitude() {
		return this.maxAltitude;
	}
	
	public double getMinAltitude() {
		return this.minAltitude;
	}
	
	public double[]getRefAltitudes(int index) {
		return this.refAltitudes.get(index);
	}
		
	public RadioCovPolygon(Collection<Curtain> curtains) {
        this.addCurtains(curtains);
    }

    public RadioCovPolygon(AirspaceAttributes attributes) {
        super(attributes);
    }


    public RadioCovPolygon() {    	
    }

    public List<Curtain> getCurtains() {
        //return Collections.unmodifiableList(this.curtains);
    	return this.curtains;
    }

    /*
    public List<Curtain> getSaveCurtains() {
    	return this.saveCurtains;
    }
 */   
    public void setCurtains(Collection<Curtain> curtains) {
        this.curtains.clear();
        this.addCurtains(curtains);
    }

    protected void addCurtains(Iterable<Curtain> newCurtains) {
        if (newCurtains != null)
        {
            for (Curtain c : newCurtains)
                if (c != null) this.curtains.add(c);            	
        }
    }
	
    public void removeCurtains(int index) {
    	this.curtains.remove(index);
    }
    
	   public void addCurtain(List<? extends LatLon> locations, double lowerAltitude, double upperAltitude) {      

		boolean[] terrainConformant = this.isTerrainConforming();
        Curtain curtain = new Curtain();
        curtain.setAltitudes(lowerAltitude, upperAltitude);
        curtain.setTerrainConforming(terrainConformant[0], terrainConformant[1]);
        curtain.setLocations(locations);
        this.curtains.add(curtain);        
    }
	
	   public void addCurtain(Curtain c) {
		   this.curtains.add(c);
	   }
/*	   
	   public void addSaveCurtain(Curtain c) {
		   this.saveCurtains.add(c);
	   }
*/	   
	   public boolean isEnableInnerCaps()
	    {
	        return this.enableInnerCaps;
	    }

	    public void setEnableInnerCaps(boolean draw)
	    {
	        this.enableInnerCaps = draw;
	        this.setCurtainsOutOfDate();
	    }
	    	    
	    public void setAltitudes(double lowerAltitude, double upperAltitude)
	    {
	        super.setAltitudes(lowerAltitude, upperAltitude);

	        for (Curtain c : this.curtains)
	            c.setAltitudes(lowerAltitude, upperAltitude);

	        this.setCurtainsOutOfDate();
	    }

	    public void setTerrainConforming(boolean lowerTerrainConformant, boolean upperTerrainConformant)
	    {
	        super.setTerrainConforming(lowerTerrainConformant, upperTerrainConformant);

	        for (Curtain c : this.curtains)
	            c.setTerrainConforming(lowerTerrainConformant, upperTerrainConformant);

	        this.setCurtainsOutOfDate();
	    }

	    public boolean isAirspaceVisible(DrawContext dc)
	    {
	        if (dc == null)
	        {
	            String message = Logging.getMessage("nullValue.DrawContextIsNull");
	            Logging.logger().severe(message);
	            throw new IllegalArgumentException(message);
	        }
	        
	        boolean visible = false;
	        for (Curtain c : this.curtains)
	            if (c.isAirspaceVisible(dc))
	                visible = true;
	        return visible;
	    }

	    public Position getReferencePosition()
	    {
	        return this.computeReferencePosition((List<LatLon>)(this.curtains.get(0).getLocations()), this.getAltitudes());
	    }

	    protected void doMoveTo(Position oldRef, Position newRef)
	    {
	        if (oldRef == null)
	        {
	            String message = "nullValue.OldRefIsNull";
	            Logging.logger().severe(message);
	            throw new IllegalArgumentException(message);
	        }
	        if (newRef == null)
	        {
	            String message = "nullValue.NewRefIsNull";
	            Logging.logger().severe(message);
	            throw new IllegalArgumentException(message);
	        }

	        // Don't call super.moveTo(). Each box should move itself according to the properties it was constructed with.

	        for (Curtain curtain : this.curtains)
	            //curtain.doMoveTo(oldRef, newRef);
	        	curtain.moveTo(newRef);
	        this.setExtentOutOfDate();
	        this.setCurtainsOutOfDate();
	    }

	    protected boolean isCurtainsOutOfDate()
	    {
	        return this.curtainsOutOfDate;
	    }

	    protected void setCurtainsOutOfDate()
	    {
	        this.curtainsOutOfDate = true;
	    }
	
	    
	    //**************************************************************//
	    //********************  Geometry Rendering  ********************//
	    //**************************************************************//

	    protected Extent doComputeExtent(DrawContext dc)
	    {
	        // TODO: compute bounding cylinder enclosing legs
	        return null;
	    }

	    protected void doRenderGeometry(DrawContext dc, String drawStyle)
	    {
	    	if (DEBUG) System.out.println("AbstractAirspace.doRenderGeometry");
	    	if (dc == null)
	        {
	            String message = Logging.getMessage("nullValue.DrawContextIsNull");
	            Logging.logger().severe(message);
	            throw new IllegalArgumentException(message);
	        }

	        //if (this.isCurtainsOutOfDate())
//FIXME	            this.doUpdateCurtains(dc);
	       
	        for (Curtain c : this.curtains)
	            if (c.isVisible()==Boolean.TRUE) {
	            	if (DEBUG) System.out.println("doRenderGeometry :curtain "+c.isVisible());
	            	c.renderGeometry(dc, drawStyle);
	            	//c.render(dc);
	            	
	            }
	       }

	    protected void doRenderExtent(DrawContext dc)
	    {
	    	if (DEBUG) System.out.println("AbstractAirspace.doRenderContext");
	    	if (dc == null)
	        {
	            String message = Logging.getMessage("nullValue.DrawContextIsNull");
	            Logging.logger().severe(message);
	            throw new IllegalArgumentException(message);
	        }
	       // super.doRenderExtent(dc);
	    	if (DEBUG) System.out.println("doRenderExtent");
	        for (Curtain c : this.curtains) {  
	        	if (c.isAirspaceVisible(dc)==Boolean.TRUE) c.renderExtent(dc); 
	        }
	    }

	    //**************************************************************//
	    //********************  END Geometry Rendering  ****************//
	    //**************************************************************//
	    
}