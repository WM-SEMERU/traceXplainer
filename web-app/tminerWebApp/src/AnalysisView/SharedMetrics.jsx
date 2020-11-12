import './SharedMetrics.css';

import {HTMLSelect, InputGroup} from '@blueprintjs/core';

import React from 'react';
import { getAllArtifactInfos } from '../interfaces/ArtifactInterface';
import { getAnalysisMetrics } from '../interfaces/ArtifactInterface'; //ADDED

export default class SharedMetrics extends React.Component {

	state = {
		artifactInfos: null,
		//ADDED ATTRIBUTES
		analysisMetrics: null,
		numberDocs: 0,
		vocabSize: 0,
		avgNumTokens: 0,
		reqVocab: {},
		srcVocab: {},
		sharedVocab: {},
	}

	currentArtifactClass = 'req';
	
	//ADDED to retrieve data using api methods and update state 
	componentDidMount() {
		getAnalysisMetrics().then((analysisMetrics) => {
			console.log(analysisMetrics);
			this.setState({
				numberDocs: analysisMetrics.num_doc //(metrics.num_doc) //[0] + metrics.num_doc[1]), //add num_req and num_src
				//vocabSize: analysisMetrics.vocab_size, //(metrics.vocab_size[0] + metrics.vocab_size[1]), //add vocab_req and vocab_src
				//avgNumTokens: analysisMetrics.avg_tokens,
				//avgNumTokens: (metrics.avg_tokens[0] + metrics.avg_tokens[1]), //add token_req and token_src
				//reqVocab: analysisMetrics.rec_vocab //recVocab: metrics.rec_vocab
			});
		});
	}

	//reloadContent() {
		//this.setState({loading: true});
	//	getAnalysisMetrics().then((analysisMetrics) => {
	//		this.setState({
	//			numberDocs: analysisMetrics.num_doc, //(metrics.num_doc) //[0] + metrics.num_doc[1]), //add num_req and num_src
	//			vocabSize: analysisMetrics.vocab_size, //(metrics.vocab_size[0] + metrics.vocab_size[1]), //add vocab_req and vocab_src
	//			avgNumTokens: analysisMetrics.avg_tokens,
				//avgNumTokens: (metrics.avg_tokens[0] + metrics.avg_tokens[1]), //add token_req and token_src
	//			reqVocab: analysisMetrics.rec_vocab //recVocab: metrics.rec_vocab
	//                })
	//        });
        //}

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
							<td>{this.state.num_doc}</td>
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
