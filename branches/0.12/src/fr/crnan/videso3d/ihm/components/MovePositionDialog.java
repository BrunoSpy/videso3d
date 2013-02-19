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

import gov.nasa.worldwind.geom.Position;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EmptyBorder;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
/**
 * Small IHM to edit lat, lon and altitude of an AirspaceControlPoint
 * @author Bruno Spyckerelle
 * @version 0.2.1
 */
public class MovePositionDialog extends JDialog {

	private int result=-1;
	
	private Position pos;
	
	private Integer latDeg;
	private Integer latMin;
	private Integer latSec;
	
	private Integer lonDeg;
	private Integer lonMin;
	private Integer lonSec;	
	private JTextField latSecField;
	private JTextField latMinField;
	private JTextField lonDegField;
	private JTextField latDegField;
	private JTextField lonSecField;
	private JTextField lonMinField;
	private JTextField flField;
	private JComboBox<String> latNSField;
	private JComboBox<String> lonEWField;
	
	public MovePositionDialog(Position position){
		this.setModal(true);
		
		getContentPane().setLayout(new BorderLayout());
		
		this.pos = position;
		
		double lat = this.pos.getLatitude().degrees;
		double lon = this.pos.getLongitude().degrees;
		
		this.latDeg = Math.abs((int)lat);
		this.latMin = Math.abs((int)((lat-latDeg)*60));
		this.latSec = Math.abs((int)(((lat-latDeg)*60-latMin)*60));
		
		this.lonDeg = Math.abs((int)lon);
		this.lonMin = Math.abs((int)((lon-lonDeg)*60));
		this.lonSec = Math.abs((int)(((lon-lonDeg)*60-lonMin)*60));
		
		JPanel content = new JPanel();
		content.setBorder(new EmptyBorder(10, 0, 0, 0));
		
		getContentPane().add(content, BorderLayout.WEST);
		
		JLabel latLabel = new JLabel("Latitude ");
		
		JLabel lonLabel = new JLabel("Longitude");
		
		latSecField = new JTextField();
		latSecField.setColumns(3);
		latSecField.setText(this.latSec.toString());

		latMinField = new JTextField();
		latMinField.setColumns(3);
		latMinField.setText(this.latMin.toString());

		lonDegField = new JTextField();
		lonDegField.setColumns(3);
		lonDegField.setText(this.lonDeg.toString());

		latDegField = new JTextField();
		latDegField.setColumns(3);
		latDegField.setText(this.latDeg.toString());
		
		
		lonSecField = new JTextField();
		lonSecField.setColumns(3);
		lonSecField.setText(this.lonSec.toString());

		lonMinField = new JTextField();
		lonMinField.setColumns(3);
		lonMinField.setText(this.lonMin.toString());

		JLabel flLabel = new JLabel("Niveau");
		
		flField = new JTextField();
		flField.setColumns(3);
		flField.setText((int)(this.pos.getAltitude()/30.48)+"");
		
		latNSField = new JComboBox<String>();
		latNSField.setModel(new DefaultComboBoxModel<String>(new String[] {"N", "S"}));
		latNSField.setSelectedItem((lat > 0 ? "N" : "S"));
		
		lonEWField = new JComboBox<String>();
		lonEWField.setModel(new DefaultComboBoxModel<String>(new String[] {"E", "W"}));
		lonEWField.setSelectedItem((lon > 0 ? "E" : "W"));
		
		GroupLayout gl_content = new GroupLayout(content);
		gl_content.setHorizontalGroup(
			gl_content.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_content.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_content.createParallelGroup(Alignment.LEADING)
						.addComponent(latLabel)
						.addComponent(lonLabel)
						.addComponent(flLabel))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_content.createParallelGroup(Alignment.LEADING)
						.addComponent(flField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGroup(gl_content.createSequentialGroup()
							.addComponent(latDegField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(latMinField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(latSecField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(latNSField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGroup(gl_content.createSequentialGroup()
							.addComponent(lonDegField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(lonMinField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(lonSecField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(lonEWField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);
		gl_content.setVerticalGroup(
			gl_content.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_content.createSequentialGroup()
					.addGroup(gl_content.createParallelGroup(Alignment.BASELINE)
						.addComponent(latLabel)
						.addComponent(latSecField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(latMinField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(latDegField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(latNSField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addGroup(gl_content.createParallelGroup(Alignment.BASELINE)
						.addComponent(lonLabel)
						.addComponent(lonDegField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lonSecField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lonMinField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lonEWField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_content.createParallelGroup(Alignment.BASELINE)
						.addComponent(flLabel)
						.addComponent(flField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addContainerGap())
		);
		content.setLayout(gl_content);
		
		
		JPanel buttons = new JPanel();
		buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
		buttons.add(Box.createHorizontalGlue());
		JButton val = new JButton("Valider");
		val.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				result = JOptionPane.OK_OPTION;
				double lat = Math.abs((Double.parseDouble(latDegField.getText())+
							Double.parseDouble(latMinField.getText())/60.0+
							Double.parseDouble(latSecField.getText())/3600.0))*
							(latNSField.getSelectedItem().equals("N") ? 1.0 : -1.0);
				double lon = Math.abs((Double.parseDouble(lonDegField.getText())+
							Double.parseDouble(lonMinField.getText())/60.0+
							Double.parseDouble(lonSecField.getText())/3600.0))*
							(lonEWField.getSelectedItem().equals("E") ? 1.0 : -1.0);
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
		getContentPane().add(buttons, BorderLayout.SOUTH);
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
