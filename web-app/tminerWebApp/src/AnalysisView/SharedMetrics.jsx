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
		vocabSize: 0,
		avgNumTokens: 0,
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
			var vocab = analysisMetrics.shared_vocab;
			//retrieve shared vocab
			var v1 = Object.keys(vocab)[0];
			var v2 = Object.keys(vocab)[1];
			var v3 = Object.keys(vocab)[2];
			//retrieve shared vocab counts/frequencies
			var v1_ct = (vocab)[v1][0];
			var v1_fr = ((vocab)[v1][1]).toFixed(10);
			var v2_ct = (vocab)[v2][0];
			var v2_fr = ((vocab)[v2][1]).toFixed(10);
			var v3_ct = (vocab)[v3][0];
			var v3_fr = ((vocab)[v3][1]).toFixed(10);

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
						</tr>
						<tr>
							<td>Number of Documents</td>
							<td>{this.state.numberDocs}</td>
						</tr>
						<tr>
							<td>Vocabulary Size</td>
							<td>{this.state.vocabSize}</td>
						</tr>
						<tr>
							<td>Avg. Number of Tokens per Document</td>
							<td>{this.state.avgNumTokens}</td>
						</tr>
					</table>
										
					<table className="metricTable rightTable">
						<tr>
							<th>Token</th>
							<th>Count</th>
							<th>Frequency</th>
						</tr>
						<tr>
							<td>{this.state.vocab1}</td>
							<td>{this.state.vocab1_count}</td>
							<td>{this.state.vocab1_freq}</td>
						</tr>
						<tr>
							<td>{this.state.vocab2}</td>
							<td>{this.state.vocab2_count}</td>
							<td>{this.state.vocab2_freq}</td>
						</tr>
						<tr>
							<td>{this.state.vocab3}</td>
							<td>{this.state.vocab3_count}</td>
							<td>{this.state.vocab3_freq}</td>
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
