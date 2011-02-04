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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import fr.crnan.videso3d.Triplet;
import fr.crnan.videso3d.layers.TrajectoriesLayer;
/**
 * IHM de configuration des filtres de couleurs pour les chevelus
 * @author Bruno Spyckerelle
 * @version 0.2
 */
public class TrajectoriesColorsDialog extends JPanel {

	/**
	 * Liste des paramètres
	 */
	private List<FiltrePanel> results = new LinkedList<FiltrePanel>();
		
	private JPanel container = new JPanel();
		
	public TrajectoriesColorsDialog(List<Triplet<String, String, Color>> param){
		this.setLayout(new BorderLayout());

		this.container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

		

		//remplissage de l'IHM en fonction des paramètres
		if(param == null){
			FiltrePanel filtre = new FiltrePanel();
			results.add(filtre);
			this.container.add(filtre);
		} else {
			for(Triplet<String, String, Color> t : param){
				FiltrePanel filtre = new FiltrePanel(t);
				results.add(filtre);
				this.container.add(filtre);
			}
		}

		
		
		//boutons
		JButton ok = new JButton("Valider");
		ok.setToolTipText("Appliquer les filtres");
		JButton cancel = new JButton("Supprimer");
		cancel.setToolTipText("Supprimer tous les filtres");
		
		ok.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				valuesChanged();
			}
		});
		cancel.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				container.removeAll();
				results.clear();
				FiltrePanel filtre = new FiltrePanel();
				results.add(filtre);
				container.add(filtre);
				container.validate();
				valuesChanged();
			}
		});
		JPanel boutons = new JPanel();
		boutons.setLayout(new BoxLayout(boutons, BoxLayout.X_AXIS));
		boutons.add(ok);
		boutons.add(cancel);
		
		//ajout d'un filtre
		JButton ajout = new JButton("Ajouter");
		ajout.setToolTipText("Ajouter un filtre supplémentaire");
		ajout.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				FiltrePanel filtre = new FiltrePanel(new Triplet<String, String, Color>());
				results.add(filtre);
				container.add(filtre);
				container.validate();
			}
		});
		boutons.add(Box.createHorizontalGlue());
		boutons.add(ajout);
		
		this.add(boutons, BorderLayout.SOUTH);
		
		this.add(container, BorderLayout.CENTER);
		
//		this.pack();
	}

	public void valuesChanged(){
		List<Triplet<String, String, Color>> params = new LinkedList<Triplet<String,String,Color>>();
		for(FiltrePanel p : results){
			params.add(p.getResult());
		}
		this.firePropertyChange("valuesChanged", null, params);
	}
	
	/**
	 * Panel de choix d'un filtre
	 * @author Bruno Spyckerelle
	 * @version 0.2
	 */
	private class FiltrePanel extends JPanel {
		
		private JComboBox champs;
		private JTextField regexp;
		private JButton changeColor;
		
		public FiltrePanel(){
			this(new Triplet<String, String, Color>());
		}
		
		public FiltrePanel(Triplet<String, String, Color> param){
			this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

			champs = new JComboBox();
			for(int i=1;i<6;i++){
				champs.addItem(TrajectoriesLayer.type2string(i));
			}
			if(param.getFirst() != null) champs.setSelectedItem(param.getFirst());
			this.add(champs);
			regexp = new JTextField(15);
			regexp.setToolTipText("<html><b>Exemples :</b><br/>" +
					" - tous les terrains français : LF.*<br/>" +
					" - LFPG ou LFPO : LFPG|LFPO<br />" +
					" - tous les terrains anglais ou irlandais : EG.*|EI.* ou E[GI].*</html>");
			if(param.getSecond() != null) regexp.setText(param.getSecond());
			this.add(regexp);

			changeColor = new JButton(new ImageIcon(getClass().getResource("/resources/fill-color.png"))); 

			changeColor.setBackground(param.getThird() == null ? Color.RED : param.getThird());
			this.add(changeColor);
			
			changeColor.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					changeColor.setBackground(JColorChooser.showDialog(null, "Couleur", changeColor.getBackground()));
				}
			});
		}
		
		public Triplet<String, String, Color> getResult(){
			Triplet<String, String, Color> result = new Triplet<String, String, Color>(champs.getSelectedItem().toString(), regexp.getText(), changeColor.getBackground());			
			return result;
		}
	}
	
}
