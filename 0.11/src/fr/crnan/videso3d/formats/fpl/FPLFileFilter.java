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
package fr.crnan.videso3d.formats.fpl;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 * Filtre des fichiers en .txt ou .fpl ou .pln (fichiers plan de vol) pour un fileChooser.
 * @author Adrien Vidal
 *
 */
public class FPLFileFilter extends FileFilter {

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
			if (ext.equals("txt") || ext.equals("fpl") || ext.equals("pln")) {
				return true;
			} else {
				return false;
			}
		} else {
			return true;
		}
	}

	@Override
	public String getDescription() {
		return "Fichiers PLN";
	}

}
