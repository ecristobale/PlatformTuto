package entities;

import static utils.Constants.PlayerConstants.*;
import static utils.HelperMethods.*;
import static utils.Constants.*;
import static utils.Constants.Directions.*;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import audio.AudioPlayer;
import gamestates.Playing;
import main.Game;
import utils.LoadSave;

public class Player extends Entity {

    private BufferedImage[][] animations;
    private boolean left;
    private boolean right;
    private boolean moving = Boolean.FALSE;
    private boolean attacking = Boolean.FALSE;
    private boolean jump;
    private int[][] levelData;
    private float xDrawOffset = 21 * Game.SCALE; //padding of character width
    private float yDrawOffset = 4 * Game.SCALE; //padding of character height

    // Jumping/Gravity
    private float jumpSpeed = -2.25f * Game.SCALE;
    private float fallSpeedAfterCollision = 0.5f * Game.SCALE;

    // StatusBarUI
    private BufferedImage statusBarImg;

    private int statusBarWidth = (int) (192 * Game.SCALE);
    private int statusBarHeight = (int) (58 * Game.SCALE);
    private int statusBarX = (int) (10 * Game.SCALE);
    private int statusBarY = (int) (10 * Game.SCALE);

    private int healthBarWidth = (int) (150 * Game.SCALE);
    private int healthBarHeight = (int) (4 * Game.SCALE);
    private int healthBarXStart = (int) (34 * Game.SCALE);
    private int healthBarYStart = (int) (14 * Game.SCALE);
    private int healthWidth = healthBarWidth;

    private int powerBarWidth = (int) (104 * Game.SCALE);
    private int powerBarHeight = (int) (2 * Game.SCALE);
    private int powerBarXStart = (int) (44 * Game.SCALE);
    private int powerBarYStart = (int) (34 * Game.SCALE);
    private int powerWidth = powerBarWidth;
    private int maxPower = 200;
    private int currentPower = maxPower;

    private int flipX = 0;
    private int flipW = 1;

    private boolean attackCheck;
    private Playing playing;

    private int tileY = 0;

    private boolean powerAttackActive = Boolean.FALSE;
    private int powerAttackTick = 0;
    private int powerGrowSpeed = 15;
    private int powerGrowTick = 0;

    public Player(float x, float y, int width, int height, Playing playing) {
        super(x, y, width, height);
        this.playing = playing;
        this.state = IDLE;
        this.maxHealth = 100;
        this.currentHealth = maxHealth;
        this.walkSpeed = 1.0f * Game.SCALE;
        loadAnimations();
        initHitbox(20, 27); //hitbox: 20x27
        initAttackBox();
    }

    public void setSpawn(Point spawn) {
        x = spawn.x;
        y = spawn.y;
        hitbox.x = x;
        hitbox.y = y;
    }

    private void initAttackBox() {
        attackBox = new Rectangle2D.Float(x, y, (int) (20 * Game.SCALE), (int) (20 * Game.SCALE));
        resetAttackBox();
    }

    public void update() {
        updateHealthBar();
        updatePowerBar();

        if (currentHealth <= 0) {
            if (state != DEAD) {
                state = DEAD;
                animationTick = 0;
                animationIndex = 0;
                playing.setPlayerDying(Boolean.TRUE);
                playing.getGame().getAudioPlayer().playEffect(AudioPlayer.DIE);
                // if player died in air
                if (!isEntityOnFloor(hitbox, levelData)) {
                    inAir = true;
                    airSpeed = 0;
                }
            } else if (animationIndex == getSpriteAmount(DEAD) - 1 && animationTick >= ANIMATION_SPEED - 1) {
                playing.setGameOver(Boolean.TRUE);
                playing.getGame().getAudioPlayer().stopSong();
                playing.getGame().getAudioPlayer().playEffect(AudioPlayer.GAMEOVER);
            } else {
                updateAnimationTick();
                // Fall if in air
                if (inAir)
                    if (canMoveHere(hitbox.x, hitbox.y + airSpeed, hitbox.width, hitbox.height, levelData)) {
                        hitbox.y += airSpeed;
                        airSpeed += GRAVITY;
                    } else
                        inAir = false;
            }

            return;
        }

        updateAttackBox();

        if (state == GETTING_HIT) {
            if (animationIndex <= getSpriteAmount(state) - 3)
                pushBack(pushBackDir, levelData, 1.25f);
            updatePushBackDrawOffset();
        } else
            updatePosition();

        if (moving) {
            checkPotionTouched();
            checkSpikesTouched();
            checkInsideWater();
            tileY = (int) (hitbox.y / Game.TILES_SIZE);
            if (powerAttackActive) {
                powerAttackTick++;
                if (powerAttackTick >= 35) {
                    powerAttackTick = 0;
                    powerAttackActive = Boolean.FALSE;
                }
            }
        }
        if (attacking || powerAttackActive)
            checkAttack();

        updateAnimationTick();
        setAnimation();
    }

