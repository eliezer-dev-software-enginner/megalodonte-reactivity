package megalodonte;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ListenerManager {

    private record Entry(Consumer<?> listener, Runnable cleanup) { }
    private static final List<Entry> listeners = new ArrayList<>();
    //private static int disposeCount = 0;

    public static void register(Consumer<?> listener) {
        register(listener, () -> {});
    }
    public static void register(Consumer<?> listener, Runnable cleanup) { listeners.add(new Entry(listener, cleanup)); }

    public static boolean unregister(Consumer<?> listener) {
        for (int i = 0; i < listeners.size(); i++) {
            if (listeners.get(i).listener() == listener) { listeners.remove(i); return true; }
        }
        return false;
    }

    public static int getListenerCount() {
        return listeners.size();
    }

    public static void disposeAll() {
        //int countBefore = listeners.size();
        //System.out.println("[" + (++disposeCount) + "] Sem dar dispose, a aplicacao esta consumindo " + countBefore + " listeners ainda abertos");
        
        List<Entry> snapshot = List.copyOf(listeners);
        listeners.clear();
        snapshot.forEach(entry -> entry.cleanup().run());
        
        //System.out.println("[" + disposeCount + "] Após o dispose, a aplicacao agora tem 0 listeners abertos");
    }
}
