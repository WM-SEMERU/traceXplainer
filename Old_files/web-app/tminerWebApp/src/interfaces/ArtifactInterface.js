import {ARTIFACTS_LOCATION} from './InterfaceConsts';
import artifactMetadataJSON from './artifact_infos.json'

let artifactContentCache = {
	req: {},
	src: {},
	tc: {},
};


//export async function getArtifactContent(artifactClass, id) {
//	if (!(id in artifactContentCache[artifactClass])) {
//		const fileUrl = `${ARTIFACTS_LOCATION}/${artifactClass}/${id}`;
//		console.log('url ' + fileUrl);
//		const file = await fetch(fileUrl, {mode: 'no-cors'});
//		const artifactContent = await file.text();
//		artifactContentCache[artifactClass][id] = artifactContent;
//	}
//	return artifactContentCache[artifactClass][id];
//}

// method to retrieve analysis metrics json
export async function getAnalysisMetrics() {
	const idPath = 'tminer/api/getAnalysisMetrics' // url to api method
	const metrics = fetch(idPath)
		.then(function (response) { // return json if the response is ok
			if (response.ok) {
				return response.json();
			}
			throw new Error("Something went wrong.");
		})
		.then(function (text) {
			console.log("Request successful", text);
			return (text);
		})
		.catch(function (error) {
			console.log("Request failed", error);
		});
	return metrics;
}

export async function getArtifactContent(artifactClass, id) {
    if (!(id in artifactContentCache[artifactClass])) {
        const idPath = 'tminer/api/getdb/' + artifactClass + '/' + id
        const artifactContent = fetch(idPath).then(data => data.text())
        artifactContentCache[artifactClass][id] = artifactContent
    }
	return artifactContentCache[artifactClass][id];
}

export function getArtifactInfo(artifactClass, id) {
	return artifactMetadataJSON[artifactClass][id];
}

export function getAllArtifactInfos(artifactClass) {
	//const artifactInfoText = fetch('tminer/api/getArtifactInfo').then(data => data.text())
	//console.log(artifactInfoText);
	//console.log("NOW ARTIFACT INFO FOR GIVEN CLASS");
	//const artifactJSON = JSON.parse(artifactInfoText);
	//console.log(artifactInfoText[artifactClass]);
	return Object.values(artifactMetadataJSON[artifactClass]);
	//const idPath = 'tminer/api/getArtifactInfo/' + artifactClass
	//const artifactInfos = await fetch(idPath).then(data => data.json())
	//console.log(artifactInfos)
	//const artifactJSON = artifactInfos.json()
	//console.log(artifactJSON)
	//console.log("PRINTING METADATA JSON")
	//console.log(artifactMetadataJSON)
	//return  Object.values(artifactInfos);
}

export function getNumberOfArtifacts(artifactClass) {
	return Object.keys(artifactMetadataJSON[artifactClass]).length
}

export function getArtifactClassName(artifactClass) {
	switch(artifactClass) {
		case 'req': return "Requirement";
		case 'src': return "Source Code";
		case 'tc': return "Test Case";
		default: return "Unknown";
	}
}

export function getArtifactClass(artifactId) {
	if(Object.keys(artifactMetadataJSON['req']).includes(artifactId)) {
		return 'req';
	}
	else if(Object.keys(artifactMetadataJSON['src']).includes(artifactId)) {
		return 'src';
	}
	else if(Object.keys(artifactMetadataJSON['tc']).includes(artifactId)) {
		return 'tc';
	}
	else {
		return 'Unknown';
	}
}
