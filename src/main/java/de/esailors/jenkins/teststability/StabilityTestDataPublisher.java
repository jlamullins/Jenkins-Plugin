/*
 * The MIT License
 * 
 * Copyright (c) 2013, eSailors IT Solutions GmbH
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package de.esailors.jenkins.teststability;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.Descriptor;
import hudson.model.User;
import hudson.scm.EditType;
import hudson.scm.ChangeLogSet;
import hudson.scm.ChangeLogSet.AffectedFile;
import hudson.tasks.junit.PackageResult;
import hudson.tasks.junit.TestDataPublisher;
import hudson.tasks.junit.TestResult;
import hudson.tasks.junit.TestResultAction.Data;
import hudson.tasks.junit.ClassResult;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jenkins.model.Jenkins;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import de.esailors.jenkins.teststability.StabilityTestData.Result;
import de.esailors.jenkins.teststability.TestHistory.TestHistoryData;

/**
 * {@link TestDataPublisher} for the test stability history.
 * 
 * @author ckutz
 */
public class StabilityTestDataPublisher extends TestDataPublisher {

	public static final boolean DEBUG = false;

	@DataBoundConstructor
	public StabilityTestDataPublisher() {
	}

	/***
	 * get different file path with param as "Test" or "Run "Test" returns a
	 * file path of test directory "Run" returns a file path of Jenkins
	 * directory
	 * 
	 * @param type
	 * @return
	 */
	public static String getFilePath(String type) {
		String userHomeFolder = System.getProperty("user.home");
		if (type.equals("Test")) {
			return userHomeFolder;
		} else {
			return Jenkins.getInstance().getRootDir().getAbsolutePath();
		}

	}

	/***
	 * Write all cases and corresponding builds histories into a file
	 * 
	 * @param testHistoryMap
	 * @param path
	 * @param projectName
	 * @throws IOException
	 */
	public static void writeTestsHistoriesToFile(
			Map<String, TestHistory> testHistoryMap, String path,
			String projectName) throws IOException {
		File file = new File(path, projectName + "AllTestsHist.txt");
		BufferedWriter testsWriter = new BufferedWriter(new FileWriter(file));
		StringBuffer toWrite = new StringBuffer();
		Set<String> keys = testHistoryMap.keySet();
		for (String key : keys) {
			TestHistory testHist = testHistoryMap.get(key);
			Map<Integer, TestHistoryData> caseTestHistory = testHist
					.getFilteredTestHistory();
			Set<Integer> buildNumbers = caseTestHistory.keySet();

			for (int buildNum : buildNumbers) {
				boolean result = testHist.getResult(buildNum);
				toWrite.append(key + "\t" + buildNum + "\t" + result + "\n");
			}
		}
		testsWriter.write(toWrite.toString());
		testsWriter.flush();
		testsWriter.close();
	}

	@Override
	public Data getTestData(AbstractBuild<?, ?> build, Launcher launcher,
			BuildListener listener, TestResult testResult) throws IOException,
			InterruptedException {
		
		Map<String, CircularStabilityHistory> stabilityHistoryPerTest = new HashMap<String, CircularStabilityHistory>();
		// Creating a map of TestHistory results with the case name as the key
		int startBuild = getDescriptor().getStartBuild();
		int endBuild = getDescriptor().getEndBuild();
		Map<String, TestHistory> testHistoryMap = TestHistoryUtil
				.buildTestHistory(build, listener, testResult, startBuild,
						endBuild);
		Collection<hudson.tasks.test.TestResult> classAndCaseResults = getClassAndCaseResults(testResult);
		debug("Found " + classAndCaseResults.size() + " test results", listener);
		StringBuffer resultInfo = new StringBuffer();
		for (hudson.tasks.test.TestResult result : classAndCaseResults) {
			resultInfo.append(result.toPrettyString());
			CircularStabilityHistory history = getPreviousHistory(result);
			
			// If there was previous history, simply adds current test result to the history.
			// Otherwise, build up a test history from the current test result.
			if (history != null ) {
				if (result.isPassed()) {
					history.add(build.getNumber(), true, new CommitResult(
							result));
				} else if (result.getFailCount() > 0) {
					history.add(build.getNumber(), false, new CommitResult(
							result));
				}
				stabilityHistoryPerTest.put(result.getId(), history);

			} else if (isFirstTestFailure(result, history)) {
				debug("Found failed test " + result.getId(), listener);
				int maxHistoryLength = getDescriptor().getMaxHistoryLength();
				CircularStabilityHistory ringBuffer = new CircularStabilityHistory(
						maxHistoryLength);
				buildUpInitialHistoryWithCommitHistory(ringBuffer, result,
						maxHistoryLength - 1);
				ringBuffer.add(build.getNumber(), false, new CommitResult(
						result));
				stabilityHistoryPerTest.put(result.getId(), ringBuffer);
			} else {
				int maxHistoryLength = getDescriptor().getMaxHistoryLength();
				CircularStabilityHistory ringBuffer = new CircularStabilityHistory(
						maxHistoryLength);
				buildUpInitialHistoryWithCommitHistory(ringBuffer, result,
						maxHistoryLength - 1);
				ringBuffer.add(build.getNumber(), true, new CommitResult(
						result));
				stabilityHistoryPerTest.put(result.getId(), ringBuffer);
			}
		}

		StabilityTestDataPublisher.writeTestsHistoriesToFile(testHistoryMap,
				getFilePath("Run"), build.getProject().getName());
		return new StabilityTestData(stabilityHistoryPerTest, testHistoryMap);
	}

