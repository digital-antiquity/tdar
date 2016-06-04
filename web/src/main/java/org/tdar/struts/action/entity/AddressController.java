package org.tdar.struts.action.entity;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Namespaces;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.entity.Address;
import org.tdar.core.bean.entity.AddressType;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.service.EntityService;
import org.tdar.struts.action.AbstractAuthenticatableAction;
import org.tdar.struts.action.PersistableLoadingAction;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.action.AbstractPersistableController.RequestType;
import org.tdar.struts.interceptor.annotation.PostOnly;
import org.tdar.struts.interceptor.annotation.WriteableSession;
import org.tdar.utils.PersistableUtils;

import com.opensymphony.xwork2.Preparable;
import com.opensymphony.xwork2.Validateable;

@Namespaces(value={
    @Namespace("entity/institution"),
    @Namespace("entity/person")}
)
@Component
@Scope("prototype")
@ParentPackage("secured")
@SuppressWarnings("rawtypes")
public class AddressController extends AbstractAuthenticatableAction implements Preparable, Validateable, PersistableLoadingAction<Creator> {

    private static final long serialVersionUID = 4823125326026850873L;
    public static final String ADDRESS_IS_NOT_VALID = "address is not valid";
    public static final String CANNOT_SAVE_NULL_ADDRESS = "cannot save null address";
    public static final String RETURN_URL = "RETURN_URL";

    private Long id;
    private Long addressId;
    private Address address;
    private String returnUrl;

    @Autowired
    private EntityService entityService;
    private Creator creator;

    @Override
    public void validate() {
        super.validate();
        try {
            if (getAddress() == null) {
                addActionError(CANNOT_SAVE_NULL_ADDRESS);
            } else {
                getAddress().isValidForController();
            }
        } catch (Exception e) {
            addActionErrorWithException(ADDRESS_IS_NOT_VALID, e);
        }
    }

    @WriteableSession
    @PostOnly
    @Action(value = "save-address",
            interceptorRefs = { @InterceptorRef("editAuthenticatedStack") },
            results = {
                    @Result(name = SUCCESS, type = TDAR_REDIRECT, location = "../../browse/creators/${id}"),
                    @Result(name = RETURN_URL, type = TDAR_REDIRECT, location = "${returnUrl}"),
                    @Result(name = INPUT, location = "../address-info.ftl")
            })
    public String saveAddress() throws TdarActionException {
        entityService.saveAddress(getAddress(), getCreator());
        getLogger().info("{}", getAddress().getId());
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
                    @Result(name = SUCCESS, type = TDAR_REDIRECT, location = "../../creator/browse?id=${id}")
            })
    public String deleteAddress() throws TdarActionException {
        entityService.deleteAddressForCreator(address, getCreator());
        return SUCCESS;
    }

    @SkipValidation
    @Action(value = "{id}/address", results = { @Result(name = SUCCESS, location = "../address-info.ftl") })
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

    public Creator<?> getCreator() {
        return getPersistable();
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    @Override
    public Class<Creator> getPersistableClass() {
        return Creator.class;
    }

    @Override
    public boolean authorize() throws TdarActionException {
        return getAuthorizationService().canEditCreator(getPersistable(), getAuthenticatedUser());
    }

    @Override
    public Creator<?> getPersistable() {
        return creator;
    }

    @Override
    public void setPersistable(Creator persistable) {
        this.creator = persistable;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public InternalTdarRights getAdminRights() {
        return InternalTdarRights.EDIT_PERSONAL_ENTITES;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public void prepare() throws Exception {
        prepareAndLoad(this, RequestType.EDIT);        
    }
}
