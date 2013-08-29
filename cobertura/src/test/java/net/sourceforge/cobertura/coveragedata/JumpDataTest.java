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

public class JumpDataTest {

	private final JumpData a = new JumpData(0);
	private final JumpData b = new JumpData(1);

	@Test
	public void testEquals() {
		assertFalse(a.equals(null));
		assertFalse(a.equals(new Integer(4)));

		assertTrue(a.equals(a));
		assertFalse(a.equals(b));

		JumpData aPrime = new JumpData(0);
		assertTrue(a.equals(aPrime));
	}

	@Test
	public void testHashCode() {
		assertEquals(a.hashCode(), a.hashCode());

		JumpData aPrime = new JumpData(0);
		assertEquals(a.hashCode(), aPrime.hashCode());
	}

	@Test
	public void testGetBranchNumber() {
		assertEquals(0, a.getConditionNumber());
		assertEquals(1, b.getConditionNumber());
	}

	@Test
	public void testGetNumbers() {
		assertEquals(0, a.getBranchCoverageRate(), 0);
		assertEquals(2, a.getNumberOfValidBranches());
		assertEquals(0, a.getNumberOfCoveredBranches());

		for (int i = 0; i < 5; i++) {
			a.touchBranch(true, 1);
			assertEquals(0.5, a.getBranchCoverageRate(), 0);
			assertEquals(2, a.getNumberOfValidBranches());
			assertEquals(1, a.getNumberOfCoveredBranches());
		}

		a.touchBranch(false, 1);
		assertEquals(1, a.getBranchCoverageRate(), 0);
		assertEquals(2, a.getNumberOfValidBranches());
		assertEquals(2, a.getNumberOfCoveredBranches());
	}

	@Test
	public void testTouchBranch() {
		assertEquals(0, a.getTrueHits());
		for (int i = 0; i < 400; i++)
			a.touchBranch(true, 1);
		assertEquals(400, a.getTrueHits());

		assertEquals(0, a.getFalseHits());
		for (int i = 0; i < 200; i++)
			a.touchBranch(false, 1);
		assertEquals(200, a.getFalseHits());
	}

}
