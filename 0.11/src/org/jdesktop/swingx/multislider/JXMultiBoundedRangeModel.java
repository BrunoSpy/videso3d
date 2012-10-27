package org.jdesktop.swingx.multislider;

import javax.swing.*;

/**
 * @author Arash Nikkar
 */
public class JXMultiBoundedRangeModel extends DefaultBoundedRangeModel {

	//treat value as innerValue
    private int outerValue = 0;

    /**
     * Initializes value, extent, minimum and maximum. Adjusting is false.
     * Throws an <code>IllegalArgumentException</code> if the following
     * constraints aren't satisfied:
     * <pre>
     * min &lt;= value &lt;= value+extent &lt;= max
     * </pre>
     */
    public JXMultiBoundedRangeModel(int innerValue, int outerValue, int extent, int min, int max) {
		super(innerValue, extent, min, max);
        if(((innerValue + extent) <= outerValue) && (outerValue >= innerValue) &&
		        ((outerValue + extent) >= outerValue) &&  ((outerValue + extent) <= max)) {
            this.outerValue = outerValue;
        }
        else {
            throw new IllegalArgumentException("invalid range properties");
        }
    }


    /**
     * Returns the model's current inner value.
     * @return the model's current inner value
     * @see #setInnerValue
     * @see #setOuterValue
     * @see BoundedRangeModel#getValue
     */
    public int getInnerValue() { 
      return getValue();
    }

	/**
     * Returns the model's current inner value.
     * @return the model's current inner value
     * @see #setInnerValue
     * @see #setOuterValue
     * @see BoundedRangeModel#getValue
     */
    public int getOuterValue() {
      return outerValue;
    }


    /**
     * Sets the current value of the model. For a slider, that
     * determines where the knob appears. Ensures that the new
     * value, <I>n</I> falls within the model's constraints:
     * <pre>
     *     minimum &lt;= value &lt;= value+extent &lt;= maximum
     * </pre>
     *
     * @see BoundedRangeModel#setValue
     */
    public void setInnerValue(int n) {
       setValue(n);
    }

	public void setValue(int n) {
		n = Math.min(n, Integer.MAX_VALUE - getExtent());

        int newValue = Math.max(n, getMinimum());
        if (newValue + getExtent() > outerValue) {
            newValue = outerValue - getExtent();
        }
        setRangeProperties(newValue, outerValue, getExtent(), getMinimum(), getMaximum(), getValueIsAdjusting());

	}

	/**
     * Sets the current value of the model. For a slider, that
     * determines where the knob appears. Ensures that the new
     * value, <I>n</I> falls within the model's constraints:
     * <pre>
     *     minimum &lt;= value &lt;= value+extent &lt;= maximum
     * </pre>
     *
     * @see BoundedRangeModel#setValue
     */
    public void setOuterValue(int n) {
        n = Math.min(n, Integer.MAX_VALUE - getExtent());
		
        int newValue = Math.max(n, getInnerValue());
        if (newValue + getExtent() > getMaximum()) {
            newValue = getMaximum() - getExtent();
        }
        setRangeProperties(getInnerValue(), newValue, getExtent(), getMinimum(), getMaximum(), getValueIsAdjusting());
    }


    /**
     * Sets the extent to <I>n</I> after ensuring that <I>n</I>
     * is greater than or equal to zero and falls within the model's
     * constraints:
     * <pre>
     *     minimum &lt;= value &lt;= value+extent &lt;= maximum
     * </pre>
     * @see BoundedRangeModel#setExtent
     */
    public void setExtent(int n) {
        int newExtent = Math.max(0, n);
        if(outerValue + newExtent > getMaximum()) {
            newExtent = getMaximum() - outerValue;
        }
        setRangeProperties(getInnerValue(), outerValue, newExtent, getMinimum(), getMaximum(), getValueIsAdjusting());
    }


