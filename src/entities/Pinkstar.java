package entities;

import static utils.Constants.EnemyConstants.*;
import static utils.Constants.Dialogue.*;
import static utils.HelperMethods.canMoveHere;
import static utils.HelperMethods.isFloor;
import static utils.Constants.Directions.*;

import gamestates.Playing;

public class Pinkstar extends Enemy {

    private boolean preRoll = true;
    private int tickSinceLastDmgToPlayer;
    private int tickAfterRollInIdle;
    private int rollDurationTick, rollDuration = 300;

    public Pinkstar(float x, float y) {
        super(x, y, PINKSTAR_WIDTH, PINKSTAR_HEIGHT, PINKSTAR);
        initHitbox(17, 21);
    }

    public void update(int[][] lvlData, Playing playing) {
        updateBehavior(lvlData, playing);
        updateAnimationTick();
    }

    private void updateBehavior(int[][] lvlData, Playing playing) {
        if (firstUpdate)
            firstUpdateCheck(lvlData);

        if (inAir)
            inAirChecks(lvlData, playing);
        else {
            switch (state) {
            case IDLE:
                preRoll = true;
                if (tickAfterRollInIdle >= 120) {
                    if (isFloor(hitbox, lvlData))
                        newState(RUNNING);
                    else
                        inAir = true;
                    tickAfterRollInIdle = 0;
                    tickSinceLastDmgToPlayer = 60;
                } else
                    tickAfterRollInIdle++;
                break;
            case RUNNING:
                if (canSeePlayer(lvlData, playing.getPlayer())) {
                    newState(ATTACK);
                    setWalkDir(playing.getPlayer());
                }
                move(lvlData, playing);
                break;
            case ATTACK:
                if (preRoll) {
                    if (animationIndex >= 3)
                        preRoll = false;
                } else {
                    move(lvlData, playing);
                    checkDmgToPlayer(playing.getPlayer());
                    checkRollOver(playing);
                }
                break;
            case HITTED:
                if (animationIndex <= getSpriteAmount(enemyType, state) - 2)
                    pushBack(pushBackDir, lvlData, 2f);
                updatePushBackDrawOffset();
                tickAfterRollInIdle = 120;

                break;
            }
        }
    }

    private void checkDmgToPlayer(Player player) {
        if (hitbox.intersects(player.getHitbox()))
            if (tickSinceLastDmgToPlayer >= 60) {
                tickSinceLastDmgToPlayer = 0;
                player.changeHealth(-getEnemyDmg(enemyType), this);
            } else
                tickSinceLastDmgToPlayer++;
    }

    private void setWalkDir(Player player) {
        if (player.getHitbox().x > hitbox.x)
            walkDir = RIGHT;
        else
            walkDir = LEFT;

    }

    protected void move(int[][] lvlData, Playing playing) {
        float xSpeed = 0;

        if (walkDir == LEFT)
            xSpeed = -walkSpeed;
        else
            xSpeed = walkSpeed;

        if (state == ATTACK)
            xSpeed *= 2;

        if (canMoveHere(hitbox.x + xSpeed, hitbox.y, hitbox.width, hitbox.height, lvlData))
            if (isFloor(hitbox, xSpeed, lvlData)) {
                hitbox.x += xSpeed;
                return;
            }

        if (state == ATTACK) {
            rollOver(playing);
            rollDurationTick = 0;
        }

        changeWalkDir();

    }

    private void checkRollOver(Playing playing) {
        rollDurationTick++;
        if (rollDurationTick >= rollDuration) {
            rollOver(playing);
            rollDurationTick = 0;
        }
    }

    private void rollOver(Playing playing) {
        newState(IDLE);
        playing.addDialogue((int) hitbox.x, (int) hitbox.y, QUESTION);
    }

}
