package main.javakanban.exception;

public class TimeIntervalConflictException extends RuntimeException {
    public TimeIntervalConflictException(String message) {
        super(message);
    }
}

