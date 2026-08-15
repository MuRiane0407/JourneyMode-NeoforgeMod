package com.muriane.journeymode;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class Config {
    public static final ModConfigSpec serverSpec;
    public static final Server SERVER;
    public static final ModConfigSpec clientSpec;
    public static final Client CLIENT;

    static {
        final Pair<Server, ModConfigSpec> specServerPair = new ModConfigSpec.Builder().configure(Server::new);
        serverSpec = specServerPair.getRight();
        SERVER = specServerPair.getLeft();
        final Pair<Client, ModConfigSpec> specClientPair = new ModConfigSpec.Builder().configure(Client::new);
        clientSpec = specClientPair.getRight();
        CLIENT = specClientPair.getLeft();
    }

    public static class Server {
        public final ModConfigSpec.DoubleValue RESEARCH_DEMAND_MULTIPLIER;

        Server(ModConfigSpec.Builder builder){
            builder.push("copy");
            RESEARCH_DEMAND_MULTIPLIER = builder
                    .translation("journeymode.configuration.copy.research_demand_multiplier")
                    .defineInRange("research_demand_multiplier", 1.0, 0, 1024);
            builder.pop();
        }
    }

    public static class Client {
        Client(ModConfigSpec.Builder builder){

        }
    }
}
