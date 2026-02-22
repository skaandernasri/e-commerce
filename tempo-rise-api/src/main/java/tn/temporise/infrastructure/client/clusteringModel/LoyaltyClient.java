package tn.temporise.infrastructure.client.clusteringModel;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import tn.temporise.domain.model.*;


@FeignClient(name = "loyaltyClient", url = "${models.url}")
public interface LoyaltyClient {

  // Get model cluster
  @GetMapping("/model/cluster/kmeans")
  ClusterResponse getClustersWithKmeans(
      @RequestParam(value = "n_clusters", required = false) int n_clusters);

  @GetMapping("/model/cluster/gmm")
  ClusterResponse getClustersWithGMM(
      @RequestParam(value = "n_clusters", required = false) int n_clusters);

  // Get Compare Kmean with GMM
  @GetMapping("/model/cluster/compare")
  ClusterComparisonResponse getCompare(
      @RequestParam(value = "n_clusters", required = false) int n_clusters);
}

