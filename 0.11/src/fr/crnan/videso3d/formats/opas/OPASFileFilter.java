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
package fr.crnan.videso3d.formats.opas;

import java.io.File;

import javax.swing.filechooser.FileFilter;
/**
 * Filtre des fichiers OPAS pour un FileChooser
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class OPASFileFilter extends FileFilter {

	@Override
	public boolean accept(File f) {
		if (f.isDirectory()) {
            return true;
        }

		String ext = null;
		String s = f.getName();
		int i = s.lastIndexOf('.');
		if (i > 0 &&  i < s.length() - 1) {
			ext = s.substring(i+1).toLowerCase();
		}

		if (ext != null) {
			if (ext.equals("opas")) {
				return true;
			} else {
				return false;
			}
		}
		
		return false;
	}

	@Override
	public String getDescription() {
		return "Fichiers OPAS";
	}

}
