package me.lorenzo0111.packbypass.mixin;

import me.lorenzo0111.packbypass.PackBypass;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.ServerList;
import net.minecraft.network.packet.c2s.play.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.s2c.play.ResourcePackSendS2CPacket;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;

import java.net.URL;
import java.util.concurrent.CompletableFuture;

@Debug(export = true)
@Mixin(ClientPlayNetworkHandler.class)
public abstract class ResourcepackMixin {

    @Shadow
    @Final
    private MinecraftClient client;

    @Shadow
    @Final
    @Nullable
    private ServerInfo serverInfo;

    @Shadow
    protected abstract void feedbackAfterDownload(CompletableFuture<?> downloadFuture);

    @Shadow
    protected abstract void sendResourcePackStatus(ResourcePackStatusC2SPacket.Status packStatus);

    @Shadow
    private static Text getServerResourcePackPrompt(Text defaultPrompt, @Nullable Text customPrompt) {
        return null;
    }

    @Shadow
    @Nullable
    private static URL resolveUrl(String url) {
        return null;
    }

    @Shadow
    public abstract @Nullable ServerInfo getServerInfo();

    /**
     * @author Lorenzo0111 - PackBypass
     * @reason Remove the resourcepack prompt kick
     */
    @Overwrite
    public void onResourcePackSend(ResourcePackSendS2CPacket packet) {
        URL url = resolveUrl(packet.getURL());
        String sha = packet.getSHA1();

        if (url == null) {
            this.sendResourcePackStatus(ResourcePackStatusC2SPacket.Status.DECLINED);
            return;
        }

        ServerInfo.ResourcePackPolicy policy = this.getServerInfo() != null ? this.getServerInfo().getResourcePackPolicy() : ServerInfo.ResourcePackPolicy.PROMPT;
        PackBypass.LOGGER.info("Resourcepack policy: " + policy);

        switch (policy) {
            case ENABLED -> {
                this.sendResourcePackStatus(ResourcePackStatusC2SPacket.Status.ACCEPTED);
                this.feedbackAfterDownload(this.client.getServerResourcePackProvider().download(url, sha, true));
            }
            case DISABLED -> {
                this.sendResourcePackStatus(ResourcePackStatusC2SPacket.Status.ACCEPTED);
                this.sendResourcePackStatus(ResourcePackStatusC2SPacket.Status.SUCCESSFULLY_LOADED);
            }
            case PROMPT -> this.client.execute(() -> this.client.setScreen(new ConfirmScreen((accepted) -> {
                this.client.setScreen(null);

                if (accepted) {
                    if (this.serverInfo != null) {
                        this.serverInfo.setResourcePackPolicy(ServerInfo.ResourcePackPolicy.ENABLED);
                    }

                    this.sendResourcePackStatus(ResourcePackStatusC2SPacket.Status.ACCEPTED);
                    this.feedbackAfterDownload(this.client.getServerResourcePackProvider().download(url, sha, true));
                } else {
                    this.sendResourcePackStatus(ResourcePackStatusC2SPacket.Status.ACCEPTED);
                    this.sendResourcePackStatus(ResourcePackStatusC2SPacket.Status.SUCCESSFULLY_LOADED);

                    if (this.serverInfo != null) {
                        this.serverInfo.setResourcePackPolicy(ServerInfo.ResourcePackPolicy.DISABLED);
                    }
                }

                if (this.serverInfo != null) {
                    ServerList.updateServerListEntry(this.serverInfo);
                }

            }, Text.translatable("multiplayer.texturePrompt.line1").append(" ").append(Text.translatable("")), getServerResourcePackPrompt(Text.translatable("multiplayer.texturePrompt.line2"), packet.getPrompt()), ScreenTexts.YES, Text.translatable("multiplayer.texturePrompt.bypass"))));
        }

    }

}
