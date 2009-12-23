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

import gov.nasa.worldwind.util.Logging;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import javax.swing.ProgressMonitorInputStream;

/**
 * Lecteur de fichier LPLN.<br />
 * Un LPLN ne contenant pas les coordonnées des balises, un liaison à une base de donnée Stip est nécessaire.
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class LPLNReader {

	private List<LPLNTrack> tracks = new LinkedList<LPLNTrack>();

	private String name;

	public LPLNReader(){
		super();
	}

	public LPLNReader(File selectedFile) {
		try {
			this.readFile(selectedFile.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Boolean isLPLNFile(File file){
		Boolean lpln = false;
		try {
			BufferedReader in = new BufferedReader(
					new InputStreamReader(
							new FileInputStream(file)));
			int count = 0; //nombre de lignes lues
			while(in.ready() && !lpln && count < 50){
				if(in.readLine().startsWith("-                           NUMERO PLN")){
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

	/**
	 * @param path
	 * @throws IllegalArgumentException if <code>path</code> is null
	 * @throws java.io.IOException
	 */
	public void readFile(String path) throws IOException
	{
		if (path == null)
		{
			String msg = Logging.getMessage("nullValue.PathIsNull");
			Logging.logger().severe(msg);
			throw new IllegalArgumentException(msg);
		}

		this.setName(path);

		java.io.File file = new java.io.File(path);
		if (!file.exists())
		{
			String msg = Logging.getMessage("generic.FileNotFound", path);
			Logging.logger().severe(msg);
			throw new FileNotFoundException(path);
		}

		FileInputStream fis = new FileInputStream(file);
		this.doReadStream(fis);
	}

	private void doReadStream(InputStream stream)
	{
		String sentence;

		BufferedReader in = new BufferedReader(
				new InputStreamReader(
						new ProgressMonitorInputStream(null, 
								"Extraction du fichier LPLN ...",
								stream)));

		try
		{
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
							if(track.getNumPoints() > 0) this.tracks.add(track);
							//réinitialisation des compteurs
							count=0;
							balisesFound = false;
						}
						track = new LPLNTrack(sentence.substring(40, 44).trim());
						track.setIndicatif(sentence.substring(57, 65).trim());
					}
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
									track.addPoint(new LPLNTrackPoint(sentence));
								}
							}
						}
					}
				} 
			}
		}
		catch (NoSuchElementException e)
		{
			//noinspection UnnecessaryReturnStatement
			return;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public List<LPLNTrack> getTracks(){
		return tracks;
	}

}
