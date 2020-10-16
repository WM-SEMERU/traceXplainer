import React, { useState, useEffect } from 'react';
import { BrowserRouter, Route, Switch, Link } from 'react-router-dom';
import { Button, ButtonGroup } from '@blueprintjs/core';
import ArtifactNavigator from './ArtifactNavigator/ArtifactNavigator';
import Analysis from './assets/js/Analysis'
import LinkBrowser from './assets/js/LinkBrowser'
import Navbar from './assets/js/Navbar'
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
      <BrowserRouter>
          <div>
            <div class="sidebar">
                <div class="sidebar-wrapper">
                  <div class="logo">
                    <a target="_blank" rel="sponsored noopener noreferrer" class="simple-text logo-normal title">
                      T-Miner
                    </a>
                  </div>
                  <ul class="nav">
                    <li class="{% if 'index' in segment %} active {% endif %}" >
                      <Link to="/home">Traceability</Link>
                        <i class="tim-icons icon-chart-pie-36"></i>
                        <p class="sidebar-item">Traceability</p>
                    </li>
                    <li class="{% if 'icons' in segment %} active {% endif %}" >
                      <Link to="/analysis">Analysis</Link>
                        <i class="tim-icons icon-atom"></i>
                        <p class="sidebar-item">Analysis</p>
                    </li>
                    <li class="{% if 'icons' in segment %} active {% endif %}" >
                      <Link to="/linkbrowser">Link Browser</Link>
                        <i class="tim-icons icon-atom"></i>
                        <p class="sidebar-item">Link Browser</p>
                    </li>
                  </ul>
                </div>
              </div>

            <Switch>
              <Route path="/home">
                <div class="browser-container"><ArtifactNavigator /></div>
              </Route>
              <Route path="/analysis">
                <div class="browser-container"><Analysis /></div>
              </Route>
              <Route path="/linkbrowser">
                <div class="browser-container"><LinkBrowser /></div>
              </Route>
            </Switch>
          </div>
        </BrowserRouter>



	</body>
);
}

export default App;

/*
<div class="sidebar">
    <div class="sidebar-wrapper">
      <div class="logo">
        <a target="_blank" rel="sponsored noopener noreferrer" class="simple-text logo-normal title">
          T-Miner
        </a>
      </div>

      <ul class="nav">

        <li class="{% if 'index' in segment %} active {% endif %}" >
          <a href="/">
            <i class="tim-icons icon-chart-pie-36"></i>
            <p class="sidebar-item">Traceability</p>
          </a>
        </li>
        <li class="{% if 'icons' in segment %} active {% endif %}" >
          <a href="test.js">
            <i class="tim-icons icon-atom"></i>
            <p class="sidebar-item">Analysis</p>
          </a>
        </li>
        <li class="{% if 'maps' in segment %} active {% endif %}" >
          <a href="/ui-maps.html">
            <i class="tim-icons icon-pin"></i>
            <p class="sidebar-item">Link Browser</p>
          </a>
        </li>

      </ul>

    </div>
  </div>

  <div class="browser-container">
    <ArtifactNavigator />
  </div>

*/
