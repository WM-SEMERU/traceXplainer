import './ArtifactDetails.css';
import 'prismjs/themes/prism.css';

import { Cell, Column, Table } from "@blueprintjs/table";
import { getArtifactClassName, getArtifactContent, getArtifactClass} from '../interfaces/ArtifactInterface';
import { getLinkThreshold, getTraceLinks } from '../interfaces/TraceabilityInterface';
import Checkbox from './Checkbox'

import { Icon } from '@blueprintjs/core';
import Prism from 'prismjs';
import React from 'react';

const OPTIONS = ["Links", "Non-Links", "Source Code", "Test Cases"];


export default class ArtifactDetails extends React.Component {

	state = {
		artifactInfo: null,
		artifactContent: null,
		traceLinks: null,
		checkboxes: OPTIONS.reduce(
      (options, option) => ({
        ...options,
        [option]: true
      }),
      {}
    )
	}


	constructor(props) {
		super(props);

		this.codeRef = React.createRef();
		this.state.checkboxes['Links'] = true;
		this.state.checkboxes['Non-Links'] = true;
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

		var result = traceLinksList;
		const linkThreshold = getLinkThreshold();

		if(!this.state.checkboxes['Links']){
			result = result.filter(element => element.traceValue <= linkThreshold);
		}
		if(!this.state.checkboxes['Non-Links']){
			result = result.filter(element => element.traceValue > linkThreshold);
		}
		function srcFunction(element){
			if(getArtifactClass(element.artifactId) == 'src') {
				return false;
			}
			return true;
		}
		if(!this.state.checkboxes['Source Code']){
			result = result.filter(srcFunction);
		}

		function tcFunction(element){
			if(getArtifactClass(element.artifactId) == 'tc') {
				return false;
			}
			return true;
		}
		if(!this.state.checkboxes['Test Cases']){
			result = result.filter(tcFunction);
		}



		result.sort((a, b) => {
			return b.traceValue - a.traceValue;
		});
		const valueCellRenderer = (index) => {
			return <Cell>{result[index].traceValue}</Cell>
		}

		const targetNameCellRenderer = (index) => {
			return <Cell>{result[index].artifactId}</Cell>
		}

		const linkStatusCellRenderer = (index) => {
			const linkStatus = result[index].traceValue > linkThreshold;
			return <Cell style={{display: 'flex', justifyContent: 'center'}}>
				<Icon icon={linkStatus ? 'link' : 'delete'} color={linkStatus ? 'green' : 'red'}/>
			</Cell>
		}

		const artifactTypeCellRenderer = (index) => {
			return <Cell>
				{getArtifactClassName(result[index].artifactType)}
			</Cell>
		}

		const feedbackCellRenderer = (index) => {
			return <Cell>
				<form>
				<label for="cars"></label>
 <select name="cars" id="cars">
	 <option value="agree">Agree</option>
	 <option value="neutral">Neutral</option>
	 <option value="disagree">Disagree</option>
 </select>
				</form>
			</Cell>
		}

		return (
		<Table class="link-table"
			numRows={result.length}
			columnWidths={[null, null, null, null, null]}
		>
			<Column name="Link Status" cellRenderer={linkStatusCellRenderer} />
			<Column name="Value" cellRenderer={valueCellRenderer} />
			<Column name="Filename" cellRenderer={targetNameCellRenderer} />
			<Column name="Type" cellRenderer={artifactTypeCellRenderer} />
			<Column name="Feedback" cellRenderer={feedbackCellRenderer} />
		</Table>
		);
	}

	selectAllCheckboxes = isSelected => {
		Object.keys(this.state.checkboxes).forEach(checkbox => {
			// BONUS: Can you explain why we pass updater function to setState instead of an object?
			this.setState(prevState => ({
				checkboxes: {
					...prevState.checkboxes,
					[checkbox]: isSelected
				}
			}));
		});
	};

	selectAll = () => this.selectAllCheckboxes(true);

	deselectAll = () => this.selectAllCheckboxes(false);

	handleCheckboxChange = changeEvent => {
		const { name } = changeEvent.target;
		this.setState(prevState => ({
			checkboxes: {
				...prevState.checkboxes,
				[name]: !prevState.checkboxes[name]
			}
		}));
	};

	handleFormSubmit = formSubmitEvent => {
		formSubmitEvent.preventDefault();

		Object.keys(this.state.checkboxes)
			.filter(checkbox => this.state.checkboxes[checkbox])
			.forEach(checkbox => {
				console.log(checkbox, "is selected.");
			});
	};

	createCheckbox = option => (
		<Checkbox
			label={option}
			isSelected={this.state.checkboxes[option]}
			onCheckboxChange={this.handleCheckboxChange}
			key={option}
		/>
	);

	createCheckboxes = () => OPTIONS.map(this.createCheckbox);

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
				<div className="checkboxes">
				<form onSubmit={this.handleFormSubmit}>
							{this.createCheckboxes()}

				</form>
				</div>
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
