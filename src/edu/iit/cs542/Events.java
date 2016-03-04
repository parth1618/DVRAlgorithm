package edu.iit.cs542;

/**
 * Created by Parth on 2/25/2016.
 */
public abstract class Events {

    public static final String READY_STATE = "Ready_State"; /*state of router before processing update */
    public static final String PROCESSING_STATE = "Processing_State"; /* state of router while processing update */
    public static final String STABLE_STATE = "Stable_State";  /*state of router after processing all event */
    public static final int NO_PATH_COST = 1000; /* internal no path */
    public static final int NO_PATH_INPUT_COST = 999; /* for non direct path in input file */
    public static final String NO_HOP = "-"; /* represent No NextHop */
    public static final String ROUTER_UPDATE_EVENT = "update"; /* hold router update event */
}
