package com.tessera.window;

import org.lwjgl.nuklear.NkVec2;

public interface WindowEvents {
    public void windowResizeEvent(int width, int height);

    public boolean keyEvent(int key, int scancode, int action, int mods);

    public boolean mouseButtonEvent(int button, int action, int mods) ;

    public boolean mouseScrollEvent(NkVec2 scroll, double xoffset, double yoffset);
}
