package models;

import be.objectify.deadbolt.core.models.Permission;

/**
 * Initial version based on work by Steve Chaloner (steve@objectify.be) for
 * Deadbolt2
 */
public class UserPermission implements Permission {
	private static final long serialVersionUID = 1L;

	public Long id;

	public String value;

	public String getValue() {
		return value;
	}

}
