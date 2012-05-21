package fr.crnan.videso3d.trajectography;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.SwingWorker;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

import fr.crnan.videso3d.formats.VidesoTrack;
import fr.crnan.videso3d.formats.geo.GEOTrack;
import fr.crnan.videso3d.formats.lpln.LPLNTrack;
import fr.crnan.videso3d.formats.opas.OPASTrack;
import fr.crnan.videso3d.formats.plns.PLNSTrack;
import fr.crnan.videso3d.graphics.VPolygon;
import gov.nasa.worldwind.tracks.Track;
import gov.nasa.worldwind.tracks.TrackPoint;
import gov.nasa.worldwind.util.Logging;
/**
 * Represents a set of tracks
 * @author Bruno Spyckerelle
 * @version 0.1.1
 */
public class TracksModel extends AbstractTableModel {

	/**
	 * Les différents champs accessibles
	 */
	public static final int FIELD_ADEP = 1;
	public static final int FIELD_ADEST = 2;
	public static final int FIELD_IAF = 3;
	public static final int FIELD_INDICATIF = 4;
	public static final int FIELD_TYPE_AVION = 5;
	public static final int FIELD_TYPE_MODE_A = 6;
	
	private String[] columnNames = {"Indicatif", "Départ", "Arrivée", "IAF", "Type", "Mode A", "Affiché"};

	protected List<Object> tracks = null;

	protected Collection<VidesoTrack> visibleTracks;
	
	protected Collection<VidesoTrack> selectedTracks;
		
	protected Collection<VidesoTrack> allTracks;
	
	private boolean isChanging = false;
	
	private Collection<VidesoTrack> tempTracksAdded;
	private Collection<VidesoTrack> tempTracksRemoved;
	private Collection<VidesoTrack> tempTracksVisible;
	private Collection<VidesoTrack> tempTracksNonVisible;
	private Collection<VidesoTrack> tempTracksSelected;
	private Collection<VidesoTrack> tempTracksUnselected;

	private boolean disjunctive = true; //"or" by default
	protected HashMap<Integer, String> filters = new HashMap<Integer, String>();

	
	/**
	 * Filtres par polygone
	 */
	private HashSet<PolygonsSetFilter> polygonFilters;
	
	public TracksModel(Collection<VidesoTrack> tracks){
		super();
		this.tracks = Arrays.asList(tracks.toArray());//TODO gérer les mauvais fichiers
		visibleTracks = new HashSet<VidesoTrack>(tracks);
		selectedTracks = new HashSet<VidesoTrack>();
		allTracks = tracks;
	}
	
	public TracksModel(){
		super();
		this.allTracks = new HashSet<VidesoTrack>();
		this.visibleTracks = new HashSet<VidesoTrack>(allTracks);
		this.selectedTracks = new HashSet<VidesoTrack>();
		this.tracks = new LinkedList<Object>();
	}
	
	protected void setTracks(List<Object> tracks){
		this.tracks = tracks;
	}
	
	/**
	 * If <code>isChanging</code> is True, events are fired when it is set to False
	 * @return the isChanging
	 */
	public boolean isChanging() {
		return isChanging;
	}

	/**
	 * If <code>isChanging</code> is <code>false</code>, apply pending changes.
	 * @param isChanging
	 */
	public void setChanging(boolean isChanging) {
		if(this.isChanging != isChanging){
			this.isChanging = isChanging;
			if(!isChanging()){
				//fire events and apply changes
				if(tempTracksAdded != null){
					this.addTrack(tempTracksAdded);
					tempTracksAdded = null;
				}
				if(tempTracksRemoved != null){
					this.remove(tempTracksRemoved);
					tempTracksRemoved = null;
				}
				if(tempTracksNonVisible != null){
					this.setVisible(false, tempTracksNonVisible);
					tempTracksNonVisible = null;
				}
				if(tempTracksVisible != null){
					this.setVisible(true, tempTracksVisible);
					tempTracksVisible = null;
				}
				if(tempTracksUnselected != null){
					this.setSelected(false, tempTracksUnselected);
					tempTracksUnselected = null;
				}
				if(tempTracksSelected != null){
					this.setSelected(true, tempTracksSelected);
					tempTracksSelected = null;
				}
			}
		}
	}

