package ust.tad.dockerplugin.analysis.util;

import org.springframework.stereotype.Service;
import ust.tad.dockerplugin.models.tadm.ComponentType;

@Service
public class ComponentTypeProvider {

    public ComponentType createSoftwareApplicationType(ComponentType parentType) {
        ComponentType softwareApplicationType = new ComponentType();
        softwareApplicationType.setParentType(parentType);
        softwareApplicationType.setName("SoftwareApplication");
        return softwareApplicationType;
    }

    public ComponentType createDatabaseSystemType(ComponentType parentType) {
        ComponentType databaseSystemType = new ComponentType();
        databaseSystemType.setParentType(parentType);
        databaseSystemType.setName("DatabaseSystem");
        return databaseSystemType;
    }

    public ComponentType createMessageBrokerType(ComponentType parentType) {
        ComponentType messageBrokerType = new ComponentType();
        messageBrokerType.setParentType(parentType);
        messageBrokerType.setName("MessageBroker");
        return messageBrokerType;
    }
}
