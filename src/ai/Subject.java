package ai;

/**
 * Giao diện Subject để gửi các sự kiện cho các Observer.
 */
public interface Subject {
    void attach(Observer observer);
    void detach(Observer observer);
    void notifyObservers(Event event);
}
