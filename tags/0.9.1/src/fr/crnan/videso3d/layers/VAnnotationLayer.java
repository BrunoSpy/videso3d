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

import gov.nasa.worldwind.layers.AnnotationLayer;
import gov.nasa.worldwind.render.Annotation;
import gov.nasa.worldwind.util.Logging;
/**
 * Extension de AnnotationLayer qui s'assure qu'une annotation ne peut pas être ajoutée deux fois.
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class VAnnotationLayer extends AnnotationLayer {

	public void addAnnotation(Annotation annotation){
		if (annotation == null)
		{
			String msg = Logging.getMessage("nullValue.AnnotationIsNull");
			Logging.logger().severe(msg);
			throw new IllegalArgumentException(msg);
		}
		if (this.annotationsOverride != null)
		{
			String msg = Logging.getMessage("generic.LayerIsUsingCustomIterable");
			Logging.logger().severe(msg);
			throw new IllegalStateException(msg);
		}
		if(!this.annotations.contains(annotation)){
			this.annotations.add(annotation);
		} 
	}

   public Boolean contains(Annotation annotation){
	   if (annotation == null)
		{
			String msg = Logging.getMessage("nullValue.AnnotationIsNull");
			Logging.logger().severe(msg);
			throw new IllegalArgumentException(msg);
		}
		if(this.annotationsOverride != null){
			for(Annotation a : this.annotationsOverride){
				if(annotation == a) return true; 
			}
			return false;
		} else {
			return this.annotations.contains(annotation);
		}
   }
	
}
