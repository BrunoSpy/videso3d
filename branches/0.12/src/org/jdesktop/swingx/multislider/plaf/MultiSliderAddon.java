package org.jdesktop.swingx.multislider.plaf;

import org.jdesktop.swingx.multislider.JXMultiSlider;
import org.jdesktop.swingx.plaf.AbstractComponentAddon;
import org.jdesktop.swingx.plaf.DefaultsList;
import org.jdesktop.swingx.plaf.LookAndFeelAddons;

/**
 * @author Arash Nikkar
 */
public class MultiSliderAddon extends AbstractComponentAddon {

	public MultiSliderAddon() {
		super("JXMultiSlider");
	}

    @Override
    protected void addBasicDefaults(LookAndFeelAddons addon, DefaultsList defaults) {
		defaults.add(JXMultiSlider.uiClassID, "org.jdesktop.swingx.plaf.basic.BasicMultiSliderUI");
    }

	@Override
	protected void addLinuxDefaults(LookAndFeelAddons addon, DefaultsList defaults) {
		addMetalDefaults(addon, defaults);
	}

	@Override
	protected void addMetalDefaults(LookAndFeelAddons addon, DefaultsList defaults) {
		super.addMetalDefaults(addon, defaults);
        defaults.add(JXMultiSlider.uiClassID, "org.jdesktop.swingx.plaf.metal.MetalMultiSliderUI");
	}

	@Override
	protected void addMotifDefaults(LookAndFeelAddons addon, DefaultsList defaults) {
		super.addMetalDefaults(addon, defaults);
        defaults.add(JXMultiSlider.uiClassID, "org.jdesktop.swingx.plaf.motif.MotifMultiSliderUI");
	}

    @Override
    protected void addWindowsDefaults(LookAndFeelAddons addon, DefaultsList defaults) {
        super.addWindowsDefaults(addon, defaults);
	    defaults.add(JXMultiSlider.uiClassID, "org.jdesktop.swingx.plaf.windows.WindowsMultiSliderUI");
    }

	/*
	@Override
	protected void addMacDefaults(LookAndFeelAddons addon, DefaultsList defaults) {
		super.addMacDefaults(addon, defaults);
		defaults.add(JXTaskPane.uiClassID, "org.jdesktop.swingx.plaf.misc.");
	}*/
}
