import React from 'react';
import ReactDOM from 'react-dom';
import './index.css';
import App from './App';
import * as serviceWorker from './serviceWorker';

var rootRenderTarget = document.getElementById('root');

if (rootRenderTarget) {
    ReactDOM.render(
  <React.StrictMode>
    <App />
  </React.StrictMode>,
  document.getElementById('root') //CJ-this finds the root element in public/index.html and renders it as a react element
);
}

// If you want your app to work offline and load faster, you can change
// unregister() to register() below. Note this comes with some pitfalls.
// Learn more about service workers: https://bit.ly/CRA-PWA
serviceWorker.unregister();

/*
import React from "react";
import ReactDOM from "react-dom";
import { BrowserRouter, Link, Route } from "react-router-dom";

import "./styles.css";

const App = () => {
  return (
    <div>
      <nav>
        <Link to="/">Home</Link>
        <br />
        <Link to="/dashboard">Dashboard</Link>
      </nav>
      <div>
        <Route exact path="/" render={() => <h1>Welcome to Home!</h1>} />
      </div>
      <div>
        <Route path="/dashboard" component={Dashboard} />
      </div>
    </div>
  );
};

const Dashboard = () => {
  return <h1>Welcome to the Dashboard</h1>;
};

const Router = () => {
  return (
    <BrowserRouter>
      <App />
    </BrowserRouter>
  );
};

const rootElement = document.getElementById("root");
ReactDOM.render(<Router />, rootElement);
*/
