package entities;

import static utils.Constants.Dialogue.*;
import static utils.Constants.Directions.LEFT;
import static utils.Constants.EnemyConstants.*;
import static utils.HelperMethods.canMoveHere;
import static utils.HelperMethods.isFloor;

import gamestates.Playing;

public class Shark extends Enemy {

    public Shark(float x, float y) {
        super(x, y, SHARK_WIDTH, SHARK_HEIGHT, SHARK);
        initHitbox(18, 22);
        initAttackBox(20, 20, 20);
    }

    public void update(int[][] lvlData, Playing playing) {
        updateBehavior(lvlData, playing);
        updateAnimationTick();
        updateAttackBoxFlip();
    }

    private void updateBehavior(int[][] lvlData, Playing playing) {
        if (firstUpdate)
            firstUpdateCheck(lvlData);

        if (inAir)
            inAirChecks(lvlData, playing);
        else {
            switch (state) {
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
                break;
            case ATTACK:
                if (animationIndex == 0)
                    attackChecked = false;
                else if (animationIndex == 3) {
                    if (!attackChecked)
                        checkPlayerHit(attackBox, playing.getPlayer());
                    attackMove(lvlData, playing);
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

    protected void attackMove(int[][] lvlData, Playing playing) {
        float xSpeed = 0;

        if (walkDir == LEFT)
            xSpeed = -walkSpeed;
        else
            xSpeed = walkSpeed;

        if (canMoveHere(hitbox.x + xSpeed * 4, hitbox.y, hitbox.width, hitbox.height, lvlData))
            if (isFloor(hitbox, xSpeed * 4, lvlData)) {
                hitbox.x += xSpeed * 4;
                return;
            }
        newState(IDLE);
        playing.addDialogue((int) hitbox.x, (int) hitbox.y, EXCLAMATION);
    }
}