	/**
	 * Ajoute un Track, visible par défaut
	 * @param track {@link Track}
	 */
	public void addTrack(VidesoTrack track){
		if(!isChanging) {
			this.allTracks.add(track);
			this.visibleTracks.add(track);
			fireTrackAdded(track);
		} else {
			if(tempTracksAdded == null) tempTracksAdded = new HashSet<VidesoTrack>();
			tempTracksAdded.add(track);
			//try to remove tracks from previously removed tracks
			if(tempTracksRemoved != null) tempTracksRemoved.remove(track);
		}
	}

	public void addTrack(Collection<VidesoTrack> tracks){
		if(!isChanging){
			this.allTracks.addAll(tracks);
			this.visibleTracks.addAll(tracks);
			fireTrackAdded(tracks);
		} else {
			if(tempTracksAdded == null) tempTracksAdded = new HashSet<VidesoTrack>();
			tempTracksAdded.addAll(tracks);
			//try to remove tracks from previously removed tracks
			if(tempTracksRemoved != null) tempTracksRemoved.removeAll(tracks);
		}
	}

	public void removeTrack(VidesoTrack track){
		if(!isChanging) {
			this.allTracks.remove(track);
			this.selectedTracks.remove(track);
			this.visibleTracks.remove(track);
			fireTrackRemoved(track);
		} else {
			if(tempTracksRemoved == null) tempTracksRemoved = new HashSet<VidesoTrack>();
			tempTracksRemoved.add(track);
			//try to remove tracks from previously added tracks
			if(tempTracksAdded != null) tempTracksAdded.remove(track);
		}
	}

	public void remove(Collection<VidesoTrack> tracks){
		if(!isChanging) {
			this.allTracks.removeAll(tracks);
			this.selectedTracks.removeAll(tracks);
			this.visibleTracks.removeAll(tracks);
			fireTrackRemoved(tracks);
		} else {
			if(tempTracksRemoved == null) tempTracksRemoved = new HashSet<VidesoTrack>();
			tempTracksRemoved.addAll(tracks);
			//try to remove tracks from previously added tracks
			if(tempTracksAdded != null) tempTracksAdded.removeAll(tracks);
		}
	}

	public Collection<VidesoTrack> getVisibleTracks(){
		return this.visibleTracks;
	}
	
	public Collection<VidesoTrack> getSelectedTracks(){
		return this.selectedTracks;
	}
	
	public Collection<VidesoTrack> getAllTracks(){
		return this.allTracks;
	}
	
	public Boolean isSelected(Track track){
		return this.selectedTracks.contains(track);
	}

	public void setSelected(boolean selected, VidesoTrack track){
		if(selected){
			if(!isChanging) {
				if(this.selectedTracks.add(track)) fireTrackSelectionChanged(track, selected);
			} else {
				if(tempTracksSelected == null) tempTracksSelected = new HashSet<VidesoTrack>();
				tempTracksSelected.add(track);
				//try to remove tracks from previously unselected tracks
				if(tempTracksUnselected != null) tempTracksUnselected.remove(track);
			}

		} else {
			if(!isChanging) {
				if(this.selectedTracks.remove(track)) fireTrackSelectionChanged(track, selected);
			} else {
				if(tempTracksUnselected == null) tempTracksUnselected = new HashSet<VidesoTrack>();
				tempTracksUnselected.add(track);
				//try to remove tracks from previously selected tracks
				if(tempTracksSelected != null) tempTracksSelected.remove(track);
			}
		}
	}

