package utils;

import java.awt.geom.Rectangle2D;

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

    public static float getEntityXPositionNextToWall(Rectangle2D.Float hitbox, float xSpeed) {
        int currentTile = (int) (hitbox.x / Game.TILES_SIZE);
        if (xSpeed > 0) {// to right
            // Pixel value for the current tile
            int tileXPositionPixel = currentTile * Game.TILES_SIZE;
            int xOffset = (int) (Game.TILES_SIZE - hitbox.width);
            return tileXPositionPixel + xOffset - 1;
        } else {// to left
            return currentTile * Game.TILES_SIZE;
        }
    }

    public static float getEntityYPositionUnderRoofOrAboveFloor(Rectangle2D.Float hitbox, float airSpeed) {
        int currentTile = (int) (hitbox.y / Game.TILES_SIZE);
        if (airSpeed > 0) {
            // Falling - touching floor
            int tileYPositionPixel = currentTile * Game.TILES_SIZE;
            int yOffset = (int) (Game.TILES_SIZE - hitbox.height);
            return tileYPositionPixel + yOffset - 1;
        } else { // Jumping
            return currentTile * Game.TILES_SIZE;
        }
    }

    public static boolean isEntityOnFloor(Rectangle2D.Float hitbox, int[][] levelData) {
        // Check pixel below bottomleft and bottomright
        if (!isSolid(hitbox.x, hitbox.y + hitbox.height + 1, levelData))
            if (!isSolid(hitbox.x + hitbox.width, hitbox.y + hitbox.height + 1, levelData))
                return Boolean.FALSE;
        return Boolean.TRUE;
    }

    private static boolean isSolid(float x, float y, int[][] levelData) {
        int maxWidth = levelData[0].length * Game.TILES_SIZE;
        if (x < 0 || x >= maxWidth) {
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
