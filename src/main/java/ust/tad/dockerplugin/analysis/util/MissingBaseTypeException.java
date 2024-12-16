package ust.tad.dockerplugin.analysis.util;

public class MissingBaseTypeException extends Exception{
    public MissingBaseTypeException(String errorMessage) {
        super(errorMessage);
    }
}
