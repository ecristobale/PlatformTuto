package objects;

import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import gamestates.Playing;
import levels.Level;
import utils.LoadSave;

import static utils.Constants.ObjectConstants.*;

public class ObjectHandler {

    private Playing playing;
    private BufferedImage[][] potionImgs;
    private BufferedImage[][] containerImgs;
    private ArrayList<Potion> potions;
    private ArrayList<GameContainer> containers; //19:18

    public ObjectHandler(Playing playing) {
        this.playing = playing;
        loadImgs();
    }

    public void checkObjectTouched(Rectangle2D.Float hitbox) {
        for (Potion p : potions)
            if (p.isActive()) {
                if (hitbox.intersects(p.getHitbox())) {
                    p.setActive(Boolean.FALSE);
                    applyEffectToPlayer(p);
                }
            }
    }

    public void applyEffectToPlayer(Potion p) {
        if (p.getObjectType() == RED_POTION)
            playing.getPlayer().changeHealth(RED_POTION_VALUE);
        else
            playing.getPlayer().changePower(BLUE_POTION_VALUE);
    }

    public void checkObjectHit(Rectangle2D.Float attackBox) {
        for (GameContainer gc : containers)
            if (gc.isActive() && !gc.doAnimation)
                if (gc.getHitbox().intersects(attackBox)) {
                    gc.setAnimation(Boolean.TRUE);
                    int type = 0;
                    if (gc.getObjectType() == BARREL)
                        type = 1;
                    potions.add(new Potion((int) (gc.getHitbox().x + gc.getHitbox().width / 2),
                            (int) (gc.getHitbox().y - gc.getHitbox().height / 2),
                            type));
                    return;
                }
    }

    public void loadObjects(Level newLevel) {
        potions = new ArrayList<>(newLevel.getPotionList());
        containers = new ArrayList<>(newLevel.getContainerList());

    }

    private void loadImgs() {
        BufferedImage potionSprite = LoadSave.getSpriteAtlas(LoadSave.POTION_ATLAS);
        potionImgs = new BufferedImage[2][7];

        for(int j = 0; j < potionImgs.length; j++)
            for(int i = 0; i < potionImgs[j].length; i++)
                potionImgs[j][i] = potionSprite.getSubimage(12 * i, 16 * j, 12, 16);

        BufferedImage containerSprite = LoadSave.getSpriteAtlas(LoadSave.CONTAINER_ATLAS);
        containerImgs = new BufferedImage[2][8];

        for(int j = 0; j < containerImgs.length; j++)
            for(int i = 0; i < containerImgs[j].length; i++)
                containerImgs[j][i] = containerSprite.getSubimage(40 * i, 30 * j, 40, 30);
    }

    public void update() {
        for(Potion p : potions)
            if (p.isActive())
                p.update();

        for(GameContainer gc : containers)
            if (gc.isActive())
                gc.update();
    }

    public void draw(Graphics g, int xLvlOffset) {
        drawPotions(g, xLvlOffset);
        drawContainers(g, xLvlOffset);
    }

    private void drawContainers(Graphics g, int xLvlOffset) {
        for(GameContainer gc : containers)
            if (gc.isActive()) {
                int type = 0;
                if (gc.getObjectType() == BARREL) {
                    type = 1;
                }
                g.drawImage(containerImgs[type][gc.getAniIndex()],
                        (int) (gc.getHitbox().x -gc.getxDrawOffset() - xLvlOffset),
                        (int) (gc.getHitbox().y -gc.getyDrawOffset()),
                        CONTAINER_WIDTH,
                        CONTAINER_HEIGHT,
                        null);
            }
    }

    private void drawPotions(Graphics g, int xLvlOffset) {
        for(Potion p : potions)
            if (p.isActive()) {
                int type = 0;
                if (p.getObjectType() == RED_POTION) {
                    type = 1;
                }
                g.drawImage(potionImgs[type][p.getAniIndex()],
                        (int) (p.getHitbox().x -p.getxDrawOffset() - xLvlOffset),
                        (int) (p.getHitbox().y -p.getyDrawOffset()),
                        POTION_WIDTH,
                        POTION_HEIGHT,
                        null);
            }

    }

    public void resetAllObjects() {
        loadObjects(playing.getLevelHandler().getCurrentLevel()); // prevent lists to becoming higher at restart lvl

        for(Potion p : potions)
            p.reset();

        for(GameContainer gc : containers)
            gc.reset();
    }
}
