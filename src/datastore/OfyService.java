package datastore;

import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;


public class OfyService {
    static {
    	ObjectifyService.register(Rule.class);
    	ObjectifyService.register(Schema.class);
        ObjectifyService.register(Player.class);
        ObjectifyService.register(Mission.class);
    }

    public static void ofy() {
        ObjectifyService.begin();
    }

    public static ObjectifyFactory factory() {
        return ObjectifyService.factory();
    }

}