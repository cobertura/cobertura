/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2003 jcoverage ltd.
 * Copyright (C) 2005 Mark Doliner
 * Copyright (C) 2006 Jiri Mares
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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import net.sourceforge.cobertura.CoverageIgnore;

/**
 * <p>
 * ProjectData information is typically serialized to a file. An
 * instance of this class records coverage information for a single
 * class that has been instrumented.
 * </p>
 */

@CoverageIgnore
public class ClassData extends CoverageDataContainer
	implements Comparable<ClassData> {
	private static final long serialVersionUID = 5;


	/**
	 * Each key is a line number in this class, stored as an Integer object.
	 * Each value is information about the line, stored as a LineData object.
	 */
	private Map<Integer,LineData> branches = new HashMap<Integer,LineData>();

	private boolean containsInstrumentationInfo = false;

	private Set<String> methodNamesAndDescriptors = new HashSet<String>();

	private String name = null;

	private String sourceFileName = null;

	public ClassData() {}
	
	/**
	 * @param name In the format "net.sourceforge.cobertura.coveragedata.ClassData"
	 */
	public ClassData(String name)
	{
		if (name == null)
			throw new IllegalArgumentException(
				"Class name must be specified.");
		this.name = name;
	}

	public LineData addLine(int lineNumber, String methodName,
			String methodDescriptor)
	{
		lock.lock();
		try
		{
			LineData lineData = getLineData(lineNumber);
			if (lineData == null)
			{
				lineData = new LineData(lineNumber);
				// Each key is a line number in this class, stored as an Integer object.
				// Each value is information about the line, stored as a LineData object.
				children.put(new Integer(lineNumber), lineData);
			}
			lineData.setMethodNameAndDescriptor(methodName, methodDescriptor);
	      
			// methodName and methodDescriptor can be null when cobertura.ser with 
			// no line information was loaded (or was not loaded at all).
			if( methodName!=null && methodDescriptor!=null)
				methodNamesAndDescriptors.add(methodName + methodDescriptor);
			return lineData;
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 * This is required because we implement Comparable.
	 */
	public int compareTo(ClassData o)
	{
		if (!o.getClass().equals(ClassData.class))
			return Integer.MAX_VALUE;
		return this.name.compareTo(((ClassData)o).name);
	}

	public boolean containsInstrumentationInfo()
	{
		lock.lock();
		try
		{
			return this.containsInstrumentationInfo;
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 * Returns true if the given object is an instance of the
	 * ClassData class, and it contains the same data as this
	 * class.
	 */
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if ((obj == null) || !(obj.getClass().equals(this.getClass())))
			return false;

		ClassData classData = (ClassData)obj;
		getBothLocks(classData);
		try
		{
			return super.equals(obj)
				&& this.branches.equals(classData.branches)
				&& this.methodNamesAndDescriptors
					.equals(classData.methodNamesAndDescriptors)
				&& this.name.equals(classData.name)
				&& this.sourceFileName.equals(classData.sourceFileName);
		}
		finally
		{
			lock.unlock();
			classData.lock.unlock();
		}
	}

	public String getBaseName()
	{
		int lastDot = this.name.lastIndexOf('.');
		if (lastDot == -1)
		{
			return this.name;
		}
		return this.name.substring(lastDot + 1);
	}

	/**
	 * @return The branch coverage rate for a particular method.
	 */
	public double getBranchCoverageRate(String methodNameAndDescriptor)
	{
		int total = 0;
		int covered = 0;

		lock.lock();
		try
		{
			for (Iterator<LineData> iter = branches.values().iterator(); iter.hasNext();) {
				LineData next = (LineData) iter.next();
				if (methodNameAndDescriptor.equals(next.getMethodName() + next.getMethodDescriptor()))
				{
					total += next.getNumberOfValidBranches();
					covered += next.getNumberOfCoveredBranches();
				}
			}
			if (total == 0) return 1.0;
			return (double) covered / total;
		}
		finally
		{
			lock.unlock();
		}
	}

	public Collection<Integer> getBranches() 
	{
		lock.lock();
		try
		{
			return Collections.unmodifiableCollection(branches.keySet());
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 * @param lineNumber The source code line number.
	 * @return The coverage of the line
	 */
	public LineData getLineCoverage(int lineNumber) 
	{
		Integer lineObject = new Integer(lineNumber);
		lock.lock();
		try
		{
			if (!children.containsKey(lineObject)) 
			{
				return null;
			}
	
			return (LineData) children.get(lineObject);
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 * @return The line coverage rate for particular method
	 */
	public double getLineCoverageRate(String methodNameAndDescriptor) 
	{
		int total = 0;
		int hits = 0;

		lock.lock();
		try
		{
			Iterator<CoverageData> iter = children.values().iterator();
			while (iter.hasNext()) 
			{
				LineData next = (LineData) iter.next();
				if (methodNameAndDescriptor.equals(next.getMethodName() + next.getMethodDescriptor())) 
				{
					total++;
					if (next.getHits() > 0) {
						hits++;
					}
				}
			}
			if (total == 0) return 1d;
			return (double) hits / total;
		}
		finally
		{
			lock.unlock();
		}
	}

	public LineData getLineData(int lineNumber)
	{		
		lock.lock();
		try
		{
			return (LineData)children.get(Integer.valueOf(lineNumber));
		}
		finally
		{
			lock.unlock();
		}
	}

	public SortedSet<CoverageData> getLines()
	{
		lock.lock();
		try
		{
			return new TreeSet<CoverageData>(this.children.values());
		}
		finally
		{
			lock.unlock();
		}
	}

	public Collection<CoverageData> getLines(String methodNameAndDescriptor)
	{
		Collection<CoverageData> lines = new HashSet<CoverageData>();
		lock.lock();
		try
		{
			Iterator<CoverageData> iter = children.values().iterator();
			while (iter.hasNext())
			{
				LineData next = (LineData)iter.next();
				if (methodNameAndDescriptor.equals(next.getMethodName()
						+ next.getMethodDescriptor()))
				{
					lines.add(next);
				}
			}
			return lines;
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 * @return The method name and descriptor of each method found in the
	 *         class represented by this instrumentation.
	 */
	public Set<String> getMethodNamesAndDescriptors() 
	{
		lock.lock();
		try
		{
			return methodNamesAndDescriptors;
		}
		finally
		{
			lock.unlock();
		}
	}

	public String getName() 
	{
		return name;
	}

	/**
	 * @return The number of branches in this class.
	 */
	public int getNumberOfValidBranches() 
	{
		int number = 0;
		lock.lock();
		try
		{
			for (Iterator<LineData> i = branches.values().iterator(); 
				i.hasNext(); 
				number += ((LineData) i.next()).getNumberOfValidBranches())
				;
			return number;
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 * @see net.sourceforge.cobertura.coveragedata.CoverageData#getNumberOfCoveredBranches()
	 */
	public int getNumberOfCoveredBranches() 
	{
		int number = 0;
		lock.lock();
		try
		{
			for (Iterator<LineData> i = branches.values().iterator(); 
				i.hasNext(); 
				number += ((LineData) i.next()).getNumberOfCoveredBranches())
				;
			return number;
		}
		finally
		{
			lock.unlock();
		}
	}

	public String getPackageName()
	{
		int lastDot = this.name.lastIndexOf('.');
		if (lastDot == -1)
		{
			return "";
		}
		return this.name.substring(0, lastDot);
	}

	 /**
	 * Return the name of the file containing this class.  If this
	 * class' sourceFileName has not been set (for whatever reason)
	 * then this method will attempt to infer the name of the source
	 * file using the class name.
	 *
	 * @return The name of the source file, for example
	 *         net/sourceforge/cobertura/coveragedata/ClassData.java
	 */
	public String getSourceFileName()
	{
		String baseName;
		lock.lock();
		try
		{
			if (sourceFileName != null)
				baseName = sourceFileName;
			else
			{
				baseName = getBaseName();
				int firstDollarSign = baseName.indexOf('$');
				if (firstDollarSign == -1 || firstDollarSign == 0)
					baseName += ".java";
				else
					baseName = baseName.substring(0, firstDollarSign)
						+ ".java";
			}
	
			String packageName = getPackageName();
			if (packageName.equals(""))
				return baseName;
			return packageName.replace('.', '/') + '/' + baseName;
		}
		finally
		{
			lock.unlock();
		}
	}

	public int hashCode()
	{
		return this.name.hashCode();
	}

	/**
	 * @return True if the line contains at least one condition jump (branch)
	 */
	public boolean hasBranch(int lineNumber) 
	{
		lock.lock();
		try
		{
			return branches.containsKey(Integer.valueOf(lineNumber));
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 * Determine if a given line number is a valid line of code.
	 *
	 * @return True if the line contains executable code.  False
	 *         if the line is empty, or a comment, etc.
	 */
	public boolean isValidSourceLineNumber(int lineNumber) 
	{
		lock.lock();
		try
		{
			return children.containsKey(Integer.valueOf(lineNumber));
		}
		finally
		{
			lock.unlock();
		}
	}

	public void addLineJump(int lineNumber, int branchNumber) 
	{
		lock.lock();
		try
		{
			LineData lineData = getLineData(lineNumber);
			if (lineData != null) 
			{
				lineData.addJump(branchNumber);
				this.branches.put(Integer.valueOf(lineNumber), lineData);
			}
		}
		finally
		{
			lock.unlock();
		}
	}

	public void addLineSwitch(int lineNumber, int switchNumber, int min, int max, int maxBranches) 
	{
		lock.lock();
		try
		{
			LineData lineData = getLineData(lineNumber);
			if (lineData != null) 
			{
				lineData.addSwitch(switchNumber, min, max, maxBranches);
				this.branches.put(Integer.valueOf(lineNumber), lineData);
			}
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 * Merge some existing instrumentation with this instrumentation.
	 *
	 * @param coverageData Some existing coverage data.
	 */
	public void merge(CoverageData coverageData)
	{
		ClassData classData = (ClassData)coverageData;

		// If objects contain data for different classes then don't merge
		if (!this.getName().equals(classData.getName()))
			return;

		getBothLocks(classData);
		try
		{
			super.merge(coverageData);
	
			// We can't just call this.branches.putAll(classData.branches);
			// Why not?  If we did a putAll, then the LineData objects from
			// the coverageData class would overwrite the LineData objects
			// that are already in "this.branches"  And we don't need to
			// update the LineData objects that are already in this.branches
			// because they are shared between this.branches and this.children,
			// so the object hit counts will be moved when we called
			// super.merge() above.
			for (Iterator<Integer> iter = classData.branches.keySet().iterator(); iter.hasNext();)
			{
				Integer key = iter.next();
				if (!this.branches.containsKey(key))
				{
					this.branches.put(key, classData.branches.get(key));
				}
			}
	
			this.containsInstrumentationInfo |= classData.containsInstrumentationInfo;
			this.methodNamesAndDescriptors.addAll(classData
					.getMethodNamesAndDescriptors());
			if (classData.sourceFileName != null)
				this.sourceFileName = classData.sourceFileName;
		}
		finally
		{
			lock.unlock();
			classData.lock.unlock();
		}
	}

	public void removeLine(int lineNumber)
	{
		Integer lineObject = Integer.valueOf(lineNumber);
		lock.lock();
		try
		{
			children.remove(lineObject);
			branches.remove(lineObject);
		}
		finally
		{
			lock.unlock();
		}
	}

	public void setContainsInstrumentationInfo()
	{
		lock.lock();
		try
		{
			this.containsInstrumentationInfo = true;
		}
		finally
		{
			lock.unlock();
		}
	}

	public void setSourceFileName(String sourceFileName)
	{
		lock.lock();
		try
		{
			this.sourceFileName = sourceFileName;
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 * Increment the number of hits for a particular line of code.
	 *
	 * @param lineNumber the line of code to increment the number of hits.
	 * @param hits how many times the piece was called
	 */
	public void touch(int lineNumber,int hits)
	{
		lock.lock();
		try
		{
			LineData lineData = getLineData(lineNumber);
			if (lineData == null)
				lineData = addLine(lineNumber, null, null);
			lineData.touch(hits);
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 * Increments the number of hits for particular hit counter of particular branch on particular line number.
	 * 
	 * @param lineNumber The line of code where the branch is
	 * @param branchNumber  The branch on the line to change the hit counter
	 * @param branch The hit counter (true or false)
	 * @param hits how many times the piece was called
	 */
	public void touchJump(int lineNumber, int branchNumber, boolean branch,int hits) {
		lock.lock();
		try
		{
			LineData lineData = getLineData(lineNumber);
			if (lineData == null)
				lineData = addLine(lineNumber, null, null);
			lineData.touchJump(branchNumber, branch,hits);
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 * Increments the number of hits for particular hit counter of particular switch branch on particular line number.
	 * 
	 * @param lineNumber The line of code where the branch is
	 * @param switchNumber  The switch on the line to change the hit counter
	 * @param branch The hit counter
	 * @param hits how many times the piece was called  
	 */
	public void touchSwitch(int lineNumber, int switchNumber, int branch,int hits) {
		lock.lock();
		try
		{
			LineData lineData = getLineData(lineNumber);
			if (lineData == null)
				lineData = addLine(lineNumber, null, null);
			lineData.touchSwitch(switchNumber, branch,hits);
		}
		finally
		{
			lock.unlock();
		}
	}


}
