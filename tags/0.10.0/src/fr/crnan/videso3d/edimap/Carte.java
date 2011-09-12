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
package fr.crnan.videso3d.edimap;

import fr.crnan.videso3d.DatabaseManager.Type;
import fr.crnan.videso3d.geom.LatLonCautra;
import fr.crnan.videso3d.graphics.DatabaseVidesoObject;
import fr.crnan.videso3d.graphics.PolygonAnnotation;
import fr.crnan.videso3d.graphics.SurfacePolygonAnnotation;
import fr.crnan.videso3d.graphics.VidesoAnnotation;
import fr.crnan.videso3d.layers.FilterableAirspaceLayer;
import fr.crnan.videso3d.layers.PriorityRenderableLayer;
import fr.crnan.videso3d.layers.TextLayer;
import gov.nasa.worldwind.Restorable;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.GeographicText;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.UserFacingText;
import gov.nasa.worldwind.render.airspaces.Airspace;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.RestorableSupport;
import gov.nasa.worldwind.util.RestorableSupport.StateObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Carte Edimap
 * @author Bruno Spyckerelle
 * @version 0.3.0
 */
public class Carte implements DatabaseVidesoObject{
	/**
	 * Ensemble des points de référence de la carte
	 */
	private HashMap<String,LatLonCautra> pointsRef;

	/**
	 * Ensemble des id ATC de la carte
	 */
	private HashMap<String, Entity> idAtc;
	
	private int type;
	
	private String name;
	
	/**
	 * Layers
	 */
	private PriorityRenderableLayer surfaceLayer;
	private FilterableAirspaceLayer airspaceLayer;
	private TextLayer textLayer;
	
	private Set<Renderable> renderables;
	private Set<Airspace> airspaces;
	private Set<UserFacingText> texts;

	private boolean visible = false;
		
	public Carte(){
		super();
		
		renderables = new HashSet<Renderable>();
		airspaces = new HashSet<Airspace>();
		texts = new HashSet<UserFacingText>();
	}
	
	public Carte(PriorityRenderableLayer surfaceLayer, FilterableAirspaceLayer airspaceLayer, TextLayer textLayer){
		super();
		
		this.setLayers(surfaceLayer, airspaceLayer, textLayer);
		
		renderables = new HashSet<Renderable>();
		airspaces = new HashSet<Airspace>();
		texts = new HashSet<UserFacingText>();
	}
	
	public Carte(Entity carte, PaletteEdimap palette, int typeCarte,
			PriorityRenderableLayer surfaceLayer, FilterableAirspaceLayer airspaceLayer, TextLayer textLayer){
		
		this(surfaceLayer, airspaceLayer, textLayer);
		
		this.setType(typeCarte);
		
		Entity map = carte.getEntity("map");
		this.name = map.getValue("name");
		//récupération des points de référence
		pointsRef = new HashMap<String,LatLonCautra>();
		Iterator<Entity> iterator = map.getValues("ref_point").iterator();
		while(iterator.hasNext()){
			Entity point = iterator.next();
			pointsRef.put(point.getValue("name"), PointEdimap.fromEntity((point)));
		}
		//récupération des id atc
		idAtc = new HashMap<String, Entity>();
		iterator = carte.getValues("id_atc").iterator();
		while(iterator.hasNext()){
			Entity id = iterator.next();
			idAtc.put(id.getValue("name"),id);
		}
		List<Entity> entities = map.getEntity("submap").getValues("entity");
		iterator = entities.iterator();
		while(iterator.hasNext()){
			Entity entity = iterator.next();
			String type = entity.getValue("shape");
			if(type.equalsIgnoreCase("PolylineEntity")){
				String fill = (idAtc.get(entity.getValue("id_atc"))).getValue("fill_visibility");
				if(fill != null && fill.equals("1")){ //polygone
					PolygonEdimap polygon = new PolygonEdimap(entity, pointsRef, palette, idAtc);
					polygon.setName(name);
					polygon.setType(typeCarte);
					this.renderables.add(polygon);
				} else { //polyligne
					PolylineEdimap polyline = new PolylineEdimap(entity, pointsRef, palette, idAtc);
					polyline.setName(name);
					polyline.setType(typeCarte);
					this.renderables.add(polyline);
				}
				
			} else if(type.equalsIgnoreCase("LineEntity")) {
				PolylineEdimap polyline = new PolylineEdimap(entity, pointsRef, palette, idAtc);
				polyline.setName(name);
				polyline.setType(typeCarte);
				this.renderables.add(polyline);
			} else if(type.equalsIgnoreCase("RectangleEntity")){
				RectangleEdimap rectangle = new RectangleEdimap(entity, pointsRef, palette, idAtc);
				rectangle.setName(name);
				rectangle.setType(typeCarte);
				this.renderables.add(rectangle);
			} else if(type.equalsIgnoreCase("TextEntity")){
				this.texts.add(new TextEdimap(entity, pointsRef, palette, idAtc));
			} else if(type.equalsIgnoreCase("EllipseEntity")){
				EllipseEdimap ellipse = new EllipseEdimap(entity, pointsRef, palette, idAtc);
				ellipse.setName(name);
				ellipse.setType(typeCarte);
				this.renderables.add(ellipse);
			} else if(type.equalsIgnoreCase("MosaiqueEntity")) {
				this.airspaces.addAll(new MosaiqueEntity(entity, name, pointsRef));
			}
		}
//		Iterator<Entry<String, PointEdimap>> ite = pointsRef.entrySet().iterator();
//		while(ite.hasNext()){
//			Entry<String, PointEdimap> entry = ite.next();
//			System.out.println(entry.getKey() + " x : "+entry.getValue().x() +"; y : "+entry.getValue().y());
//		}
	}

