package org.camunda.community.bpmndt;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class GeneratorPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        // TODO Auto-generated method stub
    	project.getTasks().register("generate", GeneratorTask.class);
        
    }

}