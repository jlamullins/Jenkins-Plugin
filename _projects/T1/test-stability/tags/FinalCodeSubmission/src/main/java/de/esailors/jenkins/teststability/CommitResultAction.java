package de.esailors.jenkins.teststability;

import java.util.ArrayList;

import de.esailors.jenkins.teststability.StabilityTestData.Result;
import hudson.tasks.junit.TestAction;

public class CommitResultAction extends TestAction {
	private CircularStabilityHistory ringBuffer;
	private ArrayList<CommitResult> commitResultsList = null;

	public CommitResultAction(CircularStabilityHistory ringBuffer) {
		this.ringBuffer = ringBuffer;

		if (ringBuffer != null) {
			this.commitResultsList = getInternalCommitResults();
		}
	}
	
	/**
	 * Turn ringBuffer into an array list of commit result 
	 * that contains only valid commit result data.
	 * 
	 * @author sunakim1
	 * @author mschmid2
	 * @return commitResultsList
	 */
	public ArrayList<CommitResult> getInternalCommitResults() {
		ArrayList<CommitResult> commitResultsList = new ArrayList<CommitResult>();
		if (this.ringBuffer == null) {
			commitResultsList.add(new CommitResult("ringBuffer is null"));
		} else if (this.ringBuffer.isEmpty()) {
			commitResultsList.add(new CommitResult("ringBuffer is empty"));
		} else {
			Result[] data = this.ringBuffer.getData();
			if (data.length <= 0) {
				commitResultsList
						.add(new CommitResult("ringBuffer length <= 0"));
			} else {
				for (Result r : data) {
					CommitResult cr = r.getCommitResult();
					if (cr != null && cr.isValid)
						commitResultsList.add(cr);
				}
			}
		}
		if (commitResultsList.isEmpty())
			commitResultsList.add(new CommitResult());
		return commitResultsList;

	}

	/**
	 * Avoids returning null value, it adds initial value for debugging purpose.
	 * 
	 * @author sunakim1
	 * @author mschmid2
	 * @return commitResultList
	 */
	public ArrayList<CommitResult> getCommitResults() {
		if (this.commitResultsList == null) {
			this.commitResultsList = new ArrayList<CommitResult>();
			this.commitResultsList.add(new CommitResult("commitResultList is null"));
		}
		return this.commitResultsList;
	}

	public String getCommitResultsHTML() {

		ArrayList<CommitResult> commitResultsList = getCommitResults();
		String htmlResult = "<table border=\"1\"><tr><td>ID</td><td>Author</td><td>Message</td><td>Timestamp</td><td>Build Number</td></tr>";
		for (CommitResult each : commitResultsList) {
			htmlResult = htmlResult + "<tr><td>" + each.ID + "</td>" + "<td>"
					+ each.author + "</td><td>" + each.msg + "</td>" + "<td>"
					+ each.time + "</td><td>" + each.buildNumber + "</td></tr>";
		}
		htmlResult += "</table>";
		return htmlResult;

	}

	public String getDisplayName() {
		return null;
	}

	public String getIconFileName() {
		return null;
	}

	public String getUrlName() {
		return null;
	}

}
