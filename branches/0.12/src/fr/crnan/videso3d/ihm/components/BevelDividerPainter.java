package fr.crnan.videso3d.ihm.components;

import java.awt.Color;
import java.awt.Graphics2D;

import javax.swing.JComponent;

import org.jdesktop.swingx.JXMultiSplitPane.DividerPainter;
import org.jdesktop.swingx.MultiSplitLayout.Divider;

public class BevelDividerPainter extends DividerPainter {
	private JComponent owner;

	public BevelDividerPainter( JComponent c )
	{
		owner = c;
	}

	public void doPaint(Graphics2D g, Divider divider, int width, int height)
	{
		Color c = owner.getBackground();
		g.setColor( c );
		g.fillRect(0, 0, width, height);

		int size = 1;
		if ( divider.isVertical()) {
			size = Math.max( size, ( width / 5 ) -1 );
			g.setColor( c.brighter());
			g.fillRect( 1, 0, size, height);
			g.setColor( c.darker());
			g.fillRect( width-size-1, 0, size, height);

			g.setColor( c.darker().darker());
			g.drawLine( 0, 0, 0, height);
			g.drawLine( width-1, 0, width-1, height);
		}
		else {
			size = Math.max( size, height / 5 );
			int middle = width/2;
			g.setColor( c.brighter());
	//		g.fillRect( middle-10, 1, 20, size );
			g.setColor( c.darker());
			g.fillRect( middle-8, height-size-1, 16, size );

			g.setColor( c.darker().darker());
	//		g.drawLine( middle-10, 0, middle+10, 0 );
	//		g.drawLine( middle-10, height-1, middle+10, height-1);
		}
	}
}