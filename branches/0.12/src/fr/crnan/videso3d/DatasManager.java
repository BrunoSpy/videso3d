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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;

import fr.crnan.videso3d.databases.DatabaseManager;
import fr.crnan.videso3d.databases.DatabaseManager.Type;
import fr.crnan.videso3d.databases.aip.AIPContext;
import fr.crnan.videso3d.databases.aip.AIPController;
import fr.crnan.videso3d.databases.edimap.EdimapContext;
import fr.crnan.videso3d.databases.edimap.EdimapController;
import fr.crnan.videso3d.databases.exsa.STRContext;
import fr.crnan.videso3d.databases.exsa.STRController;
import fr.crnan.videso3d.databases.radio.RadioCovContext;
import fr.crnan.videso3d.databases.radio.RadioCovController;
import fr.crnan.videso3d.databases.skyview.SkyViewContext;
import fr.crnan.videso3d.databases.skyview.SkyViewController;
import fr.crnan.videso3d.databases.stip.StipContext;
import fr.crnan.videso3d.databases.stip.StipController;
import fr.crnan.videso3d.databases.stpv.StpvContext;
import fr.crnan.videso3d.databases.stpv.StpvController;
import fr.crnan.videso3d.ihm.AIPView;
import fr.crnan.videso3d.ihm.EdimapView;
import fr.crnan.videso3d.ihm.RadioCovView;
import fr.crnan.videso3d.ihm.SkyView;
import fr.crnan.videso3d.ihm.StipView;
import fr.crnan.videso3d.ihm.StpvView;
import fr.crnan.videso3d.ihm.StrView;
import fr.crnan.videso3d.ihm.components.DataView;
import gov.nasa.worldwind.util.Logging;

/**
 * Singleton gèrant les données sélectionnées.<br />
 * Enregistre les ccontroleurs, les contexts de façon à partager ces éléments avec<br />
 * parties de l'application qui en ont besoin.
 * @author Bruno Spyckerelle
 * @version 0.1.2
 */
public final class DatasManager {

	private static DatasManager instance = new DatasManager();
	
	private HashMap<DatabaseManager.Type, VidesoController> controllers = new HashMap<DatabaseManager.Type, VidesoController>();
	
	private HashMap<DatabaseManager.Type, Context> contexts = new HashMap<DatabaseManager.Type, Context>();
	
	private HashMap<DatabaseManager.Type, DataView> views = new HashMap<DatabaseManager.Type, DataView>();
		
	private PropertyChangeSupport support;
	
	private DatasManager(){
		super();
		support = new PropertyChangeSupport(this);
	}
	
	public static void addDatas(DatabaseManager.Type type, VidesoController controller, Context context, DataView view){
		instance.controllers.put(type, controller);
		instance.contexts.put(type, context);
		instance.views.put(type, view);
	}
	
	public static VidesoController getController(DatabaseManager.Type type){
		return instance.controllers.get(type);
	}
	
	public static Context getContext(DatabaseManager.Type type){
		return instance.contexts.get(type);
	}
	
	public static DataView getView(DatabaseManager.Type type){
		return instance.views.get(type);
	}
	
	/**
	 * Create and replace controller and context of the given type
	 * @param type
	 * @throws Exception 
	 */
	public static void createDatas(DatabaseManager.Type type, VidesoGLCanvas wwd) throws Exception{
		deleteDatas(type);
		if(DatabaseManager.getCurrent(type) != null){
			switch (type) {
			case STIP:
				DatasManager.addDatas(type, new StipController(wwd), new StipContext(), new StipView());
				break;
			case STPV:
				DatasManager.addDatas(type, new StpvController(wwd), new StpvContext(), new StpvView());
				break;
			case EXSA:
				DatasManager.addDatas(type, new STRController(wwd), new STRContext(), new StrView());
				break;
			case Edimap:
				DatasManager.addDatas(type, new EdimapController(wwd), new EdimapContext(), new EdimapView());
				break;
			case AIP:
				DatasManager.addDatas(type, new AIPController(wwd), new AIPContext(), new AIPView());
				break;
			case RadioCov:
				//la vue radio a besoin du controlleur ...
				//d'où obligation d'enregistrer le controleur
				instance.controllers.put(type, new RadioCovController(wwd));
				instance.contexts.put(type, new RadioCovContext());
				instance.views.put(type, new RadioCovView());
				break;
			case SkyView:
				DatasManager.addDatas(type, new SkyViewController(wwd), new SkyViewContext(), new SkyView());
				break;
			default:
				Logging.logger().severe("Type "+type+" inconnu");
				throw new Exception("Type "+type+" inconnu");
			}
		}
		instance.support.firePropertyChange("done", null, true);
	}
	
	public static void deleteDatas(DatabaseManager.Type type){
		if(instance.controllers.containsKey(type)) {
			instance.controllers.get(type).removeAllLayers();
			instance.controllers.remove(type);
		}
		if(instance.views.containsKey(type)) instance.views.remove(type);
		if(instance.contexts.containsKey(type)) instance.contexts.remove(type);
	}
	
	public static int getNumberInitSteps(Type t){
		switch (t) {
		case STIP:
			return StipController.getNumberInitSteps();
		case EXSA:
			return STRController.getNumberInitSteps();
		case Edimap:
			return EdimapController.getNumberInitSteps();
		case SkyView:
			return SkyViewController.getNumberInitSteps();
		case AIP:
			return AIPController.getNumberInitSteps();
		case STPV:
			return StpvController.getNumberInitSteps();
		case RadioCov:
			return RadioCovController.getNumberInitSteps();
		default:
			return 0;
		}
	}

	public static int numberViews(){
		return instance.views.size();
	}
	
	public static Iterable<DataView> getViews() {
		return instance.views.values();
	}
	
	/* ****************************************************** */
	/* *************** Gestion des listeners **************** */
	/* ****************************************************** */
	
	public static void addPropertyChangeListener(PropertyChangeListener l){
		instance.support.addPropertyChangeListener(l);
	}
	
	public static void addPropertyChangeListener(String propertyName, PropertyChangeListener l){
		instance.support.addPropertyChangeListener(propertyName, l);
	}
	
	public static void removePropertyChangeListener(PropertyChangeListener l){
		instance.support.removePropertyChangeListener(l);
	}
	
	public static void removePropertyChangeListener(String propertyName, PropertyChangeListener l){
		instance.support.removePropertyChangeListener(propertyName, l);
	}
}
