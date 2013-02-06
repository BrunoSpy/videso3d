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


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;

import org.jdesktop.swingx.JXTreeTable;

import fr.crnan.videso3d.DatasManager;
import fr.crnan.videso3d.DatasManager.Type;
import fr.crnan.videso3d.UserObjectsController;
import fr.crnan.videso3d.databases.edimap.Carte;
import fr.crnan.videso3d.formats.images.EditableSurfaceImage;
import fr.crnan.videso3d.graphics.Balise2D;
import fr.crnan.videso3d.graphics.Balise3D;
import fr.crnan.videso3d.graphics.VidesoObject;
import fr.crnan.videso3d.ihm.components.DataView;
import fr.crnan.videso3d.ihm.components.FilteredTreeTableNode;
import fr.crnan.videso3d.ihm.components.FilteredTreeTableModel;
import fr.crnan.videso3d.ihm.components.RegexViewFilter;
import fr.crnan.videso3d.ihm.components.UserObjectNode;
import fr.crnan.videso3d.ihm.components.UserObjectTreeTableModel;
import fr.crnan.videso3d.kml.KMLMutableTreeNode;
import fr.crnan.videso3d.project.Project;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.ogc.kml.KMLRoot;
import gov.nasa.worldwind.render.GeographicText;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.airspaces.Airspace;
/**
 * Panel for the Data Explorer showing all objects created by the user either by importing a project or by using the {@link DrawToolbar}<br />
 * For user generated objects, we use the type field as an id of the object.<br />
 * A view has to be created before the controller.
 * @author Bruno Spyckerelle
 * @version 0.1.0
 */
public class UserObjectsView extends JPanel implements DataView {

	private UserObjectTreeTableModel rootModel;
	private JPanel tablePanel;
	
	private JTextField filtre = new JTextField(20);
	private JPanel panel = new JPanel();
	
	
	private HashMap<Integer, UserObjectNode> objects = new HashMap<Integer, UserObjectNode>();
	
	private JXTreeTable treeTable;
	
