package gov.nasa.worldwindx.examples.view.orbit;

import gov.nasa.worldwind.awt.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.view.orbit.*;

import java.awt.*;
import java.awt.event.*;
/**
 * 
 * @author pabercrombie
 *
 */
public class ZoomToCursorViewInputHandler extends OrbitViewInputHandler
{
    protected class ZoomActionHandler extends VertTransMouseWheelActionListener
    {
        @Override
        public boolean inputActionPerformed(AbstractViewInputHandler inputHandler,
            MouseWheelEvent mouseWheelEvent, ViewInputAttributes.ActionAttributes viewAction)
        {
            double zoomInput = mouseWheelEvent.getWheelRotation();
            Position position = computeSelectedPosition();

            // Zoom toward the cursor if we're zooming in. Move straight out when zooming out.
            if (zoomInput < 0 && position != null)
                return this.zoomToPosition(position, zoomInput, viewAction);
            else
                return super.inputActionPerformed(inputHandler, mouseWheelEvent, viewAction);
        }

        protected boolean zoomToPosition(Position position, double zoomInput, ViewInputAttributes.ActionAttributes viewAction)
        {
            BasicOrbitView orbitView = (BasicOrbitView) getView();
            Position centerPosition = orbitView.getCenterPosition();

            LatLon delta = position.subtract(centerPosition);

            Rectangle viewport = orbitView.getViewport();
            double viewportWidth = viewport.getWidth();
            double viewportHeight = viewport.getHeight();

            // Compute a scale factor based on how far the mouse cursor is from the viewport center.
            double dist = getMousePoint().distanceSq(viewport.getCenterX(), viewport.getCenterY());
            double scale = dist / (viewportWidth * viewportWidth + viewportHeight * viewportHeight);

            Angle latitudeChange = delta.latitude.multiply(scale);
            Angle longitudeChange = delta.longitude.multiply(scale);

            // Apply horizontal translation, if necessary.
            if (!latitudeChange.equals(Angle.ZERO) || !longitudeChange.equals(Angle.ZERO))
            {
                Position newPosition = orbitView.getCenterPosition().add(
                    new Position(latitudeChange, longitudeChange, 0.0));

                setCenterPosition(orbitView, uiAnimControl, newPosition, viewAction);
            }

            double zoomChange = zoomInput * getScaleValueRotate(viewAction);
            onVerticalTranslate(zoomChange, viewAction);

            return true;
        }
    }

    public ZoomToCursorViewInputHandler()
    {
        ViewInputAttributes.ActionAttributes actionAttrs = this.getAttributes().getActionMap(
            ViewInputAttributes.DEVICE_MOUSE_WHEEL).getActionAttributes(
            ViewInputAttributes.VIEW_VERTICAL_TRANSLATE);
        actionAttrs.setMouseActionListener(new ZoomActionHandler());
    }
}