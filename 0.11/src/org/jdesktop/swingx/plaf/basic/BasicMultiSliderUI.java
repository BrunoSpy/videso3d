package org.jdesktop.swingx.plaf.basic;

import sun.swing.UIAction;

import javax.swing.plaf.basic.BasicSliderUI;
import javax.swing.plaf.ComponentUI;
import javax.swing.*;

import org.jdesktop.swingx.multislider.JXMultiSlider;
import org.jdesktop.swingx.multislider.plaf.MultiSliderUI;


import java.awt.*;
import java.awt.event.*;



/**
 * @author Arash Nikkar
 */
public class BasicMultiSliderUI extends BasicSliderUI implements MultiSliderUI {
    // Old actions forward to an instance of this.
    private static final Actions SHARED_ACTION = new Actions();

	public enum Thumb {INNER, OUTER};

    protected JXMultiSlider slider;

    protected Rectangle outerThumbRect = null;

    private transient boolean isInnerDragging, isOuterDragging;

    public BasicMultiSliderUI(JXMultiSlider b) { super(b); }

	 /**
     * Returns true if the user is dragging the slider.
     *
     * @return true if the user is dragging the slider
     * @since 1.5
     */
    protected boolean isDragging() {
        return isInnerDragging || isOuterDragging;
    }

    /////////////////////////////////////////////////////////////////////////////
    // ComponentUI Interface Implementation methods
    /////////////////////////////////////////////////////////////////////////////
    public static BasicMultiSliderUI createUI(JComponent b) {
        return new BasicMultiSliderUI((JXMultiSlider)b);
    }

    public void installUI(JComponent c)   {
        slider = (JXMultiSlider) c;

        isInnerDragging = false;
        isOuterDragging = false;

		outerThumbRect = new Rectangle();
		super.installUI(c);
		calculateGeometry(); // This figures out where the labels, ticks, track, and thumb are.	
    }

    public void uninstallUI(JComponent c) {
	    super.uninstallUI(c);
        outerThumbRect = null;
        slider = null;
    }

    protected void calculateThumbSize() {
	    super.calculateThumbSize();
		Dimension size = getThumbSize();
		outerThumbRect.setSize( size.width, size.height );
    }

    protected void calculateThumbLocation() {
        if ( slider.getSnapToTicks() ) {
			int innerValue = slider.getInnerValue();
			int outerValue = slider.getOuterValue();
			int innerSnappedValue = innerValue;
			int outerSnappedValue = outerValue;
			int majorTickSpacing = slider.getMajorTickSpacing();
			int minorTickSpacing = slider.getMinorTickSpacing();
			int tickSpacing = 0;

			if ( minorTickSpacing > 0 ) {
				tickSpacing = minorTickSpacing;
			}
			else if ( majorTickSpacing > 0 ) {
				tickSpacing = majorTickSpacing;
			}

			if ( tickSpacing != 0 ) {
				// If it's not on a tick, change the value
				if ( (innerValue - slider.getMinimum()) % tickSpacing != 0 ) {
					float temp = (float)(innerValue - slider.getMinimum()) / (float)tickSpacing;
					int whichTick = Math.round( temp );
					innerSnappedValue = slider.getMinimum() + (whichTick * tickSpacing);
				}

				if(innerSnappedValue != innerValue ) {
					slider.setInnerValue( innerSnappedValue );
				}

				// If it's not on a tick, change the value -- outer
				if ( (outerValue - slider.getMinimum()) % tickSpacing != 0 ) {
					float temp = (float)(outerValue - slider.getMinimum()) / (float)tickSpacing;
					int whichTick = Math.round( temp );
					outerSnappedValue = slider.getMinimum() + (whichTick * tickSpacing);
				}

				if(outerSnappedValue != outerValue ) {
					slider.setOuterValue( outerSnappedValue );
				}
			}
		}

        if ( slider.getOrientation() == JXMultiSlider.HORIZONTAL ) {
            int valuePosition = xPositionForValue(slider.getInnerValue());

			thumbRect.x = valuePosition - (thumbRect.width / 2);
			thumbRect.y = trackRect.y;

	        //outer
	        valuePosition = xPositionForValue(slider.getOuterValue());

			outerThumbRect.x = valuePosition - (outerThumbRect.width / 2);
			outerThumbRect.y = trackRect.y;
        }
        else {
            int valuePosition = yPositionForValue(slider.getInnerValue());

			thumbRect.x = trackRect.x;
			thumbRect.y = valuePosition - (thumbRect.height / 2);

	        valuePosition = yPositionForValue(slider.getOuterValue());

			outerThumbRect.x = trackRect.x;
			outerThumbRect.y = valuePosition - (outerThumbRect.height / 2);
        }
    }

