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
package fr.crnan.videso3d.util.diff;

import java.awt.Color;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Highlighter;
import javax.swing.text.SimpleAttributeSet;

import fr.crnan.videso3d.util.diff.Diff.change;

/**
 * Prints a diff into three panels.<br />
 * @author Bruno Spyckerelle
 * @version 0.1.0
 */
public class ThreePanelsDiffPrinter extends AbstractDiffPrinter {

	private DefaultStyledDocument dstDoc;
	private DefaultStyledDocument srcDoc;
	private DefaultStyledDocument numLinesDoc;

	private Color addedColor = Color.GREEN;
	private Color deletedColor = Color.PINK;

	private Highlighter.HighlightPainter addedPainter = new DefaultHighlighter.DefaultHighlightPainter(addedColor);
	private Highlighter.HighlightPainter deletedPainter = new DefaultHighlighter.DefaultHighlightPainter(deletedColor);

	private JTextPane srcPane;
	private JTextPane numLinesPane;
	private JTextPane dstPane;

	/**
	 * Number of conxtual lines to print before and after changes
	 */
	private int context = 3;
	
	private SimpleAttributeSet unchanged = new SimpleAttributeSet();

	/**
	 * 
	 * @param a
	 * @param b
	 * @param src {@link JTextPane} for source file
	 * @param numLines {@link JTextPane} for line numbers
	 * @param dst {@link JTextPane} for destintation file
	 */
	public ThreePanelsDiffPrinter(Object[] a, Object[] b, JTextPane src, JTextPane numLines, JTextPane dst) {
		super(a, b);
		this.srcDoc = new DefaultStyledDocument();
		this.dstDoc = new DefaultStyledDocument();
		this.numLinesDoc = new DefaultStyledDocument();

		this.srcPane = src;
		this.dstPane = dst;
		this.numLinesPane = numLines;
	}


	
	/* (non-Javadoc)
	 * @see fr.crnan.videso3d.util.AbstractDiffPrinter#print_script(fr.crnan.videso3d.util.Diff.change)
	 */
	@Override
	public void print_script(change script) {
		super.print_script(script);
		srcPane.setDocument(srcDoc);
		dstPane.setDocument(dstDoc);
		numLinesPane.setDocument(numLinesDoc);
	}



	@Override
	protected void print_hunk(change hunk) {

		analyze_hunk(hunk);

		if (deletes == 0 && inserts == 0)
			return;
		
		first0 = Math.max(first0 - context, 0);
		first1 = Math.max(first1 - context, 0);
		last0 = Math.min(last0 + context, file0.length - 1);
		last1 = Math.min(last1 + context, file1.length - 1);
		
		addBlankLines();
		
		Diff.change next = hunk;
		int i = first0;
		int j = first1;
		
		while (i <= last0 || j <= last1) {

			/* If the line isn't a difference, output the context from file 0. */

			if (next == null || i < next.line0) {
				if (i < file0.length) {
					//outfile.print(' ');
					//print_1_line ("", file0[i++]);
					addUnchangedLine(file0[i++].toString(), i);
				}
				j++;
			}
			else {

				int k = next.deleted;
				int k2 = next.inserted;
				
				while (k-- > 0 && k2-- > 0) {
					//		outfile.print('-');
					//		print_1_line ("", file0[i++]);
					addChangedLine(file0[i++].toString(), file1[j++].toString(), i);
				}

				while(k-- > 0){
					addDeletedLine(file0[i++].toString(), i);
				}
				
				while(k2-- > 0){
					addAddedLine(file1[j++].toString(), j);
				}

				/* We're done with this hunk, so on to the next! */

				next = next.link;
			}
			
		}
	}

	private void addUnchangedLine(String line, int numLine){
		try {
			srcDoc.insertString(srcDoc.getLength(), line+"\n", unchanged );
			dstDoc.insertString(dstDoc.getLength(), line+"\n", unchanged);
			numLinesDoc.insertString(numLinesDoc.getLength(), numLine+"\n", unchanged);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	private void addChangedLine(String lineSrc, String lineDst, int numLine){
		try {
			srcDoc.insertString(srcDoc.getLength(), lineSrc+"\n", unchanged );
			srcPane.getHighlighter().addHighlight(srcDoc.getLength()-lineSrc.length()-1,
					srcDoc.getLength(), deletedPainter);
			dstDoc.insertString(dstDoc.getLength(), lineDst+"\n", unchanged);
			dstPane.getHighlighter().addHighlight(dstDoc.getLength()-lineDst.length()-1,
					dstDoc.getLength(), addedPainter);
			numLinesDoc.insertString(numLinesDoc.getLength(), numLine+"\n", unchanged);
		} catch (BadLocationException e) {

			e.printStackTrace();
		}
	}
	
	private void addDeletedLine(String line, int numLine){

		try {
			srcDoc.insertString(srcDoc.getLength(), line+"\n", unchanged );
			srcPane.getHighlighter().addHighlight(srcDoc.getLength()-line.length()-1,
					srcDoc.getLength(), deletedPainter);
			dstDoc.insertString(dstDoc.getLength(), "\n", unchanged);
			numLinesDoc.insertString(numLinesDoc.getLength(), numLine+"\n", unchanged);
		} catch (BadLocationException e) {

			e.printStackTrace();
		}
	}

	private void addAddedLine(String line, int numLine){

		try {
			srcDoc.insertString(srcDoc.getLength(), " \n", unchanged );
			dstDoc.insertString(dstDoc.getLength(), line+"\n", unchanged);
			dstPane.getHighlighter().addHighlight(dstDoc.getLength()-line.length()-1,
					dstDoc.getLength(), addedPainter);
			numLinesDoc.insertString(numLinesDoc.getLength(), numLine+"\n", unchanged);
		} catch (BadLocationException e) {

			e.printStackTrace();
		}
	}
	
	private void addBlankLines(){
		try {
			srcDoc.insertString(srcDoc.getLength(), " \n", unchanged );			
			dstDoc.insertString(dstDoc.getLength(), " \n", unchanged);
			numLinesDoc.insertString(numLinesDoc.getLength(), "\n", unchanged);

			srcDoc.insertString(srcDoc.getLength(), " \n", unchanged );			
			dstDoc.insertString(dstDoc.getLength(), " \n", unchanged);
			numLinesDoc.insertString(numLinesDoc.getLength(), "...\n", unchanged);

			srcDoc.insertString(srcDoc.getLength(), " \n", unchanged );			
			dstDoc.insertString(dstDoc.getLength(), " \n", unchanged);
			numLinesDoc.insertString(numLinesDoc.getLength(), "\n", unchanged);
		} catch (BadLocationException e) {

			e.printStackTrace();
		}
	}

	
	/**
	 * @return the addedColor
	 */
	public Color getAddedColor() {
		return addedColor;
	}


	/**
	 * @param addedColor the addedColor to set
	 */
	public void setAddedColor(Color addedColor) {
		this.addedColor = addedColor;
	}


	/**
	 * @return the deletedColor
	 */
	public Color getDeletedColor() {
		return deletedColor;
	}
	
	/**
	 * @param deletedColor the deletedColor to set
	 */
	public void setDeletedColor(Color deletedColor) {
		this.deletedColor = deletedColor;
	}



	/**
	 * @return the context
	 */
	public int getContext() {
		return context;
	}

	/**
	 * @param context the context to set
	 */
	public void setContext(int context) {
		this.context = context;
	}
	
}
