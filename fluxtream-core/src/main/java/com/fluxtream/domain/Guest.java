package com.fluxtream.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import org.hibernate.annotations.Index;

@Entity(name="Guest")
@NamedQueries ( {
	@NamedQuery( name="guest.byEmail",
			query="SELECT guest FROM Guest guest WHERE guest.email=?"),
	@NamedQuery( name="guest.byUsername",
			query="SELECT guest FROM Guest guest WHERE guest.username=?"),
	@NamedQuery( name="guests.all",
			query="SELECT guest FROM Guest guest")
})
public class Guest extends AbstractEntity {

	public static final String ROLE_USER = "ROLE_USER";
	public static final String ROLE_ADMIN = "ROLE_ADMIN";
	public static final String ROLE_COACH = "ROLE_COACH";
	public static final String ROLE_ROOT = "ROLE_ROOT";
	
	@Index(name="username_index")
	public String username;
	public String firstname, lastname, password;
	@Index(name="email_index")
	public String email;
	public String salt;
	public String connectorConfigStateKey;
	
	transient List<String> userRoles;
	public String roles = ROLE_USER;

	public Guest() {}

	public boolean hasRole(String role) {
		return getUserRoles().contains(role);
	}
	
	public List<String> getUserRoles() {
		if (userRoles==null) {
			userRoles = new ArrayList<String>();
			StringTokenizer st = new StringTokenizer(roles, ",");
			while(st.hasMoreTokens())
				userRoles.add(st.nextToken());
		}
		return userRoles;
	}

	public Object getGuestName() {
		if (firstname!=null)
			return firstname;
		else return username;
	}

}