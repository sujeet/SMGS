
package alviz2.util;

import java.util.Map;
import java.util.EnumMap;

public class StateMachine<E extends Enum> {
	private static class StatePair<E extends Enum> {
		private StateHandler<E> enter;
		private StateHandler<E> leave;

		private StatePair(StateHandler<E> e, StateHandler<E> l) {
			enter = e;
			leave = l;
		}
	}

	public interface StateHandler<E extends Enum> {
		public void run(E state);
	}

	public static class StateMachineBuilder<E extends Enum> {
		private Map<E, StatePair<E>> table;
		private Class<E> enmClass;

		public StateMachineBuilder(Class<E> enm) {
			table = new EnumMap<>(enm);
			enmClass = enm;
		}

		public StateMachineBuilder<E> add(E k, StateHandler<E> enter, StateHandler<E> leave) {
			table.put(k, new StatePair<>(enter, leave));
			return this;
		}

		public StateMachine<E> build(E initialState) {
			for (E e : enmClass.getEnumConstants()) {
				if (!table.containsKey(e)) {
					throw new IllegalStateException();
				}
			}

			return new StateMachine<E>(table, initialState);
		}
	}

	private Map<E, StatePair<E>> stateTable;
	private E currentState;

	private StateMachine(Map<E, StatePair<E>> table, E initialState) {
		stateTable = table;
		currentState = initialState;
	}

	public E getState() {
		return currentState;
	}

	public void setState(E state) {
		stateTable.get(currentState).leave.run(currentState);
		E prevState = currentState;
		currentState = state;
		stateTable.get(currentState).enter.run(prevState);
	}
}