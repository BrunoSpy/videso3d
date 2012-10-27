package fr.crnan.videso3d.ihm.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.border.Border;

import fr.crnan.videso3d.Pallet;
/**
 * 
 * @author SwingSet3
 */
public class ChiselBorder implements Border {

	private Insets insets = new Insets(1, 0, 1, 0);

	@Override
	public Insets getBorderInsets(Component c) {
		return insets;
	}
	@Override
	public boolean isBorderOpaque() {
		return true;
	}
	@Override
	public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
		Color color = c.getBackground();
		// render highlight at top
		g.setColor(Pallet.deriveColorHSB(color, 0, 0, .2f));
		g.drawLine(x, y, x + width, y);
		// render shadow on bottom
		g.setColor(Pallet.deriveColorHSB(color, 0, 0, -.2f));
		g.drawLine(x, y + height - 1, x + width, y + height - 1);
	}



}
