exports = function (appId, userId) {

  const clientFiles = getDb(appId).collection("clientfiles");

  const result = clientFiles.deleteOne({ ownerId: userId })

  return result;
};

function getDb(appId) {
  const mongodb = context.services.get("BackingDB");

  try {
    return mongodb.db(`__realm_sync_${appId}`);
  } catch (error) {
    return mongodb.db("__realm_sync");
  }
}