	private void debug(String msg, BuildListener listener) {
		if (StabilityTestDataPublisher.DEBUG) {
			listener.getLogger().println(msg);
		}
	}

	private CircularStabilityHistory getPreviousHistory(
			hudson.tasks.test.TestResult result) {
		hudson.tasks.test.TestResult previous = getPreviousResult(result);

		if (previous != null) {
			StabilityTestAction previousAction = previous
					.getTestAction(StabilityTestAction.class);
			if (previousAction != null) {
				CircularStabilityHistory prevHistory = previousAction
						.getRingBuffer();

				if (prevHistory == null) {
					return null;
				}

				// copy to new to not modify the old data
				CircularStabilityHistory newHistory = new CircularStabilityHistory(
						getDescriptor().getMaxHistoryLength());
				newHistory.addAll(prevHistory.getData());
				return newHistory;
			}
		}
		return null;
	}

	private boolean isFirstTestFailure(hudson.tasks.test.TestResult result,
			CircularStabilityHistory previousRingBuffer) {
		return previousRingBuffer == null && result.getFailCount() > 0;
	}

	/**
	 * Builds up test history by traversing back to previous test result
	 * with getPreviousResult method. 
	 * Original from test-stability with added commit result information.
	 * 
	 * modified by sunakim1 & mschmid2
	 * @param ringBuffer
	 * @param result
	 * @param number
	 */
	private void buildUpInitialHistoryWithCommitHistory(
			CircularStabilityHistory ringBuffer,
			hudson.tasks.test.TestResult result, int number) {
		List<Result> testResultsFromNewestToOldest = new ArrayList<Result>(
				number);
		hudson.tasks.test.TestResult previousResult = getPreviousResult(result);
		while (previousResult != null) {
			CommitResult cr = new CommitResult(previousResult);
			testResultsFromNewestToOldest.add(new Result(previousResult
					.getOwner().getNumber(), previousResult.isPassed(), cr));
			previousResult = previousResult.getPreviousResult();
		}

		for (int i = testResultsFromNewestToOldest.size() - 1; i >= 0; i--) {
			ringBuffer.add(testResultsFromNewestToOldest.get(i));
		}
	}

	private hudson.tasks.test.TestResult getPreviousResult(
			hudson.tasks.test.TestResult result) {
		try {
			return result.getPreviousResult();
		} catch (RuntimeException e) {
			// there's a bug (only on freestyle builds!) that getPreviousResult
			// may throw a NPE (only for ClassResults!) in Jenkins 1.480
			// Note: doesn't seem to occur anymore in Jenkins 1.520
			// Don't know about the versions between 1.480 and 1.520

			// TODO: Untested:
			// if (result instanceof ClassResult) {
			// ClassResult cr = (ClassResult) result;
			// PackageResult pkgResult = cr.getParent();
			// hudson.tasks.test.TestResult topLevelPrevious =
			// pkgResult.getParent().getPreviousResult();
			// if (topLevelPrevious != null) {
			// if (topLevelPrevious instanceof TestResult) {
			// TestResult junitTestResult = (TestResult) topLevelPrevious;
			// PackageResult prvPkgResult =
			// junitTestResult.byPackage(pkgResult.getName());
			// if (pkgResult != null) {
			// return pkgResult.getClassResult(cr.getName());
			// }
			// }
			// }
			//
			// }

			return null;
		}
	}

	private Collection<hudson.tasks.test.TestResult> getClassAndCaseResults(
			TestResult testResult) {
		List<hudson.tasks.test.TestResult> results = new ArrayList<hudson.tasks.test.TestResult>();

		Collection<PackageResult> packageResults = testResult.getChildren();
		for (PackageResult pkgResult : packageResults) {
			Collection<ClassResult> classResults = pkgResult.getChildren();
			for (ClassResult cr : classResults) {
				results.add(cr);
				results.addAll(cr.getChildren());
			}
		}

		return results;
	}
	
	@Override
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl) super.getDescriptor();
	}

	@Extension
	public static class DescriptorImpl extends Descriptor<TestDataPublisher> {

		private int maxHistoryLength = 30;
		private int startBuild = 0;
		private int endBuild = 100;

		@Override
		public boolean configure(StaplerRequest req, JSONObject json)
				throws FormException {
			this.maxHistoryLength = json.getInt("maxHistoryLength");
			this.startBuild = json.getInt("startBuild");
			this.endBuild = json.getInt("endBuild");
			save();
			return super.configure(req, json);
		}

		public int getMaxHistoryLength() {
			return this.maxHistoryLength;
		}

		@Override
		public String getDisplayName() {
			return "Test stability history";
		}

		public int getEndBuild() {
			return this.endBuild;
		}

		public int getStartBuild() {
			return this.startBuild;
		}
	}
}
