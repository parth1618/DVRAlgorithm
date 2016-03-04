package edu.iit.cs542;

/**
 * Created by Parth on 2/25/2016.
 */
public class Record {

    private String __destRouter; /*destination network. In this case, it is router */
    private int __cost;  /*cost to reach that router */
    private String __nextHop; /*next router to reach destination router */


    public String getDestRouter() {
        return __destRouter;
    }

    public void setDestRouter(String __destRouter) {
        this.__destRouter = __destRouter;
    }

    public int getCost() {
        return __cost;
    }

    public void setCost(int __cost) {
        this.__cost = __cost;
    }

    public String getNextHop() {
        return __nextHop;
    }

    public void setNextHop(String __nextHop) {
        this.__nextHop = __nextHop;
    }

    @Override
    public String toString() {
        String result = "[" + __destRouter + "," + __cost + "," + __nextHop + "]";
        return result;
    }
}
