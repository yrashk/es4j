package com.eventsourcing.cep.protocols;

import com.eventsourcing.Command;
import com.eventsourcing.Event;
import com.eventsourcing.Model;
import com.eventsourcing.Repository;
import com.eventsourcing.cep.events.NameChanged;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.testng.annotations.Test;

import java.util.UUID;
import java.util.stream.Stream;

import static org.testng.Assert.*;

public class NameProtocolTest extends RepositoryTest {

    public NameProtocolTest() {
        super(NameChanged.class.getPackage(), NameProtocolTest.class.getPackage());
    }

    @Accessors(fluent = true)
    public static class Rename extends Command<String> {

        @Getter @Setter
        private UUID id;
        @Getter @Setter
        private String name;

        @Override
        public Stream<Event> events(Repository repository) throws Exception {
            return Stream.of(new NameChanged().reference(id).name(name));
        }

        @Override
        public String onCompletion() {
            return name;
        }
    }

    @Accessors(fluent = true)
    public static class TestModel implements Model, NameProtocol {

        @Getter @Accessors(fluent = false)
        private final Repository repository;

        @Getter
        private final UUID id;

        public TestModel(Repository repository, UUID id) {
            this.repository = repository;
            this.id = id;
        }

    }

    @Test(invocationCount = 20)
    @SneakyThrows
    public void renaming() {
        TestModel model = new TestModel(repository, UUID.randomUUID());
        Rename rename = new Rename().id(model.id()).name("Name #1");
        repository.publish(rename).get();
        assertEquals(model.name(), "Name #1");
        rename = new Rename().id(model.id()).name("Name #2");
        repository.publish(rename).get();
        assertEquals(model.name(), "Name #2");
    }
}