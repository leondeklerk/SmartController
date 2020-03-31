package com.leondeklerk.smartcontroller;

import com.leondeklerk.smartcontroller.data.Response;

public interface NetworkCallback {

  void onPreExecute(NetworkTask task);

  void onFinish(NetworkTask task, Response response, int deviceNum);

  void onCancel(NetworkTask task);
}
