package edu.iit.cs542;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Queue;

/**
 * Created by Parth on 2/25/2016.
 */
public class AlgorithmImpl extends Thread {

    private Router __router; /*hold the reference of router*/
    private Queue<RecordEvent> __recordQ; /*hold event pushed by router */
    private boolean __keepRunning = true; /*flag to keep thread running */
    private int __numIteration = 0;

    public AlgorithmImpl() {

    }

    public AlgorithmImpl(Router __router, Queue __recordQ, String __threadName) {

        this.__router =  __router;
        this.__recordQ = __recordQ;
        setName(__threadName);
    }

    public int getNumIteration() {
        return __numIteration;
    }

    @Override
    public void run() {

        int count = 0;
        __router.setState(Events.READY_STATE);
        while(__keepRunning) {
            RecordEvent recordEvent = getNextRecordEvent(); /* check for record event from neighbours */

            if(recordEvent != null) {
                count = 0;
                __numIteration++;
                __router.setState(Events.PROCESSING_STATE);
                updateRoutingTable(recordEvent);
            }
            else {

                try {
                    if(count == 3 && __router.getState().equals(Events.PROCESSING_STATE)) {
                        __router.setState(Events.STABLE_STATE);

                    }
                    synchronized(__recordQ) {
                        __recordQ.wait(1000);
                        count++;
                    }

                }
                catch(InterruptedException ie) {
                    System.out.println("Thread Interrrupted: "+ie);
                }
            }
        }
    }

    private RecordEvent getNextRecordEvent() {
        return __router.removeEventFromQueue();
    }

    /* used to update routing table */
    private void updateRoutingTable(RecordEvent recordEvent) {

        Record neighborRecord = recordEvent.getRecord();
        String sourceRouter = recordEvent.getSource();
        String destinationRouter = neighborRecord.getDestRouter();

        if(recordEvent.getEventType().equals(Events.ROUTER_UPDATE_EVENT)) {

            Record record = getRecord(destinationRouter);
            int linkCost = getCost(sourceRouter);

            if(record != null) {

                int netCost = linkCost + neighborRecord.getCost();
                int currentCost = record.getCost();
                String currentNextHop = record.getNextHop();

                if((netCost < currentCost) || (sourceRouter.equalsIgnoreCase(currentNextHop) && currentCost != netCost)) {

                    netCost = (netCost > Events.NO_PATH_INPUT_COST) ? Events.NO_PATH_INPUT_COST : netCost;
                    String nextHop = (netCost == Events.NO_PATH_INPUT_COST) ? Events.NO_HOP : sourceRouter ;

                    record.setCost(netCost);
                    record.setNextHop(nextHop);
                    __router.sendEventToNeighbors(sourceRouter);
                }
                else {
                    // no change is needed
                }
            }
            else {
                // insert new entry
                System.out.println("THREAD:"+this.getName()+ ":Inserting a new route entry ");

                int netCost = linkCost + neighborRecord.getCost();
                __router.addToTable(destinationRouter, netCost, sourceRouter);
                __router.sendEventToNeighbors(sourceRouter);
            }
        }
    }

    private Record getRecord(String destinationRouter) {

        Record record = null;

        ArrayList<Record> routerTable = __router.getRouterTable();
        Iterator<Record> it = routerTable.iterator();
        while(it.hasNext()){
            Record rcd = it.next();
            String dest = rcd.getDestRouter();
            if(dest != null && dest.equalsIgnoreCase(destinationRouter)) {
                return rcd;
            }
        }
        return record;
    }

    private int getCost(String sourceRouter) {
        int cost = 0;

        ArrayList<Record> routerTable = __router.getRouterTable();
        Iterator<Record> it = routerTable.iterator();
        while(it.hasNext()){
            Record rcd = it.next();
            String dest = rcd.getDestRouter();
            if(dest != null && dest.equalsIgnoreCase(sourceRouter)) {
                return rcd.getCost();
            }
        }
        return cost;
    }

    public void shutdown(){
        __keepRunning = false;
    }
}
