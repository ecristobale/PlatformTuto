package levels;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Iterator;

import main.Game;
import utils.LoadSave;


public class LevelHandler {

    private Game game;
    private BufferedImage[] levelSprite;
    private Level level1;

    public LevelHandler(Game game) {
        this.game = game;
//        levelSprite = LoadSave.getSpriteAtlas(LoadSave.LEVEL_ATLAS);
        importOutsideSprites();
        level1 = new Level(LoadSave.getLevelData());
    }

    private void importOutsideSprites() {
        // Sprite level atlas: 48 = 4 HEIGHT * 12 WIDTH
        BufferedImage img = LoadSave.getSpriteAtlas(LoadSave.LEVEL_ATLAS);
        levelSprite = new BufferedImage[48];
        for (int j = 0; j < 4; j++) {
            for (int i = 0; i < 12; i++) {
                int index = j * 12 + i;
                levelSprite[index] = img.getSubimage(i * 32, j * 32, 32, 32);
            }
        }

    }

    public void draw(Graphics g) {
        for (int j = 0; j < Game.TILES_IN_HEIGHT; j++) {
            for (int i = 0; i < Game.TILES_IN_WIDTH; i++) {
                int index = level1.getSpriteData(i, j);
                g.drawImage(levelSprite[index], Game.TILES_SIZE*i, Game.TILES_SIZE*j, Game.TILES_SIZE, Game.TILES_SIZE, null);
            }
        }
    }

    public void update() {

    }

    public Level getCurrentLevel() {
        return level1;
    }

}
