package models;

import be.objectify.deadbolt.core.models.Permission;
import be.objectify.deadbolt.core.models.Role;
import be.objectify.deadbolt.core.models.Subject;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionList;
import com.couchbase.client.CouchbaseClient;
import com.feth.play.module.pa.providers.password.UsernamePasswordAuthUser;
import com.feth.play.module.pa.user.*;
import com.google.gson.Gson;
import datasources.Couchbase;
import models.TokenAction.Type;
import play.data.format.Formats;
import play.data.validation.Constraints;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ExecutionException;


//public class UserCB  implements Subject {
public class UserCB {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public Long id;

    // if you make this unique, keep in mind that users *must* merge/link their
    // accounts then on signup with additional providers
    // @Column(unique = true)
    public String email;

    public String name;

    public String firstName;

    public String lastName;

    public String google;
    public String password;

    @Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date lastLogin;

    public boolean active;

    public boolean emailValidated;

    public List<SecurityRoleCB> roles;

    public List<String> permissions;


    public void save(String providerName) throws ExecutionException, InterruptedException {

        Gson gson = new Gson();

        Long uid = client.incr("u::count", 1);

        UserCB.client.add("u::" + Long.toString(uid), gson.toJson(this));
        UserCB.client.set("email::" + this.email, uid);
        if (providerName != "password") {
            UserCB.client.set(providerName + "::" + this.google, uid);
        } else {
            UserCB.client.set("username::" + this.name, uid);
        }


    }

    public static final CouchbaseClient client = Couchbase.getInstance();

    //	@Override
//	public String getIdentifier()
//	{
//		return Long.toString(id);
//	}
//
    public List<? extends Role> getRoles() {
        return roles;
    }

