package demo.gos.common;

import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.micrometer.Label;
import io.vertx.micrometer.MicrometerMetricsOptions;
import io.vertx.micrometer.VertxPrometheusOptions;

import java.util.EnumSet;

public final class Commons {

  private static final int METRICS_ENABLED = Commons.getIntEnv("METRICS_ENABLED", 0);
  public static final int UI_PORT = getIntEnv("GOS_UI_PORT", 8080);
  public static final String UI_HOST = getStringEnv("GOS_UI_HOST", "localhost");
  public static final int BATTLEFIELD_PORT = getIntEnv("GOS_BATTLEFIELD_PORT", 8081);
  public static final String BATTLEFIELD_HOST = getStringEnv("GOS_BATTLEFIELD_HOST", "localhost");

  private Commons() {
  }

  public static String getStringEnv(String varname, String def) {
    String val = System.getenv(varname);
    if (val == null || val.isEmpty()) {
      return def;
    } else {
      System.out.println(varname + " = " + html(val));
      return html(val);
    }
  }

  public static int getIntEnv(String varname, int def) {
    String val = System.getenv(varname);
    if (val == null || val.isEmpty()) {
      return def;
    } else {
      return Integer.parseInt(val);
    }
  }

  public static double getDoubleEnv(String varname, double def) {
    String val = System.getenv(varname);
    if (val == null || val.isEmpty()) {
      return def;
    } else {
      return Double.parseDouble(val);
    }
  }

  public static String html(String str) {
    StringBuilder out = new StringBuilder();
    for (char c : str.toCharArray()) {
      if (!Character.isLetterOrDigit(c)) {
        out.append(String.format("&#x%x;", (int) c));
      } else {
        out.append(c);
      }
    }
    return out.toString();
  }

  public static VertxOptions vertxOptions() {
    if (METRICS_ENABLED == 1) {
      return new VertxOptions().setMetricsOptions(new MicrometerMetricsOptions()
          .setPrometheusOptions(new VertxPrometheusOptions()
              .setStartEmbeddedServer(true)
              .setEmbeddedServerOptions(new HttpServerOptions().setPort(9090))
              .setPublishQuantiles(true)
              .setEnabled(true))
          .setLabels(EnumSet.of(Label.POOL_TYPE, Label.POOL_NAME, Label.CLASS_NAME, Label.HTTP_CODE, Label.HTTP_METHOD, Label.HTTP_PATH, Label.EB_ADDRESS, Label.EB_FAILURE, Label.EB_SIDE))
          .setEnabled(true));
    }
    return new VertxOptions();
  }
}
