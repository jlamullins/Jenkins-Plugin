package de.esailors.jenkins.teststability.AllTestsHistoriesAction

import com.google.common.collect.Multimap
import hudson.Functions
import hudson.model.AbstractBuild
import hudson.model.AbstractBuild.DependencyChange
import hudson.scm.ChangeLogSet
import java.text.DateFormat
import org.jvnet.localizer.LocaleProvider
import java.util.ArrayList;

f = namespace(lib.FormTagLib)
l = namespace(lib.LayoutTagLib)
t = namespace("/lib/hudson")
st = namespace("jelly:stapler")

l.layout(title: _("ALL TESTS", my.project.name)) {
  st.include(page: "sidepanel.jelly", it: my.project)
  l.main_panel() {
   
    h1(_("All Changes"))
      showChanges()
  }
}


private showChanges() {
  
  
  if(my.results!=null){
    raw("All test history for this project")
    raw("<table class='pane sortable' id='testresult'>")

    raw("<tr>")
    raw("<td class='pane-header'>CaseName </td>")
    builds = my.getAllBuildNum()
    builds.each{
      build ->
       raw("<td class='pane-header'> Build "+build+" </td>")
    }
    raw("</tr>")

    caseNames = my.getCaseNames()

    caseNames.each{
      caseName ->
      raw("<tr>")
      raw("<td class='pane'>"+caseName+"</td>")
        builds.each{
          build ->
          if(my.getResult(caseName, build).equals("Pass"))
            raw("<td class='pane' style='background-color:#00FF00'>Pass</td>")
          if(my.getResult(caseName, build).equals("Fail"))
            raw("<td class='pane' style='background-color:#FF0000'>Fail</td>")
          if(my.getResult(caseName, build).equals("N/A"))
            raw("<td class='pane'>N/A</td>")
        }
      raw("</tr>")

    }
  }else{
    raw("Please build the job first.")

  }
}

