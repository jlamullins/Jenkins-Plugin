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

import hudson.tasks.junit.TestAction;
import hudson.tasks.junit.TestObject;
import hudson.tasks.junit.TestResultAction.Data;
import hudson.tasks.junit.CaseResult;
import hudson.tasks.junit.ClassResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jenkins.model.Jenkins;

/**
 * {@link Data} for the test stability history.
 * 
 * @author ckutz
 */
@SuppressWarnings("deprecation")
class StabilityTestData extends Data {

	static {
		Jenkins.XSTREAM2.aliasType("circularStabilityHistory",
				CircularStabilityHistory.class);
	}

	private final Map<String, CircularStabilityHistory> stability;
	private final Map<String, TestHistory> testHistory;// caseName, testHistory

	public StabilityTestData(
			Map<String, CircularStabilityHistory> stabilityHistory) {
		this.stability = stabilityHistory;
		this.testHistory = new HashMap<String, TestHistory>(); 
	}

	/**
	 * StabilityTestData constructor. Initializes the CircularStabilityHistory
	 * and map of TestHistory
	 * 
	 * @author dzhang29
	 * @author jmullin2
	 * @param stabilityHistory
	 * @param testHist
	 */
	public StabilityTestData(
			Map<String, CircularStabilityHistory> stabilityHistory,
			Map<String, TestHistory> testHist) {
		this.stability = stabilityHistory;
		this.testHistory = testHist;
	}

	/**
	 * Returns a list of StabilityTestAction. If the testObject is a CaseResult
	 * testHistory is initialized.
	 * 
	 * @param testObject
	 * @return List
	 */
	@Override
	public List<? extends TestAction> getTestAction(TestObject testObject) {
		if (testObject instanceof CaseResult
				|| testObject instanceof ClassResult) {
			CircularStabilityHistory ringBuffer = stability.get(testObject
					.getId());
			if (testObject instanceof CaseResult) {
				return Collections.singletonList(new StabilityTestAction(
						ringBuffer, testHistory.get(testObject.getName())));
			} else {
				ArrayList<TestAction> ret_col = new ArrayList<TestAction>();
				ret_col.add(new StabilityTestAction(ringBuffer));
				ret_col.add(new CommitResultAction(ringBuffer)); 
				return Collections.unmodifiableList(ret_col);
			}
		}
		return Collections.emptyList();
	}

	public static class Result {
		int buildNumber;
		boolean passed;
		CommitResult cr = null; // Added for commit history per test

		public Result(int buildNumber, boolean passed) {
			super();
			this.buildNumber = buildNumber;
			this.passed = passed;
		}

		/***
		 * Constructor added for commit history per test
		 * 
		 * @author sunakim1
		 * @author mschmid2
		 */
		public Result(int buildNumber, boolean passed, CommitResult cr) {
			super();
			this.buildNumber = buildNumber;
			this.passed = passed;
			this.cr = cr;
		}

		public CommitResult getCommitResult() {
			return this.cr;
		}
	}

}
