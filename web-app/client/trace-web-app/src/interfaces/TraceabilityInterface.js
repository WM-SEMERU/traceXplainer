

export function getTraceLinkValue(sourceName, targetName) {
	//return traceability.links[sourceName][targetName]
	return 0.5;
}

export function getTraceLinks(artifactName) {
	/*
	if (artifactName in traceability.links) {
		return traceability.links[artifactName]
	}

	const traceLinks = {};
	if (artifactName in traceability.links[Object.keys(traceability.links)[0]]) {
		
		Object.keys(traceability.links).forEach((sourceName) => {
			traceLinks[sourceName] = traceability.links[sourceName][artifactName];
		});
	}
	return traceLinks;
	*/
	return {};
}

export function getLinkThreshold() {
	return 0.5;
}