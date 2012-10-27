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

import java.util.List;

import javax.swing.Action;
import javax.swing.Icon;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.table.ColumnControlButton;
/**
 * 
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class ColumnControl extends ColumnControlButton {

	public ColumnControl(JXTable table) {
		super(table);
	}
	
	public ColumnControl(JXTable table, Icon icon){
		super(table, icon);
	}

	public void addActions(List<? extends Action> actions){
		this.getColumnControlPopup().addAdditionalActionItems(actions);
	}
}
