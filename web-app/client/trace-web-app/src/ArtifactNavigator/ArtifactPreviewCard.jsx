import React from 'react';
import { getArtifactContent } from '../interfaces/ArtifactInterface';

export default class ArtifactPreviewCard extends React.Component {

	state = {
		hovered: false,
		selected: false,
		loading: true,
		content: null,
	}

	componentDidMount() {
		getArtifactContent(this.props.artifactInfo.type, this.props.artifactInfo.id).then((content) => {
			this.setState({
				loading: false,
				content: content
			});
		});
	}

	reloadContent() {
		this.setState({loading: true});
		getArtifactContent(this.props.artifactInfo.type, this.props.artifactInfo.id).then((content) => {
			this.setState({
				loading: false,
				content: content
			})
		});
	}

	onMouseEnter() {
		this.setState({hovered: true});
	}

	onMouseLeave() {
		this.setState({hovered: false});
	}

	select() {
		this.setState({selected: true});
	}

	deselect() {
		this.setState({selected: false})
	}

	toggleSelect() {
		this.setState((state) => {
			return {
				selected: !state.selected
			}
		})
	}

	render() {
		return (
			<div 
			style={{
				maxWidth: '100%',
				display: 'flex',
				flexGrow: 1,
				backgroundColor: this.state.selected ? 'royalblue' : 'white',
				padding: 10,
				borderRadius: 10,
				borderWidth: 1,
				borderColor: 'black',
				boxShadow: '0px 0px 10px ' + (this.state.hovered ? 'royalblue' : 'lightgray'), 
			}} 
			onMouseEnter={() => {this.onMouseEnter()}}
			onMouseLeave={() => {this.onMouseLeave()}}
			onClick={() => {
				this.select();
				this.props.onClick();
			}}
			>
				<div style={{flexGrow: 1, display: 'flex', flexDirection: 'column', maxWidth: 'inherit'}}>
					<h3 style={{padding: 0, margin: 0, color: this.state.selected ? 'white' : 'black'}}>
						{this.props.artifactInfo.id}
					</h3>
					<div style={{flexGrow: 1, overflow: 'hidden'}}>
						<p style={{padding: 0, margin: 0, color: this.state.selected ? 'white' : 'black'}}>
							{this.state.loading ? '' : this.state.content}
						</p>
					</div>
					
				</div>
				
			</div>
		);
	}

}