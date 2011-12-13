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

import fr.crnan.videso3d.graphics.Balise3D;
import fr.crnan.videso3d.graphics.Profil3D;
import gov.nasa.worldwind.layers.RenderableLayer;
/**
 * Layer d'accueil pour des {@link Profil3D}
 * @author Bruno Spyckerelle
 * @version 0.2.2
 */
@SuppressWarnings("serial")
public class ProfilLayer extends LayerSet {

	/**
	 * Texte des couples balise/niveau
	 */
	private Balise3DLayer baliseLayer = new Balise3DLayer("Balises");
	/**
	 * Dessins
	 */
	private RenderableLayer renderableLayer = new RenderableLayer();
	/**
	 * Projection
	 */
	private RenderableLayer shapeLayer = new RenderableLayer();

	
	public ProfilLayer(String name){
		this.setName(name);
		this.add(baliseLayer);
		this.add(renderableLayer);
		this.add(shapeLayer);
	}
	
	public void addProfil3D(Profil3D profil3d){
		this.shapeLayer.addRenderable(profil3d.getProjection());
		this.renderableLayer.addRenderable(profil3d.getProfil());
		if(profil3d.withMarkers()) {
			this.baliseLayer.addBalises(profil3d.getBalises());
			this.baliseLayer.showAll();
		}
		if(profil3d.isPlain()) this.renderableLayer.addRenderable(profil3d.getCurtain());
	}
	
	public void removeAllRenderables(){
		this.shapeLayer.removeAllRenderables();
		this.renderableLayer.removeAllRenderables();
		this.baliseLayer.eraseAllBalises();
	}
	
	public void removeProfil3D(Profil3D profil){
		this.shapeLayer.removeRenderable(profil.getProjection());
		this.renderableLayer.removeRenderable(profil.getProfil());
		if(profil.isPlain()) this.renderableLayer.removeRenderable(profil.getCurtain());
		if(profil.withMarkers()) {
			for(Balise3D b : profil.getBalises()){
				this.baliseLayer.removeBalise(b);
			}
		}
	}
}
