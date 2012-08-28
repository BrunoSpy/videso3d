package fr.crnan.videso3d.graphics.editor;

import fr.crnan.videso3d.graphics.VEllipsoid;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.AbstractShape;
import gov.nasa.worldwind.render.RigidShape;
import gov.nasa.worldwindx.examples.shapebuilder.AbstractShapeEditor;
import gov.nasa.worldwindx.examples.shapebuilder.RigidShapeEditor;
import gov.nasa.worldwindx.examples.util.ShapeUtils;
/**
 * @see gov.nasa.worldwindx.examples.shapebuilder.RigidShapeBuilder
 */
public class EllipsoidFactory extends AbstractShapeFactory {

	@Override
	public AbstractShape createShape(WorldWindow wwd, boolean fitShapeToViewport) {
		RigidShape shape = new VEllipsoid();
        shape.setAttributes(getDefaultAttributes());
        shape.setValue(AVKey.DISPLAY_NAME, getNextName(toString()));
        this.initializeShape(wwd, shape, fitShapeToViewport);

        return shape;
	}

	@Override
	public AbstractShapeEditor createEditor(AbstractShape shape) {
		 RigidShapeEditor editor = new RigidShapeEditor();
         shape.setAltitudeMode(editor.getAltitudeMode());
         editor.setShape(shape);
         return editor;
	}

	 protected void initializeShape(WorldWindow wwd, RigidShape shape, boolean fitShapeToViewport) {
         // Creates a shape in the center of the viewport. Attempts to guess at a reasonable size and height.

         double radius = fitShapeToViewport ?
             ShapeUtils.getViewportScaleFactor(wwd) / 2 : DEFAULT_SHAPE_SIZE_METERS;
         Position position = ShapeUtils.getNewShapePosition(wwd);

         // adjust position height so shape sits on terrain surface
         Vec4 centerPoint = wwd.getSceneController().getTerrain().getSurfacePoint(position.getLatitude(),
             position.getLongitude(), radius);
         Position centerPosition = wwd.getModel().getGlobe().computePositionFromPoint(centerPoint);

         shape.setCenterPosition(centerPosition);
         shape.setEastWestRadius(radius);
         shape.setVerticalRadius(radius);
         shape.setNorthSouthRadius(radius);
         shape.setHeading(Angle.ZERO);
         shape.setTilt(Angle.ZERO);
         shape.setRoll(Angle.ZERO);
         shape.setAltitudeMode(WorldWind.ABSOLUTE);
     }

     public String toString() {
         return "Ellipsoid";
     }
}
