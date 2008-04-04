/*
****************   DEFAULT VOTE WIDGET **************

******************************************************
*/
var isDebug = false;
var instanceid_key;
var proxyUrl;
var widgetAPIUrl;
var isActive=false;
var username="";
var thisUserUnlocked = false;
var isAdmin = false;
var isQuestionSet = false;
var answerSeparator = "<answer>";
//var countSeparator = "<count>";
var question = null;
var answers = null;
var lastVote = 0;


function genHex(){
colors = new Array(14)
colors[0]="0"
colors[1]="1"
colors[2]="2"
colors[3]="3"
colors[4]="4"
colors[5]="5"
colors[5]="6"
colors[6]="7"
colors[7]="8"
colors[8]="9"
colors[9]="a"
colors[10]="b"
colors[11]="c"
colors[12]="d"
colors[13]="e"
colors[14]="f"

digit = new Array(5)
color=""
for (i=0;i<6;i++){
digit[i]=colors[Math.round(Math.random()*14)]
color = color+digit[i]
}
return color;
}

// set the local username
function setLocalUsername(p){
	username = p;	
	widget.sharedDataForKey(instanceid_key, "isSet", doSetup);
}	 


// #########################setting up the vote setion #################################

var responseCount=0; // count amount of formfields
var responseArr = new Array();

function doSetup(isSet){
    if(isSet == null){
		if(isAdminUser()){
			createQuestions();
		}
		else{
			showNotReadyYet();
		}
	}
	else{
		widget.preferenceForKey(instanceid_key, "hasVoted", showOrVoteScreen);
	
	}
}

function showOrVoteScreen(hasVoted){
// show questions
	if(isAdminUser()){
		showVoteDisplayStageOne();		
	}
	else {	
		if(hasVoted!="true"){
			setupVoteDisplayStageOne();
		}
		else{
			showVoteDisplayStageOne();	
		}
	}
}

function findObj(n, d) { //v4.0
	var p,i,x; if(!d) d=document;
	if((p=n.indexOf("?"))>0&&parent.frames.length) {
		d=parent.frames[n.substring(p+1)].document; n=n.substring(0,p);
	}
	if(!(x=d[n])&&d.all) x=d.all[n]; for (i=0;!x&&i<d.forms.length;i++)
		x=d.forms[i][n];
		for(i=0;!x&&d.layers&&i<d.layers.length;i++)
			x=findObj(n,d.layers[i].document);
		if(!x && document.getElementById) x=document.getElementById(n); return x;
}


function createNewInput(){
	for(var k=1;k<=responseCount;k++){
		responseArr[k-1] =  findObj("response"+k).value;
	}
	responseCount++
	findObj("dynForm").innerHTML+="Response "+responseCount+"<input type='text'  size='50' maxlength='100' id='response" + responseCount + "'><br>";
	for(k=1;k<responseCount;k++){
  		findObj("response"+k).value = responseArr[k-1];
	}
}

function doValidation(){
	var validResponseCount = 0;
	var responseXML = "";
	var responses ="";
			
	var q = findObj("questionfield").value;
	if (q.length < 1){
		alert("Step one: You have not set a question");
		return;
	}

	for(var j=1;j<=responseCount;j++){
		rText =  findObj("response"+j).value;
		if(rText != ""){
			responses += "Response "+ j + ":  " + rText;
			responseXML+= answerSeparator + rText;
			// here we set all the number of votes of these to be zero			
			validResponseCount++;
		}
		else{
			responses += "Response "+ j + ":  " + "<is empty so wont be added>";

		}
		responses += "\n";
	}

	if(validResponseCount < 2){
			alert("Step two: You need at least two responses");
			return;
	}

	var answer =  confirm("Are you sure you have finished & want to set the vote?\n\nChoose 'OK' to finish and make the vote available.\nChoose 'Cancel' to carry on editing.\n\n"
		+ "Question: " + q + "\n" +responses);

	if (answer){
		//submit the form
		//alert("question:" + q + "\n" + "Answers:" + responseXML);
		widget.setSharedDataForKey(instanceid_key, "question", q);
		widget.setSharedDataForKey(instanceid_key, "answers", responseXML);
		widget.setSharedDataForKey(instanceid_key, "isSet", "true");
		// make a db entry for each of these options/responses, set to zero
		for(var j=1;j<=validResponseCount;j++){
			widget.setSharedDataForKey(instanceid_key, "response"+j, "0");
		}
		widget.setSharedDataForKey(instanceid_key, "totalvotes", "0");
		//now redirect to see the results page	
		showVoteDisplayStageOne();		
	}
	else{}

}

