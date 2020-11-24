import './ArtifactBrowser.css';

import {HTMLSelect, InputGroup, Tooltip, Button, Intent, Position} from '@blueprintjs/core';

import { getLinkThreshold, getTraceLinks } from '../interfaces/TraceabilityInterface';

import ArtifactPreviewCard from './ArtifactPreviewCard';
import React from 'react';
import { getAllArtifactInfos } from '../interfaces/ArtifactInterface';
import Checkbox from './Checkbox'

const REQ_ARTIFACT_OPTIONS = ["Security-Related", "Non Security-Related", ".txt", "Orphans", "Non-Orphans"];

export default class ArtifactBrowser extends React.Component {

	state = {
		traceLinks: null,
		artifactInfos: null,
		checkboxes: REQ_ARTIFACT_OPTIONS.reduce(
      (options, option) => ({
        ...options,
        [option]: true
      }),
      {}
    ),
		showFilters: true,
	}

	currentArtifactClass = 'req';

	constructor(props) {
		super(props);

		this.artifactCardRefs = [];
		this.currentlySelectedArtifactIndex = -1;

		const artifactInfos = getAllArtifactInfos(this.currentArtifactClass);
		this.state = {
			traceLinks: null,
			artifactInfos: artifactInfos,
			showFilters: true,
			checkboxes: REQ_ARTIFACT_OPTIONS.reduce(
	      (options, option) => ({
	        ...options,
	        [option]: true
	      }),
	      {}
	    )
		};

		this.artifactCardRefs = artifactInfos.map((artifactInfo) => React.createRef());
	}



	fetchArtifacts(artifactClass) {
		const artifactInfos = getAllArtifactInfos(artifactClass);

		this.artifactCardRefs = artifactInfos.map((artifactInfo) => React.createRef());
		this.setState({
			artifactInfos: artifactInfos
		}, () => {
			this.artifactCardRefs.forEach((ref) => {
				if (ref.current) {
					ref.current.reloadContent()
				}
			})
		});

	}

	deselectCurrentlySelectedArtifact() {
		if (this.currentlySelectedArtifactIndex !== -1) {
			this.artifactCardRefs[this.currentlySelectedArtifactIndex].current.deselect();
		}
		this.currentlySelectedArtifactIndex = -1;
	}

	getArtifactPreviewCards() {
		var artifactCardList = this.state.artifactInfos.map((artifactInfo, index) => {
			const cardHeight = artifactInfo.type === 'req' ? '150px' : '65px';
				return (
					<div className="artifactPreviewContainer" style={{height: cardHeight,}}>
						<ArtifactPreviewCard
							ref={this.artifactCardRefs[index]}
							artifactInfo={artifactInfo}
							onClick={() => {
								this.deselectCurrentlySelectedArtifact()
								this.currentlySelectedArtifactIndex = index;
								this.props.onArtifactSelect(artifactInfo, this.currentArtifactClass);
							}}
						/>
					</div>
				);
			}
	);

	if(!this.state.checkboxes['Security-Related']){
		artifactCardList = artifactCardList.filter(element => element.props.children.props.artifactInfo.security_status == '0');
	}
	if(!this.state.checkboxes['Non Security-Related']){
		artifactCardList = artifactCardList.filter(element => element.props.children.props.artifactInfo.security_status == '1');
	}
	if(!this.state.checkboxes['.txt']){
		artifactCardList = artifactCardList.filter(element => {
			var str = element.props.children.props.artifactInfo.id;
			return !str.endsWith('.txt');
		});
	}

	//getTraceLinks('RQ38.txt').then((traceLinks) => {
		//this.setState({traceLinks: traceLinks});
	//});
	//console.log(this.state.traceLinks);
	if(!this.state.checkboxes['Orphans']){
		artifactCardList = artifactCardList.filter(element => element.props.children.props.artifactInfo.security_status == '1');
	}
	if(!this.state.checkboxes['Non-Orphans']){
		artifactCardList = artifactCardList.filter(element => element.props.children.props.artifactInfo.security_status == '1');
	}

	return artifactCardList;
	}

	handleCheckboxChange = changeEvent => {
		this.deselectCurrentlySelectedArtifact();
		this.props.onArtifactDeselect();
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

	createCheckboxes = () => REQ_ARTIFACT_OPTIONS.map(this.createCheckbox);

	handleFilterClick = () => {
		console.log('clicked');
		this.setState({ showFilters: !this.state.showFilters });
	};

	render() {
		const filter = (
            <Tooltip content={`Show Filters`}>
                <Button
                    icon={'list'}
                    intent={Intent.WARNING}
                    minimal={true}
                    onClick={this.handleFilterClick}
                />
            </Tooltip>
        );

		const checkboxes = (
			<div className="checkboxes">
			<form onSubmit={this.handleFormSubmit}>
						{this.createCheckboxes()}
			</form>
			</div>
		);

		return (
			<div className="artifactBrowserContainer">
				<div className="artifactBrowser">
					<div className="aartifactClassSelectorContainer">
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

					<div className="artifactFiltersContainer">
						<InputGroup large className="artifactFilters" leftElement={filter}/>
						{ this.state.showFilters && this.currentArtifactClass=='req' ? checkboxes : null }
						{ this.state.showFilters && this.currentArtifactClass=='src' ? checkboxes : null }
					</div>

					<div className="artifactPreviewsContainer">
						{this.getArtifactPreviewCards()}
					</div>
					<div style={{height: 20}}/>
				</div>
			</div>
		);
	}


}
