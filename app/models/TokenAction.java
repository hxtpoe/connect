package models;

import java.util.Date;

import com.couchbase.client.CouchbaseClient;
import com.google.gson.Gson;
import datasources.Couchbase;
import play.data.format.Formats;

public class TokenAction  {
	private static final long serialVersionUID = 1L;

	/**
	 * Verification time frame (until the user clicks on the link in the email)
	 * in seconds
	 * Defaults to one week
	 */
	private final static long VERIFICATION_TIME = 7 * 24 * 3600;

	public String token;

	public String targetUser;

	public String type;

	@Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date created;

	@Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date expires;

    public static final CouchbaseClient client = Couchbase.getInstance();

	public static TokenAction findByToken(final String token, final String type) {
        Object uid = TokenAction.client.get("token::" + token);
        Gson gson = new Gson();
        return gson.fromJson((String) uid, TokenAction.class);
	}

	public static void deleteByUser(final User u, final String type) {
        //@ToDO implement
	}

	public boolean isValid() {
		return this.expires.after(new Date());
	}

	public static TokenAction create(final String type, final String token,
			final User targetUser) {

		final TokenAction ua = new TokenAction();
        ua.targetUser = Long.toString(targetUser.id);
        ua.token = token;
        ua.type = type;
        final Date created = new Date();
        ua.created = created;
        ua.expires = new Date(created.getTime() + VERIFICATION_TIME * 1000);

        Gson gson = new Gson();
        TokenAction.client.add("token::" + ua.token, gson.toJson(ua));

		return ua;
	}
}
