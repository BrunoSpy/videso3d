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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;

import fr.crnan.videso3d.Pallet;
import fr.crnan.videso3d.geom.LatLonCautra;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.geom.Extent;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.airspaces.Airspace;
import gov.nasa.worldwind.render.airspaces.AirspaceAttributes;
import gov.nasa.worldwind.render.airspaces.AirspaceRenderer;
import gov.nasa.worldwind.render.airspaces.BasicAirspaceAttributes;
import gov.nasa.worldwind.render.airspaces.CappedCylinder;
import gov.nasa.worldwind.render.airspaces.DetailLevel;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.RestorableSupport;
/**
 * Représentation simple d'un stack par un cylindre et son volume de protection.
 * @author Bruno Spyckerelle
 * @version 0.1.1
 */
public class SimpleStack3D implements Airspace, VidesoObject {

	private CappedCylinder stack;
	
	private CappedCylinder protec;
	
	private String name;
	
	private VidesoAnnotation annotation;

	private boolean highlighted = false;

	private AirspaceAttributes normalAttrs ;

	private AirspaceAttributes highlightAttrs;

	public SimpleStack3D(){
		this.stack = new CappedCylinder();
		this.protec = new CappedCylinder();
	}
	
	/**
	 * @param name Nom du stack
	 * @param center Centre du cylindre
	 * @param rayonInt Rayon du stack en NM
	 * @param rayonExt Rayon du volume de protection en NM
	 * @param flInf Niveau infèrieur
	 * @param flSup Niveau supèrieur
	 */
	public SimpleStack3D(String name, LatLon center, double rayonInt, double rayonExt, int flInf, int flSup){
		this.stack = new CappedCylinder(center, rayonInt*LatLonCautra.NM);
		this.stack.setAltitudes(flInf*30.48, flSup*30.48);
		this.protec = new CappedCylinder(center, rayonExt*LatLonCautra.NM);
		this.protec.setAltitudes(flInf*30.48, flSup*30.48);
		
		this.setName(name);
	}

	@Override
	public void render(DrawContext dc) {
		this.stack.render(dc);
		this.protec.render(dc);
	}

	@Override
	public String getRestorableState() {
		RestorableSupport rs = RestorableSupport.newRestorableSupport();
        this.doGetRestorableState(rs, null);

        return rs.getStateAsXml();
	}

	protected void doGetRestorableState(RestorableSupport rs, RestorableSupport.StateObject context){
		// Method is invoked by subclasses to have superclass add its state and only its state
		this.doMyGetRestorableState(rs, context);
	}

	private void doMyGetRestorableState(RestorableSupport rs, RestorableSupport.StateObject context){
		
		rs.addStateValueAsString(context, "stack", this.stack.getRestorableState());
		rs.addStateValueAsString(context, "protec", this.protec.getRestorableState());
		
		this.getHighlightAttributes().getRestorableState(rs, rs.addStateObject(context, "highlightattributes"));
		
		 rs.addStateValueAsString(context, "annotation", this.getAnnotation(Position.ZERO).getText());
		 
		 if(this.getName() != null)
			 rs.addStateValueAsString(context, "name", this.getName());
	}

	@Override
	public void restoreState(String stateInXml) {
		 if (stateInXml == null)
	        {
	            String message = Logging.getMessage("nullValue.StringIsNull");
	            Logging.logger().severe(message);
	            throw new IllegalArgumentException(message);
	        }

	        RestorableSupport rs;
	        try
	        {
	            rs = RestorableSupport.parse(stateInXml);
	        }
	        catch (Exception e)
	        {
	            // Parsing the document specified by stateInXml failed.
	            String message = Logging.getMessage("generic.ExceptionAttemptingToParseStateXml", stateInXml);
	            Logging.logger().severe(message);
	            throw new IllegalArgumentException(message, e);
	        }

	        this.doRestoreState(rs, null);		
	}

	protected void doRestoreState(RestorableSupport rs, RestorableSupport.StateObject context)   {
		// Method is invoked by subclasses to have superclass add its state and only its state
		this.doMyRestoreState(rs, context);
	}