	public UserObjectsView(){
		
		rootModel = new UserObjectTreeTableModel(new DefaultMutableTreeNode("root"));
		
		//build IHM
		//in fact it is an adaptation of FilteredMultiTreeTableView
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));


		panel.setLayout(new BorderLayout());

		//ajout d'un filtre
		JPanel filterPanel = new JPanel();
		filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.X_AXIS));
		filterPanel.add(new Label("Filtre : "));
		filterPanel.add(filtre);

		panel.add(filterPanel, BorderLayout.NORTH);

		tablePanel = new JPanel();
		tablePanel.setLayout(new BorderLayout());
		tablePanel.setBorder(BorderFactory.createTitledBorder(""));
		
		panel.add(tablePanel, BorderLayout.CENTER);
		
		this.add(panel);

		treeTable = new JXTreeTable();
		
		//set the model
		rootModel.addTreeModelListener(new TreeModelListener() {

			@Override
			public void treeNodesChanged(TreeModelEvent e) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)e.getTreePath().getLastPathComponent();
				FilteredTreeTableNode source = (FilteredTreeTableNode)node.getUserObject();
				if(node.isLeaf()){
					//if leaf, source instanceof UserObjectNode
					if(source instanceof UserObjectNode){
						if(source.isVisible()){
							getController().showObject(((UserObjectNode) source).getId(), source.getName());
						} else {
							getController().hideObject(((UserObjectNode) source).getId(), source.getName());
						}
					} else if(source instanceof KMLMutableTreeNode){
						//TODO faire ça côté controleur, nécessite de revoir tout le controleur cependant...
						((KMLMutableTreeNode) source).getFeature().setVisibility(source.isVisible());
						getController().refreshKML();
					}
				}
			}

			@Override
			public void treeNodesInserted(TreeModelEvent evt) {
				treeTable.expandAll();
			}

			@Override
			public void treeNodesRemoved(TreeModelEvent evt) {
				treeTable.expandAll();
			}

			@Override
			public void treeStructureChanged(TreeModelEvent evt) {
				treeTable.expandAll();
			}
			
		});
	
		treeTable.setTableHeader(null);
		treeTable.setRootVisible(false);
		treeTable.setTreeTableModel(rootModel);
		treeTable.setOpaque(false);
		treeTable.setBackground(new Color(214, 217, 223));
		treeTable.getColumnExt(1).setMaxWidth(15);
		treeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		//Ajout du filtre
		filtre.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(filtre.getText().isEmpty()){
					((FilteredTreeTableModel) rootModel).setViewFilter(null);
				} else {
					((FilteredTreeTableModel) rootModel).setViewFilter(new RegexViewFilter(filtre.getText()));
				}
			}
		});	

		//update wwd with action on the table
		treeTable.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseClicked(MouseEvent e){
				
				int row = treeTable.rowAtPoint(e.getPoint());  
				if(row != -1){
					Object[] path = treeTable.getPathForRow(row).getPath();
					DefaultMutableTreeNode node = (DefaultMutableTreeNode)path[path.length-1];

					if(node.getUserObject() instanceof UserObjectNode && node.isLeaf()){
						if(e.getButton() == MouseEvent.BUTTON1 && e.getClickCount()==2){

						} else if(e.getButton() == MouseEvent.BUTTON1 && e.getClickCount()==1 ){
							//getController().highlight(((UserObjectNode)node.getUserObject()).getId(), ((UserObjectNode)node.getUserObject()).getName());
						} else if(e.getButton() == MouseEvent.BUTTON2 && e.getClickCount()==1 ){
							//Menu pour supprimer/éditer
							//D'abord refactoriser les menus avant de faire cette fonction
						}
					}
				}
			}

		});
		
		JScrollPane scrollPane = new JScrollPane(treeTable);
		scrollPane.setBorder(null);
		tablePanel.add(scrollPane);
		
	}
	
	@Override
	public UserObjectsController getController() {
		return (UserObjectsController) DatasManager.getController(Type.UserObject);
	}

	@Override
	public void showObject(int type, String name) {
		//TODO synchroniser vue si modif directe de la vue 3D
	}

	@Override
	public void hideObject(int type, String name) {
		//TODO synchroniser vue si modif directe de la vue 3D
	}
	
	public void addProject(Project project){
		DefaultMutableTreeNode projectNode = new DefaultMutableTreeNode(new FilteredTreeTableNode(project.getName(), true));
		if(!project.getCartes().isEmpty()){
			DefaultMutableTreeNode cartesNode = new DefaultMutableTreeNode(new FilteredTreeTableNode("Cartes", true));
			projectNode.add(cartesNode);
			for(Carte c : project.getCartes()){
				cartesNode.add(new DefaultMutableTreeNode(new UserObjectNode(c.getName(), getController().getID(c), true)));
			}
		}
		if(!project.getAirspaces().isEmpty()){
			DefaultMutableTreeNode airspacesNode = new DefaultMutableTreeNode(new FilteredTreeTableNode("Airspaces", true));
			projectNode.add(airspacesNode);
			for(Airspace a : project.getAirspaces()){
				if(a instanceof VidesoObject){
					airspacesNode.add(new DefaultMutableTreeNode(new UserObjectNode(((VidesoObject) a).getName(), getController().getID(a), true)));
				} else {
					throw new IllegalArgumentException();
				}
			}
		}
		if(!project.getRenderables().isEmpty()){
			DefaultMutableTreeNode renderablesNode = new DefaultMutableTreeNode(new FilteredTreeTableNode("Renderables", true));
			projectNode.add(renderablesNode);
			for(Renderable r : project.getRenderables()){
				if(r instanceof VidesoObject){
					renderablesNode.add(new DefaultMutableTreeNode(new UserObjectNode(((VidesoObject) r).getName(), getController().getID(r), true)));
				} else {
					throw new IllegalArgumentException();
				}
			}
		}
		if(!project.getBalises2D().isEmpty() || !project.getBalises3D().isEmpty()){
			DefaultMutableTreeNode balisesNode = new DefaultMutableTreeNode(new FilteredTreeTableNode("Balises", true));
			projectNode.add(balisesNode);
			for(Balise2D b : project.getBalises2D()){
				balisesNode.add(new DefaultMutableTreeNode(new UserObjectNode(b.getName(), getController().getID(b), true)));
			}
			for(Balise3D b : project.getBalises3D()){
				balisesNode.add(new DefaultMutableTreeNode(new UserObjectNode(b.getName(), getController().getID(b), true)));
			}
		}
		if(!project.getTexts().isEmpty()){
			DefaultMutableTreeNode textsNode = new DefaultMutableTreeNode(new FilteredTreeTableNode("Texts", true));
			projectNode.add(textsNode);
			for(GeographicText t : project.getTexts()){
				textsNode.add(new DefaultMutableTreeNode(new UserObjectNode(t.getText().toString(), getController().getID(t), true)));
			}
		}
		if(!project.getImages().isEmpty()){
			DefaultMutableTreeNode imagesNode = new DefaultMutableTreeNode(new FilteredTreeTableNode("Images", true));
			projectNode.add(imagesNode);
			for(EditableSurfaceImage i : project.getImages()){
				imagesNode.add(new DefaultMutableTreeNode(new UserObjectNode((String) i.getName(),getController().getID(i), true)));
			}
		}
		if(!project.getLayers().isEmpty()){
			DefaultMutableTreeNode layersNode = new DefaultMutableTreeNode(new FilteredTreeTableNode("Calques", true));
			projectNode.add(layersNode);
			for(Layer l : project.getLayers()){
				//TODO changer la façon de gérer les calques pour ajouter le support du nom
				int id = getController().getID(l);
				layersNode.add(new DefaultMutableTreeNode(new UserObjectNode("Calque "+id, id, true)));
			}
		}
		
		rootModel.addProject(projectNode);
		
		treeTable.validate();
	}

	public void addKML(KMLRoot kml){
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) KMLMutableTreeNode.fromKMLFeature(kml.getFeature());
		rootModel.addKMLNode(node);
		treeTable.validate();
	}
	
	/**
	 * Add a user object, not a project
	 * @param o
	 */
	public void addObject(VidesoObject o){
		UserObjectNode node = new UserObjectNode(o.getName(), getController().getID(o), true);
		objects.put(node.getId(), node);
		rootModel.addObjectNode(new DefaultMutableTreeNode(node));	
	}
	
	public void remove(Object o){
		//TODO la suppression d'un objet a des effets de bords (liste des objets non mise à jour)
	}
	
	@Override
	public void reset() {
		
	}

}
