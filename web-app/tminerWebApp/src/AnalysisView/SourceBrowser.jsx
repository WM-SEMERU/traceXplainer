import './SourceBrowser.css';

import {HTMLSelect, InputGroup} from '@blueprintjs/core';

import React from 'react';
import { getAllArtifactInfos } from '../interfaces/ArtifactInterface';
import { Cell, Column, Table } from "@blueprintjs/table";
import { getLinkThreshold, getTraceLinks } from '../interfaces/TraceabilityInterface';
import { getArtifactClassName, getArtifactContent, } from '../interfaces/ArtifactInterface';
import { Icon } from '@blueprintjs/core';

export default class SourceBrowser extends React.Component {

	state = {
		artifactInfos: null,
	}

	currentArtifactClass = 'req';

	constructor(props) {
		super(props);

		this.artifactCardRefs = [];
		this.currentlySelectedArtifactIndex = -1;

		const artifactInfos = getAllArtifactInfos(this.currentArtifactClass);
		this.state = {
			artifactInfos: artifactInfos,
		};

		this.artifactCardRefs = artifactInfos.map((artifactInfo) => React.createRef());
	}

	render() {
		return (
			<div className="artifactBrowserContainer">
				<div className="artifactBrowser">
					
					<div className="sourceTitle">
					<div className="sourceTitleRow">
					
					<div className="leftTitle">
						&nbsp; &nbsp; &nbsp; Source
					</div>
					
					<div className="artifactClassSelectorContainer">
						<HTMLSelect onChange={(event) => {
							this.deselectCurrentlySelectedArtifact();
							this.props.onArtifactDeselect();
							this.currentArtifactClass = event.currentTarget.value;
							this.fetchArtifacts(event.currentTarget.value);
						}}>
							<option value="req">Requirements</option>
							<option value="src">Source Code</option>
						</HTMLSelect>
					</div>
					
					</div>
					</div>
					
					<table className="metricTable">
						<tr>
							<th>Metric</th>
							<th>Value</th>
							<th>Target Difference</th>
						</tr>
						<tr>
							<td>Number of Documents</td>
							<td>66</td>
							<td>+14</td>
						</tr>
						<tr>
							<td>Vocabulary Size</td>
							<td>3516</td>
							<td>+1057</td>
						</tr>
						<tr>
							<td>Avg. Number of Tokens per Document</td>
							<td>568</td>
							<td>+435</td>
						</tr>
					</table>
					
					<div className="heading">
						Vocab
					</div>
					
					<table className="metricTable">
						<tr>
							<th>Token</th>
							<th>Count</th>
							<th>Frequency</th>
						</tr>
						<tr>
							<td>est</td>
							<td>4082</td>
							<td>0.12</td>
						</tr>
						<tr>
							<td>http</td>
							<td>1065</td>
							<td>0.05</td>
						</tr>
						<tr>
							<td>client</td>
							<td>1023</td>
							<td>0.05</td>
						</tr>
					</table>

					<div style={{height: 20}}/>
				</div>
			</div>
		);
	}
}