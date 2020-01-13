import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.lang.reflect.Type;
import java.time.Duration;
import java.util.*;
import java.io.IOException;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import akka.actor.*;
import com.google.gson.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import scala.concurrent.Await;
import scala.concurrent.Future;


class ExamMain {
    public static void main(String[] args) throws InterruptedException
    {
        final String dataFile = "data/IFF7-4_ValinskisV_E1_dat_1.json";
        final ActorSystem system = ActorSystem.create("ActorSystem"); // Create actor system

        System.out.println("ActorSystem created!");

        // Read json data form file
        System.out.println("Reading JSON data...");
        List<Person> people = new ArrayList<>();
        Gson gson = new Gson();
        try (Reader reader = new FileReader(dataFile)) {
            Type listType = new TypeToken<List<Person>>() {}.getType(); // Get list type
            people = gson.fromJson(reader, listType); // Convert JSON File to Java Object

        } catch (IOException e) {
            System.out.println("Error reading file!");
            System.exit(-1);
        }
        System.out.println("Done reading JSON!");

        long startTime = System.nanoTime(); // Current time

        // Create MasterManager actor
        ActorRef masterManagerActor = system.actorOf(Props.create(MasterManager.class), "MasterManager");

        // Send all people form array, single person at a time
        for(Person p: people)
        {
            masterManagerActor.tell(p, ActorRef.noSender());
        }

        System.out.println("Finished sending people, sending finished flag...");
        // Send "finished" message master manager to notify about end
        masterManagerActor.tell("finished", ActorRef.noSender());

        // Wait for complete actor system termination
        Future<Terminated> done = system.whenTerminated();
        do
        {
          Thread.sleep(10);
        } while (!done.isCompleted());

        // Get elapsed miliseconds
        long endTime = System.nanoTime();
        double elapsed = (endTime - startTime) / 1e6;

        System.out.println(String.format("Program finished all work in %.2fms!", elapsed));
    }
}



//        Scanner myObj = new Scanner(System.in);
//        String name;
//        Person person = new Person("John", 65.6, 55);
//
//
//        // Enter username and press Enter
//        System.out.println(Person.tableHeader());
//        person.calculateHash(1_000_000);
//        System.out.println(person);
//
//        Gson gson = new GsonBuilder().setPrettyPrinting().create();
//
//        List<Person> people = Collections.nCopies(5, person);
//
//        try (FileWriter writer = new FileWriter("IFF7-4_ValinskisV_E1_res.json")) {
//            gson.toJson(people, writer);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
