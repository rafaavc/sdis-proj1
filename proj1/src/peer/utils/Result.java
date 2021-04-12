package utils;

import java.io.Serializable;

public class Result implements Serializable {
    private final boolean success;
    private final String message;
    
    public Result(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public boolean success() {
        return success;
    }
}
