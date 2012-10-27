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
package fr.crnan.videso3d.ihm.components;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
/**
 * InputStream that fires events to monitor the progress of the stream (from 0 to 100)
 * @author Bruno Spyckerelle
 * @version 0.1.1
 */
public class ProgressInputStream extends FilterInputStream {

	public static final String UPDATE = "update";
	
	private PropertyChangeSupport support;
	
	private int size;
	private double nread;
	
	public ProgressInputStream(InputStream in) {
		super(in);
		support = new PropertyChangeSupport(this);
		try {
			size = in.available();
		} catch(IOException e) {
			size = 0;
		}
	}
	
	/* (non-Javadoc)
	 * @see java.io.FilterInputStream#read()
	 */
	@Override
	public int read() throws IOException {
		int c = in.read();
		if( c>= 0) {
			int old = (int) ((nread*100.0)/size);
			nread++;
			firePropertyChange(UPDATE, old, (int) ((nread*100)/size));
		}
		return c;
	}

	
	
	/* (non-Javadoc)
	 * @see java.io.FilterInputStream#read(byte[], int, int)
	 */
	@Override
	public int read(byte[] b, int o, int l) throws IOException {
		int nr = in.read(b, o, l);
		if(nr > 0){
			int old = (int) ((nread*100)/size);
			nread += nr;
			firePropertyChange(UPDATE, old, (int)((nread*100)/size));
		}
		return nr;
	}

	/* (non-Javadoc)
	 * @see java.io.FilterInputStream#read(byte[])
	 */
	@Override
	public int read(byte[] b) throws IOException {
		int nr = in.read(b);
		if(nr > 0){
			int old = (int) ((nread*100)/size);
			nread += nr;
			firePropertyChange(UPDATE, old, (int)((nread*100)/size));
		}
		return nr;
	}

	
	
	/* (non-Javadoc)
	 * @see java.io.FilterInputStream#reset()
	 */
	@Override
	public synchronized void reset() throws IOException {
		in.reset();
		int old = (int) ((nread*100)/size);
		nread = size - in.available();
		firePropertyChange(UPDATE, old, (int)((nread*100)/size));
		
	}

	/* (non-Javadoc)
	 * @see java.io.FilterInputStream#skip(long)
	 */
	@Override
	public long skip(long n) throws IOException {
		long nr = in.skip(n);
		if(nr > 0){
			int old = (int) ((nread*100)/size);
			nread += nr; 
			firePropertyChange(UPDATE, old, (int)((nread*100)/size));
		}
		return nr;
	}

	public void firePropertyChange(PropertyChangeEvent event){
		this.support.firePropertyChange(event);
	}
	
	public void firePropertyChange(String name, Object oldValue, Object newValue){
		this.support.firePropertyChange(name, oldValue, newValue);
	}
	
	public void addPropertyChangeListener(PropertyChangeListener l){
		this.support.addPropertyChangeListener(l);
	}
	
	public void addPropertyChangeListener(String propertyName, PropertyChangeListener l){
		this.support.addPropertyChangeListener(propertyName, l);
	}
	
	public void removePropertyChangeListener(PropertyChangeListener l){
		this.support.removePropertyChangeListener(l);
	}
	
	public void removePropertyChangeListener(String propertyName, PropertyChangeListener l){
		this.support.removePropertyChangeListener(propertyName, l);
	}

}
