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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SwitchDataTest {

	private final SwitchData a = new SwitchData(0, new int[]{0, 1, 2, 3},
			Integer.MAX_VALUE);

	private final SwitchData b = new SwitchData(1, 1, 9, Integer.MAX_VALUE);

	@Test
	public void testEquals() {
		assertFalse(a.equals(null));
		assertFalse(a.equals(Integer.valueOf(4)));

		assertTrue(a.equals(a));
		assertFalse(a.equals(b));

		SwitchData aPrime = new SwitchData(0, new int[]{0, 1, 2, 3},
				Integer.MAX_VALUE);
		assertTrue(a.equals(aPrime));
	}

	@Test
	public void testHashCode() {
		assertEquals(a.hashCode(), a.hashCode());

		SwitchData aPrime = new SwitchData(0, new int[]{0, 1, 2, 3},
				Integer.MAX_VALUE);
		assertEquals(a.hashCode(), aPrime.hashCode());
	}

	@Test
	public void testGetSwitchNumber() {
		assertEquals(0, a.getSwitchNumber());
		assertEquals(1, b.getSwitchNumber());
	}

	@Test
	public void testGetNumbers() {
		assertEquals(0, a.getBranchCoverageRate(), 0);
		assertEquals(5, a.getNumberOfValidBranches(), 0);
		assertEquals(0, a.getNumberOfCoveredBranches(), 0);

		for (int i = 0; i < 5; i++) {
			a.touchBranch(1, 1);
			assertEquals(0.2, a.getBranchCoverageRate(), 0);
			assertEquals(5, a.getNumberOfValidBranches(), 0);
			assertEquals(1, a.getNumberOfCoveredBranches(), 0);
		}

		a.touchBranch(-1, 1);
		assertEquals(0.4, a.getBranchCoverageRate(), 0);
		assertEquals(5, a.getNumberOfValidBranches(), 0);
		assertEquals(2, a.getNumberOfCoveredBranches(), 0);

		a.touchBranch(0, 1);
		assertEquals(0.6, a.getBranchCoverageRate(), 0);
		assertEquals(5, a.getNumberOfValidBranches(), 0);
		assertEquals(3, a.getNumberOfCoveredBranches(), 0);

		a.touchBranch(2, 1);
		assertEquals(0.8, a.getBranchCoverageRate(), 0);
		assertEquals(5, a.getNumberOfValidBranches(), 0);
		assertEquals(4, a.getNumberOfCoveredBranches(), 0);

		a.touchBranch(3, 1);
		assertEquals(1, a.getBranchCoverageRate(), 0);
		assertEquals(5, a.getNumberOfValidBranches(), 0);
		assertEquals(5, a.getNumberOfCoveredBranches(), 0);
	}

	@Test
	public void testTouch() {
		assertEquals(0, a.getHits(0));
		for (int i = 0; i < 400; i++)
			a.touchBranch(0, 1);
		assertEquals(400, a.getHits(0));

		assertEquals(0, a.getHits(1));
		for (int i = 0; i < 4500; i++)
			a.touchBranch(1, 1);
		assertEquals(4500, a.getHits(1));

		assertEquals(0, a.getHits(2));
		for (int i = 0; i < 300; i++)
			a.touchBranch(2, 1);
		assertEquals(300, a.getHits(2));

		assertEquals(0, a.getHits(3));
		for (int i = 0; i < 800; i++)
			a.touchBranch(3, 1);
		assertEquals(800, a.getHits(3));

		assertEquals(0, a.getDefaultHits());
		for (int i = 0; i < 200; i++)
			a.touchBranch(-1, 1);
		assertEquals(200, a.getDefaultHits());
	}

	@Test
	public void testMerge() {
		a.touchBranch(0, 1);
		a.touchBranch(0, 1);
		a.touchBranch(2, 1);
		a.touchBranch(-1, 1);
		SwitchData x = new SwitchData(0, Integer.MAX_VALUE);
		x.touchBranch(3, 1);
		x.touchBranch(3, 1);
		a.merge(x);
		assertEquals(2, a.getHits(0));
		assertEquals(0, a.getHits(1));
		assertEquals(1, a.getHits(2));
		assertEquals(2, a.getHits(3));
		assertEquals(1, a.getDefaultHits());

		x = new SwitchData(0, Integer.MAX_VALUE);
		x.touchBranch(5, 1);
		x.touchBranch(-1, 1);
		a.merge(x);
		assertEquals(2, a.getHits(0));
		assertEquals(0, a.getHits(1));
		assertEquals(1, a.getHits(2));
		assertEquals(2, a.getHits(3));
		assertEquals(0, a.getHits(4));
		assertEquals(1, a.getHits(5));
		assertEquals(2, a.getDefaultHits());
	}
}
