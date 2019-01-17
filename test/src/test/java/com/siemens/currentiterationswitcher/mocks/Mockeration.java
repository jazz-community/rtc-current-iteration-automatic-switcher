package com.siemens.currentiterationswitcher.mocks;

import com.ibm.team.process.common.*;
import com.ibm.team.repository.common.*;

import java.sql.Timestamp;
import java.util.Date;

//mock of IIterator
public class Mockeration implements IIteration {
    private Date startDate;
    private Date endDate;

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date date) {
        startDate = date;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date date) {
        endDate = date;
    }

    /***nothing implemented beyond here***************************************************************************************************/
    public String getId() {
        return null;
    }

    public void setId(String s) {

    }

    public String getLabel() {
        return null;
    }

    public IIterationHandle[] getChildren() {
        return new IIterationHandle[0];
    }

    public void setChildren(IIterationHandle[] iIterationHandles) {

    }

    public void insertChildAfter(IIterationHandle iIterationHandle, IIterationHandle iIterationHandle1) {

    }

    public void addChild(IIterationHandle iIterationHandle) {

    }

    public void removeChild(IIterationHandle iIterationHandle) {

    }

    public IIterationHandle getParent() {
        return null;
    }

    public void setParent(IIterationHandle iIterationHandle) {

    }

    public IDevelopmentLineHandle getDevelopmentLine() {
        return null;
    }

    public void setDevelopmentLine(IDevelopmentLineHandle iDevelopmentLineHandle) {

    }

    public boolean isArchived() {
        return false;
    }


    public boolean hasDeliverable() {
        return false;
    }

    public void setHasDeliverable(boolean b) {

    }

    public IIterationTypeHandle getIterationType() {
        return null;
    }

    public void setIterationType(IIterationTypeHandle iIterationTypeHandle) {

    }

    public int getProcessItemType() {
        return 0;
    }

    public String getPropertyName(String s) {
        return null;
    }

    public String getName() {
        return null;
    }

    public void setName(String s) {

    }

    public IDescription getDescription() {
        return null;
    }

    public IAuditableHandle getPredecessorState() {
        return null;
    }

    public IAuditableHandle getMergePredecessorState() {
        return null;
    }

    public boolean isNewItem() {
        return false;
    }

    public boolean hasHistory() {
        return false;
    }

    public IContributorHandle getModifiedBy() {
        return null;
    }

    public Date modified() {
        return null;
    }

    public boolean isComplete() {
        return false;
    }

    public boolean isPropertySet(String s) {
        return false;
    }

    public boolean isWorkingCopy() {
        return false;
    }

    public IItem getWorkingCopy() {
        return null;
    }

    public boolean isRedactedCopy() {
        return false;
    }

    public IItem getRedactedCopy() {
        return null;
    }

    public IItemHandle getItemHandle() {
        return null;
    }

    public IItemHandle getStateHandle() {
        return null;
    }

    public UUID getContextId() {
        return null;
    }

    public void setContextId(UUID uuid) {

    }

    public void setRequestedStateId(UUID uuid) {

    }

    public UUID getRequestedStateId() {
        return null;
    }

    public void setRequestedModified(Timestamp timestamp) {

    }

    public Timestamp getRequestedModified() {
        return null;
    }

    public UUID getItemId() {
        return null;
    }

    public boolean hasStateId() {
        return false;
    }

    public UUID getStateId() {
        return null;
    }

    public boolean sameStateId(IItemHandle iItemHandle) {
        return false;
    }

    public IItemType getItemType() {
        return null;
    }

    public Object getOrigin() {
        return null;
    }

    public boolean isImmutable() {
        return false;
    }

    public void makeImmutable() {

    }

    public void protect() {

    }

    public boolean sameItemId(IItemHandle iItemHandle) {
        return false;
    }

    public boolean hasFullState() {
        return false;
    }

    public IItem getFullState() {
        return null;
    }

    public boolean isAuditable() {
        return false;
    }

    public boolean isUnmanaged() {
        return false;
    }

    public boolean isSimple() {
        return false;
    }

    public boolean isConfigurationAware() {
        return false;
    }

    public Object getAdapter(Class aClass) {
        return null;
    }

    public long size() {
        return 0;
    }
}
