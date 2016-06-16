# video-rental-store

Hybrid project of clojure/java. The alternative approach is to do this with maven
(https://joelholder.com/2015/10/23/how-to-cleanly-integrate-java-and-clojure-in-the-same-package/

## Usage

Leiningen needs to be installed (http://leiningen.org/)

Once installed, one can run tests with `lein midje` and run the server with `lein run`

The REST API exposes endpoints to manage film and customer resources and operations to rent.

Postman (http://www.getpostman.com/) for chrome is a great tool for exploring APIs.
In folder dev-resources of the project there are 3 suites of tests that can be imported in Postman
(they are not meant to be compatible with each other, you might want to restart the server between each suite or send
whichever individual requests you want to try from each collection).

The endpoints implement GET, POST, PUT and DELETE methods. They all use json media type as input and output.
These endpoints are:
/film (POST)
/film/:id (GET POST PUT DELETE)
/customer (POST)
/customer/:id (GET POST DELETE)
/customer/:cust-id/rent/:film-id (POST)
/customer/:cust-id/rent (POST) <-- for renting a list of films
TODO: these endpoints not exposed yet
/customer/:cust-id/return/:film-id (POST)
/customer/:cust-id/return/ (POST) <-- for list of films to return

## License

Copyright © 2016

Distributed under the Eclipse Public License either version 1.0 or any later version.
