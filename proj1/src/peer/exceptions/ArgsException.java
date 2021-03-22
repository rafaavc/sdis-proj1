package exceptions;

import java.util.HashMap;

public class ArgsException extends Exception {
    private static final long serialVersionUID = -714327807018527511L;

    public static enum Type {
        ARGS_LENGTH,
        VERSION_NO,
        CHUNK_NO,
        REPLICATION_DEG,
        FILE_DOESNT_EXIST
    }

    private static final HashMap<Type, String> messages = new HashMap<>();

    static {
        messages.put(Type.ARGS_LENGTH, "Wrong amount of program arguments.");
        messages.put(Type.VERSION_NO, "Invalid version number. It should be <n>'.'<m>, where <n> and <m> are the ASCII codes of digits.");
        messages.put(Type.CHUNK_NO, "Invalid chunk no. Cannot exceed 6 chars.");
        messages.put(Type.REPLICATION_DEG, "Invalid replication degree. Must be a digit.");
        messages.put(Type.FILE_DOESNT_EXIST, "File doesnt exist");
    }

    public ArgsException(Type type) {
        super(messages.get(type));
    }

    public ArgsException(Type type, String extra) {
        super(messages.get(type) + " (" + extra + ")");
    }
}