function showNotReadyYet(){
	var notReadytext = "<div id='legendDiv'>This vote instance has not yet been initialised by the teacher.</div>";
	dwr.util.setValue("maincanvas", notReadytext, { escapeHtml:false });
}

function createQuestions(){
	var authorVoteText = "<div id='legendDiv'>Setup vote widget</div>";
	authorVoteText += "<div id='questionDiv'>Step One: Enter the question here:&nbsp;<input type='text' size='50' maxlength='100' id='questionfield'></div>";
	authorVoteText += "<div id='responseDiv'>Step Two: Create the responses:&nbsp;(click&nbsp;<a href='#' onClick='createNewInput()'>here</a>&nbsp;to add more response fields)<div id='dynForm'></div></div>";
	authorVoteText += "<div id='submitDiv'>Step Three: Click&nbsp;<a href='#' onClick='doValidation()'>here</a>&nbsp;to check and finish</div>";
	dwr.util.setValue("maincanvas", authorVoteText, { escapeHtml:false });
}

function setupVoteDisplayStageOne(){
	widget.sharedDataForKey(instanceid_key, "question", setupVoteDisplayStageTwo);
}

function setupVoteDisplayStageTwo(pQuestion){
	question = pQuestion;
	widget.sharedDataForKey(instanceid_key, "answers", setupVoteDisplayStageThree);
}

function setupVoteDisplayStageThree(pAnswers){
	answers = pAnswers;
	
	var questionText = "<div id='questionDiv'><img border='0' src='/wookie/shared/images/vote.png'>"+question+"</div>";
	var answerText = "<div id='responseDiv'><form name='responseform'>";	
	var answerArray = pAnswers.split(answerSeparator);	
	var count=0;
	for (var data in answerArray) {		
		// put each answer in a new div
		if(answerArray[data].length>0){
			count++;
    		answerText += "<div>&nbsp;&nbsp;<INPUT TYPE=RADIO NAME='answer' VALUE='"+count+"'>&nbsp;" + dwr.util.escapeHtml(answerArray[data]) + "</div>";
    	}
    }
    answerText += "</form></div>";    
    var submitText= questionText+ answerText + "<div id='submitDiv'>Click&nbsp;<a href='#' onClick='doVote()'>here</a>&nbsp;to vote</div>";		
	dwr.util.setValue("maincanvas", submitText, { escapeHtml:false });
	var respDiv = dwr.util.byId('responseDiv');
	respDiv.style.height = "200px"; 
}

function doVote(){	
	var respValue = checkradioform();
	//check that the user has chosen a response
	if(respValue == -1){
		alert("Please select a response.");
		return;
	}
	else{
		// stop user clicking again
		var submitPressedText = "Click here to vote";
		dwr.util.setValue("submitDiv", submitPressedText, { escapeHtml:false });
		// update the response count
		lastVote = respValue;
		widget.sharedDataForKey(instanceid_key, "response"+ respValue, doVoteUpdate);		
		widget.sharedDataForKey(instanceid_key, "totalvotes", doTotalVoteUpdate);
		// set in prefs that user has now voted
		widget.setPreferenceForKey(instanceid_key, "hasVoted", "true");
		// now show the vote	
		showVoteDisplayStageOne();		
	}
}

function doVoteUpdate(respCountVal){
	var actualIntCount = parseInt(respCountVal);   
	actualIntCount++;
	widget.setSharedDataForKey(instanceid_key, "response"+ lastVote, actualIntCount);
}

function doTotalVoteUpdate(totalCountVal){
	var totalIntCount = parseInt(totalCountVal);   
	totalIntCount++;
	widget.setSharedDataForKey(instanceid_key, "totalvotes", totalIntCount);
}

//######show the vote ########

function showVoteDisplayStageOne(){
	widget.sharedDataForKey(instanceid_key, "question", showVoteDisplayStageTwo);
}

function showVoteDisplayStageTwo(pQuestion){
	question = pQuestion;
	widget.sharedDataForKey(instanceid_key, "answers", showVoteDisplayStageThree);
}

function showVoteDisplayStageThree(pAnswers){
	answers = pAnswers;
	var questionText = "<div id='questionDiv'><img border='0' src='/wookie/shared/images/vote.png'>"+question+"</div>";
	var answerText = "<div id='responseDiv'><form name='responseform'><table border='0'>";	
	var answerArray = pAnswers.split(answerSeparator);	
	var count=0;
	for (var data in answerArray) {		
		// put each answer in a new div
		if(answerArray[data].length>0){
			count++;			
    		answerText += "<tr><td width='290px'><div id='tt"+count+"'>&nbsp;" + dwr.util.escapeHtml(answerArray[data]) + "</div></td>";
    		answerText += "<td width='100px'><div id='bar"+count+"' style=\"width: 0px; background-color:#"+genHex()+";\">&nbsp;</div></td>";
    		answerText += "<td width='40px'><div id='percentDiv"+count+"'></div></td>";
    		answerText += "<td width='70px'><div id='sresponse"+count+"'></div></td></tr>";
    	}
    }
    answerText += "</table></form></div>";    
    var endText= questionText+ answerText;		
	dwr.util.setValue("maincanvas", endText, { escapeHtml:false });
	
	for(var k=1;k<=count;k++){		
		var callMetaData = { 
  			callback:getResults, 
  			args: k // specify an argument to pass to the callback and exceptionHandler
		};
		widget.sharedDataForKey(instanceid_key, "response"+k, callMetaData);
	}
}


