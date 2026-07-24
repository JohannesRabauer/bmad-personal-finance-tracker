package com.bmad.finance;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.filter.cookie.CookieFilter;
import jakarta.transaction.Transactional;

@QuarkusTest
class TransactionResourceTest {

    private static final String FORM = "application/x-www-form-urlencoded";

    // --- Matrix: unauthenticated GET /transactions -> redirect to login ---
    @Test
    void unauthenticatedIsRedirectedToLogin() {
        given()
            .redirects().follow(false)
        .when()
            .get("/transactions")
        .then()
            .statusCode(302)
            .header("location", containsString("/login"));
    }

    // --- Matrix: bad credentials -> redirected to the login failure page,
    //     which renders an explicit error message ---
    @Test
    void badCredentialsRedirectToFailurePageAndShowError() {
        given()
            .redirects().follow(false)
            .contentType(FORM)
            .formParam("j_username", "alice")
            .formParam("j_password", "wrong-password")
        .when()
            .post("/j_security_check")
        .then()
            .statusCode(302)
            .header("location", containsString("/login-failed"));

        given()
        .when()
            .get("/login-failed")
        .then()
            .statusCode(200)
            .body(containsString("Invalid username or password"));
    }

    // --- Matrix (end-to-end): a real form login with embedded credentials,
    //     session cookie, then submit and see the transaction ---
    @Test
    void realFormLoginPersistsAndListsTransaction() {
        CookieFilter cookies = new CookieFilter();

        given()
            .filter(cookies)
            .redirects().follow(false)
            .contentType(FORM)
            .formParam("j_username", "bob")
            .formParam("j_password", "bob-pw")
        .when()
            .post("/j_security_check")
        .then()
            .statusCode(302);

        given()
            .filter(cookies)
            .redirects().follow(false)
            .contentType(FORM)
            .formParam("type", "OUT")
            .formParam("amount", "7.30")
            .formParam("date", LocalDate.now().toString())
            .formParam("category", "DINING")
        .when()
            .post("/transactions")
        .then()
            .statusCode(303)
            .header("location", containsString("/transactions"));

        given()
            .filter(cookies)
        .when()
            .get("/transactions")
        .then()
            .statusCode(200)
            .body(containsString("Dining"))
            .body(containsString("7.30"));
    }

    // --- Matrix: authed user logs a valid OUT; persisted fields are exact ---
    @Test
    @TestSecurity(user = "heidi", roles = "user")
    void validOutPersistsWithExactFields() {
        given()
            .redirects().follow(false)
            .contentType(FORM)
            .formParam("type", "OUT")
            .formParam("amount", "12.50")
            .formParam("date", "2026-07-24")
            .formParam("category", "GROCERIES")
        .when()
            .post("/transactions")
        .then()
            .statusCode(303)
            .header("location", containsString("/transactions"));

        List<Transaction> rows = QuarkusTransaction.requiringNew()
                .call(() -> Transaction.listForOwner("heidi"));
        assertEquals(1, rows.size());
        Transaction t = rows.get(0);
        assertEquals("heidi", t.owner);
        assertEquals(TransactionType.OUT, t.type);
        assertEquals(0, new BigDecimal("12.50").compareTo(t.amount));
        assertEquals(LocalDate.parse("2026-07-24"), t.date);
        assertEquals(Category.GROCERIES, t.category);
    }

    // --- Matrix: authed user logs a valid IN ---
    @Test
    @TestSecurity(user = "ivan", roles = "user")
    void validIncomePersists() {
        given()
            .redirects().follow(false)
            .contentType(FORM)
            .formParam("type", "IN")
            .formParam("amount", "2000")
            .formParam("date", LocalDate.now().toString())
            .formParam("category", "SALARY")
        .when()
            .post("/transactions")
        .then()
            .statusCode(303);

        given()
        .when()
            .get("/transactions")
        .then()
            .statusCode(200)
            .body(containsString("Salary"))
            // "2000" is normalized to two decimal places on display.
            .body(containsString("2000.00"));
    }

