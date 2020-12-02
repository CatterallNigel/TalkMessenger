package io.illuminates.communications.server.models;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Hashtable;
import java.util.Map;

@Service
public class Registry {

    private static Logger logger = LoggerFactory.getLogger(Registry.class);

    private static Registry.Services listedServices;

    public Registry(){
        listedServices = new Registry.Services();
        listedServices.setServices(servicesIn);
    }

    public static Services getListedServices() {
        return listedServices;
    }

    // Client UUID , Client ConnectionKey
    public static Map<String,String> clients = new Hashtable<String,String>();

    // Client ConnectionKey, Client UUID
    public static Map<String,String> connections = new Hashtable<String,String>();

    public static void cleanRegistry(String connectionKey){
        String userUUID = Registry.connections.get(connectionKey);
        Registry.clients.remove(userUUID);
        Registry.connections.remove(connectionKey);
        //TODO: De-register clients  non-persistent topics and make persistent topics isActive = false
        //TODO: Subscribtion - Catch they are inActive onSend - queue messages
        //TODO: Clean up any Inner Services of the client
    }

    //Maps a routeFrom to service type
    public static final Map<String,String> servicesIn = new Hashtable<String,String>(){{
        put("direct://services/in", "Services");
        put("direct://topics/in", "Topics");
        put("direct://relay/in", "Relay");
        put("direct://broadcast/in", "Broadcast");
        put("direct://publish/in", "Publish");
        put("direct://subscribe/in", "Subscribe");
        put("direct://flash/in", "Flash");
    }};

    //Maps a service type to a routeTo
    public static final Map<String,String> servicesOut = new Hashtable<String,String>(){{
        put( "Services", "direct://services/out");
        put( "Topics", "direct://topics/out");
        put( "Relay", "direct://relay/out");
        put( "Broadcast", "direct://broadcast/out");
        put( "Publish", "direct://publish/out");
        put( "Subscribe", "direct://subscribe/out");
        put( "Flash", "direct://flash/out");
    }};

    public class Services {

        public Map<String,String> services = new Hashtable<String,String>();
        public Services(){}

        public Map<String, String> getServices() {
            return services;
        }

        public void setServices(Map<String, String> services) {
            this.services = services;
        }
    }
}
