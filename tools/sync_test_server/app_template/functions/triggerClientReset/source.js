exports = function(arg){
  const mongodb = context.services.get("BackingDB");
  const clientFiles = mongodb.db("__realm_sync").collection("clientfiles");
   
  const result = clientFiles.deleteOne({ownerId: arg})
   
  return {arg: result};
};