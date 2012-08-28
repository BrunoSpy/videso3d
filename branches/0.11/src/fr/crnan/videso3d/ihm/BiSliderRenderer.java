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

// import javax.swing.JLabel;
import javax.swing.JTable;
// import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;
// import java.awt.Color;
import java.awt.Component;
import com.visutools.nav.bislider.*;

import fr.crnan.videso3d.ihm.components.VBiSlider;
import gov.nasa.worldwind.render.airspaces.*;

/**
 * BiSliderRenderer.java  used by RadioCovView.java.
 * @author Mickaël Papail
 */
public class BiSliderRenderer extends VBiSlider implements TableCellRenderer { 
	
	private Boolean DEBUG = Boolean.TRUE;
	private BiSlider biSlider;
	private Airspace airspace;
	//	Border unselectedBorder = null;
	//	Border selectedBorder = null;
	//	boolean isBordered = true;

/*
	public ColorRenderer(boolean isBordered) {
	this.isBordered = isBordered;
	setOpaque(true); //MUST do this for background to show up.
	}
*/
	
	public BiSliderRenderer() {
		//this.isBordered = isBordered;
		super();
		setOpaque(true); 
	}
		
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,int row, int column) {
		//Color newColor = (Color)color;					
				if (value instanceof Airspace) {
					// if (DEBUG) System.out.println("-- appel methode getTableCellRendererComponent classe BiSliderRenderer condition : if Airspace--");
					// if (DEBUG) System.out.println("l'adresse de l'airspace est"+ airspace);
					airspace=(Airspace)value;
					this.setMaximumValue(airspace.getAltitudes()[1]); if (DEBUG) System.out.println("valeur max "+this.getMaximumValue());
					this.setMinimumValue(airspace.getAltitudes()[0]); if (DEBUG) System.out.println("Valeur min "+this.getMaximumValue());
					this.setMaximumColoredValue(airspace.getAltitudes()[1]); if (DEBUG) System.out.println("Valeur Colored max "+this.getMaximumColoredValue());
					this.setMinimumColoredValue(airspace.getAltitudes()[0]); if (DEBUG) System.out.println("Valeur colored min "+this.getMinimumColoredValue());
					this.setVisible(true); if(DEBUG) System.out.println("Visible ? : " + this.isVisible());
					//this.setMaximumColoredValue(airspace.getAltitudes()[1]);
					//this.setMinimumColoredValue(airspace.getAltitudes()[0]);
				}
				
		//if (DEBUG) System.out.println("-- methode getTableCellRendererComponent classe BiSliderRenderer --");
		// setSelectedItem(value);
		/*if (value instanceof Airspace) {
			biSlider = new BiSlider();
			return (biSlider);
		}
		*/
		return this;		
	}
	
}
	

