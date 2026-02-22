package tn.temporise.infrastructure.client.recommandation;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import tn.temporise.domain.model.*;

import java.util.UUID;

@FeignClient(name = "recommendationClient", url = "${models.url}")
public interface RecommandationClient {

  // Train model (default or custom config)
  @GetMapping("/model/train")
  TrainModelResponse trainModel(@RequestParam(value = "loss", required = false) String loss,
      @RequestParam(value = "learning_rate", required = false) Double learningRate,
      @RequestParam(value = "epochs", required = false) Integer epochs,
      @RequestParam(value = "no_components", required = false) Integer no_components);

  // Hyperparameter tuning
  @GetMapping("/model/tune")
  TuneModelResponse tuneModel(
      @RequestParam(value = "n_trials", defaultValue = "20") Integer nTrials,
      @RequestParam(value = "epochs", required = false) Integer epochs);

  // Get recommendations for user
  @GetMapping("/recommendations")
  RecommendationResponse getRecommendations(
      @RequestParam(value = "user_id", required = false) Long userId,
      @RequestParam(value = "anonymous_user_session_id",
          required = false) UUID anonymousUserSessionToken,
      @RequestParam(value = "num_recs", defaultValue = "5") Integer numRecs);

  // Get model metrics
  @GetMapping("/model/metrics")
  ModelMetricsResponse getModelMetrics();

  // Get current config
  @GetMapping("/model/config")
  ModelConfigResponse getModelConfig();
}