    //
//	@Override
//	public List<? extends Permission> getPermissions() {
//		return permissions;
//	}
//
//	public static boolean existsByAuthUserIdentity(
//			final AuthUserIdentity identity) {
//		final ExpressionList<UserCB> exp;
//		if (identity instanceof UsernamePasswordAuthUser) {
//			exp = getUsernamePasswordAuthUserFind((UsernamePasswordAuthUser) identity);
//		} else {
//			exp = getAuthUserFind(identity);
//		}
//		return exp.findRowCount() > 0;
//	}
//
//	private static ExpressionList<UserCB> getAuthUserFind(
//			final AuthUserIdentity identity) {
//		return find.where().eq("active", true)
//				.eq("linkedAccounts.providerUserId", identity.getId())
//				.eq("linkedAccounts.providerKey", identity.getProvider());
//	}
//
//	public static UserCB findByAuthUserIdentity(final AuthUserIdentity identity) {
//		if (identity == null) {
//			return null;
//		}
//		if (identity instanceof UsernamePasswordAuthUser) {
//			return findByUsernamePasswordIdentity((UsernamePasswordAuthUser) identity);
//		} else {
//			return getAuthUserFind(identity).findUnique();
//		}
//	}
//
//	public static UserCB findByUsernamePasswordIdentity(
//			final UsernamePasswordAuthUser identity) {
//		return getUsernamePasswordAuthUserFind(identity).findUnique();
//	}
//
//	private static ExpressionList<UserCB> getUsernamePasswordAuthUserFind(
//			final UsernamePasswordAuthUser identity) {
//		return getEmailUserFind(identity.getEmail()).eq(
//				"linkedAccounts.providerKey", identity.getProvider());
//	}
//
//	public void merge(final UserCB otherUser) {
//		for (final LinkedAccount acc : otherUser.linkedAccounts) {
//			this.linkedAccounts.add(LinkedAccount.create(acc));
//		}
//		// do all other merging stuff here - like resources, etc.
//
//		// deactivate the merged user that got added to this one
//		otherUser.active = false;
//		Ebean.save(Arrays.asList(new UserCB[] { otherUser, this }));
//	}
//
    public static UserCB create(final AuthUser authUser) {
        final UserCB user = new UserCB();

        SecurityRoleCB role = new SecurityRoleCB();
        role.roleName = controllers.Application.USER_ROLE;

        user.roles = Collections.singletonList(role);
        user.permissions = new ArrayList<String>();
        user.permissions.add(controllers.Application.USER_ROLE);

        user.active = true;
        user.lastLogin = new Date();

        Field field = null;
        try {
            field = user.getClass().getDeclaredField(authUser.getProvider());
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        field.setAccessible(true);
        try {
            field.set(user, authUser.getId());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        if (authUser instanceof EmailIdentity) {
            final EmailIdentity identity = (EmailIdentity) authUser;
            // Remember, even when getting them from FB & Co., emails should be
            // verified within the application as a security breach there might
            // break your security as well!
            user.email = identity.getEmail();
            user.emailValidated = false;
        }

        if (authUser instanceof NameIdentity) {
            final NameIdentity identity = (NameIdentity) authUser;
            final String name = identity.getName();
            if (name != null) {
                user.name = name;
            }
        }

        if (authUser instanceof FirstLastNameIdentity) {
            final FirstLastNameIdentity identity = (FirstLastNameIdentity) authUser;
            final String firstName = identity.getFirstName();
            final String lastName = identity.getLastName();
            if (firstName != null) {
                user.firstName = firstName;
            }
            if (lastName != null) {
                user.lastName = lastName;
            }
        }

//		user.saveManyToManyAssociations("roles");
        // user.saveManyToManyAssociations("permissions");
        return user;
    }

//	public static void merge(final AuthUser oldUser, final AuthUser newUser) {
//		UserCB.findByAuthUserIdentity(oldUser).merge(
//				UserCB.findByAuthUserIdentity(newUser));
//	}

    //	public Set<String> getProviders() {
//		final Set<String> providerKeys = new HashSet<String>(
//				linkedAccounts.size());
//		for (final LinkedAccountCB acc : linkedAccounts) {
//			providerKeys.add(acc.providerKey);
//		}
//		return providerKeys;
//	}
//
//	public static void addLinkedAccount(final AuthUser oldUser,
//			final AuthUser newUser) {
//		final UserCB u = UserCB.findByAuthUserIdentity(oldUser);
//		u.linkedAccounts.add(LinkedAccount.create(newUser));
//		u.save();
//	}
//
    public static void setLastLoginDate(final AuthUser knownUser) {
//		final UserCB u = UserCB.findByAuthUserIdentity(knownUser);
//		u.lastLogin = new Date();
//		u.save();
    }

    public static UserCB findByEmail(final String email) {
        Object uid = UserCB.client.get("email::" + email);
        Gson gson = new Gson();
        UserCB user = gson.fromJson((String) UserCB.client.get("u::" + uid), UserCB.class);

        return user;
    }


    //
//	private static ExpressionList<UserCB> getEmailUserFind(final String email) {
//		return find.where().eq("active", true).eq("email", email);
//	}
//
//	public UserCB getAccountByProvider(final UsernamePasswordAuthUser authUser) {
//
//
//
//
//		return LinkedAccountCB.findByProviderKey(this, providerKey);
//	}
//
//	public static void verify(final UserCB unverified) {
//		// You might want to wrap this into a transaction
//		unverified.emailValidated = true;
//		unverified.save();
//		TokenAction.deleteByUser(unverified, Type.EMAIL_VERIFICATION);
//	}
//
    public void changePassword(final UsernamePasswordAuthUser authUser,
                               final boolean create) {
//		LinkedAccount a = this.getAccountByProvider(authUser);
//		if (a == null) {
//			if (create) {
//				a = LinkedAccount.create(authUser);
//				a.user = this;
//			} else {
//				throw new RuntimeException(
//						"Account not enabled for password usage");
//			}
//		}
//		a.providerUserId = authUser.getHashedPassword();
//		a.save();
    }
//
//	public void resetPassword(final UsernamePasswordAuthUser authUser,
//			final boolean create) {
//		// You might want to wrap this into a transaction
//		this.changePassword(authUser, create);
//		TokenAction.deleteByUser(this, Type.PASSWORD_RESET);
//	}
}
