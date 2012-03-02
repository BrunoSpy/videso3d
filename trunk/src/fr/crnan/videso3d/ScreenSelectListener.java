package fr.crnan.videso3d;

import java.awt.event.MouseEvent;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwindx.examples.util.ScreenSelector;
/**
 * Modified {@link ScreenSelector} that is always enabled but only active when control_key is down<br />
 * Doesn't allow multiple rectangle selection due to WWJ limitation : restarts the selection each time the mouse is dragged.
 * @author Bruno Spyckerelle
 * @version 0.1.0
 */
public class ScreenSelectListener extends ScreenSelector {

	private SelectionHighlightController selectionHighlightController;
	
	public ScreenSelectListener(WorldWindow worldWindow) {
		super(worldWindow);
		this.enable();//always enabled
	}

	@Override
    public void mousePressed(MouseEvent mouseEvent)
    {
        if (mouseEvent == null) // Ignore null events.
            return;

        int onmask = MouseEvent.CTRL_DOWN_MASK | MouseEvent.BUTTON1_DOWN_MASK;
        int offmask = MouseEvent.SHIFT_DOWN_MASK | MouseEvent.ALT_DOWN_MASK;

        if (!((mouseEvent.getModifiersEx() & (onmask | offmask)) == onmask)) // Respond to button 1 down with only ctrl down.
        	return;

        this.armed = true;
        
        ((VidesoGLCanvas) this.wwd).getHighlightController().dispose(); //change highlight controller
        if(selectionHighlightController == null) {
        	selectionHighlightController = new SelectionHighlightController(this.wwd, this);
        } else {
        	this.wwd.addSelectListener(selectionHighlightController);
        	this.addMessageListener(selectionHighlightController);
        }
                
        this.selectionStarted(mouseEvent);
        mouseEvent.consume(); // Consume the mouse event to prevent the view from responding to it.
    }
	

    public void mouseReleased(MouseEvent mouseEvent)
    {
        if (mouseEvent == null) // Ignore null events.
            return;

        if (!this.armed) // Respond to mouse released events when armed.
            return;

        this.armed = false;
        
        //disable specific highlight controller if selection is empty
        if(this.wwd.getObjectsInSelectionBox().size() == 0){
        	selectionHighlightController.dispose();
        	this.wwd.addSelectListener(((VidesoGLCanvas) this.wwd).getHighlightController());
        }
        this.selectionEnded(mouseEvent);
        mouseEvent.consume(); // Consume the mouse event to prevent the view from responding to it.
    }
	
}
