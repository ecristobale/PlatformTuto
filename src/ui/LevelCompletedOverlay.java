package ui;

import static utils.Constants.UI.UrmButtons.*;

import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import gamestates.GameState;
import gamestates.Playing;
import main.Game;
import utils.LoadSave;

public class LevelCompletedOverlay {

    private Playing playing;
    private UrmButton menu;
    private UrmButton next;
    private BufferedImage img;
    private int bgX;
    private int bgY;
    private int bgW;
    private int bgH;

    public LevelCompletedOverlay(Playing playing) {
        this.playing = playing;
        initImg();
        initButtons();
    }

    private void initButtons() {
        int menuX = (int) (330 * Game.SCALE);
        int nextX = (int) (445 * Game.SCALE);
        int y = (int) (195 * Game.SCALE);
        next = new UrmButton(nextX, y, URM_SIZE, URM_SIZE, 0);
        menu = new UrmButton(menuX, y, URM_SIZE, URM_SIZE, 2);
    }

    private void initImg() {
        img = LoadSave.getSpriteAtlas(LoadSave.COMPLETED_LVL);
        bgW = (int) (img.getWidth() * Game.SCALE);
        bgH = (int) (img.getHeight() * Game.SCALE);
        bgX = Game.GAME_WIDTH / 2 - bgW /2;
        bgY = (int) (75 * Game.SCALE);
    }

    public void draw(Graphics g) {
        g.drawImage(img, bgX, bgY, bgW, bgH, null);
        next.draw(g);
        menu.draw(g);
    }

    public void update() {
        next.update();
        menu.update();
    }

    private boolean isIn(UrmButton b, MouseEvent e) {
        return b.getBounds().contains(e.getX(), e.getY());
    }

    public void mouseMoved(MouseEvent e) {
        next.setMouseOver(Boolean.FALSE);
        menu.setMouseOver(Boolean.FALSE);

        if (isIn(menu, e))
            menu.setMouseOver(Boolean.TRUE);
        else if (isIn(next, e))
            next.setMouseOver(Boolean.TRUE);
    }

    public void mouseReleased(MouseEvent e) {
        if (isIn(menu, e)) {
            if (menu.isMousePressed()){
                playing.resetAll();
                GameState.state = GameState.MENU;
            }
        } else if (isIn(next, e))
            if (next.isMousePressed())
                playing.loadNextLevel();

        menu.resetBools();
        next.resetBools();
    }

    public void mousePressed(MouseEvent e) {
        if (isIn(menu, e))
            menu.setMousePressed(Boolean.TRUE);
        else if (isIn(next, e))
            next.setMousePressed(Boolean.TRUE);
    }
}
