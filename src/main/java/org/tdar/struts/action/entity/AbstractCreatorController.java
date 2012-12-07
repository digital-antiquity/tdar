package org.tdar.struts.action.entity;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.entity.Address;
import org.tdar.core.bean.entity.AddressType;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.struts.WriteableSession;
import org.tdar.struts.action.AbstractPersistableController;
import org.tdar.struts.action.TdarActionException;

public abstract class AbstractCreatorController<T extends Creator> extends AbstractPersistableController<T> {

    private static final long serialVersionUID = -2125910954088505227L;

    private static final String RETURN_URL = "RETURN_URL";

    private Long addressId;
    private Address address;
    private String returnUrl;

    @SkipValidation
    @WriteableSession
    @Action(value = "save-address", results = {
            @Result(name = SUCCESS, type = "redirect", location = "../../browse/creators?id=${id}"),
            @Result(name = RETURN_URL, type = "redirect", location = "${returnUrl}")
    })
    public String saveAddress() throws TdarActionException {
        checkValidRequest(RequestType.MODIFY_EXISTING, this, InternalTdarRights.EDIT_ANYTHING);
        getPersistable().getAddresses().add(getAddress());
        getGenericService().saveOrUpdate(getPersistable());
        if (StringUtils.isNotBlank(getReturnUrl())) {
            return RETURN_URL;
        }
        return SUCCESS;
    }

    @SkipValidation
    @WriteableSession
    @Action(value = "delete-address", results = {
            @Result(name = SUCCESS, type = "redirect", location = "../../creator/browse?id=${id}")
    })
    public String deleteAddress() throws TdarActionException {
        checkValidRequest(RequestType.MODIFY_EXISTING, this, InternalTdarRights.EDIT_ANYTHING);
        getPersistable().getAddresses().remove(getAddress());
        // this is likely superflouous, but I'm tired
        getGenericService().delete(getAddress());
        getGenericService().saveOrUpdate(getPersistable());
        return SUCCESS;
    }

    @SkipValidation
    @Action(value = "address", results = { @Result(name = SUCCESS, location = "../address-info.ftl") })
    public String editBillingAddress() throws TdarActionException {
        checkValidRequest(RequestType.MODIFY_EXISTING, this, InternalTdarRights.EDIT_ANYTHING);
        if (Persistable.Base.isNotNullOrTransient(getAddressId())) {
            setAddress(getGenericService().find(Address.class, getAddressId()));
        }
        return SUCCESS;
    }

    public Long getAddressId() {
        return addressId;
    }

    public void setAddressId(Long addressId) {
        this.addressId = addressId;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public List<AddressType> getAllAddressTypes() {
        return Arrays.asList(AddressType.values());
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

}