    public void paint( Graphics g, JComponent c )   {
	    super.paint(g,c);
	    
		Rectangle clip = g.getClipBounds();
	    
		if ( clip.intersects( outerThumbRect ) ) {
			paintOuterThumb( g );
		}
    }

	public void paintOuterThumb(Graphics g)  {
        Rectangle knobBounds = outerThumbRect;
        int w = knobBounds.width;
        int h = knobBounds.height;

        g.translate(knobBounds.x, knobBounds.y);

        if ( slider.isEnabled() ) {
            g.setColor(slider.getBackground());
        }
        else {
            g.setColor(slider.getBackground().darker());
        }

		Boolean paintThumbArrowShape =
			(Boolean)slider.getClientProperty("Slider.paintThumbArrowShape");

		if ((!slider.getPaintTicks() && paintThumbArrowShape == null) ||
			paintThumbArrowShape == Boolean.FALSE) {

	        // "plain" version
            g.fillRect(0, 0, w, h);

            g.setColor(Color.black);
            g.drawLine(0, h-1, w-1, h-1);
            g.drawLine(w-1, 0, w-1, h-1);

            g.setColor(getHighlightColor());
            g.drawLine(0, 0, 0, h-2);
            g.drawLine(1, 0, w-2, 0);

            g.setColor(getShadowColor());
            g.drawLine(1, h-2, w-2, h-2);
            g.drawLine(w-2, 1, w-2, h-3);
        }
        else if ( slider.getOrientation() == JXMultiSlider.HORIZONTAL ) {
            int cw = w / 2;
            g.fillRect(1, 1, w-3, h-1-cw);
            Polygon p = new Polygon();
            p.addPoint(1, h-cw);
            p.addPoint(cw-1, h-1);
            p.addPoint(w-2, h-1-cw);
            g.fillPolygon(p);

            g.setColor(getHighlightColor());
            g.drawLine(0, 0, w-2, 0);
            g.drawLine(0, 1, 0, h-1-cw);
            g.drawLine(0, h-cw, cw-1, h-1);

            g.setColor(Color.black);
            g.drawLine(w-1, 0, w-1, h-2-cw);
            g.drawLine(w-1, h-1-cw, w-1-cw, h-1);

            g.setColor(getShadowColor());
            g.drawLine(w-2, 1, w-2, h-2-cw);
            g.drawLine(w-2, h-1-cw, w-1-cw, h-2);
        }
        else {  // vertical
            int cw = h / 2;
			if(slider.getComponentOrientation().isLeftToRight()) {
				g.fillRect(1, 1, w-1-cw, h-3);
	            Polygon p = new Polygon();
			    p.addPoint(w-cw-1, 0);
			    p.addPoint(w-1, cw);
			    p.addPoint(w-1-cw, h-2);
			    g.fillPolygon(p);

                g.setColor(getHighlightColor());
				g.drawLine(0, 0, 0, h - 2);                  // left
				g.drawLine(1, 0, w-1-cw, 0);                 // top
				g.drawLine(w-cw-1, 0, w-1, cw);              // top slant

                g.setColor(Color.black);
	            g.drawLine(0, h-1, w-2-cw, h-1);             // bottom
	            g.drawLine(w-1-cw, h-1, w-1, h-1-cw);        // bottom slant

                g.setColor(getShadowColor());
                g.drawLine(1, h-2, w-2-cw,  h-2 );         // bottom
                g.drawLine(w-1-cw, h-2, w-2, h-cw-1 );     // bottom slant
			}
			else {
			    g.fillRect(5, 1, w-1-cw, h-3);
	            Polygon p = new Polygon();
                p.addPoint(cw, 0);
                p.addPoint(0, cw);
                p.addPoint(cw, h-2);
                g.fillPolygon(p);

                g.setColor(getHighlightColor());
                g.drawLine(cw-1, 0, w-2, 0);             // top
                g.drawLine(0, cw, cw, 0);                // top slant

                g.setColor(Color.black);
                g.drawLine(0, h-1-cw, cw, h-1 );         // bottom slant
                g.drawLine(cw, h-1, w-1, h-1);           // bottom

                g.setColor(getShadowColor());
                g.drawLine(cw, h-2, w-2,  h-2 );         // bottom
                g.drawLine(w-1, 1, w-1,  h-2 );          // right
	        }
        }

        g.translate(-knobBounds.x, -knobBounds.y);
    }

