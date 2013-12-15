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

import hudson.model.HealthReport;
import hudson.tasks.junit.TestAction;
import hudson.tasks.junit.CaseResult;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import javax.annotation.CheckForNull;

import org.jvnet.localizer.Localizable;

import de.esailors.jenkins.teststability.StabilityTestData.Result;
import de.esailors.jenkins.teststability.TestHistory.TestHistoryData;

/**
 * {@link TestAction} for the test stability history.
 * 
 * @author ckutz
 */
class StabilityTestAction extends TestAction {

	private CircularStabilityHistory ringBuffer;

	private String description;
	private int total;
	private int failed;
	private int testStatusChanges;
	private int stability = 100;
	private int flakiness;
	private TestHistory testHistory;

	/**
	 * Returns this testHistory
	 * 
	 * @author dzhang29
	 * @author jmullin2
	 * @return TestHistory
	 */
	public TestHistory getTestHistory() {
		return testHistory;
	}

	public StabilityTestAction(@CheckForNull CircularStabilityHistory ringBuffer) {
		this.ringBuffer = ringBuffer;

		if (ringBuffer != null) {
			Result[] data = ringBuffer.getData();
			this.total = data.length;

			computeStability(data);
			computeFlakiness(data);
		}

		if (this.stability == 100) {
			this.description = "No known failures. Flakiness 0%, Stability 100%";
		} else {
			this.description = String
					.format("Failed %d times in the last %d runs. Flakiness: %d%%, Stability: %d%%",
							failed, total, flakiness, stability);
		}
	}

	/**
	 * Constructor for StabilityTestAction. Initializes the
	 * CircularStabilityHistory, TestHistory, and CommitHistory
	 * 
	 * @author dzhang29
	 * @author jmullin2
	 * @param ringBuffer
	 * @param testHistory
	 */
	public StabilityTestAction(
			@CheckForNull CircularStabilityHistory ringBuffer,
			TestHistory testHistory) {
		this.ringBuffer = ringBuffer;

		if (ringBuffer != null) {
			Result[] data = ringBuffer.getData();
			this.total = data.length;

			computeStability(data);
			computeFlakiness(data);
		}

		if (this.stability == 100) {
			this.description = "No known failures. Flakiness 0%, Stability 100%";
		} else {
			this.description = String
					.format("Failed %d times in the last %d runs. Flakiness: %d%%, Stability: %d%%",
							failed, total, flakiness, stability);
		}

		// Initialize the testHistory.
		this.testHistory = testHistory;
	}

	/**
	 * Writes the TestHistory to a file. Can be used for testing. Result is in
	 * the user's home directory
	 * 
	 * @author dzhang29
	 * @author jmullin2
	 * @param testHist
	 */
	private void writeInfoToFile(TestHistory testHist) {
		try {
			DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
			Date date = new Date();
			String userHomeFolder = System.getProperty("user.home");
			File file = new File(userHomeFolder, "testHistoryFile"
					+ dateFormat.format(date) + ".txt");
			BufferedWriter output = new BufferedWriter(new FileWriter(file));
			Map<Integer, TestHistoryData> testHistoryMap = testHist
					.getFilteredTestHistory();
			Set<Integer> keySet = testHistoryMap.keySet();
			output.write(testHist.getName());
			for (Integer key : keySet) {
				output.write("Key: " + key + "\n");
				TestHistoryData thd = testHistoryMap.get(key);
				CaseResult cr = thd.testCase;
				output.write(cr.toPrettyString() + "\n");
			}
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void computeStability(Result[] data) {
		if(total == 0){ this.stability = 0; return;}
		for (Result r : data) {
			if (!r.passed) {
				failed++;
			}
		}
		this.stability = 100 * (total - failed) / total;
	}

	/**
	 * Computes the flakiness based on the ringbuffer in data
	 * Relays the call to TestHistoryUtil.computeFlakinessFromBoolList for actual calculation
	 * 
	 * @author gandhi23
	 * @author mschmid2
	 * @param data
	 */
	private void computeFlakiness(Result[] data) {
		if(data == null){this.flakiness = 0;return;}
		ArrayList<Boolean> resultList = new ArrayList<Boolean>();

		for (Result r : this.ringBuffer.getData()) {
			boolean thisPassed = r.passed;
			resultList.add(thisPassed);
		}

		this.flakiness = TestHistoryUtil.computeFlakinessFromBoolList(resultList);
	}

	public int getFlakiness() {
		return this.flakiness;
	}

	public String getBigImagePath() {
		HealthReport healthReport = new HealthReport(100 - flakiness,
				(Localizable) null);
		return healthReport.getIconUrl("32x32");
	}

	public String getSmallImagePath() {
		HealthReport healthReport = new HealthReport(100 - flakiness,
				(Localizable) null);
		return healthReport.getIconUrl("16x16");
	}

	public CircularStabilityHistory getRingBuffer() {
		return this.ringBuffer;
	}

	public String getDescription() {
		return this.description;
	}

	public String getIconFileName() {
		return null;
	}

	public String getDisplayName() {
		return null;
	}

	public String getUrlName() {
		return null;
	}

	private static String getFlakinessCellData(int flakiness) {
		HealthReport healthReport = new HealthReport(100 - flakiness,
				(Localizable) null);
		String icon_url = healthReport.getIconUrl("16x16");
		String return_data = Integer.toString(flakiness) + "%<img src=\""
				+ icon_url + "\">";
		return return_data;
	}

}