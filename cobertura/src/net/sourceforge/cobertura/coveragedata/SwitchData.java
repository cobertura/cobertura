/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
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

import java.io.Serializable;
import java.util.Arrays;

import net.sourceforge.cobertura.CoverageIgnore;

@CoverageIgnore
public class SwitchData implements BranchCoverageData, Comparable<Object>, Serializable {
	private static final long serialVersionUID = 9;

	private int switchNumber;
	
	private long defaultHits;

	private long[] hits;
	
	private int[] keys;
	
	private int maxBranches;

	public SwitchData(int switchNumber, int[] keys, int maxBranches) {
		this.switchNumber = switchNumber;
		defaultHits = 0;
		hits = new long[keys.length];
		Arrays.fill(hits, 0);
		this.keys = new int[keys.length];
		System.arraycopy(keys, 0, this.keys, 0, keys.length);
		this.maxBranches = maxBranches;
	}

	public SwitchData(int switchNumber, int min, int max, int maxBranches) {
		this.switchNumber = switchNumber;
		defaultHits = 0;
		hits = new long[max - min + 1];
		Arrays.fill(hits, 0);
		this.keys = new int[max - min + 1];
		for (int i = 0; min <= max; keys[i++] = min++);
		this.maxBranches = maxBranches;		
	}

	public SwitchData(int switchNumber, int maxBranches) {
		this(switchNumber, new int[0], maxBranches);
	}

	public int compareTo(Object o) {
		if (!o.getClass().equals(SwitchData.class))
			return Integer.MAX_VALUE;
		return this.switchNumber - ((SwitchData) o).switchNumber;
	}
	
	void touchBranch(int branch,int new_hits) {
		if (branch == -1) {
			defaultHits+=new_hits;
		} else {
			if (hits.length <= branch) {
				long[] old = hits;
				hits = new long[branch + 1];
				System.arraycopy(old, 0, hits, 0, old.length);
				Arrays.fill(hits, old.length, hits.length - 1, 0);
			}
			hits[branch]+=new_hits;
		}
	}
	
	public int getSwitchNumber() {
		return this.switchNumber;
	}

	public long getHits(int branch) {
		return (hits.length > branch) ? hits[branch] : -1;		
	}

	public long getDefaultHits() {
		return defaultHits;
	}

	public double getBranchCoverageRate() {
		int branches = getNumberOfValidBranches();
		int hit = (defaultHits > 0) ? 1 : 0;
		for (int i = hits.length - 1; i >= 0; hit += ((hits[i--] > 0) ? 1 : 0));
		return ((double) hit) / branches;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if ((obj == null) || !(obj.getClass().equals(this.getClass())))
			return false;

		SwitchData switchData = (SwitchData) obj;
		return (this.defaultHits == switchData.defaultHits)
				&& (Arrays.equals(this.hits, switchData.hits))
				&& (this.switchNumber == switchData.switchNumber);
	}

	public int hashCode() {
		return this.switchNumber;
	}

	public int getNumberOfCoveredBranches() {
		int ret = (defaultHits > 0) ? 1 : 0;
		for (int i = hits.length -1; i >= 0;i--) 
		{
			if (hits[i] > 0) ret++;
		}
		return ret;
	}

	public int getNumberOfValidBranches() {
		return Math.min(hits.length + 1, maxBranches);		
	}

	public void merge(BranchCoverageData coverageData) {
		SwitchData switchData = (SwitchData) coverageData;
		defaultHits += switchData.defaultHits;
		for (int i = Math.min(hits.length, switchData.hits.length) - 1; i >= 0; i--)
			hits[i] += switchData.hits[i];
		if (switchData.hits.length > hits.length)
		{
			long[] old = hits;
			hits = new long[switchData.hits.length];
			System.arraycopy(old, 0, hits, 0, old.length);
			System.arraycopy(switchData.hits, old.length, hits, old.length, hits.length - old.length);
		}
		if ((this.keys.length == 0) && (switchData.keys.length > 0))
			this.keys = switchData.keys;
		maxBranches = Math.min(maxBranches, switchData.getMaxBranches());
	}
	
	public int getMaxBranches() {
		return maxBranches;
	}
	
	public void setMaxBranches(int maxBranches) {
		this.maxBranches = maxBranches;
	}
}