    private void checkInsideWater() {
        if (isEntityInWater(hitbox, playing.getLevelHandler().getCurrentLevel().getLevelData()))
            currentHealth = 0;
    }

    private void checkSpikesTouched() {
        playing.checkSpikesTouched(this);
    }

    private void checkPotionTouched() {
        playing.checkPotionTouched(hitbox);

    }

    private void checkAttack() {
        if (attackCheck || animationIndex != 1)
            return;
        attackCheck = Boolean.TRUE;

        if (powerAttackActive)
            attackCheck = Boolean.FALSE;

        playing.checkEnemyHit(attackBox);
        playing.checkObjectHit(attackBox);
        playing.getGame().getAudioPlayer().playAttackSound();
    }

    private void setAttackBoxOnRightSide() {
        attackBox.x = hitbox.x + hitbox.width - (int) (Game.SCALE * 5);
    }

    private void setAttackBoxOnLeftSide() {
        attackBox.x = hitbox.x - hitbox.width - (int) (Game.SCALE * 10);
    }

    private void updateAttackBox() {
        if (right && left) {
            if (flipW == 1)
                setAttackBoxOnRightSide();
            else
                setAttackBoxOnLeftSide();
        } else if (right || (powerAttackActive && flipW == 1))
            setAttackBoxOnRightSide();
        else if (left || (powerAttackActive && flipW == -1))
            setAttackBoxOnLeftSide();

        attackBox.y = hitbox.y + (int) (10 * Game.SCALE);
    }

    private void updatePowerBar() {
        // % health * health width in pixels
        powerWidth = (int) (currentPower / (float) maxPower * powerBarWidth);

        powerGrowTick++;
        if (powerGrowTick >= powerGrowSpeed) {
            powerGrowTick = 0;
            changePower(1);
        }
    }

    private void updateHealthBar() {
        // % health * health width in pixels
        healthWidth = (int) (currentHealth / (float) maxHealth * healthBarWidth);

    }

    public void render(Graphics g, int xLvlOffset) {
        g.drawImage(animations[state][animationIndex], (int) (hitbox.x - xDrawOffset) - xLvlOffset + flipX, (int) (hitbox.y - yDrawOffset + (int) (pushDrawOffset)), width * flipW, height, null);
//        drawHitbox(g, xLvlOffset);
//        drawAttackBox(g, xLvlOffset);
        drawUI(g);
    }

    private void drawUI(Graphics g) {
        // Background UI
        g.drawImage(statusBarImg, statusBarX, statusBarY, statusBarWidth, statusBarHeight, null);

        // Health bar
        g.setColor(Color.RED);
        g.fillRect(healthBarXStart + statusBarX, healthBarYStart + statusBarY, healthWidth, healthBarHeight);

        // Power bar
        g.setColor(Color.YELLOW);
        g.fillRect(powerBarXStart + statusBarX, powerBarYStart + statusBarY, powerWidth, powerBarHeight);
    }

    private void updateAnimationTick() {
        animationTick++;
        if (animationTick >= ANIMATION_SPEED) {
            animationTick = 0;
            animationIndex++;
            if (animationIndex >= getSpriteAmount(state)) {
                animationIndex = 0;
                attacking = Boolean.FALSE;
                attackCheck = Boolean.FALSE;
                if (state == GETTING_HIT) {
                    newState(IDLE);
                    airSpeed = 0f;
                    if (!isFloor(hitbox, 0, levelData))
                        inAir = Boolean.TRUE;
                }
            }
        }

    }

    private void setAnimation() {

        int startAnimation = state;

        if (state == GETTING_HIT)
            return;

        if (moving)
            state = RUNNING;
        else
            state = IDLE;

        if (inAir) {
            if (airSpeed < 0) // going up
                state = JUMPING;
            else
                state = FALLING;
        }

        if (powerAttackActive) {
            state = ATTACK;
            animationIndex = 1;
            animationTick = 0;
            return;
        }

        if (attacking) {
            state = ATTACK;
            if (startAnimation != ATTACK) {
                animationIndex = 1;
                animationTick = 0;
                return;
            }
        }

        if (startAnimation != state) {
            resetAnimationTick();
        }
    }

    private void resetAnimationTick() {
        animationTick = 0;
        animationIndex = 0;
    }

