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

package fr.crnan.videso3d.graphics;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.sun.opengl.util.BufferUtil;

import fr.crnan.videso3d.Couple;
import fr.crnan.videso3d.DatabaseManager;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.examples.util.DirectedPath;
import gov.nasa.worldwind.geom.Cylinder;
import gov.nasa.worldwind.geom.Frustum;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Line;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.terrain.Terrain;
/**
 * Route en 2D.<br />
 * Couleurs respectant le codage SIA
 * @author Bruno Spyckerelle
 * @version 0.3.2
 */
public class Route2D extends DirectedPath implements Route{

	private VidesoAnnotation annotation;
	
	private Space space;
	
	private Sens sens;
	
	private int type;
	
	private String name;
	
	private List<String> balises;
	
	private DatabaseManager.Type base;
	
	private HashMap<Position, Couple<Position, Integer>> directionsMap = new HashMap<Position, Couple<Position,Integer>>();

	private List<Integer> directions;
	
	public Route2D(String name, Space s, DatabaseManager.Type base, int type){
		this(base, type);
		this.setAnnotation("Route "+name);
		this.setSpace(s);
		this.setName(name);
	}
	public Route2D(DatabaseManager.Type base, int type) {
		super();
		this.setDatabaseType(base);
		this.setType(type);
		this.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
		this.setMaxScreenSize(9.0);
		this.setArrowLength(40000);
		this.setFollowTerrain(true);
	}
	
	public Route2D(String name, DatabaseManager.Type base, int type) {
		this(base, type);
		this.setName(name);
	}

	@Override
	public DatabaseManager.Type getDatabaseType() {
		return this.base;
	}

	@Override
	public void setDatabaseType(DatabaseManager.Type type) {
		this.base = type;
	}
	
	/**
	 * Affecte la couleur de la route suivant le codage SIA
	 * @param name Nom de la route
	 * @param type {@link Espace} de la route
	 */
	private void setColor(String name) {
		BasicShapeAttributes attrs = new BasicShapeAttributes();
		switch (space) {
		case FIR:
			Character c = name.charAt(0);
			switch (c) {
			case 'A':
				attrs.setOutlineMaterial(Material.YELLOW);
				break;
			case 'G' :
				attrs.setOutlineMaterial(Material.GREEN);
				break;
			case 'B' :
				attrs.setOutlineMaterial(Material.BLUE);
				break;
			case 'R' :
				attrs.setOutlineMaterial(Material.RED);
				break;
			default:
				attrs.setOutlineMaterial(Material.BLACK);
				break;
			}
			break;
		case UIR:
			if(sens!=null){
				switch (sens){
				case RED :
					attrs.setOutlineMaterial(Material.RED);
					break;
				case GREEN :
					attrs.setOutlineMaterial(Material.GREEN);
					break;
				case BLUE :
					attrs.setOutlineMaterial(Material.BLUE);
					break;
				}
			}else{
				attrs.setOutlineMaterial(Material.BLACK);
			}
			break;
		default:
			break;
		}
		attrs.setEnableAntialiasing(true);
		attrs.setDrawInterior(false);
		attrs.setOutlineWidth(1.0);
		this.setAttributes(attrs);
	}


	@Override
	public VidesoAnnotation getAnnotation(Position pos) {
		if(annotation == null) this.setAnnotation("Route "+this.name);
		annotation.setPosition(pos);
		return annotation;
	}

	@Override
	public void setAnnotation(String text) {
		if(annotation == null) {
			annotation = new VidesoAnnotation(text);
		} else {
			annotation.setText(text);
		}
	}

	@Override
	public void setSpace(Space s) {
		Space temp = this.space;
		this.space = s;
		if(this.space != temp && this.name != null) this.setColor(this.name);
	}

	@Override
	public Space getSpace(){
		return this.space;
	}
	
	public void setName(String name) {
		String temp = this.name;
		this.name = name;
		if(this.name != temp && this.space != null) this.setColor(this.name);
	}
	
