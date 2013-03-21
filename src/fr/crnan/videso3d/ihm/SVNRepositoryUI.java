package fr.crnan.videso3d.ihm;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;

import fr.crnan.videso3d.databases.SVNManager;
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
import javax.swing.tree.TreePath;
import java.awt.BorderLayout;

/**
 * Fenêtre affichant l'arboresence du dépôt SVN sélectionné
 * @author vidal
 *
 */
public class SVNRepositoryUI extends JDialog{
	private static final long serialVersionUID = -2707712944901661771L;
	private Color background = new Color(230,230,230);

	public SVNRepositoryUI(String repository, DatabaseManagerUI parent){
		super(parent,"SVN");
		SVNManager.initialize(repository, parent);
		
		TitledPanel title = new TitledPanel("Arborescence du dépôt");
		getContentPane().add(title, BorderLayout.NORTH);
		
		final JTree tree = new JTree(SVNManager.listEntries());
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
					SVNManager.getDatabase(svnPath, -1);
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
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

	}
}
