package de.esailors.jenkins.teststability;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import hudson.tasks.junit.SuiteResult;
import hudson.tasks.junit.TestResult;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;

import de.esailors.jenkins.teststability.StabilityTestData.Result;

public class CommitHistoryTest {

	private File getDataFile(String name) throws URISyntaxException {
		// return new File(TestCase.class.getResource(name).toURI());
		return new File(name);
	}

	@Test
	public void getTestList() throws Exception {

		TestResult testResult = new TestResult();
		testResult
				.parse(getDataFile("testResultXMLExample.xml"));
		Collection<SuiteResult> suites = testResult.getSuites();
		assertEquals("Wrong number of test suites", 1, suites.size());
		int testCaseCount = 0;
		for (SuiteResult suite : suites) {
			testCaseCount += suite.getCases().size();
		}
		assertEquals("Wrong number of test cases", 2, testCaseCount);

	}

	@Test
	public void noNullReturnFromHistory() {
		CircularStabilityHistory ringBuffer = new CircularStabilityHistory(10);
		ringBuffer.add(new Result(0, true, new CommitResult()));
		CommitResultAction testAction = new CommitResultAction(ringBuffer);
		assertNotNull(testAction.getCommitResults());
	}

	@Test
	public void returnOnlyValidCommitResults() {
		CircularStabilityHistory ringBuffer = new CircularStabilityHistory(10);
		CommitResult cr1 = new CommitResult("debug");
		CommitResult cr2 = new CommitResult("debug2");

		// Not Valid
		ringBuffer.add(new Result(0, true, new CommitResult()));
		// Valid
		ringBuffer.add(new Result(0, true, cr1));
		// Not Valid
		ringBuffer.add(new Result(0, true, new CommitResult()));
		// Valid
		ringBuffer.add(new Result(0, true, cr2));
		CommitResultAction testAction = new CommitResultAction(ringBuffer);

		ArrayList<CommitResult> commitResultsList = new ArrayList<CommitResult>();
		// Debugging Message
		commitResultsList.add(cr1);
		commitResultsList.add(cr2);
		assertEquals(commitResultsList, testAction.getCommitResults());
	}

	@Test
	public void testCommitResultFields() {

		int buildNumber = 10;
		String ID = "buildID";
		String author = "Build Author";
		String msg = "Commit Msg";
		long time = 1000000000000L;
		CommitResult cr = new CommitResult(buildNumber, ID, author, msg, time);

		CircularStabilityHistory ringBuffer = new CircularStabilityHistory(10);
		ringBuffer.add(new Result(0, true, cr));

		Result[] cshResultsList = ringBuffer.getData();
		assertEquals(1, cshResultsList.length);

		CommitResult crData = cshResultsList[0].cr;
		assertTrue(crData.isValid);
		assertEquals(buildNumber, crData.buildNumber);
		assertTrue(ID.equals(crData.ID));
		assertTrue(author.equals(crData.author));
		assertTrue(msg.equals(crData.msg));
		assertEquals(time, crData.time);
	}

	@Test
	public void commitHistoryTable() {
		CircularStabilityHistory ringBuffer = new CircularStabilityHistory(10);

		CommitResult cr1 = new CommitResult("debug");
		CommitResult cr2 = new CommitResult("debug2");
		ringBuffer.add(new Result(0, true, cr2));
		ringBuffer.add(new Result(0, true, cr1));
		CommitResultAction testAction = new CommitResultAction(ringBuffer);

		String htmlResult = testAction.getCommitResultsHTML();
		String expectedResult = "<table border=\"1\"><tr><td>ID</td><td>Author</td><td>Message</td><td>Timestamp</td><td>Build Number</td></tr><tr><td>N/A</td><td>N/A</td><td>debug2</td><td>0</td><td>0</td></tr><tr><td>N/A</td><td>N/A</td><td>debug</td><td>0</td><td>0</td></tr></table>";
		assertEquals(expectedResult, htmlResult);

	}

	@Test
	public void commitResultTimeStampCheck() {
		int buildNumber = 10;
		String ID = "buildID";
		String author = "Build Author";
		String msg = "Commit Msg";
		long time = 1000000000000L;
		CommitResult cr = new CommitResult(buildNumber, ID, author, msg, time);
		CircularStabilityHistory ringBuffer = new CircularStabilityHistory(10);
		ringBuffer.add(new Result(0, true, cr));

		CommitResultAction testAction = new CommitResultAction(ringBuffer);
		ArrayList<CommitResult> crArray = testAction.getCommitResults();
		assertEquals("Timestamp got corrupted", time, crArray.get(0).time);
	}

}
