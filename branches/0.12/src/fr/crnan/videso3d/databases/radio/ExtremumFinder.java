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

package fr.crnan.videso3d.databases.radio;

/**
* @author mickael papail
* @version 0.4
**/

import java.util.ArrayList;
import java.util.Iterator;

import fr.crnan.videso3d.Couple;
import fr.crnan.videso3d.Triplet;

public class ExtremumFinder {

	public double lat = 0;
	public double lon =0 ;
	public double minLat = 0;
	public double maxLat = 0;
	public double minLon = 0;
	public double maxLon = 0;
	private boolean  newLat = true;	
	private double alt = 0;
	private boolean debug = false;
	

	
	private ArrayList<Triplet<Double,Double,Double>> tab = new ArrayList<Triplet<Double,Double,Double>>();		 
	private ArrayList<Couple<Double,Double>> latLon = new ArrayList<Couple<Double,Double>>();	
	private ArrayList<Double> averageLonTab = new ArrayList<Double>();
		
	Iterator <Triplet<Double,Double,Double>> iter = tab.iterator();
	Iterator<Couple<Double,Double>> latLonIter =latLon.iterator();
	
	int i = 0;
	
	public ExtremumFinder() {		
	}
	
	public void setAlt(double alt) {
		this.alt = alt;
	}
			
	public void setLatMinMax(double currentLat) {
		if (currentLat< minLat) minLat = currentLat;
		if (currentLat>maxLat) maxLat = currentLat;
	}
		
	public ArrayList<Couple<Double,Double>> getLatLon() {
		return this.latLon;
	}
	
	public ArrayList<Triplet<Double,Double,Double>> getTab() {
		return this.tab;
	}
	
	public void compute(double currentLat, double currentLon, double level) {

		if (level>=0) {
			/*1ere ligne du fichier , puis lignes a partir de la 2eme pour chaque nouvelle latitude*/
			if (newLat == true ) {
				lat = currentLat;
				if (i==0) {
					minLon = currentLon;
					maxLon = currentLon;
				}						
				newLat = false;
				i++;									
			}		
			/*cas normal */
			if ((i != 0) && (currentLat == lat)) {
				maxLon = currentLon;
			}
	
			/*Changement de valeur de latitude dans le fichier + nouvelle 1ere ligne de latitude*/
			if ((newLat == false) && (currentLat != lat)) {
			
				Triplet<Double,Double,Double> triplet = new Triplet<Double,Double,Double>();
				triplet.setFirst(lat);
				triplet.setSecond(minLon);
				triplet.setThird(maxLon);								
				tab.add(triplet);				
				averageLonTab.add((maxLon-minLon)/2);
				minLon = currentLon;
				maxLon = currentLon;
				newLat = true;
				i=1;						
			}
		}	
		if (debug )System.out.println("taille du tableau :"+tab.size());
	}
	
	public double  computeAverageLon() {
		double averageLon = 0;
		/*double size = averageLonTab.size();
		for (int i=0;i<size;i++) {
			average += averageLonTab.get(i);
		}
		*/
		//return average / size;
		averageLon  = tab.get(0).getSecond()+tab.get(tab.size()-1).getSecond();
		return (double) averageLon / 2;
	}
	
	
	public double computeAverageLat() {
		double averageLat = 0;
		averageLat = tab.get(0).getFirst()+tab.get(tab.size()-1).getFirst();
		return (double) averageLat / 2;
	}
		
