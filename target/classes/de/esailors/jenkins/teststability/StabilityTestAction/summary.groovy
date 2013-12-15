raw("<div><img src='${rootURL}${my.bigImagePath}'/> ${my.description}</div><br>")

if(my.testHistory != null){ //The table shows up only on the case result page.
	
	raw("Build history for this test :")
	
	// Create the table. It uses class 'pane sortable' and id 'testresult'
	// to inherit styles and sortability from the CSS and Javascript in the header.
	raw("<table class='pane sortable' id='testresult'>")
	
	// Create the table header. Specifying 'pane-header' allows the column to be sortable.
	raw("<tr>")
	raw("<td class='pane-header'>Build Number</td>")
	raw("<td class='pane-header'>Results</td>")
	raw("<td class='pane-header'>Duration</td>")
	raw("<td class='pane-header'>Commit Author</td>")
	raw("<td class='pane-header'>Flakiness</td>")
	raw("</tr>")

	// getTestHistory returns a map containing build numbers and TestHistoryData objects.
	// We then iterate through each of these pairs to build the table
	caseResult = my.testHistory.getFilteredTestHistory()
		caseResult.each{
		
		key, value -> 
		raw("<tr>")
			raw("<td class='pane'>"+key+"</td>")
			if(value.testCase.isPassed())
				raw("<td class='pane' style='background-color:#00FF00'>Pass</td>")
			else
				raw("<td class='pane' style='background-color:#FF0000'>Fail</td>")
			
			raw("<td class='pane'>"+value.testCase.getDuration()+"</td>")
			if(value.commitResult != null)
				raw("<td class='pane'>"+value.commitResult.author+"</td>")
			else
				raw("<td class='pane'>N/A.</td>")
			raw("<td class='pane'>"+my.getFlakinessCellData(value.flakiness)+"</td>")
			
			raw("</tr>")	
	}
	
	raw("</table>")
}