    private void updatePosition() {
        moving = Boolean.FALSE;

        if (jump) {
            jump();
        }

        if (!inAir)
            if (!powerAttackActive)
                if ((!left && !right) || (left && right))
                    return;

        float xSpeed = 0;

        if (left && !right) {
            xSpeed -= walkSpeed;
            flipX = width;
            flipW = -1;
        }
        if (right && !left) {
            xSpeed += walkSpeed;
            flipX = 0;
            flipW = 1;
        }

        if (powerAttackActive) {
            if ((!left && !right) || (left && right)) {
                if (flipW == -1)
                    xSpeed = -walkSpeed;
                else
                    xSpeed = walkSpeed;
            }
            xSpeed *=3;
        }

        if (!inAir) {
            if (!isEntityOnFloor(hitbox, levelData)) {
                inAir = true;

            }
        }

        if (inAir && !powerAttackActive) {
            if (canMoveHere(hitbox.x, hitbox.y + airSpeed, hitbox.width, hitbox.height, levelData)) {
                hitbox.y += airSpeed;
                airSpeed += GRAVITY;
                updateXPosition(xSpeed);
            } else {
                hitbox.y = getEntityYPositionUnderRoofOrAboveFloor(hitbox, airSpeed);
                if (airSpeed > 0) {
                    // Above floor
                    resetInAir();
                }
                else {
                    airSpeed = fallSpeedAfterCollision;
                }
                updateXPosition(xSpeed);
            }
        } else
            updateXPosition(xSpeed);

        moving = true;
    }


    private void jump() {
        if (inAir) {
            return;
        }
        playing.getGame().getAudioPlayer().playEffect(AudioPlayer.JUMP);
        inAir = Boolean.TRUE;
        airSpeed = jumpSpeed;
    }

    private void resetInAir() {
        inAir = Boolean.FALSE;
        airSpeed = 0;
    }

    private void updateXPosition(float xSpeed) {
          if (canMoveHere(hitbox.x + xSpeed, hitbox.y, hitbox.width, hitbox.height, levelData)) {
              hitbox.x += xSpeed;
          } else {
              hitbox.x = getEntityXPositionNextToWall(hitbox, xSpeed);
              if (powerAttackActive) {
                  powerAttackActive = Boolean.FALSE;
                  powerAttackTick = 0;
              }
          }
    }

    public void changeHealth(int value) {
        if (value < 0) {
            if (state == GETTING_HIT)
                return;
            else
                newState(GETTING_HIT);
        }
        currentHealth += value;
        currentHealth = Math.max(Math.min(currentHealth, maxHealth), 0);
    }

    public void changeHealth(int value, Enemy e) {
        if (state == GETTING_HIT)
            return;
        changeHealth(value);
        pushBackOffsetDir = UP;
        pushDrawOffset = 0;

        if (e.getHitbox().x < hitbox.x)
            pushBackDir = RIGHT;
        else
            pushBackDir = LEFT;
    }

    public void kill() {
        currentHealth = 0;

    }

    public void changePower(int value) {
        currentPower += value;
        currentPower = Math.max(Math.min(currentPower, maxPower), 0);
    }

    private void loadAnimations() {

        BufferedImage img = LoadSave.getSpriteAtlas(LoadSave.PLAYER_ATLAS);
        animations = new BufferedImage[7][8];

        for (int j = 0; j < animations.length; j++)
            for (int i = 0; i < animations[j].length; i++)
                animations[j][i] = img.getSubimage(i*64, j*40, 64, 40);

        statusBarImg = LoadSave.getSpriteAtlas(LoadSave.STATUS_BAR);
    }

    public void loadLevelData(int[][] levelData) {
        this.levelData = levelData;
        if (!isEntityOnFloor(hitbox, levelData))
            inAir = Boolean.TRUE;
    }

    public void resetDirBooleans() {
        left = Boolean.FALSE;
        right = Boolean.FALSE;
    }

    public void setAttack(boolean attacking) {
        this.attacking = attacking;
    }

    public boolean isLeft() {
        return left;
    }

    public void setLeft(boolean left) {
        this.left = left;
    }

    public boolean isRight() {
        return right;
    }

    public void setRight(boolean right) {
        this.right = right;
    }

    public void setJump(Boolean jump) {
        this.jump = jump;
    }

    public void resetAll() {
        resetDirBooleans();
        inAir = Boolean.FALSE;
        attacking = Boolean.FALSE;
        moving = Boolean.FALSE;
        airSpeed = 0f;
        state = IDLE;
        currentHealth = maxHealth;
        powerAttackActive = false;
        powerAttackTick = 0;
        currentPower = maxPower;

        hitbox.x = x;
        hitbox.y = y;
        resetAttackBox();

        if (!isEntityOnFloor(hitbox, levelData))
            inAir = true;
    }

    private void resetAttackBox() {
        if (flipW == 1)
            setAttackBoxOnRightSide();
        else
            setAttackBoxOnLeftSide();
    }

    public int getTileY() {
        return tileY;
    }

    public void powerAttack() {
        if (powerAttackActive)
            return;
        if (currentPower >= 60) {
            powerAttackActive = Boolean.TRUE;
            changePower(-60);
        }

    }
}
