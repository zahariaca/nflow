package io.nflow.rest.v1.msg;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.nflow.engine.model.ModelObject;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Response to wake up request.")
@SuppressFBWarnings(value = "URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD", justification = "jackson reads dto fields")
public class WakeupResponse extends ModelObject {
    @ApiModelProperty("List of expected states. Can be empty, meaning any state.")
    public boolean wakeupSuccess;
}
