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

	return traceModel.getTracesForArtifact(artifactName);
	
}

export function getLinkThreshold() {
	return 0.3;
}