	public void setSens(Sens sens){
		Sens temp = this.sens;
		this.sens = sens;
		if(this.sens != temp && this.name != null) this.setColor(this.name);
		
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void setType(int type) {
		this.type = type;
	}

	@Override
	public int getType() {
		return this.type;
	}
	
	@Override
	public void highlight(boolean highlight) {
		if(highlight){
			BasicShapeAttributes attrs = (BasicShapeAttributes) this.getAttributes();
			attrs.setOutlineMaterial(Material.WHITE);
			attrs.setOutlineWidth(2.0);
			this.setAttributes(attrs);
			
		} else {
			this.setColor(this.getName());
		}
	}
	
	public void setBalises(List<String> balises){
		this.balises = balises;
	}
	
	public void addBalise(String balise){
		if(this.balises == null){
			this.balises = new LinkedList<String>();
		}
		this.balises.add(balise);
	}

	@Override
	public List<String> getBalises(){
		return this.balises;
	}
	
	@Override
	public Iterable<? extends LatLon> getLocations() {
		return this.getPositions();
	}
	
	
	public void setLocations(Iterable<? extends LatLon> locations){
		List<Position> positions = new ArrayList<Position>();
		for(LatLon loc : locations){
			positions.add(new Position(loc, 0));
		}
		this.setPositions(positions);
	}
	
	/**
	 * <b>Warning !</b> locations.size() == directions.size() -1
	 * @param locations
	 * @param directions
	 */
	public void setLocations(Iterable<? extends LatLon> locations, List<Integer> directions){
		List<Position> positions = new ArrayList<Position>();
		Position first = null;
		Iterator<Integer> direction = directions.iterator();
		for(LatLon loc : locations){
			Position temp = new Position(loc, 0);
			positions.add(temp);
			if(first != null){
				directionsMap.put(first, new Couple<Position, Integer>(temp, direction.next()));
			}
			first = temp;
		}
		this.setPositions(positions);
		this.directions = directions;
	}

	

	/* (non-Javadoc)
	 * @see gov.nasa.worldwind.render.Path#isSegmentVisible(gov.nasa.worldwind.render.DrawContext, gov.nasa.worldwind.geom.Position, gov.nasa.worldwind.geom.Position, gov.nasa.worldwind.geom.Vec4, gov.nasa.worldwind.geom.Vec4)
	 */
	@Override
	protected boolean isSegmentVisible(DrawContext dc, Position posA,
			Position posB, Vec4 ptA, Vec4 ptB) {
		
		if(directionsMap.containsKey(posA)){
			if(directionsMap.get(posA).getFirst().equals(posB)){
				if(directionsMap.get(posA).getSecond() == LEG_FORBIDDEN){
					return false;
				}
			}
		}
		if(this.directionsMap.containsKey(posB)){
			if(directionsMap.get(posB).getFirst().equals(posA)){
				if(directionsMap.get(posB).getSecond() == LEG_FORBIDDEN){
					return false;
				}
			}
		}
		
		Frustum f = dc.getView().getFrustumInModelCoordinates();

        if (f.contains(ptA))
            return true;

        if (f.contains(ptB))
            return true;

        if (ptA.equals(ptB))
            return false;

        Position posC = Position.interpolateRhumb(0.5, posA, posB);
        Vec4 ptC = this.computePoint(dc.getTerrain(), posC);
        if (f.contains(ptC))
            return true;

        double r = Line.distanceToSegment(ptA, ptB, ptC);
        Cylinder cyl = new Cylinder(ptA, ptB, r == 0 ? 1 : r);
        return cyl.intersects(dc.getView().getFrustumInModelCoordinates());
	}


	@Override
	protected void doDrawOutline(DrawContext dc) {
		if(this.directions != null){
			this.computeDirectionArrows(dc, this.getCurrentPathData());
			this.drawDirectionArrows(dc, this.getCurrentPathData());
		}
		super.doDrawOutline(dc);
	}

	@Override
	protected void computeDirectionArrows(DrawContext dc, PathData pathData) {		
		IntBuffer polePositions = pathData.getPolePositions();
        int numPositions = polePositions.limit() / 2; // One arrow head for each path segment
        List<Position> tessellatedPositions = pathData.getTessellatedPositions();

        final int FLOATS_PER_ARROWHEAD = 9; // 3 points * 3 coordinates per point
        FloatBuffer buffer = (FloatBuffer) pathData.getValue(ARROWS_KEY);
        if (buffer == null || buffer.capacity() < numPositions * FLOATS_PER_ARROWHEAD)
            buffer = BufferUtil.newFloatBuffer(FLOATS_PER_ARROWHEAD * numPositions);
        pathData.setValue(ARROWS_KEY, buffer);

        buffer.clear();

        Terrain terrain = dc.getTerrain();

        double arrowBase = this.getArrowLength() * this.getArrowAngle().tanHalfAngle();

        // Step through polePositions to find the original path locations.
        int thisPole = polePositions.get(0) / 2;
        Position poleA = tessellatedPositions.get(thisPole);
        Vec4 polePtA = this.computePoint(terrain, poleA);

        // Draw one arrowhead for each segment in the original position list. The path may be tessellated,
        // so we need to find the tessellated segment halfway between each pair of original positions.
        // polePositions holds indices into the rendered path array of the original vertices. Step through
        // polePositions by 2 because we only care about where the top of the pole is, not the bottom.
        int num = 0;
        for (int i = 2; i < polePositions.limit(); i += 2)
        {
            // Find the position of this pole and the next pole. Divide by 2 to convert an index in the
            // renderedPath buffer to a index in the tessellatedPositions list.
            int nextPole = polePositions.get(i) / 2;

            Position poleB = tessellatedPositions.get(nextPole);

            Vec4 polePtB = this.computePoint(terrain, poleB);

            // Find the segment that is midway between the two poles.
            int midPoint = (thisPole + nextPole) / 2;

            Position posA = tessellatedPositions.get(midPoint);
            Position posB = tessellatedPositions.get(midPoint + 1);

            Vec4 ptA = this.computePoint(terrain, posA);
            Vec4 ptB = this.computePoint(terrain, posB);

            if(this.directions != null ){
            	//on ne dessine une flèche que lorsque c'est pertinent
            	if(this.directions.get(num) != LEG_AUTHORIZED && this.directions.get(num) != LEG_FORBIDDEN) { 
            		this.computeArrowheadGeometry(dc, polePtA, polePtB, ptA, ptB, this.getArrowLength(), arrowBase, buffer,
            			pathData, this.directions.get(num));
            		}
            }
            thisPole = nextPole;
            polePtA = polePtB;
            num++;
        }
	}

	
	protected void computeArrowheadGeometry(DrawContext dc, Vec4 polePtA,
			Vec4 polePtB, Vec4 ptA, Vec4 ptB, double arrowLength,
			double arrowBase, FloatBuffer buffer, PathData pathData, int direction) {
		
		 // Build a triangle to represent the arrowhead. The triangle is built from two vectors, one parallel to the
        // segment, and one perpendicular to it. The plane of the arrowhead will be parallel to the surface.

        double poleDistance = polePtA.distanceTo3(polePtB);

        // Compute parallel component
        Vec4 parallel = ptA.subtract3(ptB);

        Vec4 surfaceNormal = dc.getGlobe().computeSurfaceNormalAtPoint(ptB);

        // Compute perpendicular component
        Vec4 perpendicular = surfaceNormal.cross3(parallel);

        // Compute midpoint of segment
        Vec4 midPoint = ptA.add3(ptB).divide3(2.0);

        if (!this.isArrowheadSmall(dc, midPoint, 1))
        {
            // Compute the size of the arrowhead in pixels to make ensure that the arrow does not exceed the maximum
            // screen size.
            View view = dc.getView();
            double midpointDistance = view.getEyePoint().distanceTo3(midPoint);
            double pixelSize = view.computePixelSizeAtDistance(midpointDistance);
            if (arrowLength / pixelSize > this.maxScreenSize)
            {
                arrowLength = this.maxScreenSize * pixelSize;
                arrowBase = arrowLength * this.getArrowAngle().tanHalfAngle();
            }

            // Don't draw an arrowhead if the path segment is smaller than the arrow
            if (poleDistance <= arrowLength)
                return;

            perpendicular = perpendicular.normalize3().multiply3(arrowBase);
            parallel = parallel.normalize3().multiply3(arrowLength);

            // If the distance between the poles is greater than the arrow length, center the arrow on the midpoint.
            // Otherwise position the tip of the arrow at the midpoint. On short segments it looks weird if the
            // tip of the arrow does not fall on the path, but on longer segments it looks better to center the
            // arrow on the segment.
            if (poleDistance > arrowLength)
                midPoint = midPoint.subtract3(parallel.divide3(2.0));

            Vec4 vertex1;
            Vec4 vertex2;
            if(direction == LEG_DIRECT){
            // Compute geometry of direction arrow
            	vertex1 = midPoint.add3(parallel).add3(perpendicular);
            	vertex2 = midPoint.add3(parallel).add3(perpendicular.multiply3(-1.0));
            } else {
            	vertex1 = midPoint.add3(perpendicular);
                vertex2 = midPoint.add3(perpendicular.multiply3(-1.0));
                midPoint = midPoint.add3(parallel);
            }
            
            // Add geometry to the buffer
            Vec4 referencePoint = pathData.getReferencePoint();
            buffer.put((float) (vertex1.x - referencePoint.x));
            buffer.put((float) (vertex1.y - referencePoint.y));
            buffer.put((float) (vertex1.z - referencePoint.z));

            buffer.put((float) (vertex2.x - referencePoint.x));
            buffer.put((float) (vertex2.y - referencePoint.y));
            buffer.put((float) (vertex2.z - referencePoint.z));

            buffer.put((float) (midPoint.x - referencePoint.x));
            buffer.put((float) (midPoint.y - referencePoint.y));
            buffer.put((float) (midPoint.z - referencePoint.z));
        }
		
	}
	
	
}
