/*
 * Copyright 2015 Workday, Inc.
 *
 * This software is available under the MIT license.
 * Please see the LICENSE.txt file in this project.
 */

package com.workday.autoparse.xml.context;

import com.workday.autoparse.xml.annotations.XmlParserPartition;
import com.workday.autoparse.xml.parser.GeneratedClassNames;
import com.workday.autoparse.xml.parser.ParserMap;
import com.workday.autoparse.xml.parser.XmlElementParser;
import com.workday.autoparse.xml.parser.XmlStreamParser;
import com.workday.autoparse.xml.utils.StringTransformer;
import com.workday.meta.ConcreteTypeNames;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author nathan.taylor
 * @since 2013-9-23-11:52
 */
public class XmlParserSettingsBuilder {

    public static final String DEFAULT_PACKAGE = ParserMap.class.getPackage().getName();

    private Class<?> unknownElementClass;
    private XmlElementParser<?> unknownElementParser;
    private XmlParserSettings.UnknownElementHandling unknownElementHandling =
            XmlParserSettings.UnknownElementHandling.PARSE;
    private boolean ignoreUnexpectedChildren;
    private List<String> partitionPackages = new ArrayList<>();
    private List<StringTransformer> stringFilters = new ArrayList<>();

    /**
     * Use the given {@link XmlParserSettings.UnknownElementHandling} when parsing. This value
     * defaults to {@link XmlParserSettings.UnknownElementHandling#PARSE}.
     *
     * @param unknownElementHandling The unknown element handling strategy to use.
     *
     * @return This SettingsBuilder.
     */
    public XmlParserSettingsBuilder withUnknownElementHandling(XmlParserSettings
                                                                       .UnknownElementHandling
                                                                       unknownElementHandling) {
        this.unknownElementHandling = unknownElementHandling;
        return this;
    }

    /**
     * If {@code ignoreUnexpectedChildren} is {@code true}, then when the autoparse encounters a
     * child element for which there is no field in the parent class annotated with {@link
     * com.workday.autoparse.xml.annotations.XmlChildElement}, the element will be skipped.
     * <p/>
     * If {@code ignoreUnexpectedChildren} is {@code false}, then when the autoparse encounters a
     * child element for which there is no field in the parent class annotated with {@link
     * com.workday.autoparse.xml.annotations.XmlChildElement}, then an {@link
     * com.workday.autoparse.xml.parser.UnexpectedChildException} will be thrown.
     *
     * @param ignoreUnexpectedChildren {@code true} if undeclared children will be ignored, {@code
     * false} if an exception should be thrown when an undeclared child is encountered.
     *
     * @return This SettingsBuilder.
     */
    public XmlParserSettingsBuilder ignoreUnexpectedChildren(boolean ignoreUnexpectedChildren) {
        this.ignoreUnexpectedChildren = ignoreUnexpectedChildren;
        return this;
    }

    public <T> XmlParserSettingsBuilder withUnknownElementParser(XmlElementParser<T>
                                                                         unknownElementParser,
                                                                 Class<T> unknownElementType) {
        this.unknownElementParser = unknownElementParser;
        this.unknownElementClass = unknownElementType;
        return this;
    }

    /**
     * You can use this method to set the unknown element parser if that parser was generated by
     * Autoparse.
     *
     * @param unknownObjectClass The type of object that will be created when an unknown element tag
     * is encountered in the XML document. This class must be annotated with {@literal@}{@link
     * com.workday.autoparse.xml.annotations.XmlUnknownElement}.
     */
    public XmlParserSettingsBuilder withUnknownElementClass(Class<?> unknownObjectClass) {
        this.unknownElementClass = unknownObjectClass;
        String parserName = ConcreteTypeNames.constructClassName(unknownObjectClass,
                                                                 GeneratedClassNames.PARSER_SUFFIX);
        try {
            @SuppressWarnings("unchecked")
            Class<? extends XmlElementParser<?>> parserClass =
                    (Class<? extends XmlElementParser<?>>) Class.forName(
                            parserName);
            Object instance = parserClass.getDeclaredField("INSTANCE").get(null);
            if (!(instance instanceof XmlElementParser)) {
                throw new RuntimeException(
                        "Public constant INSTANCE must be of type XmlElementParser");
            }
            unknownElementParser = (XmlElementParser<?>) instance;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("No XmlElementParser found for class of type "
                                               + unknownObjectClass, e);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Parser does not have static field named INSTANCE.", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Field INSTANCE is not public.", e);
        } catch (NullPointerException e) {
            throw new RuntimeException("Field INSTANCE is not static", e);
        }
        return this;
    }

    /**
     * Indicate which partitions the {@link XmlStreamParser} should use. If no partitions are
     * indicated, the default partition is assumed. To include explicit partitions and the default
     * partition, include {@link #DEFAULT_PACKAGE} in the list.
     * <p/>
     * When the {@link XmlParserSettings} instance is created, a validation is performed to ensure
     * that there are no name collisions (multiple classes mapping to the same XML element tag)
     * among the partitions. If there are, an {@link IllegalArgumentException} is thrown.
     *
     * @param partitionPackages The fully qualified names of the packages that hold the partitions.
     * These are packages annotated with {@literal@}{@link XmlParserPartition}.
     *
     * @return This SettingsBuilder.
     */
    public XmlParserSettingsBuilder withPartitions(String... partitionPackages) {
        this.partitionPackages.addAll(Arrays.asList(partitionPackages));
        return this;
    }

    /**
     * Add a new collection of filters that will be applied when parsing string attributes and text
     * content of elements. Filters are applied in the order they are added to all elements parsed.
     *
     * @param stringFilters The filters to be applied to all string attributes and text content.
     *
     * @return This SettingsBuilder.
     *
     * @see #addFilter(StringTransformer)
     */
    public XmlParserSettingsBuilder addFilters(Collection<StringTransformer> stringFilters) {
        this.stringFilters.addAll(stringFilters);
        return this;
    }

    /**
     * Add a new filter that will be applied when parsing string attributes and text content of
     * elements. Filters are applied in the order they are added to all elements parsed.
     * <p/>
     * For example, if you want replace the character "&amp;#xa;" with "\n" you can create a {@link
     * StringTransformer} to do that and add it here.
     * <pre>
     *      StringTransformer newLineFilter = new StringTransformer() {
     *          {@literal@}Override
     *          public String apply(String input) {
     *              return input.replace('&amp;#xa;', '\n');
     *      };
     * </pre>
     *
     * @param stringFilter The filter to be applied to all string attributes and text content.
     *
     * @return This SettingsBuilder.
     */
    public XmlParserSettingsBuilder addFilter(StringTransformer stringFilter) {
        stringFilters.add(stringFilter);
        return this;
    }

    /**
     * Creates a new instance of {@link XmlParserSettings} with the specified preferences set.
     *
     * @return The new Settings instance.
     */
    public XmlParserSettings build() {
        if (partitionPackages.isEmpty()) {
            partitionPackages.add(DEFAULT_PACKAGE);
        }
        return new XmlParserSettings(unknownElementHandling,
                                     ignoreUnexpectedChildren,
                                     unknownElementClass,
                                     unknownElementParser,
                                     stringFilters,
                                     partitionPackages);
    }
}