function getResults(server, index){	
	var textResults = "<div>&nbsp;("+server+" votes)</div>";
	dwr.util.setValue("sresponse"+index, textResults, { escapeHtml:false });
	var mcallMetaData = { 
  			callback:updatePercentages, 
  			args: server+"#"+index  // specify an argument to pass to the callback and exceptionHandler  			
		};
	widget.sharedDataForKey(instanceid_key, "totalvotes", mcallMetaData);
}

function updatePercentages(total, oneResponse){
	var temp = new Array();
	temp = oneResponse.split('#');
	var tot = parseInt(total);	
	var thisResponse = parseInt(temp[0]);
	
	if(tot==0&&thisResponse==0){
		res = 0;
	}
	else{
		result = (thisResponse / tot) * 100;	
		res = roundNumber(result,2);
	}
		
	dwr.util.setValue("percentDiv"+temp[1], res+"%", { escapeHtml:false });
	var bar = dwr.util.byId('bar'+temp[1]); 
	bar.style.width = res+"%";								 
	var respDiv = dwr.util.byId('responseDiv');
	respDiv.style.height = "250px"; 
	
}

function roundNumber(num, dec) {
	var result = Math.round(num*Math.pow(10,dec))/Math.pow(10,dec);
	return result;
}

//#############

function checkradioform(){	
	// Loop from zero to the one minus the number of radio button selections
	for (counter = 0; counter < document.responseform.answer.length; counter++){
	// If a radio button has been selected it will return true
	// (If not it will return false)
		if (document.responseform.answer[counter].checked){
			return document.responseform.answer[counter].value;
		}
	}	
	return -1;
}

//########################################################


function isAdminUser(){
	if (username.indexOf("staff")!=-1){
		return true;
	}
	else if(username.indexOf("teacher")!=-1){
		return true;
	}
	return false;
}

// on start up set some values & init with the server
function init() {
	if(!isActive){
		// This gets the id_key and assigns it to instanceid_key
		// This page url will be called with e.g. idkey=4j45j345jl353n5lfg09cw03f05
		// so grab that key to use as authentication against the server
		var query = window.location.search.substring(1);
		var pairs = query.split("&");
		for (var i=0;i<pairs.length;i++){
			var pos = pairs[i].indexOf('=');
			if (pos >= 0){				
				var argname = pairs[i].substring(0,pos);
				if(argname=="idkey"){
					instanceid_key = pairs[i].substring(pos+1);
					//alert("idkey="+instanceid_key);
				}
				if(argname=="proxy"){
					proxyUrl = pairs[i].substring(pos+1);
					//alert("proxy="+proxyUrl);
				}
				if(argname=="serviceapi"){
					widgetAPIUrl = pairs[i].substring(pos+1);
					//alert("serviceapi="+widgetAPIUrl);
				}				
			}
		}	
		isActive = true;
		// this line tells DWR to use call backs (i.e. will call onsharedupdate() when an event is recevied for shared data
	 	dwr.engine.setActiveReverseAjax(true);
	 	widget.preferenceForKey(instanceid_key, "LDUsername", setLocalUsername);	 	
 	}
}

function cleanup() {
	if(isActive){	
	}
}


// Note: Not currently used - but it is possible for the server to pass a parameter 
// back to this method from, for example onsharedupdate().
// was this "messages" parameter causing a problem?
function handleSharedUpdate(messages){
	if (isDebug) alert("handle sharedupdate");
	}

function handleLocked(){
if (isDebug) alert("handle Locked");
	isActive = false;		
}

function handleUnlocked(){	
if (isDebug) alert("start handle Unlocked");
	isActive = true;
	if(thisUserUnlocked){
		thisUserUnlocked = false;		
	}	
    if (isDebug) alert("end handle Unlocked"); 
}

// handleUpdate is our local implementation of onSharedUpdate
widget.onSharedUpdate = handleSharedUpdate;
widget.onLocked = handleLocked;
widget.onUnlocked = handleUnlocked;
onunload = cleanup;
