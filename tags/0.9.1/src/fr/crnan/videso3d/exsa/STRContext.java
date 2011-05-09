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

package fr.crnan.videso3d.exsa;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;

import org.jdesktop.swingx.JXTaskPane;

import fr.crnan.videso3d.Context;
import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.ihm.components.VXTable;
/**
 * 
 * @author Bruno Spyckerelle
 * @author Adrien Vidal
 * @version 0.1
 */
public class STRContext extends Context {

	@Override
	public List<JXTaskPane> getTaskPanes(int type, String name) {
		if(type == STRController.MOSAIQUE_VVF){
			return showVVFInfos(name);
		}
		return null;
	}

	private List<JXTaskPane> showVVFInfos(String name){
		String vvfName = name.split("\\s+")[1];
		JXTaskPane infos = new JXTaskPane();
		infos.setTitle("Suites de codes associées au VVF");
		ArrayList<String> debut = new ArrayList<String>();
		ArrayList<String> fin = new ArrayList<String>();
		ArrayList<String> espaces = new ArrayList<String>();
		try {
			PreparedStatement st = DatabaseManager.prepareStatement(DatabaseManager.Type.EXSA, "select * from centscodf where vvf = ?");
			st.setString(1, vvfName);
			ResultSet rs = st.executeQuery();
			while(rs.next()){
				debut.add(rs.getString(3));
				fin.add(rs.getString(4));
				espaces.add(rs.getString(5));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		DefaultTableModel model = new DefaultTableModel(){
			@Override
			public boolean isCellEditable(int row, int column){
				return false;
			}
		};
		model.addColumn("Début", debut.toArray());
		model.addColumn("Fin", fin.toArray());
		model.addColumn("Espaces de visualisation", espaces.toArray());
		VXTable table = new VXTable(model);

		table.setFillsViewportHeight(true);
		JScrollPane jsp = new JScrollPane(table);
		jsp.setBorder(null);
		infos.add(jsp);
		ArrayList<JXTaskPane> list = new ArrayList<JXTaskPane>();
		list.add(infos);
		return list;		
	}
	

}
