import com.sun.net.httpserver.HttpServer;
import java.awt.AWTException;
import java.awt.Robot;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

public class WebKeyboard {

  public static void main(String[] args) throws AWTException, IOException {

    Robot robot = new Robot();
    robot.mouseMove(0, 0);

    HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
    server.setExecutor(Executors.newCachedThreadPool());
    server.start();

    server.createContext("/", exchange -> {

      if (!Objects.equals(exchange.getRequestMethod(), "GET")) {
        exchange.sendResponseHeaders(404, -1);
        exchange.getResponseBody().close();
        return;
      }

      byte[] body = Files.readAllBytes(Paths.get("WebKeyboard.html"));

      exchange.sendResponseHeaders(200, body.length);
      exchange.getResponseBody().write(body);
      exchange.getResponseBody().close();

    });

    server.createContext("/api", exchange -> {

      if (!Objects.equals(exchange.getRequestMethod(), "PUT")) {
        exchange.sendResponseHeaders(404, -1);
        exchange.getResponseBody().close();
        return;
      }

      BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));

      int[] keycodes = reader.lines().filter(line -> line.matches("^(?:[0-9]|[1-8][0-9]|9[0-9]|[1-8][0-9]{2}|9[0-8][0-9]|99[0-9]|[1-8][0-9]{3}|9[0-8][0-9]{2}|99[0-8][0-9]|999[0-9]|[1-5][0-9]{4}|6[0-4][0-9]{3}|65[0-4][0-9]{2}|655[0-2][0-9]|6553[0-5])$"))
          .mapToInt(Integer::parseInt)
          .toArray();

      for (int keycode : keycodes) {
        robot.keyPress(keycode);
      }

      for (int keycode : IntStream.rangeClosed(1, keycodes.length).map(i -> keycodes[keycodes.length - i]).toArray()) {
        robot.keyRelease(keycode);
      }

      exchange.sendResponseHeaders(200, -1);
      exchange.getResponseBody().close();

    });

  }

}
