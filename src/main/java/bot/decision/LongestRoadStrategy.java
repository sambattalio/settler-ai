package bot.decision;

import soc.game.*;

import soc.debug.D;

import soc.robot.SOCPossibleCard;
import soc.robot.SOCPossibleCity;
import soc.robot.SOCPossiblePiece;
import soc.robot.SOCPossibleSettlement;
import soc.robot.SOCPossibleRoad;
import soc.robot.SOCRobotDM;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import static soc.game.SOCResourceConstants.WOOD;
import static soc.game.SOCResourceConstants.CLAY;

import bot.NDHelpers;

import static soc.robot.SOCPossiblePiece.SETTLEMENT;
import static soc.robot.SOCPossiblePiece.ROAD;
import static soc.robot.SOCPossiblePiece.CITY;
import static soc.robot.SOCPossiblePiece.CARD;

public class LongestRoadStrategy {

    public static boolean shouldUse(SOCGame game, SOCPlayer player) {
    	//TODO check how far ahead we are and make sure we are not trapped
        return NDHelpers.isLongestRoadPossible(game, player.getPlayerNumber()) && !(NDHelpers.canSwitchFromLongestRoad(game, player.getPlayerNumber()));
    }

    public static SOCPossiblePiece plan(DecisionTreeDM decisionTreeDM) {
    	D.ebugPrintln("----- Start Longest Road Plan-----");
        Optional<SOCPossibleSettlement> possibleSettlement;
        Optional<SOCPossibleCity> possibleCity;
        Optional<SOCPossiblePiece> possibleRoad;

        //TODO Check if road is threatened before performing other actions
        if (NDHelpers.haveResourcesForRoadAndSettlement(decisionTreeDM.getBrain())) {
            D.ebugPrintln("----- Road & Settlement -----");
            Optional<SOCPossiblePiece> result = NDHelpers.findQualityRoadForLongestRoad(decisionTreeDM.getBrain());
            //TODO add other checks like this
            if(result.isPresent()) {
            	return result.get();
			}
        }

        if (decisionTreeDM.getBrain().getAttempt(SETTLEMENT) && NDHelpers.canBuildSettlement(decisionTreeDM.getPlayerNo(), decisionTreeDM.getBrain()) && (possibleSettlement = NDHelpers.findQualitySettlementFor(Arrays.asList(WOOD, CLAY), decisionTreeDM.getBrain())).isPresent()) {
        	D.ebugPrintln("----- Settlement -----");
		   	return possibleSettlement.get();
        } else {
        	D.ebugPrintln("No settlement");
        }	

        //decisionTreeDM.getBrain().getAttempt(ROAD) && 
        if(decisionTreeDM.getBrain().getAttempt(ROAD) &&  (possibleRoad = NDHelpers.findQualityRoadForLongestRoad(decisionTreeDM.getBrain())).isPresent()) {
        	D.ebugPrintln("----- Road -----");
	        return possibleRoad.get();
        } else {
        	D.ebugPrintln("No Road");
        }

        //decisionTreeDM.getBrain().getAttempt(CITY) && 
        if (decisionTreeDM.getBrain().getAttempt(CITY) &&  (possibleCity = NDHelpers.findQualityCityFor(Arrays.asList(WOOD, CLAY), decisionTreeDM.getBrain())).isPresent()) {
        	D.ebugPrintln("----- City -----");
        	return possibleCity.get();
        }
        else {
        	D.ebugPrintln("No quality city");
        }

        if (NDHelpers.haveResourcesFor(CARD, decisionTreeDM.getBrain()) && NDHelpers.getPlayerResources(decisionTreeDM.getBrain()).getTotal() > 5) {
        	D.ebugPrintln("----- Card -----");
            return new SOCPossibleCard(decisionTreeDM.getPlayer(), 0);
        } else {
        	D.ebugPrintln("No card");
        }

        D.ebugPrintln("Reached Null");

        return null;
    }
}
