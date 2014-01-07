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

package fr.crnan.videso3d.databases.exsa;

import java.text.ParseException;

/**
 * Contains CENT_CENTR datas
 * @author Bruno Spyckerelle
 * @version 0.2
 */
public class CentCentr {
		/**
		 * Nom du centre
		 */
		private String name;
		/**
		 * Numéro du système logique
		 */
		private Integer sl;
		/**
		 * Type de centre
		 */
		private String typeCentre;
		/**
		 * Identification du STR (SIC)
		 */
		private Integer sic;
		/**
		 * Niveau plafond MSAW
		 */
		private Integer nivMsaw;
		/**
		 * Indicateur RVSM
		 */
		private String rvsm;
		/**
		 * Niveau plancher RSVM
		 */
		private Integer nivPlancherRvsm;
		/**
		 * Niveau plafond RVSM
		 */
		private Integer nivPlafondRvsm;
		/**
		 * Type de données
		 */
		private String typeDonnees;
		/**
		 * Version des données ADP
		 */
		private String versionADP = "";
		
		//Constructor
		
		
		public CentCentr(String line, Boolean formated) throws ParseException {
			if(!formated){
				//suppression du ; en fin de ligne
				line = line.substring(0, line.length() - 1);
			}
			String[] word = line.split(formated ? "\\s+" : ",");
			int i = formated ? 0 : 1;
			if (word[0].equals(formated ? "CENT_CENTR" : "CENT.CENTR")){
				this.setName(word[1+i]);
				this.setSl(word[2+i]);
				this.setTypeCentre(word[3+i]);
				this.setSic(word[4+i]);
				this.setNivMsaw(word[5+i]);
				this.setRvsm(word[6+i]);
				this.setNivPlancherRvsm(word[7+i]);
				this.setNivPlafondRvsm(word[8+i]);
				this.setTypeDonnees(word[9+i]);
				if(word.length>10+i){//paramètre spécifique CRNAN
					this.setVersionADP(word[10+i]);
				}
			} else {
				throw new ParseException("CENT_CENTR Parse Error at " + line, 0);
			}
		}
		//Getters and Setters
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public Integer getSl() {
			return sl;
		}
		public void setSl(String sl){
			this.setSl(new Integer(sl));
		}
		public void setSl(Integer sl) {
			this.sl = sl;
		}
		public String getTypeCentre() {
			return typeCentre;
		}
		public void setTypeCentre(String typeCentre) {
			this.typeCentre = typeCentre;
		}
		public Integer getSic() {
			return sic;
		}
		public void setSic(String sic) {
			this.setSic(new Integer(sic));
		}
		public void setSic(Integer sic) {
			this.sic = sic;
		}
		public Integer getNivMsaw() {
			return nivMsaw;
		}
		private void setNivMsaw(String nivMsaw) {
			this.setNivMsaw(new Integer(nivMsaw));
		}
		public void setNivMsaw(Integer nivMsaw) {
			this.nivMsaw = nivMsaw;
		}
		public String getRvsm() {
			return rvsm;
		}
		public void setRvsm(String rvsm) {
			this.rvsm = rvsm;
		}
		public Integer getNivPlancherRvsm() {
			return nivPlancherRvsm;
		}
		public void setNivPlancherRvsm(String nivPlancherRvsm) {
			this.setNivPlancherRvsm(new Integer(nivPlancherRvsm));
		}
		public void setNivPlancherRvsm(Integer nivPlancherRvsm) {
			this.nivPlancherRvsm = nivPlancherRvsm;
		}
		public Integer getNivPlafondRvsm() {
			return nivPlafondRvsm;
		}
		public void setNivPlafondRvsm(String nivPlafondRvsm) {
			this.setNivPlafondRvsm(new Integer(nivPlafondRvsm));
		}
		public void setNivPlafondRvsm(Integer nivPlafondRvsm) {
			this.nivPlafondRvsm = nivPlafondRvsm;
		}
		public String getTypeDonnees() {
			return typeDonnees;
		}
		public void setTypeDonnees(String typeDonnees) {
			this.typeDonnees = typeDonnees;
		}
		public String getVersionADP() {
			return versionADP;
		}
		public void setVersionADP(String versionADP) {
			this.versionADP = versionADP;
		}
		
}
