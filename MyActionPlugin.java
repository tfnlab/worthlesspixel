import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.support.ActionFilter;
import org.elasticsearch.action.support.ActionFilterChain;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.TransportAction;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.tasks.Task;
import org.elasticsearch.transport.TransportService;

public class MyActionPlugin extends TransportAction<MyActionRequest, MyActionResponse> {

    private final String apiEndpoint;

    @Inject
    public MyActionPlugin(Settings settings, TransportService transportService, ActionFilters actionFilters,
                          String apiEndpoint) {
        super(MyActionRequest.NAME, transportService, actionFilters, MyActionRequest::new);
        this.apiEndpoint = apiEndpoint;
    }

    @Override
    protected void doExecute(Task task, MyActionRequest request, ActionListener<MyActionResponse> listener) {
        try {
            // Build the JSON payload for the API request
            XContentBuilder builder = XContentFactory.jsonBuilder();
            builder.startObject();
            builder.field("key1", request.getKey1());
            builder.field("key2", request.getKey2());
            builder.endObject();
            String payload = builder.string();

            // Create a new URL object for the API endpoint
            URL url = new URL(apiEndpoint);

            // Open a new HTTP connection to the API endpoint
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // Write the JSON payload to the output stream
            OutputStream os = connection.getOutputStream();
            os.write(payload.getBytes());
            os.flush();
            os.close();

            // Check the response code and return the result
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                listener.onResponse(new MyActionResponse(RestStatus.OK));
            } else {
                listener.onFailure(new RuntimeException("API request failed with response code " + responseCode));
            }
        } catch (IOException e) {
            listener.onFailure(e);
        }
    }
}
