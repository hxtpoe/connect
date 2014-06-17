package models;

import com.couchbase.client.CouchbaseClient;
import com.couchbase.client.protocol.views.Query;
import com.couchbase.client.protocol.views.View;
import com.couchbase.client.protocol.views.ViewResponse;
import com.feth.play.module.pa.user.AuthUser;
import datasources.Couchbase;
import play.db.ebean.Model;


public class LinkedAccountCB {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

//	public Long id;
//
//	public User user;

	public String providerUserId;
	public String providerKey;

    public static final CouchbaseClient client = Couchbase.getInstance();

    public static LinkedAccountCB create(final AuthUser authUser) {
		final LinkedAccountCB ret = new LinkedAccountCB();
		ret.update(authUser);
		return ret;
	}
	
	public void update(final AuthUser authUser) {
		this.providerKey = authUser.getProvider();
		this.providerUserId = authUser.getId();
	}

//    public static LinkedAccountCB findByProviderKey(final UserCB user, String key) {
//
//
//        String designDoc = "users";
//        String viewName = "by_firstname";
//        View view = client.getView(designDoc, viewName);
//
//// 2: Create a Query object to customize the Query
//        Query query = new Query();
//        query.setIncludeDocs(true); // Include the full document body
//
//// 3: Actually Query the View and return the results
//        ViewResponse response = client.query(view, query);
//
//
//        return find.where().eq("user", user).eq("providerKey", key)
//                .findUnique();
//    }

	public static LinkedAccountCB create(final LinkedAccountCB acc) {
		final LinkedAccountCB ret = new LinkedAccountCB();
		ret.providerKey = acc.providerKey;
		ret.providerUserId = acc.providerUserId;

		return ret;
	}
}