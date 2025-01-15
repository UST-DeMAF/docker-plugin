package ust.tad.dockerplugin.analysis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ust.tad.dockerplugin.analysis.util.ComponentTypeProvider;
import ust.tad.dockerplugin.analysis.util.MissingBaseTypeException;
import ust.tad.dockerplugin.analysis.util.MissingDockerImageException;
import ust.tad.dockerplugin.models.tadm.Artifact;
import ust.tad.dockerplugin.models.tadm.Component;
import ust.tad.dockerplugin.models.tadm.ComponentType;
import ust.tad.dockerplugin.models.tadm.TechnologyAgnosticDeploymentModel;

import java.util.List;
import java.util.Optional;

@Service
public class DockerImageAnalysisService {

    @Value("#{${image-identifiers.database}}")
    private List<String> databaseImageIdentifiers;

    @Value("#{${image-identifiers.message-broker}}")
    private List<String> messageBrokerImageIdentifiers;

    @Autowired
    private ComponentTypeProvider componentTypeProvider;

    /**
     * Analyze the Docker image of a Component. Based on the name of the Docker image, classify the
     * Docker image, transform the component accordingly, and add the changes to the given tadm.
     *
     * @param componentToAnalyze the component containing the Docker image to analyze.
     * @param tadm               the complete tadm.
     * @return the updated tadm.
     * @throws MissingDockerImageException if the Component does not contain a Docker image as an
     *                                     artifact.
     * @throws MissingBaseTypeException    if the tadm does not contain a component type 'BaseType'.
     */
    public TechnologyAgnosticDeploymentModel analyzeDockerImageOfComponent(
            Component componentToAnalyze, TechnologyAgnosticDeploymentModel tadm) throws
            MissingDockerImageException, MissingBaseTypeException {
        String imageIdentifier = getImageIdentifierFromComponent(componentToAnalyze);
        if (databaseImageIdentifiers.contains(imageIdentifier)) {
            transformComponentWithDatabaseImage(componentToAnalyze, tadm, imageIdentifier);
        } else if (messageBrokerImageIdentifiers.contains(imageIdentifier)) {
            transformComponentWithMessageBrokerImage(componentToAnalyze, tadm, imageIdentifier);
        } else {
            transformComponentWithoutImageClassification(componentToAnalyze, tadm, imageIdentifier);
        }
        return tadm;
    }

    /**
     * Transform a Component that contains a Docker image classified as a database system and
     * persist the changes in the given tadm.
     *
     * @param componentToAnalyze the component with the Docker image.
     * @param tadm               the tadm to update with the transformation changes.
     * @param imageIdentifier    the Docker image identifier.
     * @throws MissingBaseTypeException if the tadm does not contain a component type 'BaseType'.
     */
    private void transformComponentWithDatabaseImage(
            Component componentToAnalyze, TechnologyAgnosticDeploymentModel tadm,
            String imageIdentifier)
            throws MissingBaseTypeException {
        ComponentType databaseSystemType = getOrCreateDatabaseSystemType(tadm);
        setComponentSpecificType(componentToAnalyze, databaseSystemType, tadm, imageIdentifier);
    }

    /**
     * Get the Component Type for the 'Database System' from the given tadm or create it if it is
     * not present.
     *
     * @param tadm the tadm from which to get the Component Type.
     * @return the Component Type for the 'Database System'.
     * @throws MissingBaseTypeException if the tadm does not contain a component type 'BaseType'.
     */
    private ComponentType getOrCreateDatabaseSystemType(TechnologyAgnosticDeploymentModel tadm)
            throws MissingBaseTypeException {
        Optional<ComponentType> databaseSystemTypeOpt =
                tadm.getComponentTypes().stream().filter(
                        componentType -> componentType.getName().equals("DatabaseSystem")).findFirst();
        if (databaseSystemTypeOpt.isPresent()) {
            return databaseSystemTypeOpt.get();
        } else {
            ComponentType databaseSystemType =
                    componentTypeProvider.createDatabaseSystemType(getOrCreateSoftwareApplicationType(tadm));
            tadm.addComponentTypes(List.of(databaseSystemType));
            return databaseSystemType;
        }
    }

    /**
     * Transform a Component that contains a Docker image classified as a message broker and
     * persist the changes in the given tadm.
     *
     * @param componentToAnalyze the component with the Docker image.
     * @param tadm               the tadm to update with the transformation changes.
     * @param imageIdentifier    the Docker image identifier.
     * @throws MissingBaseTypeException if the tadm does not contain a component type 'BaseType'.
     */
    private void transformComponentWithMessageBrokerImage(
            Component componentToAnalyze, TechnologyAgnosticDeploymentModel tadm,
            String imageIdentifier)
            throws MissingBaseTypeException {
        ComponentType messageBrokerType = getOrCreateMessageBrokerType(tadm);
        setComponentSpecificType(componentToAnalyze, messageBrokerType, tadm, imageIdentifier);
    }

