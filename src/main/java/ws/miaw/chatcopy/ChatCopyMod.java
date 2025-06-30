package ws.miaw.chatcopy;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = ChatCopyMod.MODID, version = ChatCopyMod.VERSION)
public class ChatCopyMod {
    public static final String MODID = "chatcopy";
    public static final String VERSION = "1.1.1";

    @EventHandler
    public void init(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new MiawGUI());
    }

}
