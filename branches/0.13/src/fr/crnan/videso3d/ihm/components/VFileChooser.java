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

import javax.swing.JFileChooser;

import fr.crnan.videso3d.Configuration;
/**
 * Sélecteur de fichier avec un comportement plus ergonomique
 * @author Bruno Spyckerelle
 * @version 0.1.1
 */
public class VFileChooser extends JFileChooser {

	private String defaultrep;
	
	public VFileChooser(){
		super(Configuration.getProperty(Configuration.DEFAULT_REP, System.getProperty("user.dir")));
	}
	
	public VFileChooser(String defaultrep){
		super(Configuration.getProperty(defaultrep, System.getProperty("user.dir")));
		this.defaultrep = defaultrep;
	}

	@Override
	public void approveSelection() {
		super.approveSelection();
		Configuration.setProperty((this.defaultrep != null ? this.defaultrep : Configuration.DEFAULT_REP), this.getSelectedFile().getAbsolutePath());
	}
	
	
	
}
