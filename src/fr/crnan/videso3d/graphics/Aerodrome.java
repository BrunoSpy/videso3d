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
package fr.crnan.videso3d.graphics;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.UserFacingText;
/**
 * 
 * @author Adrien Vidal
 * @version 2.1
 */
public interface Aerodrome extends VidesoObject{

	public Position getRefPosition();
	public UserFacingText getUserFacingText();
	public String getName();
	public String getAnnotationText();
	public String getNomPiste();
	public void setVisible(boolean b);
	public boolean isVisible();
}
