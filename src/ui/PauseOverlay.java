package ui;

import static utils.Constants.UI.UrmButtons.*;

import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import gamestates.GameState;
import gamestates.Playing;
import main.Game;
import utils.LoadSave;

public class PauseOverlay {

    private Playing playing;
    private BufferedImage backgroundImg;
    private int bgX;
    private int bgY;
    private int bgW;
    private int bgH;
    private AudioOptions audioOptions;
    private UrmButton menuB;
    private UrmButton replayB;
    private UrmButton unpauseB;

    public PauseOverlay(Playing playing) {
        this.playing = playing;
        loadBackground();
        audioOptions = playing.getGame().getAudioOptions();
        createUrmButtons();
    }

    private void createUrmButtons() {
        int menuX = (int) (313 * Game.SCALE);
        int replayX = (int) (387 * Game.SCALE);
        int unpauseX = (int) (462 * Game.SCALE);
        int bY = (int) (325 * Game.SCALE);

        menuB = new UrmButton(menuX, bY, URM_SIZE, URM_SIZE, 2);
        replayB = new UrmButton(replayX, bY, URM_SIZE, URM_SIZE, 1);
        unpauseB = new UrmButton(unpauseX, bY, URM_SIZE, URM_SIZE, 0);
    }

    private void loadBackground() {
        backgroundImg = LoadSave.getSpriteAtlas(LoadSave.PAUSE_BACKGROUND);
        bgW = (int) (backgroundImg.getWidth() * Game.SCALE);
        bgH = (int) (backgroundImg.getHeight() * Game.SCALE);
        bgX = Game.GAME_WIDTH /2 - bgW / 2;
        bgY = (int) (25 * Game.SCALE);

    }

    public void update() {

        menuB.update();
        replayB.update();
        unpauseB.update();
        audioOptions.update();
    }

    public void draw(Graphics g) {
        // Background
        g.drawImage(backgroundImg, bgX, bgY, bgW, bgH, null);


        // URM buttons
        menuB.draw(g);
        replayB.draw(g);
        unpauseB.draw(g);

        audioOptions.draw(g);
    }

    public void mouseDragged(MouseEvent e) {
        audioOptions.mouseDragged(e);
    }

    public void mousePressed(MouseEvent e) {
        if (isIn(e, menuB))
            menuB.setMousePressed(Boolean.TRUE);
        else if (isIn(e, replayB))
            replayB.setMousePressed(Boolean.TRUE);
        else if (isIn(e, unpauseB))
            unpauseB.setMousePressed(Boolean.TRUE);
        else
            audioOptions.mousePressed(e);
    }

    public void mouseReleased(MouseEvent e) {
        if (isIn(e, menuB)) {
            if (menuB.isMousePressed()) {
                GameState.state = GameState.MENU;
                playing.unpauseGame();
            }
        } else if (isIn(e, replayB)) {
            if (replayB.isMousePressed()) {
                playing.resetAll();
                playing.unpauseGame();
            }
        } else if (isIn(e, unpauseB)) {
            if (unpauseB.isMousePressed())
                playing.unpauseGame();
        } else
            audioOptions.mouseReleased(e);

        menuB.resetBools();
        replayB.resetBools();
        unpauseB.resetBools();
    }

    public void mouseMoved(MouseEvent e) {
        menuB.setMouseOver(Boolean.FALSE);
        replayB.setMouseOver(Boolean.FALSE);
        unpauseB.setMouseOver(Boolean.FALSE);

        if (isIn(e, menuB))
            menuB.setMouseOver(Boolean.TRUE);
        else if (isIn(e, replayB))
            replayB.setMouseOver(Boolean.TRUE);
        else if (isIn(e, unpauseB))
            unpauseB.setMouseOver(Boolean.TRUE);
        else
            audioOptions.mouseMoved(e);
    }

    private boolean isIn(MouseEvent e, PauseButton button) {
        return button.getBounds().contains(e.getX(), e.getY());
    }
}
