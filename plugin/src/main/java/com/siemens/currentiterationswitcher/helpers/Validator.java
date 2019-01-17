package com.siemens.currentiterationswitcher.helpers;

import com.ibm.team.process.common.IProjectArea;
import com.ibm.team.process.service.IProcessServerService;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.repository.service.IConfigurationPropertyValidator2;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Validator implements IConfigurationPropertyValidator2 {
    private final static String MODE_KEY = "CurrentIterationAutomaticSwitcher.mode";
    private final static String TIME_KEY = "CurrentIterationAutomaticSwitcher.time";
    private final static String PAS_KEY = "CurrentIterationAutomaticSwitcher.palist";
    private final static String DELIMITER_KEY = "CurrentIterationAutomaticSwitcher.delimiter";
    private final static String PLUGIN_ID = "com.siemens.currentiterationswitcher";

    @Override
    public Map<String, IStatus> validateProperties(Map<String, Object> properties, boolean b, Map<String, Object> serviceMap) {
        //the map to return
        HashMap<String, IStatus> propStatus = new HashMap<String, IStatus>(4);
        String mode = (String) properties.get(MODE_KEY);
        Long time = (Long) properties.get(TIME_KEY);
        String pas = (String) properties.get(PAS_KEY);
        String del = (String) properties.get(DELIMITER_KEY);

        IStatus modeStatus = validateMode(mode);
        IStatus timeStatus = validateTime(time);
        IStatus paStatus = validatePAs(pas, del, serviceMap);
        IStatus delStatus = del.isEmpty() ? new Status(Status.ERROR, PLUGIN_ID, "Please don't leave empty") : Status.OK_STATUS;

        propStatus.put(MODE_KEY, modeStatus);
        propStatus.put(TIME_KEY, timeStatus);
        propStatus.put(PAS_KEY, paStatus);
        propStatus.put(DELIMITER_KEY, delStatus);

        return propStatus;
    }

    private IStatus validatePAs(String pas, String delimiter, Map<String, Object> serviceMap) {
        if (pas.isEmpty()) {
            return Status.OK_STATUS;
        }
        List<String> notFoundPas = new LinkedList<>();
        Boolean notAllFound = false;
        IProcessServerService processServerService = (IProcessServerService) serviceMap.get(IProcessServerService.class.getName());
        String[] paList = pas.split(delimiter);

        for (String paName : paList) {
            paName = paName.replaceAll("[&<>\"'/]", "");//make url safe
            IProjectArea pa;
            try {
                pa = (IProjectArea) processServerService.findProcessArea(paName.trim().replace(" ", "%20"), null);
            } catch (TeamRepositoryException e) {
                return new Status(Status.ERROR, PLUGIN_ID, "Needs ServerProcessService to validate.");
            }

            if (pa == null) {
                notAllFound=true;
                notFoundPas.add(paName);
            }
        }
        if(notAllFound){
            return new Status(Status.WARNING, PLUGIN_ID, "The project areas named:" + notFoundPas + " don't exist or you're missing permissions");
        }
        return Status.OK_STATUS;
    }

    private IStatus validateTime(Long time) {
        if (time < 0 || time > 2400) {
            return new Status(Status.ERROR, PLUGIN_ID, "Time must be between 0 and 2400");
        } else if (time % 100 >= 60) {
            return new Status(Status.ERROR, PLUGIN_ID, "The hour has only 60 minutes...");
        } else {
            return Status.OK_STATUS;
        }
    }

    private IStatus validateMode(String mode) {

        if (mode.equalsIgnoreCase("ALL") || mode.equalsIgnoreCase("NONE") || mode.equalsIgnoreCase("SELECT")) {
            return Status.OK_STATUS;
        } else {
            return new Status(Status.ERROR, PLUGIN_ID, "Mode must be one of 'ALL', 'SELECT' or 'NONE'.");
        }
    }

    @Override
    public Map<String, IStatus> validateProperties(Map<String, Object> map, boolean b) {
        throw new UnsupportedOperationException("Service dependencies are needed to validate the configuration properties for " + this.getClass().getName());
    }
}
