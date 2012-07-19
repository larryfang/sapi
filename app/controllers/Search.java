package controllers;


import actor.SearchActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorFactory;
import akka.routing.RoundRobinRouter;
import job.SapiSearch;
import play.libs.Akka;
import play.libs.F.Function;
import play.mvc.Controller;
import play.mvc.Result;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import static akka.pattern.Patterns.ask;
import static play.libs.Json.toJson;

public class Search extends Controller {
    private static final ActorRef router = Akka.system().actorOf(new Props(new UntypedActorFactory() {
        public UntypedActor create() {
            return new SearchActor();
        }
    }).withRouter(new RoundRobinRouter(1000)));

    public static Result search(String term, String location, String page) throws Exception {

        SapiSearch ssapi = new SapiSearch();

        SapiSearch.Response response = ssapi.search(term, location, Integer.parseInt(page), null);
        return ok(toJson(response));

    }

    public static Result fast(String term, String location, String page) throws MalformedURLException {
        Map<String, String> message = new HashMap<String, String>();
        message.put("page", page);
        message.put("term", term);
        message.put("location", location);

        return async(
                Akka.asPromise(ask(router, message, 10000)).map(
                        new Function<Object, Result>() {
                            public Result apply(Object response) {
                                return ok(toJson(response));
                            }
                        }
                )
        );
    }


}
