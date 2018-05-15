# FIS Rest Endpoint with JDG integration

### Introduction

This example demonstrates how you to use FIS & JDG on openshift. The example is really simple: put a key/object in a remote cache and get the same key.
You'll need a running JDG server to run this example.

The FIS Rest

List of Operations
GET /payees/  -> List Payees


POST /payees/  -> Create a new Payee
Request Body:
{
  "id": 0,
  "name": "string",
  "bankName": "string",
  "accountNumber": "string"
}

GET /payees/payee/{id}  -> Details of a Payee by id
Response Body:
{
  "id": 0,
  "name": "string",
  "bankName": "string",
  "accountNumber": "string"
}

### Build
To build the whole sample you will need to run the pipeline
	
	$ oc create --save-config fis-rest-1-pipeline.yaml

For a full details on promoting API objects have a look on 
https://docs.openshift.com/container-platform/3.9/dev_guide/application_lifecycle/promoting_applications.html#dev-guide-promoting-applications-importing-api-object-state


You can build this example using:

    	$ mvn clean install

### Run

You can run this example using (you need to be loggedin to OCP first):

    $ mvn fabric:deploy


