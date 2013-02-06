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

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
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
import gov.nasa.worldwind.data.BufferedImageRaster;
import gov.nasa.worldwind.data.TiledImageProducer;
import gov.nasa.worldwindx.examples.dataimport.DataInstallUtil;
import gov.nasa.worldwindx.examples.util.ShapeUtils;
import gov.nasa.worldwind.formats.gcps.GCPSReader;
import gov.nasa.worldwind.formats.tab.TABRasterReader;
import gov.nasa.worldwind.formats.tiff.GeotiffReader;
import gov.nasa.worldwind.formats.tiff.GeotiffWriter;
import gov.nasa.worldwind.formats.worldfile.WorldFile;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.render.SurfaceImage;
import gov.nasa.worldwind.util.ImageUtil;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.RasterControlPointList;
import gov.nasa.worldwind.util.WWIO;
import gov.nasa.worldwind.util.WWMath;
/**
 * Methods to operate images
 * @author Bruno Spyckerelle
 * @version 0.1.1
 */
public class ImageUtils {


	public static SurfaceImage createGeoreferencedSurfaceImage(File file, BufferedImage image)	{
		try	{
			SurfaceImage si = null;
			File tabFile = getAssociatedTABFile(file);
			if (tabFile != null)
				si = createSurfaceImageFromTABFile(image, tabFile);

			if (si == null)	{
				File gcpsFile = getAssociatedGCPSFile(file);
				if (gcpsFile != null)
					si = createSurfaceImageFromGCPSFile(image, gcpsFile);
			}

			if (si == null) {
				File[] worldFiles = getAssociatedWorldFiles(file);
				if (worldFiles != null)
					si = createSurfaceImageFromWorldFiles(image, worldFiles);
			}

			if(si == null &&
				(file.getName().toLowerCase().endsWith(".tiff") || 
					file.getName().toLowerCase().endsWith(".geotiff") ||
					file.getName().toLowerCase().endsWith(".tif") ||
					file.getName().toLowerCase().endsWith(".gtif"))){
				GeotiffReader reader = new GeotiffReader(file);
				int imageIndex = 0;
				image = reader.read(imageIndex);
				if (reader.isGeotiff(imageIndex)){
					AVList values = new AVListImpl();
					if (null != image)
					{
						values.setValue(AVKey.IMAGE, image);
						values.setValue(AVKey.WIDTH, image.getWidth());
						values.setValue(AVKey.HEIGHT, image.getHeight());
					}

					ImageUtil.readGeoKeys(reader, imageIndex, values);

					if (AVKey.COORDINATE_SYSTEM_PROJECTED.equals(values.getValue(AVKey.COORDINATE_SYSTEM)))
						ImageUtil.reprojectUtmToGeographic(values, ImageUtil.NEAREST_NEIGHBOR_INTERPOLATION);

					if(values != null){
						si = new SurfaceImage(values.getValue(AVKey.IMAGE), (Sector) values.getValue(AVKey.SECTOR));
					}
				}
			}
			
			return si;
		} catch (Exception e)	{
			e.printStackTrace();
			return null;
		}
	}

	public static File getAssociatedTABFile(File file) {
		File tabFile = TABRasterReader.getTABFileFor(file);
		if (tabFile != null && tabFile.exists()) {
			TABRasterReader reader = new TABRasterReader();
			if (reader.canRead(tabFile))
				return tabFile;
		}

		return null;
	}

	public static File getAssociatedGCPSFile(File file) {
		File gcpsFile = GCPSReader.getGCPSFileFor(file);
		if (gcpsFile != null && gcpsFile.exists()) {
			GCPSReader reader = new GCPSReader();
			if (reader.canRead(gcpsFile))
				return gcpsFile;
		}

		return null;
	}

	public static File[] getAssociatedWorldFiles(File file) {
		try {
			File[] worldFiles = WorldFile.getWorldFiles(file);
			if (worldFiles != null && worldFiles.length > 0)
				return worldFiles;
		} catch (Exception ignored) {
		}

		return null;
	}

	public static SurfaceImage createSurfaceImageFromWorldFiles(BufferedImage image, File[] worldFiles)
	throws java.io.IOException  {
		AVList worldFileParams = new AVListImpl();
		WorldFile.decodeWorldFiles(worldFiles, worldFileParams);

		BufferedImage alignedImage = createPowerOfTwoImage(image.getWidth(), image.getHeight());
		Sector sector = ImageUtil.warpImageWithWorldFile(image, worldFileParams, alignedImage);

		return new SurfaceImage(alignedImage, sector);
	}

	public static SurfaceImage createSurfaceImageFromTABFile(BufferedImage image, File tabFile)
	throws java.io.IOException  {
		TABRasterReader reader = new TABRasterReader();
		RasterControlPointList controlPoints = reader.read(tabFile);

		return createSurfaceImageFromControlPoints(image, controlPoints);
	}

	public static SurfaceImage createSurfaceImageFromGCPSFile(BufferedImage image, File gcpsFile)
	throws java.io.IOException {
		GCPSReader reader = new GCPSReader();
		RasterControlPointList controlPoints = reader.read(gcpsFile);

		return createSurfaceImageFromControlPoints(image, controlPoints);
	}

