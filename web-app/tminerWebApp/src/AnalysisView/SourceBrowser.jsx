import './SourceBrowser.css';

import {HTMLSelect, InputGroup} from '@blueprintjs/core';

import React from 'react';
import { getAllArtifactInfos } from '../interfaces/ArtifactInterface';
import { getAnalysisMetrics } from '../interfaces/ArtifactInterface'; //ADDED
import { Icon } from '@blueprintjs/core';

export default class SourceBrowser extends React.Component {

	state = {
		numberDocs: 0,
		numberDocs_diff: 0,
		vocabSize: 0,
		vocabSize_diff: 0,
		avgNumTokens: 0,
		avgNumTokens_diff: 0,
		vocab1: "",
		vocab1_count: 0,
		vocab1_freq: 0,
		vocab2: "",
		vocab2_count: 0,
		vocab2_freq: 0,
		vocab3: "",
		vocab3_count: 0,
		vocab3_freq: 0
	}

	convertDiffToString(num) {
		var diff = "";
		if (num > 0) {
			diff = "+" + num;
		}
		else {
			diff = "" + num;
		}
		return diff;
	}

	//RETRIEVE DATA
	componentDidMount() {
		getAnalysisMetrics().then((analysisMetrics) => {
			var numDoc_diff = this.convertDiffToString(analysisMetrics.num_doc[2]);
			var vocab = analysisMetrics.src_vocab;
			var v1 = Object.keys(vocab)[0];
			var v2 = Object.keys(vocab)[1];
			var v3 = Object.keys(vocab)[2];
			var v1_ct = (vocab)[v1][0];
			var v1_fr = ((vocab)[v1][1]).toFixed(10);
			var v2_ct = (vocab)[v2][0];
			var v2_fr = ((vocab)[v2][1]).toFixed(10);
			var v3_ct = (vocab)[v3][0];
			var v3_fr = ((vocab)[v3][1]).toFixed(10);

			this.setState({
				// set source fields
				numberDocs: analysisMetrics.num_doc[0],
				numberDocs_diff: this.convertDiffToString(analysisMetrics.num_doc[2]),
				vocabSize: analysisMetrics.vocab_size[0],
				vocabSize_diff: this.convertDiffToString(analysisMetrics.vocab_size[2]),
				avgNumTokens: (analysisMetrics.avg_tokens[0]).toFixed(10),
				avgNumTokens_diff: this.convertDiffToString((analysisMetrics.avg_tokens[2]).toFixed(10)),
				// set vocab
				vocab1: v1,
				vocab2: v2,
				vocab3: v3,
				// set vocab counts
				vocab1_count: v1_ct,
				vocab2_count: v2_ct,
				vocab3_count: v3_ct,
				// set vocab frequencies
				vocab1_freq: v1_fr,
				vocab2_freq: v2_fr,
				vocab3_freq: v3_fr
			});
		});
	}

/*	currentArtifactClass = 'req';

	constructor(props) {
		super(props);

		this.artifactCardRefs = [];
		this.currentlySelectedArtifactIndex = -1;

		const artifactInfos = getAllArtifactInfos(this.currentArtifactClass);
		this.state = {
			artifactInfos: artifactInfos,
		};

		this.artifactCardRefs = artifactInfos.map((artifactInfo) => React.createRef());
	}*/

	deselectCurrentlySelectedArtifact() {

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
