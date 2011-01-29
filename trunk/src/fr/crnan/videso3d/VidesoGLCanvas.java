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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileFilter;
import java.util.Iterator;
import java.util.List;

import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;
import javax.xml.xpath.XPath;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import fr.crnan.videso3d.formats.TrackFilesReader;
import fr.crnan.videso3d.formats.fpl.FPLReader;
import fr.crnan.videso3d.formats.geo.GEOReader;
import fr.crnan.videso3d.formats.lpln.LPLNReader;
import fr.crnan.videso3d.formats.opas.OPASReader;
import fr.crnan.videso3d.globes.EarthFlatCautra;
import fr.crnan.videso3d.globes.FlatGlobeCautra;
import fr.crnan.videso3d.graphics.Aerodrome;
import fr.crnan.videso3d.graphics.Balise2D;
import fr.crnan.videso3d.graphics.Route;
import fr.crnan.videso3d.graphics.Secteur3D;
import fr.crnan.videso3d.layers.FPLTracksLayer;
import fr.crnan.videso3d.layers.FrontieresStipLayer;
import fr.crnan.videso3d.layers.GEOTracksLayer;
import fr.crnan.videso3d.layers.LPLNTracksLayer;
import fr.crnan.videso3d.layers.OPASTracksLayer;
import fr.crnan.videso3d.layers.TrajectoriesLayer;
import fr.crnan.videso3d.layers.VAnnotationLayer;
import fr.crnan.videso3d.layers.VerticalScaleBar;
import fr.crnan.videso3d.util.VMeasureTool;

