package org.example;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class MessageHandler  {
    private static final Queue<ChatMessage> messageHistory = new LinkedList<>();
    private static final int MESSAGE_HISTORY_SIZE = 50;
    private MessageHandler() {}

    public static void handle(ChannelHandlerContext ctx, List<Channel> channels, String text) {
        if (channels.isEmpty()) {
            channels.add(ctx.channel());
        }

        var parts = text.split("user: | message: ");

        if (parts.length == 3) {
            var sender = parts[1];
            var messageText = parts[2];
            var chatMessage = new ChatMessage(sender, messageText, ctx);

            if (messageHistory.size() + 1 > MESSAGE_HISTORY_SIZE) {
                messageHistory.poll();
            }

            messageHistory.add(chatMessage);
            broadcast(channels, chatMessage);
        } else {
            System.out.println("Некорректный формат строки: " + text);
        }
    }

    public static void sendHistory(ChannelHandlerContext ctx) {
        for (var message : messageHistory) {
            ctx.writeAndFlush(new TextWebSocketFrame(message.toString()));
        }
    }

    public static void broadcast(List<Channel> channels, ChatMessage message) {
        for (var channel : channels) {
            channel.writeAndFlush(new TextWebSocketFrame(message.toString()));
        }
    }
}
