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

import java.sql.SQLException;

import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.DatabaseManager.Type;
import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.action.predefined.CCloseAction;
import bibliothek.gui.dock.common.intern.CDockable;
/**
 * Dockable which tells the DatabaseManager when the user closes it
 * @author Bruno Spyckerelle
 * @version 0.1.0
 */
public class ClosableSingleDockable extends DefaultSingleCDockable {

	private Type type;
	
	private class VCloseAction extends CCloseAction {

		public VCloseAction(CControl control) {
			super(control);
		}

		/* (non-Javadoc)
		 * @see bibliothek.gui.dock.common.action.predefined.CCloseAction#close(bibliothek.gui.dock.common.intern.CDockable)
		 */
		@Override
		public void close(CDockable dockable) {
			super.close(dockable);
			try {
				DatabaseManager.unselectDatabase(((ClosableSingleDockable) dockable).getType());
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}		
	}
	
	public ClosableSingleDockable(String id) {
		super(id);
	}

	public void addCloseAction(CControl control){
		this.addAction(new VCloseAction(control));
	}
	
	public void setType(Type type) {
		this.type = type;
	}

	public Type getType() {
		return type;
	}
	
	
}
