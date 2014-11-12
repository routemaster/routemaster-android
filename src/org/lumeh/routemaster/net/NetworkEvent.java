package org.lumeh.routemaster.net;

import org.lumeh.routemaster.models.Journey;

/**
 * Base classes for network events. This allows us to subscribe to all network
 * events of a certain type. Eg. all HTTP POST requests.
 *
 * <p>TODO: See if {@code retrofit.RequestInterceptor} would do a better job of
 * this.
 *
 * <p>TODO: Otto doesn't support subscribing to interfaces, we should submit a
 * patch upstream: http://git.io/JNCqvg
 *
 * <p>TODO: Investigate the possibility of adding a "process" method to events.
 */
public class NetworkEvent<T> {
    private final T value;

    public NetworkEvent(T value) {
        this.value = value;
    }

    public T get() {
        return value;
    }

    public static class Request<T> extends NetworkEvent<T> {
        public Request(T value) {
            super(value);
        }
    }

    public static class Post<T> extends Request<T> {
        public Post(T value) {
            super(value);
        }
    }

    public static class Get<T> extends Request<T> {
        public Get(T value) {
            super(value);
        }
    }

    /**
     * These type arguments don't really matter, because otto doesn't understand
     * generic types properly anyways.
     */
    public static class Response<T, K extends Request<?>>
                        extends NetworkEvent<T> {
        private final K request;

        public Response(T value, K request) {
            super(value);
            this.request = request;
        }

        public K getRequest() {
            return request;
        }
    }
}
