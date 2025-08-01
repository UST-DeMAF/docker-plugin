package ust.tad.dockerplugin.models.tadm;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ComponentType extends ModelElementType {

  private ComponentType parentType;

  public ComponentType() {
    super();
  }

  public ComponentType(
      String name,
      String description,
      List<Property> properties,
      List<Operation> operations,
      ComponentType parentType) {
    super(name, description, properties, operations);
    this.parentType = parentType;
  }

  public ComponentType getParentType() {
    return this.parentType;
  }

  public void setParentType(ComponentType parentType) {
    this.parentType = parentType;
  }

  public ComponentType parentType(ComponentType parentType) {
    setParentType(parentType);
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) return true;
    if (!(o instanceof ComponentType)) {
      return false;
    }
    ComponentType componentType = (ComponentType) o;
    return Objects.equals(getId(), componentType.getId())
        && Objects.equals(getName(), componentType.getName())
        && Objects.equals(getDescription(), componentType.getDescription())
        && Objects.equals(getProperties(), componentType.getProperties())
        && Objects.equals(getOperations(), componentType.getOperations())
        && Objects.equals(parentType, componentType.parentType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        getId(), getName(), getDescription(), getProperties(), getOperations(), parentType);
  }

  @Override
  public String toString() {
    return "{"
        + " id='"
        + getId()
        + "'"
        + ", name='"
        + getName()
        + "'"
        + ", parentType='"
        + getParentType()
        + "'"
        + ", description='"
        + getDescription()
        + "'"
        + ", properties='"
        + getProperties()
        + "'"
        + ", operations='"
        + getOperations()
        + "'"
        + "}";
  }

  /**
   * Add Properties from another ComponentType to this ComponentType if they are not
   * present.
   *
   * @param otherComponentType the ComponentType the Properties are added from.
   */
  public void addPropertiesIfNotPresent(ComponentType otherComponentType) {
    List<Property> existingComponentTypeProperties = this.getProperties();
    List<String> propertyKeys =
            this.getProperties().stream().map(Property::getKey).collect(Collectors.toList());
    for (Property property: otherComponentType.getProperties()) {
      if (!propertyKeys.contains(property.getKey())) {
        existingComponentTypeProperties.add(property);
        this.setProperties(existingComponentTypeProperties);
      }
    }
  }

  /**
   * Add Operations from another ComponentType to this ComponentType if they are not
   * present.
   *
   * @param otherComponentType the ComponentType the Operations are added from.
   */
  public void addOperationsIfNotPresent(ComponentType otherComponentType) {
    List<Operation> existingComponentTypeOperations = this.getOperations();
    List<String> operationNames =
            this.getOperations().stream().map(Operation::getName).collect(Collectors.toList());
    for (Operation operation: otherComponentType.getOperations()) {
      if (!operationNames.contains(operation.getName())) {
        existingComponentTypeOperations.add(operation);
        this.setOperations(existingComponentTypeOperations);
      }
    }
  }
}
