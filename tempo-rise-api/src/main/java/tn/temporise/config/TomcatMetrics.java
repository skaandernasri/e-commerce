package tn.temporise.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.catalina.connector.Connector;
import org.apache.coyote.ProtocolHandler;
import org.apache.coyote.AbstractProtocol;
import org.springframework.boot.web.embedded.tomcat.TomcatWebServer;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class TomcatMetrics implements Filter, ApplicationListener<WebServerInitializedEvent> {

  private final MeterRegistry registry;
  private volatile Connector connector;
  private final Counter tomcatErrorCounter;

  public TomcatMetrics(MeterRegistry registry) {
    this.registry = registry;
    this.tomcatErrorCounter = Counter.builder("tomcat_global_error_total")
        .description("Total Tomcat error count").register(registry);
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    HttpServletResponse httpResponse = (HttpServletResponse) response;
    chain.doFilter(request, response);
    int status = httpResponse.getStatus();
    if (status >= 400 && status < 600) {
      tomcatErrorCounter.increment();
    }
  }

  @Override
  public void onApplicationEvent(WebServerInitializedEvent event) {
    if (event.getWebServer() instanceof TomcatWebServer tomcatWebServer) {
      this.connector = tomcatWebServer.getTomcat().getConnector();

      // Register the gauge once connector is ready
      Gauge.builder("tomcat_threads_config_max", this, TomcatMetrics::getMaxThreads)
          .description("Maximum configured threads for Tomcat connector").register(registry);
    }
  }

  public double getMaxThreads() {
    if (connector == null) {
      return 0;
    }
    ProtocolHandler protocolHandler = connector.getProtocolHandler();
    if (protocolHandler instanceof AbstractProtocol<?>) {
      return ((AbstractProtocol<?>) protocolHandler).getMaxThreads();
    }
    return 0;
  }
}
