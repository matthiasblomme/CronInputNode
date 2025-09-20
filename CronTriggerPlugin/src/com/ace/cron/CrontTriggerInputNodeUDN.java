package com.ace.cron;

import com.ibm.broker.config.appdev.InputTerminal;
import com.ibm.broker.config.appdev.Node;
import com.ibm.broker.config.appdev.NodeProperty;
import com.ibm.broker.config.appdev.OutputTerminal;

/*** 
 * <p>  <I>CrontTriggerInputNodeUDN</I> instance</p>
 * <p></p>
 */
public class CrontTriggerInputNodeUDN extends Node {

	private static final long serialVersionUID = 1L;

	// Node constants
	protected final static String NODE_TYPE_NAME = "com/ace/cron/CronTriggerInputNode";
	protected final static String NODE_GRAPHIC_16 = "platform:/plugin/CronTriggerPlugin/icons/full/obj16/com/ace/cron/CronTriggerInput.gif";
	protected final static String NODE_GRAPHIC_32 = "platform:/plugin/CronTriggerPlugin/icons/full/obj30/com/ace/cron/CronTriggerInput.gif";

	protected final static String PROPERTY_CRON = "cron";
	protected final static String PROPERTY_TIMEZONE = "timeZone";

	protected NodeProperty[] getNodeProperties() {
		return new NodeProperty[] {
			new NodeProperty(CrontTriggerInputNodeUDN.PROPERTY_CRON,		NodeProperty.Usage.MANDATORY,	false,	NodeProperty.Type.STRING, null,"","",	"com/ace/cron/CronTriggerInput",	"CronTriggerPlugin"),
			new NodeProperty(CrontTriggerInputNodeUDN.PROPERTY_TIMEZONE,		NodeProperty.Usage.OPTIONAL,	false,	NodeProperty.Type.STRING, null,"","",	"com/ace/cron/CronTriggerInput",	"CronTriggerPlugin")
		};
	}

	public CrontTriggerInputNodeUDN() {
	}

	@Override
	public InputTerminal[] getInputTerminals() {
		return null;
	}

	public final OutputTerminal OUTPUT_TERMINAL_CATCH = new OutputTerminal(this,"OutTerminal.catch");
	public final OutputTerminal OUTPUT_TERMINAL_FAILURE = new OutputTerminal(this,"OutTerminal.failure");
	public final OutputTerminal OUTPUT_TERMINAL_OUT = new OutputTerminal(this,"OutTerminal.out");
	@Override
	public OutputTerminal[] getOutputTerminals() {
		return new OutputTerminal[] {
			OUTPUT_TERMINAL_CATCH,
			OUTPUT_TERMINAL_FAILURE,
			OUTPUT_TERMINAL_OUT
		};
	}

	@Override
	public String getTypeName() {
		return NODE_TYPE_NAME;
	}

	protected String getGraphic16() {
		return NODE_GRAPHIC_16;
	}

	protected String getGraphic32() {
		return NODE_GRAPHIC_32;
	}

	/**
	 * Set the <I>CrontTriggerInputNodeUDN</I> "<I>cron</I>" property
	 * 
	 * @param value String ; the value to set the property "<I>cron</I>"
	 */
	public CrontTriggerInputNodeUDN setCron(String value) {
		setProperty(CrontTriggerInputNodeUDN.PROPERTY_CRON, value);
		return this;
	}

	/**
	 * Get the <I>CrontTriggerInputNodeUDN</I> "<I>cron</I>" property
	 * 
	 * @return String; the value of the property "<I>cron</I>"
	 */
	public String getCron() {
		return (String)getPropertyValue(CrontTriggerInputNodeUDN.PROPERTY_CRON);
	}

	/**
	 * Set the <I>CrontTriggerInputNodeUDN</I> "<I>Scheduled timeZone</I>" property
	 * 
	 * @param value String ; the value to set the property "<I>Scheduled timeZone</I>"
	 */
	public CrontTriggerInputNodeUDN setTimeZone(String value) {
		setProperty(CrontTriggerInputNodeUDN.PROPERTY_TIMEZONE, value);
		return this;
	}

	/**
	 * Get the <I>CrontTriggerInputNodeUDN</I> "<I>Scheduled timeZone</I>" property
	 * 
	 * @return String; the value of the property "<I>Scheduled timeZone</I>"
	 */
	public String getTimeZone() {
		return (String)getPropertyValue(CrontTriggerInputNodeUDN.PROPERTY_TIMEZONE);
	}

	public String getNodeName() {
		String retVal = super.getNodeName();
		if ((retVal==null) || retVal.equals(""))
			retVal = "CrontTriggerInput";
		return retVal;
	};
}
