package de.otto.edison.jobtrigger.trigger;

import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.ListenableFuture;
import com.ning.http.client.Response;
import de.otto.edison.jobtrigger.definition.JobDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Guido Steinacker
 * @since 05.09.15
 */
class TriggerRunnables {

    private TriggerRunnables() {}

    public static Runnable httpTriggerRunnable(final AsyncHttpClient httpClient,
                                               final JobDefinition jobDefinition,
                                               final TriggerResponseConsumer consumer) {
        return () -> {
            final Logger LOG = LoggerFactory.getLogger("de.otto.edison.jobtrigger.HttpTriggerRunnable");
            final String triggerUrl = jobDefinition.getTriggerUrl();
            try {
                final ListenableFuture<Response> futureResponse = httpClient.preparePost(triggerUrl).execute(new AsyncCompletionHandler<Response>() {
                    @Override
                    public Response onCompleted(final Response response) throws Exception {
                        final String location = response.getHeader("Location");
                        final int status = response.getStatusCode();
                        consumer.consume(response);
                        return response;
                    }

                    @Override
                    public void onThrowable(Throwable t) {
                        consumer.consume(t);
                    }
                });

            } catch (final Exception e) {
                LOG.error("Exception caught when trying to trigger '{}': {}", triggerUrl, e.getMessage());
            }
        };
    }
}

