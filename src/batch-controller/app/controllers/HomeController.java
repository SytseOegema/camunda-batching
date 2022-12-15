package controllers;

import play.mvc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class HomeController extends Controller {

    /**
     * An action that renders an HTML page with a welcome message.
     * The configuration in the <code>routes</code> file means that
     * this method will be called when the application receives a
     * <code>GET</code> request with a path of <code>/</code>.
     */
    public Result index() {
        final Logger accessLogger = LoggerFactory.getLogger("access");
        accessLogger.warn("Lekker indexen in mijn super interessante endpoint");
        return ok(views.html.index.render());
    }

}
