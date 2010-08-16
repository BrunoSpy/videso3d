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

package fr.crnan.videso3d.skyview;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.VidesoController;
import fr.crnan.videso3d.VidesoGLCanvas;
import gov.nasa.worldwind.layers.Layer;

/**
 * Gestion des données SkyView
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class SkyViewController implements VidesoController {

	private VidesoGLCanvas wwd;
	
	public SkyViewController(VidesoGLCanvas wwd){
		this.wwd = wwd;
	}
	
	@Override
	public void highlight(String name) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unHighlight(String name) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addLayer(String name, Layer layer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeLayer(String name, Layer layer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeAllLayers() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void toggleLayer(Layer layer, Boolean state) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showObject(int type, String name) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void hideObject(int type, String name) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void set2D(Boolean flat) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}

	
	
}