    /**
     * Sets the minimum to <I>n</I> after ensuring that <I>n</I>
     * that the other three properties obey the model's constraints:
     * <pre>
     *     minimum &lt;= value &lt;= value+extent &lt;= maximum
     * </pre>
     * @see #getMinimum
     * @see BoundedRangeModel#setMinimum
     */
    public void setMinimum(int n) {
        int newMax = Math.max(n, getMaximum());
        int newInnerValue = Math.max(n, getInnerValue());
	    int newOuterValue = Math.max(newInnerValue, outerValue);
        int newExtent = Math.min(newMax - newOuterValue, getExtent());
        setRangeProperties(newInnerValue, newOuterValue, newExtent, n, newMax, getValueIsAdjusting());
    }


    /**
     * Sets the maximum to <I>n</I> after ensuring that <I>n</I>
     * that the other three properties obey the model's constraints:
     * <pre>
     *     minimum &lt;= value &lt;= value+extent &lt;= maximum
     * </pre>
     * @see BoundedRangeModel#setMaximum
     */
    public void setMaximum(int n) {
        int newMin = Math.min(n, getMinimum());
        int newExtent = Math.min(n - newMin, getExtent());
        int newOuterValue = Math.min(n - newExtent, outerValue);
	    int newInnerValue = Math.min(getInnerValue(), newOuterValue);
        setRangeProperties(newInnerValue, newOuterValue, newExtent, newMin, n, getValueIsAdjusting());
    }


    /**
     * Sets the <code>valueIsAdjusting</code> property.
     *
     * @see #getValueIsAdjusting
     * @see #setInnerValue
     * @see #setOuterValue
     * @see BoundedRangeModel#setValueIsAdjusting
     */
    public void setValueIsAdjusting(boolean b) {
        setRangeProperties(getInnerValue(), outerValue, getExtent(), getMinimum(), getMaximum(), b);
    }


    /**
     * Sets all of the <code>BoundedRangeModel</code> properties after forcing
     * the arguments to obey the usual constraints:
     * <pre>
     *     minimum &lt;= value &lt;= value+extent &lt;= maximum
     * </pre>
     * <p>
     * At most, one <code>ChangeEvent</code> is generated.
     *
     * @see BoundedRangeModel#setRangeProperties
     * @see #setInnerValue
     * @see #setOuterValue
     * @see #setExtent
     * @see #setMinimum
     * @see #setMaximum
     * @see #setValueIsAdjusting
     */
    public void setRangeProperties(int newInnerValue, int newOuterValue, int newExtent, int newMin, int newMax, boolean adjusting) {
	    if(newInnerValue > newOuterValue) {
		    newOuterValue = newInnerValue;
	    }

        if (newOuterValue > newMax) {
	        newMax = newOuterValue;
        }

	    if (newInnerValue < newMin) {
		    newMin = newInnerValue;
	    }

		/* Convert the addends to long so that extent can be
		 * Integer.MAX_VALUE without rolling over the sum.
		 * A JCK test covers this, see bug 4097718.
		 */
        if (((long)newExtent + (long)newOuterValue) > newMax) {
            newExtent = newMax - newOuterValue;
		}

        if (newExtent < 0) {
            newExtent = 0;
		}

        boolean isChange =
            (newInnerValue != getInnerValue()) ||
            (newOuterValue != outerValue) ||
            (newExtent != getExtent()) ||
            (newMin != getMinimum()) ||
            (newMax != getMaximum()) ||
            (adjusting != getValueIsAdjusting());

        if (isChange) {
	        int tempOut = outerValue;
            outerValue = newOuterValue;
	        super.setRangeProperties(newInnerValue, newExtent, newMin, newMax, adjusting);
	        if(tempOut != outerValue) {
		        fireStateChanged();
	        }
        }
    }


    /**
     * Returns a string that displays all of the
     * <code>BoundedRangeModel</code> properties.
     */
    public String toString()  {
        String modelString =
            "inner value=" + getInnerValue() + ", " +
            "outer value=" + getOuterValue() + ", " +
            "extent=" + getExtent() + ", " +
            "min=" + getMinimum() + ", " +
            "max=" + getMaximum() + ", " +
            "adj=" + getValueIsAdjusting();

        return getClass().getName() + "[" + modelString + "]";
    }   
}
