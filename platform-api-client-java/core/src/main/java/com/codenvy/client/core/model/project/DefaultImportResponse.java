/*******************************************************************************
 * Copyright (c) [2012] - [2017] Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.client.core.model.project;

import com.codenvy.client.core.model.DefaultProject;
import com.codenvy.client.model.Project;
import com.codenvy.client.model.project.ImportResponse;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

/**
 * @author Florent Benoit
 */
@JsonInclude(NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DefaultImportResponse implements ImportResponse {

    private DefaultProject defaultProject;

    @JsonCreator
    public DefaultImportResponse(
            @JsonProperty("projectDescriptor") DefaultProject defaultProject) {
        this.defaultProject = defaultProject;
    }

    @JsonProperty("projectDescriptor")
    @Override
    public Project project() {
        return this.defaultProject;
    }
}
