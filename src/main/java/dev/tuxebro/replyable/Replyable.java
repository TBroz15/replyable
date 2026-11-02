package dev.tuxebro.replyable;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.commands.MsgCommand;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

@EventBusSubscriber
@Mod(Replyable.MODID)
public class Replyable {
    public static final String MODID = "replyable";
    private static final Logger LOGGER = LogUtils.getLogger();
    public Replyable() {
        LOGGER.info("its time to reply");
    }

    public static final String MESSAGE_KEY = "message";

    public static final Component NO_PREV_SENDER_COMP = Component
                .literal("You need a previous sender in order to reply!")
                .withStyle(ChatFormatting.RED);

    public static final Component PREV_SENDER_OFFLINE_COMP = Component
            .literal("Previous sender had to be online in order to reply!")
            .withStyle(ChatFormatting.RED);

    public static final Component SOMETHING_WENT_WRONG_COMP = Component
            .literal("Something went wrong when trying to reply...")
            .withStyle(ChatFormatting.RED);

    public static final HashMap<UUID, UUID> RECIEVER_2_PREV_SENDER = new HashMap<>();

    public static void sendReply(CommandSourceStack sourceReplier, PlayerChatMessage message) {
        var replier = sourceReplier.getPlayer();
        if (replier == null) return;

        var replierUUID = replier.getUUID();
        if (!RECIEVER_2_PREV_SENDER.containsKey(replierUUID)) {
            sourceReplier.sendSystemMessage(NO_PREV_SENDER_COMP);
            return;
        }

        var prevSenderUUID = RECIEVER_2_PREV_SENDER.get(replierUUID);
        var lastSender = replier.server.getPlayerList().getPlayer(prevSenderUUID);
        if (lastSender == null) {
            sourceReplier.sendSystemMessage(PREV_SENDER_OFFLINE_COMP);
            return;
        }

        var singleSenderList = Collections.singletonList(lastSender);

        MsgCommand.sendMessage(sourceReplier, singleSenderList, message);
    }

    public static int onSendReply(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack sourceReplier = ctx.getSource();

        try {
            MessageArgument.resolveChatMessage(ctx, "message", (message)->{
                Replyable.sendReply(sourceReplier, message);
            });
        } catch (Exception e) {
            LOGGER.info("We got an error?\n" + String.join("\n     ", e.getMessage()));
            sourceReplier.sendSystemMessage(SOMETHING_WENT_WRONG_COMP);
            return 0;
        }

        return 1;
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        var dispatcher = event.getDispatcher();

        var replyCommand = Commands
                .literal("reply")
                .then(Commands.argument(MESSAGE_KEY, new MessageArgument())
                        .executes(Replyable::onSendReply));
        var replyNode = dispatcher.register(replyCommand);

        var shortReplyCommand = Commands
                .literal("r")
                .redirect(replyNode);
        dispatcher.register(shortReplyCommand);
    }
}