    // Used exclusively by setThumbLocation()
    private static Rectangle unionRect = new Rectangle();

	public void setInnerThumbLocation(int x, int y) {
		setThumbLocation(x,y);
	}

    public void setThumbLocation(int x, int y)  {
        unionRect.setBounds( thumbRect );

	    if(slider.getInverted()) {
		    thumbRect.setLocation( Math.max(x, outerThumbRect.x), Math.min(y, outerThumbRect.y) );
	    }
	    else {
		    thumbRect.setLocation( Math.min(x, outerThumbRect.x), Math.max(y, outerThumbRect.y) );
	    }

		SwingUtilities.computeUnion( thumbRect.x, thumbRect.y, thumbRect.width, thumbRect.height, unionRect );
        slider.repaint( unionRect.x, unionRect.y, unionRect.width, unionRect.height );
    }

	public void setOuterThumbLocation(int x, int y)  {
        unionRect.setBounds( outerThumbRect );
		
		if(slider.getInverted()) {
			outerThumbRect.setLocation( Math.min(x, thumbRect.x), Math.max(y, thumbRect.y) );

	    }
	    else {
			outerThumbRect.setLocation( Math.max(x, thumbRect.x), Math.min(y, thumbRect.y) ); 
		}

		SwingUtilities.computeUnion( outerThumbRect.x, outerThumbRect.y, outerThumbRect.width, outerThumbRect.height, unionRect );
        slider.repaint( unionRect.x, unionRect.y, unionRect.width, unionRect.height );
    }

    public void scrollByBlock(int direction, Thumb thumb, MouseEvent me) {
        synchronized(slider) {

	        if(thumb == Thumb.INNER) {
				int oldValue = slider.getInnerValue();
				int blockIncrement = (slider.getMaximum() - slider.getMinimum()) / 10;
				if (blockIncrement <= 0 && slider.getMaximum() > slider.getMinimum()) {
					blockIncrement = 1;
				}

		        int mouseValue = slider.getOrientation() == JXMultiSlider.HORIZONTAL ?
				        valueForXPosition(me.getX())  :
				        valueForYPosition(me.getY()) ;
				int delta = blockIncrement * ((direction > 0) ? POSITIVE_SCROLL : NEGATIVE_SCROLL);

		        if((direction > 0 && (oldValue + delta) > mouseValue) || (direction < 0 && (oldValue + delta) < mouseValue)) {
			        slider.setInnerValue(mouseValue);
		        }
		        else {
					slider.setInnerValue(oldValue + delta);
		        }
	        }
	        else if(thumb == Thumb.OUTER) {
				int oldValue = slider.getOuterValue();
				int blockIncrement = (slider.getMaximum() - slider.getMinimum()) / 10;
				if (blockIncrement <= 0 && slider.getMaximum() > slider.getMinimum()) {
					blockIncrement = 1;
				}

		         int mouseValue = slider.getOrientation() == JXMultiSlider.HORIZONTAL ?
				        valueForXPosition(me.getX()) :
				        valueForYPosition(me.getY());
				int delta = blockIncrement * ((direction > 0) ? POSITIVE_SCROLL : NEGATIVE_SCROLL);

		        if((direction > 0 && (oldValue + delta) > mouseValue) || (direction < 0 && (oldValue + delta) < mouseValue)) {
			        slider.setOuterValue(mouseValue);
		        }
		        else {
			        slider.setOuterValue(oldValue + delta);
		        }
	        }
        }
    }

