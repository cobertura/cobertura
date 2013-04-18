/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2005 Grzegorz Lukasik
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
package net.sourceforge.cobertura.merge;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import junit.framework.TestCase;
import net.sourceforge.cobertura.coveragedata.ClassData;
import net.sourceforge.cobertura.coveragedata.LineData;
import net.sourceforge.cobertura.coveragedata.PackageData;
import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.coveragedata.SourceFileData;

/**
 * Tests merge feature by calling directly ProjectData.merge method.
 */
public class MergeTest extends TestCase {
	private ClassData firstClass = new ClassData("test.First");
	private ClassData secondClass = new ClassData("test.Second");
	private ClassData thirdClass = new ClassData("test.Third");
	private ClassData firstClassB = new ClassData("test.First");
	private ClassData fourthClass = new ClassData("test.me.Fourth");
	private ClassData fifthClass = new ClassData("beautiful.Fourth");
	private ClassData sixthClass = new ClassData("Fourth");
	private ClassData seventhClass = new ClassData("Seventh");

	private ProjectData greenProject = new ProjectData();
	private ProjectData redProject = new ProjectData();

	public void testMergePackages() {
		greenProject.addClassData( firstClass);
		greenProject.addClassData( fourthClass);
		redProject.addClassData( fifthClass);
		redProject.addClassData( sixthClass);
		redProject.addClassData( seventhClass);
		
		//merge with null - should not change the greenProject
		greenProject.merge(null);
		
		greenProject.merge( redProject);
		
		Iterator subpackages = greenProject.getSubPackages( "").iterator();
		assertEquals( "", ((PackageData)subpackages.next()).getName());
		assertEquals( "beautiful", ((PackageData)subpackages.next()).getName());
		assertEquals( "test", ((PackageData)subpackages.next()).getName());
		assertEquals( "test.me", ((PackageData)subpackages.next()).getName());
		assertFalse( subpackages.hasNext());
		
		assertEquals(5, greenProject.getClasses().size());
		assertEquals(3, redProject.getClasses().size());

		assertNotNull( greenProject.getClassData("test.First"));
		assertNotNull( greenProject.getClassData("test.me.Fourth"));
		assertNotNull( greenProject.getClassData("beautiful.Fourth"));
		assertNotNull( greenProject.getClassData("Fourth"));
		assertNotNull( greenProject.getClassData("Seventh"));
		assertNull( redProject.getClassData( "test.First"));
		
		Iterator packages = greenProject.getPackages().iterator();
		
		PackageData first = (PackageData) packages.next();
		assertEquals( "", first.getName());
		assertEquals( 2, first.getNumberOfChildren());
		assertNotNull( first.getChild("Fourth"));
		assertNotNull( first.getChild("Seventh"));
		
		PackageData beautiful = (PackageData) packages.next();
		assertEquals( "beautiful", beautiful.getName());
		assertEquals( 1, beautiful.getNumberOfChildren());
		assertNotNull( beautiful.getChild("Fourth"));
		
		PackageData test = (PackageData) packages.next();
		assertEquals( "test", test.getName());
		assertEquals( 1, test.getNumberOfChildren());
		assertNotNull( test.getChild("First"));
		assertNull( test.getChild("test/me/First.java"));
		assertNull( test.getChild("Fourth.java"));
		
		PackageData testMe = (PackageData) packages.next();
		assertEquals( "test.me", testMe.getName());
		assertEquals( 1, testMe.getNumberOfChildren());
		assertNull( testMe.getChild("test/First.java"));
		assertNotNull( testMe.getChild("Fourth"));
		assertNull( testMe.getChild("Fourth.java"));
		
		assertFalse( packages.hasNext());
	}

	
	public void testMergeDifferentClassData() {
		greenProject.addClassData( firstClass);
		
		redProject.addClassData( secondClass);
		redProject.addClassData( thirdClass);
		
		greenProject.merge( redProject);
		
		assertEquals( 1, greenProject.getNumberOfChildren());
		assertEquals( 3, greenProject.getClasses().size());
		
		assertNotNull( greenProject.getClassData("test.First"));
		assertNotNull( greenProject.getClassData("test.Second"));
		assertNotNull( greenProject.getClassData("test.Third"));

		assertNull( redProject.getClassData("test.First"));
		assertNotNull( redProject.getClassData("test.Second"));
		assertNotNull( redProject.getClassData("test.Third"));
	}
	
	public void testMergeSimillarClassData() {
		greenProject.addClassData( secondClass);
		greenProject.addClassData( thirdClass);
		
		redProject.addClassData( firstClass);
		redProject.addClassData( thirdClass);
		
		greenProject.merge( redProject);

		assertEquals( 1, greenProject.getNumberOfChildren());
		assertEquals( 3, greenProject.getClasses().size());
		
		assertNotNull( greenProject.getClassData("test.First"));
		assertNotNull( greenProject.getClassData("test.Second"));
		assertNotNull( greenProject.getClassData("test.Third"));

		assertNotNull( redProject.getClassData("test.First"));
		assertNull( redProject.getClassData("test.Second"));
		assertNotNull( redProject.getClassData("test.Third"));
	}

