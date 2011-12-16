package fr.crnan.videso3d.ihm.components;

import javax.swing.JTextField;
import javax.swing.plaf.basic.BasicComboBoxEditor;

public class JUpperCaseComboBoxEditor extends BasicComboBoxEditor {

	/**
     * Creates the internal editor component. Override this to provide
     * a custom implementation.
     *
     * @return Un JUpperCaseTextField pour tout afficher en majuscule dans la combobox.
     */
	@Override
    protected JTextField createEditorComponent() {
        JTextField editor = new JUpperCaseTextField();
        editor.setColumns(9);
        return editor;
    }

}
