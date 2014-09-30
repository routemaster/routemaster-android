package org.lumeh.routemaster.server;

import android.os.AsyncTask;
import android.util.Log;
import com.google.common.base.Preconditions;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Queue;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.StringEntity;
import org.apache.http.HttpResponse;
import org.apache.http.impl.client.DefaultHttpClient;
import org.lumeh.routemaster.models.Uploadable;

public class Uploader {
    private static final String TAG = "RouteMaster";
    private static final String SERVER = "http://routemaster.lumeh.org";

    private Queue<Uploadable> queue = new ArrayDeque<>();

    public void add(Uploadable u) {
        queue.add(u);
    }

    public void uploadAll() {
        Log.d(TAG, "Uploading EVERYTHING");
        while (!queue.isEmpty()) {
            new UploadJsonTask().execute(queue.poll());
        }
    }

    /**
     * Upload a single Uploadable to the server using a POST request.
     */
    private class UploadJsonTask extends AsyncTask<Uploadable, Void, Boolean> {
        private Uploadable uploadable;

        @Override
        protected Boolean doInBackground(Uploadable... args) {
            Preconditions.checkArgument(args.length == 1);
            uploadable = args[0];

            HttpClient httpclient = new DefaultHttpClient();
            Log.d(TAG, "Attempting to upload " + uploadable);
            URI uri;
            try {
                uri = new URI(SERVER + uploadable.getUploadPath());
            } catch (URISyntaxException e) {
                throw new RuntimeException("This should never happen", e);
            }

            StringEntity data;
            try {
                data = new StringEntity(uploadable.toJson().toString(),
                                        "utf-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("This should never happen", e);
            }
            data.setContentType("application/json");

            HttpPost request = new HttpPost(uri);
            request.setEntity(data);

            HttpResponse response;
            try {
                response = httpclient.execute(request);
            } catch (IOException e) {
                Log.w(TAG, "Error contacting server: " + e);
                return false;
            }
            boolean success = String.valueOf(
                response.getStatusLine().getStatusCode()).startsWith("2");
            if (success) {
                Log.d(TAG, "Successfully uploaded " + uploadable);
                queue.poll();  // Remove the item from the queue
            } else {
                Log.w(TAG, "Error uploading " + uploadable + ": " +
                      response.getStatusLine());
            }
            return success;
        }

        /**
         * Add the object back on the queue if the upload failed. Note that this
         * gets run on the UI thread, so the queue doesn't need to be
         * thread-safe.
         */
        @Override
        protected void onPostExecute(Boolean success) {
            if (!success) {
                queue.add(uploadable);
            }
        }
    }
}
