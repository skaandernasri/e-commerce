package tn.temporise.application.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tn.temporise.application.exception.client.NotFoundException;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.domain.model.ClusterComparisonResponse;
import tn.temporise.domain.model.ClusterResponse;
import tn.temporise.domain.model.ClusterResponseUserClusterMapInner;
import tn.temporise.domain.model.UtilisateurModel;
import tn.temporise.domain.port.UserRepo;
import tn.temporise.infrastructure.client.clusteringModel.LoyaltyClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoyaltyService {
  private final ExceptionFactory exceptionFactory;
  private final LoyaltyClient loyaltyClient;
  private final UserRepo userRepo;

  @Transactional
  public ClusterResponse getClustersUsingKmeans(int n_clusters) {
    try {
      ClusterResponse clusterResponse = loyaltyClient.getClustersWithKmeans(n_clusters);
      for (ClusterResponseUserClusterMapInner cluster : clusterResponse.getUserClusterMap()) {
        UtilisateurModel user = userRepo.findById((long) cluster.getUserId());
        user = user.toBuilder().loyalty_group(cluster.getCluster()).build();
        userRepo.save(user);
      }
      return clusterResponse;
    } catch (NotFoundException e) {
      log.error("Error while getting clusters", e);
      throw e;
    } catch (Exception e) {
      log.error("Error while getting clusters", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  @Transactional
  public ClusterResponse getClustersUsingGMM(int n_clusters) {
    try {
      ClusterResponse clusterResponse = loyaltyClient.getClustersWithGMM(n_clusters);
      for (ClusterResponseUserClusterMapInner cluster : clusterResponse.getUserClusterMap()) {
        UtilisateurModel user = userRepo.findById((long) cluster.getUserId());
        user = user.toBuilder().loyalty_group(cluster.getCluster()).build();
        userRepo.save(user);
      }
      return clusterResponse;
    } catch (NotFoundException e) {
      log.error("Error while getting clusters", e);
      throw e;
    } catch (Exception e) {
      log.error("Error while getting clusters", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }


  public ClusterComparisonResponse compareClusters(int n_clusters) {
    try {
      return loyaltyClient.getCompare(n_clusters);
    } catch (Exception e) {
      log.error("Error while comparing clusters", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  @Scheduled(cron = "0 0 0 * * *") // Runs every day at 00:00 (midnight)
  @Transactional
  public void updateUserClustersDaily() {
    log.info("Running daily user clustering job...");
    getClustersUsingGMM(3); // Or your dynamic value for clusters
  }

}
