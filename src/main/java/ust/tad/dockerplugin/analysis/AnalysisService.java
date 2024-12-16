package ust.tad.dockerplugin.analysis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ust.tad.dockerplugin.analysis.util.MissingBaseTypeException;
import ust.tad.dockerplugin.analysis.util.MissingComponentsException;
import ust.tad.dockerplugin.analysis.util.MissingDockerImageException;
import ust.tad.dockerplugin.analysistask.AnalysisTaskResponseSender;
import ust.tad.dockerplugin.analysistask.TADMEntities;
import ust.tad.dockerplugin.models.ModelsService;
import ust.tad.dockerplugin.models.tadm.Component;
import ust.tad.dockerplugin.models.tadm.TechnologyAgnosticDeploymentModel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AnalysisService {
  @Autowired
  private ModelsService modelsService;

  @Autowired
  private AnalysisTaskResponseSender analysisTaskResponseSender;

  @Autowired
  private DockerImageAnalysisService dockerImageAnalysisService;

  private TechnologyAgnosticDeploymentModel tadm;

  /**
   * Start the analysis of the deployment model.
   * 1. Retrieve internal deployment models from models service
   * 2. Run the analysis
   * 3. Send updated model to models service
   * 4. Send AnalysisTaskResponse
   *
   * @param taskId the id of the analysis task.
   * @param transformationProcessId the id of the transformation process.
   * @param tadmEntities the entities of the tadm to analyze.
   */
  public void startAnalysis(
          UUID taskId,
          UUID transformationProcessId,
          List<TADMEntities> tadmEntities) {
    this.tadm = modelsService.getTechnologyAgnosticDeploymentModel(transformationProcessId);
    try {
      runAnalysis(tadmEntities);
    } catch (MissingComponentsException e) {
      e.printStackTrace();
      analysisTaskResponseSender.sendFailureResponse(taskId, e.getClass() + ": " + e.getMessage());
      return;
    }
    modelsService.updateTechnologyAgnosticDeploymentModel(tadm);
    analysisTaskResponseSender.sendSuccessResponse(taskId);
  }

  /**
   * Run the analysis by retrieving the components from the tadm that are listed in the given tadm
   * entities.
   *
   * @param tadmEntities the tadm entities with the list of components to analyze.
   * @throws MissingComponentsException if tadmEntities does not contain components to analyze.
   */
  private void runAnalysis(List<TADMEntities> tadmEntities) throws MissingComponentsException {
    if (tadmEntities == null) {
      throw new MissingComponentsException("No components to analyze in request.");
    }
    Optional<TADMEntities> componentsToAnalyze =
            tadmEntities.stream().filter(tadmEntities1 -> tadmEntities1.getTadmEntitiesType().equals(
                    "Component")).findFirst();
    if (componentsToAnalyze.isPresent()) {
      List<String> componentIds = componentsToAnalyze.get().getTadmEntityIds();
      for (String componentId : componentIds) {
        Optional<Component> optionalComponentToAnalyze =
                tadm.getComponents().stream().filter(component -> component.getId().equals(componentId)).findFirst();
        optionalComponentToAnalyze.ifPresent(component -> {
          try {
            this.tadm =
                    dockerImageAnalysisService.analyzeDockerImageOfComponent(component, tadm);
          } catch (MissingDockerImageException | MissingBaseTypeException ignored) {
          }
        });
      }
    } else {
      throw new MissingComponentsException("No components to analyze in request.");
    }
  }
}
