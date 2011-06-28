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
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import fr.crnan.videso3d.DatabaseManager;

public class LiaisonPanel extends ResultPanel implements ActionListener{

	private JTextArea t = new JTextArea(15,40);
	private JButton prec = new JButton("< Précédente ");
	private JButton suiv = new JButton(" Suivante >");
	private JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
	private JTabbedPane tabPane;
		
	private File f;
	
	private int searchNum;
	
	public LiaisonPanel(String searchNum, JTabbedPane tabPane){
		try {
			f = new File(DatabaseManager.getCurrentStpvPath()+"\\CODE");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		this.tabPane = tabPane;
		this.searchNum = Integer.parseInt(searchNum);
		this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		t.setEditable(false);
		t.setFocusable(false);
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
					int nbLines = 1;
					boolean finLiaison = false;
					while(in.ready() && !finLiaison){
						line = in.readLine();
						if(line.startsWith("CODE 30S")  || line.startsWith("CODE 31S")){
							lp+=line+"\n";
							nbLines++;
						}
						else if(line.startsWith("CODE 31")){
							line = line.replaceFirst("1", "1 ");
							lp+=line+"\n";
							nbLines++;
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
		tabPane.setTitleAt(tabPane.getSelectedIndex(), "Liaison "+searchNum);
	}
}
