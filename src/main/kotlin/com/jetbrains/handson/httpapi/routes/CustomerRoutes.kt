package com.jetbrains.handson.httpapi.routes

import com.jetbrains.handson.httpapi.models.Customer
import com.jetbrains.handson.httpapi.models.customerStorage
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun Route.customerRouting() {
    route("/customer") {
        // Gets all customers from our in-memory storage
        get {
            if (customerStorage.isNotEmpty()) {
                call.respond(customerStorage)
            } else {
                call.respondText("No customers found", status = HttpStatusCode.NotFound)
            }
        }
        // Get customer by id
        get("{id}") {
            // Verify if id exists or is valid
            val id = call.parameters["id"] ?: call.respondText(
                "Missing or malformed id",
                status = HttpStatusCode.BadRequest
            )
            val customer = customerStorage.find {
                it.id == id
            } ?: return@get call.respondText(
                "No customer with id $id",
                status = HttpStatusCode.NotFound
            )
            call.respond(customer)
        }
        post {
            val customer = call.receive<Customer>()
            customerStorage.add(customer)
            call.respondText("Customer added correctly", status = HttpStatusCode.Created)
        }
        delete("{id}") {
            // Verify if id exists or valid
            val id = call.parameters["id"] ?: call.respondText(
                "Missing or malformed id",
                status = HttpStatusCode.BadRequest
            )
            if (customerStorage.removeIf { it.id == id }) {
                call.respondText("Customer removed correctly", status = HttpStatusCode.Accepted)
            } else {
                call.respondText("Not found", status = HttpStatusCode.NotFound)
            }
        }
    }
}

/**
 * POST http://127.0.0.1:8080/customer
Content-Type: application/json

{
"id": "100",
"firstName": "Jane",
"lastName": "Smith",
"email": "jane.smith@company.com"
}


###
POST http://127.0.0.1:8080/customer
Content-Type: application/json

{
"id": "200",
"firstName": "John",
"lastName": "Smith",
"email": "john.smith@company.com"
}

###
POST http://127.0.0.1:8080/customer
Content-Type: application/json

{
"id": "300",
"firstName": "Mary",
"lastName": "Smith",
"email": "mary.smith@company.com"
}


###
GET http://127.0.0.1:8080/customer
Accept: application/json

###
GET http://127.0.0.1:8080/customer/200

###
GET http://127.0.0.1:8080/customer/500

###
DELETE http://127.0.0.1:8080/customer/100

###
DELETE http://127.0.0.1:8080/customer/500
 */