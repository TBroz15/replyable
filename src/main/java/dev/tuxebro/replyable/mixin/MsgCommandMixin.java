package dev.tuxebro.replyable.mixin;

import dev.tuxebro.replyable.Replyable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.commands.MsgCommand;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

@Mixin(MsgCommand.class)
public class MsgCommandMixin {
    @Inject(method = "sendMessage",at = @At(value = "HEAD"))
    private static void sendMessage(CommandSourceStack source, Collection<ServerPlayer> targets, PlayerChatMessage message, CallbackInfo ci) {
        ServerPlayer sender = source.getPlayer();
        if (sender == null) return;

        targets.forEach(receiver -> {
            Replyable.RECIEVER_2_PREV_SENDER.put(receiver.getUUID(), sender.getUUID());
        });
    }
}
