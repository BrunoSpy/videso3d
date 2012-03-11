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
package fr.crnan.videso3d.formats.plns;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.swing.ProgressMonitorInputStream;

/**
 * 
 * @author Bruno Spyckerelle
 * @version 0.1.0
 */
public class PLNSExtractor {

	private final int buf = 1024;
	private File trm;
	private File bal;
	private File cen;
	private File sec;
	private File typ;
	
	private Connection database;
	
	public PLNSExtractor(File[] files, Connection database){
		this.database = database;
		for(File f : files){
			//extract files
			File tempRep = new File("temp_"+f.getName()); 
			tempRep.mkdir();
			trm = new File(tempRep+"/trm");
			bal = new File(tempRep+"/bal");
			cen = new File(tempRep+"/cen");
			sec = new File(tempRep+"/sec");
			typ = new File(tempRep+"/typ");
			
			try {

				ProgressMonitorInputStream in = new ProgressMonitorInputStream(null, 
						"Extraction du fichier PLNS ...",
						new FileInputStream(f));
				System.out.println("début");
				extractFiles(in);
				System.out.println("connect");
				
				connectDatabase(database);
				System.out.println("read");
				readFiles();
				System.out.println("fin");

			}  catch (FileNotFoundException e1) {
				e1.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			}

//			for(File f : tempRep.listFiles()){
//			f.delete();
//		
//			tempRep.delete();
		}
	}
	
