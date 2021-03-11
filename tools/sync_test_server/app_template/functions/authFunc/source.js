
  /*

    This function will be run when a user logs in with this provider.

    The return object must contain a string id, this string id will be used to login with an existing
    or create a new user. This is NOT the Stitch user id, but it is the id used to identify which user has
    been created or logged in with. 

    If an error is thrown within the function the login will fail.

    The default function provided below will always result in failure.
  */

  exports = (loginPayload) => {
    return loginPayload["realmCustomAuthFuncUserId"];
  };
