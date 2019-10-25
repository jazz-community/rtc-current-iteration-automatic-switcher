package com.siemens.currentiterationswitcher.tasks;

import com.ibm.team.process.common.*;
import com.ibm.team.process.service.IProcessServerService;
import com.ibm.team.repository.common.IContributor;
import com.ibm.team.repository.common.IContributorHandle;
import com.ibm.team.repository.common.IItemType;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.repository.common.query.IItemQuery;
import com.ibm.team.repository.common.query.ast.IDynamicQueryModel;
import com.ibm.team.repository.common.query.ast.IItemQueryModel;
import com.ibm.team.repository.service.IMailerService;
import com.ibm.team.repository.service.IRepositoryItemService;
import com.ibm.team.repository.service.MailSender;
import com.ibm.team.repository.service.async.AbstractAutoScheduledTask;
import com.ibm.team.workitem.common.IAuditableCommon;
import com.ibm.team.workitem.common.internal.util.ItemQueryIterator;
import com.ibm.team.workitem.service.IAuditableServer;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.osgi.util.NLS;

import javax.mail.MessagingException;
import java.util.Arrays;
import java.util.List;

import static com.siemens.currentiterationswitcher.helpers.TimeHelper.delayFromMilitaryTime;
import static com.siemens.currentiterationswitcher.helpers.TimeHelper.isNotCurrent;

public class CurrentIterationSetterAsyncTask extends AbstractAutoScheduledTask {

    private static final String TASK_ID = "CurrentIterationAutomaticSwitcher"; //should equal that in plugin.xml
    private IRepositoryItemService itemService;
    private IProcessServerService processServerService;
    private String serviceMessage = "";

    public CurrentIterationSetterAsyncTask() {
        super();
    }

    @Override
    public String getTaskId() {
        return TASK_ID;
    }

    @Override
    public long getFixedDelay() {
        //sets fixed delay so task will be executed at start time
        //gets called at server start and every time task is executed(after successful execution)
        //time corresponds to server time(zone)
        long time = getLongConfigProperty(getTaskId() + ".time");
        return delayFromMilitaryTime(time);
        //return 180; //run every 3 min for debugging
    }


    @Override
    public void runTask() throws TeamRepositoryException {

        log(false, "Current Iteration Automatic Switcher started");
        //setup services etc
        IProgressMonitor monitor = new NullProgressMonitor();
        itemService = getService(IRepositoryItemService.class);
        processServerService = getService(IProcessServerService.class);

        String propertyID = getTaskId() + ".";
        Boolean modeSelect = false;
        List<String> palist = null;

        //collect config properties, names must match plugin.xml !!!
        String mode = getStringConfigProperty(propertyID + "mode");
        String delimiter = getStringConfigProperty(propertyID + "delimiter");

        //if mode is none or nonsensical do nothing
        if (!(mode.equalsIgnoreCase("ALL") || mode.equalsIgnoreCase("SELECT"))) {
            return;
        }
        if (mode.equalsIgnoreCase("SELECT")) {
            palist = Arrays.asList(getStringConfigProperty(propertyID + "palist").split(delimiter));
            modeSelect = true;
        }
        String path = getStringConfigProperty(propertyID + "entrypoint") != null ? getStringConfigProperty(propertyID + "entrypoint") : "";
        //no length check required for path, since it is required string property-> length will be at least 1
        String[] splitPath = path.split(delimiter);
        if (splitPath[0].length() < 1) {
            getLog().warn("Invalid (empty) entry point given");
            return;
        }
        String pas = modeSelect ? palist.toString() : "";
        log(false, "Mode: " + mode + " Entrypoint: " + path + " PAs: " + pas + " Delimiter: " + delimiter);
        List<IProjectAreaHandle> projectAreas = findProjectAreas(monitor);

        loopPAs(modeSelect, palist, splitPath, projectAreas);

        log(false, "Current Iteration Automatic Switcher terminated normally.");
    }

    //loop through PAs and do the stuff
    private void loopPAs(Boolean modeSelect, List<String> palist, String[] splitPath, List<IProjectAreaHandle> projectAreas) throws TeamRepositoryException {
        if (modeSelect) {
            for (String paName : palist) {
                paName = paName.replaceAll("[&<>\"'/]", "");//make url safe
                IProjectArea pa = (IProjectArea) processServerService.findProcessArea(paName.trim().replace(" ", "%20"), null);
                log(true, "Searching PA " + paName);
                if (pa != null) {
                    log(true, "Found PA " + paName + ". Processing it now.");
                    checkDevLineAndIterationsBeforeIteratingThem(splitPath, pa);
                }

            }
        } else {
            for (IProjectAreaHandle projectArea : projectAreas) {
                IProjectArea pa = (IProjectArea) itemService.fetchItem(projectArea, null);
                log(true, "Processing PA " + pa.getName());
                checkDevLineAndIterationsBeforeIteratingThem(splitPath, pa);
            }
        }
    }

    //find out if everythings okey and if a correct current iteration has already been set
    private void checkDevLineAndIterationsBeforeIteratingThem(String[] splitPath, IProjectArea pa) throws TeamRepositoryException {
        IDevelopmentLine devLine = getDevLine(pa, splitPath[0]);
        if (devLine != null) {
            String oldIter = "";//reset name of old current iter
            IIterationHandle iterationHandle = devLine.getCurrentIteration();
            if (iterationHandle == null) {
                iterateIterations(splitPath, devLine, pa, oldIter);
                return;
            }
            IIteration iteration = (IIteration) itemService.fetchItem(iterationHandle, null);
            //only search for new iteration if current isn't "current"
            if (isNotCurrent(iteration)) {
                oldIter = iteration.getName();
                iterateIterations(splitPath, devLine, pa, oldIter);
            }
        }
    }


