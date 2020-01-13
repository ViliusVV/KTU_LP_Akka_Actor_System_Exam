import akka.actor.AbstractActor;
import akka.actor.PoisonPill;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import akka.routing.Broadcast;

import java.io.FileWriter;
import java.io.IOException;

public class ResultGatherer extends  AbstractActor{
    // ActorSystem logger instance
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(),this);
    private final String  OUTPUT_FILE= "data/IFF7-4_ValinskisV_E1_res.txt";
    private final FileWriter writer = new FileWriter(OUTPUT_FILE);


    public ResultGatherer() throws IOException
    {
        writer.write(Person.tableHeader());
    }


    @Override
    public AbstractActor.Receive createReceive(){
        ReceiveBuilder builder = ReceiveBuilder.create();

        // Match Person object
        builder.match(Person.class, p -> {
            log.info("Writing person to file: " +  p.toStringShort());
            writer.write('\n' + p.toString());

        });

        // Match "finished" message
        builder.matchEquals("finished", s -> {
            log.info("Recieved finished, stopping system...");
            this.getContext().system().terminate();
        });

        // Match everything
        builder.matchAny(o -> log.info("Recieved unknown message"));

        return builder.build();
    }

    // Startup
    @Override
    public void preStart() {
        log.info("ResultGatherer actor started!");
    }

    // Exit message
    @Override
    public void postStop() throws  IOException{
        log.info("Closing file!");
        writer.close(); // Close file before exiting
        log.info("ResultGatherer actor stopped!");
    }
}
