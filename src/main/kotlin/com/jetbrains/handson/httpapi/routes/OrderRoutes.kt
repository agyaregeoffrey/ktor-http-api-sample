package com.jetbrains.handson.httpapi.routes

import com.jetbrains.handson.httpapi.models.Order
import com.jetbrains.handson.httpapi.models.OrderItem
import com.jetbrains.handson.httpapi.models.customerStorage
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*


val orderStorage = mutableListOf<Order>()
    val fakeOrders = listOf(Order(
    "2020-04-06-01", listOf(
        OrderItem("Ham Sandwich", 2, 5.50),
        OrderItem("Water", 1, 1.50),
        OrderItem("Beer", 3, 2.30),
        OrderItem("Cheesecake", 1, 3.75)
    )),
    Order("2020-04-03-01", listOf(
        OrderItem("Cheeseburger", 1, 8.50),
        OrderItem("Water", 2, 1.50),
        OrderItem("Coke", 2, 1.76),
        OrderItem("Ice Cream", 1, 2.35)
    ))
)

fun Route.listOrderRoute() {
    get("/order") {
        if (customerStorage.isNotEmpty()) {
            call.respond(orderStorage)
        }
    }
}

fun Route.getOrderRoute() {
    get("/order/{id}") {
        val id = call.parameters["number"] ?: call.respondText(
            "Missing or malformed order number",
            status = HttpStatusCode.BadRequest
        )
        val order = orderStorage.find {
            it.number == id
        } ?: return@get call.respondText(
            "No order with order number $id",
            status = HttpStatusCode.NotFound
        )
        call.respond(order)
    }
}

fun Route.totalizeOrderRoute() {
    get("/order/{id}/total") {
        val id = call.parameters["number"] ?: call.respondText(
            "Missing or malformed order number",
            status = HttpStatusCode.BadRequest
        )
        val order = orderStorage.find {
            it.number == id
        } ?: return@get call.respondText(
            "No order with order number $id",
            status = HttpStatusCode.NotFound
        )
        val total = order.contents.sumOf {
            it.quantity * it.price
        }
        call.respond(total)
    }
}

fun Route.orderRoutes() {
    route("/order") {
        get {
            if (orderStorage.isNotEmpty()) {
                call.respond(orderStorage)
            } else {
                call.respondText(
                    "No orders found",
                    status = HttpStatusCode.NotFound
                )
            }
        }
        get("{number}") {
            val number = call.parameters["number"] ?: call.respondText(
                "Missing or malformed order number",
                status = HttpStatusCode.BadRequest
            )
            val order = orderStorage.find {
                it.number == number
            } ?: return@get call.respondText(
                "No order with order number $number",
                status = HttpStatusCode.NotFound
            )
            call.respond(order)
        }
        post {
            val order = call.receive<Order>()
            orderStorage.add(order)
            call.respondText("Order added correctly", status = HttpStatusCode.Created)
        }
        delete("{number}") {
            val number = call.parameters["number"] ?: call.respondText(
                "Missing or malformed order number",
                status = HttpStatusCode.BadRequest
            )
            if (orderStorage.removeIf { it.number == number }) {
                call.respondText("Order removed correctly", status = HttpStatusCode.Accepted)
            } else {
                call.respondText("Not found", status = HttpStatusCode.NotFound)
            }
        }
    }
}