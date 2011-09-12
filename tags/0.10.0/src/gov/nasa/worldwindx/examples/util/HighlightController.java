/*
Copyright (C) 2001, 2009 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwindx.examples.util;

import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.render.Highlightable;
import gov.nasa.worldwind.util.Logging;

/**
 * Controls highlighting of shapes implementing {@link Highlightable} in response to pick events. Monitors a specified
 * World Window for an indicated {@link gov.nasa.worldwind.event.SelectEvent} type and turns highlighting on and off in
 * response.
 *
 * @author tag
 * @version $Id: HighlightController.java 1 2011-07-16 23:22:47Z dcollins $
 */
public class HighlightController implements SelectListener
{
    protected Object highlightEventType = SelectEvent.ROLLOVER;
    protected Highlightable lastHighlightObject;

    /**
     * Creates a controller for a specified World Window.
     *
     * @param highlightEventType the type of {@link SelectEvent} to highlight in response to. The default is {@link
     *                           SelectEvent#ROLLOVER}.
     */
    public HighlightController(Object highlightEventType)
    {
        this.highlightEventType = highlightEventType;
    }

    public void selected(SelectEvent event)
    {
        try
        {
            if (this.highlightEventType != null && event.getEventAction().equals(this.highlightEventType))
                highlight(event.getTopObject());
        }
        catch (Exception e)
        {
            // Wrap the handler in a try/catch to keep exceptions from bubbling up
            Logging.logger().warning(e.getMessage() != null ? e.getMessage() : e.toString());
        }
    }

    protected void highlight(Object o)
    {
        if (this.lastHighlightObject == o)
            return; // same thing selected

        // Turn off highlight if on.
        if (this.lastHighlightObject != null)
        {
            this.lastHighlightObject.setHighlighted(false);
            this.lastHighlightObject = null;
        }

        // Turn on highlight if object selected.
        if (o instanceof Highlightable)
        {
            this.lastHighlightObject = (Highlightable) o;
            this.lastHighlightObject.setHighlighted(true);
        }
    }
}
