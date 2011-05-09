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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.airspaces.editor.AirspaceControlPoint;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
/**
 * Small IHM to edit lat, lon and altitude of an AirspaceControlPoint
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class MoveAirspaceControlPointDialog extends JDialog {

	private int result;
	
	private Position pos;
	
	private Integer latDeg;
	private Integer latMin;
	private Integer latSec;
	
	private Integer lonDeg;
	private Integer lonMin;
	private Integer lonSec;	
	
	public MoveAirspaceControlPointDialog(WorldWindow wwd, AirspaceControlPoint point){
		this.setModal(true);
		
		this.setLayout(new BorderLayout());
		
		this.pos = wwd.getModel().getGlobe().computePositionFromPoint(point.getPoint());
		
		double lat = this.pos.getLatitude().degrees;
		double lon = this.pos.getLongitude().degrees;
		
		this.latDeg = (int)lat;
		this.latMin = (int)((lat-latDeg)*60);
		this.latSec = (int)(((lat-latDeg)*60-latMin)*60);
		
		this.lonDeg = (int)lon;
		this.lonMin = (int)((lon-lonDeg)*60);
		this.lonSec = (int)(((lon-lonDeg)*60-lonMin)*60);
		
		JPanel content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		
		JPanel latPanel = new JPanel();
		latPanel.setLayout(new BoxLayout(latPanel, BoxLayout.X_AXIS));
		JLabel latLabel = new JLabel("Latitude ");
		final JTextField latDeg = new JTextField(3);
		latDeg.setText(this.latDeg.toString());
		final JTextField latMin = new JTextField(3);
		latMin.setText(this.latMin.toString());
		final JTextField latSec = new JTextField(3);
		latSec.setText(this.latSec.toString());
		
		latPanel.add(latLabel);
		latPanel.add(latDeg);
		latPanel.add(latMin);
		latPanel.add(latSec);
		
		content.add(latPanel);
		
		JPanel lonPanel = new JPanel();
		lonPanel.setLayout(new BoxLayout(lonPanel, BoxLayout.X_AXIS));
		JLabel lonLabel = new JLabel("Longitude ");
		final JTextField lonDeg = new JTextField(3);
		lonDeg.setText(this.lonDeg.toString());
		final JTextField lonMin = new JTextField(3);
		lonMin.setText(this.lonMin.toString());
		final JTextField lonSec = new JTextField(3);
		lonSec.setText(this.lonSec.toString());
		
		lonPanel.add(lonLabel);
		lonPanel.add(lonDeg);
		lonPanel.add(lonMin);
		lonPanel.add(lonSec);
		
		content.add(lonPanel);
		
		JPanel fl = new JPanel();
		fl.setLayout(new BoxLayout(fl, BoxLayout.X_AXIS));
		fl.add(new JLabel("Niveau "));
		final JTextField flField = new JTextField(3);
		flField.setText((int)(this.pos.getAltitude()/30.48)+"");
		fl.add(flField);
		content.add(fl);
		
		this.add(content, BorderLayout.CENTER);
		
		JPanel buttons = new JPanel();
		buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
		buttons.add(Box.createHorizontalGlue());
		JButton val = new JButton("Valider");
		val.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				result = JOptionPane.OK_OPTION;
				double lat = Double.parseDouble(latDeg.getText())+
							Double.parseDouble(latMin.getText())/60.0+
							Double.parseDouble(latSec.getText())/3600.0;
				double lon = Double.parseDouble(lonDeg.getText())+
							Double.parseDouble(lonMin.getText())/60.0+
							Double.parseDouble(lonSec.getText())/3600.0;
				pos = Position.fromDegrees(lat, lon, Double.parseDouble(flField.getText())*30.48);
				setVisible(false);
			}
		});
		buttons.add(val);
		JButton cnl = new JButton("Annuler");
		cnl.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				result = JOptionPane.CANCEL_OPTION;
				setVisible(false);
			}
		});
		buttons.add(cnl);
		this.add(buttons, BorderLayout.SOUTH);
		this.pack();
	}
	
	public int showDialog(MouseEvent evt){
		this.setLocation(evt.getXOnScreen(), evt.getYOnScreen());
		this.setVisible(true);
		return result;
	}
	
	public Position getPosition(){
		return this.pos;
	}
	
}
