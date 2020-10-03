import './ArtifactDetails.css';
import 'prismjs/themes/prism.css';
import './2.76480504.chunk.css';

import { Cell, Column, Table } from "@blueprintjs/table";
import { getArtifactClassName, getArtifactContent, } from '../interfaces/ArtifactInterface';
import { getLinkThreshold, getTraceLinks } from '../interfaces/TraceabilityInterface';

import { Icon } from '@blueprintjs/core';
import Prism from 'prismjs';
import React from 'react';

export default class ArtifactDetails extends React.Component {

	state = {
		artifactInfo: null,
		artifactContent: null,
		traceLinks: null,
	}


	constructor(props) {
		super(props);

		this.codeRef = React.createRef();
	}

	loadArtifact(artifactInfo) {

		this.setState({
			artifactContent: null,
			artifactInfo: artifactInfo,
		}, () => {
			getArtifactContent(artifactInfo.type, artifactInfo.id).then((artifactContent) => {
				this.setState({artifactContent: artifactContent}, () => {
					if (artifactInfo.type !== "req") {
						Prism.highlightElement(this.codeRef.current);
					}
				});
			});
			
			getTraceLinks(artifactInfo.id).then((traceLinks) => {
				this.setState({traceLinks: traceLinks});
			})
		});
	}

	unloadArtifact() {
		this.setState({artifactInfo: null, artifactContent: null});
	}

	getArtifactTitle() {
		return this.state.artifactInfo ? this.state.artifactInfo.id : '';
	}

	getArtifactComponent() {
		const content = this.state.artifactContent ? this.state.artifactContent : '';
		if (this.state.artifactInfo.type === 'req') {
			return content;
		} else {
			return <code ref={this.codeRef} className="language-c" style={{margin: 0}}>
				{content}
			</code>
		}
		
	}

	getNoSelectionComponent() {
		return (
			<div className="noArtifactSelectedContainer">
				<p class="please-select">Please select a source artifact</p>
			</div>
		)
	}

	getTraceLinksTable() {
		const traceLinksList = this.state.traceLinks;
		if (!traceLinksList) {
			return null;
		}

		traceLinksList.sort((a, b) => {
			return b.traceValue - a.traceValue;
		});
		const valueCellRenderer = (index) => {
			return <Cell>{traceLinksList[index].traceValue}</Cell>
		}

		const targetNameCellRenderer = (index) => {
			return <Cell>{traceLinksList[index].artifactId}</Cell>
		}

		const linkThreshold = getLinkThreshold();
		const linkStatusCellRenderer = (index) => {
			const linkStatus = traceLinksList[index].traceValue > linkThreshold;
			return <Cell style={{display: 'flex', justifyContent: 'center'}}>
				<Icon icon={linkStatus ? 'link' : 'delete'} color={linkStatus ? 'green' : 'red'}/>
			</Cell>
		}

		const artifactTypeCellRenderer = (index) => {
			return <Cell>
				{getArtifactClassName(traceLinksList[index].artifactType)}
			</Cell>
		}

		return (
		<Table class="link-table"
			numRows={traceLinksList.length}
			columnWidths={[null, null, null, null,]}
		>
			<Column name="Link Status" cellRenderer={linkStatusCellRenderer} />
			<Column name="Value" cellRenderer={valueCellRenderer} />
			<Column name="Filename" cellRenderer={targetNameCellRenderer} />
			<Column name="Type" cellRenderer={artifactTypeCellRenderer} />
		</Table>
		);
	}

	getDetailsComponent() {
		return (
			<div className="artifactSelectedContainer">
				<div style={{padding: 15}}>
						<h1 style={{margin: 0,}}>{this.getArtifactTitle()}</h1>
				</div>
				<div className="artifactContentContainer">

					<pre className="artifactContent" 
					style={{
						whiteSpace: this.state.artifactInfo.type === 'req' ? 'pre-wrap' : null,
					}}>
						{this.getArtifactComponent()}
					</pre>

				</div>
				<div className="traceLinksTableContainer">
					<h2 style={{margin: 0}}>Trace Links</h2>
					{this.getTraceLinksTable()}
				</div>
			</div>
		);
	}

	render() {
		return (
			<div className="artifactDetailsContainer">
				<div className="artifactDetails">
					{this.state.artifactInfo ? this.getDetailsComponent() : this.getNoSelectionComponent()}
					<div style={{height: 20}}/>
				</div>
			</div>
		);
	}
}