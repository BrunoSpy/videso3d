package fr.crnan.videso3d.ihm.components;

import java.awt.Color;

import gov.nasa.worldwind.render.airspaces.Curtain;
import com.visutools.nav.bislider.*;
import fr.crnan.videso3d.graphics.RadioCovPolygon;
import fr.crnan.videso3d.ihm.RadioCovView;
import fr.crnan.videso3d.radio.RadioCovController;


/**  
 * @author Mickael Papail
**/

public class VBiSlider extends BiSlider {

	private Boolean DEBUG = Boolean.FALSE;
	
	public VBiSlider() {
		super();
	}
	
	public VBiSlider(String interpolationMode) {
		super(interpolationMode);
	}
	
	public VBiSlider(String interpolationMode, Boolean minOnTop) {
		super(interpolationMode,minOnTop);
	}

	
	public void setUnhighlighted() {
		this.setOpaque(true);
		this.setFocusable(false);
		this.setEnabled(false);
		this.setFocusTraversalKeysEnabled(false);
		this.setForeground(Color.GRAY);
		//this.setBackground(Color.GRAY);
		this.setMinimumColor(Color.GRAY);
		this.setMaximumColor(Color.GRAY);
	}
	
	public void setHighlighted() {
		this.setOpaque(true);
		this.setFocusable(true);
		this.setEnabled(true);
		this.setFocusTraversalKeysEnabled(true);
		this.setForeground(Color.BLACK);
		//this.setBackground(Color.WHITE);
		this.setMinimumColor(Color.BLUE);
		this.setMaximumColor(Color.GREEN);
	}
	
	/*
	public void setMinMaxValue(Object o) {
		if (DEBUG) System.out.println("Appel methode setMinMax");
		RadioCovPolygon radioCov;
		Object[] tabRadioCov;
		
		if (o instanceof RadioCovPolygon) {
			
			radioCov = (RadioCovPolygon)o;
			tabRadioCov = radioCov.getCurtains().toArray();
			double minValue = ((Curtain)tabRadioCov[0]).getAltitudes()[0];
			double maxValue = ((Curtain)tabRadioCov[tabRadioCov.length-1]).getAltitudes()[1];
			// FIXME this.setMinimumValue(minValue);
			this.setMinimumValue(0);
			this.setMaximumValue(19500);
			// FIXME this.setMaximumValue(maxValue);
			this.setMinimumColoredValue(0);
			this.setMaximumColoredValue(19500);
			this.setToolTipText(radioCov.getName());
			this.setSegmentSize(((int)((this.getMaximumValue()-this.getMinimumValue())/12)));		
		}
	}
	*/
	
