package gamestates;

import java.awt.event.MouseEvent;

import audio.AudioPlayer;
import main.Game;
import ui.MenuButton;

public class State {

    protected Game game;

    public State(Game game) {
        this.game = game;
    }

    public boolean isIn(MouseEvent e, MenuButton button) {
        return button.getBounds().contains(e.getX(), e.getY());
    }

    public Game getGame() {
        return game;
    }

    @SuppressWarnings("incomplete-switch")
    public void setGameState(GameState state) {
        switch(state) {
        case MENU -> game.getAudioPlayer().playSong(AudioPlayer.MENU_1);
        case PLAYING -> game.getAudioPlayer().setLvlSong(game.getPlaying().getLevelHandler().getLvlIndex());
        }

        GameState.state = state;
    }

}
