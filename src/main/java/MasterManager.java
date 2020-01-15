import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import akka.routing.Broadcast;
import akka.routing.SmallestMailboxPool;

public class MasterManager extends AbstractActor {
    // ActorSystem logger instance
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(),this);

    private final static int WORKER_COUNT = 4;
    // Result gatherer actor
    private final ActorRef resultGatherer = this.getContext().actorOf(Props.create(ResultGatherer.class), "resultGatherer");
    // Worker router, including its worker children
    private final ActorRef workerRouter = this.getContext().actorOf(new SmallestMailboxPool(WORKER_COUNT).props(Worker.props(resultGatherer)), "WorkerRouter");

    public MasterManager()
    {
        this.getContext().watch(workerRouter);
    }

    @Override
    public Receive createReceive(){
        ReceiveBuilder builder = ReceiveBuilder.create();

        // Match person object
        builder.match(Person.class, p -> {
            log.info("Recieved person: " +  p.toStringShort());
            workerRouter.tell(p, this.getSelf());
        });

        // Match "finished" message
        builder.matchEquals("finished", s -> {
            log.info("Recieved finished, sending poison to router...");
            workerRouter.tell(new Broadcast(PoisonPill.getInstance()), this.getSelf());

        });

        // Match Terminated event (watch workerRouter)
        builder.match(Terminated.class, t -> t.actor().equals(workerRouter), t -> {
                log.info("Router died, notyfing resultGatherer");
                resultGatherer.tell("finished", this.getSelf());
                this.context().stop(this.getSelf());
            });

        // Match anything
        builder.matchAny(o -> log.info("Recieved unknown message"));

        return builder.build();
    }

    // Startup
    @Override
    public void preStart() {
        log.info("MasterManager actor started!");
    }

    // Exit message
    @Override
    public void postStop() {
        log.info("MasterManager actor stopped!");
    }

}
