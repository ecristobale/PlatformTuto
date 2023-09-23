package ui;

import static utils.Constants.UI.PauseButtons.*;
import static utils.Constants.UI.UrmButtons.*;
import static utils.Constants.UI.VolumeButtons.*;

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
    private SoundButton musicButton;
    private SoundButton sfxButton;
    private UrmButton menuB;
    private UrmButton replayB;
    private UrmButton unpauseB;
    private VolumeButton volumeButton;

    public PauseOverlay(Playing playing) {
        this.playing = playing;
        loadBackground();
        createSoundButtons();
        createUrmButtons();
        createVolumeButton();
    }

    private void createVolumeButton() {
        int vX = (int) (309*Game.SCALE);
        int vY = (int) (278*Game.SCALE);
        volumeButton = new VolumeButton(vX, vY, SLIDER_WIDTH, VOLUME_HEIGHT);

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

    private void createSoundButtons() {
        int soundX = (int) (450 * Game.SCALE);
        int musicY = (int) (140 * Game.SCALE);
        int sfxY = (int) (186 * Game.SCALE);
        musicButton = new SoundButton(soundX, musicY, SOUND_SIZE, SOUND_SIZE);
        sfxButton = new SoundButton(soundX, sfxY, SOUND_SIZE, SOUND_SIZE);
    }

    private void loadBackground() {
        backgroundImg = LoadSave.getSpriteAtlas(LoadSave.PAUSE_BACKGROUND);
        bgW = (int) (backgroundImg.getWidth() * Game.SCALE);
        bgH = (int) (backgroundImg.getHeight() * Game.SCALE);
        bgX = Game.GAME_WIDTH /2 - bgW / 2;
        bgY = (int) (25 * Game.SCALE);

    }

    public void update() {
        musicButton.update();
        sfxButton.update();

        menuB.update();
        replayB.update();
        unpauseB.update();

        volumeButton.update();
    }

    public void draw(Graphics g) {
        // Background
        g.drawImage(backgroundImg, bgX, bgY, bgW, bgH, null);

        // Sound buttons
        musicButton.draw(g);
        sfxButton.draw(g);

        // URM buttons
        menuB.draw(g);
        replayB.draw(g);
        unpauseB.draw(g);

        //Volume Slider
        volumeButton.draw(g);
    }

    public void mouseDragged(MouseEvent e) {
        if (volumeButton.isMousePressed())
            volumeButton.changeX(e.getX());
    }

    public void mousePressed(MouseEvent e) {
        if (isIn(e, musicButton)) {
            musicButton.setMousePressed(Boolean.TRUE);
        } else if (isIn(e, sfxButton))
            sfxButton.setMousePressed(Boolean.TRUE);
        else if (isIn(e, menuB))
            menuB.setMousePressed(Boolean.TRUE);
        else if (isIn(e, replayB))
            replayB.setMousePressed(Boolean.TRUE);
        else if (isIn(e, unpauseB))
            unpauseB.setMousePressed(Boolean.TRUE);
        else if (isIn(e, volumeButton))
            volumeButton.setMousePressed(Boolean.TRUE);
    }

    public void mouseReleased(MouseEvent e) {
        if (isIn(e, musicButton)) {
            if (musicButton.isMousePressed())
                musicButton.setMuted(!musicButton.isMuted());
        } else if (isIn(e, sfxButton)) {
            if (sfxButton.isMousePressed())
                sfxButton.setMuted(!sfxButton.isMuted());
        } else if (isIn(e, menuB)) {
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
        }


        musicButton.resetBools();
        sfxButton.resetBools();
        menuB.resetBools();
        replayB.resetBools();
        unpauseB.resetBools();
        volumeButton.resetBools();
    }

    public void mouseMoved(MouseEvent e) {
        musicButton.setMouseOver(Boolean.FALSE);
        sfxButton.setMouseOver(Boolean.FALSE);
        menuB.setMouseOver(Boolean.FALSE);
        replayB.setMouseOver(Boolean.FALSE);
        unpauseB.setMouseOver(Boolean.FALSE);
        volumeButton.setMouseOver(Boolean.FALSE);

        if (isIn(e, musicButton))
            musicButton.setMouseOver(Boolean.TRUE);
        else if (isIn(e, sfxButton))
            sfxButton.setMouseOver(Boolean.TRUE);
        else if (isIn(e, menuB))
            menuB.setMouseOver(Boolean.TRUE);
        else if (isIn(e, replayB))
            replayB.setMouseOver(Boolean.TRUE);
        else if (isIn(e, unpauseB))
            unpauseB.setMouseOver(Boolean.TRUE);
        else if (isIn(e, volumeButton))
            volumeButton.setMouseOver(Boolean.TRUE);
    }

    private boolean isIn(MouseEvent e, PauseButton button) {
        return button.getBounds().contains(e.getX(), e.getY());
    }
}
