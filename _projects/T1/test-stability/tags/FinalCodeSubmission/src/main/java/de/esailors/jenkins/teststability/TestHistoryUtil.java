package de.esailors.jenkins.teststability;

import hudson.XmlFile;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.tasks.junit.SuiteResult;
import hudson.tasks.junit.TestResult;
import hudson.tasks.junit.CaseResult;
import hudson.util.HeapSpaceStringConverter;
import hudson.util.XStream2;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.thoughtworks.xstream.XStream;

import de.esailors.jenkins.teststability.TestHistory.TestHistoryData;

public class TestHistoryUtil {

	final static XStream XSTREAM = new XStream2();

	/**
	 * Gets the previous history for the Case Result
	 * 
	 * @author dzhang29
	 * @author jmullin2
	 * @param currentResult
	 * @return TestHistory
	 */
	public static TestHistory getPreviousCaseHistory(CaseResult currentResult) {
		try {
			CaseResult previous = currentResult.getPreviousResult();
			if (previous != null) {
				StabilityTestAction previousAction = previous
						.getTestAction(StabilityTestAction.class);
				if (previousAction != null) {
					TestHistory prevHistory = previousAction.getTestHistory();

					if (prevHistory == null) {
						return null;
					}
					return prevHistory;
				}
			}
		} catch (RuntimeException e) {
			System.out.println(e.toString());
		}
		return null;
	}

	/**
	 * Builds up the initial history for the given Case Result.
	 * 
	 * @author dzhang29
	 * @author jmullin2
	 * @param caseResult
	 * @return Map<Integer, CaseResult>
	 */
	public static Map<Integer, TestHistoryData> buildInitialHistory(
			CaseResult caseResult) {
		Map<Integer, TestHistoryData> history = new HashMap<Integer, TestHistoryData>();
		CaseResult previousResult = caseResult.getPreviousResult();
		while (previousResult != null) {
			TestHistory.TestHistoryData previousData = new TestHistory.TestHistoryData(
					previousResult, null);
			history.put(previousResult.getOwner().getNumber(), previousData);
			previousResult = previousResult.getPreviousResult();
		}
		return history;
	}

	/**
	 * Creates a map of Case Name and TestHistory for the given TestResult
	 * object.
	 * 
	 * @author dzhang29
	 * @author jmullin2
	 * @param build
	 * @param listener
	 * @param result
	 * @return Map<String, TestHistory>
	 */
	public static Map<String, TestHistory> buildTestHistory(
			AbstractBuild<?, ?> build, BuildListener listener,
			TestResult result, int startBuild, int endBuild) {
		Map<String, TestHistory> testHistoryMap = new HashMap<String, TestHistory>();
		List<CaseResult> caseResults = getAllCaseResults(result);
		for (CaseResult caseResult : caseResults) {
			Map<Integer, TestHistoryData> history = buildCaseHistory(
					build.getNumber(), caseResult);
			TestHistory testHist = new TestHistory(caseResult, history,
					startBuild, endBuild);
			testHistoryMap.put(caseResult.getName(), testHist);
		}
		return testHistoryMap;
	}

	/**
	 * Creates a map of the build history for the given Case Result, the key in
	 * the map is the build number.
	 * 
	 * @author dzhang29
	 * @author jmullin2
	 * @param buildNumber
	 * @param currentResult
	 * @return Map<Integer, CaseResult>
	 */
	public static Map<Integer, TestHistoryData> buildCaseHistory(
			int buildNumber, CaseResult currentResult) {
		TestHistory previousHistory = getPreviousCaseHistory(currentResult);
		int flakiness = computeFlakiness(currentResult);
		TestHistory.TestHistoryData currentData = new TestHistory.TestHistoryData(
				currentResult, null, flakiness);
		Map<Integer, TestHistoryData> history;
		if (previousHistory != null) {
			history = previousHistory.getAllTestHistory();
		} else {
			history = buildInitialHistory(currentResult);
		}
		history.put(buildNumber, currentData);
		return history;
	}

	/**
	 * Unmarshalls the test results from the CaseResult object into a list
	 * passes the list which has test results for all the builds prior to the
	 * build that is passed in makes a call to compute_flakiness with the list
	 * of test results
	 * 
	 * @author gandhi23
	 * @author mschmid2
	 * @param currentResult
	 * @return int
	 */
	public static int computeFlakiness(CaseResult currentResult) {
		if (currentResult == null) {
			return 0;
		}

		CaseResult prevResult = currentResult.getPreviousResult();
		ArrayList<Boolean> resultList = new ArrayList<Boolean>();
		while (prevResult != null) {
			resultList.add(prevResult.isPassed());
			prevResult = prevResult.getPreviousResult(); // keep going back
		}
		return computeFlakinessFromBoolList(resultList);
	}

	/**
	 * Computes the flakiness of the test result list passed in computiation is
	 * done based on the flip-flopyness of the test results list passed it is
	 * assumed that the tests passed in are in build-sorted order
	 * 
	 * @author gandhi23
	 * @author mschmid2
	 * @param List
	 *            <Boolean>
	 * @return int
	 */
	public static int computeFlakinessFromBoolList(List<Boolean> resultList) {
		if (resultList == null || resultList.isEmpty() || resultList.size() == 1) {
			return 0;
		}
		int total = resultList.size(), testStatusChanges = 0;
		for (int i = 1; i < total; i++) {
			if (resultList.get(i - 1) != resultList.get(i)) {
				testStatusChanges++;
			}
		}
		return 100 * testStatusChanges / (total - 1);
	}

	/**
	 * Returns a list of all Case Results for a given Test Result
	 * 
	 * @author dzhang29
	 * @author jmullin2
	 * @param result
	 * @return List<CaseResult>
	 */
	public static List<CaseResult> getAllCaseResults(TestResult result) {
		List<CaseResult> caseResults = new ArrayList<CaseResult>();
		Collection<SuiteResult> suites = result.getSuites();
		for (SuiteResult suite : suites) {
			caseResults.addAll(suite.getCases());
		}
		return caseResults;
	}

	/**
	 * Creates a dummy TestHistory to be used in testing
	 */
	public static TestHistory getDummyTestHistory(int startBuild, int endBuild) {
		List<CaseResult> caseResults = TestHistoryUtil
				.getAllCaseResults(getDummyTestResult());
		Map<Integer, TestHistoryData> testHistoryMap = new HashMap<Integer, TestHistoryData>();
		int buildNumber = 1;
		for (CaseResult caseResult : caseResults) {
			TestHistory.TestHistoryData caseData = new TestHistory.TestHistoryData(
					caseResult, null);
			testHistoryMap.put(buildNumber, caseData);
			buildNumber++;
		}
		TestHistory testHist = new TestHistory(caseResults.get(0),
				testHistoryMap, startBuild, endBuild);
		return testHist;
	}

	/**
	 * Gets a Test Result that can be used in testing.
	 * 
	 * @return TestResult
	 */
	public static TestResult getDummyTestResult() {
		TestResult result;
		XSTREAM.alias("result", TestResult.class);
		XSTREAM.alias("suite", SuiteResult.class);
		XSTREAM.alias("case", CaseResult.class);
		XSTREAM.registerConverter(new HeapSpaceStringConverter(), 100);

		XmlFile junitXml = new XmlFile(XSTREAM, new File("junitResult.xml"));

		try {
			result = (TestResult) junitXml.read();
		} catch (IOException e) {
			System.out.println("Failed to load " + junitXml + "\n" + e);
			result = new TestResult(); // return a dummy
		}
		return result;
	}
}
