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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.HashMap;

import fr.crnan.videso3d.layers.contourspays.*;
import fr.crnan.videso3d.Configuration;
import fr.crnan.videso3d.Pallet;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.SurfacePolygon;
/**
 * Layer contenant le contour de certains pays européens selon le définition des frontières fournie par SATIN
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class FrontieresStipLayer extends RenderableLayer {
	
	private HashMap<String, SurfacePolygon> pays = new HashMap<String, SurfacePolygon>();
	
	public FrontieresStipLayer(){
		this.setName("FondsPays");
		Configuration.addPropertyChangeListener(new PropertyChangeListener() {	
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if(evt.getPropertyName().equals(Configuration.COLOR_FOND_PAYS)){
					for(SurfacePolygon r : pays.values()){
						ShapeAttributes attrs =  r.getAttributes();
						attrs.setInteriorMaterial(new Material(Pallet.getColorFondPays()));
						r.setAttributes(attrs);
					}
					firePropertyChange(AVKey.LAYER, null, this);
				}
			}
		});
		this.setPickEnabled(false);
		this.setEnabled(true);		
	}

	public void setFrance(){
		setFondPays("France", France.FRANCE);
	}
	
	public void setEurope(){		
		setFondPays("Corse", France.CORSE);
		setFondPays("Allemange", Allemagne.ALLEMAGNE);
		setFondPays("France", France.FRANCE);
		setFondPays("Belgique", BeNeLux.BELGIQUE);
		setFondPays("Luxembourg", BeNeLux.LUXEMBOURG);
		setFondPays("PaysBas", BeNeLux.PAYSBAS);
		setFondPays("PaysBasSO", BeNeLux.PAYSBASSO);
		setFondPays("Angleterre", GrandeBretagne.ANGLETERRE);
		setFondPays("IrlandeNord", GrandeBretagne.IRLANDENORD);
		setFondPays("IrlandeSud", GrandeBretagne.IRLANDESUD);
		setFondPays("Italie", Italie.ITALIE);
		setFondPays("Sicile", Italie.SICILE);
		setFondPays("Espagne", PeninsuleIberique.ESPAGNE);
		setFondPays("Portugal", PeninsuleIberique.PORTUGAL);
		setFondPays("Andorre", PeninsuleIberique.ANDORRE);
		setFondPays("Suisse", Suisse.SUISSE);
	}
	
	public void setFondPays(String name, double[] contour){
		if(pays.containsKey(name)){
			this.addRenderable(pays.get(name));
		}else{
			final SurfacePolygon polygon = new SurfacePolygon(new BasicShapeAttributes());
			polygon.setLocations(makeLatLon(contour));
			ShapeAttributes attrs = polygon.getAttributes();
			attrs.setInteriorMaterial(new Material(Pallet.getColorFondPays()));
			polygon.setAttributes(attrs);
			this.addRenderable(polygon);
			pays.put(name, polygon);
		}
	}
	
	public void removeFond(){
		this.removeAllRenderables();
	}
	
	private static Iterable<LatLon> makeLatLon(double[] src){
		int length = src.length;
		int numCoords = (int) Math.floor(length / 2.0);
        LatLon[] dest = new LatLon[numCoords];
        for (int i = 0; i < numCoords; i++)
        {
            double latDegrees = src[2 * i];
            double lonDegrees = src[2 * i + 1];
            dest[i] = LatLon.fromDegrees(latDegrees, lonDegrees);
        }
        return Arrays.asList(dest);
	}
	
}