	private void doMyRestoreState(RestorableSupport rs, RestorableSupport.StateObject context){
		String s= rs.getStateValueAsString(context, "stack");
		if(s != null )
			this.stack.restoreState(s);

		s = rs.getStateValueAsString(context, "protec");
		if(s != null )
			this.protec.restoreState(s);

		RestorableSupport.StateObject soh = rs.getStateObject(context, "highlightattributes");
		if (soh != null)
			this.getHighlightAttributes().restoreState(rs, soh);

		s = rs.getStateValueAsString(context, "name");
		if(s != null)
			this.setName(s);

		s = rs.getStateValueAsString(context, "annotation");
		if(s != null)
			this.setAnnotation(s);



	}

	@Override
	public Object setValue(String key, Object value) {
		return this.stack.setValue(key, value);
	}

	@Override
	public AVList setValues(AVList avList) {
		return this.stack.setValues(avList);
	}

	@Override
	public Object getValue(String key) {
		return this.stack.getValue(key);
	}

	@Override
	public Collection<Object> getValues() {
		return this.stack.getValues();
	}

	@Override
	public String getStringValue(String key) {
		return this.stack.getStringValue(key);
	}

	@Override
	public Set<Entry<String, Object>> getEntries() {
		return this.stack.getEntries();
	}

	@Override
	public boolean hasKey(String key) {
		return this.stack.hasKey(key);
	}

	@Override
	public Object removeKey(String key) {
		return this.stack.removeKey(key);
	}

