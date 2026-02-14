package megalodonte;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ListenerManager {

    private static final List<Consumer<?>> listeners = new ArrayList<>();
    private static int disposeCount = 0;

    public static void register(Consumer<?> listener) {
        listeners.add(listener);
    }

    public static boolean unregister(Consumer<?> listener) {
        return listeners.remove(listener);
    }

    public static int getListenerCount() {
        return listeners.size();
    }

    public static void disposeAll() {
        int countBefore = listeners.size();
        System.out.println("[" + (++disposeCount) + "] Sem dar dispose, a aplicacao esta consumindo " + countBefore + " listeners ainda abertos");
        
        listeners.clear();
        
        System.out.println("[" + disposeCount + "] Após o dispose, a aplicacao agora tem 0 listeners abertos");
    }
}
