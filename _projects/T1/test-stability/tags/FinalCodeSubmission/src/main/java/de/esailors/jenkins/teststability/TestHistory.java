package de.esailors.jenkins.teststability;

import hudson.tasks.junit.CaseResult;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TestHistory {

	/**
	 * Data class for storing the test history data (both CaseResult and
	 * CommitResult data).
	 * 
	 * @author mschmid2
	 * @author jmullin2
	 */
	public static class TestHistoryData {

		public CaseResult testCase;
		public CommitResult commitResult;
		public int flakiness;

		public TestHistoryData() {
			this.testCase = null;
			this.commitResult = null;
			this.flakiness = 4;
		}

		public TestHistoryData(CaseResult testResult, CommitResult cr) {
			this.testCase = testResult;
			this.commitResult = cr;
			this.flakiness = 1;
		}

		public TestHistoryData(CaseResult testResult, CommitResult cr,
				int flakiness) {
			this.testCase = testResult;
			this.commitResult = cr;
			this.flakiness = flakiness;
		}

	}

	private TestHistoryData testCaseData;
	private Map<Integer, TestHistoryData> testHistory; // build history
	private int startBuild;
	private int endBuild;

	/**
	 * Returns the name of this CaseResult
	 * 
	 * @author dzhang29
	 * @author jmullin2
	 * @return String
	 */
	public String getName() {
		return testCaseData.testCase.getName();
	}

	/**
	 * Returns this CaseResult
	 * 
	 * @author dzhang29
	 * @author jmullin2
	 * @return CaseResult
	 */
	public CaseResult getCaseResult() {
		return testCaseData.testCase;
	}

	/**
	 * Returns this CommitResult
	 * 
	 * @author mschmid2
	 * @author jmullin2
	 * @return CaseResult
	 */
	public CommitResult getCommitResult() {
		return testCaseData.commitResult;
	}

	/**
	 * Returns the result of this test case for the given build id. True if
	 * passed.
	 * 
	 * @author dzhang29
	 * @author jmullin2
	 * @param build_id
	 * @return Boolean
	 */
	public Boolean getResult(int build_id) {
		CaseResult result = testHistory.get(build_id).testCase;
		return result.isPassed();
	}

	/**
	 * Returns a map of this test history. The build number for the key, the
	 * CaseResult for the value.
	 * 
	 * @author dzhang29
	 * @author jmullin2
	 * @return Map<Integer, CaseResult>
	 */
	public Map<Integer, TestHistoryData> getFilteredTestHistory() {
		Set<Integer> keySet = testHistory.keySet();
		Map<Integer, TestHistoryData> returnMap = new HashMap<Integer, TestHistoryData>();
		for (Integer key : keySet) {
			if (key >= startBuild && key <= endBuild) {
				returnMap.put(key, testHistory.get(key));
			}
		}
		return returnMap;
	}

	public Map<Integer, TestHistoryData> getAllTestHistory() {
		return testHistory;
	}

	/**
	 * Constructor for TestHistory
	 * 
	 * @author dzhang29
	 * @author jmullin2
	 * @param CaseResult
	 * @param Map
	 *            <Integer, CaseResult>
	 */
	public TestHistory(CaseResult result,
			Map<Integer, TestHistoryData> testHist, int startBuild, int endBuild) {
		this.testCaseData = new TestHistoryData(result, null);
		this.testHistory = testHist;
		this.startBuild = startBuild;
		this.endBuild = endBuild;
	}

	/**
	 * Constructor for TestHistory
	 * 
	 * @author mschmid2
	 * @author jmullin2
	 * @param CaseResult
	 * @param Map
	 * <Integer, CaseResult>
	 */
	public TestHistory(CaseResult result, CommitResult cr,
			Map<Integer, TestHistoryData> testHist, int startBuild, int endBuild) {
		this.testCaseData = new TestHistoryData(result, cr);
		this.testHistory = testHist;
		this.startBuild = startBuild;
		this.endBuild = endBuild;
	}

}
