package ust.tad.dockerplugin.analysis;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ust.tad.dockerplugin.analysis.util.ComponentTypeProvider;
import ust.tad.dockerplugin.analysis.util.MissingBaseTypeException;
import ust.tad.dockerplugin.analysis.util.MissingDockerImageException;
import ust.tad.dockerplugin.models.tadm.Artifact;
import ust.tad.dockerplugin.models.tadm.Component;
import ust.tad.dockerplugin.models.tadm.ComponentType;
import ust.tad.dockerplugin.models.tadm.TechnologyAgnosticDeploymentModel;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class DockerImageAnalysisTest {
    @Autowired private DockerImageAnalysisService dockerImageAnalysisService;
    @Autowired private ComponentTypeProvider componentTypeProvider;

    @Test
    public void testAnalysisSuccessful() throws MissingBaseTypeException, MissingDockerImageException {
        TechnologyAgnosticDeploymentModel tadm = createDummyModel();
        System.out.println("Transformation input: " + tadm);
        for (Component componentToAnalyze: tadm.getComponents()) {
            tadm = dockerImageAnalysisService.analyzeDockerImageOfComponent(componentToAnalyze, tadm);
        }
        TechnologyAgnosticDeploymentModel expectedTADM = createExpectedTransformationResult();
        System.out.println("Transformation result: " + tadm);
        System.out.println("Expected result: " + expectedTADM);
    }

    private TechnologyAgnosticDeploymentModel createDummyModel() {
        TechnologyAgnosticDeploymentModel tadm = new TechnologyAgnosticDeploymentModel();

        ComponentType baseType = new ComponentType();
        baseType.setName("BaseType");
        tadm.setComponentTypes(new ArrayList<>(List.of(baseType)));

        createComponentsWithArtifacts(tadm, List.of("registry/postgres:6.7.8-bla", "minio",
                "kafka", "reg/registry/postgres:6.7.8-bla", "///mysql", "mongo:456", "random"));

        return tadm;
    }

    private TechnologyAgnosticDeploymentModel createExpectedTransformationResult() {
        TechnologyAgnosticDeploymentModel tadm = new TechnologyAgnosticDeploymentModel();

        ComponentType baseType = new ComponentType();
        baseType.setName("BaseType");

        ComponentType softwareApplicationType = componentTypeProvider.createSoftwareApplicationType(baseType);
        ComponentType databaseSystemType = componentTypeProvider.createDatabaseSystemType(softwareApplicationType);

        Artifact artifact = new Artifact();
        artifact.setType("docker_image");
        artifact.setName("registry/postgres:6.7.8-bla");

        Component dummyComponent = new Component();
        dummyComponent.setName("dummyComponent");
        dummyComponent.setType(databaseSystemType);
        dummyComponent.setArtifacts(new ArrayList<>(List.of(artifact)));

        tadm.setComponentTypes(new ArrayList<>(List.of(baseType, softwareApplicationType, databaseSystemType)));
        tadm.setComponents(new ArrayList<>(List.of(dummyComponent)));

        return tadm;
    }

    private void createComponentsWithArtifacts(
            TechnologyAgnosticDeploymentModel tadm, List<String> artifactNames) {
        for (String artifactName: artifactNames) {
            Artifact artifact = new Artifact();
            artifact.setType("docker_image");
            artifact.setName(artifactName);

            ComponentType dummyComponentType = new ComponentType();
            dummyComponentType.setName("ComponentType-"+artifactName);
            tadm.addComponentTypes(new ArrayList<>(List.of(dummyComponentType)));

            Component dummyComponent = new Component();
            dummyComponent.setName("Component-"+artifactName);
            dummyComponent.setType(dummyComponentType);
            dummyComponent.setArtifacts(new ArrayList<>(List.of(artifact)));
            tadm.addComponents(new ArrayList<>(List.of(dummyComponent)));
        }
    }

}
