package ws.miaw.chatcopy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.concurrent.atomic.AtomicReference;

public class MiawGUI {

    // Duration of the fade-in animation in ms
    private static final long FADE_IN = 200;

    // Duration that the toast stays fully visible (no movement)
    private static final long SHOW = 1000;

    // Duration of the fade-out animation in ms
    private static final long FADE_OUT = 200;

    private static final long TOTAL = FADE_IN + SHOW + FADE_OUT;

    // Padding inside the toast box, in px
    private static final int PADDING = 6;

    // Margin from screen edge, in px
    private static final int MARGIN = 0;

    // Vertical position as a percentage of screen height
    private static final float DROP_DOWN_PERCENT = 0.1f;

    private static final AtomicReference<Toast> current = new AtomicReference<>();

    public static void show(String text, int bgColor, int textColor) {
        show(bgColor, new Segment(text, textColor));
    }

    public static void show(String text, int bgColor, Color textColor) {
        show(bgColor, new Segment(text, textColor));
    }

    public static void show(int bgColor, Segment... segments) {
        current.set(new Toast(bgColor, System.currentTimeMillis(), segments));
    }

    public static void show(Color bgColor, Segment... segments) {
        current.set(new Toast(bgColor, System.currentTimeMillis(), segments));
    }

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) return;
        Toast t = current.get();
        if (t == null) return;

        long elapsed = System.currentTimeMillis() - t.startTime;
        if (elapsed > TOTAL) {
            current.set(null);
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution sr = new ScaledResolution(mc);
        int sw = sr.getScaledWidth();
        int sh = sr.getScaledHeight();
        FontRenderer fr = mc.fontRendererObj;

        // total width of all segments
        int totalTextW = 0;
        for (Segment seg : t.segments) totalTextW += fr.getStringWidth(seg.text);
        int boxW = totalTextW + PADDING * 2;
        int boxH = fr.FONT_HEIGHT + PADDING * 2;

        // compute X animation for right side
        float startX = sw + boxW + MARGIN;
        float visibleX = sw - boxW - MARGIN;
        float x;
        if (elapsed < FADE_IN) {
            float p = elapsed / (float) FADE_IN;
            x = startX + (visibleX - startX) * p;
        } else if (elapsed < FADE_IN + SHOW) {
            x = visibleX;
        } else {
            float p = (elapsed - FADE_IN - SHOW) / (float) FADE_OUT;
            x = visibleX + (startX - visibleX) * p;
        }

        // compute Y as a percentage up from the bottom
        int y = sh - boxH - (int) (sh * DROP_DOWN_PERCENT);

        // draw background
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        drawRect(x, y, x + boxW, y + boxH, t.bgColor);

        // draw text segments
        int textX = (int) x + PADDING;
        int textY = y + PADDING;
        for (Segment seg : t.segments) {
            fr.drawString(seg.text, textX, textY, seg.color);
            textX += fr.getStringWidth(seg.text);
        }
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private static void drawRect(float x1, float y1, float x2, float y2, int color) {
        Gui.drawRect((int) x1, (int) y1, (int) x2, (int) y2, color);
    }

    protected static int convertToColourDrawInt(Color awtColour) {
        return (awtColour.getAlpha() << 24)  // Alpha
                | (awtColour.getRed() << 16)  // Red
                | (awtColour.getGreen() << 8)   // Green
                | awtColour.getBlue();         // Blue
    }

    public static class Segment {
        public final String text;
        public final int color; // RGB hex, e.g. 0xFF0000 for red

        public Segment(String text, int color) {
            this.text = text;
            this.color = color;
        }

        public Segment(String text, Color color) {
            this.text = text;
            this.color = convertToColourDrawInt(color);
        }
    }

    private static class Toast {
        final int bgColor;   // ARGB
        final long startTime;
        final Segment[] segments;

        Toast(int bg, long start, Segment... segs) {
            bgColor = bg;
            startTime = start;
            segments = segs;
        }

        Toast(Color bg, long start, Segment... segs) {
            bgColor = convertToColourDrawInt(bg);
            startTime = start;
            segments = segs;
        }
    }

}
