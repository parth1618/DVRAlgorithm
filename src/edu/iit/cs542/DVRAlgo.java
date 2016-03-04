package edu.iit.cs542;

import java.io.*;
import java.util.*;

/**
 * Created by Parth on 2/25/2016.
 */
public class DVRAlgo {

    private ArrayList<Record>[] __routerTableDetail; /*routing table*/
    private int __numRouter = 0; /*number of router */
    private final int MAX_ROUTER = 6; /* default number of router */
    private Router[] __routerList = null; /*router in network */
    private HashMap<String, ArrayList<String>> __observerMap = new HashMap<>();

    public DVRAlgo() {

        __routerTableDetail = new ArrayList[MAX_ROUTER];
    }

    /* Read Routing Table Info from Input file default.txt */
    private void readInputFile() {

        try {

           /* File f1 = new File("default.txt");
            FileReader fr = new FileReader(f1.getAbsoluteFile());
            BufferedReader br = new BufferedReader(fr);*/
            InputStream is = DVRAlgo.class.getResourceAsStream("/default.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            String line;

            while ((line = br.readLine()) != null) {
                line = line.trim().replaceAll(" +", " ");

                String[] lineToken = line.split(" ");
                __numRouter++;

                ArrayList<Record> routerTable = new ArrayList<>();
                ArrayList<String> obsList = new ArrayList<>();
                // this is the column count.
                int destRouter = 1;
                for (String lt : lineToken) {
                    String token = lt.trim();

                    Record record = new Record();
                    // Assuming that the input is always an int.
                    int costFromInput = Integer.parseInt(token);

                    String destRouterName = "R" + destRouter++;
                    String nextHop = "R" + __numRouter;

                    record.setCost(costFromInput);
                    record.setDestRouter(destRouterName);
                    record.setNextHop(Events.NO_HOP);

                    if (costFromInput != Events.NO_PATH_INPUT_COST && costFromInput != 0) {
                        // Creating HashMap<String,List<String>> to hold the list of neighbours for a particular router.
                        // This map will be used while registering each router with their neighbours( registerRouters())
                        if (__observerMap.containsKey(nextHop)) {
                            obsList = __observerMap.get(nextHop);
                            if (obsList == null) {
                                new ArrayList<String>().add(destRouterName);
                            } else {
                                obsList.add(destRouterName);
                            }
                        } else {
                            // add new to the map
                            obsList.add(destRouterName);
                            __observerMap.put(nextHop, obsList);
                        }
                    }

                    // Put the routing table entry to the Routing table.
                    routerTable.add(record);

                }
                __routerTableDetail[__numRouter - 1] = routerTable;
            }

            br.close();

        } catch (FileNotFoundException fnfe) {
            System.out.println(fnfe);
        } catch (IOException | NumberFormatException ioe) {
            System.out.println(ioe);
        }

    }

    /* Print Routing Table */
    private void printRoutingTable() {

        System.out.println("\nDefault Routing Table:");
        System.out.println();

        for (int i = 1; i <= __numRouter; i++) {
            System.out.print("R" + i + "\t");
        }
        System.out.println();
        System.out.print("--------------------------------------------");
        System.out.println();

        for (int i = 0; i < __numRouter; i++) {
            ArrayList<Record> routerTable = __routerTableDetail[i];
            Iterator<Record> routerTableIterator = routerTable.iterator();

            while (routerTableIterator.hasNext()) {
                Record tableEntry = routerTableIterator.next();
                System.out.print(tableEntry.getCost() + "\t");

            }
            System.out.println();
        }
    }

    /* Init all Router with respective routing table */
    private void initRouters() {

        __routerList = new Router[__numRouter];
        for (int i = 0; i < __numRouter; i++) {
            __routerList[i] = new Router("R" + (i + 1));
            // Now set its routing table.
            __routerList[i].setRouterTable(__routerTableDetail[i]);
            //__routerList[i].printRoutingTable();	//option optional to check routing table for every router
        }
        // subscribe each router to the neighbouring routers for receiving notification on routing table modification.
        registerRouters();
        //Initialize the routers to share the routing info with neighbours
        for (Router router : __routerList)
        {
            router.init();
        }
    }

    /* use to register router with their respective neighbours info */
    private void registerRouters() {

        Iterator<Map.Entry<String,ArrayList<String>>> it = __observerMap.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry<String,ArrayList<String>> entry = it.next();
            String key = entry.getKey();
            ArrayList<String> valueList = entry.getValue();
            registerNeighbors(key,valueList);
        }

    }

