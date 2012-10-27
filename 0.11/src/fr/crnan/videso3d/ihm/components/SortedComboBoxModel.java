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

package fr.crnan.videso3d.ihm.components;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;


/**
 *  Custom model to make sure the items are stored in a sorted order.
 *  The default is to sort in the natural order of the item, but a
 *  Comparator can be used to customize the sort order.
 *
 *  The data is initially sorted before the model is created. Any updates
 *  to the model will cause the items to be inserted in sorted order.
 */
public class SortedComboBoxModel extends DefaultComboBoxModel {

	private Comparator<Object> comparator;

	/*
	 *  Static method is required to make sure the data is in sorted order
	 *  before it is added to the model
	 */
	protected static Vector<Object> sortVector(Vector<Object> items, Comparator<Object> comparator)	{
		Collections.sort(items, comparator);
		return items;
	}

	/*
	 *  Static method is required to make sure the data is in sorted order
	 *  before it is added to the model
	 */
	protected static Object[] sortArray(Object[] items, Comparator<Object> comparator)	{
		Arrays.sort(items, comparator);
		return items;
	}

	/**
	 *  Create an empty model that will use the natural sort order of the item
	 */
	public SortedComboBoxModel()	{
		super();
	}

	/**
	 *  Create an empty model that will use the specified Comparator
	 */
	public SortedComboBoxModel(Comparator comparator)	{
		super();
		this.comparator = comparator;
	}

	/**
	 *	Create a model with data and use the nature sort order of the items
	 */
	public SortedComboBoxModel(Object[] items)	{
		super( sortArray(items, null) );
	}

	/**
	 *  Create a model with data and use the specified Comparator
	 */
	public SortedComboBoxModel(Object[] items, Comparator<Object> comparator)	{
		super( sortArray(items, comparator) );
		this.comparator = comparator;
	}

	/**
	 *	Create a model with data and use the nature sort order of the items
	 */
	public SortedComboBoxModel(Vector<Object> items)	{
		this( items, null );
	}

	/**
	 *  Create a model with data and use the specified Comparator
	 */

	public SortedComboBoxModel(Vector<Object> items, Comparator<Object> comparator)	{
		super( sortVector(items, comparator) );
		this.comparator = comparator;
	}

	@Override
	public void addElement(Object element)	{
		insertElementAt(element, 0);
	}

	@Override
	public void insertElementAt(Object element, int index)	{
		int size = getSize();

		//  Determine where to insert element to keep model in sorted order

		for (index = 0; index < size; index++)
		{
			if (comparator != null)
			{
				Object o = getElementAt( index );

				if (comparator.compare(o, element) > 0)
					break;
			}
			else
			{
				Comparable<Object> c = (Comparable<Object>)getElementAt( index );

				if (c.compareTo(element) > 0)
					break;
			}
		}

		super.insertElementAt(element, index);
	}
}

