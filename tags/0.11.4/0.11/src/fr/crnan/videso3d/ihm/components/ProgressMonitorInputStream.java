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

import java.awt.Component;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;

/**
 * {@link javax.swing.ProgressMonitorInputStream} qui se déclenche même pour les fichiers dont la taille est infèrieure à 8ko.
 * @author Adrien Vidal
 * @version 0.1.0
 */
public class ProgressMonitorInputStream extends javax.swing.ProgressMonitorInputStream {

	private int nread = 0;
	
	public ProgressMonitorInputStream(Component parent, Object message,
			InputStream in) {
		super(parent, message, in);
	}

	@Override
	public int read(byte[]b, int off, int len) throws IOException{
		   int nr = in.read(b, off, Math.min(len, 32));
	       if (nr > 0){
	    	   super.getProgressMonitor().setProgress(nread += nr);
	       }
	        if (super.getProgressMonitor().isCanceled()) {
	            InterruptedIOException exc =
	                                    new InterruptedIOException("progress");
	           exc.bytesTransferred = nread;
	           throw exc;
	       }
	        return nr;
	}
}
