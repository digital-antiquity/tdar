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
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.struts.WriteableSession;
import org.tdar.struts.action.AbstractPersistableController;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.interceptor.PostOnly;

public abstract class AbstractCreatorController<T extends Creator> extends AbstractPersistableController<T> {

    public static final String CANNOT_SAVE_NULL_ADDRESS = "cannot save null address";

    private static final long serialVersionUID = -2125910954088505227L;

    public static final String RETURN_URL = "RETURN_URL";

    private Long addressId;
    private Address address;
    private String returnUrl;

    @SkipValidation
    @WriteableSession
    @PostOnly
    @Action(value = "save-address", results = {
            @Result(name = SUCCESS, type = "redirect", location = "../../browse/creators?id=${id}"),
            @Result(name = RETURN_URL, type = "redirect", location = "${returnUrl}")
    })
    public String saveAddress() throws TdarActionException {
        checkValidRequest(RequestType.MODIFY_EXISTING, this, InternalTdarRights.EDIT_ANYTHING);
        Address address2 = getAddress();
        if (address2 == null) {
            throw new TdarRecoverableRuntimeException(CANNOT_SAVE_NULL_ADDRESS);
        }
        if (address2.isValidForController()) {
            getPersistable().getAddresses().add(address2);
            getGenericService().saveOrUpdate(getPersistable());
            logger.info("{}", address2.getId());
            if (StringUtils.isNotBlank(getReturnUrl())) {
                return RETURN_URL;
            }
            return SUCCESS;
        }
        return ERROR;
    }

    @SkipValidation
    @WriteableSession
    @PostOnly
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
    public String editAddress() throws TdarActionException {
        checkValidRequest(RequestType.MODIFY_EXISTING, this, InternalTdarRights.EDIT_ANYTHING);

        return SUCCESS;
    }

    public Long getAddressId() {
        return addressId;
    }

    public void setAddressId(Long addressId) {
        this.addressId = addressId;
    }

    public Address getAddress() {
        if (Persistable.Base.isNotNullOrTransient(getAddressId())) {
            setAddress(getGenericService().find(Address.class, getAddressId()));
        } else if (address == null) {
            setAddress(new Address());
            getAddress().setType(AddressType.BILLING);
        }
        logger.info("returning address {}" , address);
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