    private void registerNeighbors(String key, ArrayList<String> valueList) {

        String indexStr = key.substring(key.length() -1);
        int routerIndex = Integer.parseInt(indexStr);

        Router router = __routerList[routerIndex -1]; /* Get router object from index */
        ArrayList<Router> neighborList = new ArrayList<>();

        for(String value : valueList) {

            String str = value.substring(value.length()-1);
            int neighborIndex = Integer.parseInt(str);
            neighborList.add(__routerList[neighborIndex-1]);
        }
        router.setNeighbors(neighborList);
    }

    public void waitForRouteUpdate() {
        boolean flag = true;

        while (flag) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) { System.out.println(e);
            }

            if (this.checkStableRouters()) {
                this.printFinalRoutingTable();
                flag = false;
            }
        }
    }

    private boolean checkStableRouters() {

        boolean stable = false;
        int stableCount = 0;
        for (Router router : __routerList) {
            if (router.getState().equals(Events.STABLE_STATE)) {
                stableCount++;
            }
        }
        if (stableCount == __routerList.length) {
            stable = true;
        }

        return stable;
    }

    private void printFinalRoutingTable() {
        System.out.println("\n\nFinal Routing table computed by DV Algorithm:");
        System.out.println();
        for (int i = 1; i <= __numRouter; i++) {
            System.out.print("R" + i + "\t");
        }
        System.out.println();
        System.out.print("-------------------------------------------");
        System.out.println();

        for(Router router : __routerList) {
            ArrayList<Record> routingTable = router.getRouterTable();
            for(Record tableEntry : routingTable) {
                System.out.print(tableEntry.getCost() + "\t");
            }
            System.out.println();
        }
    }

    private void computeShortestPath(int sourceRouter, int destRouter) {

        // Are the source router and destination router numbers right?
        if (sourceRouter < 1 || sourceRouter > __routerList.length || destRouter < 1 ||
                destRouter > __routerList.length || sourceRouter == destRouter) {
            System.out.println("No/Invalid source or destination router number specified");
        }

        StringBuffer strbuf = new StringBuffer();
        strbuf.append(sourceRouter);
        Router source = __routerList[sourceRouter - 1];
        String destination = __routerList[destRouter - 1].getName();
        int spCost = 0;

        //Trace the path to destination from source routing table
        Router router = source;
        boolean pathFound = false;

        while (!pathFound) {
            // Check if the route exists between the source and destination.
            Record tableEntry = getRoutingTableDetails(router, destination);
            if (tableEntry == null) {
                // No path found to destination router
                System.out.println("No path found from R" + sourceRouter + "to R" + destRouter);
                pathFound = false;
                break;
            } else {
                if (router.getName().equalsIgnoreCase(source.getName())) {
                    // this is the source router.
                    spCost = tableEntry.getCost(); // this value gives the total cost to destination

                    if (spCost == Events.NO_PATH_INPUT_COST) {
                        // No path to this destination
                        System.out.println("No path found from R" + sourceRouter + " to R" + destRouter);
                        pathFound = false;
                        break;

                    }
                }
                String nextHop = tableEntry.getNextHop();
                if (nextHop.equals("-")) {
                    // We have found the path for destination
                    strbuf.append("-").append(destRouter);
                    pathFound = true;
                    break;
                } else {
                    // Now traverse the routing table of Next Hop field to find the destination
                    String indexStr = null;

                    if (nextHop.length() == 2) {
                        indexStr = nextHop.substring(nextHop.length() - 1);
                    } else {
                        indexStr = nextHop.substring(nextHop.length() - 2);
                    }

                    int routerIdx = Integer.parseInt(indexStr);
                    router = __routerList[routerIdx - 1];
                    strbuf.append("-").append(indexStr); // add next hop to the path

                }
            }

        }

        if (pathFound) {
            System.out.println("Shortest Path from " + sourceRouter + " to " + destRouter + " is " + strbuf);
            System.out.println("Minimum Cost = " + spCost);
        }

    }

    private Record getRoutingTableDetails(Router router, String destination) {
        Record resultEntry = null;
        ArrayList<Record> routingTable = router.getRouterTable();
        //Iterate through the routing entries for a matching destination
        Iterator<Record> it = routingTable.iterator();
        while (it.hasNext()) {
            Record entry = it.next();
            String dest = entry.getDestRouter();
            if (null != dest && dest.equalsIgnoreCase(destination)) {
                //match found. return the routing table entry
                return entry;
            }
        }
        return resultEntry;
    }

    private void shutdown() {
        for (Router router : __routerList) {
            router.shutdown();
        }

    }

    private void simulateRouterFailure(int router){
        String routerNo = "R"+router;
        for(ArrayList<Record> recordList : __routerTableDetail)
        {
            for(Record record : recordList)
            {
                if(record.getDestRouter().equalsIgnoreCase(routerNo))
                {
                    record.setCost(Events.NO_PATH_INPUT_COST);
                    record.setNextHop(Events.NO_HOP);
                }
            }
        }
        for (int i = 0; i < __numRouter; i++)
        {
            __routerList[i].setRouterTable(__routerTableDetail[i]);
        }
        registerRouters();

        for (Router rtr : __routerList)
        {
            rtr.init();
        }
    }

    private int findNumIteration() {
        int max = __routerList[0].getNumIteration();
        for(Router _rtr : __routerList)
        {
            if(max < _rtr.getNumIteration())
            {
                max = _rtr.getNumIteration();
            }
        }
        return max;
    }

    public static void main(String[] args) {

        Scanner in = new Scanner(System.in);

        String isContinue;
        int numIteration;
        DVRAlgo dva = new DVRAlgo();

        System.out.println("\tTHE DISTANCE VECTOR(DV) ROUTING ALGORITHM");

        System.out.println("Reading defult topology...");
        dva.readInputFile();
        dva.printRoutingTable();
        dva.initRouters();
        dva.waitForRouteUpdate();
        dva.findNumIteration();
        numIteration = dva.findNumIteration();
        System.out.println("Required Number of Iteration: "+numIteration);
        do
        {
            System.out.println("\n1. Compute Shortest Path");
            System.out.println("2. Test Router Failure");
            System.out.println("3. Exit");
            System.out.println("Enter Your Choice: ");
            int ch = in.nextInt();
            switch (ch) {
                case 1:
                    System.out.println("Enter Source Router:");
                    int sourceRouter = in.nextInt();
                    System.out.println("Enter Destination Router:");
                    int destRouter = in.nextInt();
                    if(sourceRouter < 1 || destRouter > 6)
                    {
                        System.out.println("Invalid Entry. Router Range is between 1 & 6");
                    }
                    else
                    {
                        dva.computeShortestPath(sourceRouter, destRouter);

                    }
                    in.nextLine();
                    break;
                case 2:
                    System.out.println("Enter the number of router whose failure is to be simulated: ");
                    int router = in.nextInt();
                    dva.simulateRouterFailure(router);
                    dva.waitForRouteUpdate();
                    numIteration = dva.findNumIteration();
                    System.out.println("Required Number of Iteration: "+numIteration);
                    in.nextLine();
                    break;
                case 3:
                    System.exit(1);
                    break;
                default:
                    System.out.println("Invalid input");
                    break;
            }
            System.out.println("\nDo you want to continue? Y/N");
            isContinue = in.nextLine();

        }while(isContinue.equalsIgnoreCase("Y"));
        dva.shutdown();
    }
}
