package megalodonte.v2;

import megalodonte.base.state.ReadableState;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ListStateFilterTest {

    @Test
    void createsReactiveFilteredView() {
        ListState<Integer> source = ListState.of(List.of(1, 2, 3));
        ReadableState<List<Integer>> even = source.filter(value -> value % 2 == 0);
        List<List<Integer>> notifications = new ArrayList<>();

        even.subscribe(notifications::add);
        source.add(4);
        source.remove(2);

        assertEquals(List.of(4), even.get());
        assertEquals(List.of(List.of(2), List.of(2, 4), List.of(4)), notifications);
    }

    @Test
    void rejectsNullPredicate() {
        ListState<Integer> source = ListState.ofEmpty();

        assertThrows(NullPointerException.class, () -> source.filter(null));
    }
}
