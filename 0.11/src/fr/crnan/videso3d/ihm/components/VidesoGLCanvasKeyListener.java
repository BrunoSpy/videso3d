package fr.crnan.videso3d.ihm.components;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import fr.crnan.videso3d.VidesoGLCanvas;
/**
 * Handles Keyboards Shortcuts
 * @author Bruno Spyckerelle
 * @version 0.1.0
 */
public class VidesoGLCanvasKeyListener implements KeyListener, ClipboardOwner {

	private VidesoGLCanvas wwd;

	public VidesoGLCanvasKeyListener(VidesoGLCanvas wwd){
		this.wwd = wwd;
	}
	
	@Override
	public void lostOwnership(Clipboard arg0, Transferable arg1) {}

	@Override
	public void keyPressed(KeyEvent e) {
		if(e.isControlDown() && e.getKeyCode() == KeyEvent.VK_C){//CTRL+C
			wwd.copySelectedObjectsToClipboard();
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

}
