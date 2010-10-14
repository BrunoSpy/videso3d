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

import java.awt.Component;
import java.awt.Cursor;
import java.io.File;
import java.util.ArrayList;

import javax.xml.xpath.XPath;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import fr.crnan.videso3d.formats.TrackFilesReader;
import fr.crnan.videso3d.formats.geo.GEOReader;
import fr.crnan.videso3d.formats.lpln.LPLNReader;
import fr.crnan.videso3d.formats.opas.OPASReader;
import fr.crnan.videso3d.globes.EarthFlatCautra;
import fr.crnan.videso3d.globes.FlatGlobeCautra;
import fr.crnan.videso3d.layers.FrontieresStipLayer;
import fr.crnan.videso3d.layers.GEOTracksLayer;
import fr.crnan.videso3d.layers.LPLNTracksLayer;
import fr.crnan.videso3d.layers.OPASTracksLayer;
import fr.crnan.videso3d.layers.TrajectoriesLayer;
import fr.crnan.videso3d.layers.VAnnotationLayer;
import fr.crnan.videso3d.util.VMeasureTool;
import fr.crnan.videso3d.layers.RadioCovLayer;

import gov.nasa.worldwind.Factory;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.examples.util.LayerManagerLayer;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.globes.Earth;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.AirspaceLayer;
import gov.nasa.worldwind.layers.AnnotationLayer;
import gov.nasa.worldwind.layers.LatLonGraticuleLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.SkyColorLayer;
import gov.nasa.worldwind.layers.SkyGradientLayer;
import gov.nasa.worldwind.layers.placename.PlaceNameLayer;
import gov.nasa.worldwind.render.airspaces.Airspace;
import gov.nasa.worldwind.tracks.Track;
import gov.nasa.worldwind.util.DataConfigurationFilter;
import gov.nasa.worldwind.util.DataConfigurationUtils;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.WWIO;
import gov.nasa.worldwind.util.WWXML;
import gov.nasa.worldwind.view.orbit.BasicOrbitView;
import gov.nasa.worldwind.view.orbit.FlatOrbitView;
/**
 * Extension de WorldWindCanvas prenant en compte la création d'éléments 3D
 * @author Bruno Spyckerelle
 * @version 0.8.0
 */
@SuppressWarnings("serial")
public class VidesoGLCanvas extends WorldWindowGLCanvas {



	/**
	 * Layer contenant les annotations
	 */
	private AnnotationLayer annotationLayer;
	/**
	 * Layer pour les frontières
	 */
	private FrontieresStipLayer frontieres;
	/**
	 * Projection 2D
	 */
	private FlatGlobeCautra flatGlobe;
	private Globe roundGlobe;
	private String projection;
	/**
	 * Outil de mesure (alidade)
	 */
	private VMeasureTool measureTool;	
	
	/**
	 * Liste des layers couvertures radios
	 */
	 private RadioCovLayer radioCovLayer;
	

//	private Layer lastLayer;
	private AirspaceLayer selectedAirspaces = new AirspaceLayer();
		
	/**
	 * Initialise les différents objets graphiques
	 */
	public void initialize(){		
		
		//Proxy
		Configuration.initializeProxy();

		//Latitudes et longitudes
		Layer latlon = new LatLonGraticuleLayer();
		latlon.setEnabled(false);
		this.getModel().getLayers().add(latlon);
				
		//on screen layer manager
		LayerManagerLayer layerManager = new LayerManagerLayer(this);
		layerManager.setEnabled(false); //réduit par défaut
		this.getModel().getLayers().add(0, layerManager);
						
		//mise à jour des calques de WorldWindInstalled
		firePropertyChange("step", "", "Ajout des layers installés");
		this.updateWWI();
		
		//layer d'accueil des objets séléctionnés
		this.getModel().getLayers().add(selectedAirspaces);
		
		if (isFlatGlobe())
		{
			this.flatGlobe = (FlatGlobeCautra)this.getModel().getGlobe();
			this.roundGlobe = new Earth();
		}
		else
		{
			this.flatGlobe = new EarthFlatCautra();
			this.roundGlobe = this.getModel().getGlobe();
		}
		
		//Layer des radio couv
		radioCovLayer = new RadioCovLayer("Radio Coverage",this);
		
		//position de départ centrée sur la France
		this.getView().setEyePosition(Position.fromDegrees(47, 0, 2500e3));
		
	}
	
