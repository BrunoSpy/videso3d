package fr.crnan.videso3d.graphics.editor;

import java.awt.Color;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.render.AbstractShape;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwindx.examples.shapebuilder.AbstractShapeEditor;

/**
 * @see gov.nasa.worldwindx.examples.shapebuilder.RigidShapeBuilder
 *
 */
public abstract class AbstractShapeFactory {

	protected static long nextEntryNumber = 1;
	
	protected static final double DEFAULT_SHAPE_SIZE_METERS = 100000.0; // 200 km
	
	public abstract AbstractShape createShape(WorldWindow wwd, boolean fitShapeToViewport);
	
	public abstract AbstractShapeEditor createEditor(AbstractShape shape);
	
    public static ShapeAttributes getDefaultAttributes()  {
        ShapeAttributes attributes = new BasicShapeAttributes();
        attributes.setInteriorMaterial(new Material(Color.BLACK, Color.LIGHT_GRAY, Color.DARK_GRAY, Color.BLACK, 0.0f));
        attributes.setOutlineMaterial(Material.DARK_GRAY);
        attributes.setDrawOutline(false);
        attributes.setInteriorOpacity(.75);
        attributes.setOutlineOpacity(.95);
        attributes.setOutlineWidth(2);
        attributes.setEnableLighting(true);
        return attributes;
    }

    public static ShapeAttributes getSelectionAttributes()  {
        ShapeAttributes attributes = new BasicShapeAttributes();
        attributes.setInteriorMaterial(Material.WHITE);
        attributes.setOutlineMaterial(Material.BLACK);
        attributes.setDrawOutline(false);
        attributes.setInteriorOpacity(0.85);
        attributes.setOutlineOpacity(0.8);
        attributes.setOutlineWidth(2);
        attributes.setEnableLighting(true);
        return attributes;
    }

    public static String getNextName(String base)  {
        StringBuilder sb = new StringBuilder();
        sb.append(base);
        sb.append(nextEntryNumber++);
        return sb.toString();
    }
}
