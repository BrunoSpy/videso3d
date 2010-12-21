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

package fr.crnan.videso3d.render;

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

import com.sun.opengl.util.j2d.TextRenderer;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Frustum;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.GeographicText;
import gov.nasa.worldwind.render.GeographicTextRenderer;
import gov.nasa.worldwind.render.OrderedRenderable;
import gov.nasa.worldwind.terrain.SectorGeometryList;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.OGLTextRenderer;
import gov.nasa.worldwind.util.WWMath;
/**
 * Draw Geographic Text AMSL instead of AGL
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class AMSLGeographicTextRenderer extends GeographicTextRenderer {

	private TextRenderer lastTextRenderer = null;
	private final GLU glu = new GLU();

	private static final Font DEFAULT_FONT = Font.decode("Arial-PLAIN-12");
	private static final Color DEFAULT_COLOR = Color.white;

	// Distance scaling and fading
	private boolean isDistanceScaling = false;
	private double lookAtDistance = 0;

	private boolean hasJOGLv111Bug = false;

	public AMSLGeographicTextRenderer()
	{
	}


	public void render(DrawContext dc, Iterable<GeographicText> text)
	{
		this.drawMany(dc, text);
	}

	public void render(DrawContext dc, GeographicText text, Vec4 textPoint)
	{
		if (!isTextValid(text, false))
			return;

		this.drawOne(dc, text, textPoint);
	}

	private void drawMany(DrawContext dc, Iterable<GeographicText> textIterable)
	{
		if (dc == null)
		{
			String msg = Logging.getMessage("nullValue.DrawContextIsNull");
			Logging.logger().fine(msg);
			throw new IllegalArgumentException(msg);
		}
		if (textIterable == null)
		{
			String msg = Logging.getMessage("nullValue.IterableIsNull");
			Logging.logger().fine(msg);
			throw new IllegalArgumentException(msg);
		}

		if (dc.getVisibleSector() == null)
			return;

		SectorGeometryList geos = dc.getSurfaceGeometry();
		if (geos == null)
			return;

		Iterator<GeographicText> iterator = textIterable.iterator();
		if (!iterator.hasNext())
			return;

		Frustum frustumInModelCoords = dc.getView().getFrustumInModelCoordinates();
		double horizon = dc.getView().computeHorizonDistance();

		while (iterator.hasNext())
		{
			GeographicText text = iterator.next();
			if (!isTextValid(text, true))
				continue;

			if (!text.isVisible())
				continue;

			Angle lat = text.getPosition().getLatitude();
			Angle lon = text.getPosition().getLongitude();

			if (!dc.getVisibleSector().contains(lat, lon))
				continue;

			//Vec4 textPoint = geos.getSurfacePoint(lat, lon,
			Vec4 textPoint = dc.getGlobe().computePointFromPosition(lat, lon,
					text.getPosition().getElevation() * dc.getVerticalExaggeration());
			if (textPoint == null)
				continue;

			double eyeDistance = dc.getView().getEyePoint().distanceTo3(textPoint);
			if (eyeDistance > horizon)
				continue;

			if (!frustumInModelCoords.contains(textPoint))
				continue;

			dc.addOrderedRenderable(new OrderedText(text, textPoint, eyeDistance));
		}
	}

	private void drawOne(DrawContext dc, GeographicText text, Vec4 textPoint)
	{
		if (dc == null)
		{
			String msg = Logging.getMessage("nullValue.DrawContextIsNull");
			Logging.logger().fine(msg);
			throw new IllegalArgumentException(msg);
		}
		if (dc.getView() == null)
		{
			String msg = Logging.getMessage("nullValue.ViewIsNull");
			Logging.logger().fine(msg);
			throw new IllegalArgumentException(msg);
		}

		if (dc.getVisibleSector() == null)
			return;

		SectorGeometryList geos = dc.getSurfaceGeometry();
		if (geos == null)
			return;

		if (!text.isVisible())
			return;

		if (textPoint == null)
		{
			if (text.getPosition() == null)
				return;

			Angle lat = text.getPosition().getLatitude();
			Angle lon = text.getPosition().getLongitude();

			if (!dc.getVisibleSector().contains(lat, lon))
				return;

			//textPoint = geos.getSurfacePoint(lat, lon,
			textPoint = dc.getGlobe().computePointFromPosition(lat, lon,
				text.getPosition().getElevation() * dc.getVerticalExaggeration());
			if (textPoint == null)
				return;
		}

		double horizon = dc.getView().computeHorizonDistance();
		double eyeDistance = dc.getView().getEyePoint().distanceTo3(textPoint);
		if (eyeDistance > horizon)
			return;

		if (!dc.getView().getFrustumInModelCoordinates().contains(textPoint))
			return;

		dc.addOrderedRenderable(new OrderedText(text, textPoint, eyeDistance));
	}

	protected class OrderedText implements OrderedRenderable, Comparable<OrderedText>
	{
		GeographicText text;
		Vec4 point;
		double eyeDistance;

		OrderedText(GeographicText text, Vec4 point, double eyeDistance)
		{
			this.text = text;
			this.point = point;
			this.eyeDistance = eyeDistance;
		}

		// When overlapping text are culled we want to sort them front to back by priority.
		public int compareTo(OrderedText t)
		{
			if (t.text.getPriority() - this.text.getPriority() == 0)
			{
				return (int) (this.eyeDistance - t.eyeDistance);
			}
			else
				return (int) (t.text.getPriority() - this.text.getPriority());
		}

		public double getDistanceFromEye()
		{
			return this.eyeDistance;
		}

		private GeographicTextRenderer getRenderer()
		{
			return AMSLGeographicTextRenderer.this;
		}

		public void render(DrawContext dc)
		{
			AMSLGeographicTextRenderer.this.beginRendering(dc);
			try
			{
				if (isCullTextEnabled())
				{
					ArrayList<OrderedText> textList = new ArrayList<OrderedText>();
					textList.add(this);

					// Draw as many as we can in a batch to save ogl state switching.
					Object nextItem = dc.getOrderedRenderables().peek();
					while (nextItem != null && nextItem instanceof OrderedText)
					{
						OrderedText ot = (OrderedText) nextItem;
						if (ot.getRenderer() != AMSLGeographicTextRenderer.this)
							break;

						textList.add(ot);
						dc.getOrderedRenderables().poll(); // take it off the queue
						nextItem = dc.getOrderedRenderables().peek();
					}

					Collections.sort(textList); // sort for rendering priority then front to back

					ArrayList<Rectangle2D> textBounds = new ArrayList<Rectangle2D>();
					for (OrderedText ot : textList)
					{
						double[] scaleAndOpacity = AMSLGeographicTextRenderer.this.computeDistanceScaleAndOpacity(dc, ot);
						Rectangle2D newBounds = AMSLGeographicTextRenderer.this.computeTextBounds(dc, ot, scaleAndOpacity[0]);
						if (newBounds == null)
							continue;

						boolean overlap = false;
						newBounds = AMSLGeographicTextRenderer.this.computeExpandedBounds(newBounds, getCullTextMargin());
						for (Rectangle2D rect : textBounds)
						{
							if (rect.intersects(newBounds))
								overlap = true;
						}

						if (!overlap)
						{
							textBounds.add(newBounds);
							AMSLGeographicTextRenderer.this.drawText(dc, ot, scaleAndOpacity[0], scaleAndOpacity[1]);
						}
					}
				}
				else //just draw each label
				{
					double[] scaleAndOpacity = AMSLGeographicTextRenderer.this.computeDistanceScaleAndOpacity(dc, this);
					AMSLGeographicTextRenderer.this.drawText(dc, this, scaleAndOpacity[0], scaleAndOpacity[1]);
					// Draw as many as we can in a batch to save ogl state switching.
					Object nextItem = dc.getOrderedRenderables().peek();
					while (nextItem != null && nextItem instanceof OrderedText)
					{
						OrderedText ot = (OrderedText) nextItem;
						if (ot.getRenderer() != AMSLGeographicTextRenderer.this)
							break;

						scaleAndOpacity = AMSLGeographicTextRenderer.this.computeDistanceScaleAndOpacity(dc, ot);
						AMSLGeographicTextRenderer.this.drawText(dc, ot, scaleAndOpacity[0], scaleAndOpacity[1]);
						dc.getOrderedRenderables().poll(); // take it off the queue
						nextItem = dc.getOrderedRenderables().peek();
					}
				}
			}
			catch (WWRuntimeException e)
			{
				Logging.logger().log(java.util.logging.Level.SEVERE, "generic.ExceptionWhileRenderingText", e);
			}
			catch (Exception e)
			{
				Logging.logger().log(java.util.logging.Level.SEVERE, "generic.ExceptionWhileRenderingText", e);
			}
			finally
			{
				AMSLGeographicTextRenderer.this.endRendering(dc);
			}
		}

		public void pick(DrawContext dc, java.awt.Point pickPoint)
		{
		}
	}

	protected Rectangle2D computeTextBounds(DrawContext dc, OrderedText uText, double scale) throws Exception
	{
		GeographicText geographicText = uText.text;

		final CharSequence charSequence = geographicText.getText();
		if (charSequence == null)
			return null;

		final Vec4 screenPoint = dc.getView().project(uText.point);
		if (screenPoint == null)
			return null;

		Font font = geographicText.getFont();
		if (font == null)
			font = DEFAULT_FONT;

		try
		{
			TextRenderer textRenderer = OGLTextRenderer.getOrCreateTextRenderer(dc.getTextRendererCache(), font);
			if (textRenderer != this.lastTextRenderer)
			{
				if (this.lastTextRenderer != null)
					this.lastTextRenderer.end3DRendering();
				textRenderer.begin3DRendering();
				this.lastTextRenderer = textRenderer;
			}

			Rectangle2D textBound = textRenderer.getBounds(charSequence);
			double x = screenPoint.x - textBound.getWidth() / 2d;
			Rectangle2D bounds = new Rectangle2D.Float();
			bounds.setRect(x, screenPoint.y, textBound.getWidth(), textBound.getHeight());

			return computeScaledBounds(bounds, scale);
		}
		catch (Exception e)
		{
			handleTextRendererExceptions(e);
			return null;
		}
	}

	@SuppressWarnings({"UnusedDeclaration"})
	protected double[] computeDistanceScaleAndOpacity(DrawContext dc, OrderedText ot)
	{
		if (!this.isDistanceScaling)
			return new double[] {1, 1};

		// Determine scale and opacity factors based on distance from eye vs the distance to the look at point.
		double lookAtDistance = this.lookAtDistance;
		double eyeDistance = ot.getDistanceFromEye();
		double distanceFactor = Math.sqrt(lookAtDistance / eyeDistance);
		double scale = WWMath.clamp(distanceFactor,
				this.getDistanceMinScale(), this.getDistanceMaxScale());
		double opacity = WWMath.clamp(distanceFactor,
				this.getDistanceMinOpacity(), 1);

		return new double[] {scale, opacity};
	}

	protected double computeLookAtDistance(DrawContext dc)
	{
		View view = dc.getView();

		// Get point in the middle of the screen
		// TODO: Get a point on the surface rather then the geoid
		Position groundPos = view.computePositionFromScreenPoint(
				view.getViewport().getCenterX(), view.getViewport().getCenterY());

		// Update look at distance if center point found
		if (groundPos != null)
		{
			// Compute distance from eye to the position in the middle of the screen
			this.lookAtDistance = view.getEyePoint().distanceTo3(dc.getGlobe().computePointFromPosition(groundPos));
		}

		return this.lookAtDistance;
	}


	protected void beginRendering(DrawContext dc)
	{
		GL gl = dc.getGL();
		int attribBits =
			GL.GL_ENABLE_BIT // for enable/disable changes
			| GL.GL_COLOR_BUFFER_BIT // for alpha test func and ref, and blend
			| GL.GL_CURRENT_BIT      // for current color
			| GL.GL_DEPTH_BUFFER_BIT // for depth test, depth func, and depth mask
			| GL.GL_TRANSFORM_BIT    // for modelview and perspective
			| GL.GL_VIEWPORT_BIT;    // for depth range
		gl.glPushAttrib(attribBits);

		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		glu.gluOrtho2D(0, dc.getView().getViewport().width, 0, dc.getView().getViewport().height);
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		gl.glMatrixMode(GL.GL_TEXTURE);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		// Set model view as current matrix mode
		gl.glMatrixMode(GL.GL_MODELVIEW);


		// Enable the depth test but don't write to the depth buffer.
		gl.glEnable(GL.GL_DEPTH_TEST);
		gl.glDepthMask(false);

		// Suppress polygon culling.
		gl.glDisable(GL.GL_CULL_FACE);

		// Suppress any fully transparent image pixels
		gl.glEnable(GL.GL_ALPHA_TEST);
		gl.glAlphaFunc(GL.GL_GREATER, 0.001f);

		// Cache distance scaling values
		this.isDistanceScaling = this.getDistanceMinScale() != 1 || this.getDistanceMaxScale() != 1
		|| this.getDistanceMinOpacity() != 1;
		this.computeLookAtDistance(dc);
	}

	protected void endRendering(DrawContext dc)
	{
		if (this.lastTextRenderer != null)
		{
			this.lastTextRenderer.end3DRendering();
			this.lastTextRenderer = null;
		}

		GL gl = dc.getGL();

		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glPopMatrix();
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glPopMatrix();
		gl.glMatrixMode(GL.GL_TEXTURE);
		gl.glPopMatrix();

		gl.glPopAttrib();
	}

	protected Vec4 drawText(DrawContext dc, OrderedText uText, double scale, double opacity) throws Exception
	{
		if (uText.point == null)
		{
			String msg = Logging.getMessage("nullValue.PointIsNull");
			Logging.logger().fine(msg);
			return null;
		}

		GeographicText geographicText = uText.text;
		GL gl = dc.getGL();

		final CharSequence charSequence = geographicText.getText();
		if (charSequence == null)
			return null;

		final Vec4 screenPoint = dc.getView().project(uText.point);
		if (screenPoint == null)
			return null;

		Font font = geographicText.getFont();
		if (font == null)
			font = DEFAULT_FONT;

		try
		{
			TextRenderer textRenderer = OGLTextRenderer.getOrCreateTextRenderer(dc.getTextRendererCache(), font);
			if (textRenderer != this.lastTextRenderer)
			{
				if (this.lastTextRenderer != null)
					this.lastTextRenderer.end3DRendering();
				textRenderer.begin3DRendering();
				this.lastTextRenderer = textRenderer;
			}

			this.setDepthFunc(dc, uText, screenPoint);

			Rectangle2D textBounds = textRenderer.getBounds(
					charSequence);//note:may already be calculated during culling
					textBounds = this.computeScaledBounds(textBounds, scale);
			Point.Float drawPoint = computeDrawPoint(dc, textBounds, screenPoint);

			if (drawPoint != null)
			{
				if (scale != 1d)
				{
					gl.glScaled(scale, scale, 1d);
					drawPoint.setLocation(drawPoint.x / (float)scale, drawPoint.y / (float)scale);
				}

				Color color = geographicText.getColor();
				if (color == null)
					color = DEFAULT_COLOR;
				color = this.applyOpacity(color, opacity);

				Color background = geographicText.getBackgroundColor();
				if (background != null)
				{
					background = this.applyOpacity(background, opacity);
					textRenderer.setColor(background);
					if (this.getEffect().equals(AVKey.TEXT_EFFECT_SHADOW))
					{
						textRenderer.draw3D(charSequence, drawPoint.x + 1, drawPoint.y - 1, 0, 1);
					}
					else if (this.getEffect().equals(AVKey.TEXT_EFFECT_OUTLINE))
					{
						textRenderer.draw3D(charSequence, drawPoint.x + 1, drawPoint.y - 1, 0, 1);
						textRenderer.draw3D(charSequence, drawPoint.x + 1, drawPoint.y + 1, 0, 1);
						textRenderer.draw3D(charSequence, drawPoint.x - 1, drawPoint.y - 1, 0, 1);
						textRenderer.draw3D(charSequence, drawPoint.x - 1, drawPoint.y + 1, 0, 1);
					}
				}

				textRenderer.setColor(color);
				textRenderer.draw3D(charSequence, drawPoint.x, drawPoint.y, 0, 1);
				textRenderer.flush();

				if (scale != 1d)
					gl.glLoadIdentity();
			}
		}
		catch (Exception e)
		{
			handleTextRendererExceptions(e);
		}

		return screenPoint;
	}

	private void handleTextRendererExceptions(Exception e) throws Exception
	{
		if (e instanceof IOException)
		{
			if (!this.hasJOGLv111Bug)
			{
				// This is likely a known JOGL 1.1.1 bug - see AMZN-287 or 343
				// Log once and then ignore.
				Logging.logger().log(java.util.logging.Level.SEVERE, "generic.ExceptionWhileRenderingText", e);
				this.hasJOGLv111Bug = true;
			}
		}
		else
		{
			throw e;
		}
	}

	/**
	 * Computes the final draw point for the given rectangle lower left corner and target screen point. If the returned
	 * point is <code>null</code> the text will not be drawn.
	 *
	 * @param dc          the current {@link DrawContext}
	 * @param rect        the text rectangle to draw.
	 * @param screenPoint the projected screen point the text relates to.
	 *
	 * @return the final draw point for the given rectangle lower left corner or <code>null</code>.
	 */
	@SuppressWarnings({"UnusedDeclaration"})
	protected Point.Float computeDrawPoint(DrawContext dc, Rectangle2D rect, Vec4 screenPoint)
	{
		return new Point.Float((float) (screenPoint.x - rect.getWidth() / 2d), (float) (screenPoint.y));
	}

	@SuppressWarnings({"UnusedDeclaration"})
	protected void setDepthFunc(DrawContext dc, OrderedText uText, Vec4 screenPoint)
	{
		GL gl = dc.getGL();

		//if (uText.text.isAlwaysOnTop())
		//{
		//    gl.glDepthFunc(GL.GL_ALWAYS);
		//    return;
		//}

		Position eyePos = dc.getView().getEyePosition();
		if (eyePos == null)
		{
			gl.glDepthFunc(GL.GL_ALWAYS);
			return;
		}

		double altitude = eyePos.getElevation();
		if (altitude < (dc.getGlobe().getMaxElevation() * dc.getVerticalExaggeration()))
		{
			double depth = screenPoint.z - (8d * 0.00048875809d);
			depth = depth < 0d ? 0d : (depth > 1d ? 1d : depth);
			gl.glDepthFunc(GL.GL_LESS);
			gl.glDepthRange(depth, depth);
		}
		//else if (screenPoint.z >= 1d)
			//{
			//    gl.glDepthFunc(GL.GL_EQUAL);
			//    gl.glDepthRange(1d, 1d);
		//}
		else
		{
			gl.glDepthFunc(GL.GL_ALWAYS);
		}
	}

}