	public void compute(Object o, double minimumColoredValue, double maximumColoredValue) {
		
		double minColor = minimumColoredValue;
		double maxColor =  maximumColoredValue;
		int minIndex=0, maxIndex = 0;
		RadioCovPolygon radioCov;
		Object[] tabRadioCov;
						
		if (o instanceof RadioCovPolygon) {
				radioCov = (RadioCovPolygon)o;
				tabRadioCov=radioCov.getCurtains().toArray();
				// maxIndex = tabRadioCov.length-1;
				maxIndex = 0;
								
				
				//for (int i=0;i<tabRadioCov.length-2;i++) {
				//	System.out.println("||||"+radioCov.getRefAltitudes(i)[0]+"|||"+radioCov.getRefAltitudes(i)[1]);
				//}
				
				//for (int i=0;i<tabRadioCov.length-2;i++) {
				//	radioCov.getCurtains().get(i).setAltitudes(radioCov.getRefAltitudes(i)[0],radioCov.getRefAltitudes(i)[1]);
				
					
					/*	System.out.println("la valeur du getRefaltitudes 0 est :"+radioCov.getRefAltitudes(i)[0]);
					System.out.println("la valeur du getRefaltitudes 1 est :"+radioCov.getRefAltitudes(i)[1]);										
					System.out.println("Valeur du maxColoredValue :"+maximumColoredValue);
					System.out.println("Valeur du minColoredValue :"+minimumColoredValue);
				*/
					// if (DEBUG) System.out.println("initialisation");
				//}
		
				// recherche de la position des indices min et max de chaque curseur.
				for (int i=0;i<tabRadioCov.length;i++) {
					//if (biSlider2.getMinimumColoredValue()>((Curtain)tabRadioCov[i]).getAltitudes()[0]) {minIndex++;}								
					
					 if (maximumColoredValue>((Curtain)tabRadioCov[i]).getAltitudes()[1] && (maxIndex <tabRadioCov.length)) {
						maxIndex++;									
					}
					
					if (minimumColoredValue>=(radioCov).getCurtains().get(i).getAltitudes()[0]) {									
						minIndex++;
					}
/*					for (int j=tabRadioCov.length-2;j>0;j--) {
					if (maximumColoredValue>((Curtain)tabRadioCov[j]).getAltitudes()[1]) {
						maxIndex --;
					}
					
				}
*/
					// if (biSlider2.getMinimumColoredValue()>biSlider2.getMaximumColoredValue()) biSlider2.setMinimumColoredValue(biSlider2.getMaximumColoredValue());
					// Il y a un serieux probleme sur la position des indices des curseurs						
				
				}							
				if (DEBUG) System.out.println("valeur de l'index maximum "+maxIndex);
				if (DEBUG) System.out.println("valeur de l'index minimum "+minIndex);
				
				if ((minIndex)>(tabRadioCov.length-1)){ minIndex=tabRadioCov.length-1; System.out.println("minIndex trop grand !");}
				if ((maxIndex)>(tabRadioCov.length-1)){ minIndex=tabRadioCov.length-1; System.out.println("maxIndex trop grand !");}
				if ((minIndex)<0) minIndex=0;
				if ((maxIndex)<0) maxIndex=0;							
				
				// Liste des conditions														
				if (minIndex==0 && maxIndex==0) {	
				
					radioCov.setVisible(0,tabRadioCov.length, false);
					radioCov.setVisible(0,true);																
					// radioCov.getCurtains().get(0).setAltitudes(this.getMinimumColoredValue(), this.getMaximumColoredValue());
				}
				if (minIndex==0 && maxIndex !=0) {
		
					if (maxIndex==(tabRadioCov.length-1)) {
						radioCov.setVisible(0,tabRadioCov.length,true);	
						// radioCov.getCurtains().get(maxIndex).setAltitudes(radioCov.getCurtains().get(maxIndex).getAltitudes()[0], this.getMaximumColoredValue());
						// radioCov.getCurtains().get(minIndex).setAltitudes(this.getMinimumColoredValue(),radioCov.getCurtains().get(minIndex).getAltitudes()[1]);
					}								
					if (maxIndex<(tabRadioCov.length-1)) {
				
						radioCov.setVisible(maxIndex,tabRadioCov.length,false);
						radioCov.setVisible(0,maxIndex-1,true);
						// radioCov.getCurtains().get(maxIndex).setAltitudes(radioCov.getCurtains().get(maxIndex).getAltitudes()[0], this.getMaximumColoredValue());
						radioCov.setVisible(maxIndex,true);
					}
				}
				if (minIndex >0 && maxIndex >0) {

					if (maxIndex==tabRadioCov.length-1) {									
						if (maxIndex==minIndex) {				
							radioCov.setVisible(0,maxIndex,false);										
							radioCov.setVisible(maxIndex,true);
							// radioCov.getCurtains().get(maxIndex).setAltitudes(this.getMinimumColoredValue(),this.getMaximumColoredValue());									
						}
						if (maxIndex!=minIndex) {
							for (int i=0;i<minIndex;i++) {
								radioCov.setVisible(i, false);							
							}
							radioCov.setVisible(0, minIndex, false);											
							radioCov.setVisible(minIndex,tabRadioCov.length-1,true);					
							// radioCov.getCurtains().get(minIndex).setAltitudes(this.getMinimumColoredValue(),radioCov.getCurtains().get(minIndex).getAltitudes()[1]);	
							// radioCov.getCurtains().get(maxIndex).setAltitudes(radioCov.getCurtains().get(maxIndex).getAltitudes()[0],this.getMaximumColoredValue());
							if (DEBUG) System.out.println("Le nom du radioCov courant est"+radioCov.getName());
						}
					}
					if (maxIndex < tabRadioCov.length-1) {						
						if (maxIndex==minIndex) {
							radioCov.setVisible(0,minIndex-1,false);											
							radioCov.setVisible(minIndex,true);
							radioCov.setVisible(minIndex,tabRadioCov.length-1,false);											
							// radioCov.getCurtains().get(maxIndex).setAltitudes(radioCov.getCurtains().get(maxIndex).getAltitudes()[0],this.getMaximumColoredValue());
							// radioCov.getCurtains().get(minIndex).setAltitudes(this.getMinimumColoredValue(),radioCov.getCurtains().get(minIndex).getAltitudes()[1]);
						}
						if (maxIndex > minIndex) {
							radioCov.setVisible(0,minIndex,false);
							radioCov.setVisible(maxIndex-1,tabRadioCov.length,false);
							radioCov.setVisible(minIndex,maxIndex,true);
						//	System.out.println("passage dans la condition, la valeur du scroll haut est "+this.getMaximumColoredValue());
					//FIXME		//radioCov.getCurtains().get(maxIndex).setAltitudes(radioCov.getCurtains().get(maxIndex).getAltitudes()[0],biSlider2.getMaximumColoredValue());
						// 	radioCov.getCurtains().get(maxIndex-1).setAltitudes(radioCov.getCurtains().get(maxIndex-1).getAltitudes()[0],this.getMaximumColoredValue());
						// 	radioCov.getCurtains().get(minIndex).setAltitudes(this.getMinimumColoredValue(),radioCov.getCurtains().get(minIndex).getAltitudes()[1]);											
						}
					}																	
				}							
			}		
		}
	}

