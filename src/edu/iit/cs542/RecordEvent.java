package edu.iit.cs542;

/**
 * Created by Parth on 2/25/2016.
 */
public class RecordEvent {

    private Record __record = null;
    private String __source = null;
    private String __receiver = null;
    private String __eventType = Events.ROUTER_UPDATE_EVENT;

    public Record getRecord() {
        return __record;
    }

    public void setRecord(Record __record) {
        this.__record = __record;
    }

    public String getSource() {
        return __source;
    }

    public void setSource(String __source) {
        this.__source = __source;
    }

    public String getReceiver() {
        return __receiver;
    }

    public void setReceiver(String __receiver) {
        this.__receiver = __receiver;
    }

    public String getEventType() {
        return __eventType;
    }

    public void setEventType(String __eventType) {
        this.__eventType = __eventType;
    }

    @Override
    public String toString() {
        return "[ Source :" + __source + ",Record : " + __record + " ]" ;
    }
}
