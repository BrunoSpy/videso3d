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

import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;

/**
 * Lit un fichier Nectar et fournit des méthodes permettant l'accès à l'ensemble d'entités correspondant.
 * @author Bruno Spyckerelle
 * @version 0.2.2
 */
public class NectarReader extends QSignalEmitter implements Runnable{
	
	/**
	 * Signal envoyé à la fin de la lecture d'un fichier
	 */
	public Signal0 fileRead = new Signal0();
	/**
	 * Envoit du pourcentage de données lues
	 */
	public Signal1<Integer> percentage = new Signal1<Integer>();
	
	private Entity datas;
	
	private String path;
	
	private int percent = 0;
	
	private long endFile;
	
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
 		if(QFile.exists(path)){
			this.path = path;
		} else {
			throw new FileNotFoundException(path);
		}
 	}
	
	public void run(){
		QFile file = new QFile(path);
		if (file.open(new QIODevice.OpenMode(QIODevice.OpenModeFlag.ReadOnly,
										 QIODevice.OpenModeFlag.Text))) {
			QTextStream in = new QTextStream(file);
			//on positionne le pointeur à la fin du fichier
			in.readAll();
			endFile = in.pos();
			this.percentage.emit(0);
			datas = new Entity("root",this.getEntity(in, 0, endFile));
		}
		this.percentage.emit(100);
		this.fileRead.emit();
	}
	
	/**
	 * Parse une chaine au format Nectar et crée l'ensemble d'entités correspondant
	 * @param stream QTextStream
	 * @param begin début de la chaine à parser
	 * @param end fin de la chaine à parser
	 * @return List<Entity> ensemble d'entités
	 */
	private List<Entity> getEntity(QTextStream stream, long begin, long end){
		List<Entity> result = new LinkedList<Entity>();
		//début de l'entité
		long start = begin;
		//fin de l'entité
		long finish = end;
		//position du pointeur
		long pos = begin;
		//compteurs de parenthèses ouvrantes et fermantes
		int compteurO = 0;
		int compteurF = 0;
		
		//on positionne le pointeur au début de la chaîne à traiter
		stream.seek(begin);
		while(pos<end && !stream.atEnd()){
			compteurO = 0;
			compteurF = 0;
			Character car = stream.read(1).charAt(0);
			//une entité commence toujours par une '('
			while(compteurO == 0 && !stream.atEnd() && pos<end){
				if(car.compareTo('(') == 0) {
					compteurO++;
				} else {
					car = stream.read(1).charAt(0);
				}
				pos = stream.pos();
			}
			if(compteurO == 1){ //on a atteint la fin du fichier sans trouver de parenthèse ouvrante
				//sauvegarde de la position de la parenthèse ouvrante
				start = stream.pos();
				//recherche de la parenthèse suivante
				while( (compteurO + compteurF < 2) && !stream.atEnd()){
					car = stream.read(1).charAt(0);
					if(car.compareTo('(') == 0){
						compteurO++;
					} else if(car.compareTo(')') == 0) {
						compteurF++;
					}
				}
				//sauvegarde de la position de la deuxième parenthèse
				pos = stream.pos();
				if(compteurO == compteurF && compteurO == 1) {//entité simple
					long length = pos - start;
					stream.seek(start); //on n'enregistre pas la parenthèse ouvrante
					String entitySimple = stream.read(length-1);//on ne garde pas la dernière parenthèse
					String[] values = entitySimple.split("\\s+",2);
					result.add(new Entity(values[0], values[1].trim()));
					stream.read(1);//lecture de la dernière parenthèse
				} else { //ensemble d'entités
					long length = pos - start;
					//on récupère la clef
					stream.seek(start);
					String key = stream.read(length-1).trim();
					//recherche de la dernière parenthèse fermante
					stream.seek(pos);
					while(compteurO != compteurF && !stream.atEnd()){
						car = stream.read(1).charAt(0);
						if(car.compareTo('(') == 0) compteurO++;
						if(car.compareTo(')') == 0) compteurF++;
					}
					finish = stream.pos();
					result.add(new Entity(key, this.getEntity(stream, pos-1, finish)));
					pos = finish;
				}

			}
		}
		//calcul du pourcentage de données lues
		Integer percentCurrent = new Long(100*(pos)/endFile).intValue();
		if(percentCurrent > percent) {
			percent = percentCurrent;
			this.percentage.emit(percent);
		}
		return result;
	}
	
	/**
	 * Accès à l'entitée parsée
	 * @return Entity Entitée parsée
	 */
	public Entity getEntity(){
		return datas;
	}
}