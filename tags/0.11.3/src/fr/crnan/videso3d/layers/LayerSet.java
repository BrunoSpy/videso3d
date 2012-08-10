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

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Logging;
import java.util.Iterator;
import java.util.logging.Level;

/**
 * LayerSet allows create tree of layers. 
 * LayerSet extends the LayerList class and implements the Layer interface,
 * thus you can create composite layers.
 * 
 * You can add Layers, LayersList or LayerSet elements. Take into account, if 
 * you add a LayerList it is internally stored as a LayerSet with all elements
 * of the LayerList.
 * 
 * The methods render, pick, setOpacity, setEnable and setPickEnabled are 
 * invoked on all contained layers.
 * 
 * @author Antonio Santiago [asantiagop(at)gmail.com]
 * @author Bruno Spyckerelle
 * @version 0.3.1
 */
@SuppressWarnings("serial")
public class LayerSet extends LayerList implements Layer {
	
    private boolean enabled = true;
    private boolean pickable = true;
    private double opacity = 1d;
    private double minActiveAltitude = -Double.MAX_VALUE;
    private double maxActiveAltitude = Double.MAX_VALUE;
    private boolean networkDownloadEnabled = true;
    private long expiryTime = 0;
    
    public boolean add(LayerList list) {
        if (list == null) {
            String message = Logging.getMessage("nullValue.LayerIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        LayerSet temp = new LayerSet();
        for (Layer layer : list) {
            temp.add(layer);
        }

        super.add(temp);
        temp.addPropertyChangeListener(this);
        this.firePropertyChange(AVKey.LAYERS, null, this);

        return true;
    }

    public boolean add(LayerSet set) {
        if (set == null) {
            String message = Logging.getMessage("nullValue.LayerIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        super.add(set);
        set.addPropertyChangeListener(this);
        this.firePropertyChange(AVKey.LAYERS, null, this);

        return true;
    }


    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public boolean isPickEnabled() {
        return pickable;
    }

    @Override
    public void setPickEnabled(boolean pickable) {
        if (this.pickable == pickable) {
            return;
        }

        this.pickable = pickable;

        for (Layer layer : this) {
            try {
                if (layer != null) {
                    layer.setPickEnabled(pickable);
                }
            }
            catch (Exception e) {
                String message = Logging.getMessage("nullValue.LayerIsNull");
                Logging.logger().log(Level.SEVERE, message, e);
            // Don't abort; continue on to the next layer.
            }
        }
    }

    @Override
    public void setEnabled(boolean enabled) {

        if (this.enabled == enabled) {
            return;
        }

        this.enabled = enabled;

        for (Layer layer : this) {
            try {
                if (layer != null) {
                    layer.setEnabled(enabled);
                }
            }
            catch (Exception e) {
                String message = Logging.getMessage("nullValue.LayerIsNull");
                Logging.logger().log(Level.SEVERE, message, e);
            // Don't abort; continue on to the next layer.
            }
        }
    }

    @Override
    public String getName() {
        Object n = this.getValue(AVKey.DISPLAY_NAME);

        return n != null ? n.toString() : this.toString();
    }

    @Override
    public void setName(String name) {
        this.setValue(AVKey.DISPLAY_NAME, name);
    }

    @Override
    public String toString() {
        Object n = this.getValue(AVKey.DISPLAY_NAME);

        return n != null ? n.toString() : super.toString();
    }

    @Override
    public double getOpacity() {
        return opacity;
    }

    @Override
    public void setOpacity(double opacity) {

        if (this.opacity == opacity) {
            return;
        }

        this.opacity = opacity;

        for (Layer layer : this) {
            try {
                if (layer != null) {
                    layer.setOpacity(opacity);
                }
            }
            catch (Exception e) {
                String message = Logging.getMessage("nullValue.LayerIsNull");
                Logging.logger().log(Level.SEVERE, message, e);
            // Don't abort; continue on to the next layer.
            }
        }
    }

    public double getMinActiveAltitude() {
        return minActiveAltitude;
    }

    public void setMinActiveAltitude(double minActiveAltitude) {
        this.minActiveAltitude = minActiveAltitude;
    }

    public double getMaxActiveAltitude() {
        return maxActiveAltitude;
    }

    public void setMaxActiveAltitude(double maxActiveAltitude) {
        this.maxActiveAltitude = maxActiveAltitude;
    }

    /**
     * Indicates whether the layer is in the view. The method implemented here is a default indicating the layer is in
     * view. Subclasses able to determine their presence in the view should override this implementation.
     *
     * @param dc the current draw context
     * @return <code>true</code> if the layer is in the view, <code>false</code> otherwise.
     */
    public boolean isLayerInView(DrawContext dc) {
        if (dc == null) {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        return true;
    }

    /**
     * Indicates whether the layer is active based on arbitrary criteria. The method implemented here is a default
     * indicating the layer is active if the current altitude is within the layer's min and max active altitudes.
     * Subclasses able to consider more criteria should override this implementation.
     *
     * @param dc the current draw context
     * @return <code>true</code> if the layer is active, <code>false</code> otherwise.
     */
    public boolean isLayerActive(DrawContext dc) {
        if (dc == null) {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        if (null == dc.getView()) {
            String message = Logging.getMessage("layers.LayerList.NoViewSpecifiedInDrawingContext");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        Position eyePos = dc.getView().getEyePosition();
        if (eyePos == null) {
            return false;
        }
        double altitude = eyePos.getElevation();
        return altitude >= this.minActiveAltitude && altitude <= this.maxActiveAltitude;
    }

    /**
     * @param dc the current draw context
     * @throws IllegalArgumentException if <code>dc</code> is null, or <code>dc</code>'s <code>Globe</code> or
     *                                  <code>View</code> is null
     */
    @Override
    public void render(DrawContext dc) {
        if (!this.enabled) {
            return; // Don't check for arg errors if we're disabled
        }
        if (null == dc) {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        if (null == dc.getGlobe()) {
            String message = Logging.getMessage("layers.LayerList.NoGlobeSpecifiedInDrawingContext");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        if (null == dc.getView()) {
            String message = Logging.getMessage("layers.LayerList.NoViewSpecifiedInDrawingContext");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        if (!this.isLayerActive(dc)) {
            return;
        }
        if (!this.isLayerInView(dc)) {
            return;
        }
        this.doRender(dc);
    }

    /**
     * Render all container layers.
     */
    protected void doRender(DrawContext dc) {
        for (Layer layer : this) {
            try {
                if (layer != null) {
                    layer.render(dc);
                }
            }
            catch (Exception e) {
                String message = Logging.getMessage("nullValue.LayerIsNull");
                Logging.logger().log(Level.SEVERE, message, e);
            // Don't abort; continue on to the next layer.
            }
        }
    }

    public void pick(DrawContext dc, java.awt.Point point) {
        if (!this.enabled) {
            return; // Don't check for arg errors if we're disabled
        }
        if (null == dc) {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        if (null == dc.getGlobe()) {
            String message = Logging.getMessage("layers.LayerList.NoGlobeSpecifiedInDrawingContext");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        if (null == dc.getView()) {
            String message = Logging.getMessage("layers.LayerList.NoViewSpecifiedInDrawingContext");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        if (!this.isLayerActive(dc)) {
            return;
        }
        if (!this.isLayerInView(dc)) {
            return;
        }
        this.doPick(dc, point);
    }

    protected void doPick(DrawContext dc, java.awt.Point point) {
        for (Layer layer : this) {
            try {
                if (layer != null) {
                    layer.pick(dc, point);
                }
            }
            catch (Exception e) {
                String message = Logging.getMessage("nullValue.LayerIsNull");
                Logging.logger().log(Level.SEVERE, message, e);
            // Don't abort; continue on to the next layer.
            }
        }
    }

    public void dispose() {
        for (Layer layer : this) {
            try {
                if (layer != null) {
                    layer.dispose();
                }
            }
            catch (Exception e) {
                String message = Logging.getMessage("nullValue.LayerIsNull");
                Logging.logger().log(Level.SEVERE, message, e);
            // Don't abort; continue on to the next layer.
            }
        }
    }

    /**
     * Checks of this LayerSet or some of its contained Layers contains the
     * given object.
     * 
     * @param o
     * @return True if the layer contains the given object
     */
    @Override
    public boolean contains(Object o) {

        if (super.contains(o)) {
            return true;
        }

        for (Iterator<Layer> it = this.iterator(); it.hasNext();) {
            Layer layer = it.next();
            if (layer instanceof LayerList) {
                if (((LayerList) layer).contains(o)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Checks of this LayerSet or some of its contained Layers contains and 
     * object of the given class.
     * 
     * @param clazz
     * @return True if the layer contains an object of the given class
     */
    public boolean containsClass(Class<?> clazz) {

        for (Iterator<Layer> it = this.iterator(); it.hasNext();) {
            Layer object = it.next();

            if (object.getClass().equals(clazz)) {
                return true;
            }

            if (object instanceof LayerSet) {
                if (((LayerSet) object).containsClass(clazz)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Returns the index of the first occurrence of an element with the specified 
     * class in this list (only search in the current layer not in its contained
     * layers), or -1 if this list does not contain an element of 
     * that type.
     * 
     * @param clazz element's class to search for
     */
    public int indexOf(Class<?> clazz) {

        int index = -1;
        for (Iterator<Layer> it = this.iterator(); it.hasNext();) {
            Layer object = it.next();
            index++;

            if (object.getClass().equals(clazz)) {
                return index;
            }
        }

        return -1;
    }

	@Override
	public long getExpiryTime() {
        return this.expiryTime;
	}

	@Override
	public double getScale() {
		 Object o = this.getValue(AVKey.MAP_SCALE);
		 return o != null && o instanceof Double ? (Double) o : 1;
	}

	@Override
	public boolean isAtMaxResolution() {
        return !this.isMultiResolution();
	}

	@Override
	public boolean isMultiResolution() {
		return false;
	}

	@Override
	public boolean isNetworkRetrievalEnabled() {
		return networkDownloadEnabled;
	}

	@Override
	public void preRender(DrawContext dc) {
		if (!this.enabled)
            return; // Don't check for arg errors if we're disabled

        if (null == dc)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        if (null == dc.getGlobe())
        {
            String message = Logging.getMessage("layers.AbstractLayer.NoGlobeSpecifiedInDrawingContext");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        if (null == dc.getView())
        {
            String message = Logging.getMessage("layers.AbstractLayer.NoViewSpecifiedInDrawingContext");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        if (!this.isLayerActive(dc))
            return;

        if (!this.isLayerInView(dc))
            return;

        this.doPreRender(dc);
	}

	private void doPreRender(DrawContext dc) {
		 for (Layer layer : this) {
	            try {
	                if (layer != null) {
	                    layer.preRender(dc);
	                }
	            }
	            catch (Exception e) {
	                String message = Logging.getMessage("nullValue.LayerIsNull");
	                Logging.logger().log(Level.SEVERE, message, e);
	            // Don't abort; continue on to the next layer.
	            }
	        }
	}

	@Override
	public void setExpiryTime(long expiryTime) {
        this.expiryTime = expiryTime;		
	}

	@Override
	public void setNetworkRetrievalEnabled(boolean networkRetrievalEnabled) {
        this.networkDownloadEnabled = networkRetrievalEnabled;		
	}

	@Override
	public String getRestorableState() {
		return null;
	}

	@Override
	public void restoreState(String stateInXml) {
		String message = Logging.getMessage("RestorableSupport.RestoreNotSupported");
        Logging.logger().severe(message);
        throw new UnsupportedOperationException(message);
	}

	@Override
	public Double getMaxEffectiveAltitude(Double radius) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double getMinEffectiveAltitude(Double radius) {
		// TODO Auto-generated method stub
		return null;
	}
}