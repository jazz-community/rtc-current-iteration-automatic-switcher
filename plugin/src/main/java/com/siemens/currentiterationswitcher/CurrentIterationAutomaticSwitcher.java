package com.siemens.currentiterationswitcher;


import com.siemens.bt.jazz.services.base.BaseService;
import com.siemens.currentiterationswitcher.builder.Service;

/**
 * Entry point for the Service.java, called by the Jazz class loader.
 *
 * <p>This class must be implemented for enabling plug-ins to run inside Jazz. The implemented interface corresponds to
 * the component in {@code plugin.xml}, and this service is therefore the provided service by the interface.</p>
 */
public class CurrentIterationAutomaticSwitcher extends BaseService implements ICurrentIterationAutomaticSwitcher {
    /**
     * Constructs a new Service.java
     * <p>This constructor is only called by the Jazz class loader.</p>
     */
    public CurrentIterationAutomaticSwitcher() {
        super();
        router.get("run", Service.class);
    }
}
