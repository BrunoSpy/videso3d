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

package fr.crnan.videso3d.ihm;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;


import fr.crnan.videso3d.VidesoGLCanvas;

/**
 * Listener du panel StipView
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class StipViewListener {


	/**
	 * Listener de la checkbox AWY
	 * @author Bruno Spyckerelle
	 */
	private class RouteAwyListener implements ItemListener {
		@Override
		public void itemStateChanged(ItemEvent e) {
			if(e.getStateChange() == ItemEvent.SELECTED ) {
				wwd.toggleLayer(wwd.getRoutesAwy(), true);
			} else {
				wwd.toggleLayer(wwd.getRoutesAwy(), false);
			}
		}	
	}
	/**
	 * Listener de la checkbox PDR
	 * @author Bruno Spyckerelle
	 */
	private class RoutePDRListener implements ItemListener {
		@Override
		public void itemStateChanged(ItemEvent e) {
			if(e.getStateChange() == ItemEvent.SELECTED ) {
				wwd.toggleLayer(wwd.getRoutesPDR(), true);
			} else {
				wwd.toggleLayer(wwd.getRoutesPDR(), false);
			}
		}	
	}
	/**
	 * Listener de la checkbox Balises publiées
	 * @author Bruno Spyckerelle
	 *
	 */
	private class BalisesPubListener implements ItemListener {
		@Override
		public void itemStateChanged(ItemEvent e) {
			if(e.getStateChange() == ItemEvent.SELECTED ) {
				wwd.toggleLayer(wwd.getBalisesPubMarkers(), true);
				wwd.toggleLayer(wwd.getBalisesPubTexts(), true);
			} else {
				wwd.toggleLayer(wwd.getBalisesPubMarkers(), false);
				wwd.toggleLayer(wwd.getBalisesPubTexts(), false);
			}
		}	
	}

	/**
	 * Listener de la checkbox Balises non publiées
	 * @author Bruno Spyckerelle
	 *
	 */
	private class BalisesNPListener implements ItemListener {
		@Override
		public void itemStateChanged(ItemEvent e) {
			if(e.getStateChange() == ItemEvent.SELECTED ) {
				wwd.toggleLayer(wwd.getBalisesNPMarkers(), true);
				wwd.toggleLayer(wwd.getBalisesNPTexts(), true);
			} else {
				wwd.toggleLayer(wwd.getBalisesNPMarkers(), false);
				wwd.toggleLayer(wwd.getBalisesNPTexts(), false);
			}
		}	
	}

	private VidesoGLCanvas wwd;

	private RouteAwyListener routesAwy = new RouteAwyListener();
	private RoutePDRListener routesPDR = new RoutePDRListener();

	private BalisesPubListener balisesPub = new BalisesPubListener();
	private BalisesNPListener balisesNP = new BalisesNPListener();

	public StipViewListener(VidesoGLCanvas wwd){
		this.wwd = wwd;
	}
	public RouteAwyListener getRouteAwyListener(){
		return routesAwy;
	}

	public RoutePDRListener getRoutePDRListener(){
		return routesPDR;
	}
	public BalisesPubListener getBalisesPubListener() {
		return balisesPub;
	}
	public BalisesNPListener getBalisesNPListener() {
		return balisesNP;
	}
	
}
