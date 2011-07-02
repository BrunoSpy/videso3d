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

import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import fr.crnan.videso3d.DraggerListener;
import fr.crnan.videso3d.VidesoGLCanvas;
import gov.nasa.worldwind.Movable;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.event.Message;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;
/**
 * Vertical scalebar in FL, movable
 * @author Bruno Spyckerelle
 * @version 0.2.1
 */
public class VerticalScaleBar extends DraggerListener implements Layer {

	private PointPlacemark top;

	private List<PointPlacemark> hundreds = new LinkedList<PointPlacemark>();

	private List<PointPlacemark> tens = new LinkedList<PointPlacemark>();

	private final WorldWindow wwd;

	private RenderableLayer layer = new RenderableLayer();

	private boolean detailed = false;

	public VerticalScaleBar(VidesoGLCanvas wd){
		super(wd);
		this.wwd = wd;


		top = new PointPlacemark(new Position(LatLon.ZERO, 600*30.48));
		top.setAltitudeMode(WorldWind.ABSOLUTE);
		top.setApplyVerticalExaggeration(true);
		top.setLabelText("FL 600");
		top.setLineEnabled(true);
		top.setLinePickWidth(200);
		top.setEnableBatchPicking(false);
		PointPlacemarkAttributes topAttr = new PointPlacemarkAttributes();
		topAttr.setLineWidth(2d);
		topAttr.setLineMaterial(Material.WHITE);
		topAttr.setUsePointAsDefaultImage(true);
		topAttr.setLabelScale(1.0);
		top.setAttributes(topAttr);
		this.addRenderable(top);

		PointPlacemarkAttributes hundredsAttributes = new PointPlacemarkAttributes();
		hundredsAttributes.setUsePointAsDefaultImage(true);
		hundredsAttributes.setLabelScale(0.8);
		for(int i = 1;i<6;i++){
			PointPlacemark p = new PointPlacemark(new Position(LatLon.ZERO, 100*i*30.48));
			p.setLabelText("FL "+i*100);
			p.setAltitudeMode(WorldWind.ABSOLUTE);
			p.setApplyVerticalExaggeration(true);
			p.setLineEnabled(false);
			p.setLinePickWidth(200);
			top.setEnableBatchPicking(false);
			p.setAttributes(hundredsAttributes);
			hundreds.add(p);
			this.addRenderable(p);
		}

		PointPlacemarkAttributes tensAttributes = new PointPlacemarkAttributes();
		tensAttributes.setUsePointAsDefaultImage(true);
		tensAttributes.setLabelScale(0.7);
		for(int i = 1; i < 60;i++){
			if( i % 10 != 0){
				PointPlacemark p = new PointPlacemark(new Position(LatLon.ZERO, 10*i*30.48));
				p.setLabelText(""+i*10);
				p.setAltitudeMode(WorldWind.ABSOLUTE);
				p.setApplyVerticalExaggeration(true);
				p.setLineEnabled(false);
				p.setLinePickWidth(200);
				top.setEnableBatchPicking(false);
				p.setAttributes(tensAttributes);
				tens.add(p);
			}
		}
		
		//changement de l'affichage en fonction du zoom
		this.wwd.getSceneController().addPropertyChangeListener(AVKey.VERTICAL_EXAGGERATION, new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent arg0) {

				if(wwd.getSceneController().getVerticalExaggeration() >= 4.0){
					if(!detailed){
						addRenderables(tens);
						detailed = true;
					}

				} else {
					for(PointPlacemark p : tens){
						removeRenderable(p);
					}
					detailed = false;
				}
			}


		});
		wwd.getSceneController().firePropertyChange(AVKey.VERTICAL_EXAGGERATION, null, null);
	}



	private void movePointPlacemarks(LatLon latlon){
		top.setPosition(new Position(latlon, top.getPosition().getElevation()));
		for(PointPlacemark p : hundreds){
			p.setPosition(new Position(latlon, p.getPosition().getElevation()));
		}
		for(PointPlacemark p : tens){
			p.setPosition(new Position(latlon, p.getPosition().getElevation()));
		}
	}

	@Override
	public Boolean isDraggable(Object o){
		return o.equals(top) || hundreds.contains(o) || tens.contains(o);
	}

	public void initializePosition(Position position){
		this.movePointPlacemarks(position);
		this.firePropertyChange(AVKey.LAYER, null, true);
	}

	@Override
	protected void doMove(Position pos, Movable o){
		if(isDraggable(o))
			this.movePointPlacemarks(pos);
	}
	
	@Override
	public Object setValue(String key, Object value) {
		return this.layer.setValue(key, value);
	}

	@Override
	public AVList setValues(AVList avList) {
		return this.layer.setValues(avList);
	}

	@Override
	public Object getValue(String key) {
		return this.layer.getValue(key);
	}

	@Override
	public Collection<Object> getValues() {
		return this.layer.getValues();
	}

	@Override
	public String getStringValue(String key) {
		return this.layer.getStringValue(key);
	}

	@Override
	public Set<Entry<String, Object>> getEntries() {
		return this.layer.getEntries();
	}

	@Override
	public boolean hasKey(String key) {
		return this.layer.hasKey(key);
	}

	@Override
	public Object removeKey(String key) {
		return this.layer.removeKey(key);
	}

	@Override
	public void addPropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		this.layer.addPropertyChangeListener(propertyName, listener);
	}

	@Override
	public void removePropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		this.layer.removePropertyChangeListener(propertyName, listener);
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		this.layer.addPropertyChangeListener(listener);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		this.layer.removePropertyChangeListener(listener);
	}

	@Override
	public void firePropertyChange(String propertyName, Object oldValue,
			Object newValue) {
		this.layer.firePropertyChange(propertyName, oldValue, newValue);
	}

	@Override
	public void firePropertyChange(PropertyChangeEvent propertyChangeEvent) {
		this.layer.firePropertyChange(propertyChangeEvent);
	}

	@Override
	public AVList copy() {
		return this.layer.copy();
	}

	@Override
	public AVList clearList() {
		return this.layer.clearList();
	}

	@Override
	public void propertyChange(PropertyChangeEvent p) {
		this.layer.propertyChange(p);
	}

	@Override
	public void dispose() {
		this.layer.dispose();
	}

	@Override
	public String getRestorableState() {
		return this.layer.getRestorableState();
	}

	@Override
	public void restoreState(String stateInXml) {
		this.layer.restoreState(stateInXml);
	}

	@Override
	public boolean isEnabled() {
		return this.layer.isEnabled();
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.layer.setEnabled(enabled);
	}

	@Override
	public String getName() {
		return this.layer.getName();
	}

	@Override
	public void setName(String name) {
		this.layer.setName(name);
	}

	@Override
	public double getOpacity() {
		return this.layer.getOpacity();
	}

	@Override
	public void setOpacity(double opacity) {
		this.layer.setOpacity(opacity);
	}

	@Override
	public boolean isPickEnabled() {
		return this.layer.isPickEnabled();
	}

	@Override
	public void setPickEnabled(boolean isPickable) {
		this.layer.setPickEnabled(isPickable);
	}

	@Override
	public void preRender(DrawContext dc) {
		this.layer.preRender(dc);
	}

	@Override
	public void render(DrawContext dc) {
		this.layer.render(dc);
	}

	@Override
	public void pick(DrawContext dc, Point pickPoint) {
		this.layer.pick(dc, pickPoint);
	}

	@Override
	public boolean isAtMaxResolution() {
		return this.layer.isAtMaxResolution();
	}

	@Override
	public boolean isMultiResolution() {
		return this.layer.isMultiResolution();
	}

	@Override
	public double getScale() {
		return this.layer.getScale();
	}

	@Override
	public boolean isNetworkRetrievalEnabled() {
		return this.layer.isNetworkRetrievalEnabled();
	}

	@Override
	public void setNetworkRetrievalEnabled(boolean networkRetrievalEnabled) {
		this.layer.setNetworkRetrievalEnabled(networkRetrievalEnabled);
	}

	@Override
	public void setExpiryTime(long expiryTime) {
		this.layer.setExpiryTime(expiryTime);
	}

	@Override
	public long getExpiryTime() {
		return this.layer.getExpiryTime();
	}

	@Override
	public double getMinActiveAltitude() {
		return this.getMinActiveAltitude();
	}

	@Override
	public void setMinActiveAltitude(double minActiveAltitude) {
		this.layer.setMinActiveAltitude(minActiveAltitude);
	}

	@Override
	public double getMaxActiveAltitude() {
		return this.layer.getMaxActiveAltitude();
	}

	@Override
	public void setMaxActiveAltitude(double maxActiveAltitude) {
		this.layer.setMaxActiveAltitude(maxActiveAltitude);
	}

	@Override
	public boolean isLayerInView(DrawContext dc) {
		return this.layer.isLayerInView(dc);
	}

	@Override
	public boolean isLayerActive(DrawContext dc) {
		return this.layer.isLayerActive(dc);
	}

	@Override
	public Double getMaxEffectiveAltitude(Double radius) {
		return this.layer.getMaxEffectiveAltitude(radius);
	}

	@Override
	public Double getMinEffectiveAltitude(Double radius) {
		return this.layer.getMinEffectiveAltitude(radius);
	}
	
	@Override
	public void onMessage(Message msg) {
		this.layer.onMessage(msg);
	}

	private void addRenderables(List<PointPlacemark> tens) {
		this.layer.addRenderables(tens);
	}

	private void addRenderable(PointPlacemark renderable) {
		this.layer.addRenderable(renderable);
	}

	private void removeRenderable(PointPlacemark renderable) {
		this.layer.removeRenderable(renderable);
	}




}
