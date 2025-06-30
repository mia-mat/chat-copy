package ws.miaw.chatcopy.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ws.miaw.chatcopy.MiawGUI;

import javax.annotation.Nullable;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.lang.reflect.Field;
import java.util.List;

@Mixin({GuiChat.class})
public class ChatMixin {

    private static final boolean DEBUG = false;

    private static final String TOAST_PREFIX = "copied: ";
    private static final Color TOAST_PREFIX_COLOUR = new Color(236, 193, 248);

    // func_73864_a -> mouseClicked
    @Inject(method = "func_73864_a", at = @At("HEAD"), cancellable = true)
    private void onMouseClicked(int mouseX, int mouseY, int mouseButton, CallbackInfo ci) {
        // only left-click + lctrl
        if (mouseButton != 0 || !Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) return;

        Minecraft mc = Minecraft.getMinecraft();
        GuiNewChat chatGUI = mc.ingameGUI.getChatGUI();

        float chatScale = chatGUI.getChatScale();
        int chatWidthUnscaled = chatGUI.getChatWidth();
        int chatWidthPx = (int) (chatWidthUnscaled * chatScale);

        int chatLeft = 2; // chat is always left‑aligned at 2px by default
        int chatRight = chatLeft + chatWidthPx;

        // clicked outside chat window
        if (mouseX < chatLeft || mouseX > chatRight) return;


        IChatComponent component = getChatComponentFromMouseClick(mouseY);
        if (component == null) return;

        String raw = component.getUnformattedText();
        String textSans = EnumChatFormatting.getTextWithoutFormattingCodes(raw);
        Toolkit.getDefaultToolkit()
                .getSystemClipboard()
                .setContents(new StringSelection(textSans), null);

        MiawGUI.show(
                new Color(0, 0, 0, 200),
                new MiawGUI.Segment(TOAST_PREFIX, TOAST_PREFIX_COLOUR),
                new MiawGUI.Segment(raw, Color.WHITE) // text colour is transferred if it has colour in-game
        );

        ci.cancel(); // don't call any click events that the text has

    }

    @Nullable
    private IChatComponent getChatComponentFromMouseClick(int mouseY) {
        Minecraft mc = Minecraft.getMinecraft();
        GuiNewChat chatGUI = mc.ingameGUI.getChatGUI();

        try { // field_146253_i -> List<ChatLine> drawnChatLines
            Field chatLinesField = GuiNewChat.class.getDeclaredField("field_146253_i");
            chatLinesField.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<ChatLine> chatLines = (List<ChatLine>) chatLinesField.get(chatGUI);

            float chatScale = chatGUI.getChatScale();
            int lineHeight = (int) (mc.fontRendererObj.FONT_HEIGHT * chatScale);

            // shift click up by one lineHeight to compensate
            int correctedY = mouseY - lineHeight - 4; // manual offset to fix weirdness

            final int BOTTOM_BUFFER = 40; // vertical starting position of chat from the bottom of the screen
            int startY = mc.currentScreen.height - BOTTOM_BUFFER;

            if (DEBUG) System.out.println("raw mouseY = " + mouseY + ", correctedY = " + correctedY);

            // adjust for scroll position in chat

            // field_146250_j -> getScrollPos
            Field scrollField = GuiNewChat.class.getDeclaredField("field_146250_j");
            scrollField.setAccessible(true);
            int scroll = (int) scrollField.get(chatGUI);

            int visibleLines = chatGUI.getLineCount(); // typically 10 by default (number of visible lines)

            for (int i = 0; i < visibleLines && i + scroll < chatLines.size(); i++) {
                int lineIndex = i + scroll;

                int lineYTop = startY - (i + 1) * lineHeight;
                int lineYBottom = lineYTop + lineHeight;

                ChatLine line = chatLines.get(lineIndex);
                String text = line.getChatComponent().getUnformattedText();

                if (DEBUG)
                    System.out.println("Line " + lineIndex + " spans Y[" + lineYTop + " … " + lineYBottom + ") → \"" + text + "\"");

                if (correctedY >= lineYTop && correctedY < lineYBottom) {
                    if (DEBUG) System.out.println("   ^-- matched HERE");
                    return line.getChatComponent();
                }
            }
            if (DEBUG) System.out.println("--- end debug, no match ---");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
