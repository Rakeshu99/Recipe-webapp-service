0. Install Maven for your Operating System (https://maven.apache.org/install.html).

1. Execute the following command (inside a terminal within the directory where this code is located): mvnw spring-boot:run
 - This will compile and run the web service using the properties defined in application.properties (located in src/main/resources).

2. It can be accessed on http://localhost:8080
 - The following 4sample microservices have been implemented:
	-- Test app works:  	(GET) /
	-- Get all recipes: 	(GET) /recipes
	-- Add recipe: 		(POST) /recipe
	-- Delete recipe: 	(DELETE) /recipe/{name}
 - The default port is 8080. I would suggest to keep it. However, it can be changed in the application.properties file (by adding the line "server.port=9090", where 9090 would be the new port). Be mindful that this change would be needed to be replicated in the FrontEnd (as it currently ASSUMES the BackEnd to be located locally AND in the port 8080).
