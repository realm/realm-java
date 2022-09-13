/**
 * Users with an email that contains `_noread_` do not have read access,
 * all others do.
 */
exports = async (partition) => {
  const email = context.user.data.email;
  if (email != undefined) {
    return(!email.includes("_noread_"));
  } else {
    return true;
  }
}
