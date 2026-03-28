import java.util.Map;
import java.util.TreeMap;

public class Statistics {
    private final Map<Integer, Integer> nodesPerDepth;
    private final Map<Integer, Integer> prunedPerDepth;
    private int maxObservedDepth;
    
    public Statistics(){
        this.nodesPerDepth = new TreeMap<>();
        this.prunedPerDepth = new TreeMap<>();
        this.maxObservedDepth = 0;
    }

    public void incrementNode(int depth){
        nodesPerDepth.put(depth, nodesPerDepth.getOrDefault(depth, 0) + 1);
        if (depth > maxObservedDepth){
            maxObservedDepth = depth;
        }
    }

    public void incrementPruned(int depth){
        prunedPerDepth.put(depth, prunedPerDepth.getOrDefault(depth, 0) + 1);
        if (depth > maxObservedDepth){
            maxObservedDepth = depth;
        }
    }

    public Map<Integer, Integer> getNodesPerDepth(){
        return nodesPerDepth;
    }

    public Map<Integer, Integer> getPrunedPerDepth(){
        return prunedPerDepth;
    }

    public int getMaxObservedDepth(){
        return maxObservedDepth;
    }
}
