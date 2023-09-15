package ui;

import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import gamestates.GameState;
import main.Game;
import utils.LoadSave;

import static utils.Constants.UI.PauseButtons.*;

public class PauseOverlay {

    private BufferedImage backgroundImg;
    private int bgX;
    private int bgY;
    private int bgW;
    private int bgH;
    private SoundButton musicButton;
    private SoundButton sfxButton;

    public PauseOverlay() {
        loadBackground();
        createSoundButtons();
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
    }

    public void draw(Graphics g) {
        // Background
        g.drawImage(backgroundImg, bgX, bgY, bgW, bgH, null);

        // Sound buttons
        musicButton.draw(g);
        sfxButton.draw(g);
    }

    public void mouseDragged(MouseEvent e) {

    }

    public void mousePressed(MouseEvent e) {
        if (isIn(e, musicButton)) {
            musicButton.setMousePressed(Boolean.TRUE);
        } else if (isIn(e, sfxButton))
            sfxButton.setMousePressed(Boolean.TRUE);
    }

    public void mouseReleased(MouseEvent e) {
        if (isIn(e, musicButton)) {
            if (musicButton.isMousePressed())
                musicButton.setMuted(!musicButton.isMuted());
        } else if (isIn(e, sfxButton))
            if (sfxButton.isMousePressed())
                sfxButton.setMuted(!sfxButton.isMuted());

        musicButton.resetBools();
        sfxButton.resetBools();
    }

    public void mouseMoved(MouseEvent e) {
        musicButton.setMouseOver(Boolean.FALSE);
        sfxButton.setMouseOver(Boolean.FALSE);

        if (isIn(e, musicButton))
            musicButton.setMouseOver(Boolean.TRUE);
        else if (isIn(e, sfxButton))
            sfxButton.setMouseOver(Boolean.TRUE);
    }

    private boolean isIn(MouseEvent e, PauseButton button) {
        return button.getBounds().contains(e.getX(), e.getY());
    }
}
