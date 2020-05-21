package com.leondeklerk.smartcontroller;

import com.leondeklerk.smartcontroller.data.Response;

public interface NetworkCallback {

  void onPreExecute(ResponseTask task);

  void onFinish(ResponseTask task, Response response, int deviceNum);

  void onCancel(ResponseTask task);
}
