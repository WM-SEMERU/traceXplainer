import './ArtifactNavigator.css';

import ArtifactBrowser from './ArtifactBrowser';
import ArtifactDetails from './ArtifactDetails';
import React from 'react';

export default class ArtifactNavigator extends React.Component {

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
		<div className="window">
			<ArtifactBrowser 
				onArtifactSelect={(artifactInfo, artifactClass) => {
					this.loadSelectedArtifact(artifactInfo, artifactClass);
				}}
				onArtifactDeselect={() => {
					this.unloadArtifact();
				}}
			/>
			<ArtifactDetails ref={this.detailsViewRef}/>
		</div>
		);
	}
}