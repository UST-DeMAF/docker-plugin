package ust.tad.dockerplugin.analysis.util;

public class MissingComponentsException extends Exception {
    public MissingComponentsException(String errorMessage) {
        super(errorMessage);
    }
}
