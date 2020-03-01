/*
 * The MIT License
 *
 * Copyright (c) 2020, Filipe Cristovao
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.jenkinsci.plugins.workflow.multibranch;

import hudson.Extension;
import hudson.model.Action;
import hudson.model.Descriptor;
import hudson.model.DescriptorVisibilityFilter;
import hudson.model.TaskListener;
import org.jenkinsci.plugins.workflow.flow.FlowDefinition;
import org.jenkinsci.plugins.workflow.flow.FlowDefinitionDescriptor;
import org.jenkinsci.plugins.workflow.flow.FlowExecution;
import org.jenkinsci.plugins.workflow.flow.FlowExecutionOwner;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;

import java.util.List;

/**
 * Just a wrapper so that the description in the Workflow jobs are clear about where are the FlowDefinitions coming from.
 */
class FlowDefinitionWrapper extends FlowDefinition {

    private final FlowDefinition flowDefinition;

    public FlowDefinitionWrapper(FlowDefinition flowDefinition) {
        this.flowDefinition = flowDefinition;
    }

    @Override public FlowExecution create(FlowExecutionOwner handle, TaskListener listener, List<? extends Action> actions) throws Exception {
        return flowDefinition.create(handle, listener, actions);
    }

    @Extension public static class DescriptorImpl extends FlowDefinitionDescriptor {

        @Override public String getDisplayName() {
            return "Pipeline from multibranch configuration";
        }

    }

    /**
     * Display this in the r/o configuration for branch projects created by WorkflowDefinitionBranchProjectFactory,
     * but don't offer it on standalone jobs or in any other context.
     * */
    @Extension public static class OnlyShowForWorkflowJobsFromOurFactory extends DescriptorVisibilityFilter {

        @SuppressWarnings("rawtypes")
        @Override public boolean filter(Object context, Descriptor descriptor) {
            // Allow *only* ourselves to be shown for WorkflowJobs generated by WorkflowElsewhereBranchProjectFactory:
            if (context instanceof WorkflowJob
                && ((WorkflowJob) context).getParent() instanceof WorkflowMultiBranchProject
                && ((WorkflowMultiBranchProject) ((WorkflowJob) context).getParent()).getProjectFactory() instanceof WorkflowElsewhereBranchProjectFactory) {
                if (descriptor instanceof FlowDefinitionDescriptor) {
                    return descriptor instanceof DescriptorImpl;
                }
            }
            // Hide ourselves in any context that ins't our factory:
            if (descriptor instanceof DescriptorImpl) {
                return context instanceof WorkflowJob
                       && ((WorkflowJob) context).getParent() instanceof WorkflowMultiBranchProject
                       && ((WorkflowMultiBranchProject) ((WorkflowJob) context).getParent()).getProjectFactory() instanceof WorkflowElsewhereBranchProjectFactory;
            }
            return true;
        }

    }
}
