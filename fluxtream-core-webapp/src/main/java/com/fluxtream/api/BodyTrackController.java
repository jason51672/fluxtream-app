package com.fluxtream.api;

import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.fluxtream.mvc.controllers.ControllerHelper;
import com.fluxtream.services.impl.BodyTrackHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fluxtream.Configuration;
import com.fluxtream.domain.Guest;
import com.fluxtream.mvc.models.StatusModel;
import com.fluxtream.services.BodyTrackStorageService;
import com.fluxtream.services.GuestService;
import com.google.gson.Gson;

@Path("/bodytrack")
@Component("RESTBodytrackController")
@Scope("request")
public class BodyTrackController {

	@Autowired
	GuestService guestService;
	
	@Autowired
	BodyTrackStorageService bodytrackStorageService;

    @Autowired
    BodyTrackHelper bodyTrackHelper;

	Gson gson = new Gson();

	@Autowired
	Configuration env;
	
	@POST
	@Path("/uploadHistory")
	@Produces({ MediaType.APPLICATION_JSON })
	public String loadHistory(@QueryParam("username") String username,
			@QueryParam("connectorName") String connectorName) throws InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		Guest guest = guestService.getGuest(username);
        StatusModel status;
        if (!checkForPermissionAccess(guest.getId())){
            status = new StatusModel(true, "Failure!");
        }
        else{
            bodytrackStorageService.storeInitialHistory(guest.getId(), connectorName);
            status = new StatusModel(true, "Success!");
        }
		return gson.toJson(status);
    }

    @DELETE
    @Path("/users/{UID}/views/{id}")
    @Produces({ MediaType.APPLICATION_JSON })
    public String deleteBodytrackView(@PathParam("UID") Long uid, @PathParam("id") long viewId){
        StatusModel status;
        try{
            if (!checkForPermissionAccess(uid)){
                uid = null;
            }
            bodyTrackHelper.deleteView(uid,viewId);
            status = new StatusModel(true,"successfully deleted view " + viewId);
        }
        catch (Exception e){
            status = new StatusModel(false,"failed to delete view " + viewId);
        }
        return gson.toJson(status);
    }

    private boolean checkForPermissionAccess(long targetUid){
        Guest guest = ControllerHelper.getGuest();
        return targetUid == guest.getId() || guest.hasRole(Guest.ROLE_ADMIN) || guest.hasRole(Guest.ROLE_ADMIN);
    }

}
