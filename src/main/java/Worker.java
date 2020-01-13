import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;

public class Worker extends AbstractActor {
    // ActorSystem logger instance
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(),this);
    private ActorRef resultGatherer;


    public Worker(ActorRef resultGatherer)
    {
        this.resultGatherer = resultGatherer;
    }

    static Props props(ActorRef resultGatherer)
    {
        return Props.create(Worker.class, () -> new Worker(resultGatherer));
    }

    @Override
    public AbstractActor.Receive createReceive(){
        ReceiveBuilder builder = ReceiveBuilder.create();

        builder.match(Person.class, p -> {
            log.info("Recieved person: " + p.toStringShort());
            if(p.streetNumLessThan3Digits()) { // If less than 3 digits
                p.calculateHash(1_000_000);
                log.info("Hash calculated, sending to ResultGatherer...");
                resultGatherer.tell(p, this.getSelf());
            }
        });

        builder.matchAny(o -> log.info("Recieved unknown message"));

        return builder.build();
    }

    // Startup
    @Override
    public void preStart() {
        log.info("Worker actor started!");
    }

    // Exit message
    @Override
    public void postStop() {
        log.info("Worker actor stopped!");
    }
}
