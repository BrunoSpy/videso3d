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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import fr.crnan.videso3d.DatasManager;
import fr.crnan.videso3d.Pallet;
import fr.crnan.videso3d.VidesoGLCanvas;
import fr.crnan.videso3d.geom.LatLonUtils;
import fr.crnan.videso3d.graphics.PolygonAnnotation;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.globes.Earth;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.airspaces.BasicAirspaceAttributes;
/**
 * IHM d'import de liste de points pour créer un polygone 3D
 * @author Adrien Vidal
 * @version 0.1.2
 */
public class PolygonImportUI extends JFrame implements ActionListener{

	private final JTextField nameText;
	private JTextField plancherText;
	private JTextField plafondText;
	private JTextArea coordArea;
	private JButton importButton, cancelButton;
		
	private VidesoGLCanvas wwd;
	
	public PolygonImportUI(VidesoGLCanvas wwd){
		this.wwd = wwd;
		
		this.setTitle("Création d'un polygone...");
		JLabel nameLabel = new JLabel("Nom du polygone ");
		nameText = new JTextField("Polygone",12);
		nameText.addFocusListener(new FocusAdapter(){
			@Override
			public void focusGained(FocusEvent e){
				nameText.selectAll();
			}
		});
		JLabel plancherLabel = new JLabel("Plancher ");
		JLabel plafondLabel = new JLabel("Plafond ");
		plancherText = new JTextField("0",8);
		plafondText = new JTextField("100",8);
		JPanel firstPanel = new JPanel();
		GridLayout gLayout = new GridLayout(3,2);
		gLayout.setHgap(0);
		firstPanel.setLayout(gLayout);
		firstPanel.add(nameLabel);
		firstPanel.add(nameText);
		firstPanel.add(plancherLabel);
		firstPanel.add(plancherText);
		firstPanel.add(plafondLabel);
		firstPanel.add(plafondText);
		JPanel niveauxPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
		niveauxPanel.add(firstPanel);
		niveauxPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
		
		JLabel enterCoord = new JLabel("<html>Entrez les coordonnées du polygone ci-dessous : </html>");
		JPanel enterCoordPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
		enterCoordPanel.add(enterCoord);
		coordArea = new JTextArea(15,80);
		JPanel coordPanel = new JPanel();
		coordPanel.setLayout(new BoxLayout(coordPanel, BoxLayout.PAGE_AXIS));
		coordPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		coordPanel.add(enterCoordPanel);
		JScrollPane jsp = new JScrollPane(coordArea);
		jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		coordPanel.add(jsp);
		
		importButton = new JButton("Importer");
		cancelButton = new JButton("Annuler");
		importButton.addActionListener(this);
		cancelButton.addActionListener(this);
		
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
		buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		buttonPane.add(Box.createHorizontalGlue());
		buttonPane.add(cancelButton);
		buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
		buttonPane.add(importButton);

		this.getRootPane().setDefaultButton(importButton);
		
		this.getContentPane().add(niveauxPanel, BorderLayout.PAGE_START);
		this.getContentPane().add(coordPanel, BorderLayout.CENTER);
		this.getContentPane().add(buttonPane, BorderLayout.PAGE_END);
		this.pack();
		this.setLocation(100, 50);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource()==importButton){
			String error = "";
			final String text = coordArea.getText();
			double plancher = -1, plafond = -1;
			try{
				plancher = Double.parseDouble(plancherText.getText())*30.48;
				plafond = Double.parseDouble(plafondText.getText())*30.48;
			}catch(NumberFormatException exc){
				error = "Plancher ou plafond incorrect";
				exc.printStackTrace();
			}
			if(plancher<0 || plafond <0 || plafond<plancher)
				error = "Plancher ou plafond incorrect";
			PolygonAnnotation p = new PolygonAnnotation();
			ArrayList<LatLon> locationsList = parseLocations(text);
			if(locationsList.size()<3)
				error = "Coordonnées incorrectes ou insuffisantes";
			if(error.isEmpty()){
				p.setLocations(locationsList);
				p.setAltitudes(plancher, plafond);
				BasicAirspaceAttributes attrs = new BasicAirspaceAttributes();
				attrs.setDrawOutline(true);
				attrs.setMaterial(new Material(Color.CYAN));
				attrs.setOutlineMaterial(new Material(Pallet.makeBrighter(Color.CYAN)));
				attrs.setOpacity(0.2);
				attrs.setOutlineOpacity(0.9);
				attrs.setOutlineWidth(1.5);
				p.setAttributes(attrs);
				p.setAnnotation("<html><b>"+nameText.getText()+"</b><br/><i>Polygone importé</i></html>");
				if(!p.getLocations().isEmpty()){
					DatasManager.getUserObjectsController(wwd).addObject(p);
				}
				setVisible(false);
				dispose();	
			}else{
				JOptionPane.showMessageDialog(null, error, "Erreur", JOptionPane.YES_OPTION);
			}
			
		}else if(e.getSource()==cancelButton){
			dispose();
		}

	}


	private ArrayList<LatLon> parseLocations(String text) {
		ArrayList<LatLon> locationsList = new ArrayList<LatLon>();	
		//Remplacement des caractères spéciaux que l'on peut trouver dans les coordonnées géographiques du SIA
		text = text.replaceAll("--", " ");
		text = text.replaceAll("\\u00b0", "d");
		text = text.replaceAll("\\u00ba", "d");
		text = text.replaceAll("\\u2019", "'");
		text = text.replaceAll("\\u201d", "\"");
		
		//Découpage pour une lecture ligne par ligne
		String[] lines = text.split("\\n");
		for(int i = 0; i<lines.length; i++){
			String line = lines[i];
			//Si la ligne décrit un arc de cercle :
			if(line.startsWith("arc")){
				boolean anti = line.contains("anti");
				double radius = Double.parseDouble(line.split("horaire de ")[1].split("NM")[0].replaceFirst(",", "."));
				//On mémorise la position du premier point de l'arc de cercle dans la liste (c'est le dernier point de la liste à cette étape).
				int firstCirclePointPositionInList = locationsList.size()-1;
				//On découpe la ligne suivante en deux : le premier point de la ligne est le centre du cercle, le point suivant s'il existe est
				//le dernier point de l'arc de cercle
				i++;
				String[] points = lines[i].split("\\s+",2);
				lines[i] = points[1];
				//On va chercher le dernier point de l'arc de cercle dans les lignes suivantes.
				while(locationsList.size()==firstCirclePointPositionInList+1){
					addLocationsToList(locationsList, lines[i]);
					i++;
				}
				i--;
				LatLon center = null;
				String centerPoint = points[0].toUpperCase();
				if(	centerPoint.matches("\\d{1,3}([\\.,]\\d{1,4})?[NS]\\d{1,3}([\\.,]\\d{1,4})?[EW]")
						|| centerPoint.matches("\\d{1,3}D(\\d{1,2}')?(\\d{1,2}\")?[NS],?\\d{1,3}D(\\d{1,2}')?(\\d{1,2}\")?[EW]")){
					center = LatLonUtils.computeLatLonFromString(centerPoint);
				}
				//Calcul des angles formés respectivement par le centre du cercle et les premier et dernier points de l'arc par rapport au nord.
				double angle1 = LatLon.greatCircleAzimuth(center, locationsList.get(firstCirclePointPositionInList)).degrees;
				double angle2 = LatLon.greatCircleAzimuth(center, locationsList.get(firstCirclePointPositionInList+1)).degrees;
				if(angle1<0)
					angle1 += 360;
				if(angle2<0)
					angle2 += 360;
				//Calcul du nombre de kilomètres par degré de longitude à la latitude du centre du cercle
				double lonConv = LatLon.ellipsoidalDistance(center, new LatLon(center.getLatitude(), center.getLongitude().addDegrees(0.1)), Earth.WGS84_EQUATORIAL_RADIUS, Earth.WGS84_POLAR_RADIUS)/100;
				double angleToAdd=0;
				if(angle1<angle2){
					if(anti){
						angleToAdd = -360+angle2-angle1;
					}else{
						angleToAdd = angle2-angle1;
					}
				}else{
					if(anti){
						angleToAdd = angle2-angle1;
					}else{
						angleToAdd = 360-angle1+angle2;
					}
				}
				//On crée 20 points répartis sur l'arc de cercle :
				for(int j = 1; j<20; j++){
					double angle = (angle1 + angleToAdd/20*j)*Math.PI/180;
					double latitude = center.latitude.degrees + (radius /60  * Math.cos(angle));
					double longitude = center.longitude.degrees + (radius*1.852 / lonConv * Math.sin(angle));
					LatLon point = LatLon.fromDegrees(latitude, longitude);
					locationsList.add(firstCirclePointPositionInList+j, point);
				}
			}else{
				addLocationsToList(locationsList, line);
			}
		}
		
		return locationsList;
	}

	private void addLocationsToList(ArrayList<LatLon> locationsList, String line){
		String[] points = line.split("\\s+");
		boolean pointsFound = false;
		for(String p : points){
			p = p.toUpperCase();
			if(	p.matches("\\d{1,3}([\\.,]\\d{1,4})?[NS]\\d{1,3}([\\.,]\\d{1,4})?[EW]")
					|| p.matches("\\d{1,3}D(\\d{1,2}')?(\\d{1,2}\")?[NS],?\\d{1,3}D(\\d{1,2}')?(\\d{1,2}\")?[EW]")){
				LatLon latlon = LatLonUtils.computeLatLonFromString(p);
				if(latlon != null){
					locationsList.add(latlon);
					pointsFound = true;
				}
			}
		}
		//if no point was found, try with just one point per line
		if(!pointsFound){
			LatLon latlon = LatLonUtils.computeLatLonFromString(line);
			if(latlon != null){
				locationsList.add(latlon);
			}
		}
	}

}
