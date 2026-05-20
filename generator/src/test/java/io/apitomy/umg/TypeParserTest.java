package io.apitomy.umg;

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import io.apitomy.umg.models.concept.PropertyType;

public class TypeParserTest {

    @Test
    public void run() throws Exception {

        PropertyType.parse("foo");
        PropertyType.parse("foo|bar");
        PropertyType.parse("foo|bar|baz");
        PropertyType.parse("{foo|bar}");
        PropertyType.parse("{{foo|bar}}");
        PropertyType.parse("{[foo]|{bar}|baz}|quox");

        Assert.assertEquals(
                PropertyType.builder().simple(true).simpleType("foo").build(),
                PropertyType.parse("foo"));

        Assert.assertEquals(
                PropertyType.builder().union(true).nested(Set.of(
                        PropertyType.builder().simple(true).simpleType("foo").build(),
                        PropertyType.builder().simple(true).simpleType("bar").build()
                        )).build(),
                PropertyType.parse("foo|bar"));

        Assert.assertEquals(
                PropertyType.builder().union(true).nested(Set.of(
                        PropertyType.builder().simple(true).simpleType("foo").build(),
                        PropertyType.builder().simple(true).simpleType("bar").build(),
                        PropertyType.builder().simple(true).simpleType("baz").build()
                        )).build(),
                PropertyType.parse("foo|bar|baz"));

        Assert.assertEquals(
                PropertyType.builder().map(true).nested(Set.of(
                        PropertyType.builder().union(true).nested(Set.of(
                                PropertyType.builder().simple(true).simpleType("foo").build(),
                                PropertyType.builder().simple(true).simpleType("bar").build()
                                )).build()
                        )).build(),
                PropertyType.parse("{foo|bar}"));

        Assert.assertEquals(
                PropertyType.builder().map(true).nested(Set.of(
                        PropertyType.builder().map(true).nested(Set.of(
                                PropertyType.builder().union(true).nested(Set.of(
                                        PropertyType.builder().simple(true).simpleType("foo").build(),
                                        PropertyType.builder().simple(true).simpleType("bar").build()
                                        )).build()
                                )).build()
                        )).build(),
                PropertyType.parse("{{foo|bar}}"));

        Assert.assertEquals(
                PropertyType.builder().union(true).nested(Set.of(
                        PropertyType.builder().map(true).nested(Set.of(
                                PropertyType.builder().union(true).nested(Set.of(
                                        PropertyType.builder().list(true).nested(Set.of(
                                                PropertyType.builder().simple(true).simpleType("foo").build()
                                                )).build(),
                                        PropertyType.builder().map(true).nested(Set.of(
                                                PropertyType.builder().simple(true).simpleType("bar").build()
                                                )).build(),
                                        PropertyType.builder().simple(true).simpleType("baz").build()
                                        )).build()
                                )).build(),
                        PropertyType.builder().simple(true).simpleType("quox").build()
                        )).build(),
                PropertyType.parse("{[foo]|{bar}|baz}|quox"));
    }
}
