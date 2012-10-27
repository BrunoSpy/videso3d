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

import javax.swing.JTextField;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
/**
 * {@link JTextField} with forced upper case letters
 * @author Bruno Spyckerelle
 * @version 0.1.0
 */
public class JUpperCaseTextField extends JTextField {

	private class UppercaseDocumentFilter extends DocumentFilter {

		/**
		 * Override insertString method of DocumentFilter to make the text format to uppercase.
		 */
		@Override
		public void insertString(DocumentFilter.FilterBypass fb, int offset, String text, AttributeSet attr) throws BadLocationException {
			fb.insertString(offset, text.toUpperCase(), attr);
		}

		/**
		 * Override replace method of DocumentFilter to make the text format to uppercase.
		 */
		@Override
		public void replace(DocumentFilter.FilterBypass fb, int offset, int length,	String text, AttributeSet attrs) throws BadLocationException {
			fb.replace(offset, length, text.toUpperCase(), attrs);
		}
	}
	
	public JUpperCaseTextField(){
		super();
		((AbstractDocument)this.getDocument()).setDocumentFilter(new UppercaseDocumentFilter());
	}
}
