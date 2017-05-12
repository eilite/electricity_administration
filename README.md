
# electricity_administration
This project is an imlementation for a work sample.

It is a REST API that can be used to manage users electricity powerstations built with scala and play framework.

## Run the API
First create a mysql database and run the [electricity_monitoring.sql](https://github.com/eilite/electricity_administration/blob/master/etc/electricity_monitoring.sql)

Change configurations in `application.conf`

To run the api in dev mode (port 9000)
```
sbt run 
```


To try the API, you can import this postman collection [powerstations.postman_collection.json](https://github.com/eilite/electricity_administration/blob/master/etc/powerstations.postman_collection.json)
