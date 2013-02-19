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

import java.awt.Dimension;

import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import fr.crnan.videso3d.geom.LatLonUtils;
import fr.crnan.videso3d.graphics.MovableBalise3D;
import fr.crnan.videso3d.graphics.VidesoObject;
import fr.crnan.videso3d.ihm.components.AddObjectDialog;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;

/**
 * IHM to add multiple points from a CSV table
 * @author Bruno Spyckerelle
 * @version 0.1.1
 */
public class MultiplePointsAddGUI extends AddObjectDialog {

	private List<VidesoObject> points;
	private int result = -1;
	
	public MultiplePointsAddGUI(){
		points = new ArrayList<VidesoObject>();
		
		this.setHelpText("<html>" +
				"Utiliser la zone de texte à droite pour ajouter" +
				"<br/> des points avec annotation sur la vue 3D.<br/>" +
				"<br/>" +
				"<b>Format :</b><br/>" +
				"<ul><li>un point par ligne</li>" +
				"<li>données séparées par une virgule</li>" +
				"<li>dans l'ordre :" +
				"<ul><li>nom</li>" +
				"<li>latitude</li>" +
				"<li>longitude</li>" +
				"<li>altitude : nombre en mètres ou niveaux suivis de \"FL\"</li>" +
				"<li>commentaire (optionnel)</li>" +
				"</ul></ul>" +
				"</html>");


		this.setTitle("Ajout d'une liste de points");
		this.setPreferredSize(new Dimension(700, 400));



		final JTextArea textArea = new JTextArea();
		this.setContentComponent(textArea);

		this.addValidateAction(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				new SwingWorker<Void, Void>() {

					@Override
					protected Void doInBackground() throws Exception {
						doProcessTxtArea(textArea);
						return null;
					}

					@Override
					protected void done() {
						//close the dialog if OK
						if(getResult() == JOptionPane.OK_OPTION){
							MultiplePointsAddGUI.this.dispose();
						}
						
					}
				}.execute();
				
			}
		});


		this.pack();
	}

	private void doProcessTxtArea(JTextArea txtArea){
		result = JOptionPane.OK_OPTION;
		String txt = txtArea.getText();
		try{
			for(String line : txt.split("\\n")){
				this.addPoint(line);
			}
		} catch(ParseException e){
			JOptionPane.showMessageDialog(this, "<html><b>Problème :</b><br />L'import de la liste des points ne s'est pas déroulée correctement.<br /><br />" +
					"<b>Solution :</b><br />Vérifiez que le format de chaque ligne est correct.</html>", "Erreur", JOptionPane.ERROR_MESSAGE);
			points.clear();
			result = -1;
			return;
		}
		
	}

	private void addPoint(String line) throws ParseException {
		String[] words = line.split(",");
		if(words.length >= 4){
			LatLon latlon = LatLonUtils.computeLatLonFromString(words[1]+","+words[2]);
			Double altitude = null;
			try { 
				if(words[3].trim().endsWith("FL")){
					String alt = words[3].trim();
					altitude = new Double(alt.substring(0, alt.length()-2))*30.48;
				} else {
					altitude = new Double(words[3]);
				}
			} catch (NumberFormatException e){
				altitude = null;
			}
			if(latlon != null && altitude != null){
				MovableBalise3D point = new MovableBalise3D(words[0], new Position(latlon, altitude));
				if(words.length > 4)
					point.setAnnotation(words[4]);
				points.add(point);
			} else {
				throw new ParseException("Ligne mal formée : "+line, 0);
			}
		} else {
			throw new ParseException("Ligne mal formée : "+line, 0);
		}
	}

	@Override
	protected int getResult() {
		return this.result;
	}

	@Override
	public List<VidesoObject> getObjects() {
		return points;
	}
}
