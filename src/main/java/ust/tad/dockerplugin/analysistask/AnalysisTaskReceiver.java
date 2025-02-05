package ust.tad.dockerplugin.analysistask;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ust.tad.dockerplugin.analysis.AnalysisService;

@Service
public class AnalysisTaskReceiver {
  @Autowired private MessageConverter jsonMessageConverter;

  @Autowired private AnalysisTaskResponseSender analysisTaskResponseSender;

  @Autowired private AnalysisService analysisService;

  /**
   * Receives a message from the analysis task request queue. Based on the type of the message given
   * by the formatIndicator header, it calls a respective function.
   *
   * @param message
   * @throws JsonProcessingException
   */
  public void receive(Message message) {
    if (message.getMessageProperties().getHeader("formatIndicator") != null) {
      switch (message.getMessageProperties().getHeader("formatIndicator").toString()) {
        case "AnalysisTaskStartRequest":
          receiveAnalysisTaskStartRequest(message);
          break;
        default:
          respondWithErrorMessage("Could not process message: Unknown format of request message.");
          break;
      }
    } else {
      respondWithErrorMessage("Could not process message: Header with formatIndicator missing.");
    }
  }

  /**
   * Receives a message of type AnalysisTaskStartRequest. Transforms the message into an entity of
   * type AnalysisTaskStartRequest. Starts the analysis process of the plugin.
   *
   * @param message
   */
  private void receiveAnalysisTaskStartRequest(Message message) {
    ObjectMapper mapper = new ObjectMapper();
    AnalysisTaskStartRequest analysisTaskStartRequest =
        mapper.convertValue(
            jsonMessageConverter.fromMessage(message), AnalysisTaskStartRequest.class);
    analysisService.startAnalysis(
        analysisTaskStartRequest.getTaskId(),
        analysisTaskStartRequest.getTransformationProcessId(),
        analysisTaskStartRequest.getTadmEntities());
  }

  /** Creates and sends an AnalysisTaskResponse containing an error message. */
  private void respondWithErrorMessage(String errorMessage) {
    analysisTaskResponseSender.sendFailureResponse(null, errorMessage);
  }
}
