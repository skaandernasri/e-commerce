package tn.temporise.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.domain.model.*;
import tn.temporise.infrastructure.client.recommandation.RecommandationClient;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationService {
  private final ExceptionFactory exceptionFactory;
  private final RecommandationClient recommandationClient;

  public ModelConfigResponse getModelConfig() {
    try {
      return recommandationClient.getModelConfig();
    } catch (Exception e) {
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  public ModelMetricsResponse getModelMetrics() {
    try {
      return recommandationClient.getModelMetrics();
    } catch (Exception e) {
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  public TrainModelResponse trainModel(String loss, Double learningRate, Integer epochs,
      Integer no_components) {
    try {
      return recommandationClient.trainModel(loss, learningRate, epochs, no_components);
    } catch (Exception e) {
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  public TuneModelResponse tuneModel(Integer nTrials, Integer epochs) {
    try {
      return recommandationClient.tuneModel(nTrials, epochs);
    } catch (Exception e) {
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  public RecommendationResponse getRecommendations(Long userId, UUID anonymousUserSessionToken,
      Integer numRecs) {
    try {
      return recommandationClient.getRecommendations(userId, anonymousUserSessionToken, numRecs);
    } catch (Exception e) {
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }
}
