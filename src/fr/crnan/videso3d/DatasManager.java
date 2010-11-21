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

import java.util.HashMap;

import fr.crnan.videso3d.aip.AIPContext;
import fr.crnan.videso3d.aip.AIPController;
import fr.crnan.videso3d.edimap.EdimapContext;
import fr.crnan.videso3d.edimap.EdimapController;
import fr.crnan.videso3d.exsa.STRContext;
import fr.crnan.videso3d.exsa.STRController;
import fr.crnan.videso3d.ihm.AIPView;
import fr.crnan.videso3d.ihm.EdimapView;
import fr.crnan.videso3d.ihm.RadioCovView;
import fr.crnan.videso3d.ihm.SkyView;
import fr.crnan.videso3d.ihm.StipView;
import fr.crnan.videso3d.ihm.StpvView;
import fr.crnan.videso3d.ihm.StrView;
import fr.crnan.videso3d.ihm.components.DataView;
import fr.crnan.videso3d.radio.RadioCovContext;
import fr.crnan.videso3d.radio.RadioCovController;
import fr.crnan.videso3d.skyview.SkyViewContext;
import fr.crnan.videso3d.skyview.SkyViewController;
import fr.crnan.videso3d.stip.StipContext;
import fr.crnan.videso3d.stip.StipController;
import fr.crnan.videso3d.stpv.StpvContext;
import fr.crnan.videso3d.stpv.StpvController;
import gov.nasa.worldwind.util.Logging;

/**
 * Singleton gèrant les données sélectionnées.<br />
 * Enregistre les ccontroleurs, les contexts de façon à partager ces éléments avec<br />
 * parties de l'application qui en ont besoin.
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public final class DatasManager {

	private static DatasManager instance = new DatasManager();
	
	private HashMap<DatabaseManager.Type, VidesoController> controllers = new HashMap<DatabaseManager.Type, VidesoController>();
	
	private HashMap<DatabaseManager.Type, Context> contexts = new HashMap<DatabaseManager.Type, Context>();
	
	private HashMap<DatabaseManager.Type, DataView> views = new HashMap<DatabaseManager.Type, DataView>();
	
	private DatasManager(){
		super();
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
		if(instance.controllers.containsKey(type)) instance.controllers.remove(type);
		if(instance.views.containsKey(type)) instance.views.remove(type);
		if(instance.contexts.containsKey(type)) instance.contexts.remove(type);
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
			DatasManager.addDatas(type, new RadioCovController(wwd), new RadioCovContext(), new RadioCovView());
			break;
		case SkyView:
			DatasManager.addDatas(type, new SkyViewController(wwd), new SkyViewContext(), new SkyView());
			break;
		default:
			Logging.logger().severe("Type "+type+" inconnu");
			throw new Exception("Type "+type+" inconnu");
		}
	}
	
}
