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

package fr.crnan.videso3d.layers;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.GeographicText;
import gov.nasa.worldwind.render.GeographicTextRenderer;
import gov.nasa.worldwind.util.Logging;
/**
 * Layer support pour des objets de type {@link GeographicText}
 * @author Bruno Spyckerelle
 * @version 0.1.2
 */
public class TextLayer extends AbstractLayer {

	private final Collection<GeographicText> geographicTexts = new ConcurrentLinkedQueue<GeographicText>();
	private GeographicTextRenderer textRenderer = new GeographicTextRenderer();
	
	
	/**
	 * @param string Nom du layer
	 */
	public TextLayer(String string) {
		this.setName(string);
	}

	/**
	 * Adds the specified <code>text</code> to this layer's internal collection.
	 * @param text {@link GeographicText} to add
	 * @throws IllegalArgumentException If <code>text</code> is null.
	 */
	public void addGeographicText(GeographicText text){
		if (text == null){
            String msg = "nullValue.GeographicTextIsNull";
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
		this.geographicTexts.add(text);
	}
	
	public void addGeographicTexts(Iterable<? extends GeographicText> texts){
		if (texts == null)
        {
            String msg = Logging.getMessage("nullValue.IterableIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
		for(GeographicText text : texts){
			if(text != null) this.geographicTexts.add(text);
		}
	}
	
	public void removeGeographicText(GeographicText text){
		if (text == null){
            String msg = "nullValue.GeographicTextIsNull";
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
		this.geographicTexts.remove(text);
	}
	
	public void removeAllGeographicTexts() {
		this.geographicTexts.clear();
	}
	
	public Iterable<GeographicText> getActiveGeographicTexts(){
		return this.geographicTexts;
	}
	
	
	@Override
	protected void doRender(DrawContext dc) {
		this.textRenderer.render(dc, getActiveGeographicTexts());
	}

	

}
