package com.siemens.currentiterationswitcher.builder;

import com.ibm.team.repository.service.AbstractService;
import com.ibm.team.repository.service.TeamRawService;
import com.siemens.bt.jazz.services.base.rest.parameters.PathParameters;
import com.siemens.bt.jazz.services.base.rest.parameters.RestRequest;
import com.siemens.bt.jazz.services.base.rest.service.AbstractRestService;
import com.siemens.currentiterationswitcher.tasks.CurrentIterationSetterAsyncTask;
import org.apache.commons.logging.Log;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class Service extends AbstractRestService {

    private TeamRawService service;
    private Map<String, Object> map;

    public Service(
            Log log, HttpServletRequest request,
            HttpServletResponse response,
            RestRequest restRequest,
            TeamRawService parentService,
            PathParameters pathParameters) {
        super(log, request, response, restRequest, parentService, pathParameters);
        this.service = parentService;
        map = new HashMap<String, Object>();
    }

    /**
     * Execute the desired action and return a result
     */
    public void execute() throws IOException {
        try {
            /*IAsynchronousTaskSchedulerService schedulerService = service.getService(IAsynchronousTaskSchedulerService.class);
            schedulerService.schedule("com.siemens.currentiterationswitcher", "CurrentIterationAutomaticSwitcher"); // Doesn't work correctly*/

            CurrentIterationSetterAsyncTask task = new CurrentIterationSetterAsyncTask();

            Field field = AbstractService.class.getDeclaredField("requiredServicesMap");
            field.setAccessible(true);
            Object value = field.get(service);
            field.set(task, value);
            field.setAccessible(false);

            setUpProperties(pathParameters);
            task.updateConfigurationProperties(map);
            task.runTask();
            response.getWriter().write("Task run successful with Params: " + map.toString() + "\n" + task.getMessage());
            response.setStatus(200);
        } catch (Exception e) {
            response.sendError(500, e.getMessage());
            response.setStatus(500);
        }
    }

    private void setUpProperties(PathParameters pathParameters) {
        String id = "CurrentIterationAutomaticSwitcher.";
        if (restRequest.hasParameter("mode")) {
            map.put(id + "mode", restRequest.getParameterValue("mode"));
        } else {
            map.put(id + "mode", "ALL");
        }
        if (restRequest.hasParameter("delimiter")) {
            map.put(id + "delimiter", restRequest.getParameterValue("delimiter"));
        } else {
            map.put(id + "delimiter", ";");
        }
        if (restRequest.hasParameter("entrypoint")) {
            map.put(id + "entrypoint", restRequest.getParameterValue("entrypoint"));
        } else {
            map.put(id + "entrypoint", "program-timeline;roadmap;art");
        }
        if (restRequest.hasParameter("palist")) {
            map.put(id + "palist", restRequest.getParameterValue("palist"));
        } else {
            map.put(id + "palist", "");
        }
        if (restRequest.hasParameter("roleID")) {
            map.put(id + "roleID", restRequest.getParameterValue("roleID"));
        } else {
            map.put(id + "roleID", "JazzCM");
        }
        if (restRequest.hasParameter("mail")) {
            map.put(id + "mail", restRequest.getParameterValue("mail"));
        } else {
            map.put(id + "mail", "");
        }
    }
}