    public void scrollByUnit(int direction, Thumb thumb, MouseEvent me) {
        synchronized(slider)    {

	        if(thumb == Thumb.INNER) {
                int oldValue = slider.getInnerValue();

		        int mouseValue = slider.getOrientation() == JXMultiSlider.HORIZONTAL ?
				        valueForXPosition(me.getX()) :
				        valueForYPosition(me.getY());
                int delta = 1 * ((direction > 0) ? POSITIVE_SCROLL : NEGATIVE_SCROLL);

		        if((direction > 0 && (oldValue + delta) > mouseValue) || (direction < 0 && (oldValue + delta) < mouseValue)) {
			        slider.setInnerValue(mouseValue);
		        }
		        else {
					slider.setInnerValue(oldValue + delta);
		        }
	        }
	        else {
		        int oldValue = slider.getOuterValue();
                int mouseValue = slider.getOrientation() == JXMultiSlider.HORIZONTAL ?
				        valueForXPosition(me.getX()) :
				        valueForYPosition(me.getY());
                int delta = 1 * ((direction > 0) ? POSITIVE_SCROLL : NEGATIVE_SCROLL);

		        if((direction > 0 && (oldValue + delta) > mouseValue) || (direction < 0 && (oldValue + delta) < mouseValue)) {
			        slider.setOuterValue(mouseValue);
		        }
		        else {
					slider.setOuterValue(oldValue + delta);
		        }
	        }
        }
    }

    /**
     * This function is called when a mousePressed was detected in the track, not
     * in the thumb.  The default behavior is to scroll by block.  You can
     *  override this method to stop it from scrolling or to add additional behavior.
     */

    protected void scrollDueToClickInTrack(int dir, Thumb thumb, MouseEvent me) {
        scrollByBlock(dir, thumb, me);
    }

	protected JXMultiTrackListener createTrackListener(JSlider slider) {
        return new JXMultiTrackListener();
    }

    /////////////////////////////////////////////////////////////////////////
    /// Track Listener Class
    /////////////////////////////////////////////////////////////////////////
    /**
     * Track mouse movements.
     *
     * This class should be treated as a &quot;protected&quot; inner class.
     * Instantiate it only within subclasses of <Foo>.
     */
    public class JXMultiTrackListener extends BasicSliderUI.TrackListener {
        protected transient int offset;
        protected transient int currentMouseX, currentMouseY;

        public void mouseReleased(MouseEvent e) {
            if (!slider.isEnabled()) {
                return;
            }

            offset = 0;
            scrollTimer.stop();

            // This is the way we have to determine snap-to-ticks.  It's
            // hard to explain but since ChangeEvents don't give us any
            // idea what has changed we don't have a way to stop the thumb
            // bounds from being recalculated.  Recalculating the thumb
            // bounds moves the thumb over the current value (i.e., snapping
            // to the ticks).
            if (slider.getSnapToTicks() /*|| slider.getSnapToValue()*/ ) {
                isInnerDragging = false;
                isOuterDragging = false;
                slider.setValueIsAdjusting(false);
            }
            else {
                slider.setValueIsAdjusting(false);
                isInnerDragging = false;
                isOuterDragging = false;
            }
            slider.repaint();
        }

