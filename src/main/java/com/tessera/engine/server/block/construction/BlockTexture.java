package com.tessera.engine.server.block.construction;

import com.tessera.engine.server.block.BlockArrayTexture;

import java.util.Objects;

import static com.tessera.engine.client.visuals.gameScene.rendering.chunk.meshers.bufferSet.vertexSet.CompactVertexSet.MAX_BLOCK_ANIMATION_LENGTH;

public class BlockTexture {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BlockTexture that)) return false;
        return Objects.equals(POS_Y_NAME, that.POS_Y_NAME)
                && Objects.equals(NEG_Y_NAME, that.NEG_Y_NAME)
                && Objects.equals(POS_X_NAME, that.POS_X_NAME)
                && Objects.equals(NEG_X_NAME, that.NEG_X_NAME)
                && Objects.equals(POS_Z_NAME, that.POS_Z_NAME)
                && Objects.equals(NEG_Z_NAME, that.NEG_Z_NAME);
    }

    @Override
    public int hashCode() {
        return Objects.hash(POS_Y_NAME, NEG_Y_NAME, POS_X_NAME, NEG_X_NAME, POS_Z_NAME, NEG_Z_NAME);
    }

    public class FaceTexture {

        public final int zLayer;
        private byte animationFrames;

        public FaceTexture(int id, int animLength) {
            this.zLayer = id;
            this.setAnimationFrames(animLength);
        }

        public byte getAnimationFrames() {
            return animationFrames;
        }

        public void setAnimationFrames(int animationFrames) {
            if (animationFrames > MAX_BLOCK_ANIMATION_LENGTH) {
                animationFrames = MAX_BLOCK_ANIMATION_LENGTH;
            }
            this.animationFrames = (byte) animationFrames;
        }
    }

    public final void init(BlockArrayTexture textureNameMap) {
        POS_X = new FaceTexture(textureNameMap.getTextureLayer(POS_X_NAME), textureNameMap.getAnimationLength(POS_X_NAME));
        NEG_X = new FaceTexture(textureNameMap.getTextureLayer(NEG_X_NAME), textureNameMap.getAnimationLength(NEG_X_NAME));
        POS_Y = new FaceTexture(textureNameMap.getTextureLayer(POS_Y_NAME), textureNameMap.getAnimationLength(POS_Y_NAME));
        NEG_Y = new FaceTexture(textureNameMap.getTextureLayer(NEG_Y_NAME), textureNameMap.getAnimationLength(NEG_Y_NAME));
        POS_Z = new FaceTexture(textureNameMap.getTextureLayer(POS_Z_NAME), textureNameMap.getAnimationLength(POS_Z_NAME));
        NEG_Z = new FaceTexture(textureNameMap.getTextureLayer(NEG_Z_NAME), textureNameMap.getAnimationLength(NEG_Z_NAME));
    }

    //These have to be set in the constructor
    public final String POS_Y_NAME, NEG_Y_NAME, POS_X_NAME, NEG_X_NAME, POS_Z_NAME, NEG_Z_NAME;

    //These are set in the init method
    private FaceTexture POS_X, NEG_X, POS_Y, NEG_Y, POS_Z, NEG_Z;

    public FaceTexture getPOS_X() {
        return POS_X;
    }

    public FaceTexture getNEG_X() {
        return NEG_X;
    }

    public FaceTexture getPOS_Y() {
        return POS_Y;
    }

    public FaceTexture getNEG_Y() {
        return NEG_Y;
    }

    public FaceTexture getPOS_Z() {
        return POS_Z;
    }

    public FaceTexture getNEG_Z() {
        return NEG_Z;
    }

    public BlockTexture(String POS_Y, String NEG_Y, String POS_X, String NEG_X, String POS_Z, String NEG_Z) {
        this.NEG_Y_NAME = BlockArrayTexture.formatFilepath(POS_Y);
        this.POS_Y_NAME = BlockArrayTexture.formatFilepath(NEG_Y);
        this.POS_X_NAME = BlockArrayTexture.formatFilepath(POS_X);
        this.NEG_X_NAME = BlockArrayTexture.formatFilepath(NEG_X);
        this.POS_Z_NAME = BlockArrayTexture.formatFilepath(POS_Z);
        this.NEG_Z_NAME = BlockArrayTexture.formatFilepath(NEG_Z);
    }

    public BlockTexture(String POS_Y, String NEG_Y, String SIDES) {
        this.NEG_Y_NAME = BlockArrayTexture.formatFilepath(POS_Y);
        this.POS_Y_NAME = BlockArrayTexture.formatFilepath(NEG_Y);
        this.POS_X_NAME = BlockArrayTexture.formatFilepath(SIDES);
        this.NEG_X_NAME = BlockArrayTexture.formatFilepath(SIDES);
        this.POS_Z_NAME = BlockArrayTexture.formatFilepath(SIDES);
        this.NEG_Z_NAME = BlockArrayTexture.formatFilepath(SIDES);
    }

    public BlockTexture(String index) {
        POS_X_NAME = BlockArrayTexture.formatFilepath(index);
        NEG_X_NAME = BlockArrayTexture.formatFilepath(index);
        POS_Y_NAME = BlockArrayTexture.formatFilepath(index);
        NEG_Y_NAME = BlockArrayTexture.formatFilepath(index);
        POS_Z_NAME = BlockArrayTexture.formatFilepath(index);
        NEG_Z_NAME = BlockArrayTexture.formatFilepath(index);
    }

    public String toString() {
        return POS_Y_NAME + " " + NEG_Y_NAME + " " + POS_X_NAME + " " + NEG_X_NAME + " " + POS_Z_NAME + " " + NEG_Z_NAME;
    }

}
