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


import javax.swing.tree.DefaultMutableTreeNode;

import fr.crnan.videso3d.DatasManager;
import fr.crnan.videso3d.DatasManager.Type;
import fr.crnan.videso3d.UserObjectsController;
import fr.crnan.videso3d.ihm.components.FilteredMultiTreeTableView;
import fr.crnan.videso3d.ihm.components.FilteredTreeTableModel;
import fr.crnan.videso3d.project.Project;
/**
 * Panel for the Data Explorer showing all objects created by the user either by importing a project or by using the {@link DrawToolbar}<br />
 * For user generated objects, we use the type field as an id of the object.
 * @author Bruno Spyckerelle
 * @version 0.1.0
 */
public class UserObjectsView extends FilteredMultiTreeTableView {

	private FilteredTreeTableModel rootModel;
	
	public UserObjectsView(){
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
		
	}
	
	@Override
	public UserObjectsController getController() {
		return (UserObjectsController) DatasManager.getController(Type.UserObject);
	}

	@Override
	public void showObject(int type, String name) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void hideObject(int type, String name) {
		// TODO Auto-generated method stub
		
	}

	public void addProject(Project project){
		
	}
	
	
	
}
