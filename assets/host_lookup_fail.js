window.onload = onLoad;

function onLoad(){
	document.getElementById("close_app").onclick = closeApp;
	document.getElementById("reload").onclick = reloadPage;
}

function goBack(){
	navigateHistory(-2);
}