    // --- Matrix: non-positive amount is rejected, nothing persisted ---
    @Test
    @TestSecurity(user = "carol", roles = "user")
    void nonPositiveAmountIsRejected() {
        given()
            .redirects().follow(false)
            .contentType(FORM)
            .formParam("type", "OUT")
            .formParam("amount", "0")
            .formParam("date", LocalDate.now().toString())
            .formParam("category", "GROCERIES")
        .when()
            .post("/transactions")
        .then()
            .statusCode(200)
            .body(containsString("greater than zero"));

        assertEquals(0L, countFor("carol"));
    }

    // --- Matrix: missing amount is rejected, nothing persisted ---
    @Test
    @TestSecurity(user = "judy", roles = "user")
    void missingAmountIsRejected() {
        given()
            .redirects().follow(false)
            .contentType(FORM)
            .formParam("type", "OUT")
            .formParam("date", LocalDate.now().toString())
            .formParam("category", "GROCERIES")
        .when()
            .post("/transactions")
        .then()
            .statusCode(200)
            .body(containsString("Amount"));

        assertEquals(0L, countFor("judy"));
    }

    // --- Non-numeric / over-precision amounts are rejected, nothing persisted ---
    @Test
    @TestSecurity(user = "mike", roles = "user")
    void invalidAmountFormatsAreRejected() {
        postAmount("abc").then().statusCode(200).body(containsString("Amount must be a number"));
        postAmount("12.501").then().statusCode(200).body(containsString("two decimal places"));
        assertEquals(0L, countFor("mike"));
    }

    // --- Matrix (over HTTP): list shows only the caller's own rows ---
    @Test
    @TestSecurity(user = "frank", roles = "user")
    void listShowsOnlyOwnRowsOverHttp() {
        // Another user's row, seeded directly, must never appear for frank.
        QuarkusTransaction.requiringNew().run(() ->
                new Transaction("grace", TransactionType.IN, new BigDecimal("999.99"),
                        LocalDate.now(), Category.SALARY).persist());

        given()
            .redirects().follow(false)
            .contentType(FORM)
            .formParam("type", "OUT")
            .formParam("amount", "3.33")
            .formParam("date", LocalDate.now().toString())
            .formParam("category", "DINING")
        .when()
            .post("/transactions")
        .then()
            .statusCode(303);

        given()
        .when()
            .get("/transactions")
        .then()
            .statusCode(200)
            .body(containsString("3.33"))
            .body(not(containsString("999.99")));
    }

    // --- logout expires the session and returns to the login page ---
    @Test
    @TestSecurity(user = "alice", roles = "user")
    void logoutRedirectsToLogin() {
        given()
            .redirects().follow(false)
        .when()
            .get("/logout")
        .then()
            .statusCode(303)
            .header("location", containsString("/login"));
    }

    // --- listForOwner is scoped to a single user (repository level) ---
    @Test
    @Transactional
    void listForOwnerIsScopedToOwner() {
        new Transaction("dave", TransactionType.OUT, new BigDecimal("5.00"),
                LocalDate.now(), Category.DINING).persist();
        new Transaction("erin", TransactionType.IN, new BigDecimal("9.00"),
                LocalDate.now(), Category.SALARY).persist();

        List<Transaction> daveRows = Transaction.listForOwner("dave");
        assertEquals(1, daveRows.size());
        assertTrue(daveRows.stream().allMatch(t -> t.owner.equals("dave")));
    }

    private static long countFor(String owner) {
        return QuarkusTransaction.requiringNew().call(() -> Transaction.count("owner", owner));
    }

    private static io.restassured.response.Response postAmount(String amount) {
        return given()
                .redirects().follow(false)
                .contentType(FORM)
                .formParam("type", "OUT")
                .formParam("amount", amount)
                .formParam("date", LocalDate.now().toString())
                .formParam("category", "GROCERIES")
            .when()
                .post("/transactions");
    }
}
