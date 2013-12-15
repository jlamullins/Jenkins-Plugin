package de.esailors.jenkins.teststability;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import hudson.tasks.junit.TestResult;
import hudson.tasks.junit.CaseResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import de.esailors.jenkins.teststability.TestHistory.TestHistoryData;

public class StabilityTestHistoryTest {
	private TestResult r;
	private TestHistory testHist;
	private TestHistory testHistTestSize;
	private List<CaseResult> caseResults;
	private int startBuild = 0;
	private int endBuild = 50;

	@Before
	public void testSetup() {
		r = TestHistoryUtil.getDummyTestResult();
		caseResults = TestHistoryUtil.getAllCaseResults(r);
		testHist = TestHistoryUtil.getDummyTestHistory(1, 20);
		testHistTestSize = TestHistoryUtil.getDummyTestHistory(1, 2);
	}
	
	@Test
	public void testGetDuration() {
		TestHistory testHist = null;
		for (CaseResult caseResult : caseResults) {
			if (caseResult.getName().equals("testAddBook")) {
				testHist = new TestHistory(caseResult,
						new HashMap<Integer, TestHistoryData>(), startBuild, endBuild);
			}
		}
		assertEquals(5.0, testHist.getCaseResult().getDuration(), 0.1);
	}

	@Test
	public void testGetAllCaseResults() {
		assertEquals(3.0, caseResults.size(), 0.1);
	}

	@Test
	public void testGetCaseName() {
		TestHistory history = null;
		for (CaseResult caseResult : caseResults) {
			if (caseResult.getName().equals("testAddBook")) {
				history = new TestHistory(caseResult,
						new HashMap<Integer, TestHistoryData>(), startBuild, endBuild);
			}
		}
		assertEquals("test case name should be testAddBook", "testAddBook",
				history.getName());
	}

	@Test
	public void testGetResult() {
		assertTrue(testHist.getResult(2));
		assertFalse(testHist.getResult(3));
	}
	
	@Test
	public void testGetTestHistory() {
		Map<Integer, TestHistoryData> historyMap = testHistTestSize.getFilteredTestHistory();
		assertEquals(2, historyMap.size());
		assertEquals("sampleTestCase", historyMap.get(1).testCase.getName());
		assertEquals("testAddBook", historyMap.get(2).testCase.getName());
	}
}