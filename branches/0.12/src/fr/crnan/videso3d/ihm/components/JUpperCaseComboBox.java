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

import java.awt.Dimension;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

/**
 * {@link JComboBox} with forced upper case letters
 * @author Adrien Vidal
 * @version 0.1.1
 */
public class JUpperCaseComboBox extends JComboBox<String> {

	public JUpperCaseComboBox(){
		super();
		this.setEditor(new JUpperCaseComboBoxEditor());
	}

	public JUpperCaseComboBox(DefaultComboBoxModel<String> defaultComboBoxModel) {
		super(defaultComboBoxModel);
		this.setEditor(new JUpperCaseComboBoxEditor());
		this.setPreferredSize(new Dimension(111,24));

	}
}