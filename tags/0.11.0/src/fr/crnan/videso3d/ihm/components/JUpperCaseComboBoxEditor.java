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
import javax.swing.plaf.basic.BasicComboBoxEditor;
/**
 * 
 * @author Adrien Vidal
 * @version 0.1.0
 */
public class JUpperCaseComboBoxEditor extends BasicComboBoxEditor {

	/**
     * Creates the internal editor component. Override this to provide
     * a custom implementation.
     *
     * @return Un JUpperCaseTextField pour tout afficher en majuscule dans la combobox.
     */
	@Override
    protected JTextField createEditorComponent() {
        JTextField editor = new JUpperCaseTextField();
        editor.setColumns(9);
        return editor;
    }

}
