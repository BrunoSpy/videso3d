package org.jdesktop.swingx.multislider.plaf;

import java.awt.*;

/**
 * @author Arash Nikkar
 */
public interface MultiSliderUI {

	public void paintOuterThumb(Graphics g);
	public void setInnerThumbLocation(int x, int y);
	public void setOuterThumbLocation(int x, int y);
}
