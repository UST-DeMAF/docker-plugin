package ust.tad.dockerplugin.analysis.util;

public class MissingDockerImageException extends Exception{
    public MissingDockerImageException(String errorMessage) {
        super(errorMessage);
    }
}
