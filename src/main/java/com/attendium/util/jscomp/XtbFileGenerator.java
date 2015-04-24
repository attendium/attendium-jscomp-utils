package com.attendium.util.jscomp;

import com.google.javascript.jscomp.GoogleJsMessageIdGenerator;
import com.google.javascript.jscomp.JsMessage;
import org.apache.commons.lang3.StringEscapeUtils;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.ExampleMode;
import org.kohsuke.args4j.Option;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

public class XtbFileGenerator {

    private static final String BUNDLE_ELEM_NAME = "translationbundle",
            LANG_ATT_NAME = "lang",
            TRANSLATION_ELEM_NAME = "translation",
            MESSAGE_ID_ATT_NAME = "id";

    @Option(name = "--source",
            required = true,
            usage = "Source of translated messages in .properties format")
    private String source;

    @Option(name = "--target",
            required = true,
            usage = "Target of translated messages in .xtb format")
    private String target;

    @Option(name = "--base",
            required = true,
            usage = "Base of translated messages in .properties format. These are the same messages and meanings as in the source code so that the same ids are generated")
    private String base;

    @Option(name = "--lang",
            required = true,
            usage = "Language of translations")
    private String lang;

    @Option(name = "--project",
            required = true,
            usage = "Project id of translations")
    private String projectId;

    private boolean runnable = false;

    public XtbFileGenerator(final String[] args) {

        final CmdLineParser parser = new CmdLineParser(this);

        try {

            parser.parseArgument(args);
            runnable = true;
        } catch(final CmdLineException e ) {

            parser.printUsage(System.err);

            return;
        }
    }

    public boolean isRunnable() {

        return runnable;
    }

    public void run() throws FileNotFoundException, IOException {

        final Properties sourceProperties = new Properties(), baseProperties = new Properties();
        sourceProperties.load(new FileInputStream(source));
        baseProperties.load(new FileInputStream(base));

        final BufferedWriter writer = Files.newBufferedWriter(Paths.get(target), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        final JsMessage.IdGenerator idGenerator = new GoogleJsMessageIdGenerator(projectId);

        writer.write("<" + BUNDLE_ELEM_NAME + " " + LANG_ATT_NAME + "=\"" + lang + "\"" + ">");

        final Enumeration<?> names = sourceProperties.propertyNames();
        while (names.hasMoreElements()) {

            final String key = (String)names.nextElement(),
                    baseValue = baseProperties.getProperty(key),
                    sourceValue = sourceProperties.getProperty(key),
                    meaning = "MSG_" + key.toUpperCase();
            final List<CharSequence> baseMessageParts = Arrays.<CharSequence>asList(baseValue);
            final String id = idGenerator.generateId(meaning, baseMessageParts);

            writer.newLine();
            writer.write("<" + TRANSLATION_ELEM_NAME + " " + MESSAGE_ID_ATT_NAME + "=\"" + StringEscapeUtils.escapeXml(id) + "\">" + StringEscapeUtils.escapeXml(sourceValue) + "</" + TRANSLATION_ELEM_NAME + ">");
        }

        writer.newLine();
        writer.write("</" + BUNDLE_ELEM_NAME + ">");
        writer.close();
    }

    public static void main(final String[] args) throws FileNotFoundException, IOException {

        XtbFileGenerator generator = new XtbFileGenerator(args);

        if (generator.isRunnable()) generator.run();
        else System.exit(-1);
    }
}
