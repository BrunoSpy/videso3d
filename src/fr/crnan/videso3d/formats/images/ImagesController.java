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
package fr.crnan.videso3d.formats.images;

import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.SwingWorker;

import org.w3c.dom.Document;

import fr.crnan.videso3d.FileManager;
import fr.crnan.videso3d.VidesoGLCanvas;
import fr.crnan.videso3d.ihm.ProgressMonitor;
import gov.nasa.worldwind.BasicFactory;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.cache.FileStore;
import gov.nasa.worldwind.data.TiledImageProducer;
import gov.nasa.worldwindx.examples.dataimport.DataInstallUtil;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.SurfaceImage;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.WWIO;

/**
 * 
 * @author Bruno Spyckerelle
 * @version 0.1.1
 */
public class ImagesController {

	private VidesoGLCanvas wwd;
	
	private RenderableLayer imagesLayer;
	
	public ImagesController(VidesoGLCanvas ww){
		this.wwd = ww;
		imagesLayer = new RenderableLayer();
		imagesLayer.setName("Images");
		imagesLayer.setPickEnabled(true);
	}
	
	/**
	 * Add images to the view<br />
	 * @param images
	 */
	public void addEditableImages(final File[] images){
		
		new SwingWorker<Integer, Integer>() {

			@Override
			protected Integer doInBackground() throws Exception {
				for(final File file : images) {
					final BufferedImage image = ImageIO.read(file);
					if (image == null)
						return null;
					
					final SurfaceImage si = ImageUtils.createGeoreferencedSurfaceImage(file, image);
					EditableSurfaceImage editSi;
					if (si == null)	{
						editSi = new EditableSurfaceImage(file.getName(), ImageUtils.createNonGeoreferencedSurfaceImage(file, image, wwd), wwd);
					} else {	
						editSi = new EditableSurfaceImage(file.getName(), si, wwd);
					}
					imagesLayer.addRenderable(editSi);
					wwd.toggleLayer(imagesLayer, true);
				}
				return null;
			}
		}.execute();
		
	}
	
	public void deleteImage(SurfaceImage image){
		this.imagesLayer.removeRenderable(image);
		if(image instanceof EditableSurfaceImage){
			((EditableSurfaceImage) image).getEditor().setArmed(false);
		}
	}
	
	/**
	 * 
	 * @return A list of all displayed images
	 */
	public List<EditableSurfaceImage> getImages(){
		List<EditableSurfaceImage> images = new ArrayList<EditableSurfaceImage>();
		for(Renderable r : this.imagesLayer.getRenderables()){
			if(r instanceof EditableSurfaceImage)
				images.add((EditableSurfaceImage) r);
		}
		return images;
	}
	
	/**
	 * Import GEOTiff image or a directory containing GEOTiff images.<br />
	 * Images have to be projected in latlon/WGS84.
	 * @param selectedFile
	 */
	public void importImage(final File selectedFile) {

		FileStore fileStore = WorldWind.getDataFileStore();

		final File fileStoreLocation = DataInstallUtil.getDefaultInstallLocation(fileStore);
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
					wwd.insertBeforePlacenames(layer);
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
	
}
