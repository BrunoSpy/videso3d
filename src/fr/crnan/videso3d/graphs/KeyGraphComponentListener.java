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
package fr.crnan.videso3d.graphs;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.Set;

import com.mxgraph.model.mxCell;
import com.mxgraph.view.mxGraph;
/**
 * Handles various actions through keyboard shortcuts :
 * <ul><li>CTRL+A : select all</li>
 * <li>CTRL+C : copy to clipboard</li>
 * </ul>
 * @author Bruno Spyckerelle
 * @version 0.1.0
 */
public class KeyGraphComponentListener implements KeyListener, ClipboardOwner {

	private mxGraph graph;
	
	public KeyGraphComponentListener(mxGraph graph){
		this.graph = graph;
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		if(e.isControlDown() && e.getKeyCode() == KeyEvent.VK_C){
			//Use a set in order to avoid copying same cell multiple times
			Set<CellContent> cells = new HashSet<CellContent>();
			Clipboard clipBoard = Toolkit.getDefaultToolkit().getSystemClipboard();
			String selection = new String();
			for(Object cell : graph.getSelectionCells()){
				if(cell instanceof mxCell){
					if(((mxCell) cell).getValue() instanceof CellContent){
						CellContent content = (CellContent) ((mxCell)cell).getValue();
						if(!cells.contains(content)){
							cells.add(content);
							selection += content.toFormattedString()+"\n";
						}
					} else { //try to get children content
						for(int i = 0;i<((mxCell)cell).getChildCount();i++){
							if(((mxCell)cell).getChildAt(i).getValue() instanceof CellContent){
								CellContent content = (CellContent)((mxCell)cell).getChildAt(i).getValue();
								if(!cells.contains(content)){
									cells.add(content);
									selection += content.toFormattedString()+"\n";
								}
							}
						}
					}
				}
			}
			clipBoard.setContents(new StringSelection(selection), this);
		} else if(e.isControlDown() && e.getKeyCode() == KeyEvent.VK_A){
			this.graph.getSelectionModel().clear();
			for(int i=0;i<this.graph.getModel().getChildCount(graph.getDefaultParent());i++){
				this.graph.getSelectionModel().addCell(this.graph.getModel().getChildAt(this.graph.getDefaultParent(), i));
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {}

	@Override
	public void keyTyped(KeyEvent e) {
		
	}

	@Override
	public void lostOwnership(Clipboard arg0, Transferable arg1) {}

}
