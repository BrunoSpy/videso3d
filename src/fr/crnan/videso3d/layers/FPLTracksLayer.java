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

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;

import fr.crnan.videso3d.MultiValueMap;
import fr.crnan.videso3d.formats.lpln.LPLNTrack;
import fr.crnan.videso3d.formats.lpln.LPLNTrackPoint;
import fr.crnan.videso3d.formats.fpl.FPLTrack;
import fr.crnan.videso3d.graphics.Profil3D;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.tracks.Track;

/**
 * Layer d'accueil pour les trajectoires plan de vol.
 * @author Adrien Vidal
 *
 */
public class FPLTracksLayer extends LPLNTracksLayer {
	
	/**
	 * Permet de stocker plusieurs profils pour une seule trajectoire, afin de pouvoir changer la couleur d'une partie de la trajectoire.
	 */
	private MultiValueMap<FPLTrack, Profil3D> profils = new MultiValueMap<FPLTrack, Profil3D>();
	
	public FPLTracksLayer(){
		super();
		this.setName("PLN");
		this.getLayer().setName("PLN");
	}
	
	@Override
	/**
	 * Affiche et construit si nécessaire le profil correspondant à la trajectoire <code>track</code>. Si la route prévue dans le plan de vol
	 * entre deux balises n'est pas connue, on affiche un profil orange reliant directement les deux balises.
	 */
	protected void showTrack(LPLNTrack track){
		if(track instanceof FPLTrack){
			FPLTrack FPLtrack = (FPLTrack) track;
			if(profils.containsKey(FPLtrack)){
				for (Profil3D p : profils.get(FPLtrack)){
					this.getLayer().addProfil3D(p);
				}
			} else {
				LinkedList<Position> positions = new LinkedList<Position>();
				LinkedList<String> balises = new LinkedList<String>();
				LinkedList<String> annotations = new LinkedList<String>();
				LinkedList<String> segmentsIncertains = FPLtrack.getSegmentsIncertains();
				boolean segmentIncertain = false;
				for(LPLNTrackPoint point : FPLtrack.getTrackPoints()){
					String nom = point.getName();
					//Si on passe d'un segment dont la route n'est pas certaine à un segment connu, ou inversement :
					//On crée un profil3D avec les balises déjà présentes, puis on vide positions, balises et annotations afin de 
					//créér un deuxième profil d'une autre couleur pour marquer la différence entre une route connue avec certitude
					//et une route incertaine
					if(positions.size()>1 && segmentsIncertains.contains(nom)!=segmentIncertain){
						Profil3D profil = buildProfil(balises, annotations, positions, segmentIncertain);
						profils.put(FPLtrack, profil);
						this.getLayer().addProfil3D(profil);
						//On garde la dernière balise qui servira au profil3D suivant
						Position lastPos = positions.getLast();
						String lastBalise = balises.getLast();
						String lastAnnotation = annotations.getLast();
						positions.clear();
						positions.add(lastPos);
						balises.clear();
						balises.add(lastBalise);
						annotations.clear();
						annotations.add(lastAnnotation);
					}
					segmentIncertain = segmentsIncertains.contains(nom);
					positions.add(point.getPosition());
					balises.add(nom);
					int niveau = (int)(Math.ceil(point.getElevation()/30.48));
					annotations.add("Balise "+nom+"\nFL"+niveau);
				}
				if(positions.size()>1){ //only add a line if there's enough points
					Profil3D profil = buildProfil(balises, annotations, positions, segmentIncertain);
					profils.put(FPLtrack, profil);
					this.getLayer().addProfil3D(profil);
				}
			}
		}
	}
	
	
	@Override
	public void highlightTrack(Track track, Boolean b){
		List<Profil3D> profil = this.profils.get((FPLTrack)track);
		if(profil != null){
			for(Profil3D p : profil){
				p.highlight(b);
			}
		}
		this.firePropertyChange(AVKey.LAYER, null, this);
	}
	
	/**
	 * 
	 * @param balises
	 * @param annotations
	 * @param positions
	 * @param incertain
	 * @return Un profil de couleur rouge si la route est incertaine, de couleur bleue si la route est connue avec exactitude.
	 */
	private Profil3D buildProfil(List<String> balises, List<String> annotations, List<Position> positions, boolean incertain){
		Profil3D p = new Profil3D( balises, annotations, positions);
		//Si on n'est pas sûr de la route suivie, on met le profil en rouge
		if(incertain)
			p.setColor(Color.ORANGE);
		return p;
	}
	
	
}
	