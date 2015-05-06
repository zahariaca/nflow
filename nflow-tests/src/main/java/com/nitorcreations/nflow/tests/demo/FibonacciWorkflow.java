package com.nitorcreations.nflow.tests.demo;

import com.nitorcreations.nflow.engine.workflow.definition.NextAction;
import com.nitorcreations.nflow.engine.workflow.definition.StateExecution;
import com.nitorcreations.nflow.engine.workflow.definition.StateVar;
import com.nitorcreations.nflow.engine.workflow.definition.WorkflowDefinition;
import com.nitorcreations.nflow.engine.workflow.definition.WorkflowState;
import com.nitorcreations.nflow.engine.workflow.definition.WorkflowStateType;
import com.nitorcreations.nflow.engine.workflow.instance.QueryWorkflowInstances;
import com.nitorcreations.nflow.engine.workflow.instance.WorkflowInstance;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static com.nitorcreations.nflow.engine.workflow.definition.WorkflowStateType.*;

/**
 * Fibonacci series generator using recursive process.
 * Each step is handled by a new child workflow.
 */
public class FibonacciWorkflow extends WorkflowDefinition<FibonacciWorkflow.State> {
    public static final String WORKFLOW_TYPE = "fibonacci";
    private static final Logger logger = LoggerFactory.getLogger(FibonacciWorkflow.class);

    public static enum State implements WorkflowState {
        begin(start), nMinus1(normal), nMinus2(normal), poll(normal), done(end), error(manual);

        private WorkflowStateType type;

        private State(WorkflowStateType type) {
            this.type = type;
        }

        @Override
        public WorkflowStateType getType() {
            return type;
        }

        @Override
        public String getName() {
            return name();
        }

        @Override
        public String getDescription() {
            return name();
        }
    }

    public FibonacciWorkflow() {
        super(WORKFLOW_TYPE, State.begin, State.error);
        permit(State.begin, State.nMinus2);
        permit(State.nMinus2, State.nMinus1);
        permit(State.nMinus2, State.poll);
        permit(State.nMinus1, State.poll);
        permit(State.nMinus1, State.done);
        permit(State.nMinus2, State.done);
        permit(State.poll, State.done);
    }

    public NextAction begin(StateExecution execution,  @StateVar(value="requestData", readOnly=true) int n) {
        logger.info("Fibonacci step N = {}", n);
        execution.setVariable("result", 0);
        return NextAction.moveToState(State.nMinus2, "Starting N = " + n);
    }

    public NextAction nMinus2(StateExecution execution, @StateVar(value="requestData", readOnly=true) int n) {
        return nextStep(execution, n - 2, 2, State.nMinus1);
    }

    public NextAction nMinus1(StateExecution execution, @StateVar(value="requestData", readOnly = true) int n) {
        return nextStep(execution, n - 1, 1, State.poll);
    }

    private NextAction nextStep(StateExecution execution, int nextN, int offset, State nextState) {
        if(nextN < 2) {
            logger.info("nextN = {}. skipping to done", nextN);
            execution.setVariable("result", execution.getVariable("result", Integer.class) + 1);
            return NextAction.moveToState(nextState, "N - " + offset + " = " + nextN + ". Going to end.");
        }

        execution.setVariable("childrenCount", String.valueOf(getChildrenCount(execution) + 1));
        logger.info("Create child workflow N={}", nextN);
        List<WorkflowInstance> childWorkflows = Arrays.asList(createWorkflow(nextN));
        return NextAction.moveToState(nextState, childWorkflows, "Creating childWorkflow to process f(" + nextN + ")");
    }

    private int getChildrenCount(StateExecution execution) {
        try {
            return execution.getVariable("childrenCount", Integer.class);
        } catch (RuntimeException e) {
            return 0;
        }
    }

    public NextAction poll(StateExecution execution, @StateVar(value="requestData") int n) {
        // get finished and failed child workflows
        List<WorkflowInstance> children = execution.getChildWorkflows(new QueryWorkflowInstances.Builder()
                .addStatuses(manual.getStatus(), end.getStatus()).build());
        if(children.size() < getChildrenCount(execution)) {
            int delay = new Random().nextInt(5) + 1;
            return NextAction.retryAfter(DateTime.now().plusSeconds(delay), "Child workflows are not ready yet.");
        }
        int sum = 0;
        for(WorkflowInstance child : children) {
            if(child.status != WorkflowInstance.WorkflowInstanceStatus.finished) {
                return NextAction.stopInState(State.error, "Some of the children failed");
            }
            sum += Integer.parseInt(child.stateVariables.getOrDefault("result", "0"));
        }
        execution.setVariable("result", execution.getVariable("result", Integer.class) + sum);
        return NextAction.moveToState(State.done, "All is good");
    }

    public void done(StateExecution execution, @StateVar(value="requestData") int n, @StateVar(value="result") int result) {
        logger.info("We are done: fibonacci({}) == {}", n, result);
    }

    public void error(StateExecution execution, @StateVar(value="requestData") int n, @StateVar(value="result") int result) {
        logger.error("Failed to compute F({})", n);
    }
    private WorkflowInstance createWorkflow(int n) {
        // TODO these must be set by engine
        // TODO must check that type exists etc
        WorkflowInstance child = new WorkflowInstance.Builder()
                .setType(FibonacciWorkflow.WORKFLOW_TYPE)
                .setStatus(WorkflowInstance.WorkflowInstanceStatus.created)
                .setExternalId("" + new Random().nextLong())
                .setState(FibonacciWorkflow.State.begin.name())
                .setNextActivation(DateTime.now())
                .setStateVariables(Collections.singletonMap("requestData", "" + n)).build();
        return child;
    }
}
