package com.ace.cron;

import com.ibm.broker.plugin.*;
import org.quartz.CronExpression;

import java.text.ParseException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.TimeZone;

public class CronTriggerInputNode extends MbInputNode implements MbInputNodeInterface {

  // Node properties
  private String cron = "";        // e.g. "0 0/1 * * * ?"
  private String timeZone = "";    // e.g. "Europe/Brussels"

  // Scheduling state
  private volatile CronExpression expr;
  private volatile ZoneId zone;
  private volatile long nextFireEpochMs = -1L;

  // Throttle failure propagation
  private volatile long nextErrorReportMs = 0L;
  private static final long ERROR_THROTTLE_MS = 5000L;

  public CronTriggerInputNode() throws MbException {
    createOutputTerminal("out");
    createOutputTerminal("failure");
    createOutputTerminal("catch");
  }

  public static String getNodeName() { return "CronTriggerInputNode"; }

  // JavaBean properties
  public String getCron() { return cron; }
  public void setCron(String v) { cron = (v == null ? "" : v); expr = null; nextFireEpochMs = -1L; }

  public String getTimeZone() { return timeZone; }
  public void setTimeZone(String v) { timeZone = (v == null ? "" : v); zone = null; }

  // Helpers
  private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

  private void setNV(MbElement parent, String name, Object value) throws MbException {
    MbElement e = parent.getFirstElementByPath(name);
    if (e == null) parent.createElementAsLastChild(MbElement.TYPE_NAME_VALUE, name, value);
    else           e.setValue(value);
  }

  private void setJsonPropertiesOnExisting(MbElement root) throws MbException {
    // Use the Properties folder that createMessage() already created
    MbElement props = root.getFirstElementByPath("Properties");
    if (props != null) {
      setNV(props, "MessageDomain",  "JSON");
      setNV(props, "MessageSet",     "");
      setNV(props, "MessageType",    "CronTriggerEvent");
      setNV(props, "MessageFormat",  "JSON");
      setNV(props, "Encoding",       Integer.valueOf(546));   // little-endian
      setNV(props, "CodedCharSetId", Integer.valueOf(1208));  // UTF-8
    }
  }

  private void makeJsonBody(MbElement root, String type, String timeOrErrorKey, String value) throws MbException {
    // Remove BLOB if the skeleton added it
    MbElement blob = root.getFirstElementByPath("BLOB");
    if (blob != null) blob.delete();

    // Create JSON tree
    MbElement jsonRoot = root.createElementAsLastChild(MbJSON.PARSER_NAME);
    MbElement data     = jsonRoot.createElementAsLastChild(MbElement.TYPE_NAME, "Data", null);
    setNV(data, "type", type);
    setNV(data, timeOrErrorKey, value);
  }

  private void propagateFailureOnce(MbMessageAssembly assembly, String text) throws MbException {
    long now = System.currentTimeMillis();
    if (now < nextErrorReportMs) return;

    MbMessage fail = createMessage(new byte[0]); // skeleton with Properties
    try {
      MbElement root = fail.getRootElement();
      setJsonPropertiesOnExisting(root);
      makeJsonBody(root, "cron trigger error", "error", text);

      fail.finalizeMessage(MbMessage.FINALIZE_VALIDATE);
      MbMessageAssembly asm = new MbMessageAssembly(assembly, fail);
      getOutputTerminal("failure").propagate(asm);
    } finally {
      fail.clearMessage();
    }
    nextErrorReportMs = now + ERROR_THROTTLE_MS;
  }

  private boolean ensureSchedule(MbMessageAssembly assembly) throws MbException {
    if (zone == null) zone = isBlank(timeZone) ? ZoneId.systemDefault() : ZoneId.of(timeZone);

    if (isBlank(cron)) {
      propagateFailureOnce(assembly, "Property 'cron' is required.");
      return false;
    }

    if (expr == null) {
      try {
        CronExpression e = new CronExpression(cron);
        e.setTimeZone(TimeZone.getTimeZone(zone));
        expr = e;
        nextFireEpochMs = -1L;
      } catch (ParseException pe) {
        propagateFailureOnce(assembly, "Invalid cron: " + pe.getMessage());
        return false;
      }
    }

    if (nextFireEpochMs < 0) {
      Date now  = Date.from(ZonedDateTime.now(zone).toInstant());
      Date next = expr.getNextValidTimeAfter(now);
      if (next == null) {
        propagateFailureOnce(assembly, "Cron has no future fire time.");
        return false;
      }
      nextFireEpochMs = next.getTime();
    }
    return true;
  }

  @Override
  public int run(MbMessageAssembly assembly) throws MbException {
    if (!ensureSchedule(assembly)) return TIMEOUT;

    long nowMs = System.currentTimeMillis();
    if (nowMs < nextFireEpochMs) return TIMEOUT;

    MbMessage msg = createMessage(new byte[0]);  // skeleton with Properties
    try {
      MbElement root = msg.getRootElement();
      setJsonPropertiesOnExisting(root);

      String iso = ZonedDateTime.now(zone).toString();
      makeJsonBody(root, "cron trigger", "time", iso);

      msg.finalizeMessage(MbMessage.FINALIZE_VALIDATE);

      MbMessageAssembly outAsm = new MbMessageAssembly(assembly, msg);
      dispatchThread();
      getOutputTerminal("out").propagate(outAsm);

      Date next = expr.getNextValidTimeAfter(new Date(nowMs));
      nextFireEpochMs = (next == null) ? Long.MAX_VALUE : next.getTime();

      return SUCCESS_RETURN;

    } catch (Exception e) {
      propagateFailureOnce(assembly, "Runtime error: " + e.toString());
      return FAILURE_CONTINUE;
    } finally {
      msg.clearMessage();
    }
  }

  public void onDelete() { /* nothing to free */ }
}
