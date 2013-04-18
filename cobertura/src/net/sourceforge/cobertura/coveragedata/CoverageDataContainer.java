/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2003 jcoverage ltd.
 * Copyright (C) 2005 Mark Doliner
 * Copyright (C) 2005 Jeremy Thomerson
 * Copyright (C) 2005 Mark Sinke
 *
 * Cobertura is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 *
 * Cobertura is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cobertura; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 */

package net.sourceforge.cobertura.coveragedata;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import net.sourceforge.cobertura.CoverageIgnore;

/**
 * <p>
 * Coverage data information is typically serialized to a file.
 * </p>
 */
@CoverageIgnore
public abstract class CoverageDataContainer
		implements CoverageData, Serializable
{

	private static final long serialVersionUID = 2;

	protected transient Lock lock;

	/**
	 * Each key is the name of a child, usually stored as a String or
	 * an Integer object.  Each value is information about the child,
	 * stored as an object that implements the CoverageData interface.
	 */
	Map<Object,CoverageData> children = new HashMap<Object,CoverageData>();

	public CoverageDataContainer()
	{
		initLock();
	}
	
	private void initLock()
	{
		lock = new ReentrantLock();
	}
	
	/**
	 * Determine if this CoverageDataContainer is equal to
	 * another one.  Subclasses should override this and
	 * make sure they implement the hashCode method.
	 *
	 * @param obj An object to test for equality.
	 * @return True if the objects are equal.
	 */
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if ((obj == null) || !(obj.getClass().equals(this.getClass())))
			return false;

		synchronizeState();
		CoverageDataContainer coverageDataContainer = (CoverageDataContainer)obj;
		coverageDataContainer.synchronizeState();
		lock.lock();
		try
		{
			return this.children.equals(coverageDataContainer.children);
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 * @return The average branch coverage rate for all children
	 *         in this container.
	 */
	public double getBranchCoverageRate()
	{
		synchronizeState();
		int number = 0;
		int numberCovered = 0;
		lock.lock();
		try
		{
			Iterator<CoverageData> iter = this.children.values().iterator();
			while (iter.hasNext())
			{
				CoverageData coverageContainer = (CoverageData)iter.next();
				number += coverageContainer.getNumberOfValidBranches();
				numberCovered += coverageContainer.getNumberOfCoveredBranches();
			}
		}
		finally
		{
			lock.unlock();
		}
		if (number == 0)
		{
			// no branches, therefore 100% branch coverage.
			return 1d;
		}
		return (double)numberCovered / number;
	}

	/**
	 * Get a child from this container with the specified
	 * key.
	 * @param name The key used to lookup the child in the
	 *        map.
	 * @return The child object, if found, or null if not found.
	 */
	public CoverageData getChild(String name)
	{
		lock.lock();
		try
		{
			return (CoverageData)this.children.get(name);
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 * @return The average line coverage rate for all children
	 *         in this container.  This number will be a decimal
	 *         between 0 and 1, inclusive.
	 */
	public double getLineCoverageRate()
	{
		synchronizeState();
		int number = 0;
		int numberCovered = 0;
		lock.lock();
		try
		{
			Iterator<CoverageData> iter = this.children.values().iterator();
			while (iter.hasNext())
			{
				CoverageData coverageContainer = (CoverageData)iter.next();
				number += coverageContainer.getNumberOfValidLines();
				numberCovered += coverageContainer.getNumberOfCoveredLines();
			}
		}
		finally
		{
			lock.unlock();
		}
		if (number == 0)
		{
			// no lines, therefore 100% line coverage.
			return 1d;
		}
		return (double)numberCovered / number;
	}

	/**
	 * @return The number of children in this container.
	 */
	public int getNumberOfChildren()
	{
		synchronizeState();
		lock.lock();
		try
		{
			return this.children.size();
		}
		finally
		{
			lock.unlock();
		}
	}

	public int getNumberOfCoveredBranches()
	{
		synchronizeState();
		int number = 0;
		lock.lock();
		try
		{
			Iterator<CoverageData> iter = this.children.values().iterator();
			while (iter.hasNext())
			{
				CoverageData coverageContainer = (CoverageData)iter.next();
				number += coverageContainer.getNumberOfCoveredBranches();
			}
		}
		finally
		{
			lock.unlock();
		}
		return number;
	}

	public int getNumberOfCoveredLines()
	{		
		synchronizeState();
		int number = 0;
		lock.lock();
		try
		{
			Iterator<CoverageData> iter = this.children.values().iterator();
			while (iter.hasNext())
			{
				CoverageData coverageContainer = (CoverageData)iter.next();
				number += coverageContainer.getNumberOfCoveredLines();
			}
		}
		finally
		{
			lock.unlock();
		}
		return number;
	}

	public int getNumberOfValidBranches()
	{
		synchronizeState();
		int number = 0;
		lock.lock();
		try
		{
			Iterator<CoverageData> iter = this.children.values().iterator();
			while (iter.hasNext())
			{
				CoverageData coverageContainer = (CoverageData)iter.next();
				number += coverageContainer.getNumberOfValidBranches();
			}
		}
		finally
		{
			lock.unlock();
		}
		return number;
	}

	public int getNumberOfValidLines()
	{
		synchronizeState();
		int number = 0;
		lock.lock();
		try
		{
			Iterator<CoverageData> iter = this.children.values().iterator();
			while (iter.hasNext())
			{
				CoverageData coverageContainer = (CoverageData)iter.next();
				number += coverageContainer.getNumberOfValidLines();
			}
		}
		finally
		{
			lock.unlock();
		}
		return number;
	}

	/**
	 * It is highly recommended that classes extending this
	 * class override this hashCode method and generate a more
	 * effective hash code.
	 */
	public int hashCode()
	{
		synchronizeState();
		lock.lock();
		try
		{
			return this.children.size();
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 * Merge two <code>CoverageDataContainer</code>s.
	 *
	 * @param coverageData The container to merge into this one.
	 */
	public void merge(CoverageData coverageData)
	{
		synchronizeState();
		CoverageDataContainer container = (CoverageDataContainer)coverageData;
		container.synchronizeState();
		getBothLocks(container);
		try
		{
			Iterator<Object> iter = container.children.keySet().iterator();
			while (iter.hasNext())
			{
				Object key = iter.next();
				CoverageData newChild = (CoverageData)container.children.get(key);
				CoverageData existingChild = (CoverageData)this.children.get(key);
				if (existingChild != null)
				{
					existingChild.merge(newChild);
				}
				else
				{
					// TODO: Shouldn't we be cloning newChild here?  I think so that
					//       would be better... but we would need to override the
					//       clone() method all over the place?
					this.children.put(key, newChild);
				}
			}
		}
		finally
		{
			lock.unlock();
			container.lock.unlock();
		}
	}

	protected void getBothLocks(CoverageDataContainer other) {
		/*
		 * To prevent deadlock, we need to get both locks or none at all.
		 * 
		 * When this method returns, the thread will have both locks.
		 * Make sure you unlock them!
		 */
		boolean myLock = false;
		boolean otherLock = false;
		while ((!myLock) || (!otherLock))
		{
			try
			{
				myLock = lock.tryLock();
				otherLock = other.lock.tryLock();
			}
			finally
			{
				if ((!myLock) || (!otherLock))
				{
					//could not obtain both locks - so unlock the one we got.
					if (myLock)
					{
						lock.unlock();
					}
					if (otherLock)
					{
						other.lock.unlock();
					}
					//do a yield so the other threads will get to work.
					Thread.yield();
				}
			}
		}
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();
		initLock();
	}
	
	public void synchronizeState(){
		
	}
}
