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

package fr.crnan.videso3d;

import gov.nasa.worldwind.avlist.AVKey;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Singleton gérant les différentes propriétés de Videso 3D.
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public final class Configuration { 

	//Liste des propriétés accessibles
	public static final String COLOR_FOND_PAYS = "fr.crnan.videso3d.color.fond_pays";
	public static final String COLOR_BALISE_MARKER = "fr.crnan.videso3d.color.balise_marker";
	public static final String COLOR_BALISE_TEXTE = "fr.crnan.videso3d.color.balise_texte";
	
	public static final String NETWORK_PROXY_HOST = "fr.crnan.videso3d.network.proxy_host";
	public static final String NETWORK_PROXY_PORT = "fr.crnan.videso3d.network.proxy_port";
	public static final String DEFAULT_REP = "fr.crnan.videso3d.files.defaultrep";
	
	public static final String TRAJECTOGRAPHIE_SEUIL = "fr.crnan.videso3d.trajectographie.seuil";
	
	private static Configuration instance = new Configuration();

	private Properties properties;

	private PropertyChangeSupport changes = new PropertyChangeSupport(this);
	
	private File fichier;

	private Configuration(){
		properties = new Properties();
		fichier = new File("videso3d.properties");
		try {
			properties.load(new FileInputStream(fichier)); // Chargement du fichier de configuration
		} catch(FileNotFoundException e) {
			try {
				fichier.createNewFile();
				properties.load(new FileInputStream(fichier));
			} catch (IOException e1) {
				e1.printStackTrace();
			}			
		} catch(IOException e) {
			e.printStackTrace();			
		}
	}
	
	/**
	 * Met en place le proxy si besoin
	 */
	public static void initializeProxy(){
		if(!getProperty(NETWORK_PROXY_HOST, "").isEmpty()){
			gov.nasa.worldwind.Configuration.setValue(AVKey.URL_PROXY_HOST, getProperty(NETWORK_PROXY_HOST, ""));
			gov.nasa.worldwind.Configuration.setValue(AVKey.URL_PROXY_PORT, getProperty(NETWORK_PROXY_PORT, ""));
			gov.nasa.worldwind.Configuration.setValue(AVKey.URL_PROXY_TYPE, "Proxy.Type.Http");
		}
	}
	
	/**
	 * Enregistre une propriété
	 * @param key Clef de la propriéré
	 * @param value Valeur de la propriété
	 */
	public static void setProperty(String key, String value){
		Object oldValue = instance.properties.setProperty(key, value);
		try{
			instance.properties.store(new FileOutputStream(instance.fichier), "Fichier de configuration de Videso 3D");
		} catch(IOException e) {}
		instance.changes.firePropertyChange(key, oldValue, value);
	}

	/**
	 * Récupére la valeur d'une propriété
	 * @param clef Clef de la propriété
	 * @return la valeur de la propriété recherché, <code>defaut</code> si aucune propriété trouvée
	 */
	public static String getProperty(String clef, String defaut){
		return instance.properties.getProperty(clef, defaut);
	}

	public static void addPropertyChangeListener(PropertyChangeListener listener){
		instance.changes.addPropertyChangeListener(listener);
	}
	
	public static void removePropertyChangeListener(PropertyChangeListener listener){
		instance.changes.removePropertyChangeListener(listener);
	}
}