	public void setSelected(boolean selected, Collection<VidesoTrack> tracks){
		if(selected){
			if(!isChanging) {
				this.selectedTracks.addAll(tracks);
				fireTrackSelectionChanged(tracks, selected);
			} else {
				if(tempTracksSelected == null) tempTracksSelected = new HashSet<VidesoTrack>();
				tempTracksSelected.addAll(tracks);
				//try to remove tracks from previously unselected tracks
				if(tempTracksUnselected != null) tempTracksUnselected.removeAll(tracks);
			}
		} else {
			if(!isChanging) {
				this.selectedTracks.removeAll(tracks);
				fireTrackSelectionChanged(tracks, selected);
			} else {
				if(tempTracksUnselected == null) tempTracksUnselected = new HashSet<VidesoTrack>();
				tempTracksUnselected.addAll(tracks);
				//try to remove tracks from previously selected tracks
				if(tempTracksSelected != null) tempTracksSelected.removeAll(tracks);
			}
		}
	}

	/**
	 * Returns true or false wether the track is displayed on the globe or not
	 * @param track
	 * @return
	 */
	public Boolean isVisible(Track track){
		return this.visibleTracks.contains(track);
	}
	
	/**
	 * Rend visible ou non le track concerné
	 * @param b
	 * @param track
	 */
	public void setVisible(Boolean b, VidesoTrack track){
		if(b){
			if(!isChanging){
				this.visibleTracks.add(track);
				fireTrackVisibilityChanged(track, b);
			} else {
				if(tempTracksVisible == null) tempTracksVisible = new HashSet<VidesoTrack>();
				tempTracksVisible.add(track);
				if(tempTracksNonVisible != null) tempTracksNonVisible.remove(track);
			}
		} else {
			if(!isChanging){
				this.visibleTracks.remove(track);
				fireTrackVisibilityChanged(track, b);
			} else {
				if(tempTracksNonVisible == null) tempTracksNonVisible = new HashSet<VidesoTrack>();
				tempTracksNonVisible.add(track);
				if(tempTracksVisible != null) tempTracksVisible.remove(track);
			}
		}
	}

	public void setVisible(Boolean b, Collection<VidesoTrack> tracks){
		if(b){
			if(!isChanging) {
				this.visibleTracks.addAll(tracks);
				this.fireTrackVisibilityChanged(tracks, b);
			} else {
				if(tempTracksVisible == null) tempTracksVisible = new HashSet<VidesoTrack>();
				tempTracksVisible.addAll(tracks);
				if(tempTracksNonVisible != null) tempTracksNonVisible.removeAll(tracks);
			}
		} else {
			if(!isChanging) {
				this.visibleTracks.removeAll(tracks);
				this.fireTrackVisibilityChanged(tracks, b);
			} else {
				if(tempTracksNonVisible == null) tempTracksNonVisible = new HashSet<VidesoTrack>();
				tempTracksNonVisible.addAll(tracks);
				if(tempTracksVisible != null) tempTracksVisible.removeAll(tracks);
			}
		}
	}
	
	public Object getTrackAt(int row){
		return tracks.get(row);
	}
	
	public int getRow(VidesoTrack track){
		return tracks.indexOf(track);
	}
	
	@Override
	public String getColumnName(int col) {
        return columnNames[col];
    }

	
	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public int getRowCount() {
		return tracks.size();
	}