	public void setLayers(PriorityRenderableLayer surfaceLayer, FilterableAirspaceLayer airspaceLayer, TextLayer textLayer){
		this.surfaceLayer = surfaceLayer;
		this.airspaceLayer = airspaceLayer;
		this.textLayer = textLayer;
	}
	
	public void setVisible(boolean visible){
		if(this.airspaceLayer == null || this.surfaceLayer == null || this.textLayer == null){
			Logging.logger().severe("Unable to change visibility of "+this.getName()+" because layers aren't set");
			return;
		}
		if(this.visible  != visible){
			this.visible = visible;
			if(!this.visible){
				for(Airspace a : this.airspaces){
					this.airspaceLayer.removeAirspace(a);
				}
				for(Renderable r : this.renderables){
					this.surfaceLayer.removeRenderable(r);
				}
				for(UserFacingText t : texts){
					this.textLayer.removeGeographicText(t);
				}
			} else {
				for(Airspace a : this.airspaces){
					this.airspaceLayer.addAirspace(a);
				}
				for(Renderable r : this.renderables){
					this.surfaceLayer.addRenderable(r);
				}
				for(UserFacingText t : texts){
					this.textLayer.addGeographicText(t);
				}
			}
		}
	}
	
	public boolean isVisible(){
		return this.visible;
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}	
	
	@Override
	public String getRestorableState() {
		RestorableSupport rs = RestorableSupport.newRestorableSupport();
        this.doGetRestorableState(rs, null);

        return rs.getStateAsXml();
	}

	protected void doGetRestorableState(RestorableSupport rs, RestorableSupport.StateObject context) {
        // Method is invoked by subclasses to have superclass add its state and only its state
        this.doMyGetRestorableState(rs, context);
    }

    private void doMyGetRestorableState(RestorableSupport rs, RestorableSupport.StateObject context) {
    	RestorableSupport.StateObject airspace = rs.addStateObject(context, "airspaceLayer");
    	for(Airspace r : this.airspaces){
    		rs.addStateValueAsString(airspace, "airspace", r.getRestorableState());
    	}
    	
    	StateObject text = rs.addStateObject(context, "textLayer");
    	for(GeographicText t : this.texts){
    		rs.addStateValueAsString(text, "numero", t.getText().toString());
    		rs.addStateValueAsPosition(text, "position", t.getPosition());
    		rs.addStateValueAsColor(text, "color", t.getColor());
    	}
    	
    	StateObject shape = rs.addStateObject(context, "shapeLayer");
    	for(Renderable r : this.renderables){
    		rs.addStateValueAsString(shape, "shape", ((Restorable)r).getRestorableState());
    	}
    	

    }

    public void restoreState(String stateInXml){
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

    protected void doRestoreState(RestorableSupport rs, RestorableSupport.StateObject context)
    {
        // Method is invoked by subclasses to have superclass add its state and only its state
        this.doMyRestoreState(rs, context);
    }

    private void doMyRestoreState(RestorableSupport rs, RestorableSupport.StateObject context) {

    	RestorableSupport.StateObject airspace = rs.getStateObject(context, "airspaceLayer");
    	StateObject[] airspacesSo = rs.getAllStateObjects(airspace, "airspace");
    	if(airspacesSo != null && airspacesSo.length > 0){
    		for(StateObject sso : airspacesSo){
    			if(sso != null){
    				PolygonAnnotation polygon = new PolygonAnnotation();
    				polygon.restoreState(rs.getStateObjectAsString(sso));
    				this.airspaces.add(polygon);
    			}
    		}
    	}

    	StateObject txt = rs.getStateObject(context, "textLayer");
    	StateObject[] texts = rs.getAllStateObjects(txt, "numero");
    	StateObject[] pos = rs.getAllStateObjects(txt, "position");
    	StateObject[] colors = rs.getAllStateObjects(txt, "color");
    	if(texts != null && pos != null && texts.length>0 && colors.length>0 && texts.length == pos.length && texts.length == colors.length){
    		for(int i = 0; i<texts.length;i++){
    			StateObject tso = texts[i];
    			StateObject pso = pos[i];
    			StateObject cso = colors[i];
    			if(tso != null && pso != null){
    				UserFacingText uft = new UserFacingText(rs.getStateObjectAsString(tso), rs.getStateObjectAsPosition(pso));
    				uft.setColor(rs.getStateObjectAsColor(cso));
    				this.texts.add(uft);
    			}
    		}
    	}

    	StateObject shape = rs.getStateObject(context, "shapeLayer");
    	StateObject[] shapes = rs.getAllStateObjects(shape, "shape");
    	if(shapes !=null && shapes.length>0){
    		for(StateObject sso : shapes){
    			if(sso != null){
    				SurfacePolygonAnnotation polygon = new SurfacePolygonAnnotation();
    				polygon.restoreState(rs.getStateObjectAsString(sso));
    				this.renderables.add(polygon);
    			}
    		}
    	}

    }

	@Override
	public void setAnnotation(String text) {
		// TODO Auto-generated method stub
	}

	@Override
	public VidesoAnnotation getAnnotation(Position pos) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getNormalAttributes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getHighlightAttributes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isHighlighted() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setHighlighted(boolean highlighted) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Type getDatabaseType() {
		return Type.Edimap;
	}

	@Override
	public void setDatabaseType(Type type) {}

	@Override
	public void setType(int type) {
		this.type = type;
	}

	@Override
	public int getType() {
		return this.type;
	}

	@Override
	public String getRestorableClassName() {
		return this.getClass().getName();
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}
	
}
