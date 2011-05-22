# RESTclient-Android
This library is designed to greatly assist consuming RESTful webservices.  Originally I started by using the library listed in the contributions.  I then modified to DRY up the script, added PUT and DELETE verbs, HTTP basic authentication, and a JSON body.

## Usage
The code below is an example using a GET request to grab JSON from the server

	RestClient client = new RestClient(webServiceUrl);
	client.addBasicAuthentication(username, password);
	try {
		client.execute(RequestMethod.GET);
		if (client.getResponseCode() != 200) {
			//return server error
			return client.getErrorMessage();
		}
		//return valid data
		JSONObject jObj = new JSONObject(client.getResponse());
		return jObj.toString();
	} catch(Exception e) {
		return e.toString();
	}

## Contributions
http://lukencode.com/2010/04/27/calling-web-services-in-android-using-httpclient/

## License
Release under the Apache 2 license.