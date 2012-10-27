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

package fr.crnan.videso3d.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.event.RenderingEvent;
import gov.nasa.worldwindx.examples.util.ScreenShotAction;
/**
 * Takes a screenshot and adds a watermark
 * @author Bruno Spyckerelle
 * @version 0.1.0
 */
public class VScreenShotAction extends ScreenShotAction {


	public VScreenShotAction(WorldWindow wwd) {
		super(wwd);
	}


	@Override
	public void stageChanged(RenderingEvent event) {

		super.stageChanged(event);
		if (event.getStage().equals(RenderingEvent.AFTER_BUFFER_SWAP) && this.snapFile != null){
			
			BufferedImage img = null;
			try {
				img = ImageIO.read(this.snapFile);
				Graphics2D g2 = img.createGraphics();
				g2.setColor(Color.white);
				g2.setFont(new Font("Serif", Font.BOLD, 18));

				g2.drawString("Rendered with Videso 3D", img.getWidth()-250, 20);

				ImageIO.write(img, "png", this.snapFile);
			} catch (IOException e) {
				e.printStackTrace();
			} 

			this.snapFile = null;
		}
	}


}
