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

import java.awt.Component;

import fr.crnan.videso3d.Cancelable;
/**
 * {@link ProgressMonitor} which cancels an object when the "Cancel" button has been hitten.<br />
 * Due to implementation limitation, the cancellation is only verified when {@link #setProgress(int)} or {@link #setNote(String)} are called.
 * @author Bruno Spyckerelle
 * @version 0.0.1
 */
public class ProgressMonitorCanceller extends ProgressMonitor {

	private Cancelable object;
	
	
	public ProgressMonitorCanceller(Component parentComponent, Object message,
			String note, int min, int max, boolean automaticProgress,
			boolean showCancel, boolean indeterminate) {
		super(parentComponent, message, note, min, max, automaticProgress, showCancel,
				indeterminate);
	}

	public ProgressMonitorCanceller(Component parentComponent, Object message,
			String note, int min, int max, boolean automaticProgress,
			boolean showCancel) {
		super(parentComponent, message, note, min, max, automaticProgress, showCancel);
	}

	public ProgressMonitorCanceller(Component parentComponent, Object message,
			String note, int min, int max) {
		super(parentComponent, message, note, min, max);
	}

	public ProgressMonitorCanceller(Component parentComponent, Object message,
			String note, int min, int max, Cancelable object) {
		super(parentComponent, message, note, min, max);
		
		if(object != null)
			this.object = object;
		
	}

	public ProgressMonitorCanceller(Component parentComponent, Object message,
			String note, int min, int max, boolean automaticProgress,
			boolean showCancel, boolean indeterminate, Cancelable object) {
		super(parentComponent, message, note, min, max, automaticProgress, showCancel,
				indeterminate);

		if(object != null)
			this.object = object;
	}



	public ProgressMonitorCanceller(Component parentComponent, Object message,
			String note, int min, int max, boolean automaticProgress,
			boolean showCancel, Cancelable object) {
		super(parentComponent, message, note, min, max, automaticProgress, showCancel);
		if(object != null)
			this.object = object;
	}


	/**
	 * 
	 * @param object Object to cancel if "Cancel" has been hitten
	 */
	public void setCancelable(Cancelable object){
		this.object = object;
	}

	@Override
	public void setProgress(int nv) {
		super.setProgress(nv);
		if(this.isCanceled() && this.object != null)
			object.cancel();
	}

	@Override
	public void setNote(String note) {
		super.setNote(note);
		
		if(this.isCanceled() && this.object != null)
			object.cancel();
	}

	
	
}
