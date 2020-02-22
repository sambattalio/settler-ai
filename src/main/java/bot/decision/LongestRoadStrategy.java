package bot.decision;

import soc.robot.SOCPossibleCard;
import soc.robot.SOCPossibleCity;
import soc.robot.SOCPossiblePiece;
import soc.robot.SOCPossibleSettlement;

import java.util.Arrays;
import java.util.Optional;

import static soc.game.SOCResourceConstants.WOOD;
import static soc.game.SOCResourceConstants.CLAY;
import static soc.robot.SOCPossiblePiece.SETTLEMENT;
import static soc.robot.SOCPossiblePiece.ROAD;
import static soc.robot.SOCPossiblePiece.CITY;
import static soc.robot.SOCPossiblePiece.CARD;

public class LongestRoadStrategy {

    public static boolean shouldUse() {
        return false;
    }

    public static SOCPossiblePiece plan(DecisionTreeDM decisionTreeDM) {
        Optional<SOCPossibleSettlement> possibleSettlement;
        Optional<SOCPossibleCity> possibleCity;

        if (decisionTreeDM.getHelpers().haveResourcesForRoadAndSettlement()) {
            return decisionTreeDM.getHelpers().findQualityRoad(true).orElse(null);
        } else if (decisionTreeDM.getHelpers().haveResourcesFor(SETTLEMENT)) {
            if (decisionTreeDM.getHelpers().canBuildSettlement() &&
                    (possibleSettlement = decisionTreeDM.getHelpers().findQualitySettlementFor(Arrays.asList(WOOD, CLAY))).isPresent()) {
                return possibleSettlement.get();
            } else {
                return decisionTreeDM.getHelpers().findQualityRoad(true).orElse(null);
            }
        } else if (decisionTreeDM.getHelpers().haveResourcesFor(ROAD)) {
            return decisionTreeDM.getHelpers().findQualityRoad(true).orElse(null);
        } else if (decisionTreeDM.getHelpers().haveResourcesFor(CITY) &&
                (possibleCity = decisionTreeDM.getHelpers().findQualityCityFor(Arrays.asList(WOOD, CLAY))).isPresent()) {
            return possibleCity.get();
        } else if (decisionTreeDM.getHelpers().haveResourcesFor(CARD)) {
            return new SOCPossibleCard(decisionTreeDM.getPlayer(), 0);
        }
        return null;
    }
}