	@Override
	public void addPropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		this.stack.addPropertyChangeListener(propertyName, listener);
	}

	@Override
	public void removePropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		this.stack.removePropertyChangeListener(propertyName, listener);
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		this.stack.addPropertyChangeListener(listener);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		this.stack.removePropertyChangeListener(listener);
	}

	@Override
	public void firePropertyChange(String propertyName, Object oldValue,
			Object newValue) {
		this.stack.firePropertyChange(propertyName, oldValue, newValue);
	}

	@Override
	public void firePropertyChange(PropertyChangeEvent propertyChangeEvent) {
		this.stack.firePropertyChange(propertyChangeEvent);
	}

	@Override
	public AVList copy() {
		return this.stack.copy();
	}

	@Override
	public AVList clearList() {
		return this.stack.clearList();
	}

	@Override
	public boolean isVisible() {
		return this.stack.isVisible();
	}

	@Override
	public void setVisible(boolean visible) {
		this.stack.setVisible(visible);
		this.protec.setVisible(visible);
	}

	@Override
	public AirspaceAttributes getAttributes() {
		return this.stack.getAttributes();
	}

	@Override
	public void setAttributes(AirspaceAttributes attributes) {
		this.stack.setAttributes(attributes);
		AirspaceAttributes protecAttrs = new BasicAirspaceAttributes(attributes);
		protecAttrs.setMaterial(new Material(Pallet.makeBrighter(attributes.getMaterial().getDiffuse())));
		this.protec.setAttributes(protecAttrs);
	}

	@Override
	public double[] getAltitudes() {
		return this.stack.getAltitudes();
	}

	@Override
	public void setAltitudes(double lowerAltitude, double upperAltitude) {
		this.stack.setAltitudes(lowerAltitude, upperAltitude);
		this.protec.setAltitudes(lowerAltitude, upperAltitude);
	}

	@Override
	public void setAltitude(double altitude) {
		this.stack.setAltitude(altitude);
		this.protec.setAltitude(altitude);
	}

	@Override
	public boolean[] isTerrainConforming() {
		return this.stack.isTerrainConforming();
	}

	@Override
	public void setTerrainConforming(boolean lowerTerrainConformant,
			boolean upperTerrainConformant) {
		this.stack.setTerrainConforming(lowerTerrainConformant, upperTerrainConformant);
		this.protec.setTerrainConforming(lowerTerrainConformant, upperTerrainConformant);
	}

	@Override
	public void setTerrainConforming(boolean terrainConformant) {
		this.stack.setTerrainConforming(terrainConformant);
		this.protec.setTerrainConforming(terrainConformant);
	}

	@Override
	public boolean isEnableLevelOfDetail() {
		return this.stack.isEnableLevelOfDetail();
	}

	@Override
	public void setEnableLevelOfDetail(boolean enableLevelOfDetail) {
		this.stack.setEnableLevelOfDetail(enableLevelOfDetail);
		this.protec.setEnableLevelOfDetail(enableLevelOfDetail);
	}

	@Override
	public Iterable<DetailLevel> getDetailLevels() {
		return this.stack.getDetailLevels();
	}

	@Override
	public void setDetailLevels(Collection<DetailLevel> detailLevels) {
		this.stack.setDetailLevels(detailLevels);
		this.protec.setDetailLevels(detailLevels);
	}

	@Override
	public boolean isAirspaceVisible(DrawContext dc) {
		return this.protec.isAirspaceVisible(dc);
	}

	@Override
	public Extent getExtent(Globe globe, double verticalExaggeration) {
		return this.protec.getExtent(globe, verticalExaggeration);
	}

	@Override
	public Extent getExtent(DrawContext dc) {
		return this.protec.getExtent(dc);
	}

	@Override
	public void makeOrderedRenderable(DrawContext dc, AirspaceRenderer renderer) {
		this.stack.makeOrderedRenderable(dc, renderer);
		this.protec.makeOrderedRenderable(dc, renderer);
	}

	@Override
	public void renderGeometry(DrawContext dc, String drawStyle) {
		this.stack.renderGeometry(dc, drawStyle);
		this.protec.renderGeometry(dc, drawStyle);
	}

	@Override
	public void renderExtent(DrawContext dc) {
		this.stack.renderExtent(dc);
		this.protec.renderExtent(dc);
	}

	@Override
	public void setAltitudeDatum(String lowerAltitudeDatum,
			String upperAltitudeDatum) {
		this.stack.setAltitudeDatum(lowerAltitudeDatum, upperAltitudeDatum);
		this.protec.setAltitudeDatum(lowerAltitudeDatum, upperAltitudeDatum);
	}

	@Override
	public String[] getAltitudeDatum() {
		return this.stack.getAltitudeDatum();
	}

	@Override
	public void setGroundReference(LatLon groundReference) {
		this.stack.setGroundReference(groundReference);
		this.protec.setGroundReference(groundReference);
	}

	@Override
	public LatLon getGroundReference() {
		return this.stack.getGroundReference();
	}

	@Override
	public void setAnnotation(String text){
		if(annotation == null) {
			annotation = new VidesoAnnotation(text);
		} else {
			annotation.setText(text);
		}
	}
	
	@Override
	public VidesoAnnotation getAnnotation(Position pos){
		if(this.annotation == null)
			this.annotation = new VidesoAnnotation(this.getName());
		annotation.setPosition(pos);
		return annotation;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public boolean isHighlighted() {
		return this.highlighted ;
	}

	@Override
	public void setHighlighted(boolean highlighted) {
		if(this.highlighted != highlighted){
			this.setAttributes(highlighted ? this.getHighlightAttributes() : this.getNormalAttributes());
			this.highlighted = highlighted;
		}
	}
	
    public AirspaceAttributes getNormalAttributes() {
    	if(this.normalAttrs == null){
    		this.normalAttrs = new BasicAirspaceAttributes(this.getAttributes());
    	}
        return this.normalAttrs;
    }

    public void setNormalAttributes(AirspaceAttributes normalAttrs) {
        this.normalAttrs = normalAttrs;
        if(!highlighted) this.setAttributes(this.normalAttrs);
    }
    
    public AirspaceAttributes getHighlightAttributes() {
    	if(highlightAttrs == null){
    		highlightAttrs = new BasicAirspaceAttributes(this.getNormalAttributes());
    		highlightAttrs.setMaterial(Material.WHITE);
    	}
        return this.highlightAttrs;
    }

    /**
     * Specifies highlight attributes.
     *
     * @param highlightAttrs highlight attributes. May be null, in which case default attributes are used.
     */
    public void setHighlightAttributes(AirspaceAttributes highlightAttrs) {
        this.highlightAttrs = highlightAttrs;
        if(highlighted) this.setAttributes(this.highlightAttrs);
    }


	
}
