package ust.tad.dockerplugin.models.tadm;

public class InvalidPropertyValueException extends Exception {
  public InvalidPropertyValueException(String errorMessage) {
    super(errorMessage);
  }
}
