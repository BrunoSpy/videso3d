package fr.crnan.videso3d.ihm.components;

import java.awt.Dimension;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;


public class JUpperCaseComboBox extends JComboBox {

	public JUpperCaseComboBox(){
		super();
		this.setEditor(new JUpperCaseComboBoxEditor());
	}

	public JUpperCaseComboBox(DefaultComboBoxModel defaultComboBoxModel) {
		super(defaultComboBoxModel);
		this.setEditor(new JUpperCaseComboBoxEditor());
		this.setPreferredSize(new Dimension(111,24));

	}
}
