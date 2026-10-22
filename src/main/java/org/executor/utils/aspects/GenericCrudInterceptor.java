package org.executor.utils.aspects;

import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;

import java.lang.reflect.Method;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * CDI equivalent of Spring {@code GenericCrudAspect}. Subclass, override the
 * {@code around*} methods, and register the subclass as an interceptor bound
 * with {@link GenericCrudIntercepted}.
 * <p>
 * Method groups match the Spring pointcuts: find, create, overwrite, delete.
 * Advice runs once per external call, as with Spring proxies: a nested call on the
 * same bean instance (for example {@code createResource} delegating to {@code create}
 * through {@code transactionalSelf()}) skips the {@code around*} methods, while a
 * call to another CRUD service is advised normally.
 */
public abstract class GenericCrudInterceptor {

    /** Per thread: the bean instances with an advised call in progress, and that call's method. */
    private static final ThreadLocal<Map<Object, Method>> ADVISED_CALLS =
            ThreadLocal.withInitial(IdentityHashMap::new);

    @AroundInvoke
    public Object intercept(InvocationContext context) throws Exception {
        Map<Object, Method> advisedCalls = ADVISED_CALLS.get();
        Object target = context.getTarget();
        Method outer = advisedCalls.get(target);
        if (outer != null && !outer.equals(context.getMethod())) {
            return context.proceed();
        }
        boolean root = outer == null;
        if (root) {
            advisedCalls.put(target, context.getMethod());
        }
        try {
            return dispatch(context);
        } finally {
            if (root) {
                advisedCalls.remove(target);
            }
        }
    }

    private Object dispatch(InvocationContext context) throws Exception {
        String name = context.getMethod().getName();
        if (isFind(name)) {
            return aroundFind(context);
        }
        if (isCreate(name)) {
            return aroundCreate(context);
        }
        if (isOverwrite(name)) {
            return aroundOverwrite(context);
        }
        if (isDelete(name)) {
            return aroundDelete(context);
        }
        return context.proceed();
    }

    protected Object aroundFind(InvocationContext context) throws Exception {
        return context.proceed();
    }

    protected Object aroundCreate(InvocationContext context) throws Exception {
        return context.proceed();
    }

    protected Object aroundOverwrite(InvocationContext context) throws Exception {
        return context.proceed();
    }

    protected Object aroundDelete(InvocationContext context) throws Exception {
        return context.proceed();
    }

    private static boolean isFind(String name) {
        return "findOne".equals(name)
                || "findAll".equals(name)
                || "findOneResource".equals(name)
                || "findAllResources".equals(name)
                || "findAllResourcesFiltered".equals(name)
                || "findAllFiltered".equals(name);
    }

    private static boolean isCreate(String name) {
        return "create".equals(name) || "createResource".equals(name);
    }

    private static boolean isOverwrite(String name) {
        return "overwrite".equals(name) || "overwriteResource".equals(name);
    }

    private static boolean isDelete(String name) {
        return "delete".equals(name)
                || "deleteReturning".equals(name)
                || "deleteReturningResource".equals(name);
    }
}
