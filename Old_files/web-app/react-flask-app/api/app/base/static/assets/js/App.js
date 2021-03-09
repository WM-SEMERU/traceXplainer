// jshint esversion:6

import ArtifactNavigator from './ArtifactNavigator/ArtifactNavigator';
import React from 'react';
import Button from '@blueprintjs/core';
import ButtonGroup from '@blueprintjs/core';
import './App.css';

require('prismjs/components/prism-c');
require('@blueprintjs/core/lib/css/blueprint.css');
require('@blueprintjs/table/lib/css/table.css');

function App () {
	return (
		<div className="App" style={{display: 'flex', flexDirection: 'column', alignItems: 'center'}}>
			<ButtonGroup large style={{marginTop: 15}}>
				<Button active>Traceability</Button>
				<Button>Analysis</Button>
				<Button>Link Browser</Button>
			</ButtonGroup>
			<ArtifactNavigator />
		</div>
	);
}

export default App;
