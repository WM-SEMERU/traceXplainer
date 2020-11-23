import './SharedMetrics.css';

import {HTMLSelect, InputGroup} from '@blueprintjs/core';

import React from 'react';
import { getAllArtifactInfos } from '../interfaces/ArtifactInterface';
import { getAnalysisMetrics } from '../interfaces/ArtifactInterface'; //ADDED

export default class SharedMetrics extends React.Component {

	state = {
		//artifactInfos: null,
		//ADDED ATTRIBUTES
		numberDocs: 0,
		numberDocs_diff: 0,
		vocabSize: 0,
		vocabSize_diff: 0,
		avgNumTokens: 0,
		avgNumTokens_diff: 0,
		sharedVocab: {},
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

	//currentArtifactClass = 'req';
	
	//ADDED to retrieve data using api methods and update state
	componentDidMount() {
		getAnalysisMetrics().then((analysisMetrics) => {
			//RETRIEVE DATA
			var avNumTkn = ((analysisMetrics.avg_tokens[0] + analysisMetrics.avg_tokens[1]) / 2).toFixed(10);
			//retrieve shared vocab
			var v1 = Object.keys(analysisMetrics.shared_vocab)[0];
			var v2 = Object.keys(analysisMetrics.shared_vocab)[1];
			var v3 = Object.keys(analysisMetrics.shared_vocab)[2];
			//retrieve shared vocab counts/frequencies
			var v1_ct = (analysisMetrics.shared_vocab)[v1][0];
			var v1_fr = ((analysisMetrics.shared_vocab)[v1][1]).toFixed(10);
			var v2_ct = (analysisMetrics.shared_vocab)[v2][0];
			var v2_fr = ((analysisMetrics.shared_vocab)[v2][1]).toFixed(10);
			var v3_ct = (analysisMetrics.shared_vocab)[v3][0];
			var v3_fr = ((analysisMetrics.shared_vocab)[v3][1]).toFixed(10);

			this.setState({
				numberDocs: analysisMetrics.num_doc[0] + analysisMetrics.num_doc[1], //add num_req and num_src
				vocabSize: analysisMetrics.vocab_size[0] + analysisMetrics.vocab_size[1], //add vocab_req and vocab_src
				avgNumTokens: avNumTkn,
				//set vocab
				vocab1: v1,
				vocab2: v2,
				vocab3: v3,
				//set vocab counts
				vocab1_count: v1_ct,
				vocab2_count: v2_ct,
				vocab3_count: v3_ct,
				//set vocab frequencies
				vocab1_freq: v1_fr,
				vocab2_freq: v2_fr,
				vocab3_freq: v3_fr
			});
		});
	}

	//constructor(props) {
	//	super(props);

	//	this.artifactCardRefs = [];
	//	this.currentlySelectedArtifactIndex = -1;

	//	const artifactInfos = getAllArtifactInfos(this.currentArtifactClass);
	//	this.state = {
	//		artifactInfos: artifactInfos,
	//	};

	//	this.artifactCardRefs = artifactInfos.map((artifactInfo) => React.createRef());
	//}

	render() {
		return (
			<div className="metricBrowserContainer">
				<div className="artifactBrowser">
					
					<div className="sharedTable">
					
					<div className="sharedTitleRow">
					
					<div className="metricsTitle">
						&nbsp; &nbsp; &nbsp; Shared Metrics
					</div>
					
					<div className="heading headingSharedMetric">
						Vocab
					</div>
					
					</div>
					
					<div className="sharedContentRow">
					
					<table className="metricTable leftTable">
						<tr>
							<th>Metric</th>
							<th>Value</th>
							<th>Target Difference</th>
						</tr>
						<tr>
							<td>Number of Documents</td>
							<td>{this.state.numberDocs}</td>
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
										
					<table className="metricTable rightTable">
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
					
					</div>
					
					</div>

					<div style={{height: 20}}/>
				</div>
			</div>
		);
	}
}
