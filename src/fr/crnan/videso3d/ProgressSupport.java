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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Permet de suivre l'avancement d'une tache.<br />
 * Lance la propriété <code>TASK_STARTS</code> quand la tache commence. La valeur envoyée correspond au max de la progression.<br />
 * Lance la propriété <code>TASK_PROGRESS</code> à chaque incrément.<br />
 * Lance la propriété <code>TASK_ENDS</code> lorsque la tâche est terminée (facultatif).<br />
 * Lance la propriété <code>TASK_INFO</code> à chaque incrément (facultatif).
 * @author Bruno Spyckerelle
 * @version 0.2.0
 */
public class ProgressSupport {

	public final static String TASK_STARTS = "progress.start";
	
	public final static String TASK_PROGRESS = "progress.progress";
	
	public final static String TASK_INFO = "progress.info";
	
	public final static String TASK_ENDS = "progress.ends"; //optional
	
	private PropertyChangeSupport support;
	
	public ProgressSupport(){
		this.support = new PropertyChangeSupport(this);
	}
	
	public void fireTaskStarts(int maxProgress){
		this.support.firePropertyChange(TASK_STARTS, -1, maxProgress);
	}
	
	public void fireTaskProgress(int progress){
		this.support.firePropertyChange(TASK_PROGRESS, progress-1, progress);
	}
	
	public void fireTaskInfo(String info){
		this.support.firePropertyChange(TASK_INFO, null, info);
	}
	
	public void fireTaskEnds(){
		this.support.firePropertyChange(TASK_ENDS, 0, 1);
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
