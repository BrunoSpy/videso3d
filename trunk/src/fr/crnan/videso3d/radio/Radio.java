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

package fr.crnan.videso3d.radio;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.FileParser;
import fr.crnan.videso3d.DatabaseManager.Type;


public class Radio extends FileParser {
		
	/**
	 * Nombre de fichiers gérés
	 */
	private int numberFiles = 1;

	/**
	 * Version des fichiers Radio
	 */
	private String name="RadioCovBase";
	private String path;
	/**
	 * Connection à la base de données
	 */
	private Connection conn;	
	
	public Radio(){
		super();
	}
		
	public Radio(String path) {
		super(path);
		this.path=path;
	}


	@Override
	public Integer doInBackground() {
		
		try {
			//création de la connection à la base de données
			this.conn = DatabaseManager.selectDB(Type.RadioCov, this.name);
			this.conn.setAutoCommit(false); //fixes performance issue
			if(!DatabaseManager.databaseExists(this.name)){
				//création de la structure de la base de données
				DatabaseManager.createRadioCov(this.name,this.path);
				//parsing des fichiers et stockage en base
				///this.getFromFiles();				
				//this.setProgress(12);
				
				try {
					this.conn.commit();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				//this.setProgress(this.numberFiles());
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
				DatabaseManager.deleteDatabase(this.name, Type.RadioCov);
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
	
	
}
