import './ArtifactDetailsLink.css';
import 'prismjs/themes/prism.css';

import { Cell, Column, Table } from "@blueprintjs/table";
import { getArtifactClassName, getArtifactContent, getArtifactInfo, getArtifactClass} from '../interfaces/ArtifactInterface';
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
		//this.totalTraceLinks = [];

	}

	componentDidMount() {
		this.loadTable();
	}

	loadTable(){
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

		const linkThreshold = getLinkThreshold();
		var result = traceLinksList;
		if(!this.state.checkboxes['Links']){
			result = result.filter(element => element.traceValue <= linkThreshold);
		}
		if(!this.state.checkboxes['Non-Links']){
			result = traceLinksList.filter(element => element.traceValue > linkThreshold);
		}
		if(!this.state.checkboxes['Security Related']){ //if security_status=0, won't show
			result = result.filter(element =>
				getArtifactInfo('req', element.thisSourceName)['security_status'] == 0);
		}
		if(!this.state.checkboxes['Non Security Related']){ //if security_status=0, won't show
			result = result.filter(element =>
				getArtifactInfo('req', element.thisSourceName)['security_status'] == 1);
		}
		if(!this.state.checkboxes['Requirements']){
			result = result.filter(element =>
				getArtifactClass(element.artifactType) == 'req');
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

		//console.log(result);



		result.sort((a, b) => {
			return b.traceValue - a.traceValue;
		});

		//alert('length ' + result.length);
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
				{getArtifactClassName(getArtifactClass(result[index].artifactId))}
			</Cell>
		}

		const securityCellRenderer = (index) => {
			return <Cell>
				{getArtifactInfo('req', result[index].thisSourceName)['security_status']}
			</Cell>
		}

		const sourceCellRenderer = (index) => {
			return <Cell>
				{result[index].thisSourceName}
			</Cell>
		}

		//Todo, actually make work
		const sourceTypeCellRenderer = (index) => {
			return <Cell>
				{getArtifactClassName(getArtifactInfo('req', result[index].thisSourceName)['type'])}
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
			defaultRowHeight={30}
			columnWidths={[null, null, null, null, null, null, null, null,]}
		>
			<Column name="Link Status" cellRenderer={linkStatusCellRenderer} />
			<Column name="Value" cellRenderer={valueCellRenderer} />
			<Column name="Security" cellRenderer={securityCellRenderer} />
			<Column name="Source Type" cellRenderer={sourceTypeCellRenderer} />
			<Column name="Source" cellRenderer={sourceCellRenderer} />
			<Column name="Target Type" cellRenderer={artifactTypeCellRenderer} />
			<Column name="Target" cellRenderer={targetNameCellRenderer} />
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
						<h1 style={{margin: 0,}}>{}</h1>
				</div>
				<div className="artifactContentContainer">

				</div>

				<div className="traceLinksTableContainer">
				<div className="checkboxes">
				<form onSubmit={this.handleFormSubmit}>
              {this.createCheckboxes()}

							<div className="form-group mt-2">
                <button
                  type="button"
                  className="btn btn-outline-primary mr-2"
                  onClick={this.selectAll}
                >
                  Select All
                </button>
                <button
                  type="button"
                  className="btn btn-outline-primary mr-2"
                  onClick={this.deselectAll}
                >
                  Deselect All
                </button>
                <button type="submit" className="btn btn-primary">
                  Save
                </button>
              </div>

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