        /**
        * If the mouse is pressed above the "thumb" component
        * then reduce the scrollbars value by one page ("page up"),
        * otherwise increase it by one page.  If there is no
        * thumb then page up if the mouse is in the upper half
        * of the track.
        */
        public void mousePressed(MouseEvent e) {
            if (!slider.isEnabled()) {
                return;
            }

            currentMouseX = e.getX();
            currentMouseY = e.getY();

            if (slider.isRequestFocusEnabled()) {
                slider.requestFocus();
            }

            // Clicked in the Thumb area?
            if (thumbRect.contains(currentMouseX, currentMouseY)) {
                switch (slider.getOrientation()) {
                case JXMultiSlider.VERTICAL:
                    offset = currentMouseY - thumbRect.y;
                    break;
                case JXMultiSlider.HORIZONTAL:
                    offset = currentMouseX - thumbRect.x;
                    break;
                }
                isInnerDragging = true;
                return;
            }

	        if (outerThumbRect.contains(currentMouseX, currentMouseY)) {
                switch (slider.getOrientation()) {
                case JXMultiSlider.VERTICAL:
                    offset = currentMouseY - outerThumbRect.y;
                    break;
                case JXMultiSlider.HORIZONTAL:
                    offset = currentMouseX - outerThumbRect.x;
                    break;
                }
                isOuterDragging = true;
                return;
            }

			isInnerDragging = false;
			isOuterDragging = false;

            slider.setValueIsAdjusting(true);

            int direction = POSITIVE_SCROLL;
	        Thumb thumbDir = Thumb.OUTER;

            switch (slider.getOrientation()) {
				case JXMultiSlider.VERTICAL:
					if(currentMouseY < outerThumbRect.y) {
						direction = POSITIVE_SCROLL;
						thumbDir = Thumb.OUTER;
					}
					else if(currentMouseY > thumbRect.y) {
						direction = NEGATIVE_SCROLL;
						thumbDir = Thumb.INNER;
					}
					else {
						int mid = outerThumbRect.y + ((thumbRect.y - outerThumbRect.y) / 2);

						if(currentMouseY <= mid) {
							direction = NEGATIVE_SCROLL;
							thumbDir = Thumb.OUTER;
						}
						else {
							direction = POSITIVE_SCROLL;
							thumbDir = Thumb.INNER;
						}
					}
					break;
				case JXMultiSlider.HORIZONTAL:
					if(currentMouseX > outerThumbRect.x) {
						direction = POSITIVE_SCROLL;
						thumbDir = Thumb.OUTER;
					}
					else if(currentMouseX < thumbRect.x) {
						direction = NEGATIVE_SCROLL;
						thumbDir = Thumb.INNER;
					}
					else {
						int mid = thumbRect.x + ((outerThumbRect.x - thumbRect.x) / 2);

						if(currentMouseX >= mid) {
							direction = NEGATIVE_SCROLL;
							thumbDir = Thumb.OUTER;
						}
						else {
							direction = POSITIVE_SCROLL;
							thumbDir = Thumb.INNER;   							
						}
					}
					break;
            }

			if (shouldScroll(direction)) {
				scrollDueToClickInTrack(direction, thumbDir, e);
			}
			if (shouldScroll(direction)) {
				scrollTimer.stop();
				scrollListener.setDirection(direction);
				((JXMultiScrollListener)scrollListener).setMouseEvent(e);
				((JXMultiScrollListener)scrollListener).setThumb(thumbDir);
				scrollTimer.start();
			}
        }

