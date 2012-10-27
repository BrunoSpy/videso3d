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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import fr.crnan.videso3d.graphics.PriorityRenderable;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.Renderable;
/**
 * Renderable layer with managing of priorities for renderable objects with priority
 * @author Bruno Spyckerelle
 * @version 0.1.0
 */
public class PriorityRenderableLayer extends RenderableLayer {

	private Comparator<Renderable> comparator;
	
	public PriorityRenderableLayer(){
		super();
		comparator = new Comparator<Renderable>() {

			@Override
			public int compare(Renderable o1, Renderable o2) {
				if((o1 instanceof PriorityRenderable) && !(o2 instanceof PriorityRenderable))
					return 1;
				if(!(o1 instanceof PriorityRenderable) && !(o2 instanceof PriorityRenderable))
					return 0;
				if(!(o1 instanceof PriorityRenderable) && (o2 instanceof PriorityRenderable))
					return -1;
				if(o1 instanceof PriorityRenderable && o2 instanceof PriorityRenderable)
					return ((PriorityRenderable) o1).getPriority() - ((PriorityRenderable) o2).getPriority();
				return 0;
			}
		};
	}
	
	@Override
	public void addRenderable(Renderable renderable) {
		super.addRenderable(renderable);
		List<Renderable> renderables = new ArrayList<Renderable>();
		for(Renderable r : this.getRenderables()){
			renderables.add(r);
		}
		Collections.sort(renderables, comparator);
		super.removeAllRenderables();
		super.addRenderables(renderables);
	}

	@Override
	public void setRenderables(Iterable<Renderable> renderableIterable) {
		throw new UnsupportedOperationException("Method unavalaible");
	}

	
	
	
}
