package models;

import com.feth.play.module.pa.user.AuthUser;
import play.db.ebean.Model;


public class LinkedAccountCB {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Long id;

	public User user;

	public String providerUserId;
	public String providerKey;
//
//	public static final Finder<Long, LinkedAccountCB> find = new Finder<Long, LinkedAccountCB>(
//			Long.class, LinkedAccountCB.class);

//	public static LinkedAccountCB findByProviderKey(final User user, String key) {
//		return find.where().eq("user", user).eq("providerKey", key)
//				.findUnique();
//	}

	public static LinkedAccountCB create(final AuthUser authUser) {
		final LinkedAccountCB ret = new LinkedAccountCB();
		ret.update(authUser);
		return ret;
	}
	
	public void update(final AuthUser authUser) {
		this.providerKey = authUser.getProvider();
		this.providerUserId = authUser.getId();
	}

	public static LinkedAccountCB create(final LinkedAccountCB acc) {
		final LinkedAccountCB ret = new LinkedAccountCB();
		ret.providerKey = acc.providerKey;
		ret.providerUserId = acc.providerUserId;

		return ret;
	}
}