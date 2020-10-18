import './ArtifactDetailsLink.css';
import 'prismjs/themes/prism.css';

import { Cell, Column, Table } from "@blueprintjs/table";
import { getArtifactClassName, getArtifactContent, getArtifactInfo, } from '../interfaces/ArtifactInterface';
import { getLinkThreshold, getTraceLinks, getAllTraceLinks } from '../interfaces/TraceabilityInterface';

import { Icon } from '@blueprintjs/core';
import Prism from 'prismjs';
import React from 'react';
import Checkbox from './Checkbox';

const OPTIONS = ["Links", "Non-Links", "Security Related", "Non Security Related", "Requirements", "Source Code", "Test Cases"];

export default class ArtifactDetails extends React.Component {

	state = {
		artifactInfo: null,
		artifactContent: null,
		traceLinks: null,
		totalTraceLinks: null,
		checkboxes: OPTIONS.reduce(
      (options, option) => ({
        ...options,
        [option]: false
      }),
      {}
    )
	}


	constructor(props) {
		super(props);

		this.codeRef = React.createRef();
		//this.totalTraceLinks = [];

	}

	componentDidMount() {
		this.loadTable();
	}

	loadTable(){
		//getAllTraceLinks();
		this.setState({
		}, () => {
			getAllTraceLinks().then((totalTraceLinks) => {
				this.setState({totalTraceLinks: totalTraceLinks});
			})
		});
	}

	loadArtifact(artifactInfo) {

		getAllTraceLinks();

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

			getAllTraceLinks().then((totalTraceLinks) => {
				this.setState({totalTraceLinks: totalTraceLinks});
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
		const traceLinksList = this.state.totalTraceLinks;

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

		const securityCellRenderer = (index) => {
			return <Cell>
				{getArtifactInfo('req', traceLinksList[index].thisSourceName)['security_status']}
			</Cell>
		}

		const sourceCellRenderer = (index) => {
			return <Cell>
				{traceLinksList[index].thisSourceName}
			</Cell>
		}

		//Todo, actually make work
		const sourceTypeCellRenderer = (index) => {
			return <Cell>
				{'Requirement'}
					</Cell>
		}

		const tbdCellRenderer = (index) => {
			return <Cell>
				{'TBD'}
			</Cell>
		}

		return (
		<Table class="link-table"
			numRows={traceLinksList.length}
			columnWidths={[null, null, null, null, null, null, null, null,]}
		>
			<Column name="Link Status" cellRenderer={linkStatusCellRenderer} />
			<Column name="Value" cellRenderer={valueCellRenderer} />
			<Column name="Security" cellRenderer={securityCellRenderer} />
			<Column name="Source Type" cellRenderer={sourceTypeCellRenderer} />
			<Column name="Source" cellRenderer={sourceCellRenderer} />
			<Column name="Target Type" cellRenderer={artifactTypeCellRenderer} />
			<Column name="Target" cellRenderer={targetNameCellRenderer} />
			<Column name="Feedback" cellRenderer={tbdCellRenderer} />
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
						<h1 style={{margin: 0,}}>{}</h1>
				</div>
				<div className="artifactContentContainer">

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
			<div className="artifactDetailsContainerLink">
				<div className="artifactDetailsLink"  >
					{this.getDetailsComponent()}
					<div style={{height: 20}}/>
				</div>
			</div>
		);
	}
}