    //goes through iterations and calls findAndSetCurrent for last bunch of iterations if path could be followed
    private void iterateIterations(String[] splitPath, IDevelopmentLine devLine, IProjectArea pa, String oldIter) throws TeamRepositoryException {
        IIterationHandle[] iterations = devLine.getIterations(); //aka timelines
        int countHelper = 1;

        //while there's still path from configProperty follow that
        boolean iterFound = true;
        while (splitPath.length > countHelper && iterFound) {
            iterFound = false;
            for (IIterationHandle iterationHandle : iterations) {
                IIteration iteration = (IIteration) itemService.fetchItem(iterationHandle, null);
                if (iteration.getId().equalsIgnoreCase(splitPath[countHelper])) {
                    iterations = iteration.getChildren();
                    iterFound = true;
                    break;
                }
            }
            countHelper++;
        }
        //if no iterations adhering to path found stop doing things
        if (!iterFound) {
            return;
        }
        findAndSetCurrent(devLine, pa, iterations, oldIter);


    }

    //now find right one from start and end date on the lowest level
    private void findAndSetCurrent(IDevelopmentLine devLine, IProjectArea pa, IIterationHandle[] iterations, String oldIter) throws TeamRepositoryException {
        for (IIterationHandle iterationHandle : iterations) {
            IIteration iteration = (IIteration) itemService.fetchItem(iterationHandle, null);

            if (iteration.getStartDate() != null && iteration.getEndDate() != null
                    && iteration.getStartDate().getTime() < System.currentTimeMillis()
                    && iteration.getEndDate().getTime() > System.currentTimeMillis()) {
                if(iteration.getChildren().length!=0){
                    findAndSetCurrent(devLine, pa, iteration.getChildren(), oldIter);
                }
                else {
                    devLine = (IDevelopmentLine) devLine.getWorkingCopy();
                    devLine.setCurrentIteration(iterationHandle);
                    processServerService.saveProcessItem(devLine);
                    log(false, "Current iteration for Project Area " + pa.getName() + " has been set to: " + iteration.getName());
                    createEmail(pa, iteration, oldIter);
                }

            }
        }
    }

    //find out who to send email to
    private void createEmail(IProjectArea projectArea, IIteration iteration, String oldIter) {
        IContributorHandle[] members = projectArea.getMembers();
        for (IContributorHandle handle : members) {
            String[] roleAssignmentIds = projectArea.getRoleAssignmentIds(handle);
            if (Arrays.asList(roleAssignmentIds).contains("JazzCM")) {
                try {
                    IContributor contributor = (IContributor) itemService.fetchItem(handle, null);
                    sendEmail(contributor.getEmailAddress(), projectArea, iteration, oldIter);
                } catch (TeamRepositoryException e) {
                    try {
                        getLog().warn("Could not send email notification to member with id: " + handle.getItemId());//get log errors if called from service
                    } catch (Exception ex) {
                        System.out.println("Could not send email notification to member with id: " + handle.getItemId());
                    }
                }
            }

        }

    }

    private void sendEmail(String mailAddress, IProjectArea projectArea, IIteration newIter, String oldIter) {
        IMailerService mailerService = getService(IMailerService.class);
        String subject = ("Current Iteration of " + projectArea.getName() + " has been updated");
        String message1 = "Current Iteration of Project Area " + projectArea.getName() + " has been moved ";
        String message2 = !oldIter.equals("") ? "from " + oldIter + " to " + newIter.getName() : " to " + newIter.getName();
        String message = "For your Information: \n" + message1 + message2 + ".\n";
        message = message.concat("Project Area Web UI: " + this.getRequestRepositoryURL() + "web/projects/" + projectArea.getName().trim().replace(" ", "%20"));
        MailSender sender = mailerService.getDefaultSender();

        try {
            mailerService.sendMail(sender, mailAddress, subject, message, null);
        } catch (MessagingException e) {
            String warningMessage = NLS.bind("Failed to send current iteration notification email to {0}", mailAddress);
            getLog().warn(warningMessage, e);
        }
    }

    //returns the timeline who's id matches path or null
    private IDevelopmentLine getDevLine(IProjectArea projectArea, String path) throws TeamRepositoryException {
        IDevelopmentLineHandle[] developmentLines = projectArea.getDevelopmentLines();
        for (IDevelopmentLineHandle developmentLine : developmentLines) {
            IDevelopmentLine devline = (IDevelopmentLine) itemService.fetchItem(developmentLine, null);
            if (devline.getId().equals(path)) {
                return devline;
            }
        }

        return null;
    }

    //returns all projectareahandles on server
    private List<IProjectAreaHandle> findProjectAreas(IProgressMonitor monitor) throws TeamRepositoryException {
        IItemType itemType = IProjectArea.ITEM_TYPE;
        IDynamicQueryModel queryModel = itemType.getQueryModel();
        IItemQuery query = IItemQuery.FACTORY.newInstance((IItemQueryModel) queryModel);
        ItemQueryIterator<IProjectAreaHandle> iterator = new ItemQueryIterator<IProjectAreaHandle>(getAuditableCommon(), query, null, null, null);
        return iterator.toList(monitor);
    }

    private IAuditableCommon getAuditableCommon() {
        return getService(IAuditableServer.class);
    }


    //logs/reports stuff (log breaks if task is started from service)
    private void log(Boolean debug, String message) {
        try {
            if (debug) {
                getLog().debug(message);
            } else {
                getLog().info(message);
            }
        } catch (Exception e) {
            serviceMessage = serviceMessage.concat(message + "\n");
        }
    }

    public String getMessage() {
        return serviceMessage;
    }
}
