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

package fr.crnan.videso3d.layers;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.airspaces.Airspace;
import gov.nasa.worldwind.render.airspaces.AirspaceRenderer;
import gov.nasa.worldwind.util.Logging;

/**
 * Airspace layer whose elements are filterable depending on max and min altitudes.
 * @author Bruno Spyckerelle
 * @version 0.2.1
 */
public class FilterableAirspaceLayer extends AbstractLayer implements AltitudeFilterableLayer{

	private double maxAltitude = 800.0*30.47;
	
	private double minAltitude = 0.0;
	
	private LinkedList<Airspace> full = new LinkedList<Airspace>();
	
	private Set<Airspace> displayed = null;
	
    private AirspaceRenderer airspaceRenderer = new AirspaceRenderer();
    
    public FilterableAirspaceLayer(){
    	super();
    	this.setEnableBatchPicking(false); //required to enable deep picking
    }
    
	public void addAirspace(Airspace airspace) {
		if (airspace == null)
        {
            String msg = "nullValue.AirspaceIsNull";
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
		this.full.add(airspace);
		this.displayed = null;
		this.setMaximumViewableAltitude(maxAltitude);
		this.setMinimumViewableAltitude(minAltitude);
	}

	public void addAirspaces(Iterable<Airspace> airspaces) {
		if (airspaces == null)
        {
            String msg = "nullValue.AirspaceIsNull";
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
		for(Airspace a : airspaces){
			this.addAirspace(a);
		}
	}

	public Iterable<Airspace> getAirspaces() {
		if(displayed == null){
			return this.full;
		} else {
			return this.displayed;
		}
	}


	public void setAirspaces(Iterable<Airspace> airspaceIterable) {
		if (airspaceIterable == null)
        {
            String msg = "nullValue.AirspaceIsNull";
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
		this.full.clear();
		Iterator<Airspace> iterator = airspaceIterable.iterator();
		while(iterator.hasNext()){
			this.full.add(iterator.next());
		}
		this.displayed = null;
		this.setMaximumViewableAltitude(maxAltitude);
		this.setMinimumViewableAltitude(minAltitude);
	}

	@Override
	public void setMaximumViewableAltitude(double altitude) {
		LinkedList<Airspace> airspaces = new LinkedList<Airspace>();
		Iterator<Airspace> iterator;
		if(altitude < maxAltitude){
			//reduce the set of visible airspaces
			iterator = this.getAirspaces().iterator();
		} else {
			iterator = full.iterator();
		}
		while(iterator.hasNext()){
			Airspace next = iterator.next();
			if(next.getAltitudes()[0] < altitude && next.getAltitudes()[1] > minAltitude){
				airspaces.add(next);
			}
		}
		this.displayed = new HashSet<Airspace>(airspaces);
		this.maxAltitude = altitude;
		this.firePropertyChange(AVKey.LAYER, null, true);
	}

	@Override
	public void setMinimumViewableAltitude(double altitude) {
		if(altitude == minAltitude)
			return;
		LinkedList<Airspace> airspaces = new LinkedList<Airspace>();
		Iterator<Airspace> iterator;
		if(altitude>minAltitude){
			//reduce the set of visible airspaces
			iterator = this.getAirspaces().iterator();
		} else {
			iterator = full.iterator();
		}
		while(iterator.hasNext()){
			Airspace next = iterator.next();
			if(next.getAltitudes()[0] < maxAltitude && next.getAltitudes()[1] > altitude){
				airspaces.add(next);
			}
		}
		this.displayed = new HashSet<Airspace>(airspaces);
		this.minAltitude = altitude;
		this.firePropertyChange(AVKey.LAYER, null, true);
	}
	


    /**
     * Removes the specified <code>airspace</code> from this layer's internal collection, if it exists. If this layer's
     * internal collection has been overriden with a call to {@link #setAirspaces}, this will throw an exception.
     *
     * @param airspace Airspace to remove.
     *
     * @throws IllegalArgumentException If <code>airspace</code> is null.
     * @throws IllegalStateException    If a custom Iterable has been specified by a call to <code>setAirspaces</code>.
     */
    public void removeAirspace(Airspace airspace)
    {
        if (airspace == null)
        {
            String msg = "nullValue.AirspaceIsNull";
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.full.remove(airspace);
        if(this.displayed != null && this.displayed.contains(airspace)){
        	this.displayed.remove(airspace);
        }
    }

    /**
     * Clears the contents of this layer's internal Airspace collection. If this layer's internal collection has been
     * overriden with a call to {@link #setAirspaces}, this will throw an exception.
     *
     * @throws IllegalStateException If a custom Iterable has been specified by a call to <code>setAirspaces</code>.
     */
    public void removeAllAirspaces(){
        clearAirspaces();
    }

    private void clearAirspaces(){
        if (this.full != null && this.full.size() > 0) {
        	this.full.clear();
        	if(displayed != null) displayed = null;
        }
    }

    public boolean isEnableAntialiasing()
    {
        return this.airspaceRenderer.isEnableAntialiasing();
    }

    public void setEnableAntialiasing(boolean enable)
    {
        this.airspaceRenderer.setEnableAntialiasing(enable);
    }

    public boolean isEnableBlending()
    {
        return this.airspaceRenderer.isEnableBlending();
    }

    public void setEnableBlending(boolean enable)
    {
        this.airspaceRenderer.setEnableBlending(enable);
    }

    public boolean isEnableDepthOffset()
    {
        return this.airspaceRenderer.isEnableDepthOffset();
    }

    public void setEnableDepthOffset(boolean enable)
    {
        this.airspaceRenderer.setEnableDepthOffset(enable);
    }

    public boolean isEnableLighting()
    {
        return this.airspaceRenderer.isEnableLighting();
    }

    public void setEnableLighting(boolean enable)
    {
        this.airspaceRenderer.setEnableLighting(enable);
    }

    public boolean isDrawExtents()
    {
        return this.airspaceRenderer.isDrawExtents();
    }

    public void setDrawExtents(boolean draw)
    {
        this.airspaceRenderer.setDrawExtents(draw);
    }

    public boolean isDrawWireframe()
    {
        return this.airspaceRenderer.isDrawWireframe();
    }

    public void setDrawWireframe(boolean draw)
    {
        this.airspaceRenderer.setDrawWireframe(draw);
    }

    public Double getDepthOffsetFactor()
    {
        return this.airspaceRenderer.getDepthOffsetFactor();
    }

    public void setDepthOffsetFactor(Double factor)
    {
        this.airspaceRenderer.setDepthOffsetFactor(factor);
    }

    public Double getDepthOffsetUnits()
    {
        return this.airspaceRenderer.getDepthOffsetUnits();
    }

    public void setDepthOffsetUnits(Double units)
    {
        this.airspaceRenderer.setDepthOffsetUnits(units);
    }

    protected AirspaceRenderer getRenderer()
    {
        return this.airspaceRenderer;
    }

    public boolean isEnableBatchRendering()
    {
        return this.getRenderer().isEnableBatchRendering();
    }

    public void setEnableBatchRendering(boolean enableBatchRendering)
    {
        this.getRenderer().setEnableBatchRendering(enableBatchRendering);
    }

    public boolean isEnableBatchPicking()
    {
        return this.getRenderer().isEnableBatchPicking();
    }

    public void setEnableBatchPicking(boolean enableBatchPicking)
    {
        this.getRenderer().setEnableBatchPicking(enableBatchPicking);
    }

    @Override
    protected void doPick(DrawContext dc, java.awt.Point pickPoint)
    {
        this.airspaceRenderer.renderOrdered(dc, getAirspaces()); // Picking handled during ordered rendering.
    }

    @Override
    protected void doRender(DrawContext dc)
    {
        this.airspaceRenderer.renderOrdered(dc, getAirspaces());
    }

    @Override
    public String toString()
    {
        return Logging.getMessage("layers.AirspaceLayer.Name");
    }

}