	private int ord(String s){

		byte[] bytes;
		try {
			bytes = s.getBytes("ISO-8859-1");
			return bytes[0] & 0xff;
		}catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return -1;
		}

	}
	
	private int ord(byte b){
		return b & 0xff;
	}
	
	private void connectDatabase(Connection database){
		//connection par défaut
		Statement st = null;
		try {
			database.setAutoCommit(false);
			//Création de la structure si besoin
			st = database.createStatement();
			//Méthode comme une autre pour vérifier que la structure existe ... lance une exception si ce n'est pas le cas
			String query = "select * from plns where 1";
			st.executeQuery(query);
			st.close();
			
		} catch(SQLException e){
			try {
				String create = "create table plns (id integer primary key autoincrement, " +
				"date varchar(20), " +
				"heure_dep varchar(20), " +
				"indicatif varchar(12), " +
				"code int, "+
				"code_prev int, "+
				"adep varchar(4)," +
				"adest varchar(4))";
				st.executeUpdate(create);
				create = "create table secteurs (id integer primary key autoincrement, " +
						"idpln int, " +
						"secteur varchar(2))";
				st.executeUpdate(create);
				create = "create table sls (id integer primary key autoincrement, " +
						"idpln int, " +
						"sl varchar(4))";
				st.executeUpdate(create);
				st.close();
			} catch (SQLException e2) {
				e2.printStackTrace();
			}
		} 
		
	}
	
	private void readFiles() throws IOException, SQLException{
		Statement st = this.database.createStatement();
		System.out.println("test0");
		boolean stop = false;
		FileInputStream trmR = new FileInputStream(trm);
		RandomAccessFile balR = new RandomAccessFile(bal, "r");
		RandomAccessFile cenR = new RandomAccessFile(cen, "r");
		RandomAccessFile secR = new RandomAccessFile(sec, "r");
		RandomAccessFile typR = new RandomAccessFile(typ, "r");
		
		int longBuf = 3072*2;
		byte[] pln = new byte[longBuf];
		int enTete = 6;
		
		while(!stop){
			if(trmR.read(pln, 0, longBuf) == longBuf){
			    String plnString = new String(pln, "ISO-8859-1");
				
				int nbJour = ord(pln[1])*256 + ord(pln[2]);
				int annee = ord(pln[0]);

				Calendar calendar = new GregorianCalendar();
				calendar.set(20*10+annee, 1, 1);
				calendar.add(Calendar.DAY_OF_MONTH, nbJour-1);
				SimpleDateFormat formatDate = new SimpleDateFormat("dd/MM/yy");
				String date = formatDate.format(calendar.getTime());
				
				//récupération des index des champs
				int indexRensGen = ord(pln[6])*256 + ord(pln[7]);
				int indexChampRoute = ord(pln[8])*256 + ord(pln[9]);
				int indexChampCCR = ord(pln[10])*256 + ord(pln[11]);
				int indexChampSecteurExt = ord(pln[12])*256 + ord(pln[13]);
				int indexChampSecteur = ord(pln[14])*256 + ord(pln[15]);
				int indexChampCautra3 = ord(pln[16])*256 + ord(pln[17]);
				int indexChampCOOR = ord(pln[18])*256 + ord(pln[19]);
				int indexChampGestINT = ord(pln[20])*256 + ord(pln[21]);
				
				// Renseignements généraux
                String indicatif = plnString.substring(9+enTete+indexRensGen, 9+enTete+indexRensGen+7);
                int numeroCAUTRA = ord(pln[7+enTete+indexRensGen])*256 + ord(pln[8+enTete+indexRensGen]);
                int vitesse = ord(pln[17+enTete+indexRensGen])*256 + ord(pln[18+enTete+indexRensGen]);

                // On récupère le rang du type de l'avion dans le fichier type
                // Et ensuite, on extrait le type de l'avion du fichier type

                int rangTypeAvion = ord(pln[19+enTete+indexRensGen])*256+ord(pln[20+enTete+indexRensGen]);
                typR.seek(5+(rangTypeAvion-1)*10-1);
                byte[] typeAvion = new byte[4];
                typR.read(typeAvion, 0, 4);
                String typeAvionString = new String(typeAvion, "ISO-8859-1");
                
                String adep = plnString.substring(21+enTete+indexRensGen, 21+enTete+indexRensGen+4);
                String adest = plnString.substring(25+enTete+indexRensGen, 25+enTete+indexRensGen+4);
                
                int rfl = ord(pln[29+enTete+indexRensGen])*256+ord(pln[30+enTete+indexRensGen]);
                
                int heureDepMin = ord(pln[31+enTete+indexRensGen])*256 + ord(pln[32+enTete+indexRensGen]);
                if(heureDepMin > 24*60)
                	heureDepMin = heureDepMin -24*60;
                String heureDep = (heureDepMin/60) +":"+(heureDepMin%60);
                
                int pfl = ord(pln[3+enTete+indexChampCOOR])*256 + ord(pln[4+enTete+indexChampCOOR]);
                
                int nbBalises = ord(pln[45+enTete+indexRensGen]);
                
                int nbSecteur = ord(pln[46+enTete+indexRensGen]);
                
                int nbCCR = ord(pln[48+enTete+indexRensGen]);
                
                // Récupération des index des champs gestion interne
                int indexRensGenInt = ord(pln[enTete+indexChampGestINT-1])*256 + ord(pln[enTete+indexChampGestINT]);
                int indexChampRouteInt = ord(pln[enTete+indexChampGestINT+9])*256 + ord(pln[enTete+indexChampGestINT+10]);
                int indexChampCCRInt = ord(pln[enTete+indexChampGestINT+11])*256 + ord(pln[enTete+indexChampGestINT+12]);
                int indexChampSectInt = ord(pln[enTete+indexChampGestINT+15])*256 + ord(pln[enTete+indexChampGestINT+16]);
                int indexChampRadInt = ord(pln[enTete+indexChampGestINT+5])*256 + ord(pln[enTete+indexChampGestINT+6]);

                int rangSL = ord(pln[enTete+indexRensGenInt-1]);
                int rangPPT = ord(pln[enTete+indexRensGenInt]);//premier point RI
                int rangDPT = ord(pln[enTete+indexRensGenInt+1]);//dernier point RI

                //code mode A précédent
                int codePrev1 = ord(pln[enTete+indexChampRadInt-1]);
                int codePrev2 = ord(pln[enTete+indexChampRadInt]);
                int codePrev3 = ord(pln[enTete+indexChampRadInt+1]);
                int codePrev4 = ord(pln[enTete+indexChampRadInt+2]);
                int codePrev = codePrev1*1000+codePrev2*100+codePrev3*10+codePrev4;
                
                //code mode A
                int code1 = ord(pln[enTete+indexChampRadInt+3]);
                int code2 = ord(pln[enTete+indexChampRadInt+4]);
                int code3 = ord(pln[enTete+indexChampRadInt+5]);
                int code4 = ord(pln[enTete+indexChampRadInt+6]);
                int code = code1*1000+code2*100+code3*10+code4;
                
                //TODO extraction de la route
                
                //liste des SL
                List<String> sl = new ArrayList<String>();
                for(int i = 0;i<nbCCR;i++){
                	int rangCCR = ord(pln[enTete+indexChampCCR+4*i-1]);
                	cenR.seek(rangCCR*6);
                	byte[] ccr = new byte[4];
                	cenR.read(ccr, 0, 4);
                	sl.add(new String(ccr, "ISO-8859-1"));
                }
                
                List<String> secteurs = new ArrayList<String>();
                for(int i = 0;i<nbSecteur;i++){
                	int rangSecteur = ord(pln[enTete+indexChampSecteur+6*i-1])*256 + ord(pln[enTete+indexChampSecteur+6*i]);
                	secR.seek(rangSecteur-1);
                	byte[] secteur = new byte[2];
                	secR.read(secteur, 0, 2);
                	
                	int rangPPTSecteur = ord(pln[enTete+indexChampSecteur+1+6*i]);
                	int rangDPTSecteur = ord(pln[enTete+indexChampSecteur+2+6*i]);
                	
                	if((rangPPTSecteur >= rangPPT && rangPPTSecteur <= rangDPT) || (rangDPTSecteur >= rangPPT && rangDPTSecteur <= rangDPT)) {
                		secteurs.add(new String(secteur, "ISO-8859-1"));
                	}
                }
                
                ResultSet rs = st.executeQuery("select count(*) from plns where " +
                														"date='"+date+"' and " +
                														"heure_dep='"+heureDep+"' and " +
                														"adep='"+adep+"'");
                System.out.println("test01");
                if(rs.next() && rs.getInt(1) == 0){
                	System.out.println("test1");
                	PreparedStatement insert = this.database.prepareStatement("insert into plns (date, heure_dep, indicatif, code, code_prev, adep, adest) " +
                			"values (?, ?, ?, ?, ?, ?, ?)");
                	insert.setString(1, date);
                	insert.setString(2, heureDep);
                	insert.setString(3, indicatif);
                	insert.setInt(4, code);
                	insert.setInt(5, codePrev);
                	insert.setString(6, adep);
                	insert.setString(7, adest);
                	insert.executeUpdate();
                	
                	int id = insert.getGeneratedKeys().getInt(1);
                	insert.close();
                	//secteurs
                	insert = this.database.prepareStatement("insert into secteurs (idpln, secteur) values (?, ?)");
                	insert.setInt(1, id);
                	for(String s : secteurs){
                		insert.setString(2, s);
                		insert.addBatch();
                	}
                	insert.executeBatch();
                	insert.close();
                	
                	insert = this.database.prepareStatement("insert into sls (idpln, sl) values (?, ?)");
                	insert.setInt(1, id);
                	for(String s : sl){
                		insert.setString(2, s);
                		insert.addBatch();
                	}
                	insert.executeBatch();
                	insert.close();
                }
                
			} else {
				stop = true;
			}
		}
		st.close();
		this.database.commit();
	}
	
	private void extractFiles(ProgressMonitorInputStream reader) throws IOException{
		byte[] buffer = new byte[buf];
		int j = 0;
		boolean stop = false;
		boolean debutFichier = false;
		
		BufferedWriter trmW = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(trm), "ISO-8859-1"));
		BufferedWriter balW = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(bal), "ISO-8859-1"));
		BufferedWriter cenW = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(cen), "ISO-8859-1"));
		BufferedWriter secW = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(sec), "ISO-8859-1"));
		BufferedWriter typW = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(typ), "ISO-8859-1"));
		
		while(reader.read(buffer, 0, buf) != -1){
			
			String bufferString = new String(buffer, "ISO-8859-1");
			
			if(bufferString.length() != buf)
				stop = true;
			
			String finDeFic = bufferString.substring(0, 6);
			String annee = bufferString.substring(1, 2);
			String jourOctet1 = bufferString.substring(2,3);
			String jourOctet2 = bufferString.substring(3, 4);
			
			StringBuffer fin = new StringBuffer(6);
			int c;
			for(int i=0; i<6;i++){
				c = finDeFic.charAt(i);
				fin.append(Integer.toHexString(c));
			}
			
			if(fin.toString().equals("3b11d02e")){
				int pointeur = 0;
				
				while(pointeur < buf - 24){
					int natureBloc = ord(bufferString.substring(20+pointeur, 20+pointeur+1));
					if(natureBloc == 12){
						int typeBloc = ord(bufferString.substring(21+pointeur, 21+pointeur+1));
						int longBloc = ord(bufferString.substring(23+pointeur, 23+pointeur+1))*2;
						String bloc = bufferString.substring(26+pointeur, 26+pointeur+longBloc-2);
						int numBloc = ord(bufferString.substring(25+pointeur, 25+pointeur+1));
						int dernierBloc = Character.codePointAt(bufferString.subSequence(24+pointeur, 24+pointeur+1), 0);
						pointeur += longBloc + 4;
						
						if(typeBloc == 7){
							//Balises
							balW.write(bloc);
						} else if(typeBloc == 1) {
							if(numBloc == 1){
								trmW.write(annee+jourOctet1+jourOctet2+bloc.substring(3));
								debutFichier = true;
							} else {
								if(debutFichier){
									trmW.write(bloc);
								}
							}
						} else if(typeBloc == 5){
							cenW.write(bloc);
						} else if(typeBloc == 8){
							secW.write(bloc);
						} else if(typeBloc == 9){
							typW.write(bloc);
						}
					} else {
						pointeur = 1024;
					}
				}
			} else {
				stop = true;
			}
		}
		balW.close();
		trmW.close();
		cenW.close();
		secW.close();
		typW.close();
	}
	
}
