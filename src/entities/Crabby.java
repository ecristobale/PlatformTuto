package entities;

import static utils.Constants.Directions.LEFT;
import static utils.Constants.EnemyConstants.*;
import static utils.HelperMethods.canMoveHere;
import static utils.HelperMethods.getEntityYPositionUnderRoofOrAboveFloor;
import static utils.HelperMethods.isEntityOnFloor;
import static utils.HelperMethods.isFloor;

import main.Game;

public class Crabby extends Enemy {

    public Crabby(float x, float y) {
        super(x, y, CRABBY_WIDTH, CRABBY_HEIGHT, CRABBY);
        initHitbox(x, y, (int) (22 * Game.SCALE), (int) (19 * Game.SCALE));
    }

    private void updateMove(int[][] lvlData, Player player) {
        if (firstUpdate)
            firstUpdateCheck(lvlData);

        if (inAir)
            updateInAir(lvlData);
        else {
            switch(enemyState) {
            case IDLE:
                newState(RUNNING);
                break;
            case RUNNING:
                if (canSeePlayer(lvlData, player))
                    turnTowardsPlayer(player);
                if (isPlayerCloseForAttack(player))
                    newState(ATTACK);

                move(lvlData);
                break;
            }
        }
    }

    public void update(int[][] lvlData, Player player) {
        updateMove(lvlData, player);
        updateAnimationTick();
    }

}
