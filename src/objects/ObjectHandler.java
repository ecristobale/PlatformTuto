package objects;

import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import entities.Player;
import gamestates.Playing;
import levels.Level;
import main.Game;
import utils.LoadSave;

import static utils.Constants.ObjectConstants.*;
import static utils.HelperMethods.canCannonSeePlayer;
import static utils.HelperMethods.isProjectileHittingLevel;
import static utils.Constants.Projectiles.*;

public class ObjectHandler {

    private Playing playing;
    private BufferedImage[][] potionImgs;
    private BufferedImage[][] containerImgs;
    private BufferedImage[] cannonImgs;
    private BufferedImage cannonBallImg;
    private BufferedImage spikeImg;
    private ArrayList<Potion> potions;
    private ArrayList<GameContainer> containers;
    private ArrayList<Spike> spikes;
    private ArrayList<Cannon> cannons;
    private ArrayList<Projectile> projectiles = new ArrayList<>();

    public ObjectHandler(Playing playing) {
        this.playing = playing;
        loadImgs();
    }

    public void checkSpikesTouched(Player p) {
        for(Spike s : spikes)
            if (s.getHitbox().intersects(p.getHitbox()))
                p.kill();
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
        spikes = new ArrayList<>(newLevel.getSpikeList());
        cannons = new ArrayList<>(newLevel.getCannonList());
        projectiles.clear();
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

        spikeImg = LoadSave.getSpriteAtlas(LoadSave.TRAP_ATLAS);

        cannonImgs = new BufferedImage[7];
        BufferedImage temp = LoadSave.getSpriteAtlas(LoadSave.CANNON_ATLAS);
        for(int i = 0; i < cannonImgs.length; i++)
            cannonImgs[i] = temp.getSubimage(i * 40, 0, 40, 26);

        cannonBallImg = LoadSave.getSpriteAtlas(LoadSave.CANNON_BALL);
    }

    public void update(int[][] lvlData, Player player) {
        for(Potion p : potions)
            if (p.isActive())
                p.update();

        for(GameContainer gc : containers)
            if (gc.isActive())
                gc.update();

        updateCannons(lvlData, player);
        updateProjectiles(lvlData, player);
    }

    private void updateProjectiles(int[][] lvlData, Player player) {
        for(Projectile p : projectiles) {
            if (p.isActive()) {
                p.updatePos();

                if (p.getHibox().intersects(player.getHitbox())) {
                    player.changeHealth(-25);
                    p.setActive(Boolean.FALSE);
                } else if (isProjectileHittingLevel(p, lvlData))
                    p.setActive(Boolean.FALSE);
            }
        }
    }

    private boolean isPlayerInRange(Cannon c, Player player) {
        int absValue = (int) Math.abs(player.getHitbox().x - c.getHitbox().x);
        return absValue <= 5 * Game.TILES_SIZE;
    }

    private boolean isPlayerInFrontOfCannon(Cannon c, Player player) {
        if (c.getObjectType() == CANNON_LEFT)
            return (c.getHitbox().x > player.getHitbox().x);
        else // CANNON_RIGHT
            return (c.getHitbox().x < player.getHitbox().x);
    }


    /* if the cannon is not animating
     * Same tileY
     * if player is in range
     * is player in front
     * line of shine
     * fire the cannon
     */
    private void updateCannons(int[][] lvlData, Player player) {
        for(Cannon c : cannons) {
            if (!c.doAnimation)
                if (c.getTileY() == player.getTileY())
                    if (isPlayerInRange(c, player))
                        if (isPlayerInFrontOfCannon(c, player))
                            if (canCannonSeePlayer(lvlData, player.getHitbox(), c.getHitbox(), c.getTileY()))
                                c.setAnimation(Boolean.TRUE);

            c.update();
            if (c.getAniIndex() == 4 && c.getAniTick() == 0)
                shootCannon(c);
        }
    }

    private void shootCannon(Cannon c) {
        int dir = 1;
        if (c.getObjectType() == CANNON_LEFT)
            dir = -1;

        projectiles.add(new Projectile((int) c.getHitbox().x, (int) c.getHitbox().y, dir));
    }

    public void draw(Graphics g, int xLvlOffset) {
        drawPotions(g, xLvlOffset);
        drawContainers(g, xLvlOffset);
        drawTraps(g, xLvlOffset);
        drawCannons(g, xLvlOffset);
        drawProjectiles(g, xLvlOffset);
    }

    private void drawProjectiles(Graphics g, int xLvlOffset) {
        for(Projectile p : projectiles)
            if (p.isActive())
                g.drawImage(cannonBallImg, (int) (p.getHibox().x - xLvlOffset), (int) p.getHibox().y, CANNON_BALL_WIDTH, CANNON_BALL_HEIGHT, null);

    }

    private void drawCannons(Graphics g, int xLvlOffset) {
        for(Cannon c : cannons) {
            int x = (int) (c.getHitbox().x - xLvlOffset);
            int width = CANNON_WIDTH;

            if (c.getObjectType() == CANNON_RIGHT) {
                x += width;
                width *= -1;
            }
            g.drawImage(cannonImgs[c.getAniIndex()], x, (int) (c.getHitbox().y), width, CANNON_HEIGHT, null);
        }

    }

    private void drawTraps(Graphics g, int xLvlOffset) {
        for(Spike s : spikes)
            g.drawImage(spikeImg, (int) (s.getHitbox().x - xLvlOffset), (int) (s.getHitbox().y - s.getyDrawOffset()), SPIKE_WIDTH, SPIKE_HEIGHT, null);

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

        for(Cannon c : cannons)
            c.reset();
    }
}
