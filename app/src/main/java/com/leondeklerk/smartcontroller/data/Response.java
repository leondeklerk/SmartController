package com.leondeklerk.smartcontroller.data;

public class Response {
  private Exception exception;
  private String response;

  public Response(Exception exception) {
    this.exception = exception;
  }

  public Response(String result) {
    this.response = result;
  }

  public Exception getException() {
    return exception;
  }

  public String getResponse() {
    return response;
  }
}
