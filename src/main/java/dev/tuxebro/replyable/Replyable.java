package dev.tuxebro.replyable;

import com.google.common.eventbus.Subscribe;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.gui.ModMismatchDisconnectedScreen;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.UUID;

@EventBusSubscriber
@Mod(Replyable.MODID)
public class Replyable {
    public static final String MODID = "replyable";
    private static final Logger LOGGER = LogUtils.getLogger();

    public Replyable(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("its time to reply");
    }

    public static final HashMap<UUID, UUID> REPLYABLES = new HashMap<>();

    public static int sendReply(CommandContext<CommandSourceStack> ctx) {
        ctx.getSource().getServer().sendSystemMessage(Component.literal("Hi"));
        return Command.SINGLE_SUCCESS;
    }

    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterCommandsEvent event) {
        var dispatcher = event.getDispatcher();

        var replyCommand = Commands
                .literal("testmismatch")
                .executes(Replyable::sendReply);

        var shortReplyCommand = Commands
                .literal("r")
                .redirect(replyCommand.getRedirect());

        dispatcher.register(replyCommand);
        dispatcher.register(shortReplyCommand);
    }
}
