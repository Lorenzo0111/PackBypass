package me.lorenzo0111.packbypass.mixin;

import me.lorenzo0111.packbypass.PackBypass;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;

import java.net.URL;
import java.util.UUID;

@Debug(export = true)
@Mixin(ClientCommonNetworkHandler.class)
public abstract class ResourcepackMixin {

    @Shadow
    @Final
    protected MinecraftClient client;

    @Shadow
    @Final
    @Nullable
    protected ServerInfo serverInfo;

    @Shadow
    @Final
    protected ClientConnection connection;

    @Shadow
    @Nullable
    private static URL getParsedResourcePackUrl(String url) {
        return null;
    }

    @Shadow protected abstract Screen createConfirmServerResourcePackScreen(UUID id, URL url, String hash, boolean required, @Nullable Text prompt);

    /**
     * @author Lorenzo0111 - PackBypass
     * @reason Remove the resourcepack prompt kick
     */
    @Overwrite
    public void onResourcePackSend(ResourcePackSendS2CPacket packet) {
        UUID uuid = packet.id();
        URL url = getParsedResourcePackUrl(packet.url());
        String hash = packet.hash();

        if (url == null) {
            this.connection.send(new ResourcePackStatusC2SPacket(uuid, ResourcePackStatusC2SPacket.Status.INVALID_URL));
            return;
        }

        ServerInfo.ResourcePackPolicy policy = serverInfo != null ? serverInfo.getResourcePackPolicy() : ServerInfo.ResourcePackPolicy.PROMPT;
        PackBypass.LOGGER.info("Resourcepack policy: {}", policy);

        switch (policy) {
            case ENABLED -> this.client.getServerResourcePackProvider().addResourcePack(uuid, url, hash);
            case DISABLED -> {
                this.connection.send(new ResourcePackStatusC2SPacket(uuid, ResourcePackStatusC2SPacket.Status.ACCEPTED));
                this.connection.send(new ResourcePackStatusC2SPacket(uuid, ResourcePackStatusC2SPacket.Status.SUCCESSFULLY_LOADED));
            }
            case PROMPT ->
                    this.client.setScreen(this.createConfirmServerResourcePackScreen(uuid, url, hash, false, packet.prompt().orElse(null)));
        }
    }

}
