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
package fr.crnan.videso3d.ihm.components;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;

import java.awt.Dimension;

import java.awt.Color;
import javax.swing.JScrollPane;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.io.File;
import java.io.IOException;

import javax.swing.text.BadLocationException;

import fr.crnan.videso3d.FileManager;
import fr.crnan.videso3d.util.LineHighLighter;
import fr.crnan.videso3d.util.NoWrapEditorKit;
import fr.crnan.videso3d.util.diff.AbstractDiffPrinter;
import fr.crnan.videso3d.util.diff.Diff;
import fr.crnan.videso3d.util.diff.ThreePanelsDiffPrinter;

import javax.swing.ScrollPaneConstants;


/**
 * Panel to display diff between two files
 * @author Bruno Spyckerelle
 * @version 0.1.1
 */
public class DiffPanel extends JPanel {
	
	
	private JTextPane paneSrc;
	private JTextPane paneNumLines;
	private JTextPane paneDst;
	
	
	/**
	 * 
	 * @param titled True to display the name of the file above panels
	 */
	public DiffPanel(boolean titled){
		
		titled = false;
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWeights = new double[]{1.0, 0.0, 1.0};
		gridBagLayout.columnWidths = new int[]{205, 40, 205};
		gridBagLayout.rowHeights = new int[]{0, 0};
		gridBagLayout.rowWeights = new double[]{titled ? 0.0 : 1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		NoWrapEditorKit noWrapEditorKit = new NoWrapEditorKit();
				
		paneSrc = new JTextPane();
		paneSrc.setEditable(false);
		paneSrc.setEditorKit(noWrapEditorKit);
		paneSrc.setFont(new java.awt.Font("Monospaced", 0, 12));
		JScrollPane scrollPane_1 = new JScrollPane(paneSrc);
		scrollPane_1.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
		gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_1.gridx = 0;
		gbc_scrollPane_1.gridy = titled ? 1 : 0;
		add(scrollPane_1, gbc_scrollPane_1);
		
		gbc_scrollPane_1.fill = GridBagConstraints.NONE;
		gbc_scrollPane_1.gridy = 0;
		JLabel titleSrc = new JLabel();
		if(titled) add(titleSrc, gbc_scrollPane_1);
		
		paneNumLines = new JTextPane();
		paneNumLines.setEditable(false);
		paneNumLines.setFont(new java.awt.Font("Monospaced", 0, 12));
		paneNumLines.setPreferredSize(new Dimension(50, 0));
		paneNumLines.setBackground(Color.LIGHT_GRAY);
		GridBagConstraints gbc_editorPane = new GridBagConstraints();
		gbc_editorPane.fill = GridBagConstraints.BOTH;
		gbc_editorPane.gridx = 1;
		gbc_editorPane.gridy = titled ? 1 : 0;
		add(paneNumLines, gbc_editorPane);
		
		gbc_editorPane.fill = GridBagConstraints.NONE;
		gbc_editorPane.gridy = 0;
		JLabel titleLine = new JLabel("");
		if(titled) add(titleLine, gbc_editorPane);
		
		paneDst = new JTextPane();
		paneDst.setEditorKit(noWrapEditorKit);
		paneDst.setFont(new java.awt.Font("Monospaced", 0, 12));
		paneDst.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(paneDst);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 2;
		gbc_scrollPane.gridy = titled ? 1 : 0;
		add(scrollPane, gbc_scrollPane);		

		gbc_scrollPane.fill = GridBagConstraints.NONE;
		gbc_scrollPane.gridy = 0;
		JLabel titleDst = new JLabel("");
		if(titled) add(titleDst, gbc_scrollPane);
		
		paneSrc.setHighlighter(new LineHighLighter());
		paneDst.setHighlighter(new LineHighLighter());
		
	}

	/**
	 * Compare and displays the comparison
	 * @param src
	 * @param dst
	 * @throws IOException
	 */
	public void compareFiles(File src, File dst) throws IOException{
		try {
			this.fillPanels(src, dst);
		} catch (BadLocationException e) {
			e.printStackTrace();
		} 
	}
	
	public void clear(){
		paneSrc.setText("");
		paneDst.setText("");
		paneNumLines.setText("");
	}
	
	private void fillPanels(File src, File dst) throws BadLocationException, IOException {
		
		String[] stringSrc = FileManager.textFiletoArray(src);
		String[] stringDst = FileManager.textFiletoArray(dst);
        
		Diff diff = new Diff(stringSrc, stringDst);
		
		AbstractDiffPrinter panelDiffPrinter = new ThreePanelsDiffPrinter(stringSrc, stringDst, paneSrc, paneNumLines, paneDst);
		panelDiffPrinter.print_script(diff.diff_2(false));
	}

	
}
