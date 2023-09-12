package entities;

import static utils.Constants.PlayerConstants.*;
import static utils.HelperMethods.canMoveHere;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import main.Game;
import utils.LoadSave;

public class Player extends Entity {

    private BufferedImage[][] animations;
    private int animationTick = 25;
    private int animationIndex = 25;
    private int animationSpeed = 25;
    private int playerAction = IDLE;
    private boolean left;
    private boolean up;
    private boolean down;
    private boolean right;
    private boolean moving = Boolean.FALSE;
    private boolean attacking = Boolean.FALSE;
    private float playerSpeed = 2.0f;
    private int[][] levelData;
    private float xDrawOffset = 21 * Game.SCALE; //padding of char width
    private float yDrawOffset = 4 * Game.SCALE; //padding of char height

    public Player(float x, float y, int width, int height) {
        super(x, y, width, height);
        loadAnimations();
        initialiseHitbox(x, y, 20*Game.SCALE, 28*Game.SCALE); //hitbox: 20x28
    }

    public void update() {
        updatePosition();
        updateAnimationTick();
        setAnimation();
    }

    public void render(Graphics g) {
        g.drawImage(animations[playerAction][animationIndex], (int) (hitbox.x - xDrawOffset), (int) (hitbox.y - yDrawOffset), width, height, null);
        drawHitbox(g);
    }

    private void updateAnimationTick() {
        animationTick++;
        if (animationTick >= animationSpeed) {
            animationTick = 0;
            animationIndex++;
            if (animationIndex >= getSprintAmount(playerAction)) {
                animationIndex = 0;
                attacking = false;
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

        if (attacking) {
            playerAction = ATTACK_1;
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
        if(!left && !right && !up && !down) {
            return;
        }

        float xSpeed = 0;
        float ySpeed = 0;

        if (left && !right)
            xSpeed = -playerSpeed;
        else if (right && !left)
            xSpeed = playerSpeed;

        if (up && !down)
            ySpeed = -playerSpeed;
        else if (down && !up)
            ySpeed = playerSpeed;

//        if (canMoveHere(x+xSpeed, y+ySpeed, width, height, levelData)) {
//            this.x += xSpeed;
//            this.y += ySpeed;
//            moving = Boolean.TRUE;
//        }

          if (canMoveHere(hitbox.x + xSpeed, hitbox.y + ySpeed, hitbox.width, hitbox.height, levelData)) {
              hitbox.x += xSpeed;
              hitbox.y += ySpeed;
              moving = Boolean.TRUE;
          }

    }


    private void loadAnimations() {

        BufferedImage image = LoadSave.getSpriteAtlas(LoadSave.PLAYER_ATLAS);
        animations = new BufferedImage[9][6];

        for (int j = 0; j < animations.length; j++) {
            for (int i = 0; i < animations[j].length; i++) {
                animations[j][i] = image.getSubimage(i*64, j*40, 64, 40);
            }
        }
    }

    public void loadLevelData(int[][] levelData) {
        this.levelData = levelData;
    }

    public void resetDirectionBooleans() {
        left = false;
        right = false;
        up = false;
        down = false;
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
}
