package com.leondeklerk.smartcontroller;

import com.leondeklerk.smartcontroller.data.Response;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import lombok.Getter;

public class ResponseTask extends CommandTask {
  private NetworkCallback callback;
  @Getter private int deviceNum;

  ResponseTask(NetworkCallback callback, int device) {
    super();
    this.deviceNum = device;
    this.callback = callback;
  }

  @Override
  protected void onPreExecute() {
    callback.onPreExecute(this);
    // Switch processing animation (on)
  }

  /** Defines work to perform on the background thread. */
  @Override
  protected Response doInBackground(String... urls) {
    Response response = null;
    if (!isCancelled() && urls != null && urls.length > 0) {
      response = makeRequest(urls[0]);
    }
    return response;
  }

  /** Updates the DownloadCallback with the result. */
  @Override
  protected void onPostExecute(Response response) {
    // Switch processing animation (off)
    callback.onFinish(this, response, deviceNum);
  }

  @Override
  protected void onCancelled(Response response) {
    callback.onCancel(this);
  }

  private Response makeRequest(String url) {
    Response response;
    try {
      HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
      con.setRequestMethod("GET");
      con.setConnectTimeout(5000);
      int responseCode = con.getResponseCode();
      if (responseCode != HttpURLConnection.HTTP_OK) {
        throw new Exception(String.valueOf(responseCode));
      }

      BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
      String inputLine;
      StringBuilder stringBuilder = new StringBuilder();

      while ((inputLine = in.readLine()) != null) {
        stringBuilder.append(inputLine);
      }
      in.close();
      response = new Response(stringBuilder.toString());
      con.disconnect();
    } catch (Exception e) {
      response = new Response(e);
    }
    return response;
  }
}
