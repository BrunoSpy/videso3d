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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import fr.crnan.videso3d.DatasManager;
import fr.crnan.videso3d.databases.DatabaseManager;
import fr.crnan.videso3d.databases.DatabaseManager.Type;
/**
 * Panel de résultats des liaisons privilégiées
 * @author Adrien Vidal
 * @version 0.1.1
 */
public class LiaisonPanel extends ResultPanel implements ActionListener{

	private JTextArea t = new JTextArea(15,40);
	private JButton prec = new JButton("< Précédente ");
	private JButton suiv = new JButton(" Suivante >");
	private JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
	private String titleTab = "LP";
	private File f;
	
	private int searchNum;
	
	public LiaisonPanel(String searchNum){
		titleTab += " "+searchNum;
		if(DatasManager.getController(Type.STPV) == null){
			this.add(new JLabel("Pas de base STPV configurée"), BorderLayout.CENTER);
			return;
		}
		try {
			f = new File(DatabaseManager.getCurrentName(Type.STPV)+"_files","CODE");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		this.searchNum = Integer.parseInt(searchNum);
		this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		t.setEditable(false);
		t.setFocusable(true);
		t.setEnabled(false);
		t.setAlignmentX(Component.CENTER_ALIGNMENT);
		this.add(t);
		bottom.setAlignmentX(JPanel.CENTER_ALIGNMENT);
		bottom.setLayout(new BorderLayout(5,10));
		JPanel buttons = new JPanel();
		buttons.add(prec);
		buttons.add(suiv);
		bottom.add(buttons, BorderLayout.NORTH);
		this.add(bottom);		
		prec.addActionListener(this);
		suiv.addActionListener(this);
		displayLP();
	}

	@Override
	public void setContext(ContextPanel context) {
	}
	
	public void displayLP(){
		if(searchNum>0){
			try{
				FileReader fr = new FileReader(f);
				BufferedReader in = new BufferedReader(fr);
				int numLiaisonEncours = 0;
				String line = "";
				while(in.ready() && numLiaisonEncours<searchNum){
					line = in.readLine();
					if(line.startsWith("CODE 30 "))
						numLiaisonEncours+=1;
				}
				if(numLiaisonEncours==searchNum){
					line = line.replaceFirst("0", "0 ");
					String lp = line+"\n";
					boolean finLiaison = false;
					while(in.ready() && !finLiaison){
						line = in.readLine();
						if(line.startsWith("CODE 30S")  || line.startsWith("CODE 31S")){
							lp+=line+"\n";
						}
						else if(line.startsWith("CODE 31")){
							line = line.replaceFirst("1", "1 ");
							lp+=line+"\n";
						}else
							finLiaison = true;
					}
					t.setEnabled(true);
					t.setText(lp);
					t.setMaximumSize(new Dimension(500,200));
				}else
					error("Il n'y a pas autant de liaisons privilégiées.");
				
				fr.close();
				in.close();
			}catch(IOException e){
				e.printStackTrace();
			}
		}else{
			error("Veuillez entrer un nombre strictement positif.");
			
		}
	}
	
	private void error(String msg){
		t.setEnabled(false);
		t.setText(msg);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource()==prec){
			searchNum--;
			displayLP();
		}else if(e.getSource()==suiv){
			searchNum++;
			displayLP();
		}
		String oldTitle = this.titleTab;
		this.titleTab = "Liaison "+searchNum;
		firePropertyChange(ResultPanel.TITLE_TAB_NAME, oldTitle, this.titleTab);
	}

	@Override
	public String getTitleTab() {
		return this.titleTab;
	}
}
