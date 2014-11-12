package org.lumeh.routemaster.net;

import android.location.Location;
import com.google.common.base.Optional;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.Date;
import org.lumeh.routemaster.util.Dates;

/**
 * A mixed-bag of gson {@link TypeAdapter} classes for types we don't control.
 */
public class JsonAdapters {
    /**
     * Converts an optional to null (if absent) or the serialized subtype (if
     * present).
     */
    public static class OptionalAdapter<T>
                  implements JsonSerializer<Optional<T>>,
                             JsonDeserializer<Optional<T>> {
        @Override
        public JsonElement serialize(Optional<T> optional, Type type,
                                     JsonSerializationContext context) {
            return context.serialize(optional.orNull());
        }

        @Override
        public Optional<T> deserialize(JsonElement json, Type type,
                                       JsonDeserializationContext context) {
            return Optional.<T>fromNullable((T)
                context.deserialize(
                    json,
                    ((ParameterizedType) type).getActualTypeArguments()[0]
                )
            );
        }
    }

    public static class LocationAdapter extends TypeAdapter<Location> {
        @Override
        public void write(JsonWriter writer, Location loc)
                    throws IOException {
            writer.beginObject();
            writer.name("timeUtc").value(Dates.toIsoString(loc.getTime()));
            writer.name("accuracyM").value(loc.getAccuracy());
            writer.name("latitude").value(loc.getLatitude());
            writer.name("longitude").value(loc.getLongitude());
            writer.name("heightM").value(loc.getAltitude());
            writer.endObject();
        }

        @Override
        public Location read(JsonReader reader) throws IOException {
            Location loc = new Location("gson");
            reader.beginObject();
            while(reader.hasNext()) {
                String name = reader.nextName();
                if(name.equals("timeUtc")) {
                    try {
                        long t = Dates.toMillisSinceEpoch(reader.nextString());
                        loc.setTime(t);
                    } catch(ParseException ex) {
                        throw new IOException(ex);
                    }
                } else if(name.equals("accuracyM")) {
                    loc.setAccuracy((float) reader.nextDouble());
                } else if(name.equals("latitude")) {
                    loc.setLatitude(reader.nextDouble());
                } else if(name.equals("longitude")) {
                    loc.setLongitude(reader.nextDouble());
                } else if(name.equals("heightM")) {
                    loc.setAltitude(reader.nextDouble());
                } else {
                    reader.skipValue();
                }
            }
            reader.endObject();
            return loc;
        }
    }

    public static class DateAdapter extends TypeAdapter<Date> {
        @Override
        public void write(JsonWriter writer, Date d)
                    throws IOException {
            writer.value(Dates.toIsoString(d));
        }

        @Override
        public Date read(JsonReader reader) throws IOException {
            try {
                return Dates.fromIsoString(reader.nextString());
            } catch(ParseException ex) {
                throw new IOException(ex);
            }
        }
    }
}
