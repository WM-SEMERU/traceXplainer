import {TM_FILE_LOCATION} from './InterfaceConsts';
import TraceModel from './TraceModel';

let traceModel = null;

async function loadTraceModel() {
	traceModel = await TraceModel.getInstanceFromFile(TM_FILE_LOCATION);
}

export async function getTraceLinks(artifactName) {
	if (!traceModel) {
		await loadTraceModel();
	}
	try{

	return traceModel.getTracesForArtifact(artifactName);
	}
	catch(err) {
		console.log(err.message);
	}
}

export async function getAllTraceLinks() {

	if (!traceModel) {
		await loadTraceModel();
	}

	const testArr = ['RQ1.txt', 'RQ2.txt', 'RQ4.txt', 'RQ5.txt', 'RQ6.txt', 'RQ10.txt'];
	const thisModel = [];

	for(let element of testArr) {
		thisModel.push(traceModel.getTracesForArtifact(element));
	}

	var merged = thisModel.reduce(function(prev, next) {
  	return prev.concat(next);
	});

	return merged;
}

export function getLinkThreshold() {
	return 0.3;
}
