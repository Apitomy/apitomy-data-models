package io.apitomy.datamodels.refs;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import io.apitomy.datamodels.models.Node;

public class ReferenceResolverChainTest {

    private final List<IReferenceResolver> addedResolvers = new ArrayList<>();

    @After
    public void cleanup() {
        ReferenceResolverChain chain = ReferenceResolverChain.getInstance();
        for (IReferenceResolver resolver : addedResolvers) {
            chain.removeResolver(resolver);
        }
        addedResolvers.clear();
    }

    /**
     * Adds a resolver to the singleton chain and tracks it for cleanup.
     */
    private void addResolver(IReferenceResolver resolver) {
        ReferenceResolverChain.getInstance().addResolver(resolver);
        addedResolvers.add(resolver);
    }

    @Test
    public void testAddAndResolve() {
        IReferenceResolver custom = (reference, from) -> {
            if ("custom://test".equals(reference)) {
                return ResolvedReference.fromNode(from);
            }
            return null;
        };
        addResolver(custom);

        ResolvedReference result = ReferenceResolverChain.getInstance()
                .resolveRef("custom://test", null);
        Assert.assertNull("Custom resolver should return fromNode(null) which wraps null",
                result.asNode());
    }

    @Test
    public void testRemoveResolver() {
        IReferenceResolver custom = (reference, from) -> {
            if ("custom://remove-test".equals(reference)) {
                return ResolvedReference.fromNode(from);
            }
            return null;
        };
        addResolver(custom);

        ResolvedReference before = ReferenceResolverChain.getInstance()
                .resolveRef("custom://remove-test", null);
        Assert.assertNotNull("Resolver should match before removal", before);

        ReferenceResolverChain.getInstance().removeResolver(custom);
        addedResolvers.remove(custom);

        ResolvedReference after = ReferenceResolverChain.getInstance()
                .resolveRef("custom://remove-test", null);
        Assert.assertNull("Resolver should not match after removal", after);
    }

    @Test
    public void testCustomResolverTakesPrecedence() {
        final String sentinel = "custom-sentinel";
        IReferenceResolver custom = (reference, from) -> {
            if (sentinel.equals(reference)) {
                return ResolvedReference.fromNode(from);
            }
            return null;
        };
        addResolver(custom);

        ResolvedReference result = ReferenceResolverChain.getInstance()
                .resolveRef(sentinel, null);
        Assert.assertNotNull("Custom resolver should be consulted before LocalReferenceResolver",
                result);
    }

    @Test
    public void testGetResolversIsUnmodifiable() {
        List<IReferenceResolver> resolvers = ReferenceResolverChain.getInstance().getResolvers();
        try {
            resolvers.add((ref, from) -> null);
            Assert.fail("getResolvers() should return an unmodifiable list");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void testConcurrentAddAndResolve() throws Exception {
        int threadCount = 8;
        int iterationsPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CyclicBarrier barrier = new CyclicBarrier(threadCount);

        List<Future<?>> futures = new ArrayList<>();
        for (int t = 0; t < threadCount; t++) {
            final int threadIdx = t;
            futures.add(executor.submit(() -> {
                try {
                    barrier.await(5, TimeUnit.SECONDS);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                for (int i = 0; i < iterationsPerThread; i++) {
                    if (threadIdx % 2 == 0) {
                        IReferenceResolver r = (ref, from) -> null;
                        ReferenceResolverChain.getInstance().addResolver(r);
                        ReferenceResolverChain.getInstance().removeResolver(r);
                    } else {
                        ReferenceResolverChain.getInstance()
                                .resolveRef("nonexistent://ref", null);
                    }
                }
            }));
        }

        executor.shutdown();
        Assert.assertTrue("Executor should terminate in time",
                executor.awaitTermination(30, TimeUnit.SECONDS));

        for (Future<?> future : futures) {
            future.get();
        }
    }
}
