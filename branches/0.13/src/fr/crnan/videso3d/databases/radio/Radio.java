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

package fr.crnan.videso3d.databases.radio;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import fr.crnan.videso3d.DatasManager;
import fr.crnan.videso3d.DatasManager.Type;
import fr.crnan.videso3d.FileParser;
import fr.crnan.videso3d.databases.DatabaseManager;


public class Radio extends FileParser {		
	/**
	 * Nombre de fichiers gérés
	 */
	private int numberFiles = 0;	
	
	/**
	 * Version des fichiers Radio
	 */
	private String name="radio";
	private String path;
	private Document document=null;
	
	/**
	 * Connexion à la base de données
	 */

	private Connection conn;		
	public Radio(){
		super();
	}
		
	public Radio(String path) {
		super(path);
		this.path=path;
		
		SAXBuilder sxb = new SAXBuilder();
		try {
			document = sxb.build(new File(path));
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
		
		// nombre de fichiers à gérer
		
	//	RadioDataManager radioDataManager = new RadioDataManager(path);
	//	radioDataManager.loadData();	
	}

	public  void insertRadio() throws SQLException {
	PreparedStatement insert = this.conn.prepareStatement("insert into radio (id, databaseId, path) " +
	"values (?, ?, ?)");
	// id,  path
	// insert.setInt(1, new Integer(line.substring(9, 13).trim()) /2);
	insert.executeUpdate();
	}
		
		
	@Override
	public Integer doInBackground() {
		
		try {
			//création de la connexion à la base de données
			this.conn = DatabaseManager.selectDB(DatasManager.Type.RadioCov, this.name);
			this.conn.setAutoCommit(false); //fixes performance issue
		
			if(!DatabaseManager.databaseExists(DatasManager.Type.RadioCov, this.name)){
				
				System.out.println("(Radio.java) / La base de données n'existe pas" +"");
				
				// création de la structure de la base de données
				 DatabaseManager.createRadioCov(this.name,this.path);
				 DatabaseManager.insertRadioCov(this.name, this.path);
				 // parsing des fichiers et stockage en base
				// /this.getFromFiles();				
				// this.setProgress(12);
				
				try {this.conn.commit();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				//this.setProgress(this.numberFiles());
			}
			else {		
				// ajout d'une ligne dans la table radio
				// Ajout d'une ligne dans la table databases.
				System.out.println("La base de données existe");
				DatabaseManager.insertRadioCov(this.name, this.path);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e){
			e.printStackTrace();
		}
		return 0; 
	}

	@Override
	public void done(){
		if(this.isCancelled()){//si le parsing a été annulé, on fait le ménage
			try {
				DatabaseManager.deleteDatabase(this.name, DatasManager.Type.RadioCov);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		firePropertyChange("done", false, true);
	}
	
	@Override
	public int numberFiles() {
		return this.numberFiles;
	}
	
	@Override
	public void getFromFiles() {		
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public DatasManager.Type getType() {
		return DatasManager.Type.RadioCov;
	}
	
	@Override
	public List<String> getRelevantFileNames() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