	public AnnotationLayer getAnnotationLayer(){
		if(annotationLayer == null){
			annotationLayer = new VAnnotationLayer();
			this.getModel().getLayers().add(annotationLayer);
		}
		return annotationLayer;
	}
	
	/**
	 * Affiche ou non un Layer<br />
	 * Ajouter le {@link Layer} aux layers du modèle si il n'en fait pas partie
	 * @param layer {@link Layer} à afficher/enlever
	 * @param state {@link Boolean}
	 */
	public void toggleLayer(Layer layer, Boolean state){
		if (layer != null) {
			this.getModel().getLayers().addIfAbsent(layer);
			layer.setEnabled(state);
		}
	}

	/**
	 * Ajoute un calque à la fin de la liste. Le calque est affiché par défaut. 
	 * @param layer Calque à ajouter
	 */
	public void addLayer(Layer layer) throws Exception{
			this.toggleLayer(layer, true);
	}
	
	/**
	 * Supprime un calque
	 * @param layer Calque à supprimer
	 */
	public void removeLayer(Layer layer){
		this.getModel().getLayers().remove(layer);
	}
	
	/**
	 * Insère un layer suffisamment haut dans la liste pour être derrière les PlaceNames
	 * @param layer
	 */
    public void insertBeforePlacenames(Layer layer)
    {
        // Insert the layer into the layer list just before the placenames.
        int position = 0;
        LayerList layers = this.getModel().getLayers();
        for (Layer l : layers)
        {
            if (l instanceof PlaceNameLayer)
                position = layers.indexOf(l);
        }
        layers.add(position, layer);
    }
    
	/**
	 * Mets à jour les layers installés dans WorldWindInstalled
	 */
	public void updateWWI(){
		//code inspired by gov.nasa.worldwind.examples.ImportingImagesAndElevationsDemo.java
		
		File installLocation = null;
		for (java.io.File f : WorldWind.getDataFileStore().getLocations())
		{
			if (WorldWind.getDataFileStore().isInstallLocation(f.getPath()))
			{
				installLocation = f;
				break;
			}
		}
		
		String[] names = WWIO.listDescendantFilenames(installLocation, new DataConfigurationFilter(), false);
        if (names == null || names.length == 0)
            return;

        for (String filename : names)
        {
            Document dataConfig = null;

            try
            {
                File dataConfigFile = new File(installLocation, filename);
                dataConfig = WWXML.openDocument(dataConfigFile);
                dataConfig = DataConfigurationUtils.convertToStandardDataConfigDocument(dataConfig);
            }
            catch (WWRuntimeException e)
            {
                e.printStackTrace();
            }

            if (dataConfig == null)
                continue;

            AVList params = new AVListImpl();            
            XPath xpath = WWXML.makeXPath();
            Element domElement = dataConfig.getDocumentElement();

            // If the data configuration document doesn't define a cache name, then compute one using the file's path
            // relative to its file cache directory.
            String s = WWXML.getText(domElement, "DataCacheName", xpath);
            if (s == null || s.length() == 0)
                DataConfigurationUtils.getDataConfigCacheName(filename, params);

            // If the data configuration document doesn't define the data's extreme elevations, provide default values using
            // the minimum and maximum elevations of Earth.
            String type = DataConfigurationUtils.getDataConfigType(domElement);
            if (type.equalsIgnoreCase("ElevationModel"))
            {
                if (WWXML.getDouble(domElement, "ExtremeElevations/@min", xpath) == null)
                    params.setValue(AVKey.ELEVATION_MIN, -11000d); // Depth of Mariana trench.
                if (WWXML.getDouble(domElement, "ExtremeElevations/@max", xpath) == null)
                    params.setValue(AVKey.ELEVATION_MAX, 8500d); // Height of Mt. Everest.
            }
                       
            if (DataConfigurationUtils.getDataConfigType(domElement).equalsIgnoreCase("Layer"))
            {
            	Layer layer = null;
                try
                {
                    Factory factory = (Factory) WorldWind.createConfigurationComponent(AVKey.LAYER_FACTORY);
                    layer = (Layer) factory.createFromConfigSource(domElement, params);
                }
                catch (Exception e)
                {
                    String message = Logging.getMessage("generic.CreationFromDataConfigurationFailed", 
                    		DataConfigurationUtils.getDataConfigDisplayName(domElement));
                    Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
                }

                if (layer == null)
                    return;
                
                if (!this.getModel().getLayers().contains(layer)) {
                	this.insertBeforePlacenames(layer);
                	layer.setEnabled(false);
                }
            }
            
        }            
	}
	
