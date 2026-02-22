package tn.temporise.application.service;

import com.google.analytics.data.v1beta.*;
import com.google.auth.oauth2.GoogleCredentials;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import tn.temporise.config.ExceptionFactory;

import java.io.IOException;
import java.io.InputStream;

@Service
@Slf4j
@RequiredArgsConstructor
public class GoogleAnalyticsService {

  private BetaAnalyticsDataClient analyticsDataClient;
  @Value("${google.analytics.propertyId}")
  private String propertyId;
  @Value("${google.analytics.pathToServiceAccount}")
  private String pathToServiceAccount;
  private final ResourceLoader resourceLoader;
  private final ExceptionFactory exceptionFactory;

  @PostConstruct
  public void init() throws IOException {
    Resource resource = resourceLoader.getResource("classpath:" + pathToServiceAccount);

    try (InputStream credentialsStream = resource.getInputStream()) {
      GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream);
      BetaAnalyticsDataSettings settings =
          BetaAnalyticsDataSettings.newBuilder().setCredentialsProvider(() -> credentials).build();

      analyticsDataClient = BetaAnalyticsDataClient.create(settings);
      log.info("✅ Google Analytics Data Client initialized successfully.");
    } catch (IOException e) {
      log.error("❌ Failed to initialize Google Analytics client: {}", e.getMessage(), e);
      throw e;
    }
  }

  public void fetchAnalytics() {
    try {
      RunReportRequest request =
          RunReportRequest.newBuilder().setProperty("properties/" + propertyId)
              .addDateRanges(DateRange.newBuilder().setStartDate("2025-07-01").setEndDate("today"))

              // Compatible Dimensions (tested in GA4 Explorer)
              .addDimensions(Dimension.newBuilder().setName("country"))
              .addDimensions(Dimension.newBuilder().setName("deviceCategory"))
              .addDimensions(Dimension.newBuilder().setName("newVsReturning"))
              .addDimensions(Dimension.newBuilder().setName("firstUserCampaignName")) // Replaces
                                                                                      // firstUserSource
                                                                                      // (more
                                                                                      // compatible)

              // Compatible Metrics (aligned with dimensions)
              .addMetrics(Metric.newBuilder().setName("activeUsers"))
              .addMetrics(Metric.newBuilder().setName("sessions"))
              .addMetrics(Metric.newBuilder().setName("screenPageViewsPerSession"))
              .addMetrics(Metric.newBuilder().setName("averagePurchaseRevenue")) // Replaces
                                                                                 // purchaseRevenue
                                                                                 // (more reliable)
              .addMetrics(Metric.newBuilder().setName("transactions"))
              .addMetrics(Metric.newBuilder().setName("cartToViewRate"))

              .build();

      // Execute and print
      RunReportResponse response = analyticsDataClient.runReport(request);
      for (Row row : response.getRowsList()) {
        System.out.printf(
            "Country: %s | Device: %s | User Type: %s | Campaign: %s\n"
                + "Active Users: %s | Sessions: %s | Pages/Session: %s\n"
                + "Avg. Revenue: %s | Transactions: %s | Cart-to-View: %s\n\n",
            row.getDimensionValues(0).getValue(), // country
            row.getDimensionValues(1).getValue(), // deviceCategory
            row.getDimensionValues(2).getValue(), // newVsReturning
            row.getDimensionValues(3).getValue(), // firstUserCampaignName
            row.getMetricValues(0).getValue(), // activeUsers
            row.getMetricValues(1).getValue(), // sessions
            row.getMetricValues(2).getValue(), // screenPageViewsPerSession
            row.getMetricValues(3).getValue(), // averagePurchaseRevenue
            row.getMetricValues(4).getValue(), // transactions
            row.getMetricValues(5).getValue() // cartToViewRate
        );
      }
    } catch (Exception e) {
      log.error("❌ Failed to fetch Google Analytics data: {}", e.getMessage(), e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }
}
