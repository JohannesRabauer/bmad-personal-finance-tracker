package com.bmad.finance;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;

/**
 * Serves the transaction pages: the logged-in user's own list plus an entry
 * form, and the login page. Every read and write is scoped to the current
 * authenticated principal.
 */
@Path("/")
public class TransactionResource {

    @Inject
    SecurityIdentity identity;

    @CheckedTemplate
    static class Templates {
        static native TemplateInstance transactions(String user, List<Transaction> transactions,
                                                     TransactionType[] types, Category[] categories,
                                                     String today, String error);

        static native TemplateInstance login(boolean error);
    }

    private String currentUser() {
        return identity.getPrincipal().getName();
    }

    @GET
    @Path("transactions")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance list() {
        return render(null);
    }

    @POST
    @Path("transactions")
    @Transactional
    @Produces(MediaType.TEXT_HTML)
    public Response create(@FormParam("type") String typeParam,
                           @FormParam("amount") String amountParam,
                           @FormParam("date") String dateParam,
                           @FormParam("category") String categoryParam) {
        TransactionType type;
        Category category;
        try {
            type = TransactionType.valueOf(typeParam);
            category = Category.valueOf(categoryParam);
        } catch (IllegalArgumentException | NullPointerException e) {
            return Response.ok(render("Please choose a valid type and category.")).build();
        }

        BigDecimal amount;
        try {
            amount = new BigDecimal(amountParam.trim());
        } catch (NumberFormatException | NullPointerException e) {
            return Response.ok(render("Amount must be a number.")).build();
        }
        if (amount.signum() <= 0) {
            return Response.ok(render("Amount must be greater than zero.")).build();
        }
        if (amount.scale() > 2) {
            return Response.ok(render("Amount can have at most two decimal places.")).build();
        }
        // Store money at a fixed EUR scale so it persists and displays consistently.
        amount = amount.setScale(2);

        LocalDate date;
        try {
            date = (dateParam == null || dateParam.isBlank())
                    ? LocalDate.now()
                    : LocalDate.parse(dateParam.trim());
        } catch (DateTimeParseException e) {
            return Response.ok(render("Date is not valid.")).build();
        }

        new Transaction(currentUser(), type, amount, date, category).persist();
        return Response.seeOther(URI.create("/transactions")).build();
    }

    private TemplateInstance render(String error) {
        return Templates.transactions(
                currentUser(),
                Transaction.listForOwner(currentUser()),
                TransactionType.values(),
                Category.values(),
                LocalDate.now().toString(),
                error);
    }

    @GET
    @Path("login")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance login() {
        return Templates.login(false);
    }

    @GET
    @Path("login-failed")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance loginFailed() {
        return Templates.login(true);
    }

    /**
     * Logs the user out by expiring the form-auth session cookie, then returns
     * to the login page. (Quarkus form auth has no built-in logout endpoint.)
     */
    @GET
    @Path("logout")
    public Response logout() {
        NewCookie expired = new NewCookie.Builder("quarkus-credential")
                .value("")
                .path("/")
                .maxAge(0)
                .expiry(new Date(0))
                .httpOnly(true)
                .build();
        return Response.seeOther(URI.create("/login")).cookie(expired).build();
    }
}
