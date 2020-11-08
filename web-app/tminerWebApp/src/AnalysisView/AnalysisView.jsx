import './AnalysisView.css';

import SourceBrowser from './SourceBrowser';
import TargetBrowser from './TargetBrowser';
import SharedMetrics from './SharedMetrics';
import React from 'react';

export default class AnalysisView extends React.Component {

	constructor(props) {
		super(props);
		this.detailsViewRef = React.createRef();
	}

	loadSelectedArtifact(artifactInfo, artifactClass) {
		this.detailsViewRef.current.loadArtifact(artifactInfo, artifactClass);
	}

	unloadArtifact() {
		this.detailsViewRef.current.unloadArtifact();
	}

	render() {
		return (
		<div>
		<div className="window">
			<SourceBrowser />
			<TargetBrowser />
		</div>
		<div>
			<SharedMetrics />
		</div>
		</div>
		
		);
	}
}
