/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
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

import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LineDataTest {

	private final LineData a = new LineData(10, "test1", "(I)B");
	private final LineData b = new LineData(11, "test1", "(I)B");
	private final LineData c = new LineData(12, "test2", "(I)B");
	private final LineData d = new LineData(13, "test2", "(I)B");
	private final LineData e = new LineData(14);
	private final LineData f = new LineData(15);

	@Test
	public void testEquals() {
		assertFalse(a.equals(null));
		assertFalse(a.equals(new Integer(4)));

		assertTrue(a.equals(a));
		assertFalse(a.equals(b));
		assertFalse(a.equals(c));
		assertFalse(a.equals(d));
		assertFalse(a.equals(e));
		assertFalse(a.equals(f));

		LineData aPrime = new LineData(10, "test1", "(I)B");
		assertTrue(a.equals(aPrime));
	}

	@Test
	public void testHashCode() {
		assertEquals(a.hashCode(), a.hashCode());

		LineData aPrime = new LineData(10, "test1", "(I)B");
		assertEquals(a.hashCode(), aPrime.hashCode());
	}

	@Test
	public void testGetLineNumber() {
		assertEquals(10, a.getLineNumber());
		assertEquals(11, b.getLineNumber());
		assertEquals(12, c.getLineNumber());
		assertEquals(13, d.getLineNumber());
		assertEquals(14, e.getLineNumber());
		assertEquals(15, f.getLineNumber());
	}

	@Test
	public void testGetNumbers() {
		assertEquals(1, a.getBranchCoverageRate(), 0);
		assertEquals(0, a.getLineCoverageRate(), 0);
		assertEquals(0, a.getNumberOfCoveredLines());
		assertEquals(0, a.getNumberOfCoveredBranches());
		assertEquals(0, a.getNumberOfValidBranches());
		assertEquals(1, a.getNumberOfValidLines());

		a.addJump(0);
		a.addJump(1);
		assertEquals(0, a.getBranchCoverageRate(), 0);
		assertEquals(0, a.getLineCoverageRate(), 0);
		assertEquals(0, a.getNumberOfCoveredLines());
		assertEquals(0, a.getNumberOfCoveredBranches());
		assertEquals(4, a.getNumberOfValidBranches());
		assertEquals(1, a.getNumberOfValidLines());

		for (int i = 0; i < 5; i++) {
			a.touch(1);
			assertEquals(0, a.getBranchCoverageRate(), 0);
			assertEquals(1, a.getLineCoverageRate(), 0);
			assertEquals(1, a.getNumberOfCoveredLines());
			assertEquals(0, a.getNumberOfCoveredBranches());
			assertEquals(4, a.getNumberOfValidBranches());
			assertEquals(1, a.getNumberOfValidLines());
		}

		a.touchJump(0, true, 1);
		assertEquals(0.25, a.getBranchCoverageRate(), 0);
		assertEquals(1, a.getLineCoverageRate(), 0);
		assertEquals(1, a.getNumberOfCoveredLines());
		assertEquals(1, a.getNumberOfCoveredBranches());
		assertEquals(4, a.getNumberOfValidBranches());
		assertEquals(1, a.getNumberOfValidLines());

		a.touchJump(1, false, 1);
		assertEquals(0.5, a.getBranchCoverageRate(), 0);
		assertEquals(1, a.getLineCoverageRate(), 0);
		assertEquals(1, a.getNumberOfCoveredLines());
		assertEquals(2, a.getNumberOfCoveredBranches());
		assertEquals(4, a.getNumberOfValidBranches());
		assertEquals(1, a.getNumberOfValidLines());

		a.touchJump(1, true, 1);
		assertEquals(0.75, a.getBranchCoverageRate(), 0);
		assertEquals(1, a.getLineCoverageRate(), 0);
		assertEquals(1, a.getNumberOfCoveredLines());
		assertEquals(3, a.getNumberOfCoveredBranches());
		assertEquals(4, a.getNumberOfValidBranches());
		assertEquals(1, a.getNumberOfValidLines());

		a.touchJump(0, false, 1);
		assertEquals(1, a.getBranchCoverageRate(), 0);
		assertEquals(1, a.getLineCoverageRate(), 0);
		assertEquals(1, a.getNumberOfCoveredLines());
		assertEquals(4, a.getNumberOfCoveredBranches());
		assertEquals(4, a.getNumberOfValidBranches());
		assertEquals(1, a.getNumberOfValidLines());
	}

	@Test
	public void testSetConditional() {
		assertFalse(c.hasBranch());
		c.addJump(0);
		assertTrue(c.hasBranch());
		c.addJump(1);
		assertTrue(c.hasBranch());
	}

	@Test
	public void testSetMethodNameAndDescriptor() {
		e.setMethodNameAndDescriptor("test3", "(I)B");
		assertEquals("test3", e.getMethodName());
		assertEquals("(I)B", e.getMethodDescriptor());

		f.setMethodNameAndDescriptor("test4", "(I)B");
		assertEquals("test4", f.getMethodName());
		assertEquals("(I)B", f.getMethodDescriptor());
	}

	@Test
	public void testTouch() {
		assertEquals(0, a.getHits());
		for (int i = 0; i < 400; i++)
			a.touch(2);
		assertEquals(800, a.getHits());
	}

	private static void getSwitchDataIteratively(LineData data) {
		/*
		 * When this test fails, it usually does so well before 2000 iterations.   If it
		 * gets past 2000, it will usually pass, so there is not much need in going much
		 * past 2000.
		 */
		for (int i = 0; i < 2000; i++) {
			/*
			 * The following yield is needed to make sure the other thread gets
			 * some CPU.  Otherwise, this thread will get too much of a jump ahead
			 * of the other thread.
			 */
			Thread.yield();

			data.getSwitchData(i, new SwitchData(1, Integer.MAX_VALUE));
		}
	}

	private void runGetSwitchDataTestWithTwoThreads() throws Throwable {
		final LineData data = new LineData(2);
		final AtomicReference<Throwable> possibleThrowable = new AtomicReference<Throwable>();

		ThreadGroup threadGroup = new ThreadGroup("TestThreadGroup") {
			public void uncaughtException(Thread thread, Throwable t) {
				/*
				 * Save the Throwable for later use and interrupt this thread so it exits
				 */
				possibleThrowable.set(t);
				thread.interrupt();
			}
		};

		/*
		 * Create two threads using the above thread group
		 */
		Thread thread1 = new Thread(threadGroup, "1") {
			public void run() {
				getSwitchDataIteratively(data);
			}
		};
		Thread thread2 = new Thread(threadGroup, "2") {
			public void run() {
				getSwitchDataIteratively(data);
			}
		};
		thread1.start();
		thread2.start();
		/*
		 * Wait for the threads to exit
		 */
		if (thread1.isAlive())
			thread1.join();
		if (thread2.isAlive())
			thread2.join();
		Throwable t = possibleThrowable.get();
		if (t != null) {
			throw t;
		}
	}

	@Test
	public void testMultiThreadedGetSwitchData() throws Throwable {
		/*
		 * This test will often pass with only one iteration.
		 * It passes once in a while with 10.   It never passes
		 * with 100 (I hope).
		 */
		for (int i = 0; i < 100; i++) {
			runGetSwitchDataTestWithTwoThreads();
		}
	}

	private static void getJumpDataIteratively(LineData data) {
		/*
		 * When this test fails, it usually does so well before 2000 iterations.   If it
		 * gets past 2000, it will usually pass, so there is not much need in going much
		 * past 2000.
		 */
		for (int i = 0; i < 2000; i++) {
			/*
			 * The following yield is needed to make sure the other thread gets
			 * some CPU.  Otherwise, this thread will get too much of a jump ahead
			 * of the other thread.
			 */
			Thread.yield();

			data.getJumpData(i);
		}
	}

	private void runGetJumpDataTestWithTwoThreads() throws Throwable {
		final LineData data = new LineData(2);
		final AtomicReference<Throwable> possibleThrowable = new AtomicReference<Throwable>();

		ThreadGroup threadGroup = new ThreadGroup("TestThreadGroup") {
			public void uncaughtException(Thread thread, Throwable t) {
				/*
				 * Save the Throwable for later use and interrupt this thread so it exits
				 */
				possibleThrowable.set(t);
				thread.interrupt();
			}
		};

		/*
		 * Create two threads using the above thread group
		 */
		Thread thread1 = new Thread(threadGroup, "1") {
			public void run() {
				getJumpDataIteratively(data);
			}
		};
		Thread thread2 = new Thread(threadGroup, "2") {
			public void run() {
				getJumpDataIteratively(data);
			}
		};
		thread1.start();
		thread2.start();
		/*
		 * Wait for the threads to exit
		 */
		if (thread1.isAlive())
			thread1.join();
		if (thread2.isAlive())
			thread2.join();
		Throwable t = possibleThrowable.get();
		if (t != null) {
			throw t;
		}
	}

	@Test
	public void testMultiThreadedGetJumpData() throws Throwable {
		/*
		 * This test will often pass with only one iteration.
		 * It passes once in a while with 10.   It never passes
		 * with 100 (I hope).
		 */
		for (int i = 0; i < 100; i++) {
			runGetJumpDataTestWithTwoThreads();
		}
	}

}
