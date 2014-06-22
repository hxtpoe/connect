package models;

import be.objectify.deadbolt.core.models.Permission;
import be.objectify.deadbolt.core.models.Role;
import be.objectify.deadbolt.core.models.Subject;
import com.avaje.ebean.ExpressionList;
import com.couchbase.client.CouchbaseClient;
import com.feth.play.module.pa.providers.password.UsernamePasswordAuthUser;
import com.feth.play.module.pa.user.*;
import com.google.gson.Gson;
import datasources.Couchbase;
import play.data.format.Formats;
import play.data.validation.Constraints;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ExecutionException;


//public class User  implements Subject {
public class User implements Subject {
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

        User.client.add("u::" + Long.toString(this.id), gson.toJson(this));
        User.client.set("email::" + this.email, this.id);
        if (providerName != "password") {
            User.client.set(providerName + "::" + this.google, this.id);
        } else {
            User.client.set("username::" + this.name, this.id);
        }
    }

    public static final CouchbaseClient client = Couchbase.getInstance();

    public List<? extends Role> getRoles() {
        return roles;
    }

    @Override
    public List<? extends Permission> getPermissions() {
        return null;
    }

    @Override
    public String getIdentifier() {
         return Long.toString(id);
    }

    public static boolean existsByAuthUserIdentity(
            final AuthUserIdentity identity) {

            User u = findByProvider(identity);

        return u instanceof User;
    }

    //
//	private static ExpressionList<User> getAuthUserFind(
//			final AuthUserIdentity identity) {
//		return find.where().eq("active", true)
//				.eq("linkedAccounts.providerUserId", identity.getId())
//				.eq("linkedAccounts.providerKey", identity.getProvider());
//	}
//
    public static User findByAuthUserIdentity(final AuthUserIdentity identity) {
        if (identity == null) {
            return null;
        }
        if (identity instanceof UsernamePasswordAuthUser) {
            User user = findByUsernamePasswordIdentity((UsernamePasswordAuthUser) identity);
            return findByUsernamePasswordIdentity((UsernamePasswordAuthUser) identity);
        } else {


            Object uid = User.client.get(identity.getProvider() + "::" + identity.getId());
            Gson gson = new Gson();
            return gson.fromJson((String) User.client.get("u::" + uid), User.class);
        }
    }

    private static User findByProvider(final AuthUserIdentity identity) {
        Object uid = User.client.get(identity.getProvider() + "::" + identity.getId());

        if (uid == null) {
            return null;
        }
        Gson gson = new Gson();

        return gson.fromJson((String) User.client.get("u::" + uid), User.class);
    }

    ////
    public static User findByUsernamePasswordIdentity(
            final UsernamePasswordAuthUser identity) {

        User u = User.findByEmail(identity.getEmail());

        return u;
    }

    //
//	private static ExpressionList<User> getUsernamePasswordAuthUserFind(
//			final UsernamePasswordAuthUser identity) {
//		return getEmailUserFind(identity.getEmail()).eq(
//				"linkedAccounts.providerKey", identity.getProvider());
//	}
//
    public void merge(final User otherUser) {
//		for (final LinkedAccount acc : otherUser.linkedAccounts) {
//			this.linkedAccounts.add(LinkedAccount.create(acc));
//		}
//		// do all other merging stuff here - like resources, etc.
//
//		// deactivate the merged user that got added to this one
//		otherUser.active = false;
//		Ebean.save(Arrays.asList(new User[] { otherUser, this }));
    }

    //
    public static User create(final AuthUser authUser) {
        final User user = new User();

        SecurityRoleCB role = new SecurityRoleCB();
        role.roleName = controllers.Application.USER_ROLE;

        user.roles = Collections.singletonList(role);
        user.permissions = new ArrayList<String>();
        user.permissions.add(controllers.Application.USER_ROLE);

        user.active = true;
        user.lastLogin = new Date();

        user.id =  User.client.incr("u::count", 1);

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

        try {
            user.save(authUser.getProvider());
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return user;
    }

    public static void merge(final AuthUser oldUser, final AuthUser newUser) {
        User.findByAuthUserIdentity(oldUser).merge(
                User.findByAuthUserIdentity(newUser));
    }

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
//		final User u = User.findByAuthUserIdentity(oldUser);
//		u.linkedAccounts.add(LinkedAccount.create(newUser));
//		u.save();
//	}
//
    public static void setLastLoginDate(final AuthUser knownUser) {
		final User u = User.findByAuthUserIdentity(knownUser);
		u.lastLogin = new Date();
        try {
            u.save("password");
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static User findByEmail(final String email) {
        Object uid = User.client.get("email::" + email);
        Gson gson = new Gson();
        if (uid == null) {
            return null;
        }

        User u = gson.fromJson((String) User.client.get("u::" + uid), User.class);

        return u;
    }

    public static User findByUid(String uid) {
        Gson gson = new Gson();

        return gson.fromJson((String) User.client.get("u::" + uid), User.class);
    }

    public static void verify(final User unverified) {
        // You might want to wrap this into a transaction
        unverified.emailValidated = true;
        try {
            unverified.save("email");
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        TokenAction.deleteByUser(unverified, "EMAIL_VERIFICATION");
    }

    //
    public void changePassword(final UsernamePasswordAuthUser authUser,
                               final boolean create) {
        // @ToDO

    }

    public void resetPassword(final UsernamePasswordAuthUser authUser,
                              final boolean create) {
        // You might want to wrap this into a transaction
        this.changePassword(authUser, create);
        TokenAction.deleteByUser(this, "PASSWORD_RESET");
    }

    public Set<String> getProviders() {
        final Set<String> providerKeys = new HashSet<String>();
        return providerKeys;
    }


}