    /**
     * Get the Component Type for the 'Message Broker' from the given tadm or create it if it is
     * not present.
     *
     * @param tadm the tadm from which to get the Component Type.
     * @return the Component Type for the 'Message Broker'.
     * @throws MissingBaseTypeException if the tadm does not contain a component type 'BaseType'.
     */
    private ComponentType getOrCreateMessageBrokerType(TechnologyAgnosticDeploymentModel tadm)
            throws MissingBaseTypeException {
        Optional<ComponentType> messageBrokerTypeOpt =
                tadm.getComponentTypes().stream().filter(
                        componentType -> componentType.getName().equals("MessageBroker")).findFirst();
        if (messageBrokerTypeOpt.isPresent()) {
            return messageBrokerTypeOpt.get();
        } else {
            ComponentType messageBrokerType =
                    componentTypeProvider.createMessageBrokerType(getOrCreateSoftwareApplicationType(tadm));
            tadm.addComponentTypes(List.of(messageBrokerType));
            return messageBrokerType;
        }
    }

    /**
     * Transform a Component that contains a Docker image that could not be further classified and
     * persist the changes in the given tadm. In this case, the generic 'Software Application'
     * component type is assigned.
     *
     * @param componentToAnalyze the component with the Docker image.
     * @param tadm               the tadm to update with the transformation changes.
     * @param imageIdentifier    the Docker image identifier.
     * @throws MissingBaseTypeException if the tadm does not contain a component type 'BaseType'.
     */
    private void transformComponentWithoutImageClassification(
            Component componentToAnalyze, TechnologyAgnosticDeploymentModel tadm,
            String imageIdentifier)
            throws MissingBaseTypeException {
        ComponentType softwareApplicationType = getOrCreateSoftwareApplicationType(tadm);
        setComponentSpecificType(componentToAnalyze, softwareApplicationType,
                tadm, imageIdentifier);
    }

    /**
     * Get the Component Type for the 'Software Application' from the given tadm or create it if it
     * is not present.
     *
     * @param tadm the tadm from which to get the Component Type.
     * @return the Component Type for the 'Software Application'.
     * @throws MissingBaseTypeException if the tadm does not contain a component type 'BaseType'.
     */
    private ComponentType getOrCreateSoftwareApplicationType(TechnologyAgnosticDeploymentModel tadm)
            throws MissingBaseTypeException {
        Optional<ComponentType> softwareApplicationTypeOpt =
                tadm.getComponentTypes().stream().filter(
                        componentType -> componentType.getName().equals("SoftwareApplication")).findFirst();
        if (softwareApplicationTypeOpt.isPresent()) {
            return softwareApplicationTypeOpt.get();
        } else {
            Optional<ComponentType> baseType =
                    tadm.getComponentTypes().stream().filter(
                            componentType -> componentType.getName().equals("BaseType")).findFirst();
            if (baseType.isPresent()) {
                ComponentType softwareApplicationType =
                        componentTypeProvider.createSoftwareApplicationType(baseType.get());
                tadm.addComponentTypes(List.of(softwareApplicationType));
                return softwareApplicationType;
            } else {
                throw new MissingBaseTypeException("The given technology-agnostic deployment " +
                        "model does not contain the base type.");
            }
        }
    }

    /**
     * From a Docker image name, get the identifier. The identifier is the image name without the
     * tag and the registry, like so:
     * [registry]/[identifier]:[tag]
     *
     * @param component the component that contains the Docker image to analyze as an artifact.
     * @return the Docker image identifier.
     * @throws MissingDockerImageException if the Component does not contain a Docker image as an
     *                                     artifact or the contained artifact/image name is null.
     */
    private String getImageIdentifierFromComponent(Component component) throws MissingDockerImageException {
        Artifact artifact =
                component.getArtifacts().stream().filter(artifact1 -> artifact1.getType().equals(
                        "docker_image")).findFirst().orElseThrow(() -> new MissingDockerImageException(
                        "Component does not contain a Docker Image to analyze."));
        if (artifact.getName() != null) {
            String[] imageNameParts = artifact.getName().split(":")[0].split("/");
            return imageNameParts[imageNameParts.length - 1];
        } else {
            throw new MissingDockerImageException("Component does not contain a Docker Image with" +
                    " a valid image name to analyze.");
        }
    }

    /**
     * For an analyzed component, rename the component type to fit the naming scheme of
     * [image identifier]-[classified parent component type].
     * If a component type with this naming scheme already exists, then merge into one shared
     * component type.
     *
     * @param component            the analyzed component.
     * @param classifiedParentType the parent type classified for this component.
     * @param tadm                 the tadm to update with the transformation changes.
     * @param imageIdentifier      the Docker image identifier.
     */
    private void setComponentSpecificType(Component component,
                                          ComponentType classifiedParentType,
                                          TechnologyAgnosticDeploymentModel tadm,
                                          String imageIdentifier) {
        String componentTypeNewName = imageIdentifier + "-" + classifiedParentType.getName();
        ComponentType oldComponentType = tadm.getComponentTypeById(component.getType().getId());
        if (tadm.getComponentTypes().stream().noneMatch(componentType ->
                componentType.getName().equals(componentTypeNewName))) {
            oldComponentType.setName(componentTypeNewName);
            oldComponentType.setParentType(classifiedParentType);
            component.setType(oldComponentType);
        } else {
            ComponentType existingComponentType =
                    tadm.getComponentTypes().stream().filter(componentType ->
                            componentType.getName().equals(componentTypeNewName)).findFirst().orElseThrow();
            existingComponentType.addPropertiesIfNotPresent(oldComponentType);
            existingComponentType.addOperationsIfNotPresent(oldComponentType);
            component.setType(existingComponentType);
            tadm.removeComponentTypeIfUnused(oldComponentType);
        }
    }
}
