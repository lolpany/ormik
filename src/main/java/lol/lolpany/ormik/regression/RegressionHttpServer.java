package lol.lolpany.ormik.regression;

import com.github.dreamhead.moco.*;
import com.github.dreamhead.moco.model.DefaultHttpRequest;

import java.util.ArrayList;
import java.util.List;

import static com.github.dreamhead.moco.Moco.httpServer;
import static com.github.dreamhead.moco.Runner.runner;

public class RegressionHttpServer implements AutoCloseable {
    private RegressionMocoMonitor regressionMocoMonitor;
    private Runner run;

    public RegressionHttpServer(int port) {
        regressionMocoMonitor = new RegressionMocoMonitor();
        HttpServer server = httpServer(port, regressionMocoMonitor);
        server.response("<chrono>\n" +
                "    <code>000</code>\n" +
                "    <message></message>\n" +
                "</chrono>");
        run = runner(server);
        run.start();
    }

    public List<String> getRequests() {
        return regressionMocoMonitor.getRequests();
    }

    @Override
    public void close() throws Exception {
        run.stop();
    }

    private class RegressionMocoMonitor implements MocoMonitor {
        private List<String> requests = new ArrayList<>();

        @Override
        public void onMessageArrived(Request request) {
            DefaultHttpRequest httpRequest = (DefaultHttpRequest) request;
            String req = httpRequest.toFullHttpRequest().getUri() + "\n"
                    + httpRequest.getMethod() + "\n"
                    + httpRequest.getHeaders() + "\n"
                    + httpRequest.getContent() + "\n";
            requests.add(req);
            System.out.println(req);
        }

        @Override
        public void onException(Throwable t) {

        }

        @Override
        public void onMessageLeave(Response response) {

        }

        @Override
        public void onUnexpectedMessage(Request request) {

        }

        public List<String> getRequests() {
            List<String> result = new ArrayList<>(requests);
            requests.clear();
            return result;
        }
    }
}
