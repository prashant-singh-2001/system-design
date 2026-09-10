package sd.p05.day45;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * GIVEN - a dynamic proxy around a real {@link Connection} that counts every call to {@code
 * prepareStatement}. Both repositories in this package issue exactly one {@code
 * prepareStatement} call per round trip, so this is an exact, no-guessing measurement of how
 * many queries a method actually sent to the database.
 */
final class CountingConnection {

    private CountingConnection() {
    }

    static Connection wrap(Connection delegate, AtomicInteger counter) {
        InvocationHandler handler = (proxy, method, args) -> {
            if (method.getName().equals("prepareStatement")) {
                counter.incrementAndGet();
            }
            try {
                return method.invoke(delegate, args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        };
        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(), new Class<?>[]{Connection.class}, handler);
    }
}
