package fr.crnan.videso3d.databases;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;


import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import fr.crnan.videso3d.DatasManager;
import fr.crnan.videso3d.ihm.DatabaseManagerUI;

public class SVNManager {

	
	private DatasManager.Type dataType;
	
	private SVNRepository repository=null;
	private String url;
	private String name;
	private String pass;
	private DatabaseManagerUI dbmUI;
	
	private static SVNManager instance = new SVNManager();
	
	private SVNManager(){
		super();
	};
	
	public static void initialize(String repository, DatabaseManagerUI dbmUI){
		DAVRepositoryFactory.setup();
		instance.dbmUI=dbmUI;

		String[] repoParams = repository.split(";");
		instance.dataType = DatabaseManager.stringToType(repoParams[0]);
		instance.url = repoParams[1];
		instance.name = repoParams[2];
		instance.pass = repoParams[3];
		
		try {
			instance.repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded( instance.url ));
			ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager( instance.name , instance.pass );
			instance.repository.setAuthenticationManager( authManager );
		} catch (SVNException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void getDatabase(String svnPath, int rev) {
		try {
			instance.repository.setLocation(SVNURL.parseURIEncoded(instance.url+svnPath), false);
			if(rev==-1){
				rev = (int) instance.repository.getLatestRevision();	
			}
			File tempData = new File("temp"+instance.dataType+"_"+rev+svnPath.replace("/", "_"));
			if(!tempData.exists())
				tempData.mkdir();

			SVNProperties fileProperties = new SVNProperties( );
			
			Collection<SVNDirEntry> fichiers = instance.repository.getDir(svnPath, rev, fileProperties,(Collection<?>)null );

			for(SVNDirEntry dirEntry : fichiers){
				String nomFichier = dirEntry.getRelativePath();
				File fichier = new File(tempData.getPath()+"/"+nomFichier);
				if(!fichier.exists())
					fichier.createNewFile();
				ByteArrayOutputStream baos = new ByteArrayOutputStream( );
				instance.repository.getFile(nomFichier, rev , fileProperties , baos );
				baos.writeTo(new FileOutputStream(fichier));
			}
			instance.dbmUI.processSelectedFiles(new File[]{tempData}, true, instance.dataType);
		} catch (SVNException ex1) {
			ex1.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}



	public static ArrayList<String> getBranchesDirectories() {
		ArrayList<String> branchesDir = new ArrayList<String>();
		Collection<?> entries = null;
		try {
			entries = instance.repository.getDir( "/branches", -1 , null , (Collection<?>) null );
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



	public static ArrayList<String> getTagsDirectories() {
		ArrayList<String> tagsDir = new ArrayList<String>();
		Collection<?> entries = null;
		try {
			entries = instance.repository.getDir( "/tags", -1 , null , (Collection<?>) null );
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

	public static DefaultTreeModel listEntries(){
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("");
		DefaultTreeModel treeModel = new DefaultTreeModel(root);
		fillTree(treeModel, root, "");
		return treeModel;
    }
	
	private static void fillTree(DefaultTreeModel treeModel, DefaultMutableTreeNode root, String path){
		Collection<?> entries = null;
		try {
			entries = instance.repository.getDir( path, -1 , null , (Collection<?>) null );
		} catch (SVNException e) {
			e.printStackTrace();
		}
		Iterator<?> iterator = entries.iterator( );
		int i = 0;
		while ( iterator.hasNext( ) ) {
			SVNDirEntry entry = ( SVNDirEntry ) iterator.next( );
			if ( entry.getKind() == SVNNodeKind.DIR ) {
				DefaultMutableTreeNode child = new DefaultMutableTreeNode(entry.getName());
				treeModel.insertNodeInto(child, root, i);
				i++;
				fillTree(treeModel, child, path+"/"+entry.getName());
			}
		}
	}
	
}
