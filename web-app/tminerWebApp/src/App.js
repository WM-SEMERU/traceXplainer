import React, { useState, useEffect } from 'react';
import {
  BrowserRouter as Router,
  Switch,
  Route,
  Link
} from "react-router-dom";
import { Button, ButtonGroup } from '@blueprintjs/core';
import ArtifactNavigator from './ArtifactNavigator/ArtifactNavigator';
import logo from './logo.svg';
import './App.css';
import './assets/css/black-dashboard.css'
import './assets/css/nucleo-icons.css'

import '@blueprintjs/core/lib/css/blueprint.css'
import '@blueprintjs/table/lib/css/table.css';

function App() {
  const [dbitem, setDbItem] = useState([]);

  useEffect(() => {
    fetch('/api/getdb').then(res => res.json()).then(data => {
      setDbItem(data.item);
    });
  }, []);

  return (
    <body>
	
	<Router>
	
	<div class="page-title">
		<Switch>
          <Route exact path="/">
            T-MINER &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; Traceability
          </Route>
		  <Route path="/analysis">
            T-MINER &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; Analysis
          </Route>
          <Route path="/linkbrowser">
            T-MINER &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; Link Browser
          </Route>
        </Switch>
	</div>
	
    
	<div class="sidebar">
      <div class="sidebar-wrapper">		
		
        <ul class="nav">

          <li>
            <a>
              <i class="tim-icons icon-chart-pie-36"></i>
              <p class="sidebar-item"><Link to="/" className="link">Traceability</Link></p>
            </a>
          </li>
          <li>
            <a>
              <i class="tim-icons icon-atom"></i>
              <p class="sidebar-item"><Link to="/analysis" className="link">Analysis</Link></p>
            </a>
          </li>
          <li>
            <a>
              <i class="tim-icons icon-pin"></i>
              <p class="sidebar-item"><Link to="/linkbrowser" className="link">Link Browser</Link></p>
            </a>
          </li>

        </ul>
      </div>
	  
    </div>
	
	<div>
	    <Switch>
          <Route exact path="/">
            <Artifacts />
          </Route>
		  <Route path="/analysis">
            <Analysis />
          </Route>
          <Route path="/linkbrowser">
            <Links />
          </Route>
        </Switch>
	</div>
	
	</Router>
	
	</body>
  );
}

function Artifacts() {
  return (
    <div class="browser-container">
	  <ArtifactNavigator />
	</div>
  );
}

function Analysis() {
  return (
    <div class="browser-container">
      <h2>Not yet implemented</h2>
    </div>
  );
}

function Links() {
  return (
    <div class="browser-container">
      <h2>Not yet implemented</h2>
    </div>
  );
}

export default App;