// Create the table. It uses class 'pane sortable' and id 'testresult'
// to inherit styles and sortability from the CSS and Javascript in the header.
raw("<table class='pane sortable' id='testresult'>")

// Create the table header. The class 'pane-header' specifies that this column can be sorted.
raw("<tr>")
raw("<td class='pane-header'>Build Number</td>")
raw("<td class='pane-header'>Results</td>")
raw("<td class='pane-header'>Author</td>")
raw("<td class='pane-header'>Message</td>")
raw("<td class='pane-header'>Timestamps</td>")
raw("</tr>")
raw(it.displayName)

// Iterate through the an array of type Result to build the table
my.ringBuffer.data.each{ i->
	raw("<tr>")
	raw("<td class='pane'>"+i.buildNumber+"</td>")	
	if(i.passed)
		raw("<td class='pane' style='background-color:#00FF00'>"+"Pass"+"</td>")	
	else
		raw("<td class='pane' style='background-color:#FF0000'>"+"Fail"+"</td>")		
	if(i.cr != null){
		raw("<td class='pane'>"+i.cr.author+"</td>")
		raw("<td class='pane'>"+i.cr.msg+"</td>")
		if(i.cr.time > 0 ){
			myDate = new Date(i.cr.time)
			raw("<td class='pane'>"+String.format('%tF', myDate)+"</td>")
		} else {
			raw("<td class='pane'>"+"N/A"+"</td>")	
		}
	} else {
		raw("<td class='pane'>"+"N/A"+"</td>")
		raw("<td class='pane'>"+"N/A"+"</td>")
		raw("<td class='pane'>"+"N/A"+"</td>")	
	}
	raw("</tr>")	
}
raw("</table>")
