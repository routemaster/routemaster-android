package org.lumeh.routemaster.net;

import org.lumeh.routemaster.models.Journey;

public class JourneyEvent {
    public static final class Post extends NetworkEvent.Post<Journey> {
        public Post(Journey journey) {
            super(journey);
        }
    }

    public static final class Get extends NetworkEvent.Get<String> {
        public Get(String id) {
            super(id);
        }
    }

    public static final class Response<K extends NetworkEvent.Request<?>>
                            extends NetworkEvent.Response<Journey, K> {
        public Response(Journey journey, K request) {
            super(journey, request);
        }
    }
}
