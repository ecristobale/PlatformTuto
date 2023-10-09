package entities;

import static utils.Constants.EnemyConstants.*;
import static utils.Constants.Dialogue.*;
import static utils.HelperMethods.isFloor;

import gamestates.Playing;

public class Crabby extends Enemy {

    public Crabby(float x, float y) {
        super(x, y, CRABBY_WIDTH, CRABBY_HEIGHT, CRABBY);
        initHitbox(22, 19);
        initAttackBox(82, 19, 30);
    }

    public void update(int[][] lvlData, Playing playing) {
        updateBehavior(lvlData, playing);
        updateAnimationTick();
        updateAttackBox();
    }

    private void updateBehavior(int[][] lvlData, Playing playing) {
        if (firstUpdate)
            firstUpdateCheck(lvlData);

        if (inAir)
            inAirChecks(lvlData, playing);
        else {
            switch(state) {
            case IDLE:
                if (isFloor(hitbox, lvlData))
                    newState(RUNNING);
                else
                    inAir = true;
                break;
            case RUNNING:
                if (canSeePlayer(lvlData, playing.getPlayer())) {
                    turnTowardsPlayer(playing.getPlayer());
                    if (isPlayerCloseForAttack(playing.getPlayer()))
                        newState(ATTACK);
                }
                move(lvlData);

                if (inAir)
                    playing.addDialogue((int) hitbox.x, (int) hitbox.y, EXCLAMATION);

                break;
            case ATTACK:
                if (animationIndex == 0) {
                    attackChecked = Boolean.FALSE;
                }
                if (animationIndex == 3 && !attackChecked) {
                    checkPlayerHit(attackBox, playing.getPlayer());
                }
                break;
            case HITTED:
                if (animationIndex <= getSpriteAmount(enemyType, state) - 2)
                    pushBack(pushBackDir, lvlData, 2f);
                updatePushBackDrawOffset();
                break;
            }
        }
    }
}
