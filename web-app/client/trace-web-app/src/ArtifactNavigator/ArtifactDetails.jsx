import './ArtifactDetails.css';
import 'prismjs/themes/prism.css';

import { Cell, Column, Table } from "@blueprintjs/table";
import { getArtifactClassName, getArtifactContent, getArtifactInfo } from '../interfaces/ArtifactInterface';
import { getLinkThreshold, getTraceLinks } from '../interfaces/TraceabilityInterface';

import { Icon } from '@blueprintjs/core';
import Prism from 'prismjs';
import React from 'react';

export default class ArtifactDetails extends React.Component {

	state = {
		artifactInfo: null,
		artifactContent: null,
	}


	constructor(props) {
		super(props);

		this.codeRef = React.createRef();
	}

	loadArtifact(artifactInfo) {

		this.setState({
			artifactInfo: artifactInfo,
		}, () => {
			getArtifactContent(artifactInfo.type, artifactInfo.id).then((artifactContent) => {
				this.setState({artifactContent: artifactContent}, () => {
					if (artifactInfo.type !== "req") {
						Prism.highlightElement(this.codeRef.current);
					}
				});
			});
			
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
			<div style={{display: 'flex', flexGrow: 1, justifyContent: 'center', alignItems: 'center'}}>
				<p>Please select a source artifact</p>
			</div>
		)
	}

	getTraceLinksTable() {
		const traceLinks = getTraceLinks(this.state.artifactInfo.id);
		const traceLinksList = Object.keys(traceLinks).map((targetName) => {
			return {
				artifactName: targetName,
				traceValue: traceLinks[targetName]
			}
		});
		traceLinksList.sort((a, b) => {
			return b.traceValue - a.traceValue;
		});
		const valueCellRenderer = (index) => {
			return <Cell>{traceLinksList[index].traceValue}</Cell>
		}

		const targetNameCellRenderer = (index) => {
			return <Cell>{traceLinksList[index].artifactName}</Cell>
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
				{getArtifactClassName(getArtifactInfo(traceLinksList[index].artifactName).type)}
			</Cell>
		}

		return <Table numRows={Object.keys(traceLinks).length}>
			<Column name="Link Status" cellRenderer={linkStatusCellRenderer} />
			<Column name="Value" cellRenderer={valueCellRenderer} />
			<Column name="Filename" cellRenderer={targetNameCellRenderer} />
			<Column name="Type" cellRenderer={artifactTypeCellRenderer} />
		</Table>
	}

	getDetailsComponent() {
		return (
			<div style={{flexGrow: 1, display: 'flex', flexDirection: 'column', maxHeight: '100%'}}>
				<div style={{padding: 15}}>
						<h1 style={{margin: 0,}}>{this.getArtifactTitle()}</h1>
				</div>
				<div style={{display: 'flex', flexDirection: 'column', alignItems: 'stretch', padding: 10, flexGrow: 0, maxHeight: '40%'}}>

					<pre 
					style={{
						maxHeight: '100%',
						margin: 0, 
						fontFamily: 'Courier', 
						whiteSpace: this.state.artifactInfo.type === 'req' ? 'pre-wrap' : null,
						flexGrow: 1,
						backgroundColor: 'whitesmoke',
						borderWidth: 1,
						borderColor: 'black',
						borderStyle: 'solid',

						overflow: 'scroll',
						padding: 10,
					}}
					>
						{this.getArtifactComponent()}
					</pre>

				</div>
				<div style={{display: 'flex', flexDirection: 'column', padding: 10, paddingTop: 0, flexGrow: 1,}}>
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