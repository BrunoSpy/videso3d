package org.jdesktop.swingx.multislider;

import javax.swing.*;
import javax.accessibility.*;

import org.jdesktop.swingx.multislider.plaf.MultiSliderAddon;
import org.jdesktop.swingx.multislider.plaf.MultiSliderUI;
import org.jdesktop.swingx.plaf.LookAndFeelAddons;
import org.jdesktop.swingx.plaf.basic.BasicMultiSliderUI;


/**
 * @author Arash Nikkar
 */
public class JXMultiSlider extends JSlider {

	/**
     * @see #getUIClassID
     * @see #readObject
     */
    public static final String uiClassID = "MultiSliderUI";
    
	static {
        LookAndFeelAddons.contribute(new MultiSliderAddon());
    }

    /**
     * The data model that handles the numeric maximum value,
     * minimum value, and current-position value for the slider.
     */
    protected JXMultiBoundedRangeModel sliderModel;


    /**
     * Creates a horizontal slider with the range 0 to 100 and
     * an initial value of 50.
     */
    public JXMultiSlider() {
        this(HORIZONTAL, 0, 100, 25, 75);
    }


    /**
     * Creates a slider using the specified orientation with the
     * range {@code 0} to {@code 100} and an initial value of {@code 50}.
     * The orientation can be
     * either <code>SwingConstants.VERTICAL</code> or
     * <code>SwingConstants.HORIZONTAL</code>.
     *
     * @param  orientation  the orientation of the slider
     * @throws IllegalArgumentException if orientation is not one of {@code VERTICAL}, {@code HORIZONTAL}
     * @see #setOrientation
     */
    public JXMultiSlider(int orientation) {
        this(orientation, 0, 100, 25, 75);
    }


    /**
     * Creates a horizontal slider using the specified min and max
     * with an initial value equal to the average of the min plus max.
     * <p>
     * The <code>BoundedRangeModel</code> that holds the slider's data
     * handles any issues that may arise from improperly setting the
     * minimum and maximum values on the slider.  See the
     * {@code BoundedRangeModel} documentation for details.
     *
     * @param min  the minimum value of the slider
     * @param max  the maximum value of the slider
     *
     * @see BoundedRangeModel
     * @see #setMinimum
     * @see #setMaximum
     */
    public JXMultiSlider(int min, int max) {
        this(HORIZONTAL, min, max, (int)((min + max) * 0.25f), (int)((min+max) / 0.75f));
    }


    /**
     * Creates a horizontal slider using the specified min, max and value.
     * <p>
     * The <code>BoundedRangeModel</code> that holds the slider's data
     * handles any issues that may arise from improperly setting the
     * minimum, initial, and maximum values on the slider.  See the
     * {@code BoundedRangeModel} documentation for details.
     *
     * @param min  the minimum value of the slider
     * @param max  the maximum value of the slider
     * @param innerValue  the initial inner value of the slider
     * @param outerValue the initial outer value of the slider
     *
     * @see BoundedRangeModel
     * @see #setMinimum
     * @see #setMaximum
     * @see #setInnerValue
     * @see #setOuterValue
     */
    public JXMultiSlider(int min, int max, int innerValue, int outerValue) {
        this(HORIZONTAL, min, max, innerValue, outerValue);
    }


    /**
     * Creates a slider with the specified orientation and the
     * specified minimum, maximum, and initial values.
     * The orientation can be
     * either <code>SwingConstants.VERTICAL</code> or
     * <code>SwingConstants.HORIZONTAL</code>.
     * <p>
     * The <code>BoundedRangeModel</code> that holds the slider's data
     * handles any issues that may arise from improperly setting the
     * minimum, initial, and maximum values on the slider.  See the
     * {@code BoundedRangeModel} documentation for details.
     *
     * @param orientation  the orientation of the slider
     * @param min  the minimum value of the slider
     * @param max  the maximum value of the slider
     * @param innerValue  the initial inner value of the slider
     * @param outerValue the initial outer value of the slider
     *
     * @throws IllegalArgumentException if orientation is not one of {@code VERTICAL}, {@code HORIZONTAL}
     *
     * @see BoundedRangeModel
     * @see #setOrientation
     * @see #setMinimum
     * @see #setMaximum
     * @see #setInnerValue
     * @see #setOuterValue
     */
    public JXMultiSlider(int orientation, int min, int max, int innerValue, int outerValue) {
        super(orientation, min, max, innerValue);
        sliderModel = new JXMultiBoundedRangeModel(innerValue, outerValue, 0, min, max);
        sliderModel.addChangeListener(changeListener);
        updateUI();
    }


    /**
     * Creates a horizontal slider using the specified
     * BoundedRangeModel.
     *
     * @param  brm MultiBoundedRange
     */
    public JXMultiSlider(JXMultiBoundedRangeModel brm) {
        this.orientation = JSlider.HORIZONTAL;
        setModel(brm);
        sliderModel.addChangeListener(changeListener);
        updateUI();
    }


    /**
     * Gets the UI object which implements the L&F for this component.
     *
     * @return the SliderUI object that implements the Slider L&F
     */
    public BasicMultiSliderUI getUI() {
        return(BasicMultiSliderUI)ui;
    }


    /**
     * Sets the UI object which implements the L&F for this component.
     *
     * @param ui the SliderUI L&F object
     * @see UIDefaults#getUI
     * @beaninfo
     *        bound: true
     *       hidden: true
     *    attribute: visualUpdate true
     *  description: The UI object that implements the slider's LookAndFeel.
     */
    public void setUI(BasicMultiSliderUI ui) {
        super.setUI(ui);
    }


