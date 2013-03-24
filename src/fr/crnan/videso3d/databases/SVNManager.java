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

package fr.crnan.videso3d.databases;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;


import org.tmatesoft.svn.core.SVNCancelException;
import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.SVNPropertyValue;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.io.ISVNEditor;
import org.tmatesoft.svn.core.io.ISVNReporter;
import org.tmatesoft.svn.core.io.ISVNReporterBaton;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.io.diff.SVNDiffWindow;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import fr.crnan.videso3d.ProgressSupport;
import fr.crnan.videso3d.ihm.ProgressMonitor;
import gov.nasa.worldwind.util.Logging;
/**
 * 
 * @author Adrien Vidal
 * @author Bruno Spyckerelle
 * @version 0.1.0
 */
public class SVNManager extends ProgressSupport {
	
	private SVNRepository repository=null;
	private String url;
	private String name;
	private String pass;
		
	private ProgressMonitor monitor;
	
	public SVNManager(){
		super();
	};
	
	public SVNManager(String repository){
		super();
		this.initialize(repository);
	}
	
	public void initialize(String repository){
		//TODO proxy ??
		DAVRepositoryFactory.setup();

		String[] repoParams = repository.split(";");
		this.url = repoParams[0];
		
		if(repoParams.length > 2) { //login/mdp positionnés
			this.name = repoParams[1];
			this.pass = repoParams[2];
		} 
		
		try {
			this.repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded( this.url ));
			if(this.name != null && this.pass !=null){ //pas de connexion anonyme
				ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager( this.name , this.pass );
				this.repository.setAuthenticationManager( authManager );
			}
		} catch (SVNException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Sets a link to a monitor to allow cancellation of the task
	 * @param monitor
	 */
	public void setMonitor(ProgressMonitor monitor){
		this.monitor = monitor;
	}
	
	@SuppressWarnings("unchecked")
	public File getDatabase(String svnPath, int rev) {
		try {
			this.repository.setLocation(SVNURL.parseURIEncoded(this.url+svnPath), false);
			if(rev==-1){
				rev = (int) this.repository.getLatestRevision();	
			}
			File tempData = new File("temp"+"_"+rev+svnPath.replace("/", "_"));
			if(!tempData.exists())
				tempData.mkdir();

			SVNProperties fileProperties = new SVNProperties( );
			
			Collection<SVNDirEntry> fichiers = this.repository.getDir(svnPath, rev, fileProperties,(Collection<?>)null );

			fireTaskStarts(fichiers.size());
			int i = 0;
			
			for(SVNDirEntry dirEntry : fichiers){
				String nomFichier = dirEntry.getRelativePath();
				fireTaskInfo(nomFichier);
				File fichier = new File(tempData.getPath()+"/"+nomFichier);
				if(!fichier.exists())
					fichier.createNewFile();
				ByteArrayOutputStream baos = new ByteArrayOutputStream( );
				this.repository.getFile(nomFichier, rev , fileProperties , baos );
				FileOutputStream fos = new FileOutputStream(fichier);
				baos.writeTo(fos);
				baos.close();
				fos.close();
				fireTaskProgress(++i);
			}
			//téléchargement terminé
			fireTaskEnds();
			return tempData;

		} catch (SVNException ex1) {
			ex1.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public ArrayList<String> getBranchesDirectories() {
		ArrayList<String> branchesDir = new ArrayList<String>();
		Collection<?> entries = null;
		try {
			entries = this.repository.getDir( "/branches", -1 , null , (Collection<?>) null );
		} catch (SVNException e) {
			e.printStackTrace();
		}
		Iterator<?> iterator = entries.iterator( );
		while ( iterator.hasNext( ) ) {
			SVNDirEntry entry = ( SVNDirEntry ) iterator.next( );
			branchesDir.add(entry.getRelativePath());
		}
		return branchesDir;
	}



	public ArrayList<String> getTagsDirectories() {
		ArrayList<String> tagsDir = new ArrayList<String>();
		Collection<?> entries = null;
		try {
			entries = this.repository.getDir( "/tags", -1 , null , (Collection<?>) null );
		} catch (SVNException e) {
			e.printStackTrace();
		}
		Iterator<?> iterator = entries.iterator( );
		while ( iterator.hasNext( ) ) {
			SVNDirEntry entry = ( SVNDirEntry ) iterator.next( );
			tagsDir.add(entry.getRelativePath());
		}
		return tagsDir;
	}

	public DefaultTreeModel listEntries(){
		fireTaskStarts(100);
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("");
		DefaultTreeModel treeModel = new DefaultTreeModel(root);
		
		fillTree(treeModel, root, "");
		if(!monitor.isCanceled())
			fireTaskEnds(); //don't send TASK_ENDS if cancelled
		return treeModel;
    }
	
	/**
	 * Populates a tree with the content of a repo
	 * @param treeModel
	 * @param root
	 * @param path
	 */
	private void fillTree(DefaultTreeModel treeModel, DefaultMutableTreeNode root, final String path){
		try {
			final long rev = this.repository.getLatestRevision();
			
			//get the entire tree
			ISVNReporterBaton reporter = new ISVNReporterBaton() {
				
				@Override
				public void report(ISVNReporter reporter) throws SVNException {
					
					reporter.setPath(path, null, rev, SVNDepth.INFINITY, 
                            true/*we are empty, take us all like in checkout*/);
                 
                    reporter.finishReport();
				}
			};
			
			PropFetchingEditor editor = new PropFetchingEditor(root);
			editor.addPropertyChangeListener(ProgressSupport.TASK_INFO, new PropertyChangeListener() {
				
				@Override
				public void propertyChange(PropertyChangeEvent arg0) {
					SVNManager.this.fireTaskInfo((String) arg0.getNewValue());
				}
			});
			//run an update-like request which never receives any real file deltas
			this.repository.status(rev, null, SVNDepth.INFINITY, reporter, editor);
			
			//now populate treemodel
			treeModel.setRoot(root);
			
		} catch(SVNCancelException e){ 
			Logging.logger().info("SVN Status cancelled");
		} catch (SVNException e) {

			e.printStackTrace();
		}
		
	}
	
	/**
	 * Editor which only stores directories into the required {@link DefaultMutableTreeNode} root
	 * @author  TMate Software Ltd.
	 *
	 */
	private class PropFetchingEditor extends ProgressSupport implements ISVNEditor {

		/**
		 * Map containing path as key and {@link DefaultMutableTreeNode} as value
		 */
		private Map<String, DefaultMutableTreeNode> dirs = new HashMap<String, DefaultMutableTreeNode>();
				
		public PropFetchingEditor(DefaultMutableTreeNode root){
			super();
			dirs.put("/", root);
		}
		
		public void abortEdit() throws SVNException {
		}

		public void absentDir(String path) throws SVNException {
		}

		public void absentFile(String path) throws SVNException {
		}

		public void addFile(String path, String copyFromPath, long copyFromRevision) throws SVNException {
		}

		public SVNCommitInfo closeEdit() throws SVNException {
			return null;
		}

		public void closeFile(String path, String textChecksum) throws SVNException {
		}

		public void deleteEntry(String path, long revision) throws SVNException {
		}

		public void openFile(String path, long revision) throws SVNException {
		}

		public void targetRevision(long revision) throws SVNException {
		}

		public void applyTextDelta(String path, String baseChecksum) throws SVNException {
		}

		public OutputStream textDeltaChunk(String path, SVNDiffWindow diffWindow) throws SVNException {
			return null;
		}

		public void textDeltaEnd(String path) throws SVNException {
		}

		public void addDir(String path, String copyFromPath, long copyFromRevision) throws SVNException {
			if(monitor.isCanceled()){
				throw new SVNCancelException();
			}
			String absoluteDirPath = "/" + path;
			int index = absoluteDirPath.lastIndexOf("/");
			if(index != -1){
				String parent = index == 0 ? "/" : absoluteDirPath.substring(0, index);
				DefaultMutableTreeNode parentNode = dirs.get(parent);
				DefaultMutableTreeNode child = new DefaultMutableTreeNode(absoluteDirPath.substring(index+1));
				parentNode.add(child);
				dirs.put(absoluteDirPath, child);
			}			
			fireTaskInfo(absoluteDirPath);

		}

		public void changeDirProperty(String name, SVNPropertyValue value) throws SVNException {

		}

		public void changeFileProperty(String path, String propertyName, SVNPropertyValue propertyValue) throws SVNException {

		}

		public void closeDir() throws SVNException {
			
		}

		public void openDir(String path, long revision) throws SVNException {
			
		}

		public void openRoot(long revision) throws SVNException {
			
		}

	}
	
}
