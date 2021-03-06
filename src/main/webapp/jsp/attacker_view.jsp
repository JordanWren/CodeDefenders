<%@ page import="org.codedefenders.database.DatabaseAccess" %>
<%@ page import="static org.codedefenders.game.GameState.ACTIVE" %>
<%@ page import="org.codedefenders.game.GameState" %>
<%@ page import="org.codedefenders.game.Mutant" %>
<%@ page import="org.codedefenders.game.Role" %>
<%@ page import="org.codedefenders.game.Test" %>
<%@ page import="org.codedefenders.util.Constants" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.google.gson.Gson" %>
<%@ page import="java.util.stream.Collectors" %>
<%Gson gson = new Gson();%>

<% String pageTitle="Attacking Class"; %>
<%@ include file="/jsp/header_game.jsp" %>

<%
	if (game.getState().equals(GameState.FINISHED)) {
		String message = Constants.DRAW_MESSAGE;
		if (game.getAttackerScore() > game.getDefenderScore())
			message = Constants.WINNER_MESSAGE;
		else if (game.getDefenderScore() > game.getAttackerScore())
			message = Constants.LOSER_MESSAGE;
%>
<div id="finishedModal" class="modal fade">
	<div class="modal-dialog">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
				<h4 class="modal-title">Game Over</h4>
			</div>
			<div class="modal-body">
				<p><%=message%></p>
			</div>
			<div class="modal-footer">
				<button type="button" class="btn btn-primary" data-dismiss="modal">Close</button>
			</div>
		</div><!-- /.modal-content -->
	</div><!-- /.modal-dialog -->
</div><!-- /.modal -->
<%  } %>

<div class="row-fluid">
	<div class="col-md-6" id="mutants-div">
		<h3>Mutants</h3>
		<!-- Nav tabs -->
		<ul class="nav nav-tabs" role="tablist">
			<li class="active">
				<a href="#mutalivetab" role="tab" data-toggle="tab">Alive</a>
			</li>
			<li>
				<a href="#mutkilledtab" role="tab" data-toggle="tab">Killed</a>
			</li>
		</ul>
		<div class="tab-content">
			<div class="tab-pane fade active in" id="mutalivetab">
				<table class="table table-striped table-hover table-responsive table-paragraphs">
					<%
					List<Mutant> mutantsAlive = game.getAliveMutants();
					Map<Integer, List<Mutant>> mutantLines = new HashMap<>();
					if (! mutantsAlive.isEmpty()) {
						for (Mutant m : mutantsAlive) {
							for (int line : m.getLines()){
								if (!mutantLines.containsKey(line)){
									mutantLines.put(line, new ArrayList<Mutant>());
								}
								mutantLines.get(line).add(m);
							}
					%>
					<tr>
						<td><h4>Mutant <%= m.getId() %></h4></td>
						<td>
							<a href="#" class="btn btn-default btn-diff" id="btnMut<%=m.getId()%>" data-toggle="modal" data-target="#modalMut<%=m.getId()%>">View Diff</a>
							<div id="modalMut<%=m.getId()%>" class="modal fade" role="dialog">
								<div class="modal-dialog">
									<!-- Modal content-->
									<div class="modal-content">
										<div class="modal-header">
											<button type="button" class="close" data-dismiss="modal">&times;</button>
											<h4 class="modal-title">Mutant <%=m.getId()%> - Diff</h4>
										</div>
										<div class="modal-body">
											<pre class="readonly-pre"><textarea class="mutdiff" id="diff<%=m.getId()%>"><%=m.getPatchString()%></textarea></pre>
										</div>
										<div class="modal-footer">
											<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
										</div>
									</div>
								</div>
							</div>
						</td>
					</tr>
					<tr>
						<td colspan="3">
							<% for (String change :	m.getHTMLReadout()) { %>
							<p><%=change%><p>
								<% } %>
						</td>
					</tr>
					<%
						}
					} else {%>
					<tr class="blank_row">
						<td class="row-borderless" colspan="2">No mutants alive.</td>
					</tr>
					<%}
					%>
				</table>
			</div>
			<div class="tab-pane fade" id="mutkilledtab">
				<table class="table table-striped table-responsive table-paragraphs">
					<%
					List<Mutant> mutantsKilled = game.getKilledMutants();
					Map<Integer, List<Mutant>> mutantKilledLines = new HashMap<>();
					if (! mutantsKilled.isEmpty()) {
						for (Mutant m : mutantsKilled) {
							for (int line : m.getLines()){
								if (!mutantKilledLines.containsKey(line)){
									mutantKilledLines.put(line, new ArrayList<Mutant>());
								}
								mutantKilledLines.get(line).add(m);
							}
					%>
					<tr>
						<td class="col-sm-1"><h4>Mutant <%= m.getId() %></h4></td>
						<td class="col-sm-1">
							<a href="#" class="btn btn-default btn-diff" id="btnMut<%=m.getId()%>" data-toggle="modal" data-target="#modalMut<%=m.getId()%>">View Diff</a>
							<div id="modalMut<%=m.getId()%>" class="modal fade" role="dialog">
								<div class="modal-dialog">
									<!-- Modal content-->
									<div class="modal-content">
										<div class="modal-header">
											<button type="button" class="close" data-dismiss="modal">&times;</button>
											<h4 class="modal-title">Mutant <%=m.getId()%> - Diff</h4>
										</div>
										<div class="modal-body">
											<p>Killed by Test <%= DatabaseAccess.getKillingTestIdForMutant(m.getId()) %></p>
											<pre class="readonly-pre"><textarea class="mutdiff" id="diff<%=m.getId()%>"><%=m.getPatchString()%></textarea></pre>
										</div>
										<div class="modal-footer">
											<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
										</div>
									</div>
								</div>
							</div>
						</td>
					</tr>
					<tr>
						<td class="col-sm-1" colspan="3">
							<% for (String change : m.getHTMLReadout()) { %>
							<p><%=change%><p>
								<% } %>
						</td>
					</tr>
					<%
						}
					} else {%>
					<tr class="blank_row">
						<td class="row-borderless" colspan="2">No mutants killed.</td>
					</tr>
					<%}
					%>
				</table>
			</div>
		</div> <!-- tab-content -->
		<h3>JUnit Tests </h3>
		<div class="slider single-item">
			<%
				boolean isTests = false;

				String codeDivName = "cut-div";
				HashMap<Integer, ArrayList<Test>> linesCovered = new HashMap<Integer, ArrayList<Test>>();
				List<Test> tests = game.getTests();

				for (Test t : tests) {

					for (Integer lc : t.getLineCoverage().getLinesCovered()){
						if (!linesCovered.containsKey(lc)){
							linesCovered.put(lc, new ArrayList<Test>());
						}

						linesCovered.get(lc).add(t);
					}

					String tc = "";
					for (String line : t.getHTMLReadout()) { tc += line + "\n"; }
			%>
			<div><h4>Test <%=t.getId()%></h4><pre class="readonly-pre"><textarea class="utest" cols="20" rows="10"><%=tc%></textarea></pre></div>
			<%
				}
				if (tests.isEmpty()) {%>
			<div><h3></h3><p> There are currently no tests </p></div>
			<%}
			%>
		</div> <!-- slider single-item -->
	</div> <!-- col-md6 mutants -->

	<div class="col-md-6" id="cut-div">
		<form id="atk" action="<%=request.getContextPath() %>/play" method="post">
			<h3>Create a mutant here
				<% if (game.getState().equals(ACTIVE) && game.getActiveRole().equals(Role.ATTACKER)) {%>
				<button type="submit" class="btn btn-primary btn-game btn-right" id="submitMutant" form="atk" onClick="this.form.submit(); this.disabled=true; this.value='Attacking...';">Attack!</button><%}%>
			</h3>
			<input type="hidden" name="formType" value="createMutant">
			<%
				String mutantCode;
				String previousMutantCode = (String) request.getSession().getAttribute(Constants.SESSION_ATTRIBUTE_PREVIOUS_MUTANT);
				request.getSession().removeAttribute(Constants.SESSION_ATTRIBUTE_PREVIOUS_MUTANT);
				if (previousMutantCode != null) {
					mutantCode = previousMutantCode;
				} else
					mutantCode = game.getCUT().getAsString();
			%>
			<pre><textarea id="code" name="mutant" cols="80" rows="50"><%= mutantCode %></textarea></pre>
		</form>
	</div> <!-- col-md6 newmut -->
