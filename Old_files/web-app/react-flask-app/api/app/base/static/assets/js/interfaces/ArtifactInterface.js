import {ARTIFACTS_LOCATION} from './InterfaceConsts';
import artifactMetadataJSON from './artifact_infos.json'

let artifactContentCache = {
	req: {},
	src: {},
	tc: {},
};


export async function getArtifactContent(artifactClass, id) {
	if (!(id in artifactContentCache[artifactClass])) {
		const fileUrl = `${ARTIFACTS_LOCATION}/${artifactClass}/${id}`;
		const file = await fetch(fileUrl, {mode: 'no-cors'});
		const artifactContent = await file.text();
		artifactContentCache[artifactClass][id] = artifactContent;
	}
	return artifactContentCache[artifactClass][id];
}

export function getArtifactInfo(artifactClass, id) {
	return artifactMetadataJSON[artifactClass][id];
}

export function getAllArtifactInfos(artifactClass) {
	return Object.values(artifactMetadataJSON[artifactClass]);
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