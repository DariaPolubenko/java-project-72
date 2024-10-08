package hexlet.code.controllers;

import hexlet.code.dto.BasePage;
import hexlet.code.dto.UrlPage;
import hexlet.code.dto.UrlsPage;
import hexlet.code.model.Url;
import hexlet.code.repository.CheckRepository;
import hexlet.code.repository.UrlRepository;
import hexlet.code.utils.NamedRoutes;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.net.URI;

import static io.javalin.rendering.template.TemplateUtil.model;

public class UrlsController {
    public static void mainPage(Context ctx) {
        var page = new BasePage();
        page.setFlash(ctx.consumeSessionAttribute("flash"));
        page.setFlashType(ctx.consumeSessionAttribute("flash-type"));
        ctx.render("build.jte", model("page", page));
    }

    public static void create(Context ctx) {
        var name = ctx.formParam("url");
        try {
            var url = getUrl(name);

            if (UrlRepository.search(url).isEmpty()) {
                var resultUrl = new Url(url);
                UrlRepository.save(resultUrl);
                ctx.sessionAttribute("flash", "Сайт добавлен!");
                ctx.sessionAttribute("flash-type", "success");
                ctx.redirect(NamedRoutes.urlsPath());
            } else {
                ctx.sessionAttribute("flash", "Сайт уже существует");
                ctx.sessionAttribute("flash-type", "primary");
                ctx.redirect(NamedRoutes.urlsPath());
            }
        } catch (Exception e) {
            ctx.sessionAttribute("flash", "Некорректный URL");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.redirect(NamedRoutes.mainPath());
        }
    }

    public static void show(Context ctx) throws SQLException {
        var urls = UrlRepository.getEntities();
        var checks = CheckRepository.getLastChecks();
        var page = new UrlsPage(urls, checks);
        page.setFlash(ctx.consumeSessionAttribute("flash"));
        page.setFlashType(ctx.consumeSessionAttribute("flash-type"));
        ctx.render("index.jte", model("page", page));
    }

    public static void find(Context ctx) throws SQLException {
        var urlId = ctx.pathParamAsClass("id", Long.class).get();
        var url = UrlRepository.find(urlId).orElseThrow(() -> new NotFoundResponse("Сайт не найден"));

        var check = CheckRepository.findCheck(urlId);
        var page = new UrlPage(url, check);
        page.setFlash(ctx.consumeSessionAttribute("flash"));
        page.setFlashType(ctx.consumeSessionAttribute("flash-type"));
        ctx.render("show.jte", model("page", page));
    }

    public static String getUrl(String name) throws URISyntaxException, MalformedURLException {
        var url = new URI(name).toURL();
        var normalizedUrl = UriComponentsBuilder.newInstance()
                .scheme(url.getProtocol())
                .host(url.getHost())
                .port(url.getPort())
                .build()
                .toUri()
                .toURL()
                .toString();
        return normalizedUrl;
    }
}