</div> <!-- row-fluid -->

<script>
	var editor = CodeMirror.fromTextArea(document.getElementById("code"), {
		lineNumbers: true,
		indentUnit: 4,
		indentWithTabs: true,
		matchBrackets: true,
		mode: "text/x-java"
	});
	editor.setSize("100%", 500);

    testMap = {<% for (Integer i : linesCovered.keySet()){%>
    <%= i%>: [<%= linesCovered.get(i).stream().map(t -> Integer.toString(t.getId())).distinct().collect(Collectors.joining(","))%>],
    <% } %>
    };


    highlightCoverage = function(){
        highlightLine([<% for (Integer i : linesCovered.keySet()){%>
            [<%=i%>, <%=((float)linesCovered.get(i).size() / (float) tests.size())%>],
            <% } %>], COVERED_COLOR, "<%="#" + codeDivName%>");
    };

    getMutants = function(){
        return JSON.parse("<%= gson.toJson(mutants).replace("\"", "\\\"") %>");
    }

    showMutants = function(){
        mutantLine("<%="#" + codeDivName%>", "false");
    };

    var updateCUT = function(){
        showMutants();
        highlightCoverage();
    };

    editor.on("viewportChange", function(){
        updateCUT();
    });
    $(document).ready(function(){
        updateCUT();
    });

    //inline due to bug in Chrome?
    $(window).resize(function (e){setTimeout(updateCUT, 500);});

	var x = document.getElementsByClassName("utest");
	var i;
	for (i = 0; i < x.length; i++) {
		CodeMirror.fromTextArea(x[i], {
			lineNumbers: true,
			matchBrackets: true,
			mode: "text/x-java",
			readOnly: true
		});
	}
	/* Mutants diffs */
	$('.modal').on('shown.bs.modal', function() {
		var codeMirrorContainer = $(this).find(".CodeMirror")[0];
		if (codeMirrorContainer && codeMirrorContainer.CodeMirror) {
			codeMirrorContainer.CodeMirror.refresh();
		} else {
			var editorDiff = CodeMirror.fromTextArea($(this).find('textarea')[0], {
				lineNumbers: false,
				mode: "diff",
				readOnly: true /* onCursorActivity: null */
			});
			editorDiff.setSize("100%", 500);
		}
	});

	<% if (game.getActiveRole().equals(Role.DEFENDER)) {%>
	function checkForUpdate(){
		$.post('/play', {
			formType: "whoseTurn",
			gameID: <%= game.getId() %>
		}, function(data){
			if(data=="attacker"){
				window.location.reload();
			}
		},'text');
	}
	setInterval("checkForUpdate()", 10000);
	<% } %>

	$('#finishedModal').modal('show');
</script>
<%@ include file="/jsp/footer_game.jsp" %>