package de.esailors.jenkins.teststability;

import hudson.Extension;
import hudson.model.Action;
import hudson.model.TransientProjectActionFactory;
import hudson.model.AbstractProject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * New data structure that stores caseResult that is initialized from a file The
 * file is created when test data is collected by Jenkins Each abbrResult
 * represents one line in a file
 * 
 * @author dongyanzhang, minli
 * 
 */
class abbrResult {
	public String caseName = "";
	public String buildNum = "";
	public String result = "";

	/**
	 * ctor of abbrResult
	 * 
	 * @param pCaseName
	 * @param pBuildNum
	 * @param pResult
	 * @author dongyanzhang, minli
	 */
	public abbrResult(String pCaseName, String pBuildNum, String pResult) {
		caseName = pCaseName;
		buildNum = pBuildNum;
		result = pResult;
	}
}

/***
 * Support test history page on the left menu panel
 * 
 * @author dongyanzhang,minli
 * 
 */
@Extension
public class AllTestsHistories extends TransientProjectActionFactory {

	@Override
	public Collection<? extends Action> createFor(AbstractProject target) {
		try {
			ArrayList<abbrResult> results = readTestsFile(
					StabilityTestDataPublisher.getFilePath("Run"),
					target.getName());
			return Collections.singleton(new AllTestsHistoriesAction(results,
					target));
		} catch (IOException e) {
			return Collections.singleton(new AllTestsHistoriesAction(target));
		}
	}

	/**
	 * Read all test cases histories from a file
	 * 
	 * @param path
	 * @param projectName
	 * @return ArrayList<abbrResult> a list of all builds results of all cases
	 * @throws IOException
	 */
	public static ArrayList<abbrResult> readTestsFile(String path,
			String projectName) throws IOException {
		File file = new File(path, projectName + "AllTestsHist.txt");
		BufferedReader br = new BufferedReader(new FileReader(file));
		ArrayList<abbrResult> results = new ArrayList<abbrResult>();
		String s = null;
		while ((s = br.readLine()) != null) {
			String[] each = s.split("\t");
			results.add(new abbrResult(each[0], each[1], each[2]));
		}
		br.close();

		return results;
	}
}

class AllTestsHistoriesAction implements Action {

	public ArrayList<abbrResult> results;
	private AbstractProject<?, ?> project;

	/**
	 * ctor of AllTestsHistoriesAction
	 * 
	 * @param project
	 */
	AllTestsHistoriesAction(AbstractProject<?, ?> project) {
		this.project = project;
		this.results = null;

	}

	/**
	 * ctor of AllTestsHistoriesAction
	 * 
	 * @param pResult
	 * @param project
	 */
	public AllTestsHistoriesAction(ArrayList<abbrResult> pResult,
			AbstractProject<?, ?> project) {
		this.project = project;
		this.results = pResult;
	}

	public AllTestsHistoriesAction(ArrayList<abbrResult> pResults) {

		this.project = null;
		results = pResults;
	}

	/**
	 * get all the build numbers from the ArrayList<abbrResult>
	 * 
	 * @return ArrayList<Integer>
	 */
	public ArrayList<Integer> getAllBuildNum() {
		HashSet<String> buildSet = new HashSet<String>();
		for (abbrResult result : results) {
			buildSet.add(result.buildNum);
		}
		ArrayList<Integer> buildArray = new ArrayList<Integer>();
		for (String build : buildSet) {
			buildArray.add(Integer.parseInt(build));
		}
		Collections.sort(buildArray);
		return buildArray;
	}

	/**
	 * get all case names from ArrayList<abbrResult>
	 * 
	 * @return HashSet<String> all the case names
	 */
	public HashSet<String> getCaseNames() {
		HashSet<String> caseNames = new HashSet<String>();
		for (abbrResult result : results) {
			caseNames.add(result.caseName);
		}

		return caseNames;
	}

	public String getIconFileName() {
		return "notepad.png";
	}

	public String getDisplayName() {
		return "test history";
	}

	public String getUrlName() {
		return "test-history";
	}

	public String getResult(String pCaseName, Integer pBuildNum) {
		for (abbrResult abbrObject : results) {
			if (abbrObject.caseName.equals(pCaseName)
					&& abbrObject.buildNum.equals(pBuildNum.toString())) {
				if (abbrObject.result.equals("true")) {
					return "Pass";
				} else {
					return "Fail";
				}
			}
		}
		return "N/A";
	}

}
