/**
 * Users with an email that contains `_nowrite_` do not have write access,
 * all others do.
 */
exports = async (partition) => {
  const email = context.user.data.email;
  return(!email.includes("_nowrite_"));
}
