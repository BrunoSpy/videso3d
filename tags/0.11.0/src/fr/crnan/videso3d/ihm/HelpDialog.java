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

package fr.crnan.videso3d.ihm;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import fr.crnan.videso3d.Videso3D;
import fr.crnan.videso3d.ihm.components.TitledPanel;
/**
 * Dialog with authors names and help links.
 * @author Bruno Spyckerelle
 * @version 0.1.0
 */
public class HelpDialog extends JDialog {

	public HelpDialog(){
		this.setTitle("A propos ...");
		this.setModal(true);
		this.add(new TitledPanel("ViDeso 3D "+Videso3D.VERSION), BorderLayout.NORTH);
		JEditorPane text = new JEditorPane("text/html", "<p align=center><b>Auteurs</b><br />" +
				"Bruno Spyckerelle<br />" +
				"Adrien Vidal<br />" +
				"Mickael Papail<br />" +
				"<br />" +
				"<b>Liens</b><br />" +
		"<a href=\"http://code.google.com/p/videso3d/wiki/Home?tm=6\">Aide en ligne</a><br />" +
		"<a href=\"http://code.google.com/p/videso3d/issues/list\">Signaler un bug</a><br /></p>");
		text.setEditable(false);
		text.setOpaque(false);
		text.addHyperlinkListener(new HyperlinkListener() {

			@Override
			public void hyperlinkUpdate(HyperlinkEvent evt) {
				if(evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED && Desktop.isDesktopSupported()){
					final Desktop dt = Desktop.getDesktop();
					if ( dt.isSupported( Desktop.Action.BROWSE ) ){	
						try {
							dt.browse( evt.getURL().toURI() );
						} catch (IOException e) {
							e.printStackTrace();
						} catch (URISyntaxException e) {
							e.printStackTrace();
						}
					}
				}
			}
		});
		this.add(text);
		this.setPreferredSize(new Dimension(400, 240));
		this.pack();
		Toolkit tk = this.getToolkit();
		int x = (tk.getScreenSize().width - this.getWidth())/2;
		int y = (tk.getScreenSize().height - this.getHeight())/2;
		this.setLocation(x, y);
	}
	
}
