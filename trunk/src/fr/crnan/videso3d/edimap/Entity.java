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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import fr.crnan.videso3d.Couple;
/**
 * Voir paragraphe 9.1.5 du DDI EDIMAP
 * @author Bruno Spyckerelle
 * @version 0.4
 */
public class Entity extends Couple<String, Object>{
	
	public Entity(){
		super();
	}
	
	public Entity(String first, String second){
		this.setFirst(first);
		this.setSecond(second);
	}
	
	public Entity(String first, Entity entity){
		this.setFirst(first);
		List<Entity> second = new LinkedList<Entity>();
		second.add(entity);
		this.setSecond(second);
	}
	
	public Entity(String key, List<Entity> entity) {
		this.setFirst(key);
		this.setSecond(entity);
	}

	/**
	 * Ajoute une entité à l'ensemble des entités
	 * @param entity
	 */
	@SuppressWarnings("unchecked")
	public void addEntity(Entity entity){
		((List<Entity>)this.getSecond()).add(entity);
	}
	
	/**
	 * Retourne l'ensemble des valeurs correspondants au mot-clef.
	 * @param keyword Mot-clef recherché
	 * @return List<Entity>
	 */
	@SuppressWarnings("unchecked")
	public List<Entity> getValues(String keyword){
		if(this.getSecond().getClass().equals(new String().getClass())){
			//sans objet : l'entité est une entité simple
			return null;
		} else {
			List<Entity> results = new LinkedList<Entity>();
			Iterator<Entity> iterator = ((List<Entity>)this.getSecond()).iterator();
			while(iterator.hasNext()){
				Entity entity = iterator.next();
				if(entity.getKeyword().equalsIgnoreCase(keyword)){
					results.add(entity);
				}
			}
			return results;	
		}
	}
	
	/**
	 * Retourne la valeur associée au premier mot-clef correspondant rencontré
	 * @param keyword Mot-clef recherché
	 * @return String Valeur associée
	 */
	@SuppressWarnings("unchecked")
	public String getValue(String keyword){
		String result = null;
		if(this.getSecond().getClass().equals(new String().getClass())){
			//sans objet : l'entité n'a pas de fils
			return null;
		} else {
			Iterator<Entity> iterator = ((List<Entity>)this.getSecond()).iterator();
			while(iterator.hasNext()){
				Entity entity = iterator.next();
				if(entity.getKeyword().equalsIgnoreCase(keyword)){
					result = (String) entity.getValue();
				}
			}
			//on ne renvoit pas les guillemets
			//TODO pas forcément la bonne place pour faire ça
			if(result != null) result = result.replaceAll("\"", "");
			return result;
		}
	}

	/**
	 * Retourne l'entité keyword
	 * @param keyword Nom de l'entité
	 * @return Entity Entité recherchée. Vide si l'entité n'existe pas.
	 */
	@SuppressWarnings("unchecked")
	public Entity getEntity(String keyword){
		if(this.getSecond().getClass().equals(new String().getClass())){
			//sans objet : l'entité est une entité simple
			return null;
		} else {
			Entity result = new Entity();
			Iterator<Entity> iterator = ((List<Entity>)this.getSecond()).iterator();
			while(iterator.hasNext()){
				Entity entity = iterator.next();
				if(entity.getKeyword().equalsIgnoreCase(keyword)){
					result = entity;
				}
			}
			return result;	
		}
	}
	/**
	 * Retourne le mot-clef de l'entité
	 * @return mot-clef
	 */
	public String getKeyword(){
		return this.getFirst();
	}
	
	/**
	 * Retourne la valeur de l'entité
	 * @return valeur
	 */
	public Object getValue(){
		return this.getSecond();
	}
	/**
	 * Sets the key of the entity
	 * @param key String Keyword
	 */
	public void setKeyword(String key){
		this.setFirst(key);
	}
	
	/**
	 * Sets the value of the Entity : String, Entity or List<Entity>
	 * @param value String, Entity or List<Entity>
	 */
	public void setValue(Object value){
		this.setSecond(value);
	}
	
	/**
	 * Retourne une chaîne représentant l'entité
	 * TODO ajouter des tabulations
	 */
	@SuppressWarnings("unchecked")
	@Override
	public String toString(){
		String result = new String();
		result = "("+this.getKeyword();
		if(this.getValue().getClass().equals(new String().getClass())){
			result += " "+ this.getValue().toString();
		} else {
			result += "\n";
			Iterator<Entity> iterator = ((List<Entity>)this.getValue()).iterator();
			while(iterator.hasNext()){
				result += iterator.next().toString();
			}
		}
		result += ")\n";
		return result;
	}
}
