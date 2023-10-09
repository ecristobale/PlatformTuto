package effects;

import java.awt.Graphics;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.Random;

import main.Game;
import utils.LoadSave;

public class Rain {

    private Point2D.Float[] drops;
    private Random rand;
    private float rainSpeed = 1.25f;
    private BufferedImage rainParticle;

    // Disable if the game lags, a lot of resources doing it in this way
    public Rain() {
        rand = new Random();
        drops = new Point2D.Float[1000];
        rainParticle = LoadSave.getSpriteAtlas(LoadSave.RAIN_PARTICLE);
        initDrops();
    }

    private void initDrops() {
        for (int i = 0; i < drops.length; i++)
            drops[i] = getRndPos();
    }

    private Point2D.Float getRndPos() {
        return new Point2D.Float((int) getNewX(0), rand.nextInt(Game.GAME_HEIGHT));
    }

    public void update(int xLvlOffset) {
        for (Point2D.Float p : drops) {
            p.y += rainSpeed;
            if (p.y >= Game.GAME_HEIGHT) {
                p.y = -20;
                p.x = getNewX(xLvlOffset);
            }
        }
    }

    private float getNewX(int xLvlOffset) {
        float value = (-Game.GAME_WIDTH) + rand.nextInt((int) (3f * Game.GAME_WIDTH)) + xLvlOffset;
        return value;
    }

    public void draw(Graphics g, int xLvlOffset) {
        for (Point2D.Float p : drops)
            g.drawImage(rainParticle, (int) p.getX() - xLvlOffset, (int) p.getY(), 3, 12, null);
    }

}