	@Override
	public Object getValueAt(int row, int col) {
		Track t = (Track) tracks.get(row);
		if(t instanceof GEOTrack){
			switch (col) {
			case 0:
				return ((GEOTrack)t).getIndicatif();
			case 1:
				return ((GEOTrack)t).getDepart();
			case 2:
				return ((GEOTrack)t).getArrivee();
			case 3:
				return "";
			case 4:
				return ((GEOTrack)t).getType();
			case 5:
				return ((GEOTrack)t).getModeA();
			case 6:
				return isVisible((Track)t);
			default:
				return "";
			}
		} else if(t instanceof OPASTrack){
			switch (col) {
			case 0:
				return ((OPASTrack)t).getIndicatif();
			case 1:
				return ((OPASTrack)t).getDepart();
			case 2:
				return ((OPASTrack)t).getArrivee();
			case 3:
				return ((OPASTrack)t).getIaf();
			case 4:
				return "";
			case 5:
				return "";
			case 6:
				return isVisible((Track)t);
			default:
				return "";
			}
		} else if(t instanceof LPLNTrack) {
			switch (col) {
			case 0:
				return ((LPLNTrack)t).getIndicatif();
			case 1:
				return ((LPLNTrack)t).getDepart();
			case 2:
				return ((LPLNTrack)t).getArrivee();
			case 3:
				return "";
			case 4:
				return ((LPLNTrack)t).getType();
			case 5:
				return "";
			case 6:
				return isVisible((Track)t);
			default:
				return "";
			}
		} else if(t instanceof PLNSTrack) {
			switch (col) {
			case 0:
				return ((PLNSTrack)t).getIndicatif();
			case 1:
				return ((PLNSTrack)t).getDepart();
			case 2:
				return ((PLNSTrack)t).getArrivee();
			case 3:
				return "";
			case 4:
				return ((PLNSTrack)t).getType();
			case 5:
				return "";
			case 6:
				return isVisible((Track)t);
			default:
				return "";
			}
		} else {
			return "";
		}
	}

	
	/* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
	 */
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if(columnIndex == 6){
			return Boolean.class;
		} else {
			return String.class;
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
	 */
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if(columnIndex == 6){
			return true;
		} else {
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
	 */
	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if(columnIndex == 6){
			setVisible((Boolean)aValue, (VidesoTrack)tracks.get(rowIndex));
			fireTableDataChanged();
		}
	}

	/*********************************************************/
	/*********************** Filters *************************/
	/*********************************************************/
	
	/**
	 * Apply both regex and polygon filters
	 */
	public void applyFilters(){
		new SwingWorker<Integer, Integer>() {
			@Override
			protected Integer doInBackground() throws Exception {
				boolean tempChanging = isChanging();
				if(!tempChanging) setChanging(true);
				applyRegexFilters();//start : display only relevant tracks
				doUpdatePolygonFilters();//then remove tracks outside polygons
				if(!tempChanging) setChanging(false);
				return null;
			}
		}.execute();
	}
	
	/**
	 * Sets filter type.<br />
	 * Does not apply to the current filters.
	 * @param b If true, filters are conjonctives (= and), otherwise disjunctives (=or)
	 */
	public void setFilterDisjunctive(Boolean b){
		if(isFilterDisjunctive() != b ){
			this.disjunctive = b;
		}
	}
	
	public Boolean isFilterDisjunctive(){
		return this.disjunctive;
	}
	
	/**
	 * Supprime les filtres.<br />
	 * Appeler <code>applyFilters()</code> pour appliquer le changement.
	 */
	public void removeFilter() {
		this.filters.clear();
	}
	
	
	/**
	 * Filter tracks whose field matches regexp
	 * @param field FIELD_ADEP, FIELD_ADEST, FIELD_IAF, FIELD_INDICATIF
	 * @param regexp
	 */
	public void addFilter(int field, String regexp) {
		this.filters.put(field, regexp);
	}

	/**
	 * Apply filters and change tracks' visiblity according to these filters
	 */
	private void applyRegexFilters(){
		if(filters.size() == 0) {
			this.setVisible(true, this.getAllTracks());
			return;
		}
		
		Collection<VidesoTrack> temp = this.isFilterDisjunctive() ? 
				new HashSet<VidesoTrack>() : //"or" : add matching tracks
				new HashSet<VidesoTrack>(this.allTracks); //"and" : remove not matching tracks
				
				
		for(Entry<Integer, String> filter : filters.entrySet()) {
			switch (filter.getKey()) {
			case FIELD_ADEST:
				for(VidesoTrack track : this.getAllTracks()){	
					if(track.isFieldAvailable(FIELD_ADEST)){
						if(track.getArrivee().matches(filter.getValue())){
							//keep this track if disjunctive filter
							if(this.isFilterDisjunctive()){
								temp.add(track);
							}
						} else {
							//remove the track from the pool if not disjunctive filter
							if(!this.isFilterDisjunctive()){
								temp.remove(track);
							}
						}
					}
				}
				break;
			case FIELD_IAF:
				for(VidesoTrack track : this.getAllTracks()){	
					if(track.isFieldAvailable(FIELD_IAF)){
						if(track.getIaf().matches(filter.getValue())){
							//keep this track if disjunctive filter
							if(this.isFilterDisjunctive()){
								temp.add(track);
							}
						} else {
							//remove the track from the pool if not disjunctive filter
							if(!this.isFilterDisjunctive()){
								temp.remove(track);
							}
						}
					}
				}
				break;
			case FIELD_ADEP:
				for(VidesoTrack track : this.getAllTracks()){	
					if(track.isFieldAvailable(FIELD_ADEP)){
						if(track.getDepart().matches(filter.getValue())){
							//keep this track if disjunctive filter
							if(this.isFilterDisjunctive()){
								temp.add(track);
							}
						} else {
							//remove the track from the pool if not disjunctive filter
							if(!this.isFilterDisjunctive()){
								temp.remove(track);
							}
						}
					}
				}
				break;	
			case FIELD_INDICATIF:
				for(VidesoTrack track : this.getAllTracks()){	
					if(track.isFieldAvailable(FIELD_INDICATIF)){
						if(track.getIndicatif().matches(filter.getValue())){
							//keep this track if disjunctive filter
							if(this.isFilterDisjunctive()){
								temp.add(track);
							}
						} else {
							//remove the track from the pool if not disjunctive filter
							if(!this.isFilterDisjunctive()){
								temp.remove(track);
							}
						}
					}
				}
				break;
			case FIELD_TYPE_AVION:
				for(VidesoTrack track : this.getAllTracks()){	
					if(track.isFieldAvailable(FIELD_TYPE_AVION)){
						if(track.getType().matches(filter.getValue())){
							//keep this track if disjunctive filter
							if(this.isFilterDisjunctive()){
								temp.add(track);
							}
						} else {
							//remove the track from the pool if not disjunctive filter
							if(!this.isFilterDisjunctive()){
								temp.remove(track);
							}
						}
					}
				}
				break;
			case FIELD_TYPE_MODE_A:
				for(VidesoTrack track : this.getAllTracks()){
					if(track.isFieldAvailable(FIELD_TYPE_MODE_A)){
						if(track.getModeA().toString().matches(filter.getValue())){
							//keep this track if disjunctive filter
							if(this.isFilterDisjunctive()){
								temp.add(track);
							}
						} else {
							//remove the track from the pool if not disjunctive filter
							if(!this.isFilterDisjunctive()){
								temp.remove(track);
							}
						}
					}
				}
				break;
			default:
				break;
			}
		}		
		boolean tempChanging = isChanging();
		if(!tempChanging) setChanging(true);
		this.setVisible(false, allTracks);
		this.setVisible(true, temp);
		if(!tempChanging) setChanging(false);
	}
	
	/****************** Polygon filter ***********************/
	private int getNumberPolygonFiltersActives(){
		int i = 0;
		for(PolygonsSetFilter p : this.polygonFilters){
			if(p.isActive()){
				i ++;
			}
		}
		return i;
	}
	
	private void doUpdatePolygonFilters(){
//		this.firePropertyChange("change", -1, this.getNumberPolygonFiltersActives()*this.getVisibleTracks().size());
		if(this.polygonFilters == null || this.polygonFilters.size() ==0 || this.getNumberPolygonFiltersActives() == 0)
			return;
		for(PolygonsSetFilter polygon : this.polygonFilters){
			polygon.setContainedTrajectories(0);
		}
		
		Collection<VidesoTrack> paths = getVisibleTracks() ; //ne pas afficher des trajectoires déjà filtrées
		if(isChanging()){
			if(tempTracksVisible != null) paths.addAll(tempTracksVisible);
			if(tempTracksNonVisible != null) paths.removeAll(tempTracksNonVisible);
		} 
		
		Collection<VidesoTrack> visibles = new HashSet<VidesoTrack>();
		if(this.polygonFilters != null && this.polygonFilters.size() != 0 && this.getNumberPolygonFiltersActives() != 0){
//			int i = 0;
			for(PolygonsSetFilter set : this.polygonFilters){
				if(set.isActive()){
					for(VidesoTrack p : paths){
//						i++;
//						this.firePropertyChange("progress", i-1, i);
						Iterator<VPolygon> polygons = set.getPolygons().iterator();
						boolean contain = false;
						while(polygons.hasNext() && !contain){
							VPolygon polygon = polygons.next();
							Iterator<? extends TrackPoint> positions = p.getTrackPoints().iterator();
							while(positions.hasNext() && !contain){
								if(polygon.contains(positions.next().getPosition())){
									visibles.add(p);
									contain = true;
									set.setContainedTrajectories(set.getContainedTrajectories()+1);
								}
							}
						}
					}
				}
			}
		}
		boolean tempChanging = isChanging();
		if(!tempChanging) setChanging(true);
		this.setVisible(false, getVisibleTracks());
		this.setVisible(true, visibles);
		if(!tempChanging) setChanging(false);
//		this.firePropertyChange(AVKey.LAYER, null, this);
	}

	/**
	 * Call <code>applyPolygonFilters</code> to take changes into account
	 * @param polygons
	 */
	public void addPolygonFilter(PolygonsSetFilter polygons) {
		if(polygons == null){
			Logging.logger().severe("Trying to add null polygon filter");
			return;
		}
		if(this.polygonFilters ==  null) this.polygonFilters = new HashSet<PolygonsSetFilter>();
		boolean exist = false;
		Iterator<PolygonsSetFilter> filters = this.polygonFilters.iterator();
		while(filters.hasNext() && !exist){
			if(filters.next().getPolygons().containsAll(polygons.getPolygons())){
				exist =  true;
			}
		}
		if(!exist){
			this.polygonFilters.add(polygons);
		}
		this.applyFilters();
	}
	
	public void disablePolygonFilter(PolygonsSetFilter polygons) {
		if(!this.polygonFilters.contains(polygons)){
			Logging.logger().severe("Trying to disable a non-existing filter");
			return;
		}
		if(polygons.isActive()){
			polygons.setActive(false);
			this.applyFilters();
		}
	}
	
	public void enablePolygonFilter(PolygonsSetFilter polygons) {
		if(!this.polygonFilters.contains(polygons)){
			Logging.logger().severe("Trying to enable a non-existing filter");
			return;
		}
		if(!polygons.isActive()){
			polygons.setActive(true);
			this.applyFilters();
		}
	}
	
	
	public boolean isPolygonFilterActive(PolygonsSetFilter polygon){
		return polygon.isActive();
	}

	public List<PolygonsSetFilter> getPolygonFilters() {
		if(this.polygonFilters == null){
			return null;
		} else {
			return new ArrayList<PolygonsSetFilter>(this.polygonFilters);
		}
	}

	
	public void removePolygonFilter(PolygonsSetFilter polygons) {
		if(polygonFilters != null && this.polygonFilters.contains(polygons)) {
			this.polygonFilters.remove(polygons);
			this.applyFilters();
		}
	}
	
	
	public int getNumberTrajectories(PolygonsSetFilter polygon) {
		if(this.polygonFilters.contains(polygon)){
			return polygon.getContainedTrajectories();
		} else {
			return 0;
		}
	}
	
	/*********************************************************/
	/*********************** Events **************************/
	/*********************************************************/
	
	protected void fireTrackAdded(VidesoTrack track){
		for(TableModelListener l : this.getTableModelListeners()){
			if(l instanceof TracksModelListener){
				((TracksModelListener) l).trackAdded(track);
			}
		}
		//update table data
		this.tracks = Arrays.asList(this.allTracks.toArray());
		fireTableDataChanged();
	}
	
	protected void fireTrackAdded(Collection<VidesoTrack> track){
		for(TableModelListener l : this.getTableModelListeners()){
			if(l instanceof TracksModelListener){
				((TracksModelListener) l).trackAdded(track);
			}
		}
		//update table data
		this.tracks = Arrays.asList(this.allTracks.toArray());
		fireTableDataChanged();
	}
	
	protected void fireTrackRemoved(VidesoTrack track){
		for(TableModelListener l : this.getTableModelListeners()){
			if(l instanceof TracksModelListener){
				((TracksModelListener) l).trackRemoved(track);
			}
		}
		//update table data
		this.tracks = Arrays.asList(this.allTracks.toArray());
		fireTableDataChanged();
	}
	
	protected void fireTrackRemoved(Collection<VidesoTrack> track){
		for(TableModelListener l : this.getTableModelListeners()){
			if(l instanceof TracksModelListener){
				((TracksModelListener) l).trackRemoved(track);
			}
		}
		//update table data
		this.tracks = Arrays.asList(this.allTracks.toArray());
		fireTableDataChanged();
	}
	
	protected void fireTrackVisibilityChanged(VidesoTrack track, boolean visible){
		for(TableModelListener l : this.getTableModelListeners()){
			if(l instanceof TracksModelListener){
				((TracksModelListener) l).trackVisibilityChanged(track, visible);
			}
		}
	}
	
	protected void fireTrackSelectionChanged(VidesoTrack track, boolean selected){
		for(TableModelListener l : this.getTableModelListeners()){
			if(l instanceof TracksModelListener){
				((TracksModelListener) l).trackSelectionChanged(track, selected);
			}
		}
	}
	
	protected void fireTrackVisibilityChanged(Collection<VidesoTrack> track, boolean visible){
		for(TableModelListener l : this.getTableModelListeners()){
			if(l instanceof TracksModelListener){
				((TracksModelListener) l).trackVisibilityChanged(track, visible);
			}
		}
	}
	
	protected void fireTrackSelectionChanged(Collection<VidesoTrack> track, boolean selected){
		for(TableModelListener l : this.getTableModelListeners()){
			if(l instanceof TracksModelListener){
				((TracksModelListener) l).trackSelectionChanged(track, selected);
			}
		}
	}
	
	/****************************************/
	
	public static int string2type(String type){
		if("Départ".equals(type)){
			return FIELD_ADEP;
		} else if("Arrivée".equals(type)){
			return FIELD_ADEST;
		} else if("IAF".equals(type)){
			return FIELD_IAF;
		} else if("Indicatif".equals(type)){
			return FIELD_INDICATIF;
		} else if("Type avion".equals(type)){
			return FIELD_TYPE_AVION;
		} else if("Mode A".equals(type)){
			return FIELD_TYPE_MODE_A;
		}
		return 0;
	}
	
	public static String type2string(int type){
		if(type == FIELD_ADEP){
			return "Départ";
		} else if (type == FIELD_ADEST){
			return "Arrivée";
		} else if (type == FIELD_IAF){
			return "IAF";
		} else if (type == FIELD_INDICATIF){
			return "Indicatif";
		} else if (type == FIELD_TYPE_AVION){
			return "Type avion";
		} else if(type == FIELD_TYPE_MODE_A){
			return "Mode A";
		}
		return null;
	}
}