	public void testMergeDifferentLineNumbers() {
		firstClass.addLine( 2, "helloWorld","()V");
		firstClass.addLine( 3, "helloWorld","()V");
		greenProject.addClassData( firstClass);
		
		firstClassB.addLine( 1, "helloWorld","()V");
		firstClassB.addLine( 5, "helloWorld","()V");
		redProject.addClassData( firstClassB);
		
		greenProject.merge( redProject);
		
		ClassData cd = greenProject.getClassData("test.First");
		assertNotNull( cd);
		assertEquals( 4, cd.getNumberOfValidLines());
		assertEquals( 2, redProject.getClassData("test.First").getNumberOfValidLines());
		
		Iterator lines = cd.getLines().iterator();
		LineData line1 = (LineData) lines.next();
		assertEquals( 1, line1.getLineNumber());
		LineData line2 = (LineData) lines.next();
		assertEquals( 2, line2.getLineNumber());
		LineData line3 = (LineData) lines.next();
		assertEquals( 3, line3.getLineNumber());
		LineData line5 = (LineData) lines.next();
		assertEquals( 5, line5.getLineNumber());
		assertFalse( lines.hasNext());
	}

	public void testMergeSimillarLineNumbers() {
		firstClass.addLine( 2, "helloWorld","()V");
		firstClass.touch(2,1);
		firstClass.touch(2,1);
		firstClass.addLine( 3, "helloWorld","()V");
		greenProject.addClassData( firstClass);
		
		firstClassB.addLine( 2, "helloWorld","()V");
		firstClassB.touch(2,1);
		firstClassB.touch(2,1);
		firstClassB.touch(2,1);
		firstClassB.addLine( 3, "helloWorld","()V");
		firstClassB.touch(3,1);
		firstClassB.addLine( 7, "helloWorld","()V");
		redProject.addClassData( firstClassB);
		
		greenProject.merge( redProject);
		
		ClassData cd = greenProject.getClassData("test.First");
		assertNotNull( cd);
		assertEquals( 3, cd.getNumberOfValidLines());
		assertEquals( 3, redProject.getClassData("test.First").getNumberOfValidLines());
		
		Iterator lines = cd.getLines().iterator();
		LineData line2 = (LineData) lines.next();
		assertEquals( 2, line2.getLineNumber());
		assertEquals( 5, line2.getHits());
		LineData line3 = (LineData) lines.next();
		assertEquals( 3, line3.getLineNumber());
		assertEquals( 1, line3.getHits());
		LineData line7 = (LineData) lines.next();
		assertEquals( 7, line7.getLineNumber());
		assertEquals( 0, line7.getHits());
		assertFalse( lines.hasNext());
	}
	
	public void testMergeBranches() {
		firstClass.addLine( 1, "helloWorld","()V");
		firstClass.addLineJump(1, 0);
		firstClass.addLine( 2, "helloWorld","()V");
		firstClass.addLineJump(2, 0);
		firstClass.addLineJump(2, 1);
		firstClass.addLine( 3, "helloWorld","()V");
		firstClass.addLine( 4, "helloWorld","()V");
		firstClass.addLineSwitch(4, 0, 0, 2, Integer.MAX_VALUE);
		firstClass.addLine( 5, "helloWorld","()V");
		firstClass.addLine( 8, "helloWorld","()V");
		greenProject.addClassData( firstClass);
		
		firstClassB.addLine( 1, "helloWorld","()V");
		firstClassB.addLineJump(1, 0);
		firstClassB.addLine( 2, "helloWorld","()V");
		firstClassB.addLine( 3, "helloWorld","()V");
		firstClassB.addLineSwitch(3, 0, 2, 4, Integer.MAX_VALUE);
		firstClassB.addLine( 6, "helloWorld","()V");
		firstClassB.addLineJump(6, 0);
		firstClassB.addLine( 7, "helloWorld","()V");
		firstClassB.addLine( 8, "helloWorld","()V");
		redProject.addClassData( firstClassB);
		
		greenProject.merge( redProject);
		
		ClassData cd = greenProject.getClassData("test.First");
		
		Iterator lines = cd.getLines().iterator();
		
		LineData line1 = (LineData) lines.next();
		assertTrue( line1.hasBranch());
		LineData line2 = (LineData) lines.next();
		assertTrue( line2.hasBranch());
		LineData line3 = (LineData) lines.next();
		assertTrue( line3.hasBranch());
		LineData line4 = (LineData) lines.next();
		assertTrue( line4.hasBranch());
		LineData line5 = (LineData) lines.next();
		assertFalse( line5.hasBranch());
		LineData line6 = (LineData) lines.next();
		assertTrue( line6.hasBranch());
		LineData line7 = (LineData) lines.next();
		assertFalse( line7.hasBranch());
		LineData line8 = (LineData) lines.next();
		assertFalse( line8.hasBranch());
		assertFalse( lines.hasNext());
	}
	
	public void testMergeSourceFiles() {
		greenProject.addClassData( secondClass);
		greenProject.addClassData( fourthClass);
		
		redProject.addClassData( firstClass);
		redProject.addClassData( fifthClass);
		redProject.addClassData( seventhClass);
		
		greenProject.merge( redProject);
		
		Collection sources = greenProject.getSourceFiles();
		assertEquals( 5, sources.size());
		
		Set sourceNames = new HashSet();
		Iterator it = sources.iterator();
		while( it.hasNext())
			sourceNames.add( ((SourceFileData)it.next()).getName());
		
		assertTrue( sourceNames.contains("test/First.java"));
		assertTrue( sourceNames.contains("test/Second.java"));
		assertTrue( sourceNames.contains("test/me/Fourth.java"));
		assertTrue( sourceNames.contains("beautiful/Fourth.java"));
		assertTrue( sourceNames.contains("Seventh.java"));
	}
}
