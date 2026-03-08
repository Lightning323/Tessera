/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.window.nuklear;

import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkUserFont;
import org.lwjgl.nuklear.NkUserFontGlyph;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;

import static org.lwjgl.nuklear.Nuklear.*;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_LEFT;

/**
 * @author zipCoder933
 */
public class NKUtils {

    public static int text(NkContext ctx, String text, int lineHeight, int alignment) {
        int height = 0;
        nk_layout_row_dynamic(ctx, lineHeight, 1);
        String[] splitText = text.split("\n");
        for (int i = 0; i < splitText.length; i++) {
            nk_text(ctx, splitText[i], alignment);
            height += lineHeight;
        }

        return height;
    }

    public static float calculateWrappedTextHeight(NkUserFont font, String text, float wrapWidth) {
        float spaceWidth = calculateTextWidth(font, " ");
        int start = 0;
        int word = 0;
        int lineStart = 0;
        float lineWidth = 0;
        int lineCount = 0;

        while (word < text.length()) {
            while (word < text.length() && text.charAt(word) != ' ' && text.charAt(word) != '\n') {
                word++;
            }

            float wordWidth = calculateTextWidth(font, text.substring(start, word));

            if (lineWidth + wordWidth > wrapWidth || (word < text.length() && text.charAt(word) == '\n')) {
                lineCount++;
                lineStart = start;
                lineWidth = 0;
            }

            if (word < text.length() && text.charAt(word) == '\n') {
                lineCount++;
                lineStart = word + 1;
                lineWidth = 0;
            } else {
                lineWidth += wordWidth + spaceWidth;
                start = word + 1;
            }

            word++;
        }

        // Count the last line if there's any text left
        if (lineStart < text.length()) {
            lineCount++;
        }

        return lineCount * font.height();
    }


    public static float calculateTextWidth(NkUserFont font, String text) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            NkUserFontGlyph glyph = NkUserFontGlyph.malloc(stack);
            ByteBuffer textBuffer = stack.UTF8(text, false);
            float width = 0;
            for (int i = 0; i < textBuffer.remaining(); i++) {
                int codepoint = textBuffer.get(i) & 0xFF;
                font.query().invoke(font.userdata().address(), font.height(), glyph.address(), codepoint, codepoint + 1);
                width += glyph.xadvance();
            }
            return width;
        }
    }

    public static void wrapText(NkContext ctx, String text, float wrapWidth) {
        NkUserFont font = ctx.style().font();
        float spaceWidth = calculateTextWidth(font, " ");
        int start = 0;
        int word = 0;
        int lineStart = 0;
        float lineWidth = 0;


        //Add a new line at the beginning if it doesnt already start with one
        if (!text.startsWith("\n")) {
            text = "\n" + text;
        }

        while (word < text.length()) {
            while (word < text.length() && text.charAt(word) != ' ' && text.charAt(word) != '\n') {
                word++;
            }

            float wordWidth = calculateTextWidth(font, text.substring(start, word));

            if (lineWidth + wordWidth > wrapWidth || (word < text.length() && text.charAt(word) == '\n')) {
                if (lineStart < start) { // Ensure indices are within bounds
                    String line = text.substring(lineStart, start);
                    try (MemoryStack stack = MemoryStack.stackPush()) {
                        ByteBuffer lineBuffer = stack.UTF8(line);
                        nk_text(ctx, lineBuffer, NK_TEXT_LEFT);
                        nk_layout_row_dynamic(ctx, font.height(), 1);
                    }
                }
                lineStart = start;
                lineWidth = 0;
            }

            if (word < text.length() && text.charAt(word) == '\n') {
                String line = text.substring(lineStart, start);
                try (MemoryStack stack = MemoryStack.stackPush()) {
                    ByteBuffer lineBuffer = stack.UTF8(line);
                    nk_text(ctx, lineBuffer, NK_TEXT_LEFT);
                    nk_layout_row_dynamic(ctx, font.height(), 1);
                }
                lineStart = word + 1;
                lineWidth = 0;
            } else {
                lineWidth += wordWidth + spaceWidth;
                start = word + 1;
            }

            word++;
        }

        // Ensure the last line is processed
        if (lineStart < text.length()) {
            String line = text.substring(lineStart);
            try (MemoryStack stack = MemoryStack.stackPush()) {
                ByteBuffer lineBuffer = stack.UTF8(line);
                nk_text(ctx, lineBuffer, NK_TEXT_LEFT);
            }
        }
    }

//    private static void drawTextWithShadow(NkContext ctx, String text,
//            float x, float y, float xOffset, float yOffset,
//            int textColorR, int textColorG, int textColorB,
//            int shadowColorR, int shadowColorG, int shadowColorB) {
//        // Draw text with white color (main text)
//        nk_nk_text_colored(ctx, text, x + xOffset, y + yOffset, nk_rgb(textColorR, textColorG, textColorB), nk_rgb(0, 0, 0));
//
//        // Draw text with black color at a slightly offset position (shadow)
//        nk_nk_text_colored(ctx, text, x, y, nk_rgb(shadowColorR, shadowColorG, shadowColorB), nk_rgb(0, 0, 0));
//    }
}
