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

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
/**
 * {@link JDesktopPane} witj tling capability
 * @author Bruno Spyckerelle
 * @version 0.1.0
 */
public class TilingDesktopPane extends JDesktopPane {

	public void tile(int layer ) {
	    JInternalFrame[] frames = this.getAllFramesInLayer( layer );
	    if ( frames.length == 0) return;
	 
	    tile( frames, this.getBounds() );
	}
	
	public void tile(boolean onlyOpenedFrames) {
	    JInternalFrame[] frames = this.getAllFrames();
	    if(onlyOpenedFrames){
	    	List<JInternalFrame> fr = new ArrayList<JInternalFrame>();
	    	for(JInternalFrame f : frames){
	    		if(!f.isClosed() && !f.isIcon()){
	    			fr.add(f);
	    		}
	    	}
	    	frames = fr.toArray(new JInternalFrame[]{});
	    }
	    if ( frames.length == 0) return;
	 
	    tile( frames, this.getBounds() );
	}
	
	private void tile( JInternalFrame[] frames, Rectangle dBounds ) {
	    int cols = Math.max(2,(int)Math.sqrt(frames.length));
	    int rows = Math.max(2,(int)(Math.ceil( ((double)frames.length) / cols)));
	    int lastRow = frames.length - cols*(rows-1);
	    int width, height;
	 
	    if ( lastRow == 0 ) {
	        rows--;
	        height = dBounds.height / rows;
	    }
	    else {
	        height = dBounds.height / rows;
	        if ( lastRow < cols ) {
	            rows--;
	            width = dBounds.width / lastRow;
	            for (int i = 0; i < lastRow; i++ ) {
	                frames[cols*rows+i].setBounds( i*width, rows*height,
	                                               width, height );
	            }
	        }
	    }
	            
	    width = dBounds.width/cols;
	    for (int j = 0; j < rows; j++ ) {
	        for (int i = 0; i < cols; i++ ) {
	            frames[i+j*cols].setBounds( i*width, j*height,
	                                        width, height );
	        }
	    }
	}
	
}
