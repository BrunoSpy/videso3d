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
package fr.crnan.videso3d.trajectography;

import java.util.Collection;

import javax.swing.event.TableModelListener;

import fr.crnan.videso3d.formats.VidesoTrack;

/**
 * Listener to TracksModel changes
 * @author Bruno Spyckerelle
 * @version 0.1.0
 */
public interface TracksModelListener extends TableModelListener{

	public void trackAdded(VidesoTrack track);
	
	public void trackRemoved(VidesoTrack track);
	
	public void trackAdded(Collection<VidesoTrack> track);
	
	public void trackRemoved(Collection<VidesoTrack> track);
	
	public void trackVisibilityChanged(VidesoTrack track, boolean visible);
	
	public void trackSelectionChanged(VidesoTrack track, boolean selected);
	
	public void trackVisibilityChanged(Collection<VidesoTrack> track, boolean visible);
	
	public void trackSelectionChanged(Collection<VidesoTrack> track, boolean selected);
}
