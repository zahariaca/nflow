package com.nitorcreations.nflow.rest.v0.msg;

import org.joda.time.DateTime;

import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

@ApiModel(value = "Basic information of workflow instance")
public class ListWorkflowInstanceResponse {

  @ApiModelProperty(value = "Idenfier of the new workflow instance", required=true)
  public int id;

  @ApiModelProperty(value = "Workflow definition identifier", required=true)
  public String type;

  @ApiModelProperty(value = "Main business key or identifier for the started workflow instance", required=false)
  public String businessKey;

  @ApiModelProperty(value = "State of the workflow instance", required=true)
  public String state;

  @ApiModelProperty(value = "Text of describing the reason for state (free text)", required=false)
  public String stateText;

  @ApiModelProperty(value = "Next activation time for workflow instance processing", required=false)
  public DateTime nextActivation;

}
