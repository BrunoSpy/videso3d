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
package fr.crnan.videso3d.formats.lpln;

import fr.crnan.videso3d.formats.TrackFilesReader;
import fr.crnan.videso3d.stip.PointNotFoundException;
import fr.crnan.videso3d.trajectography.TracksModel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.NoSuchElementException;
import java.util.Vector;

import javax.swing.ProgressMonitorInputStream;

/**
 * Lecteur de fichier LPLN.<br />
 * Un LPLN ne contenant pas les coordonnées des balises, un liaison à une base de donnée Stip est nécessaire.
 * @author Bruno Spyckerelle
 * @version 0.2.1
 */
public class LPLNReader extends TrackFilesReader{
		
	public LPLNReader(File selectedFile, TracksModel model) throws PointNotFoundException {
		super(selectedFile, model);
	}

	public LPLNReader(Vector<File> files, TracksModel model) throws PointNotFoundException {
		super(files, model);
	}
	
	public LPLNReader(File selectedFile) throws PointNotFoundException {
		super(selectedFile);
	}

	public LPLNReader(Vector<File> files) throws PointNotFoundException {
		super(files);
	}
	
	public static Boolean isLPLNFile(File file){
		Boolean lpln = false;
		try {
			BufferedReader in = new BufferedReader(
					new InputStreamReader(
							new FileInputStream(file)));
			int count = 0; //nombre de lignes lues
			while(in.ready() && !lpln && count < 50){
				String line = in.readLine();
				if(line.startsWith("-                           NUMERO PLN")
						|| line.startsWith("CHAMP GESTION TRANSMIS")){
					lpln = true;
				}
				count++;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return lpln;
	}

	@Override
	protected void doReadStream(FileInputStream stream) throws PointNotFoundException {
		
		String sentence;

		BufferedReader in = new BufferedReader(
				new InputStreamReader(
						new ProgressMonitorInputStream(null, 
								"Extraction du fichier LPLN ...",
								stream)));

	    try {
			LPLNTrack track = null;
			boolean balisesFound = false;
			int count = 0;
			while(in.ready()){
				sentence = in.readLine();
				if (sentence != null)
				{
					if(sentence.startsWith("-                           NUMERO PLN")){
						//nouveau track et enregistrement du précédent si besoin
						if(track != null) {
							if(track.getNumPoints() > 0) this.getModel().addTrack(track);
							//réinitialisation des compteurs
							count=0;
							balisesFound = false;
						}
						track = new LPLNTrack(sentence.substring(40, 44).trim());
						track.setIndicatif(sentence.substring(57, 65).trim());
					}
					if(track != null){
						if(sentence.startsWith("AERODROME  DEP.")){
							track.setDepart(sentence.substring(18, 22));
						} else if(sentence.startsWith("AERODROME DEST.")) {
							track.setArrivee(sentence.substring(18,22));
						} else if(sentence.startsWith("TYPE AVION   ")){
							track.setType(sentence.substring(14, 22).trim());
						} else {
							if(!balisesFound){
								if(sentence.startsWith(". BALISES")){
									balisesFound = true;
								}
							} else {
								if(sentence.startsWith("----------")){
									count++;
								} else {
									if(count < 2){ //à partir de count == 2, l'ensemble des balises est passé
										try {
											track.addPoint(new LPLNTrackPoint(sentence));
										} catch (PointNotFoundException e) {
											e.printStackTrace();
											throw e;
										}
									}
								}
							}
						}
					}
				} 
			}
		}
		catch (NoSuchElementException e) {
			//noinspection UnnecessaryReturnStatement
			return;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
