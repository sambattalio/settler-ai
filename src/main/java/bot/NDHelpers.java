package bot;

import soc.game.*;
import soc.robot.*;
import soc.debug.D;

import java.util.*;

import static java.lang.Math.abs;

public class NDHelpers {
    
    static final int MAX_ROAD_DIFF = 3;
    static final int MAX_ARMY_DIFF = 2;

    /* Returns true if input player has longest or is within 3 roads */
    public static boolean isCompetitiveForLongestRoad(SOCGame game, int playerNo) {
        SOCPlayer bestPlayer = game.getPlayerWithLongestRoad();

        /* Of course i know him... he's me */
        if (bestPlayer.getPlayerNumber() == playerNo) return true;

        /* Check diff in length */
        SOCPlayer ndBot = game.getPlayer(playerNo);
        return isLongestRoadPossible(game, playerNo) && abs(ndBot.getLongestRoadLength() - bestPlayer.getLongestRoadLength()) <= MAX_ROAD_DIFF;
    }
  
    /**
     * Returns if playerNo is deemed competitive for largest army.
     *
     * @param game
     * @param playerNo
     * @return true if army count - best army count < MAX_ARMY_DIFF
     */
    public static boolean isCompetitiveForLargestArmy(SOCGame game, int playerNo) {
        SOCPlayer bestPlayer = game.getPlayerWithLargestArmy();
        if (bestPlayer.getPlayerNumber() == playerNo) return true;

        /* Check diff in length */
        SOCPlayer ndBot = game.getPlayer(playerNo);

        return game.getNumDevCards() != 0 && abs(ndBot.getNumKnights() - bestPlayer.getNumKnights()) <= MAX_ARMY_DIFF;
    }

    /**
     * Returns if a player can afford to build settlement, given a resourceset
     * 
     * @param resources
     * @return true if can afford else false
     */
    public static boolean canAffordSettlement(SOCResourceSet resources) {
        return (resources.contains(SOCResourceConstants.CLAY) && resources.contains(SOCResourceConstants.WOOD)
             && resources.contains(SOCResourceConstants.WHEAT) && resources.contains(SOCResourceConstants.SHEEP));
    }

    /**
     * Returns if a player can build a settlement
     *
     * @param game
     * @param playerNo
     * @return true if possible to build a settlement
     */
    public static boolean canBuildSettlement(SOCGame game, int playerNo) {
         return game.getPlayer(playerNo).getPotentialSettlements().size() != 0;
    }


    /**
     * Returns if there exists a settlement that will yield the resources in the set
     *
     * TODO maybe ports too?
     * Right now only handles arrays up to size 3
     *
     * @param game
     * @param playerNo
     * @param resources: int array where integers are SOCResourceConstant resource types
     * @return if there is a settlement that can yield these resources
     */
    public static boolean existsQualitySettlementFor(SOCGame game, int playerNo, List<Integer> resources) {
        return !findPotentialSettlementsFor(game, playerNo, resources).isEmpty();       
    }

     /**
     * Returns coords of settlements that will yield the resources in the set
     *
     * TODO maybe ports too?
     * Right now only handles arrays up to size 3
     *
     * @param game
     * @param playerNo
     * @param resources: int array where integers are SOCResourceConstant resource types
     * @return coords vector 
     */
    public static Vector<Integer> findPotentialSettlementsFor(SOCGame game, int playerNo, List<Integer> resources) {

        Vector<Integer> nodes = new Vector<Integer>();

        for (int node : game.getPlayer(playerNo).getPotentialSettlements_arr()) {
            
        	Set<Integer> resourceSet = new HashSet<>(); //Had weird error with constructor
            for (int i : resources) {
                resourceSet.add(i);
            }

            for (int hex : game.getBoard().getAdjacentHexesToNode(node)) {
               resourceSet.remove(game.getBoard().getHexTypeFromCoord(hex));
            }
            if (resourceSet.isEmpty()) nodes.add(node);
        }

        return nodes;
    }