	public void reduceToCouple() {
		
		int size = tab.size()-1;
		this.computeAverageLon();

		
		for (int i=0;i<tab.size();i++) {
			if (i == 0) {
				Couple<Double,Double> couple = new Couple<Double,Double>();
				couple.setFirst(tab.get(i).getFirst()); //lat
				couple.setSecond(tab.get(i).getSecond()); //lonMin
				latLon.add(couple);
			}
			else {																							
				
				// double avg = (tab.get((int)(tab.size()/2)).getSecond()-tab.get(0).getSecond())/2;
				
				if (i<tab.size()-2) {
				
				//if (Math.abs((tab.get(i).getSecond()-averageLon)) <= Math.abs((6*(tab.get(i-1).getSecond()-averageLon)))) { // Tolérance de 25% par rapport à la distance à la mediane  ( filtrage des données.)			
							
					
//					double distanceBorne = 0;
//					if ((tab.get(i).getSecond() >=0 && tab.get(i-1).getSecond() >=0) ) {distanceBorne = Math.abs(Math.abs(Math.sin( tab.get(i+1).getSecond())-Math.abs(Math.sin(tab.get(i-1).getSecond()))) );} /* i et i+1 de même signe*/
//					if ((tab.get(i).getSecond() >=0 && tab.get(i-1).getSecond() <=0) ) {distanceBorne = Math.abs(Math.abs(Math.sin( tab.get(i+1).getSecond())-Math.abs(Math.sin(tab.get(i-1).getSecond()))) );} /*i et i+1 de signe différent*/
//					if ((tab.get(i).getSecond() <=0 && tab.get(i-1).getSecond() <=0) ) {distanceBorne = Math.abs(Math.abs(Math.sin( tab.get(i+1).getSecond())-Math.abs(Math.sin(tab.get(i-1).getSecond()))) );} /*i et i+1 de meme signe*/
										
					double distance = 0;
					if ((tab.get(i).getSecond() >=0 && tab.get(i+1).getSecond() >=0) ) {distance = Math.abs(Math.abs(Math.sin( tab.get(i+1).getSecond())-Math.abs(Math.sin(tab.get(i).getSecond()))) );} /* i et i+1 de même signe*/
					if ((tab.get(i).getSecond() >=0 && tab.get(i+1).getSecond() <=0) ) {distance = Math.abs(Math.abs(Math.sin( tab.get(i).getSecond())+Math.abs(Math.sin(tab.get(i+1).getSecond()))) );} /*i et i+1 de signe différent*/
					if ((tab.get(i).getSecond() <=0 && tab.get(i+1).getSecond() <=0) ) {distance = Math.abs(Math.abs(Math.sin( tab.get(i+1).getSecond())-Math.abs(Math.sin(tab.get(i).getSecond()))) );} /*i et i+1 de meme signe*/
									
							
					/*1er quadrant */
					
					 if (tab.get(i).getSecond() <0 &&  tab.get(i).getThird() >0 && tab.get(i).getFirst()>computeAverageLat()) {
						/* if ( (tab.get(i-1).getSecond())>(tab.get(i).getSecond()) ){*/
									  										  
							 boolean test = true;
							 for (int j=0;j<i;j++) {										
							if (tab.get(j).getSecond() < tab.get(i).getSecond() ) { test = false; }
//							 for (int j=i;j<tab.size();j++) {										
//								 if (tab.get(j).getSecond() > tab.get(i).getSecond() ) { test = false; }
							 }									 
							 if (test == true) {
								 Couple<Double,Double> couple = new Couple<Double,Double>();
								 couple.setFirst(tab.get(i).getFirst()); //lat
								 couple.setSecond(tab.get(i).getSecond()); //lonMin
								 latLon.add(couple);
							}									 
					 /*	}*/
					}	
					 							 
					 if (tab.get(i).getSecond() >0 &&  tab.get(i).getThird() >0 && tab.get(i).getFirst()>computeAverageLat()) {
							/* if ( (tab.get(i-1).getSecond())>(tab.get(i).getSecond()) ){*/
										  										  
								 boolean test = true;
								 for (int j=0;j<i;j++) {										
								if (tab.get(j).getSecond() < tab.get(i).getSecond() ) { test = false; }
//								 for (int j=i;j<tab.size();j++) {										
//									 if (tab.get(j).getSecond() > tab.get(i).getSecond() ) { test = false; }
								 }									 
								 if (test == true) {
									 Couple<Double,Double> couple = new Couple<Double,Double>();
									 couple.setFirst(tab.get(i).getFirst()); //lat
									 couple.setSecond(tab.get(i).getSecond()); //lonMin
									 latLon.add(couple);
								}									 
						 /*	}*/
						}						 																
														
					/*Deuxieme quadrant*/											
						
						/* 1er quart du deuxième quadrant.*/
						
							 if (tab.get(i).getSecond() <0 && tab.get(i).getThird() >0 && tab.get(i).getFirst()<=computeAverageLat()  && tab.get(i).getSecond()<tab.get(i).getThird() && Math.abs(Math.abs(Math.sin(tab.get(i).getSecond()))-Math.abs(Math.sin(tab.get(i-1).getSecond())))<10*distance) { // derniere condition = filtre anti-crenelage sur le quadrant
								 if ( (tab.get(i-1).getSecond())>=(tab.get(i).getSecond()) ){														  										  
											 boolean test = true;
											 for (int j=i;j<tab.size();j++) {
												 if (tab.get(i).getSecond() < tab.get(j).getSecond() ) { test = false; }														 
												 	if (test == true) {
												 		Couple<Double,Double> couple = new Couple<Double,Double>();
												 		couple.setFirst(tab.get(i).getFirst()); //lat
												 		couple.setSecond(tab.get(i).getSecond()); //lonMin
												 		latLon.add(couple);
												 	}
											 }	
									 }						
						 }							 							
						 											 						
						 //// deuxième quadrant  2ème quart.
						
						 if (tab.get(i).getSecond() >=0 && tab.get(i).getThird() >=0 && tab.get(i-1).getSecond() >=0 && tab.get(i+1).getSecond()>=0 && tab.get(i).getSecond()<tab.get(i).getThird() && tab.get(i).getFirst()<=computeAverageLat()) {
										
								 boolean test = true;
								 for (int j=i;j<tab.size();j++) {
									 if (tab.get(i).getSecond() > tab.get(j).getSecond()) { test = false; }
								 }								 
								 if (test == true) {
									 Couple<Double,Double> couple = new Couple<Double,Double>();
									 couple.setFirst(tab.get(i).getFirst()); //lat
									 couple.setSecond(tab.get(i).getSecond()); //lonMin
									 latLon.add(couple);
								 }								 				
					}										 						
						 						 
				}	
			}
		}
					
		for (int i=0;i<tab.size();i++) {			
			if (i==0) {
				Couple<Double,Double> couple = new Couple<Double,Double>();
				couple.setFirst(tab.get(size).getFirst()); //lat
				couple.setSecond(tab.get(size).getThird()); //lonMin
				latLon.add(couple);
			}
			else {				
				if (size - i - 2 >=0  ) { // sinon exception déclenchée !
								
					double distanceBorne = Math.abs(Math.abs(Math.sin( tab.get(size-i+1).getThird())-Math.abs(Math.sin(tab.get(size-i-1).getThird()))) );
					double distance = Math.abs(Math.abs(Math.sin( tab.get(size-i).getThird())-Math.abs(Math.sin(tab.get(size-i+1).getThird()))) );
										
					if ( (tab.get(size-i).getThird())>(tab.get(size-i+1).getThird()) && ( distance<=(3*distanceBorne)) ){  																							
							Couple<Double,Double> couple = new Couple<Double,Double>();
							couple.setFirst(tab.get(size-i).getFirst()); //lat
							couple.setSecond(tab.get(size-i).getThird()); //lonMax
							latLon.add(couple);							
					}
				}
			}	 
		} 
		
		/*rebouclage du dernier element sur le premier*/
		Couple<Double,Double> couple = new Couple<Double,Double>();
		couple.setFirst(tab.get(0).getFirst());
		couple.setSecond(tab.get(0).getSecond()); //lonMax
		latLon.add(couple);
		
	}
	
/*
	private double computeDist(double a,double b) {
		return Math.abs(Math.abs(Math.sin(b))-Math.abs(Math.sin(a)));
	}
*/
			
	public String toString() {
		String s="";
		ArrayList<String> tempContent = new ArrayList<String>(); 		
		tempContent.add("<RadioCovPolygon>"+"\n\t");
		tempContent.add("<alt>"+alt+"</alt>"+"\n\t");
		for (int i=0;i<latLon.size();i++) {
			tempContent.add("<latlon> "+"\n\t"+
			" <latitude>"+latLon.get(i).getFirst()+"</latitude>" +"\n\t"+
			"<longitude>"+latLon.get(i).getSecond()+"</longitude>" +"\n"+
			"</latlon>"+"\n");
		}
		tempContent.add("</RadioCovPolygon>"+"\n");
		s = tempContent.toString();
		// clean the ArrayList.toString() method...
		s = s.replace("[","");
		s = s.replace("]","");
		s = s.replace(",","");
		return s;
	}
	
}
