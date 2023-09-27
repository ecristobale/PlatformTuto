package objects;

import static utils.Constants.ANIMATION_SPEED;
import static utils.Constants.ObjectConstants.*;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;

import main.Game;

public class GameObject {

    protected int x;
    protected int y;
    protected int objectType;
    protected Rectangle2D.Float hitbox;
    protected boolean doAnimation = Boolean.FALSE;
    protected boolean active = Boolean.TRUE;
    protected int aniTick;
    protected int aniIndex = 0;
    protected int xDrawOffset;
    protected int yDrawOffset;

    public GameObject(int x, int y, int objectType) {
        this.x = x;
        this.y = y;
        this.objectType = objectType;
    }

    protected void updateAnimationTick() {
        aniTick++;
        if (aniTick >= ANIMATION_SPEED) {
            aniTick = 0;
            aniIndex++;
            if (aniIndex >= getSpriteAmount(objectType)) {
                aniIndex = 0;
                if (objectType == BARREL || objectType == BOX) {
                    doAnimation = Boolean.FALSE;
                    active = Boolean.FALSE;
                }
            }
        }
    }

    public void reset() {
        aniIndex = 0;
        aniTick = 0;
        active = Boolean.TRUE;

        if (objectType == BARREL || objectType == BOX)
            doAnimation = Boolean.FALSE;
        else
            doAnimation = Boolean.TRUE;
    }

    protected void initHitbox(int width, int height) {
        hitbox = new Rectangle2D.Float(x, y, (int) (width * Game.SCALE), (int) (height * Game.SCALE));
    }

    public void drawHitbox(Graphics g, int xLvlOffset) {
        // debugging hitbox
        g.setColor(Color.RED);
        g.drawRect((int) hitbox.x - xLvlOffset, (int) hitbox.y, (int) hitbox.width, (int) hitbox.height);
    }

    public int getObjectType() {
        return objectType;
    }

    public Rectangle2D.Float getHitbox() {
        return hitbox;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setAnimation(boolean doAnimation) {
        this.doAnimation = doAnimation;
    }

    public int getxDrawOffset() {
        return xDrawOffset;
    }

    public int getyDrawOffset() {
        return yDrawOffset;
    }

    public int getAniIndex() {
        return aniIndex;
    }
}
