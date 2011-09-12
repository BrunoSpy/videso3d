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
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.BoxLayout;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import java.awt.Color;
import javax.swing.JList;

import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.DatabaseManager.Type;
import fr.crnan.videso3d.ihm.components.DiffPanel;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JSplitPane;
import java.awt.GridLayout;
/**
 * Window to compare two versions of a file
 * @author Bruno Spyckerelle
 * @version 0.1.0
 */
public class FileCompareSelector extends JFrame {

	private final JPanel contentPanel = new JPanel();
	private JList listTypes;
	private JList listFiles;
	private JPanel basesPanel;
	private DiffPanel comparePanel;
	private File path2;
	private File path1;
	
	/**
	 * Create the dialog.
	 */
	public FileCompareSelector() {
		setTitle("Comparaison de fichiers");
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));
		
		contentPanel.setAlignmentY(Component.TOP_ALIGNMENT);
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPanel.setPreferredSize(new Dimension(0, 200));
		contentPanel.setLayout(new GridLayout(0, 3, 0, 0));

		JPanel typePanel = new JPanel();
		typePanel.setAlignmentX(0.3f);
		typePanel.setBorder(new TitledBorder(null, "Type de la base de donn\u00E9es", TitledBorder.CENTER, TitledBorder.ABOVE_TOP, null, new Color(59, 59, 59)));
		contentPanel.add(typePanel);
		typePanel.setLayout(new BorderLayout(0, 0));
		
		listTypes = new JList();
		listTypes.setOpaque(false);
		JScrollPane scrollTypes = new JScrollPane(listTypes);
		scrollTypes.setBorder(null);
		typePanel.add(scrollTypes);


		JPanel basesPanelContent = new JPanel();
		basesPanelContent.setAlignmentX(0.3f);
		basesPanelContent.setBorder(new TitledBorder(null, "Bases \u00E0 comparer", TitledBorder.CENTER, TitledBorder.ABOVE_TOP, null, null));
		basesPanelContent.setLayout(new BoxLayout(basesPanelContent, BoxLayout.Y_AXIS));
		
		basesPanel = new JPanel();
		basesPanel.setLayout(new BoxLayout(basesPanel, BoxLayout.Y_AXIS));
		
		JScrollPane scrollBases = new JScrollPane(basesPanel);
		basesPanelContent.add(scrollBases);
		scrollBases.setBorder(BorderFactory.createEmptyBorder());
		contentPanel.add(basesPanelContent);

		JPanel filesPanel = new JPanel();
		filesPanel.setAlignmentX(0.3f);
		filesPanel.setBorder(new TitledBorder(null, "Fichiers \u00E0 comparer", TitledBorder.CENTER, TitledBorder.ABOVE_TOP, null, null));
		contentPanel.add(filesPanel);
		filesPanel.setLayout(new BorderLayout(0, 0));
		
		listFiles = new JList();
		listFiles.setOpaque(false);
		listFiles.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listFiles.addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				
				if(((JList) e.getSource()).getModel().getSize()>e.getLastIndex()){
					for(int i = e.getFirstIndex();i<=e.getLastIndex();i++){
						if(((JList) e.getSource()).isSelectedIndex(i)){
							final String file = (String) ((JList) e.getSource()).getModel().getElementAt(i);
							try {
								comparePanel.compareFiles(new File(path1, file), new File(path2, file));
							} catch (IOException e1) {
								e1.printStackTrace();
							}
						}
					}
				} else {
					comparePanel.clear();
				}
			}
		});
		JScrollPane scrollFiles = new JScrollPane(listFiles);
		scrollFiles.setBorder(null);
		filesPanel.add(scrollFiles);
	
		comparePanel = new DiffPanel(true);
		
		final JScrollPane scrollComparePanel = new JScrollPane(comparePanel);
		scrollComparePanel.getVerticalScrollBar().setUnitIncrement(20);
		scrollComparePanel.setPreferredSize(new Dimension(0, 500));
		scrollComparePanel.setBorder(null);
		scrollComparePanel.setWheelScrollingEnabled(true);
		comparePanel.addMouseWheelListener(new MouseWheelListener() {
			
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				scrollComparePanel.dispatchEvent(e);
			}
		});
				
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, contentPanel, scrollComparePanel);
		getContentPane().add(splitPane);
		
		this.fillTypes();
		
	}
	
	private void fillTypes(){
		DefaultListModel listModel = new DefaultListModel();
		for(Type t : Type.values()){
			listModel.addElement(t);
		}
		listTypes.setModel(listModel);
		listTypes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listTypes.addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if(((JList) e.getSource()).getModel().getSize()>e.getLastIndex()){
					for(int i = e.getFirstIndex();i<=e.getLastIndex();i++){
						if(((JList) e.getSource()).isSelectedIndex(i)){
							fillDatabases((Type) ((JList)e.getSource()).getModel().getElementAt(i));
						}
					}
				}
			}
		});
		
	}
	

	private void fillDatabases(Type type){
		basesPanel.removeAll();
		comparePanel.clear();
		try {
			Statement st = DatabaseManager.getCurrent(Type.Databases);
			ResultSet rs = st.executeQuery("select * from databases where type = '"+type.toString()+"'");
			TypeItemListener itemListener = new TypeItemListener();
			while(rs.next()){
				JCheckBox chk = new JCheckBox(rs.getString("name"));
				chk.addItemListener(itemListener);
				basesPanel.add(chk);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		this.validate();
	}
	
	private void fillFiles(String base1, String base2){
		comparePanel.clear();
		Set<String> files1 = new HashSet<String>();
		Set<String> files2 = new HashSet<String>();
		
		path1 = new File(base1+"_files");
		path2 = new File(base2+"_files");
		
		for(File f : path1.listFiles()){
			files1.add(f.getName());
		}
		
		for(File f : path2.listFiles()){
			files2.add(f.getName());
		}
		
		files1.retainAll(files2);
		
		DefaultListModel files = new DefaultListModel();
		for(String f : files1){
			files.addElement(f);
		}
		
		listFiles.setModel(files);
	}
	
	private void emptyFiles(){
		listFiles.setModel(new DefaultListModel());
	}
	
	private class TypeItemListener implements  ItemListener{

		private JCheckBox first;
		private JCheckBox second;

		public int getItemSelectedCount(){
			int count = 0;
			if (first != null) count++;
			if (second != null) count++;
			return count;
		}

		@Override
		public void itemStateChanged(ItemEvent evt) {
			JCheckBox chk = (JCheckBox) evt.getSource();
			if(evt.getStateChange() == ItemEvent.SELECTED){
				if(getItemSelectedCount() == 2){
					JCheckBox temp = second;
					second = chk;
					temp.setSelected(false);
				} else if(getItemSelectedCount() == 1) {
					if(first == null) 
						first = chk;
					else
						second = chk;
				} else if(getItemSelectedCount() == 0) {
					first = chk;
				}
			} else if(evt.getStateChange() == ItemEvent.DESELECTED) {
				if(first.equals(chk)){
					first = null;
				} else if(second.equals(chk)){
					second = null;
				}
			}
			if(getItemSelectedCount() == 2){
				fillFiles(first.getText(), second.getText());
			} else {
				emptyFiles();
			}
		}
		
	}
}
