package entities;

import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import gamestates.Playing;
import utils.LoadSave;

import static utils.Constants.EnemyConstants.*;

public class EnemyHandler {

    private Playing playing;
    private BufferedImage[][] crabbyArray;
    private ArrayList<Crabby> crabbies = new ArrayList<>();

    public EnemyHandler(Playing playing) {
        this.playing = playing;
        loadEnemyImgs();
        addEnemies();
    }

    private void addEnemies() {
        crabbies = LoadSave.getCrabs();
    }

    public void update(int[][] lvlData, Player player) {
        for (Crabby c : crabbies)
            if (c.isActive())
                c.update(lvlData, player);
    }

    public void draw(Graphics g, int xLvlOffset) {
        drawCrabs(g, xLvlOffset);
    }

    private void drawCrabs(Graphics g, int xLvlOffset) {
        for (Crabby c : crabbies)
            if (c.isActive()) {
                g.drawImage(crabbyArray[c.getEnemyState()][c.getAniIndex()],
                        (int)c.hitbox.x - xLvlOffset - CRABBY_DRAWOFFSET_X + c.flipX(),
                        (int) c.hitbox.y - CRABBY_DRAWOFFSET_Y,
                        CRABBY_WIDTH * c.flipW(),
                        CRABBY_HEIGHT, null);
                //c.drawHitbox(g, xLvlOffset);
//                c.drawAttackBox(g, xLvlOffset);
            }
    }

    public void checkEnemyHit(Rectangle2D.Float attackBox) {
        for(Crabby c : crabbies)
            if (c.isActive()){
                if (attackBox.intersects(c.getHitbox())) {
                    c.hurt(10);
                    return;
                }
            }
    }

    private void loadEnemyImgs() {
        crabbyArray = new BufferedImage[5][9];
        BufferedImage temp = LoadSave.getSpriteAtlas(LoadSave.CRABBY_SPRITE);
        for (int j = 0; j < crabbyArray.length; j++)
            for (int i = 0; i < crabbyArray[j].length; i++)
                crabbyArray[j][i] = temp.getSubimage(i * CRABBY_WIDTH_DEFAULT, j * CRABBY_HEIGHT_DEFAULT, CRABBY_WIDTH_DEFAULT, CRABBY_HEIGHT_DEFAULT);
    }

    public void resetAllEnemies() {
        for (Crabby c : crabbies) {
            c.resetEnemy();
        }

    }

}