        public boolean shouldScroll(int direction) {
	        /*
	        Rectangle r = thumbRect;
	        Rectangle r2 = outerThumbRect;
            if (slider.getOrientation() == JSlider.VERTICAL) {
                if (drawInverted() ? direction < 0 : direction > 0) {
                    if (r.y  <= currentMouseY && r2.y <= currentMouseY) {
                        return false;
                    }
                }
                else if (r.y + r.height >= currentMouseY && r2.y + r2.height >= currentMouseY) {
                    return false;
                }
            }
            else {
                if (drawInverted() ? direction < 0 : direction > 0) {
                    if (r.x + r.width  >= currentMouseX && r2.x + r2.width  >= currentMouseX) {
                        return false;
                    }
                }
                else if (r.x <= currentMouseX && r2.x <= currentMouseX) {
                    return false;
                }
            }

            if (direction > 0 && slider.getOuterValue() + slider.getExtent() >=
                    slider.getMaximum()) {
                return false;
            }
            else if (direction < 0 && slider.getInnerValue() <=
                    slider.getMinimum()) {
                return false;
            }
            */

            return true;
        }

        /**
        * Set the models value to the position of the top/left
        * of the thumb relative to the origin of the track.
        */
        public void mouseDragged(MouseEvent e) {
            int thumbMiddle = 0;

            if (!slider.isEnabled()) {
                return;
            }

            currentMouseX = e.getX();
            currentMouseY = e.getY();

            if (!isDragging()) {
                return;
            }

            slider.setValueIsAdjusting(true);

            switch (slider.getOrientation()) {
				case JXMultiSlider.VERTICAL:
					int halfThumbHeight = isInnerDragging ? thumbRect.height / 2 : outerThumbRect.height / 2;
					int thumbTop = e.getY() - offset;
					int trackTop = trackRect.y;
					int trackBottom = trackRect.y + (trackRect.height - 1);
					int vMax = yPositionForValue(slider.getMaximum() -
												slider.getExtent());

					if (drawInverted()) {
						trackBottom = vMax;
					}
					else {
						trackTop = vMax;
					}
					thumbTop = Math.max(thumbTop, trackTop - halfThumbHeight);
					thumbTop = Math.min(thumbTop, trackBottom - halfThumbHeight);

					thumbMiddle = thumbTop + halfThumbHeight;
					if(isInnerDragging) {
						setInnerThumbLocation(thumbRect.x, thumbTop);
						slider.setInnerValue( valueForYPosition( thumbMiddle ) );
					}
					else {
						setOuterThumbLocation(outerThumbRect.x, thumbTop);
						slider.setOuterValue( valueForYPosition( thumbMiddle ) );
					}

					break;
				case JXMultiSlider.HORIZONTAL:
					int halfThumbWidth = isInnerDragging ? thumbRect.width / 2 : outerThumbRect.width / 2;
					int thumbLeft = e.getX() - offset;
					int trackLeft = trackRect.x;
					int trackRight = trackRect.x + (trackRect.width - 1);
					int hMax = xPositionForValue(slider.getMaximum() - slider.getExtent());

					if (drawInverted()) {
						trackLeft = hMax;
					}
					else {
						trackRight = hMax;
					}
					
					thumbLeft = Math.max(thumbLeft, trackLeft - halfThumbWidth);
					thumbLeft = Math.min(thumbLeft, trackRight - halfThumbWidth);

					thumbMiddle = thumbLeft + halfThumbWidth;

					if(isInnerDragging) {
						setInnerThumbLocation(thumbLeft, thumbRect.y);
						slider.setInnerValue( valueForXPosition( thumbMiddle ) );
					}
					else {
						setOuterThumbLocation(thumbLeft, outerThumbRect.y);
						slider.setOuterValue( valueForXPosition( thumbMiddle ) );
					}
					break;
				default:
					return;
            }
        }

        public void mouseMoved(MouseEvent e) { }
    }

	protected JXMultiScrollListener createScrollListener( JSlider slider ) {
        return new JXMultiScrollListener();
    }

    /**
     * Scroll-event listener.
     *
     * This class should be treated as a &quot;protected&quot; inner class.
     * Instantiate it only within subclasses of <Foo>.
     */
    public class JXMultiScrollListener extends BasicSliderUI.ScrollListener {
        // changed this class to public to avoid bogus IllegalAccessException
        // bug in InternetExplorer browser.  It was protected.  Work around
        // for 4109432
        int dir = POSITIVE_SCROLL;
        boolean useBlock;
	    Thumb thumb;
	    MouseEvent me;

