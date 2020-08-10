// This function is the webhook's request handler.
exports = async function(payload, response) {
    let timestamp = new Date().toISOString()
    console.log( "time: ", timestamp)

    // Get data from query 
    let query = payload["query"]

    console.log( "data: ", data)
    
    // Decode data
    let buffer = new Buffer(query["data"], "base64")
    let data = buffer.toString('ascii')
    let event = JSON.parse(data)

    // Extract required fields
    let binding = event["properties"]["Binding"]
    let token = event["properties"]["token"]
    
    console.log("timestamp: ", timestamp)
    console.log("binding: ", binding)
    
    // Submit to S3
    // FIXME How to provision these
    let serviceName = "metric_s3_upload"
    let bucketName = "rorbech";
    let bucketRegion = "eu-central-1"
    
    const s3 = context.services.get(serviceName).s3(bucketRegion);
    ;
    const result = await s3.PutObject({
      "Bucket": bucketName,
      "Key": binding + '-' + timestamp + '-' + token,
      "Body": data
    })

    console.log(EJSON.stringify(result))

    return "";
};