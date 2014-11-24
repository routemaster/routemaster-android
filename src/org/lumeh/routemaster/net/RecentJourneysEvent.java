package org.lumeh.routemaster.net;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.lumeh.routemaster.models.Journey;

public class RecentJourneysEvent {
    public static final class Get extends NetworkEvent.Get<String> {
        public Get(String accountId) {
            super(accountId);
        }
    }

    public static final class Response<K extends NetworkEvent.Request<?>>
                      extends NetworkEvent.Response<ImmutableList<Journey>, K> {
        public Response(List<Journey> journeys, K request) {
            super(ImmutableList.copyOf(journeys), request);
        }
    }
}
