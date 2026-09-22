package dev.incendiary.api;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class Event<T> {

    private final List<Consumer<T>> listeners = new ArrayList<>();

    public void register(Consumer<T> listener) {
        listeners.add(listener);
    }

    public void fire(T payload) {
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).accept(payload);
        }
    }
}