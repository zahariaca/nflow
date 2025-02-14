import React, {useState} from 'react';
import './App.scss';
import {HashRouter as Router, Switch, Route, Redirect} from 'react-router-dom';
import {Snackbar} from '@material-ui/core';

import {Navigation, Feedback, FeedbackContext} from './component';
import {FeedbackMessage} from './types';

// TODO get rid of default exports in Pages
import WorkflowDefinitionListPage from './workflow-definition/WorkflowDefinitionListPage';
import WorkflowDefinitionDetailsPage from './workflow-definition/WorkflowDefinitionDetailsPage';

import WorkflowInstanceListPage from './workflow-instance/WorkflowInstanceListPage';
import WorkflowInstanceDetailsPage from './workflow-instance/WorkflowInstanceDetailsPage';
import {CreateWorkflowInstancePage} from './workflow-instance/CreateWorkflowInstancePage';

import ExecutorListPage from './executor/ExecutorListPage';

import AboutPage from './about/AboutPage';
import NotFoundPage from './error/NotFoundPage';
import {ReturnLink} from './component/ReturnLink';

function App() {
  const [feedback, setFeedback] = useState<FeedbackMessage | undefined>();

  const addFeedback = (feedback: FeedbackMessage) => {
    setFeedback(feedback);
  };

  const closeFeedback = () => {
    setFeedback(undefined);
  };

  return (
    <Router hashType="hashbang">
      <FeedbackContext.Provider value={{addFeedback}}>
        <div className="App">
          <header>
            <ReturnLink />
            <Navigation />
          </header>
          <br />
          <Switch>
            <Route exact path="/">
              <Redirect to="/workflow" />
            </Route>
            <Route path="/search">
              <Redirect to="/workflow" />
            </Route>
            <Route path="/workflow/create">
              <CreateWorkflowInstancePage />
            </Route>
            <Route path="/workflow/:id">
              <WorkflowInstanceDetailsPage />
            </Route>
            <Route path="/workflow">
              <WorkflowInstanceListPage />
            </Route>
            <Route path="/workflow-definition/:type">
              <WorkflowDefinitionDetailsPage />
            </Route>
            <Route path="/workflow-definition">
              <WorkflowDefinitionListPage />
            </Route>
            <Route path="/executors">
              <ExecutorListPage />
            </Route>
            <Route path="/about">
              <AboutPage />
            </Route>

            <Route path="*">
              <NotFoundPage />
            </Route>
          </Switch>
          <Snackbar
            open={!!feedback}
            autoHideDuration={10000}
            anchorOrigin={{
              vertical: 'top',
              horizontal: 'right'
            }}
            onClose={closeFeedback}
          >
            {feedback && (
              <Feedback feedback={feedback} onClose={closeFeedback} />
            )}
          </Snackbar>
        </div>
      </FeedbackContext.Provider>
    </Router>
  );
}

export default App;
