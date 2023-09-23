package entities;

import static utils.Constants.PlayerConstants.ATTACK;
import static utils.Constants.PlayerConstants.FALLING;
import static utils.Constants.PlayerConstants.IDLE;
import static utils.Constants.PlayerConstants.JUMPING;
import static utils.Constants.PlayerConstants.RUNNING;
import static utils.Constants.PlayerConstants.getSprintAmount;
import static utils.HelperMethods.canMoveHere;
import static utils.HelperMethods.getEntityXPositionNextToWall;
import static utils.HelperMethods.getEntityYPositionUnderRoofOrAboveFloor;
import static utils.HelperMethods.isEntityOnFloor;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import gamestates.Playing;
import main.Game;
import utils.LoadSave;

public class Player extends Entity {

    private BufferedImage[][] animations;
    private int animationTick;
    private int animationIndex;
    private int animationSpeed = 25;
    private int playerAction = IDLE;
    private boolean left;
    private boolean up;
    private boolean down;
    private boolean right;
    private boolean moving = Boolean.FALSE;
    private boolean attacking = Boolean.FALSE;
    private boolean jump;
    private float playerSpeed = 1.0f * Game.SCALE;
    private int[][] levelData;
    private float xDrawOffset = 21 * Game.SCALE; //padding of char width
    private float yDrawOffset = 4 * Game.SCALE; //padding of char height

    // Jumping/Gravity
    private float airSpeed = 0f;
    private float gravity = 0.04f * Game.SCALE;
    private float jumpSpeed = -2.25f * Game.SCALE;
    private float fallSpeedAfterCollision = 0.5f * Game.SCALE;
    private boolean inAir = Boolean.FALSE;

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

    private int maxHealth = 100;
    private int currentHealth = maxHealth;
    private int healthWidth = healthBarWidth;

    // AttackBox
    private Rectangle2D.Float attackBox;

    private int flipX = 0;
    private int flipW = 1;

    private boolean attackCheck;
    private Playing playing;

    public Player(float x, float y, int width, int height, Playing playing) {
        super(x, y, width, height);
        this.playing = playing;
        loadAnimations();
        initHitbox(x, y, (int)(20*Game.SCALE), (int)(27*Game.SCALE)); //hitbox: 20x27
        initAttackBox();
    }

    private void initAttackBox() {
        attackBox = new Rectangle2D.Float(x, y, (int) (20 * Game.SCALE), (int) (20 * Game.SCALE));

    }

    public void update() {
        updateHealthBar();

        if (currentHealth <= 0) {
            playing.setGameOver(Boolean.TRUE);
            return;
        }

        updateAttackBox();

        updatePosition();
        if (attacking)
            checkAttack();
        updateAnimationTick();
        setAnimation();
    }

    private void checkAttack() {
        if (attackCheck || animationIndex != 1)
            return;
        attackCheck = Boolean.TRUE;
        playing.checkEnemyHit(attackBox);
    }

    private void updateAttackBox() {
        if (right) {
            attackBox.x = hitbox.x + hitbox.width + (int) (Game.SCALE * 10);
        } else if (left) {
            attackBox.x = hitbox.x - hitbox.width - (int) (Game.SCALE * 10);
        }
        attackBox.y = hitbox.y + (int) (Game.SCALE * 10);
    }

    private void updateHealthBar() {
        // % health * health width in pixels
        healthWidth = (int) (currentHealth / (float)maxHealth * healthBarWidth);

    }

    public void render(Graphics g, int xLvlOffset) {
        g.drawImage(animations[playerAction][animationIndex],
                (int) (hitbox.x - xDrawOffset) - xLvlOffset + flipX,
                (int) (hitbox.y - yDrawOffset),
                width * flipW,
                height, null);
//        drawHitbox(g, xLvlOffset);
//        drawAttackBox(g, xLvlOffset);
        drawUI(g);
    }

    private void drawAttackBox(Graphics g, int xLvlOffset) {
        g.setColor(Color.RED);
        g.drawRect((int) attackBox.x - xLvlOffset, (int) attackBox.y, (int) attackBox.width, (int) attackBox.height);

    }

    private void drawUI(Graphics g) {
        g.drawImage(statusBarImg, statusBarX, statusBarY, statusBarWidth, statusBarHeight, null);
        g.setColor(Color.RED);
        g.fillRect(healthBarXStart + statusBarX, healthBarYStart + statusBarY, healthWidth, healthBarHeight);
    }

    private void updateAnimationTick() {
        animationTick++;
        if (animationTick >= animationSpeed) {
            animationTick = 0;
            animationIndex++;
            if (animationIndex >= getSprintAmount(playerAction)) {
                animationIndex = 0;
                attacking = Boolean.FALSE;
                attackCheck = Boolean.FALSE;
            }
        }

    }

    private void setAnimation() {

        int startAnimation = playerAction;

        if (moving) {
            playerAction = RUNNING;
        } else {
            playerAction = IDLE;
        }

        if (inAir) {
            if (airSpeed < 0) // going up
                playerAction = JUMPING;
            else
                playerAction = FALLING;
        }

        if (attacking) {
            playerAction = ATTACK;
            if (startAnimation != ATTACK) {
                animationIndex = 1;
                animationTick = 0;
                return;
            }
        }

        if (startAnimation != playerAction) {
            resetAnimationTick();
        }
    }

    private void resetAnimationTick() {
        animationTick = 0;
        animationIndex = 0;
    }

    private void updatePosition() {
        moving = false;

        if (jump) {
            jump();
        }

        if (!inAir)
            if ((!left && !right) || (left && right))
                return;

        float xSpeed = 0;

        if (left) {
            xSpeed -= playerSpeed;
            flipX = width;
            flipW = -1;
        }
        if (right) {
            xSpeed += playerSpeed;
            flipX = 0;
            flipW = 1;
        }

        if (!inAir) {
            if (!isEntityOnFloor(hitbox, levelData)) {
                inAir = true;

            }
        }

        if (inAir) {
            if (canMoveHere(hitbox.x, hitbox.y + airSpeed, hitbox.width, hitbox.height, levelData)) {
                hitbox.y += airSpeed;
                airSpeed += gravity;
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
        } else {
            updateXPosition(xSpeed);
        }

        moving = true;

    }


    private void jump() {
        if (inAir) {
            return;
        }
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
          }
    }

    public void changeHealth(int value) {
        currentHealth += value;
        if (currentHealth < 0) {
            currentHealth = 0;
            // Game Over();
        } else if (currentHealth > maxHealth) {
            currentHealth = maxHealth;
        }
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
        if (!isEntityOnFloor(hitbox, levelData)) {
            inAir = Boolean.TRUE;
        }
    }

    public void resetDirBooleans() {
        left = Boolean.FALSE;
        right = Boolean.FALSE;
        up = Boolean.FALSE;
        down = Boolean.FALSE;
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

    public boolean isUp() {
        return up;
    }

    public void setUp(boolean up) {
        this.up = up;
    }

    public boolean isDown() {
        return down;
    }

    public void setDown(boolean down) {
        this.down = down;
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
        playerAction = IDLE;
        currentHealth = maxHealth;

        hitbox.x = x;
        hitbox.y = y;

        if (!isEntityOnFloor(hitbox, levelData))
            inAir = true;
    }
}
