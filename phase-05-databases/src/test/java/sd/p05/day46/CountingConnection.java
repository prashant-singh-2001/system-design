package sd.p05.day46;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.util.concurrent.atomic.AtomicInteger;

/** GIVEN - same dynamic-proxy query counter as Day 45, duplicated because each day stands alone. */
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
