package org.tdar.struts.action.entity;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.tdar.core.bean.entity.Address;
import org.tdar.core.bean.entity.AddressType;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.resource.Status;
import org.tdar.struts.action.AbstractPersistableController;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.interceptor.annotation.PostOnly;
import org.tdar.struts.interceptor.annotation.WriteableSession;
import org.tdar.utils.PersistableUtils;

public abstract class AbstractCreatorController<T extends Creator> extends AbstractPersistableController<T> {

    public static final String ADDRESS_IS_NOT_VALID = "address is not valid";

    public static final String CANNOT_SAVE_NULL_ADDRESS = "cannot save null address";

    private static final long serialVersionUID = -2125910954088505227L;

    public static final String RETURN_URL = "RETURN_URL";

    private Long addressId;
    private Address address;
    private String returnUrl;
    private File file;
    private String fileContentType;
    private String fileFileName;

    @SkipValidation
    @WriteableSession
    @PostOnly
    @Action(value = "save-address",
            interceptorRefs = { @InterceptorRef("editAuthenticatedStack") },
            results = {
                    @Result(name = SUCCESS, type = "tdar-redirect", location = "../../browse/creators/${id}"),
                    @Result(name = RETURN_URL, type = "tdar-redirect", location = "${returnUrl}"),
                    @Result(name = INPUT, location = "../address-info.ftl")
            })
    public String saveAddress() throws TdarActionException {
        Address address2 = getAddress();
        try {
            if (address2 == null) {
                addActionError(CANNOT_SAVE_NULL_ADDRESS);
            } else {
                address2.isValidForController();
            }
        } catch (Exception e) {
            addActionErrorWithException(ADDRESS_IS_NOT_VALID, e);
            return INPUT;
        }
        getPersistable().getAddresses().add(address2);
        getGenericService().saveOrUpdate(getPersistable());
        getLogger().info("{}", address2.getId());
        if (StringUtils.isNotBlank(getReturnUrl())) {
            return RETURN_URL;
        }
        return SUCCESS;
    }

    @SkipValidation
    @WriteableSession
    @PostOnly
    @Action(value = "delete-address",
            interceptorRefs = { @InterceptorRef("editAuthenticatedStack") },
            results = {
                    @Result(name = SUCCESS, type = "tdar-redirect", location = "../../creator/browse?id=${id}")
            })
    public String deleteAddress() throws TdarActionException {
        Address toDelete = getAddress();
        getLogger().info("to delete: {} ", toDelete);
        boolean remove = getPersistable().getAddresses().remove(toDelete);
        getLogger().info("did it work: {} ", remove);
        // this is likely superflouous, but I'm tired
        getGenericService().delete(toDelete);
        getGenericService().saveOrUpdate(getPersistable());
        return SUCCESS;
    }

    @SkipValidation
    @Action(value = "address", results = { @Result(name = SUCCESS, location = "../address-info.ftl") })
    public String editAddress() throws TdarActionException {

        return SUCCESS;
    }

    public Long getAddressId() {
        return addressId;
    }

    public void setAddressId(Long addressId) {
        this.addressId = addressId;
    }

    public Address getAddress() {
        if (PersistableUtils.isNotNullOrTransient(getAddressId())) {
            setAddress(getGenericService().find(Address.class, getAddressId()));
        } else if (address == null) {
            setAddress(new Address());
            getAddress().setType(AddressType.BILLING);
        }
        getLogger().info("returning address {}", address);
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public List<AddressType> getAllAddressTypes() {
        return Arrays.asList(AddressType.values());
    }

    public Creator getCreator() {
        return getPersistable();
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    @Override
    public List<Status> getStatuses() {
        List<Status> statuses = super.getStatuses();
        if (getPersistable() != null && getPersistable().getStatus() != Status.DUPLICATE) {
            statuses.remove(Status.DUPLICATE);
        }
        statuses.remove(Status.FLAGGED_ACCOUNT_BALANCE);
        statuses.remove(Status.DRAFT);
        return statuses;
    }

    public String getFileContentType() {
        return fileContentType;
    }

    public void setFileContentType(String fileContentType) {
        this.fileContentType = fileContentType;
    }

    public String getFileFileName() {
        return fileFileName;
    }

    public void setFileFileName(String fileFileName) {
        this.fileFileName = fileFileName;
    }

}
