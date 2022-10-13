package org.camunda.community.bpmndt

import org.gradle.testkit.runner.GradleRunner

import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.TempDir

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class GeneratorPluginTest extends Specification {
    @TempDir
    File testProjectDir
    File buildFile
    
    def setup() {
        buildFile = new File(testProjectDir, 'build.gradle')
        buildFile << """
            plugins {
                id 'org.camunda.community.bpmndt'
            }
        """
    }

    def "can successfully generate tests"() {
        buildFile << """
            generate {
            }
        """

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments('generate')
            .withPluginClasspath()
            .build()

        then:
        result.output.contains("generate test code.")
        result.task(":generate").outcome == SUCCESS
    }
}
