package de.esailors.jenkins.teststability;

import hudson.model.AbstractBuild;
import hudson.scm.ChangeLogSet;
import hudson.scm.ChangeLogSet.AffectedFile;

import java.util.Collection;

public class CommitResult {

	public static final String INVALID_FIELD = "N/A";

	int buildNumber;
	String ID;
	String author;
	String msg;
	long time;
	boolean isValid;

	private void setFields(int buildNumber, String ID, String author,
			String msg, long time, boolean isValid) {

		this.buildNumber = buildNumber;
		this.ID = ID;
		this.author = author;
		this.msg = msg;
		this.time = (long) time;
		this.isValid = isValid;
	}

	public CommitResult(int buildNumber, String ID, String author, String msg,
			long time) {
		setFields(buildNumber, ID, author, msg, time, true);
	}
	/**
	 * Constructs a valid commit result 
	 * only if there was a change from SVN log && the test class was affected in that change. 
	 * 
	 * @author sunakim1
	 * @author mschmid2
	 * @param testResult
	 */
	public CommitResult(hudson.tasks.test.TestResult testResult) {
		AbstractBuild<?, ?> build = testResult.getOwner();
		if (build == null) {
			throw new NullPointerException(
					"CommitResult(testResult): testResult.getOwner() is NULL ... ");
		}

		ChangeLogSet<? extends ChangeLogSet.Entry> changeLogSet = build
				.getChangeSet();

		// If there wasn't any change
		if (changeLogSet.isEmptySet()) {
			setFields(0, INVALID_FIELD, INVALID_FIELD, INVALID_FIELD, 0, false);
			return;
		}

		// If there was any change
		for (ChangeLogSet.Entry changeLog : changeLogSet) {
			if (changeLog == null)
				break;

			Collection<? extends AffectedFile> affectedFiles = changeLog
					.getAffectedFiles();
			//If the test class is affected by the change
			for (AffectedFile curFile : affectedFiles) {
				if (curFile.getPath().contains(testResult.getName())) {
					setFields(build.getNumber(), changeLog.getCommitId(),
							changeLog.getAuthor().getDisplayName(),
							changeLog.getMsg(), changeLog.getTimestamp(), true);
					break;
				}
			}
			//If the test class is NOT affected by the change
			if (!this.isValid) {
				setFields(0, INVALID_FIELD, INVALID_FIELD, INVALID_FIELD, 0, false);
			}			
			// Iterate only once
			break;
		}
	}

	public CommitResult() {
		setFields(0, INVALID_FIELD, INVALID_FIELD, INVALID_FIELD, 0, false);
	}

	public CommitResult(String debug) {
		setFields(0, INVALID_FIELD, INVALID_FIELD, debug, 0, true);
	}

}
