package fr.crnan.videso3d.ihm.components;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.table.TableModel;

import org.jdesktop.swingx.JXTable;
/**
 * {@link JXTable} with csv export of the content accessible via the columncontrol
 * @author Bruno Spyckerelle
 * @version 0.1.0
 */
public class VXTable extends JXTable {

	private class ExportAction extends AbstractAction{

		private TableModel model;
		
		public ExportAction(TableModel model){
			super();
			this.model = model;
			this.putValue(Action.NAME, "Exporter les données dans un fichier CSV");
		}
		
		@Override
		public void actionPerformed(ActionEvent arg0) {

			VFileChooser fileChooser = new VFileChooser();
			if(fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION){
				final String file = fileChooser.getSelectedFile().getAbsolutePath();
				if(!(new File(file).exists()) || 
						(new File(file).exists() &&
								JOptionPane.showConfirmDialog(null, "Le fichier existe déjà.\n\nSouhaitez-vous réellement l'écraser ?",
										"Confirmer la suppression du fichier précédent",
										JOptionPane.OK_CANCEL_OPTION,
										JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION)) {


					final javax.swing.ProgressMonitor progress = new javax.swing.ProgressMonitor(null, "Sauvegarde des trajectoires sélectionnées", "",
							0, model.getRowCount()-1);
					progress.setMillisToDecideToPopup(0);
					progress.setMillisToPopup(0);
					new SwingWorker<Integer, Integer>() {

						@Override
						protected Integer doInBackground()throws Exception {
							if(model.getColumnCount()>0){
								final PrintWriter writer = new PrintWriter(file);
								String firstRow = model.getColumnName(0);
								for(int col=1;col<model.getColumnCount();col++){
									firstRow += ","+model.getColumnName(col);
								}
								writer.println(firstRow);
								for(int row = 0; row<model.getRowCount();row++){
									if(progress.isCanceled()){
										writer.flush();
										writer.close();
										File f = new File(file);
										if(f.exists())
											f.delete();
										return null;
									}
									progress.setProgress(row);
									progress.setNote(row+" lignes sur "+progress.getMaximum());
									String rowString = model.getValueAt(row, 0).toString();
									for(int col =1;col<model.getColumnCount();col++){
										rowString += ","+model.getValueAt(row, col);
									}
									writer.println(rowString);
								}
								writer.close();
							}
							return null;
						}

					}.execute();

					progress.close();

				}
			}
		}

	}

	public VXTable(TableModel model){
		super(model);
		this.setColumnControlVisible(true);

		List<Action> actions = new ArrayList<Action>();
		actions.add(new ExportAction(model));

		ColumnControl control = new ColumnControl(this);
		control.addActions(actions);

		this.setColumnControl(control);
		
	}

}