     /**
     * Returns *coord* of the best possible settlement for given resources
     *
     * @param board
     * @param playerNo
     * @param resources
     *
     * @return best settlement
     *
     */
    public static SOCPossibleSettlement bestPossibleSettlement(SOCGame game, SOCPlayer player, List<Integer> resources) {
        int playerNo = player.getPlayerNumber();
    	Vector<Integer> possible_nodes = findPotentialSettlementsFor(game, playerNo, resources);
        
        int best_node = possible_nodes.get(0);

        for (int i = 1; i < possible_nodes.size(); i++) {
            if (NDRobotDM.totalProbabilityAtNode(game, best_node) > NDRobotDM.totalProbabilityAtNode(game, possible_nodes.get(i))) {
                best_node = possible_nodes.get(i); 
            }
        }
        
        return new SOCPossibleSettlement(player, best_node, null); //TODO add potential road list
    }


    /**
     * Finds possible roads that can be built from given coord
     *
     * @param game
     * @param edgeCoord edge to build off of
     * @return Vector of coords
     */
    public static Vector<Integer> findPossibleRoads(SOCGame game, final int edgeCoord) {
        
        Vector<Integer> possibleRoads = new Vector<Integer>();
        
        for (int edge : game.getBoard().getAdjacentEdgesToEdge(edgeCoord)) {
            if (canBuildRoadTwo(game, edge, edgeCoord)) {
                possibleRoads.add(edge);
            } 
        }

        return possibleRoads;
    }

    /**
     * Returns coord of best possible road to place to maximize length
     *
     * @param game
     * @param playerNo
     * @return best road to build
     */
    public static SOCPossibleRoad bestPossibleLongRoad(SOCGame game, SOCPlayer player) {
        // for now the strat is to try to build off of the longest road(s)
        // of the player
        // TODO maybe import ?
        Vector<SOCLRPathData> pathData = game.getPlayer(player.getPlayerNumber()).getLRPaths();

        for (SOCLRPathData path : pathData) {
            // check if can build off beginning

            Vector<Integer> possibleFront = findPossibleRoads(game, path.getBeginning());
            // for now just return the first possible... later we need to prolly
            // search this shizz our
            if (possibleFront.size() != 0) return new SOCPossibleRoad(player, possibleFront.get(0), null);

            // same but end...
            Vector<Integer> possibleEnd = findPossibleRoads(game, path.getEnd());
            // for now just return the first possible... later we need to prolly
            // search this shizz our
            if (possibleEnd.size() != 0) return new SOCPossibleRoad(player, possibleEnd.get(0), null);
        }
        
        return null;
    }

    /**
     * Returns if longest road is possible to build
     *
     * @param game
     * @param playerNo
     * @return true if can build to longest road
     */
    public static boolean isLongestRoadPossible(SOCGame game, int playerNo) {
        if (!isCompetitiveForLongestRoad(game, playerNo)) return false;
     
        // here we know we are competitive... now lets see if we can reach
        //TODO try to build off getLRPaths()????
        
        return true;
    }

    public static boolean canBuildRoadTwo(SOCGame game, final int edgeCoord, final int sourceEdge) {
        for (SOCRoutePiece r : game.getBoard().getRoadsAndShips()) {
            if (edgeCoord == r.getCoordinates()) {
                return false;
            }
        }

        // TODO replace with a 'node on edges' method call
        List<Integer> nodesOnEdgeOne = game.getBoard().getAdjacentNodesToEdge(edgeCoord);
        List<Integer> nodesOnEdgeTwo = game.getBoard().getAdjacentNodesToEdge(sourceEdge);
        Optional<Integer> connectingNode = nodesOnEdgeOne.stream().filter(nodesOnEdgeTwo::contains).findFirst();

        if(!connectingNode.isPresent()) return false; // edges do not touch
        
        return true;
    }

}