import gov.nasa.worldwind.BasicFactory;
import gov.nasa.worldwind.Factory;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.cache.FileStore;
import gov.nasa.worldwind.data.DataImportUtil;
import gov.nasa.worldwind.data.TiledImageProducer;
import gov.nasa.worldwind.examples.util.LayerManagerLayer;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.globes.Earth;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.AnnotationLayer;
import gov.nasa.worldwind.layers.LatLonGraticuleLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.SkyColorLayer;
import gov.nasa.worldwind.layers.SkyGradientLayer;
import gov.nasa.worldwind.layers.placename.PlaceNameLayer;
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
 * @version 0.8.1
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

	private VerticalScaleBar scale;
	
	private DraggerListener dragger;
	
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

		//dragger
		this.dragger = new DraggerListener(this);
		this.addSelectListener(dragger);
		
		//on screen layer manager
		LayerManagerLayer layerManager = new LayerManagerLayer(this);
		layerManager.setEnabled(false); //réduit par défaut
		this.getModel().getLayers().add(0, layerManager);

		//mise à jour des calques de WorldWindInstalled
		firePropertyChange("step", "", "Ajout des layers installés");
		this.updateWWI();

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
	/*----------------- Gestion des trajectoires       ------------------*/
	/*-------------------------------------------------------------------*/    

	//TODO créer le controleur correspondant
	
	

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
		} else if(reader instanceof FPLReader){
			return this.addTrajectoires((FPLReader)reader);
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
			if(geo.getTracks().size() > Integer.parseInt(Configuration.getProperty(Configuration.TRAJECTOGRAPHIE_SEUIL_PRECISION, "100"))){
				trajLayer.setPrecision(Double.parseDouble(Configuration.getProperty(Configuration.TRAJECTOGRAPHIE_PRECISION, "0.02")));
			}
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
	 * Ajoute les trajectoires plan de vol
	 * @param opasReader
	 */
	public TrajectoriesLayer addTrajectoires(FPLReader fplR) {
		FPLTracksLayer trajLayer = new FPLTracksLayer();
		trajLayer.setName(fplR.getName());
		this.toggleLayer(trajLayer, true);
		for(Track track : fplR.getTracks()){
			trajLayer.addTrack(track);
		}
		return trajLayer;
	}
	
	
	/**
	 * Nombre d'étapes de l'initialisation (utile pour le splashscreen)
	 * @return int
	 */
	public int getNumberInitSteps() {
		return 7;
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


	/**
	 * Calcule l'altitude à laquelle doit se trouver la caméra pour voir correctement l'objet, 
	 * et la position sur laquelle elle doit être centrée. 
	 * @param object
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public double[] computeBestEyePosition(Object object){
		if(object instanceof Secteur3D){
			return computeBestEyePosition((Secteur3D)object);
		}else if (object instanceof Balise2D){
			return computeBestEyePosition((Balise2D)object);
		}else if(object instanceof Aerodrome){
			Position ref = ((Aerodrome) object).getRefPosition();
			return new double[]{ref.latitude.degrees, ref.longitude.degrees, 50000};
		}else if(object instanceof List){
			if(((List<?>)object).get(0) instanceof Route){
				return computeBestEyePosition((List<? extends Route>)object);
			}
		}
		return null;
	}
	
	/**
	 * Calcule l'altitude à laquelle doit se trouver l'oeil pour voir correctement la zone.
	 * @param zone
	 * @return 
	 */
	public double[] computeBestEyePosition(Secteur3D zone){
		List<LatLon> locations = zone.getLocations();
		Angle latMin = locations.get(0).latitude;
		Angle latMax = latMin;
		Angle lonMin = locations.get(0).longitude;
		Angle lonMax = lonMin;
		int step = 1;
		if(locations.size()>200)
			step = locations.size()/200+1;
		for (int i = 0; i<locations.size();i+=step){
			Angle lat = locations.get(i).latitude;
			Angle lon = locations.get(i).longitude;
			if(lat.compareTo(latMax)>0){
				latMax = lat;
			}
			if(lat.compareTo(latMin)<0){
				latMin = lat;
			}
			if(lon.compareTo(lonMax)>0){
				lonMax = lon;
			}
			if(lon.compareTo(lonMin)<0){
				lonMin = lon;
			}
		}
		double er = this.getView().getGlobe().getEquatorialRadius();
		double pr = this.getView().getGlobe().getPolarRadius();
		double maxDistance = LatLon.ellipsoidalDistance(new LatLon(latMin,lonMin), new LatLon(latMax,lonMax), er, pr);

		double elevation = computeBestElevation(maxDistance);
		return new double[]{(latMin.degrees+latMax.degrees)/2,(lonMin.degrees+lonMax.degrees)/2, Math.min(elevation,2.5e6)};
	}

	
	
	
	@SuppressWarnings("unchecked")
	public double[] computeBestEyePosition(List<? extends Route> segments){
		
			//Calcul de la hauteur idéale de la caméra
			LatLon firstLocation = (LatLon) ((Route) segments.get(0)).getLocations().iterator().next();
			LatLon secondLocation = null;
			if(segments.size()>1){
				secondLocation = ((Route) segments.get(segments.size()-1)).getLocations().iterator().next();
			}else{
				Iterator<LatLon> it = (Iterator<LatLon>) ((Route) segments.get(0)).getLocations().iterator();
				while(it.hasNext()){
					secondLocation = it.next();
				}
			}
			Angle lat1 = firstLocation.latitude;
			Angle lon1 = firstLocation.longitude;
			Angle lat2 = secondLocation.latitude;
			Angle lon2 = secondLocation.longitude;
			
			double er = this.getView().getGlobe().getEquatorialRadius();
			double pr = this.getView().getGlobe().getPolarRadius();
			double distance = LatLon.ellipsoidalDistance(new LatLon(lat1,lon1), new LatLon(lat2,lon2), er, pr);
			double elevation = computeBestElevation(distance);
			LatLon middleSegmentLocation = ((Route) segments.get(segments.size()/2)).getLocations().iterator().next();
			return new double[]{middleSegmentLocation.latitude.degrees, middleSegmentLocation.longitude.degrees, Math.min(elevation,2.5e6)};
			
	}
	
	
	public double[] computeBestEyePosition(Balise2D navFix){
		return new double[]{navFix.getPosition().latitude.degrees, navFix.getPosition().longitude.degrees, 2e5};
	}
	
	
	public double computeBestElevation(double distance){
		return -6e-7*distance*distance+2.3945*distance+175836;
	}

	/**
	 * Import GEOTiff image or a directory containing GEOTiff images.<br />
	 * Images have to be projected in latlon/WGS84.
	 * @param selectedFile
	 */
	public void importImage(final File selectedFile) {

		FileStore fileStore = WorldWind.getDataFileStore();

		final File fileStoreLocation = DataImportUtil.getDefaultImportLocation(fileStore);
		String cacheName = WWIO.replaceIllegalFileNameCharacters(selectedFile.getName());

		AVList params = new AVListImpl();
		params.setValue(AVKey.FILE_STORE_LOCATION, fileStoreLocation.getAbsolutePath());
		params.setValue(AVKey.DATA_CACHE_NAME, cacheName);
		params.setValue(AVKey.DATASET_NAME, selectedFile.getName());


		// Create a TiledImageProducer to transforms the source image to a pyramid of images tiles in the World Wind
		// Java cache format.
		final TiledImageProducer producer = new TiledImageProducer();

		// Configure the TiledImageProducer with the parameter list and the image source.
		producer.setStoreParameters(params);

		final ProgressMonitor progress = new ProgressMonitor(null, "Import des images", "Tile", 0, 100);
		progress.setMillisToDecideToPopup(0);

		//Traitement lourd --> SwingWorker
		final SwingWorker<Integer,Integer> task = new SwingWorker<Integer, Integer>() {
			@Override
			protected Integer doInBackground() throws Exception {
				try {
					if(selectedFile.isDirectory()){
						File[] files = selectedFile.listFiles(new FileFilter() {

							@Override
							public boolean accept(File pathname) {
								if (pathname.isDirectory()) {
									return false;
								}

								String ext = null;
								String s = pathname.getName();
								int i = s.lastIndexOf('.');
								if (i > 0 &&  i < s.length() - 1) {
									ext = s.substring(i+1).toLowerCase();
								}

								if (ext != null) {
									if (ext.equals("tif")||ext.equals("tiff")) {
										return true;
									} else {
										return false;
									}
								}
								return false;
							}
						});
						for(int i = 0;i<files.length;i++){
							if(progress.isCanceled()){
								this.cancel(true);
								return null;
							} else {
								Double p = (double)i/(double)files.length * 50;
								progress.setProgress(p.intValue());
								progress.setNote("Ajout de l'image "+files[i].getName());
								producer.offerDataSource(files[i], null);
							}
						}
					} else {
						producer.offerDataSource(selectedFile, null);
					}
					// Import the source image into the FileStore by converting it to the World Wind Java cache format.
					producer.startProduction();
				}
				catch (Exception e) {
					producer.removeProductionState();
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void done() {	
				if(this.isCancelled()) {
					producer.stopProduction();
					producer.removeProductionState();
					//suppression des fichiers déjà créés
					FileManager.deleteFile(new File(fileStoreLocation.getAbsoluteFile()+"/"+selectedFile.getName()));					
				} else {
					Logging.logger().info("Import des images terminé.");
					progress.setProgress(100);

					// Extract the data configuration document from the production results. If production sucessfully completed, the
					// TiledImageProducer should always contain a document in the production results, but we test the results
					// anyway.
					Iterable<?> results = producer.getProductionResults();
					if (results == null || results.iterator() == null || !results.iterator().hasNext())
						return;

					Object o = results.iterator().next();
					if (o == null || !(o instanceof Document))
						return;

					// Construct a Layer by passing the data configuration document to a LayerFactory.
					Layer layer = (Layer) BasicFactory.create(AVKey.LAYER_FACTORY, ((Document) o).getDocumentElement());
					layer.setEnabled(true); 
					insertBeforePlacenames(layer);
				}
			}
		};
		
		producer.addPropertyChangeListener(AVKey.PROGRESS, new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if(progress.isCanceled()){
					task.cancel(true);
				} else {
					progress.setNote("Conversion des images...");
					progress.setProgress(new Double((Double)evt.getNewValue()*50).intValue() + 50);
				}
			}
		});
		
		task.execute();
	}

	public DraggerListener getDraggerListener(){
		return this.dragger;
	}
	
	public void activateVerticalScaleBar(Boolean state){
		if(scale == null){
			scale = new VerticalScaleBar(this);
			this.addSelectListener(scale);
		}
		
		if(state){
			scale.initializePosition(this.getView().getGlobe().computePositionFromPoint(this.getView().getCenterPoint()));
			this.toggleLayer(scale, true);
			this.addSelectListener(scale);
		} else {
			this.toggleLayer(scale, false);
			this.removeSelectListener(scale);
		}
	}
	
}
