package actor;

import akka.actor.UntypedActor;
import job.SapiSearch;

import java.util.Map;

public class SearchActor extends UntypedActor {


    @Override
    public void onReceive(Object message) throws Exception {

        Map<String, String> messageMap = (Map<String, String>) message;
        int page = Integer.parseInt(messageMap.get("page"));


        SapiSearch.Response response = new SapiSearch().search(messageMap.get("term"), messageMap.get("location"), page, null);

        getSender().tell(response);

    }

}