	public static SurfaceImage createSurfaceImageFromControlPoints(BufferedImage image,
			RasterControlPointList controlPoints) throws java.io.IOException {
		int numControlPoints = controlPoints.size();
		Point2D[] imagePoints = new Point2D[numControlPoints];
		LatLon[] geoPoints = new LatLon[numControlPoints];

		for (int i = 0; i < numControlPoints; i++)	{
			RasterControlPointList.ControlPoint p = controlPoints.get(i);
			imagePoints[i] = p.getRasterPoint();
			geoPoints[i] = p.getWorldPointAsLatLon();
		}

		BufferedImage destImage = createPowerOfTwoImage(image.getWidth(), image.getHeight());
		Sector sector = ImageUtil.warpImageWithControlPoints(image, imagePoints, geoPoints, destImage);

		return new SurfaceImage(destImage, sector);
	}


	public static SurfaceImage createNonGeoreferencedSurfaceImage(final File file, 
			final BufferedImage image, 
			VidesoGLCanvas wwd) {

		Position position = ShapeUtils.getNewShapePosition(wwd);
		double lat = position.getLatitude().radians;
		double lon = position.getLongitude().radians;
		double sizeInMeters = ShapeUtils.getViewportScaleFactor(wwd);
		double arcLength = sizeInMeters / wwd.getModel().getGlobe().getRadiusAt(position);
		Sector sector = Sector.fromRadians(lat - arcLength, lat + arcLength, lon - arcLength, lon + arcLength);

		BufferedImage powerOfTwoImage = ImageUtils.createPowerOfTwoScaledCopy(image);

		return new SurfaceImage(powerOfTwoImage, sector);

	}

	public static BufferedImage createPowerOfTwoImage(int minWidth, int minHeight){
		return new BufferedImage(WWMath.powerOfTwoCeiling(minWidth), WWMath.powerOfTwoCeiling(minHeight),
				BufferedImage.TYPE_INT_ARGB);
	}

	public static BufferedImage createPowerOfTwoScaledCopy(BufferedImage image){
		if (WWMath.isPowerOfTwo(image.getWidth()) && WWMath.isPowerOfTwo(image.getHeight()))
			return image;

		BufferedImage powerOfTwoImage = createPowerOfTwoImage(image.getWidth(), image.getHeight());
		ImageUtil.getScaledCopy(image, powerOfTwoImage);
		return powerOfTwoImage;
	}

	public static void writeImageToFile(Sector sector, BufferedImage image, File gtFile)
	throws IOException {
		AVList params = new AVListImpl();

		params.setValue(AVKey.SECTOR, sector);
		params.setValue(AVKey.COORDINATE_SYSTEM, AVKey.COORDINATE_SYSTEM_GEOGRAPHIC);
		params.setValue(AVKey.PIXEL_FORMAT, AVKey.IMAGE);
		params.setValue(AVKey.BYTE_ORDER, AVKey.BIG_ENDIAN);

		GeotiffWriter writer = new GeotiffWriter(gtFile);
		writer.write(BufferedImageRaster.wrapAsGeoreferencedRaster(image, params));
		writer.close();
	}
	
	/**
	 * Convert images to {@link SurfaceImage}<br />
	 * A call to this method should be done in a {@link SwingWorker}
	 * @param images
	 * @param wwd
	 * @throws IOException 
	 */
	public static List<EditableSurfaceImage> createEditableImages(File[] images, VidesoGLCanvas wwd) throws IOException{

		List<EditableSurfaceImage> surfaceImages = new ArrayList<EditableSurfaceImage>();

		for(final File file : images) {
			EditableSurfaceImage editsi = createEditableImage(file, wwd);
			if(editsi != null)
				surfaceImages.add(editsi);
		}

		return surfaceImages;
	}
	
	/**
	 * Convert image to {@link SurfaceImage}<br />
	 * A call to this method should be done in a {@link SwingWorker}
	 * @param image
	 * @param wwd
	 * @return
	 * @throws IOException
	 */
	public static EditableSurfaceImage createEditableImage(File image, VidesoGLCanvas wwd) throws IOException{
		
		EditableSurfaceImage surfaceImage = null;
		
		BufferedImage bufferedImage = ImageIO.read(image);
		if(bufferedImage == null)
			return null;
		
		SurfaceImage si = createGeoreferencedSurfaceImage(image, bufferedImage);
		if(si == null){
			surfaceImage = new EditableSurfaceImage(image.getName(), createNonGeoreferencedSurfaceImage(image, bufferedImage, wwd), wwd);
		} else {
			surfaceImage = new EditableSurfaceImage(image.getName(), si, wwd);
		}
		
		return surfaceImage;
	}
	
	/**
	 * Import GEOTiff image or a directory containing GEOTiff images.<br />
	 * Images have to be projected in latlon/WGS84.
	 * @param selectedFile
	 */
	public static void importImage(final File selectedFile, final VidesoGLCanvas wwd) {

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
