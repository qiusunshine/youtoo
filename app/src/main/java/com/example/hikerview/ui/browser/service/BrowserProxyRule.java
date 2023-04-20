package com.example.hikerview.ui.browser.service;

import java.util.List;
import java.util.Map;

/**
 * 作者：By 15968
 * 日期：On 2023/4/19
 * 时间：At 10:14
 */

public class BrowserProxyRule {

  private String name;
  private String match;
  private String replace;
  private boolean force;

  private Map<String, String> requestHeaders;
  private List<BrowserProxyHeader> responseHeaders;

  public String getMatch() {
    return match;
  }

  public void setMatch(String match) {
    this.match = match;
  }

  public String getReplace() {
    return replace;
  }

  public void setReplace(String replace) {
    this.replace = replace;
  }

  public List<BrowserProxyHeader> getResponseHeaders() {
    return responseHeaders;
  }

  public void setResponseHeaders(List<BrowserProxyHeader> responseHeaders) {
    this.responseHeaders = responseHeaders;
  }

  public Map<String, String> getRequestHeaders() {
    return requestHeaders;
  }

  public void setRequestHeaders(Map<String, String> requestHeaders) {
    this.requestHeaders = requestHeaders;
  }

  public boolean isForce() {
    return force;
  }

  public void setForce(boolean force) {
    this.force = force;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}