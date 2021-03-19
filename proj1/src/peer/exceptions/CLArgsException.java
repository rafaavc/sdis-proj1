package exceptions;

import java.util.HashMap;

public class CLArgsException extends Exception {
    private static final long serialVersionUID = -714327807018527511L;

    public static enum Type {
        ARGS_LENGTH
    }

    private static final HashMap<Type, String> messages = new HashMap<>();

    static {
        messages.put(Type.ARGS_LENGTH, "Wrong amount of program arguments.");
    }

    public CLArgsException(Type type) {
        super(messages.get(type));
    }
}
