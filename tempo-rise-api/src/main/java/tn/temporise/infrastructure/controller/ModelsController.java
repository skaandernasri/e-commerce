package tn.temporise.infrastructure.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import tn.temporise.application.service.LoyaltyService;
import tn.temporise.application.service.RecommendationService;
import tn.temporise.domain.model.*;
import tn.temporise.infrastructure.api.ModelApi;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ModelsController implements ModelApi {
  private final RecommendationService recommendationService;
  private final LoyaltyService loyaltyService;

  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  @Override
  public ResponseEntity<ClusterResponse> _clusterGMM(Integer nClusters) throws Exception {
    return ResponseEntity.ok(loyaltyService.getClustersUsingGMM(nClusters));
  }

  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  @Override
  public ResponseEntity<ClusterResponse> _clusterKmeans(Integer nClusters) throws Exception {
    return ResponseEntity.ok(loyaltyService.getClustersUsingKmeans(nClusters));
  }

  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  @Override
  public ResponseEntity<ClusterComparisonResponse> _compareCluster(Integer nClusters)
      throws Exception {
    return ResponseEntity.ok(loyaltyService.compareClusters(nClusters));
  }


  @Override
  public ResponseEntity<ModelConfigResponse> _config() throws Exception {
    return ResponseEntity.ok(recommendationService.getModelConfig());
  }

  @Override
  public ResponseEntity<ModelMetricsResponse> _metrics() throws Exception {
    return ResponseEntity.ok(recommendationService.getModelMetrics());
  }


  @Override
  public ResponseEntity<RecommendationResponse> _recommendation(Long userId,
      UUID anonymousUserSessionToken, Integer numRecs) throws Exception {
    return ResponseEntity
        .ok(recommendationService.getRecommendations(userId, anonymousUserSessionToken, numRecs));
  }

  @Override
  public ResponseEntity<TrainModelResponse> _trainModel(String loss, Double learningRate,
      Integer epochs, Integer noComponents) throws Exception {
    return ResponseEntity
        .ok(recommendationService.trainModel(loss, learningRate, epochs, noComponents));
  }

  @Override
  public ResponseEntity<TuneModelResponse> _tuneModel(Integer nTrials, Integer epochs)
      throws Exception {
    return ResponseEntity.ok(recommendationService.tuneModel(nTrials, epochs));
  }
}
