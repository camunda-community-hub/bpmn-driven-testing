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
    

	@Ignore
    def "can successfully configure URL through extension and verify it"() {
        buildFile << """
            verification {
                url = 'https://www.google.com/'
            }
        """

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments('verifyUrl')
            .withPluginClasspath()
            .build()

        then:
        result.output.contains("Successfully resolved URL 'https://www.google.com/'")
        result.task(":verifyUrl").outcome == SUCCESS
    }
}
