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
package fr.crnan.videso3d.layers;

import fr.crnan.videso3d.graphics.Profil3D;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.layers.SurfaceShapeLayer;
/**
 * Layer d'accueil pour des {@link Profil3D}
 * @author Bruno Spyckerelle
 * @version 0.1
 */
@SuppressWarnings("serial")
public class ProfilLayer extends LayerSet {

	/**
	 * Texte des couples balise/niveau
	 */
	private TextLayer textLayer = new TextLayer("Balises");
	/**
	 * Dessins
	 */
	private RenderableLayer renderableLayer = new RenderableLayer();
	/**
	 * Projection
	 */
	private SurfaceShapeLayer shapeLayer = new SurfaceShapeLayer();
	/**
	 * Markers
	 */
	private MarkerLayer markerLayer = new MarkerLayer();
	
	public ProfilLayer(String name){
		this.setName(name);
		this.textLayer.setAMSL(true);
		this.add(textLayer);
		this.add(renderableLayer);
		this.add(shapeLayer);
		this.add(markerLayer);
	}
	
	public void addProfil3D(Profil3D profil3d){
		this.shapeLayer.addRenderable(profil3d.getProjection());
		this.renderableLayer.addRenderable(profil3d.getProfil());
		if(profil3d.withMarkers()) this.textLayer.addGeographicTexts(profil3d.getBalises());
		if(profil3d.withMarkers()) this.markerLayer.setMarkers(profil3d.getMarkers());
		if(profil3d.isPlain()) this.renderableLayer.addRenderable(profil3d.getCurtain());
	}
	
}
