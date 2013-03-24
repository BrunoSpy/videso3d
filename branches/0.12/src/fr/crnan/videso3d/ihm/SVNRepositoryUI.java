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

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTree;

import fr.crnan.videso3d.ihm.components.TitledPanel;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.BorderLayout;

/**
 * Fenêtre affichant l'arboresence du dépôt SVN sélectionné
 * @author Adrien Vidal
 * @author Bruno SPyckerelle
 */
public class SVNRepositoryUI extends JDialog{
	
	private static final long serialVersionUID = -2707712944901661771L;
	private Color background = new Color(230,230,230);

	private String path = "";
	
	int result = JOptionPane.CANCEL_OPTION;
	
	public SVNRepositoryUI(DefaultTreeModel svnTreeModel){
		this.setTitle("SVN");
		
		this.setModal(true);
		
		TitledPanel title = new TitledPanel("Arborescence du dépôt");
		getContentPane().add(title, BorderLayout.NORTH);
		
		final JTree tree = new JTree(svnTreeModel);
				
		tree.setRootVisible(false);
		DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer(){
			@Override 
			public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
				super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
				this.setBackgroundNonSelectionColor(background);
				if(leaf){
					this.setIcon(null);
					this.setForeground(Color.BLUE);
					setText("<html>&#8227 <u>"+getText()+"</u></html>");
				}
				return this;
			}
		};
		tree.setCellRenderer(renderer);
		tree.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent e){
				TreePath treePath = tree.getPathForLocation(e.getX(), e.getY());
				if(treePath!=null){
					String svnPath = "";
					for(Object o : treePath.getPath()){
						svnPath+="/"+o;
					}
					svnPath = svnPath.substring(1);
					SVNRepositoryUI.this.path = svnPath;
					result = JOptionPane.OK_OPTION;
					setVisible(false);
				}
			}
		});
		for(int i=0; i<tree.getRowCount();i++){
			tree.expandRow(i);
		}
		tree.addMouseMotionListener(new MouseMotionAdapter() {
		    @Override
		    public void mouseMoved(MouseEvent e) {
		        int x = (int) e.getPoint().getX();
		        int y = (int) e.getPoint().getY();
		        TreePath path = tree.getPathForLocation(x, y);
		        if (path == null) {
		        	setCursor(Cursor.getDefaultCursor());
		        } else {
		        	if(path.getLastPathComponent() instanceof DefaultMutableTreeNode){
		        		if(((DefaultMutableTreeNode)path.getLastPathComponent()).isLeaf()){
		        			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		        		}else{
		        			setCursor(Cursor.getDefaultCursor());
		        		}
		        	}
		        }
		    }
		});
		tree.setBackground(background);
		for(Component c : tree.getComponents()){
			c.setBackground(background);
		}
		
		JScrollPane scroll = new JScrollPane(tree);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		getContentPane().add(scroll);


		this.setPreferredSize(new Dimension(250,300));
		this.pack();	

	}
	
	
	
	public int showDialog(Component parent){
		this.setLocationRelativeTo(parent);

		this.setVisible(true);
		return result;
	}
	
	public String getSVNPath(){
		return this.path;
	}
}
