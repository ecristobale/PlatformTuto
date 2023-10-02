package entities;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;

import main.Game;

public abstract class Entity {

    protected float x;
    protected float y;
    protected int width;
    protected int height;
    protected int animationTick;
    protected int animationIndex;
    protected int state;
    protected float airSpeed = 0f;
    protected boolean inAir = Boolean.FALSE;
    protected int maxHealth;
    protected int currentHealth;
    protected Rectangle2D.Float attackBox;
    protected float walkSpeed = 1.0f * Game.SCALE;


    protected Rectangle2D.Float hitbox;

    public Entity(float x, float y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void drawAttackBox(Graphics g, int xLvlOffset) {
        g.setColor(Color.RED);
        g.drawRect((int) (attackBox.x - xLvlOffset), (int) attackBox.y, (int) attackBox.width, (int) attackBox.height);
    }

    protected void drawHitbox(Graphics g, int xLvlOffset) {
        // debugging hitbox
        g.setColor(Color.RED);
        g.drawRect((int) hitbox.x - xLvlOffset, (int) hitbox.y, (int) hitbox.width, (int) hitbox.height);
    }

    protected void initHitbox(int width, int height) {
        hitbox = new Rectangle2D.Float(x, y, (int) (width * Game.SCALE), (int) (height * Game.SCALE));
    }

    public Rectangle2D.Float getHitbox() {
        return hitbox;
    }

    public int getState() {
        return state;
    }

    public int getAnimationIndex() {
        return animationIndex;
    }

    public int getCurrentHealth() {
        return currentHealth;
    }
}
