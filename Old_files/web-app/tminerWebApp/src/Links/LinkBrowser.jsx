import './LinkBrowser.css';

import ArtifactBrowser from './ArtifactBrowser';
import ArtifactDetails from './ArtifactDetails';
import React from 'react';

export default class LinkBrowser extends React.Component {

	constructor(props) {
		super(props);
		this.detailsViewRef = React.createRef();
	}

	loadSelectedArtifact(artifactInfo, artifactClass) {
		this.detailsViewRef.current.loadArtifact(artifactInfo, artifactClass);
	}

  loadLinkTable() {
		this.detailsViewRef.current.loadTable();
	}

	unloadArtifact() {
		this.detailsViewRef.current.unloadArtifact();
	}

	render() {
		return (
		<div className="window">
			<div className="linkstuff">
			<ArtifactDetails ref={this.detailsViewRef}/>
			</div>
		</div>
		);
	}
}