        public JXMultiScrollListener() {
            dir = POSITIVE_SCROLL;
            useBlock = true;
	        thumb = Thumb.INNER;
        }

        public JXMultiScrollListener(int dir, boolean block, Thumb thumb, MouseEvent me)   {
            this.dir = dir;
            useBlock = block;
	        this.thumb = thumb;
	        this.me = me;
        }

	    public void setScrollByBlock(boolean b) {
		    this.useBlock = b;
	    }

	    public void setDirection(int d) {
		    this.dir = d;
	    }

	    public void setThumb(Thumb thumb) {
		    this.thumb = thumb;
	    }

	    public void setMouseEvent(MouseEvent me) {
		    this.me = me;
	    }

        public void actionPerformed(ActionEvent e) {
            if (useBlock) {
                scrollByBlock(dir, thumb, me);
            }
            else {
                scrollByUnit(dir, thumb, me);
            }
            if (!trackListener.shouldScroll(dir)) {
                ((Timer)e.getSource()).stop();
            }
        }
    }


    /**
     * As of Java 2 platform v1.3 this undocumented class is no longer used.
     * The recommended approach to creating bindings is to use a
     * combination of an <code>ActionMap</code>, to contain the action,
     * and an <code>InputMap</code> to contain the mapping from KeyStroke
     * to action description. The InputMap is is usually described in the
     * LookAndFeel tables.
     * <p>
     * Please refer to the key bindings specification for further details.
     * <p>
     * This class should be treated as a &quot;protected&quot; inner class.
     * Instantiate it only within subclasses of <Foo>.
     */
    public class JXMultiActionScroller extends ActionScroller {
        // NOTE: This class exists only for backward compatability. All
        // its functionality has been moved into Actions. If you need to add
        // new functionality add it to the Actions, but make sure this
        // class calls into the Actions.
        int dir;
        boolean block;
        JXMultiSlider slider;
	    Thumb thumb;

        public JXMultiActionScroller( JXMultiSlider slider, int dir, boolean block, Thumb thumb) {
	        super(slider, dir, block);
            this.dir = dir;
            this.block = block;
            this.slider = slider;
	        this.thumb = thumb;
        }

        public void actionPerformed(ActionEvent e) {
            SHARED_ACTION.scroll(slider, BasicMultiSliderUI.this, dir, block, thumb);
		}

		public boolean isEnabled() {
			boolean b = true;
			if (slider != null) {
				b = slider.isEnabled();
			}
			return b;
		}
    };


    /**
     * A static version of the above.
     */
    static class JXMultiSharedActionScroller extends AbstractAction {
        // NOTE: This class exists only for backward compatability. All
        // its functionality has been moved into Actions. If you need to add
        // new functionality add it to the Actions, but make sure this
        // class calls into the Actions.
        int dir;
        boolean block;
	    Thumb thumb;

        public JXMultiSharedActionScroller(int dir, boolean block, Thumb thumb) {
            this.dir = dir;
            this.block = block;
	        this.thumb = thumb;
        }

        public void actionPerformed(ActionEvent evt) {
            JXMultiSlider slider = (JXMultiSlider)evt.getSource();
            BasicMultiSliderUI ui = (BasicMultiSliderUI) getUIOfType(slider.getUI(), BasicMultiSliderUI.class);
            if (ui == null) {
                return;
            }
            SHARED_ACTION.scroll(slider, ui, dir, block, thumb);
		}
    }

