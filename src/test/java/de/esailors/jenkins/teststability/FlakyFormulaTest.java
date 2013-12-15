package de.esailors.jenkins.teststability;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;

public class FlakyFormulaTest {
	
	/** 
	 * Get the test result from the computeFlakiness class
	 * Checks if the expectedFlakiness matches with the computed flakiness
	 * 
	 * @author mschmid2
	 * @author gandhi23
	 * @param results[]
	 * @param expectedFlakiness
	 * @return ArrayList<Boolean> to pass into
	 *         TestHistoryUtil.computeFlakinessFromBoolList(.)
	 */
	private boolean singularTestCase(Boolean[] results, int expectedFlakiness){
		
		ArrayList<Boolean> pfResultList = new ArrayList<Boolean>(Arrays.asList(results));
		int computedFlakiness = TestHistoryUtil.computeFlakinessFromBoolList(pfResultList);
		return (expectedFlakiness == computedFlakiness);
	}

	/**
	 * The flakiness should be zero when a null input is passes.
	 * 
	 * @author mschmid2
	 * @author gandhi23
	 */
	@Test
	public void nullTest() throws Exception {
		assertEquals(0, TestHistoryUtil.computeFlakiness(null));
	}

	/**
	 * The flakiness formula should return zero when an empty list is passed.
	 * 
	 * @author mschmid2
	 * @author gandhi23
	 */
	@Test
	public void checkEmptyListIsZero() {
		Boolean[] emptyPFData = {};
		assertTrue(singularTestCase(emptyPFData, 0));
	}

	/**
	 * The flakiness formula should return zero when a list with one element is
	 * passed.
	 * 
	 * @author mschmid2
	 * @author gandhi23
	 */
	@Test
	public void checkSingleElementListIsZero() {

		Boolean[] singleElementPFData = { true };
		assertTrue(singularTestCase(singleElementPFData, 0));
	}

	/*
	 * This test was worked on with Prof. Darko.
	 * The pair and the prof tried to mock core jenkins test result data holder object
	 * unfortunately we weren't successful in mocking the tests
	@author gandhi23
	@author mschimid2
	@author marinov
	@Test
	public void testNew() throws Exception {
		TestResult t = new TestResult();
		t.parse(new File("/tmp/foo.xml"));
		t.tally();
		t.freeze(null);
		Collection<SuiteResult> l = t.getSuites();
		
		for (SuiteResult s : l) {
			System.out.println(s.getId());
			System.out.println(s.getPreviousResult());
		}
		
//		Method m = SuiteResult.class.getDeclaredMethod("parse", new Class[]{File.class, boolean.class});
//		m.setAccessible(true);
//		List<SuiteResult> l = (List<SuiteResult>)m.invoke(null, new File("/tmp/foo.xml"), false);
//		SuiteResult s0 = l.get(0);
//		SuiteResult s1 = l.get(1);
//		Method p = SuiteResult.class.getDeclaredMethod("setParent", new Class[]{hudson.tasks.junit.TestResult.class});
//		p.setAccessible(true);
//		p.invoke(s1, s0);
//		
//		System.out.println(l.get(0).getId());
//		System.out.println(l.get(1).getPreviousResult().getId());
	}
	*/
	
	/** 
	 * The flakiness should be zero when all tests have the same pass/fail values.
	 * 
	 * @author mschimid2
	 * 
	 * @author marinov
	 * 
	 * @Test public void testNew() throws Exception { TestResult t = new
	 * TestResult(); t.parse(new File("/tmp/foo.xml")); t.tally();
	 * t.freeze(null); Collection<SuiteResult> l = t.getSuites();
	 * 
	 * for (SuiteResult s : l) { System.out.println(s.getId());
	 * System.out.println(s.getPreviousResult()); }
	 * 
	 * // Method m = SuiteResult.class.getDeclaredMethod("parse", new
	 * Class[]{File.class, boolean.class}); // m.setAccessible(true); //
	 * List<SuiteResult> l = (List<SuiteResult>)m.invoke(null, new
	 * File("/tmp/foo.xml"), false); // SuiteResult s0 = l.get(0); //
	 * SuiteResult s1 = l.get(1); // Method p =
	 * SuiteResult.class.getDeclaredMethod("setParent", new
	 * Class[]{hudson.tasks.junit.TestResult.class}); // p.setAccessible(true);
	 * // p.invoke(s1, s0); // // System.out.println(l.get(0).getId()); //
	 * System.out.println(l.get(1).getPreviousResult().getId()); }
	 a*/

	/**
	 * The flakiness should be zero when all tests have the same pass/fail
	 * values.
	 * 
	 * @author mschmid2
	 * @author gandhi23
	 */
	@Test
	public void testAllTrue() {

		Boolean[] allTrueArray = { true, true, true, true };
		assertTrue(singularTestCase(allTrueArray, 0));

	}

	/**
	 * The flakiness should be zero when all tests have the same pass/fail
	 * values.
	 * 
	 * @author mschmid2
	 * @author gandhi23
	 */
	@Test
	public void testAllFalse() {
		Boolean[] allFalseArray = { false, false, false, false, false };
		assertTrue(singularTestCase(allFalseArray, 0));
		
	}

	/**
	 * Check the flakiness formula over other, non-corner-case pass/fail data
	 * inputs.
	 * 
	 * @author mschmid2
	 * @author gandhi23
	 */
	@Test
	public void testOtherFlakinessFormulas() {

		Boolean[] testData1 = {true, false, true, false, false};
		assertTrue(singularTestCase(testData1, 75));
		
		Boolean[] testData2 = {false, true, false, false, false};
		assertTrue(singularTestCase(testData2, 50));
		
		// tests a change at the end of the list: 
		Boolean[] testData3 = {false, false, false, false, true};
		assertTrue(singularTestCase(testData3, 25));
	
		// tests a change at the beginning of the list: 
		Boolean[] testData4 = {true, false, false, false, false};
		assertTrue(singularTestCase(testData4, 25));
		
		Boolean[] testData5 = {true, true, false, false, true};
		assertTrue(singularTestCase(testData5, 50));
		
		
	}
}