	/*--------------------------------------------------------------*/
	/*---------------------- Outil de mesure -----------------------*/
	/*--------------------------------------------------------------*/
	
	public VMeasureTool getMeasureTool(){
		if(measureTool == null){
			measureTool = new VMeasureTool(this);
		}
		return measureTool;
	}
	
	public void switchMeasureTool(Boolean bool){
		this.getMeasureTool().setArmed(bool);
		//Changement du curseur
		((Component) this).setCursor(!measureTool.isArmed() ? Cursor.getDefaultCursor()
                : Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		if(!bool){
			this.getMeasureTool().clear();
		}
	}
	
	/*--------------------------------------------------------------*/
	/*----------------- Gestion des projections --------------------*/
	/*--------------------------------------------------------------*/
	
	/**
	 * Change la projection
	 * @param projection Nom de la projection (parmi {@link FlatGlobeCautra})
	 */
	public void setProjection(String projection){
		this.projection = projection;
		if(flatGlobe != null) this.flatGlobe.setProjection(projection);
		if(isFlatGlobe()) this.redraw();
	}
	
    public boolean isFlatGlobe()
    {
        return this.getModel().getGlobe() instanceof FlatGlobeCautra;
    }
	
    public String getProjection(){
    	return projection;
    }
    /**
     * Active la vue 2D
     * @param flat
     */
    public void enableFlatGlobe(boolean flat)
    {
        if(isFlatGlobe() == flat)
            return;

        if(!flat)
        {
            // Switch to round globe
            this.getModel().setGlobe(roundGlobe) ;
            // Switch to orbit view and update with current position
            FlatOrbitView flatOrbitView = (FlatOrbitView)this.getView();
            BasicOrbitView orbitView = new BasicOrbitView();
            orbitView.setCenterPosition(flatOrbitView.getCenterPosition());
            orbitView.setZoom(flatOrbitView.getZoom( ));
            orbitView.setHeading(flatOrbitView.getHeading());
            orbitView.setPitch(flatOrbitView.getPitch());
            this.setView(orbitView);
            // Change sky layer
            LayerList layers = this.getModel().getLayers();
            for(int i = 0; i < layers.size(); i++)
            {
                if(layers.get(i) instanceof SkyColorLayer)
                    layers.set(i, new SkyGradientLayer());
            }
        }
        else
        {
            // Switch to flat globe
            this.getModel().setGlobe(flatGlobe);
            flatGlobe.setProjection(this.getProjection());
            // Switch to flat view and update with current position
            BasicOrbitView orbitView = (BasicOrbitView)this.getView();
            FlatOrbitView flatOrbitView = new FlatOrbitView();
            flatOrbitView.setCenterPosition(orbitView.getCenterPosition());
            flatOrbitView.setZoom(orbitView.getZoom( ));
            flatOrbitView.setHeading(orbitView.getHeading());
            flatOrbitView.setPitch(orbitView.getPitch());
            this.setView(flatOrbitView);
            // Change sky layer
            LayerList layers = this.getModel().getLayers();
            for(int i = 0; i < layers.size(); i++)
            {
                if(layers.get(i) instanceof SkyGradientLayer)
                    layers.set(i, new SkyColorLayer());
            }
        }
        
        this.redraw();
    }
    
	/*--------------------------------------------------------------*/
	/*----------------- Gestion des frontières ---------------------*/
	/*--------------------------------------------------------------*/
	/**
	 * Affiche ou non le fond uni suivant les frontières Stip
	 * @param toggle
	 */
    public void toggleFrontieres(Boolean toggle){
			if(frontieres == null){
				frontieres = new FrontieresStipLayer();
				this.insertBeforePlacenames(frontieres);
			}
			this.toggleLayer(frontieres, toggle);
	}
	
	/*-------------------------------------------------------------------*/
	/*----------------- Gestion des couvertures radios ------------------*/
	/*-------------------------------------------------------------------*/    
	
    public void addRadioCov(String antennaName) {    	
    	radioCovLayer.addVisibleRadioCov(antennaName);
    	this.redraw();
    }
    
    public void removeRadioCov(String antennaName) {    
    	radioCovLayer.removeVisibleRadioCov(antennaName);
    	this.redraw();
    }
    
    public void hideAllRadioCovLayers() {
    	radioCovLayer.hideAllRadioCovLayers();
    	this.redraw();    
    }
        
    public void removeAllRadioCovLayers() {
    	radioCovLayer.removeAllRadioCovLayers();
    	this.redraw();
    }
    
    public void insertAllRadioCovLayers() {
    	radioCovLayer.insertAllRadioCovLayers();
    	this.redraw();
    }
    public void insertAllRadioCovLayers(ArrayList<Airspace> airspaces) {
    	radioCovLayer.insertAllRadioCovLayers(airspaces);
    	this.redraw();
    }
    
	/*--------------------------------------------------------------*/
	/*------------------ Gestion du highlight ----------------------*/
	/*--------------------------------------------------------------*/


	/**
	 * Ajoute les trajectoires à la vue
	 * @param file Fichier contenant les trajectoires
	 */
	public TrajectoriesLayer addTrajectoires(TrackFilesReader reader){
		if(reader instanceof LPLNReader){
			return this.addTrajectoires((LPLNReader)reader);
		} else if(reader instanceof GEOReader){
			return this.addTrajectoires((GEOReader)reader);
		} else if(reader instanceof OPASReader){
			return this.addTrajectoires((OPASReader)reader);
		}
		return null;
	}
	/**
	 * Ajoute des trajectoires au format LPLN
	 * @param lpln
	 * @return {@link Layer}
	 */
	private TrajectoriesLayer addTrajectoires(LPLNReader lpln){
		LPLNTracksLayer trajLayer = new LPLNTracksLayer();
		trajLayer.setName(lpln.getName());
		this.toggleLayer(trajLayer, true);
		for(Track track : lpln.getTracks()){
			trajLayer.addTrack(track);
		}
		return trajLayer;
	}
	
	/**
	 * Ajoute les trajectoires au format Elvira GEO
	 * @param geo
	 * @return {@link Layer}
	 */
	private TrajectoriesLayer addTrajectoires(GEOReader geo) {
		GEOTracksLayer trajLayer = new GEOTracksLayer();
		trajLayer.setName(geo.getName());
		if(geo.getTracks().size() > Integer.parseInt(Configuration.getProperty(Configuration.TRAJECTOGRAPHIE_SEUIL, "20"))) {
			//au delà de x tracks, on change les paramètres de façon à ne pas perdre en perfo
			trajLayer.setStyle(TrajectoriesLayer.STYLE_SIMPLE);
			trajLayer.setTracksHighlightable(true);
		}
		this.toggleLayer(trajLayer, true);

		for(Track track : geo.getTracks()){
			trajLayer.addTrack(track);
		}
		return trajLayer;
	}

	/**
	 * Ajoute les trajectoires au format OPAS
	 * @param opasReader
	 */
	public TrajectoriesLayer addTrajectoires(OPASReader opas) {
		OPASTracksLayer trajLayer = new OPASTracksLayer();
		trajLayer.setName(opas.getName());
		this.toggleLayer(trajLayer, true);
		for(Track track : opas.getTracks()){
			trajLayer.addTrack(track);
		}
		return trajLayer;
	}

	/**
	 * Nombre d'étapes de l'initialisation (utile pour le splashscreen)
	 * @return int
	 */
	public int getNumberInitSteps() {
		return 6;
	}

	/**
	 * Recentre la vue
	 */
	public void resetView() {

		if(this.annotationLayer != null) this.annotationLayer.removeAllAnnotations();
		
		this.getView().stopMovement();
		this.getView().setEyePosition(Position.fromDegrees(47, 0, 2500e3));
		this.getView().setPitch(Angle.ZERO);
		this.getView().setHeading(Angle.ZERO);
		this.redraw();
	}
}
