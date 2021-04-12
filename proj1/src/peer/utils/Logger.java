package utils;

import channels.MulticastChannel;
import channels.MulticastChannel.ChannelType;

public class Logger {
    public static void error(Throwable thrown, boolean showStackTrace) {
        System.err.println("WAS THROWN: " + thrown.getMessage());
        if (showStackTrace) {
            thrown.printStackTrace();
        }
    }

    public static void error(Throwable thrown, String extra) {
        System.err.println("WAS THROWN: " + thrown.getMessage());
        System.err.println(extra);
    }

    public static void channelMessage(ChannelType type, String msg) {
        System.out.println("[" + MulticastChannel.messages.get(type) + "] " + msg);
    }

    public static void log(String msg) {
        System.out.println(msg);
    }
}
