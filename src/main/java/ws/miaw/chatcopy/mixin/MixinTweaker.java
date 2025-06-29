package ws.miaw.chatcopy.mixin;

import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

import java.io.File;
import java.util.List;

public class MixinTweaker implements ITweaker {
    @Override
    public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {
        MixinBootstrap.init();
        Mixins.addConfiguration("mixins.chatcopy.json");
    }

    @Override
    public void injectIntoClassLoader(LaunchClassLoader classLoader) {
    }

    @Override public String getLaunchTarget() { return "net.minecraft.client.main.Main"; }
    @Override public String[] getLaunchArguments() { return new String[0]; }
}