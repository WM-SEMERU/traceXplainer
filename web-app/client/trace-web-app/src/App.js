import './App.css';

import { Button, ButtonGroup } from '@blueprintjs/core';

import ArtifactNavigator from './ArtifactNavigator/ArtifactNavigator';
import React from 'react';

require('prismjs/components/prism-c');
require('@blueprintjs/core/lib/css/blueprint.css');
require('@blueprintjs/table/lib/css/table.css');

function App() {
	return (
		<div className="App" style={{display: 'flex', flexDirection: 'column', alignItems: 'center'}}>
			<ButtonGroup large style={{marginTop: 15}}>
				<Button>Traceability</Button>
				<Button>Analysis</Button>
				<Button>Link Browser</Button>
			</ButtonGroup>
			<ArtifactNavigator />
		</div>
	);
}

export default App;
