package de.esailors.jenkins.teststability;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

public class AllTestsHistoriesTest {

	Map<String, TestHistory> testHistMap = new HashMap<String, TestHistory>();

	@Before
	public void testSetup() throws IOException {
		TestHistory testHist1 = TestHistoryUtil.getDummyTestHistory(0, Integer.MAX_VALUE);
		TestHistory testHist2 = TestHistoryUtil.getDummyTestHistory(0, Integer.MAX_VALUE);
		
		testHistMap.put("case1", testHist1);
		testHistMap.put("case2", testHist2);
		
		StabilityTestDataPublisher.writeTestsHistoriesToFile(testHistMap,
				StabilityTestDataPublisher.getFilePath("Test"), "");
	}

	@Test
	public void readTestsFromFileTest() throws IOException {

		ArrayList<abbrResult> resultList = AllTestsHistories.readTestsFile(
				StabilityTestDataPublisher.getFilePath("Test"), "");
		assertEquals("case2", resultList.get(0).caseName);
		assertEquals("true", resultList.get(1).result);
	}

	@Test
	public void getAllBuildTest() throws IOException {
		AllTestsHistoriesAction allHistoryAction = new AllTestsHistoriesAction(
				AllTestsHistories.readTestsFile(
						StabilityTestDataPublisher.getFilePath("Test"), ""));
		ArrayList<Integer> builds = allHistoryAction.getAllBuildNum();
		assertEquals(new Integer(1), builds.get(0));
		assertEquals(new Integer(2), builds.get(1));
		assertEquals(new Integer(3), builds.get(2));
	}

	@Test
	public void getResultTest() throws IOException {
		AllTestsHistoriesAction allHistoryAction = new AllTestsHistoriesAction(
				AllTestsHistories.readTestsFile(
						StabilityTestDataPublisher.getFilePath("Test"), ""));
		assertEquals("N/A", allHistoryAction.getResult("case5", new Integer(1)));
		assertEquals("Pass",
				allHistoryAction.getResult("case1", new Integer(2)));

	}

	@AfterClass
	public static void testCleanUp() {
		File file = new File(StabilityTestDataPublisher.getFilePath("Test"),
				"AllTestsHist.txt");
		file.delete();
	}
}
