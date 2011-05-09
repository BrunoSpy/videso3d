package fr.crnan.videso3d.graphics.editor;

import java.util.HashMap;

import fr.crnan.videso3d.VidesoGLCanvas;
import gov.nasa.worldwind.layers.AirspaceLayer;
import gov.nasa.worldwind.render.airspaces.Polygon;
/**
 * Manages all PolygonEditors
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public final class PolygonEditorsManager {

	private HashMap<Polygon, PolygonEditor> polygonEditors = new HashMap<Polygon, PolygonEditor>();
	
	private VidesoGLCanvas wwd;
	
	private AirspaceLayer editorLayer = new AirspaceLayer();
	
	private static PolygonEditorsManager instance = new PolygonEditorsManager();
	
	private PolygonEditorsManager(){
		super();
	}
	
	public static void setWWD(VidesoGLCanvas wwd){
		instance.wwd = wwd;
	}
	
	/**
	 * Enable editing of an airspace
	 * @param airspace
	 * @param orphan True : doesn't belong to an airspace
	 */
	public static void editAirspace(Polygon airspace, boolean orphan){
		if(orphan){
			instance.editorLayer.addAirspace(airspace);
			instance.wwd.toggleLayer(instance.editorLayer, true);
		}
		PolygonEditor editor = new PolygonEditor();
		editor.setPolygon(airspace);
		editor.setUseRubberBand(true);
		editor.setKeepControlPointsAboveTerrain(true);
		editor.setArmed(true);
		instance.polygonEditors.put(airspace, editor);
		instance.wwd.toggleLayer(editor, true);
		AirspaceEditorController controller = new AirspaceEditorController(instance.wwd);
		controller.setEditor(editor);
	}
	
	/**
	 * Stop editing an airspace
	 * @param airspace
	 */
	public static void stopEditAirspace(Polygon airspace){
		if(isEditing(airspace)){
			PolygonEditor editor = instance.polygonEditors.get(airspace);
			editor.setArmed(false);
			instance.wwd.removeLayer(editor);
			instance.polygonEditors.remove(airspace);
		}
	}
	
	public static boolean isEditing(Polygon polygon){
		return instance.polygonEditors.containsKey(polygon);
	}
}