    /**
     * Resets the UI property to a value from the current look and feel.
     *
     * @see JComponent#updateUI
     */
    public void updateUI() {
        setUI((BasicMultiSliderUI) LookAndFeelAddons.getUI(this, MultiSliderUI.class));
        
	    // The labels preferred size may be derived from the font
        // of the slider, so we must update the UI of the slider first, then
        // that of labels.  This way when setSize is called the right
        // font is used.
        updateLabelUIs();
    }


    /**
     * Returns the name of the L&F class that renders this component.
     *
     * @return "SliderUI"
     * @see JComponent#getUIClassID
     * @see UIDefaults#getUI
     */
    public String getUIClassID() {
        return uiClassID;
    }

    /**
     * Returns the {@code BoundedRangeModel} that handles the slider's three
     * fundamental properties: minimum, maximum, value.
     *
     * @return the data model for this component
     * @see #setModel
     * @see    JXMultiBoundedRangeModel
     */
    public JXMultiBoundedRangeModel getModel() {
	    if(sliderModel == null) {
		    return new JXMultiBoundedRangeModel(25,75,0,0,100); //this is needed to avoid NPEs in BasicSliderUI.installUI();
	    }
        return sliderModel;
    }


    /**
     * Sets the {@code BoundedRangeModel} that handles the slider's three
     * fundamental properties: minimum, maximum, value.
     *<p>
     * Attempts to pass a {@code null} model to this method result in
     * undefined behavior, and, most likely, exceptions.
     *
     * @param  newModel the new, {@code non-null} <code>BoundedRangeModel</code> to use
     *
     * @see #getModel
     * @see    BoundedRangeModel
     * @beaninfo
     *       bound: true
     * description: The sliders BoundedRangeModel.
     */
    public void setModel(JXMultiBoundedRangeModel newModel) {
        JXMultiBoundedRangeModel oldModel = getModel();

        if (oldModel != null) {
            oldModel.removeChangeListener(changeListener);
        }

        sliderModel = newModel;

        if (newModel != null) {
            newModel.addChangeListener(changeListener);

            if (accessibleContext != null) {
                accessibleContext.firePropertyChange(
                                                    AccessibleContext.ACCESSIBLE_VALUE_PROPERTY,
                                                    (oldModel == null
                                                     ? null : new Integer(oldModel.getInnerValue())),     //todo: also need to fire outerValue, and differntiate the two
                                                    (newModel == null
                                                     ? null : new Integer(newModel.getInnerValue())));
            }
        }

        firePropertyChange("model", oldModel, sliderModel);
    }


    /**
     * Returns the slider's current value
     * from the {@code BoundedRangeModel}.
     *
     * @return  the current value of the slider
     * @see     #setInnerValue
     * @see     BoundedRangeModel#getValue
     */
    public int getInnerValue() {
        return getModel().getInnerValue();
    }

	/**
     * Returns the slider's current value
     * from the {@code BoundedRangeModel}.
     *
     * @return  the current value of the slider
     * @see     #setOuterValue
     * @see     BoundedRangeModel#getValue
     */
    public int getOuterValue() {
        return getModel().getOuterValue();
    }

    /**
     * Sets the slider's current value to {@code n}.  This method
     * forwards the new value to the model.
     * <p>
     * The data model (an instance of {@code BoundedRangeModel})
     * handles any mathematical
     * issues arising from assigning faulty values.  See the
     * {@code BoundedRangeModel} documentation for details.
     * <p>
     * If the new value is different from the previous value,
     * all change listeners are notified.
     *
     * @param   n       the new value
     * @see     #getInnerValue
     * @see     #addChangeListener
     * @see     BoundedRangeModel#setValue
     * @beaninfo
     *   preferred: true
     * description: The sliders current value.
     */
    public void setInnerValue(int n) {
        JXMultiBoundedRangeModel m = getModel();
        int oldValue = m.getInnerValue();
        if (oldValue == n) {
            return;
        }
        m.setInnerValue(n);

        if (accessibleContext != null) {
            accessibleContext.firePropertyChange(
                                                AccessibleContext.ACCESSIBLE_VALUE_PROPERTY,
                                                new Integer(oldValue),
                                                new Integer(m.getInnerValue()));
        }
    }

	/**
     * Sets the slider's current value to {@code n}.  This method
     * forwards the new value to the model.
     * <p>
     * The data model (an instance of {@code BoundedRangeModel})
     * handles any mathematical
     * issues arising from assigning faulty values.  See the
     * {@code BoundedRangeModel} documentation for details.
     * <p>
     * If the new value is different from the previous value,
     * all change listeners are notified.
     *
     * @param   n       the new value
     * @see     #getOuterValue
     * @see     #addChangeListener
     * @see     BoundedRangeModel#setValue
     * @beaninfo
     *   preferred: true
     * description: The sliders current value.
     */
    public void setOuterValue(int n) {
        JXMultiBoundedRangeModel m = getModel();
        int oldValue = m.getOuterValue();
        if (oldValue == n) {
            return;
        }
        m.setOuterValue(n);

        if (accessibleContext != null) {
            accessibleContext.firePropertyChange(
                                                AccessibleContext.ACCESSIBLE_VALUE_PROPERTY,
                                                new Integer(oldValue),
                                                new Integer(m.getOuterValue()));
        }
    }

}