    private static class Actions extends UIAction {
        public static final String POSITIVE_UNIT_INNER_INCREMENT = "positiveUnitIncrement";
        public static final String POSITIVE_UNIT_OUTER_INCREMENT = "positiveUnitIncrement";
        public static final String POSITIVE_BLOCK_INNER_INCREMENT = "positiveBlockIncrement";
        public static final String POSITIVE_BLOCK_OUTER_INCREMENT = "positiveBlockIncrement";
        public static final String NEGATIVE_UNIT_INNER_INCREMENT =  "negativeUnitIncrement";
        public static final String NEGATIVE_UNIT_OUTER_INCREMENT =  "negativeUnitIncrement";
        public static final String NEGATIVE_BLOCK_INNER_INCREMENT = "negativeBlockIncrement";
        public static final String NEGATIVE_BLOCK_OUTER_INCREMENT = "negativeBlockIncrement";
        public static final String MIN_SCROLL_INCREMENT = "minScroll";
        public static final String MAX_SCROLL_INCREMENT = "maxScroll";


        Actions() {
            super(null);
        }

        public Actions(String name) {
            super(name);
        }

        public void actionPerformed(ActionEvent evt) {
            JXMultiSlider slider = (JXMultiSlider)evt.getSource();
            BasicMultiSliderUI ui = (BasicMultiSliderUI)getUIOfType(slider.getUI(), BasicMultiSliderUI.class);
            String name = getName();

            if (ui == null) {
                return;
            }
            if (POSITIVE_UNIT_INNER_INCREMENT == name) {
                scroll(slider, ui, POSITIVE_SCROLL, false, Thumb.INNER);
            } else if (POSITIVE_UNIT_OUTER_INCREMENT == name) {
                scroll(slider, ui, POSITIVE_SCROLL, false, Thumb.OUTER);
            } else if (NEGATIVE_UNIT_INNER_INCREMENT == name) {
                scroll(slider, ui, NEGATIVE_SCROLL, false, Thumb.INNER);
            } else if (NEGATIVE_UNIT_OUTER_INCREMENT == name) {
                scroll(slider, ui, NEGATIVE_SCROLL, false, Thumb.OUTER);
            } else if (POSITIVE_BLOCK_INNER_INCREMENT == name) {
                scroll(slider, ui, POSITIVE_SCROLL, true, Thumb.INNER);
            } else if (POSITIVE_BLOCK_OUTER_INCREMENT == name) {
                scroll(slider, ui, POSITIVE_SCROLL, true, Thumb.OUTER);
            } else if (NEGATIVE_BLOCK_INNER_INCREMENT == name) {
                scroll(slider, ui, NEGATIVE_SCROLL, true, Thumb.INNER);
            } else if (NEGATIVE_BLOCK_OUTER_INCREMENT == name) {
                scroll(slider, ui, NEGATIVE_SCROLL, true, Thumb.OUTER);
            } else if (MIN_SCROLL_INCREMENT == name) {
                scroll(slider, ui, MIN_SCROLL, false, null);
            } else if (MAX_SCROLL_INCREMENT == name) {
                scroll(slider, ui, MAX_SCROLL, false, null);
            }
        }

        private void scroll(JXMultiSlider slider, BasicMultiSliderUI ui, int direction, boolean isBlock, Thumb thumb) {
            boolean invert = slider.getInverted();

            if (direction == NEGATIVE_SCROLL || direction == POSITIVE_SCROLL) {
                if (invert) {
                    direction = (direction == POSITIVE_SCROLL) ?
                        NEGATIVE_SCROLL : POSITIVE_SCROLL;
                }
	            
                if (isBlock) {
                    ui.scrollByBlock(direction, thumb, null);
                } else {
                    ui.scrollByUnit(direction, thumb, null);
                }
            }
            else {  // MIN or MAX
                if (invert) {
                    direction = (direction == MIN_SCROLL) ?
                        MAX_SCROLL : MIN_SCROLL;
                }

                slider.setInnerValue((direction == MIN_SCROLL) ? slider.getMinimum() : slider.getMaximum());
                slider.setOuterValue((direction == MIN_SCROLL) ? slider.getMinimum() : slider.getMaximum());
            }
        }
    }

	static Object getUIOfType(ComponentUI ui, Class klass) {
        if (klass.isInstance(ui)) {
            return ui;
        }
        return null;
    }
}