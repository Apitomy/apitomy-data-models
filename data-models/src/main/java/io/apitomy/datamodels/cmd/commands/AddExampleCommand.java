/*
 * Copyright 2019 Red Hat
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.apitomy.datamodels.cmd.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.apitomy.datamodels.cmd.AbstractCommand;
import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.openapi.OpenApiExample;
import io.apitomy.datamodels.models.openapi.OpenApiExamplesParent;
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30Example;
import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30MediaType;
import io.apitomy.datamodels.models.openapi.v3x.v31.OpenApi31Example;
import io.apitomy.datamodels.paths.NodePath;
import io.apitomy.datamodels.paths.NodePathUtil;
import io.apitomy.datamodels.util.LoggerUtil;
import io.apitomy.datamodels.util.ModelTypeUtil;

/**
 * A command used to add an Example to a MediaType, Parameter, or Header.  If an example with the same name
 * already exists, this command does nothing.
 * @author eric.wittmann@gmail.com
 */
public class AddExampleCommand extends AbstractCommand {

    public NodePath _parentPath;
    public JsonNode _newExampleValue;
    public String _newExampleName;
    public String _newExampleSummary;
    public String _newExampleDescription;

    public boolean _exampleAdded;

    public AddExampleCommand() {
    }

    public AddExampleCommand(OpenApiExamplesParent parent, JsonNode example, String exampleName, String exampleSummary, String exampleDescription) {
        this._parentPath = NodePathUtil.createNodePath((Node) parent);
        this._newExampleValue = example;
        this._newExampleName = exampleName;
        this._newExampleSummary = exampleSummary;
        this._newExampleDescription = exampleDescription;
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#execute(Document)
     */
    @Override
    public void execute(Document document) {
        LoggerUtil.info("[AddExampleCommand_30] Executing.");
        this._exampleAdded = false;

        OpenApiExamplesParent examplesParent = (OpenApiExamplesParent) NodePathUtil.resolveNodePath(this._parentPath, document);
        if (this.isNullOrUndefined(examplesParent)) {
            return;
        }
        if (!this.isNullOrUndefined(examplesParent.getExamples()) && examplesParent.getExamples().containsKey(this._newExampleName)) {
            return;
        }

        OpenApiExample example = examplesParent.createExample();
        if (ModelTypeUtil.isOpenApi30Model(example)) {
            OpenApi30Example example3 = (OpenApi30Example) example;
            example3.setDescription(this._newExampleDescription);
            example3.setSummary(this._newExampleSummary);
            example3.setValue(this._newExampleValue);
        } else if (ModelTypeUtil.isOpenApi31Model(example)) {
            OpenApi31Example example3 = (OpenApi31Example) example;
            example3.setDescription(this._newExampleDescription);
            example3.setSummary(this._newExampleSummary);
            example3.setValue(this._newExampleValue);
        }
        examplesParent.addExample(this._newExampleName, example);
        this._exampleAdded = true;
    }

    /**
     * @see io.apitomy.datamodels.cmd.ICommand#undo(Document)
     */
    @Override
    public void undo(Document document) {
        LoggerUtil.info("[AddExampleCommand_30] Reverting.");
        if (!this._exampleAdded) {
            return;
        }
        OpenApiExamplesParent parent = (OpenApiExamplesParent) NodePathUtil.resolveNodePath(this._parentPath, document);
        if (this.isNullOrUndefined(parent) || this.isNullOrUndefined(parent.getExamples())) {
            return;
        }

        parent.removeExample(this._newExampleName);
    }

}
