package fr.crnan.videso3d.ihm.components;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;

/**
 * This component is designed to work similar to a <tt>JSplitPane</tt> but for
 * an arbitrary number of splits, rather than just two. Internally, the component
 * uses nested <tt>JSplitPane</tt>s to add multiple layers.
 * @author achase
 */
public class MultipleSplitPanes extends JPanel {

	private int orientation = JSplitPane.VERTICAL_SPLIT;

	//the top-level splitpane.

	private JSplitPane mainSplit = null;

	//keep track of components that get added to the layeredPane without a splitpane

	private Component nakedComponent = null;

	private boolean isEmpty = true;

	private int update = 0;
	
	/**
	 * Make a new <tt>LayeredPane</tt> with the specified orientation, which
	 * needs to be either <tt>JSplitPane.VERTICAL_SPLIT</tt> or
	 * <tt>JSplitPane.HORIZONTAL_SPLIT</tt>
	 *
	 * @param orientation the orientation of the layers
	 */
	public MultipleSplitPanes(int orientation) {

		super(new BorderLayout());
		if (orientation != JSplitPane.VERTICAL_SPLIT && orientation != JSplitPane.HORIZONTAL_SPLIT) {

			throw new IllegalArgumentException(
					"Invalid parameter to LayeredPane, must be"
					+ "either JSplitPane.VERTICAL_SPLIT or JSplitPane.HORIZONTAL_SPLIT");
		}
		this.orientation = orientation;
	}

	/**
	 * Vertical split by default
	 */
	public MultipleSplitPanes(){
		this(JSplitPane.VERTICAL_SPLIT);
	}
	
	
	public void doLayout() {
		if(update<this.getNumberOfSplits()) { //don't update the layout more than requested
			updateLayout();
			update++;
		}
		super.doLayout();
	}

	/**
	 * Add a layer to the <tt>LayeredPane</tt> that contains the component specified.
	 * The new layer will be added as the topmost, or leftmost component in the <tt>LayeredPane</tt>
	 * @param component the object to put in the new layer.
	 */

	public void addLayer(Component component) {

		//if the whole thing is empty, just add the component straight to the panel
		if (isEmpty) {
			add(component);
			nakedComponent = component;
			isEmpty = false;
		}
		//If the lastSplit was null, then no split has been added to the panel yet,
		//create a new splitpane and add top and bottom components to it
		else if (mainSplit == null) {
			mainSplit = new JSplitPane(orientation);
			mainSplit.setBottomComponent(nakedComponent);
			mainSplit.setTopComponent(component);
			this.add(mainSplit, BorderLayout.CENTER);
		}

		//If a splitpane already exists, then create a new splitpane, add it to the
		//top component of the last splitpane, and move the existing top component from
		//the last splitpane into the bottom of the new splitpane.

		else {
			Component topComponent = mainSplit;
			JSplitPane lastSplit = mainSplit;
			while (topComponent instanceof JSplitPane) {
				lastSplit = (JSplitPane) topComponent;
				topComponent = ((JSplitPane) topComponent).getTopComponent();
			}

			if (topComponent == null) {
				lastSplit.setTopComponent(component);
			} else {
				//now topComponent should be the deepest nested component.
				JSplitPane newSplit = new JSplitPane(orientation);
				JSplitPane nestedSplit = (JSplitPane) topComponent.getParent();
				//the topComponent will be removed from its old splitpane and added
				//as the bottom of the new one
				newSplit.setBottomComponent(topComponent);
				//now add the new component to the top of the new splitpane
				newSplit.setTopComponent(component);
				//and finally, add the new splitPane to the space the topComponent
				//previously occupied
				nestedSplit.setTopComponent(newSplit);
			}
		}
		updateLayout();
		repaint();
	}

	public synchronized void removeLayer(Component component) {

		Container parent = component.getParent();
		if (parent instanceof JSplitPane) {
			//first get both top and bottom component and find out which
			//one is being removed
			Component top = ((JSplitPane) parent).getTopComponent();
			Component bottom = ((JSplitPane) parent).getBottomComponent();
			Component sibling = null;

			if (top.equals(component)) {
				sibling = bottom;
			} else if (bottom.equals(component)) {
				sibling = top;
			} else {
				System.err.println(
						"neither top or bottom component matched"
						+ "the component to be removed");
			}

			//now remove the splitpane and add the sibling back onto the layeredPane
			parent.getParent().remove(parent);

			//if the parent removed was the mainsplit, then set mainsplit to null
			if (parent.equals(mainSplit)) {
				if (sibling instanceof JSplitPane) {
					mainSplit = (JSplitPane) sibling;
					add(mainSplit);
				} else {
					mainSplit = null;
					isEmpty = true;
					if(sibling != null){
						addLayer(sibling);
					}
				}
			}
			else if (sibling != null) {
				addLayer(sibling);
			}
		} else {
			parent.remove(component);
			//there no longer is a naked component, it has been removed.
			nakedComponent = null;
			isEmpty = true;
		}

		repaint();
		updateLayout();
	}

	private void updateLayout() {

		int height = this.getHeight();
		int layerHeight = (int) ((double) height / (double) (getNumberOfSplits() + 1));
		int location = height - layerHeight;

		Component lastSplit = mainSplit;
		while(lastSplit instanceof JSplitPane){
			((JSplitPane)lastSplit).setDividerSize(3);
			((JSplitPane)lastSplit).setDividerLocation(location);
			location -= layerHeight;
			lastSplit = ((JSplitPane)lastSplit).getTopComponent();
		}
		revalidate();
	}

	/**
	 * Get the number of <tt>JSplitPanes</tt> nested within this component
	 * @return the number of <tt>JSplitPanes</tt> nested in this component
	 */

	public int getNumberOfSplits(){

		if(mainSplit == null){
			return 0;
		}

		//start at 0 which will skip counting the mainSplit, that way, when the	while
		//loop kicks out because lastSplit is not a JSplitPane the counter does not
		//have to be decremented

		int splitCount = 0;

		Component lastSplit = mainSplit;

		while(lastSplit instanceof JSplitPane){
			lastSplit = ((JSplitPane)lastSplit).getTopComponent();
			splitCount++;
		}
		return splitCount;
	}

}