/*
 * This file is part of ViDESO.
 * ViDESO is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ViDESO is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ViDESO.  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.crnan.videso3d.ihm;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
/**
 * Panel de titre<br />
 * Idée trouvée dans SwingSet3
 * @author Bruno Spyckerelle
 * @version 0.1
 */
@SuppressWarnings("serial")
public class TitledPanel extends JPanel {

	public TitledPanel(String title) {
		this.setLayout(new BorderLayout());
		//TODO Utiliser la palette
		Color titleColor = new Color(51, 98, 140);
		float hsb[] = Color.RGBtoHSB(
                titleColor.getRed(), titleColor.getGreen(), titleColor.getBlue(), null);
        GradientPanel titlePanel = new GradientPanel(
                Color.getHSBColor(hsb[0]-.013f, .15f, .85f),
                Color.getHSBColor(hsb[0]-.005f, .24f, .80f));
        titlePanel.setLayout(new BorderLayout());
        titlePanel.setBorder(new CompoundBorder(
                new ChiselBorder(), new EmptyBorder(6,8,6,0)));
        JLabel demoListLabel = new JLabel(title);
        demoListLabel.setFont(demoListLabel.getFont().deriveFont(Font.BOLD));
        demoListLabel.setOpaque(false);
        demoListLabel.setHorizontalAlignment(JLabel.LEADING);
        titlePanel.add(demoListLabel, BorderLayout.CENTER);
        this.add(titlePanel, BorderLayout.NORTH);
	}
	
}
