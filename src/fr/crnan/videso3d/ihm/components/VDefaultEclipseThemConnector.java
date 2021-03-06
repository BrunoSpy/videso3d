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
package fr.crnan.videso3d.ihm.components;

import bibliothek.extension.gui.dock.theme.eclipse.DefaultEclipseThemeConnector;
import bibliothek.gui.DockStation;
import bibliothek.gui.Dockable;
import bibliothek.gui.dock.common.intern.DefaultCommonDockable;
/**
 * Eclipse theme for Dockable with optional borders
 * @author Bruno Spyckerelle
 * @version 0.1.0
 */
public class VDefaultEclipseThemConnector extends DefaultEclipseThemeConnector {

	
	/* (non-Javadoc)
	 * @see bibliothek.extension.gui.dock.theme.eclipse.DefaultEclipseThemeConnector#getTitleBarKind(bibliothek.gui.DockStation, bibliothek.gui.Dockable)
	 */
	@Override
	public TitleBar getTitleBarKind(DockStation parent, Dockable dockable) {
		if(dockable instanceof DefaultCommonDockable){
			if(!((DefaultCommonDockable) dockable).getDockable().isTitleShown()){
				return TitleBar.NONE;
			}
		}
		return super.getTitleBarKind(parent, dockable);
	}

	
	
}
