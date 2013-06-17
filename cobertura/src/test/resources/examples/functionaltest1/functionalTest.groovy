/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (C) 2000-2002 The Apache Software Foundation.  All rights
 * reserved.
 * Copyright (C) 2008 John Lewis
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "Ant" and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */


import org.apache.tools.ant.BuildException

import static org.junit.Assert.assertEquals

//load testUtil definitions
evaluate(new File("${ant.project.baseDir}/../../testUtil.groovy"))

runReports = { set ->

	//run a full xml report
	ant."${reportTaskName}"(datafile:'${basedir}/cobertura.ser', srcdir:'${src.dir}', destdir:'${coverage.xml.dir}', format:'xml')
	
	def fullReport = readXMLReport("${ant.project.baseDir}/${properties.'coverage.xml.dir'}/coverage.xml")

	//now run the summary report
	ant."${reportTaskName}"(datafile:'${basedir}/cobertura.ser', srcdir:'${src.dir}', destdir:'${coverage.xml.dir}', format:'summaryxml')

	def summary = readXMLReport("${ant.project.baseDir}/${properties.'coverage.xml.dir'}/coverage-summary.xml")

	assertEquals(fullReport.totalComplexity, summary.totalComplexity, 0)
	assertEquals(fullReport.totalLineRate, summary.totalLineRate)
	assertEquals(fullReport.totalBranchRate, summary.totalBranchRate)
	assertEquals(fullReport.totalLinesCovered, summary.totalLinesCovered)
	assertEquals(fullReport.totalLinesValid, summary.totalLinesValid)
	assertEquals(fullReport.totalBranchesCovered, summary.totalBranchesCovered)
	assertEquals(fullReport.totalBranchesValid, summary.totalBranchesValid)
	
	ant.mkdir(dir:'${coverage.html.dir}')
	
	// maxmemory is only specified to test the attribute
	ant."${reportTaskName}"([datafile:'${basedir}/cobertura.ser', destdir:'${coverage.html.dir}', maxmemory:'512M'], set)

}

readXMLReport = { xmlReport ->
	def parser = new XmlParser()
	parser.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd",false)
	
	def info = [:]
	info.root = parser.parse(xmlReport)
	
	info.totalLineRate = rateToInt(info.root.'@line-rate')
	info.totalBranchRate = rateToInt(info.root.'@branch-rate')
	info.totalLinesCovered = info.root.'@lines-covered'.toInteger()
	info.totalLinesValid = info.root.'@lines-valid'.toInteger()
	info.totalBranchesCovered = info.root.'@branches-covered'.toInteger()
	info.totalBranchesValid = info.root.'@branches-valid'.toInteger()
	info.totalComplexity = info.root.'@complexity'.toDouble()
	
	assertEquals("line-rate should equal lines-covered/lines-valid", info.totalLineRate, calculateRate(info.totalLinesCovered, info.totalLinesValid))
	assertEquals("branch-rate should equal branches-covered/branches-valid", info.totalBranchRate, calculateRate(info.totalBranchesCovered, info.totalBranchesValid))

	info
}
 
checkRates = {
	/*
	 * Read the xml report to get the expected values
	 */
	def datafile = new File("${ant.project.baseDir}/cobertura.ser")
	def xmlReport = new File("${ant.project.baseDir}/${ant.project.properties.'coverage.xml.dir'}/coverage.xml")
	
	def info = readXMLReport(xmlReport)
	
	
	info.minPackageLineRate = rateToInt(info.root.packages.'package'.'@line-rate'*.toDouble().min().toString())
	info.minPackageBranchRate = rateToInt(info.root.packages.'package'.'@branch-rate'*.toDouble().min().toString())
	info.minClassLineRate = rateToInt(info.root.packages.'package'.classes.'class'.'@line-rate'*.toDouble().min().toString())
	info.minClassBranchRate = rateToInt(info.root.packages.'package'.classes.'class'.'@branch-rate'*.toDouble().min().toString())
	
	def ratesMap = [
			totallinerate:info.totalLineRate,
			totalbranchrate:info.totalBranchRate,
			packagelinerate:info.minPackageLineRate,
			packagebranchrate:info.minPackageBranchRate,
			linerate:info.minClassLineRate,
			branchrate:info.minClassBranchRate
	]
	
	/*
	 * Do a check with all the values - this should pass
	 */
	ant.echo(message:"Doing a check with ${ratesMap} - it should pass")
	def tempMap = [datafile:datafile]
	tempMap.putAll(ratesMap)
	ant."${checkTaskName}"(tempMap)

	/*
	 * Now do a check with each rate individually.   Add one to make sure it fails.
	 * 
	 * For those rates that equal 100, there is no way the check can fail, so do
	 * a check to make sure it passes.
	 */
	ratesMap.each { rateType, rateValue ->
		if (rateValue == 100) {
			ant.echo(message:"Doing a check with ${rateType}=100 - it should pass")
			ant."${checkTaskName}"(datafile:datafile, (rateType):rateValue)
		} else {
			ant.echo(message:"Doing a check with ${rateType}=${rateValue+1} - it should fail")
			assertCheckFailure("Check of ${rateType} with value ${rateValue + 1} should have failed") {
				ant."${checkTaskName}"(datafile:datafile, (rateType):rateValue + 1)
			}
		}
	}
	
	if (!ratesMap.values().any { it < 50 }) {
		ant.fail(message:"This test is expecting one rate to be under 50.   Otherwise the following test will fail.")
	}
	 
	/*
	 * Now do a check that does not specify any rates.   They should default to 50%.
	 * This should fail.
	 */
	ant.echo(message:"Doing a check using the defaults - it should fail")
	assertCheckFailure("Check with defaults should fail") {
		 ant."${checkTaskName}"(datafile:datafile)
	 }
	 
	/*
	 * Now pass in all 0 values.   It should pass.
	 */
	ratesMap.keySet().each { tempMap.put(it, 0) }
	ant.echo(message:"Doing a check with all zero rates: ${tempMap}")
	ant."${checkTaskName}"(tempMap)
	
}
 
 
rateToInt = { doubleString ->
	doubleString = doubleString.toString()    //in case it is not a string
	//multiply rate by 100 and drop the decimals
	(Double.parseDouble(doubleString) * 100.0).intValue()
}
 
 
assertCheckFailure = { errorMessage, closure ->
	boolean failed = false
	try {
		closure.call()
	} catch (BuildException e) {
		failed = true
	}
	if (!failed) {
		ant.fail(message:errorMessage)
	}
}

calculateRate = { covered, value ->
	if (value == 0)
	{
		return 100
	}
	rateToInt(covered/value)
}
 
 
 
 
 
 
