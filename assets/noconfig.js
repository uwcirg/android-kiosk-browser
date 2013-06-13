
window.onload=onLoad;

function onLoad(){
	document.getElementById("submit_button").onclick = createConfig; 
}

function createConfig(){
	var configFile =  document.getElementById("configURL").value;
	var xmlHttp = null;
    try{
	    xmlHttp = new XMLHttpRequest();
	    xmlHttp.open( "GET",configFile, false );
	    xmlHttp.send( null );
	}
    catch(err){
    	Android.log(err);
    }
    config_contents = xmlHttp.responseText;
    Android.writeConfigFile(config_contents);
}

