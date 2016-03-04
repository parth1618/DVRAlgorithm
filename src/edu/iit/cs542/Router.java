package edu.iit.cs542;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by Parth on 2/25/2016.
 */
public class Router {

    private ArrayList<Record> __routerTable; /* router table */
    private String __name; /* router name */
    private Queue<RecordEvent> __eventQueue; /*queue to hold events */
    private ArrayList<Router> __neighbors; /*list to hold all neighbours */
    private String __state; /* to determine the state of router. Ready, Processing or stable */
    private AlgorithmImpl __algoImplThread; /*Thread to implement update */
    private Object lock = new Object(); /* lock object to synchronize router state change */

    public Router(String __name) {
        this.__name = __name;
        __routerTable = new ArrayList<>();
        __eventQueue = new LinkedList<>();
        __algoImplThread = new AlgorithmImpl(this,__eventQueue,__name + "-algoImplThread");
        __algoImplThread.start();
    }

    public void setRouterTable(ArrayList<Record> __routerTable) {
        this.__routerTable = __routerTable;
    }

    public ArrayList<Record> getRouterTable() {
        return __routerTable;
    }

    public void setNeighbors(ArrayList<Router> __neighbors) {
        this.__neighbors = __neighbors;
    }

    public ArrayList<Router> getNeighbors() {
        synchronized (__neighbors) {
            return __neighbors;
        }
    }

    public void removeNeighbor(Router router) {
        synchronized (__neighbors) {
            __neighbors.remove(router);
        }
    }

    public void setState(String __state) {
        synchronized(lock){
            this.__state = __state;
        }
    }

    public String getState() {
        synchronized(lock){
            return __state;
        }
    }

    public String getName() {
        return __name;
    }

    public int getNumIteration() {
        return __algoImplThread.getNumIteration();
    }

    public void init() {
        sendEventToNeighbors("");

    }

    /* for each record in routing table notify all other neighbour */
    public void sendEventToNeighbors(String excludeRouter) {

        for(Router neighborRouter : __neighbors) {
            for(Record record : __routerTable) {
                RecordEvent recordEvent = buildRecordEvent(record);
                if(!neighborRouter.__name.equalsIgnoreCase(record.getDestRouter())) {
                    neighborRouter.update(this, recordEvent);
                }
            }
        }
    }

    private RecordEvent buildRecordEvent(Record record) {

        RecordEvent recordEvent = new RecordEvent();
        recordEvent.setSource(__name);
        recordEvent.setRecord(record);

        return recordEvent;
    }

    /* update method is synchronize so two router can update at same time */
    public synchronized void update(Object obj, Object recordEvent){
        addToQueue((RecordEvent)recordEvent);
    }

    private void addToQueue(RecordEvent recordEvent) {
        synchronized(__eventQueue){
            __eventQueue.add(recordEvent);
            __eventQueue.notify();

        }
    }

    public RecordEvent removeEventFromQueue() {
        synchronized(__eventQueue) {
            return __eventQueue.poll();
        }
    }

    public void addToTable(String destinationRouter, int cost, String nextHop) {
        Record record = new Record();
        record.setDestRouter(destinationRouter);
        record.setCost(cost);
        record.setNextHop(nextHop);
        __routerTable.add(record);
    }

    public void updateRoutingTable(String destination, int cost, String nextHop) {
        Record record = this.findDestinationInTable(destination);
        // If the destination network was found.
        if(record != null) {
            record.setCost(cost);
            record.setNextHop(nextHop);
        }
    }

    public Record findDestinationInTable(String destination) {
        Iterator<Record> routingTableIterator = __routerTable.iterator();
        while(routingTableIterator.hasNext()) {
            Record record = routingTableIterator.next();
            if(record.getDestRouter().equals(destination)) {
                return record;
            }
        }
        return null;
    }

    public String printRoutingTable(){
        String out = "Routing Table for "+__name+":";
        for(Record tableEntry : __routerTable){
            out = out+tableEntry+":";
        }
        System.out.println(out);
        return "";
    }

    public void shutdown(){
        __algoImplThread.shutdown();
    }
}
