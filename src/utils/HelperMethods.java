package utils;

import main.Game;

public class HelperMethods {

    public static boolean canMoveHere(float x, float y, float width, float height, int[][] levelData) {

        if (!isSolid(x, y, levelData))
            if (!isSolid(x+width, y+height, levelData))
                if (!isSolid(x+width, y, levelData))
                    if (!isSolid(x, y+height, levelData))
                        return Boolean.TRUE;
        return Boolean.FALSE;
    }

    private static boolean isSolid(float x, float y, int[][] levelData) {
        if (x < 0 || x >= Game.GAME_WIDTH) {
            return Boolean.TRUE;
        }
        if (y < 0 || y >= Game.GAME_HEIGHT) {
            return Boolean.TRUE;
        }

        float xIndex = x / Game.TILES_SIZE;
        float yIndex = y / Game.TILES_SIZE;

        int value = levelData[(int) yIndex][(int) xIndex];
        // Sprite level atlas: 48, 12º sprite is blank
        if (value >= 48 || value < 0 || value != 11) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }
}
