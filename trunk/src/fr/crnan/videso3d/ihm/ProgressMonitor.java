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

import java.awt.Component;
/**
 * ProgressMonitor dont la barre de progression avance d'un cran à chaque appel de <code>setNote()</code>.
 * @author Bruno Spyckerelle
 */
public class ProgressMonitor extends javax.swing.ProgressMonitor {

	private int step = 0;
	
	public ProgressMonitor(Component parentComponent, Object message,
			String note, int min, int max) {
		super(parentComponent, message, note, min, max);
	}
	
	@Override
	public void setNote(String note){
		super.setNote(note);
		step++;
		this.setProgress(step);
	}
}
