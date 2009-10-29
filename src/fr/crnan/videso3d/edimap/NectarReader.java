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
package fr.crnan.videso3d.edimap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import javax.swing.ProgressMonitorInputStream;
import javax.swing.SwingWorker;

/**
 * Lit un fichier Nectar
 * @author Bruno Spyckerelle
 * @version 0.3.1
 */
public class NectarReader extends SwingWorker<Integer, String>{
	
	private Entity datas;
	
	private String path;
		
	public NectarReader(){
		super();
	}
	
	/**
	 * Lecteur de fichier Nectar
	 * @param path Chemin vers le fichier
	 * @throws FileNotFoundException 
	 */
	public NectarReader(String path) throws FileNotFoundException{
		this.setPath(path);
	}
	
 	public void setPath(String path) throws FileNotFoundException{
 		if(new File(path).exists()){
			this.path = path;
		} else {
			throw new FileNotFoundException(path);
		}
 	}
	
 	public void done(){
 		if(!this.isCancelled()){
 			firePropertyChange("done", false, true);
 		}
 	}

 	public Integer doInBackground(){
 		BufferedReader in;
 		try {
 			in = new BufferedReader(new InputStreamReader(new ProgressMonitorInputStream(null, "Extraction du fichier " + this.path+" ...", new FileInputStream(this.path))));
 			this.setProgress(0);
 			datas = new Entity("root", this.getEntity(in));
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 		this.setProgress(100);
 		return 0;
 	}
	
	/**
	 * Parse une chaine au format Nectar et crée l'ensemble d'entités correspondant
	 * @param stream {@link BufferedReader}
	 * @param open nombre de parenthèses ouvrantes parcourues
	 * @param close nombre de parenthèses fermantes parcourues
	 * @return List<Entity> ensemble d'entités
	 */
	private List<Entity> getEntity(BufferedReader stream){
		List<Entity> result = new LinkedList<Entity>();
		try {
			while(stream.ready()){
				Entity entity = this.getNextEntity(stream);
				if(entity.getSecond() != null) { //gestion de la fin d'un fichier : si il ya des caractères après la dernière parenthèse, cela crée une entité vide
					result.add(entity);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return result;
	}
	
	private Entity getNextEntity(BufferedReader stream) throws IOException{
		Entity result = new Entity();
		int open = 0;
		int closed = 0;
		String value = "";
		Boolean first = false;
		while((open != closed || open == 0) && stream.ready()){
			stream.mark(2);
			char current = (char)stream.read();
			if(open == 0)  {
				if(current == '(') {
					open++;
					result.setKeyword(this.getKey(stream));
				}
			} else {
				//détermination du type de l'entité : si le caractère non blanc suivant la clef est une parenthèse ouvrante, c'est une entité complexe
				if(!first){
					if(!Character.isWhitespace(current)){
						first = true;
						if(current == '(') {
							stream.reset(); //on recule d'un caractère de façon à bien commencer par la parenthèse ouvrante
							result.addEntity(this.getNextEntity(stream));
						} else  if (current == ')'){
							closed++;
							if(!value.trim().isEmpty()) result.setValue(value.trim());
						} else {
							value += current;
						}
					}
				} else {
					if(current == '(') {
						stream.reset(); //on recule d'un caractère de façon à bien commencer par la parenthèse ouvrante
						result.addEntity(this.getNextEntity(stream));
					} else  if (current == ')'){
						closed++;
						if(!value.trim().isEmpty()) result.setValue(value.trim());
					} else {
						value += current;
					}
				}
			}
		}
		return result;
	}
	
	
	private String getKey(BufferedReader stream) throws IOException{
		String key = "";
		char current = (char)stream.read();
		while(!Character.isWhitespace(current)){
			key += current;
			current = (char)stream.read();
		}
		return key.trim();
	}
	
	/**
	 * Accès à l'entitée parsée
	 * @return Entity Entitée parsée
	 */
	public Entity getEntity(){
		return datas;
	}

}