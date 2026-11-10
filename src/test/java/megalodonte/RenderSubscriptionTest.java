package megalodonte;

import megalodonte.base.state.State;
import megalodonte.base.state.SubscriptionScope;
import megalodonte.v2.ListState;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RenderSubscriptionTest {
    @Test void closesOnlyRenderSubscriptionsWhilePreservingViewModelComputation() {
        int baseline = ListenerManager.getListenerCount();
        State<Integer> source = State.of(2);
        var computed = ComputedState.of(() -> source.get() * 2, source);
        var list = ListState.of(List.of("first"));
        var values = new ArrayList<Integer>();
        var lists = new ArrayList<List<String>>();
        var render = SubscriptionScope.capture(() -> {
            computed.subscribe(values::add);
            list.subscribe(lists::add);
            return "tree";
        });
        assertEquals(baseline + 2, ListenerManager.getListenerCount());
        render.scope().close();
        source.set(3);
        list.add("second");
        assertEquals(List.of(4), values);
        assertEquals(1, lists.size());
        assertEquals(6, computed.get());
        assertEquals(baseline, ListenerManager.getListenerCount());
        computed.close();
    }
}
